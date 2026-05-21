#!/usr/bin/env node
'use strict';
const { execFile } = require('child_process');
const path = require('path');
const vnuJar = path.join(__dirname, 'build/dist/vnu.jar');
const vnu = {
    check: async function (args = [], options = {}) {
        const { resolveJava } = require('./vnu-java-downloader');
        const javaCmd = await resolveJava();
        const finalArgs = ['-jar', vnuJar, ...args];
        return new Promise((resolve, reject) => {
            const child = execFile(javaCmd, finalArgs, (error, stdout, stderr) => {
                const output = [stdout, stderr].filter(Boolean).join('');
                if (error) {
                    const errObj = new Error(output || error.message);
                    reject(errObj);
                } else {
                    resolve(output);
                }
            });
        });
    }
};

const exported = new String(vnuJar);
exported.vnu = vnu;
module.exports = exported;

if (require.main === module) {
    (async () => {
        try {
            const { resolveJava } = require('./vnu-java-downloader');
            const javaCmd = await resolveJava();
            const { spawn } = require('child_process');
            const child = spawn(javaCmd,
                ['-jar', vnuJar, ...process.argv.slice(2)],
                { stdio: 'inherit' });
            child.on('close', (code) => process.exit(code || 0));
        } catch (err) {
            console.error(err.message.trim());
            // Set the exit code rather than calling process.exit(): exiting
            // while the sockets from a failed download are still closing
            // crashes Node with a libuv assertion on Windows.
            process.exitCode = 1;
        }
    })();
}

