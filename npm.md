# vnu-jar: Check HTML from JavaScript

`vnu-jar` lets you use the Nu Html Checker (vnu) from within JavaScript code — to catch unintended mistakes in HTML, CSS, and SVG content.

## Install latest release version

```sh
npm install --save vnu-jar
```

## Install 'next' version

```sh
npm install --save vnu-jar@next
```

## Example

```js
'use strict';

const { execFile } = require('child_process');
const vnu = require('vnu-jar');

// Print path to vnu.jar
console.log(vnu);

// Work with vnu.jar, for example get vnu.jar version
execFile('java', ['-jar', `"${vnu}"`, '--version'], { shell: true }, (error, stdout) => {
    if (error) {
        console.error(`exec error: ${error}`);
        return;
    }

    console.log(stdout);
});
```
