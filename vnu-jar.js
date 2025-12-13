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
            console.log(await vnu.check(process.argv.slice(2)));
        } catch (err) {
            console.error(err.message.trim());
            process.exit(1);
        }
    })();
}

