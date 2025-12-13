# vnu-jar: Check HTML from JavaScript

`vnu-jar` lets you use the Nu Html Checker (vnu) from within JavaScript code — to catch unintended mistakes in HTML, CSS, and SVG content.

## Install latest release version

```sh
npm install --save vnu-jar
```

## Example

Here’s an example of how to create a simple `vnu-check.js` program that uses the `vnu-jar` package to invoke the Nu Html Checker (vnu), pass it whatever arguments the user specifies, and then emit the checker output.

```js
'use strict';
const { vnu } = require('vnu-jar');
(async () => {
    try {
        console.log(await vnu.check(process.argv.slice(2)));
    } catch (err) {
        console.error(err.message.trim());
        process.exit(1);
    }
})();
```

## Command-line usage

When installed or run via `npx`, the package also provides a `vnu` command that causes the `vnu.jar` program to be invoked and run with whatever arguments you specify:

```sh
npx vnu --verbose file1.html file2.html
npx vnu --help
```

## Java auto-installation

This npm package includes the `vnu.jar` Java program — so to use the package, users will additionally need a Java environment that provides a `java` command.

For that reason, the package runs a `postinstall` script which — if it finds no `java` command in the user’s environment during install — will then _automatically_ install a (Node.js-local) Java runtime environment, in `node_modules/.cache/vnu-jar/java/`).
