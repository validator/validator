#!/usr/bin/env node
'use strict';

const { spawnSync, execSync } = require('child_process');
const { mkdirSync, existsSync, createWriteStream } = require('fs');
const { join, dirname, parse } = require('path');
const { Readable } = require('stream');

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
const CACHE_DIR = join( NODE_MODULES_DIR, '.cache', 'vnu-jar', 'java');
const TEMURIN_VERSION = '17.0.17+10'
const TEMURIN_BASE_URL = `https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${TEMURIN_VERSION}/`;

function findSystemJava() {
    const whichCmd = process.platform === 'win32' ? 'where' : 'which';
    const result = spawnSync(whichCmd, ['java'], { encoding: 'utf8' });
    if (result.status === 0 && result.stdout) {
        return result.stdout.split(/\r?\n/)[0].trim();
    }
    return null;
}

function getPlatformArchiveName() {
    let arch = process.arch;
    const plat = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'mac' : 'linux';
    if (plat === 'mac' && arch === 'arm64') {
        arch = 'aarch64';
    }
    const ext = process.platform === 'win32' ? 'zip' : 'tar.gz';
    const version = TEMURIN_VERSION.replace('+', '_');
    return `OpenJDK17U-jre_${arch}_${plat}_hotspot_${version}.${ext}`;
}

async function downloadJava() {
    mkdirSync(CACHE_DIR, { recursive: true });
    const archiveName = getPlatformArchiveName();
    const url = TEMURIN_BASE_URL + archiveName;
    const archivePath = join(CACHE_DIR, archiveName);
    console.log(`Downloading Java 17 runtime from ${url}...`);
    const res = await fetch(url);
    if (!res.ok) {
        throw new Error(`Failed to download Java: ${res.status} ${res.statusText}`);
    }
    await new Promise((resolve, reject) => {
        const file = createWriteStream(archivePath);
        Readable.fromWeb(res.body)
            .pipe(file)
            .on('finish', resolve)
            .on('error', reject);
    });
    if (process.platform === 'win32') {
        console.log('Extracting ZIP archive...');
        const extractRes = spawnSync('powershell', [
            '-NoProfile',
            '-NonInteractive',
            '-Command',
            'Expand-Archive',
            '-Force',
            archivePath,
            CACHE_DIR
        ], { stdio: 'inherit' });
        if (extractRes.status !== 0) {
            throw new Error('Failed to extract Java archive.');
        }
    } else {
        console.log('Extracting tar.gz archive...');
        execSync(`tar -xzf '${archivePath}' -C '${CACHE_DIR}'`);
    }
    console.log('Local Java runtime now installed.');
}

function resolveLocalJavaExecutable() {
    let javaPath = `${CACHE_DIR}/jdk-${TEMURIN_VERSION}-jre/bin/java`;
    if (process.platform === 'win32') {
        javaPath = join(CACHE_DIR, `jdk-${TEMURIN_VERSION}-jre`, 'bin', 'java.exe');
    } else if (process.platform === 'darwin') {
        javaPath = join(CACHE_DIR, `jdk-${TEMURIN_VERSION}-jre`, 'Contents', 'Home', 'bin', 'java');
    }
    if (!existsSync(javaPath)) {
        throw new Error('Local Java runtime not found after installation.');
    }
    return javaPath;
}

async function ensureLocalJava() {
    if (!existsSync(CACHE_DIR)) {
        await downloadJava();
    }
    return resolveLocalJavaExecutable();
}

async function resolveJava() {
    const javaPath = findSystemJava();
    if (javaPath) return javaPath;
    try {
        return resolveLocalJavaExecutable();
    } catch {
    }
    return ensureLocalJava();
}

if (require.main === module) {
    void resolveJava().catch(() => process.exit(0));
}

module.exports = { resolveJava };
