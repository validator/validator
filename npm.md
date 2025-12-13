# vnu-jar: Check HTML from JavaScript

`vnu-jar` lets you use the Nu Html Checker (vnu) from within JavaScript code — to catch unintended mistakes in HTML, CSS, and SVG content.

## Install latest release version

```sh
npm install --save vnu-jar
```

## Example

Here’s an example of how to use the `vnu-jar` package to create a simple `validate.js` program that runs the Nu Html Checker (vnu) on whatever source the user specifies as an argument.

```js
'use strict';

const { execFile } = require('child_process');
const vnu = require('vnu-jar');

const source = process.argv[2];
if (!source) {
    console.error('Usage: node validate.js <source>');
    process.exit(1);
}

execFile('java', ['-jar', vnu, source], { stdio: 'inherit' }, error => {
    if (error) {
        console.error('❌ Problems found.');
        process.exit(1);
    }
    console.log('✅ No problems found!');
});
```

## Command-line usage

When installed or run via `npx`, the package also provides a `vnu-jar` command that invokes the `vnu.jar` program to check whatever you specify:

```sh
npx vnu-jar index.html
```

## Java auto-installation

This npm package includes the `vnu.jar` Java program — so to use the package, users will additionally need a Java environment that provides a `java` command.

For that reason, the package runs a `postinstall` script which — if it finds no `java` command in the user’s environment during install — will then _automatically_ install a (Node.js-local) Java runtime environment, in `node_modules/.cache/vnu-jar/java/`).
