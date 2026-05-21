#!/usr/bin/env node
'use strict';

const { spawnSync } = require('child_process');
const { mkdirSync, existsSync, createWriteStream } = require('fs');
const { join, dirname, parse } = require('path');
const { Readable } = require('stream');
const { pipeline } = require('stream/promises');

const fetchImpl = typeof globalThis.fetch === 'function'
    ? globalThis.fetch
    : null;

function findNearestNodeModules(startDir) {
    let dir = startDir;
    const { root } = parse(startDir);
    while (true) {
        const candidate = join(dir, 'node_modules');
        if (existsSync(candidate)) {
            return candidate;
        }
        if (dir === root) {
            throw new Error('Could not find a node_modules directory.');
        }
        dir = dirname(dir);
    }
}

const NODE_MODULES_DIR = findNearestNodeModules(__dirname);
const CACHE_DIR = join(NODE_MODULES_DIR, '.cache', 'vnu-jar', 'java');
const TEMURIN_VERSION = '17.0.17+10';
const TEMURIN_BASE_URL = `https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${TEMURIN_VERSION}/`;
const MAX_FETCH_ATTEMPTS = 3;

// Builds a readable description of an error, walking the chain of cause
// errors that the fetch API attaches. The real reason for a failed fetch
// (a DNS failure, a refused connection, a TLS or proxy error) is carried
// on err.cause, not in the generic top-level message.
function describeError(err) {
    if (!err) {
        return 'unknown error';
    }
    let description = err.message || String(err);
    const seen = new Set([err]);
    let cause = err.cause;
    while (cause && !seen.has(cause)) {
        seen.add(cause);
        description += ` (cause: ${cause.code || cause.message || cause})`;
        cause = cause.cause;
    }
    return description;
}

// Returns a proxy URL from the npm or shell environment, or null. The
// built-in fetch ignores these variables; curl honors them, so the curl
// fallback forwards whatever this finds as an explicit --proxy argument.
function getProxyFromEnv() {
    const env = process.env;
    return env.npm_config_https_proxy
        || env.npm_config_proxy
        || env.HTTPS_PROXY
        || env.https_proxy
        || env.HTTP_PROXY
        || env.http_proxy
        || env.ALL_PROXY
        || env.all_proxy
        || null;
}

function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function getJavaVersion(javaPath) {
    try {
        const result = spawnSync(javaPath, ['-version'], { encoding: 'utf8' });
        const versionOutput = result.stderr || result.stdout || '';
        const match = versionOutput.match(/version "(\d+)(?:\.(\d+))?/);
        if (match) {
            return parseInt(match[1], 10);
        }
    } catch (err) {
        return null;
    }
    return null;
}

function findSystemJava() {
    const whichCmd = process.platform === 'win32' ? 'where' : 'which';
    const result = spawnSync(whichCmd, ['java'], { encoding: 'utf8' });
    if (result.status === 0 && result.stdout) {
        const javaPath = result.stdout.split(/\r?\n/)[0].trim();
        const version = getJavaVersion(javaPath);
        if (version !== null && version >= 11) {
            return javaPath;
        }
    }
    return null;
}

function getPlatformArchiveName() {
    let arch = process.arch;
    let plat = 'linux';
    if (process.platform === 'win32') {
        plat = 'windows';
    } else if (process.platform === 'darwin') {
        plat = 'mac';
    }
    if (plat === 'mac' && arch === 'arm64') {
        arch = 'aarch64';
    }
    const ext = process.platform === 'win32' ? 'zip' : 'tar.gz';
    const version = TEMURIN_VERSION.replace('+', '_');
    return `OpenJDK17U-jre_${arch}_${plat}_hotspot_${version}.${ext}`;
}

// Path of the java executable inside an extracted Temurin runtime.
function getLocalJavaExecutablePath() {
    const home = join(CACHE_DIR, `jdk-${TEMURIN_VERSION}-jre`);
    if (process.platform === 'win32') {
        return join(home, 'bin', 'java.exe');
    }
    if (process.platform === 'darwin') {
        return join(home, 'Contents', 'Home', 'bin', 'java');
    }
    return join(home, 'bin', 'java');
}

// Returns the cached java executable path, or null if none is present.
// The check targets the executable itself, not the cache directory: an
// interrupted download can leave the directory in place with no runtime
// inside it, and that must count as not installed.
function findCachedJava() {
    const javaPath = getLocalJavaExecutablePath();
    return existsSync(javaPath) ? javaPath : null;
}

// Downloads url to destPath with the built-in fetch, retrying network
// failures a few times. A non-OK HTTP response is not retried, since a
// bad status will not change between attempts.
async function downloadWithFetch(url, destPath) {
    let lastError;
    for (let attempt = 1; attempt <= MAX_FETCH_ATTEMPTS; attempt++) {
        try {
            const res = await fetchImpl(url);
            if (!res.ok) {
                throw new Error(
                    `server responded ${res.status} ${res.statusText}`);
            }
            await pipeline(Readable.fromWeb(res.body),
                createWriteStream(destPath));
            return;
        } catch (err) {
            lastError = err;
            // Network failures from fetch carry a cause; the non-OK HTTP
            // error thrown above does not. Only network failures retry.
            const transient = err.cause !== undefined;
            if (!transient || attempt === MAX_FETCH_ATTEMPTS) {
                throw err;
            }
            const waitMs = 1000 * attempt;
            console.log(`Download attempt ${attempt} failed `
                + `(${describeError(err)}); retrying in ${waitMs} ms ...`);
            await delay(waitMs);
        }
    }
    throw lastError;
}

// Downloads url to destPath by spawning curl. curl honors proxy
// environment variables and the OS trust store, both of which the
// built-in fetch ignores, so it can succeed where fetch cannot.
function downloadWithCurl(url, destPath, proxy) {
    const args = ['--fail', '--show-error', '--location',
        '--retry', '3', '--retry-delay', '2',
        '--output', destPath, url];
    if (proxy) {
        args.push('--proxy', proxy);
    }
    const result = spawnSync('curl', args, { stdio: 'inherit' });
    if (result.error) {
        const err = new Error(`could not run curl: ${result.error.message}`);
        err.cause = result.error;
        throw err;
    }
    if (result.status !== 0) {
        throw new Error(`curl exited with status ${result.status}`);
    }
}

// Downloads url to destPath, preferring the built-in fetch and falling
// back to curl. When a proxy is configured, curl is used directly,
// because the built-in fetch cannot reach the network through a proxy.
async function downloadArchive(url, destPath) {
    const proxy = getProxyFromEnv();
    if (fetchImpl && !proxy) {
        try {
            await downloadWithFetch(url, destPath);
            return;
        } catch (fetchErr) {
            console.log('Built-in download failed '
                + `(${describeError(fetchErr)}); retrying with curl ...`);
            try {
                downloadWithCurl(url, destPath, proxy);
                return;
            } catch (curlErr) {
                // The message below already states both failure reasons in
                // full, so no cause is attached: that would only make
                // describeError repeat them.
                throw new Error('Could not download the Java runtime. '
                    + `Built-in download: ${describeError(fetchErr)}. `
                    + `curl fallback: ${describeError(curlErr)}.`);
            }
        }
    }
    if (proxy) {
        console.log(`Using curl to download (proxy configured: ${proxy}).`);
    } else {
        console.log('Using curl to download (the built-in fetch is '
            + 'unavailable).');
    }
    downloadWithCurl(url, destPath, proxy);
}

function extractArchive(archivePath) {
    if (process.platform === 'win32') {
        console.log('Extracting ZIP archive ...');
        const result = spawnSync('powershell', [
            '-NoProfile',
            '-NonInteractive',
            '-Command',
            'Expand-Archive',
            '-Force',
            archivePath,
            CACHE_DIR
        ], { stdio: 'inherit' });
        if (result.error) {
            throw new Error(
                `could not run PowerShell: ${result.error.message}`);
        }
        if (result.status !== 0) {
            throw new Error('Failed to extract the Java archive.');
        }
    } else {
        console.log('Extracting tar.gz archive ...');
        const result = spawnSync('tar',
            ['-xzf', archivePath, '-C', CACHE_DIR], { stdio: 'inherit' });
        if (result.error) {
            throw new Error(`could not run tar: ${result.error.message}`);
        }
        if (result.status !== 0) {
            throw new Error('Failed to extract the Java archive.');
        }
    }
}

async function downloadJava() {
    mkdirSync(CACHE_DIR, { recursive: true });
    const archiveName = getPlatformArchiveName();
    const url = TEMURIN_BASE_URL + archiveName;
    const archivePath = join(CACHE_DIR, archiveName);
    console.log(`Downloading a Java runtime from ${url} ...`);
    await downloadArchive(url, archivePath);
    extractArchive(archivePath);
    console.log('Local Java runtime now installed.');
}

// Resolves a usable java executable: a system Java 11 or later if one is
// present, otherwise a runtime from an earlier download, otherwise a
// freshly downloaded one.
async function resolveJava() {
    const systemJava = findSystemJava();
    if (systemJava) {
        return systemJava;
    }
    const cachedJava = findCachedJava();
    if (cachedJava) {
        return cachedJava;
    }
    await downloadJava();
    const javaPath = findCachedJava();
    if (!javaPath) {
        throw new Error('The Java runtime was not found after download and '
            + `extraction; expected it at: ${getLocalJavaExecutablePath()}`);
    }
    return javaPath;
}

if (require.main === module) {
    // Invoked as the npm postinstall script. A failed download must not
    // abort npm install: vnu.jar itself is already in place, and
    // resolveJava() downloads Java on first use when it is needed.
    resolveJava().catch((err) => {
        console.warn(
            'vnu-jar: could not install a Java runtime during postinstall.');
        console.warn(`vnu-jar: reason: ${describeError(err)}`);
        console.warn('vnu-jar: this is not fatal. A Java runtime will be '
            + 'downloaded the first time you run vnu, or you can install '
            + 'Java 11 or later yourself and put it on your PATH.');
        // process.exit() is intentionally not called here. Calling it while
        // fetch sockets are still closing aborts Node with a libuv
        // assertion on Windows; letting this callback return lets the event
        // loop close those handles and then exit with status 0.
    });
}

module.exports = {
    resolveJava,
    describeError,
    getProxyFromEnv,
    getPlatformArchiveName,
    getLocalJavaExecutablePath,
    findCachedJava,
};
