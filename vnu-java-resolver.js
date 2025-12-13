'use strict';

const { spawnSync, execSync } = require('child_process');
const { mkdirSync, existsSync, createWriteStream } = require('fs');
const { tmpdir } = require('os');
const { join } = require('path');
const https = require('https');
const readline = require('readline');
const { pipeline } = require('stream');
const { promisify } = require('util');
const { extract } = require('tar');
const pipelineAsync = promisify(pipeline);

const CACHE_DIR = join(__dirname, '..', 'node_modules', '.cache', 'vnu-jar', 'java');
const TEMURIN_VERSION = '17';
const TEMURIN_BASE_URL = 'https://github.com/adoptium/temurin17-binaries/releases/latest/download/';

function findSystemJava() {
    const whichCmd = process.platform === 'win32' ? 'where' : 'which';
    const result = spawnSync(whichCmd, ['java'], { encoding: 'utf8' });
    if (result.status === 0 && result.stdout) {
        return result.stdout.split(/\r?\n/)[0].trim();
    }
    return null;
}

function getPlatformArchiveName() {
    const arch = process.arch === 'x64' ? 'x64' : process.arch;
    const plat = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'mac' : 'linux';
    const ext = process.platform === 'win32' ? 'zip' : 'tar.gz';
    return `OpenJDK17U-jre_${arch}_${plat}_hotspot_latest.${ext}`;
}

async function downloadJava(dest) {
    mkdirSync(CACHE_DIR, { recursive: true });
    const archiveName = getPlatformArchiveName();
    const url = TEMURIN_BASE_URL + archiveName;
    const archivePath = join(CACHE_DIR, archiveName);
    console.log(`Downloading Java 17 runtime from ${url}...`);
    await new Promise((resolve, reject) => {
        const file = createWriteStream(archivePath);
        https.get(url, res => {
            if (res.statusCode !== 200) {
                reject(new Error(`Failed to download Java: ${res.statusCode}`));
                return;
            }
            res.pipe(file);
            file.on('finish', () => file.close(resolve));
        }).on('error', reject);
    });
    if (process.platform === 'win32') {
        console.log('Extracting ZIP archive...');
        execSync(`powershell -Command "Expand-Archive -Force '${archivePath}' '${CACHE_DIR}'"`);
    } else {
        console.log('Extracting tar.gz archive...');
        await extract({ file: archivePath, cwd: CACHE_DIR, strip: 1 });
    }
    console.log('Local Java runtime now installed.');
}

function resolveLocalJavaExecutable() {
    const javaPath = process.platform === 'win32' ? join(CACHE_DIR, 'bin', 'java.exe') : join(CACHE_DIR, 'bin', 'java');
    if (!existsSync(javaPath)) {
        throw new Error('Local Java runtime not found after installation.');
    }
    return javaPath;
}

async function ensureLocalJava() {
    if (!existsSync(CACHE_DIR)) {
        await downloadJava(CACHE_DIR);
    }
    return resolveLocalJavaExecutable();
}

async function resolveJava() {
    const javaPath = findSystemJava();
    if (javaPath) return javaPath;
    console.log('No Java runtime found.');
    return ensureLocalJava();
}

module.exports = { resolveJava };
