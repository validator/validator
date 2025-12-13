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
const { resolveJava } = require('vnu-jar/vnu-java-resolver');

(async () => {
    try {
        const javaCmd = await resolveJava();
        const source = process.argv[2];
        if (!source) {
            console.error('Usage: node validate.js <source>');
            process.exit(1);
        }
        execFile(javaCmd, ['-jar', vnu, source], { stdio: 'inherit' }, (error) => {
            if (error) {
                console.error('❌ Problems found.');
                process.exit(1);
            }
            console.log('✅ No problems found!');
        });
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
})();
```

## Command-line usage

When installed or run via `npx`, the package also provides a `vnu-jar` command that invokes the `vnu.jar` program to check whatever you specify:

```sh
npx vnu-jar index.html
```

## Java requirement

This npm package includes the `vnu.jar` Java program — so to use the package, users will additionally need a Java environment that provides a `java` command.

To help you make things easier for your users: The package provides a `resolveJava()` method (from the `vnu-java-resolver.js` included in the package) you can use in your application to check for a `java` command in the user’s environment; and if that finds no `java` command, it will then automatically install a (Node.js-local) Java runtime environment, in `node_modules/.cache/vnu-jar/java/`).

See the Example section above for an example of how to use that `resolveJava()` method to expose a `javaCmd` (or whatever) variable the resolves either to the path for whatever existing `java` command they already have in the environment — or else to the path for the `java` command in the local Java runtime environment it will otherwise then end up installing.

However, using the package in your application does not require you to necessarily also use that `resolveJava()` method in your code. If (for the use cases of your application), you want to instead assume/require your users to already have a `java` command available in their (shell) environment, then you can just have your application code directly call out to the `java` command in their (shell) environment — rather than indirectly referencing it from a `javaCmd` (or whatever) variable.
