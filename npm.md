`vnu.jar` is a packaged version of the Nu Html Checker for batch-checking
documents from the command line and from other scripts/apps.

You can work with `vnu.jar` in CommonJS modules.

## Install latest release version
```sh
$ npm install --save vnu-jar
```

## Install latest dev version
```sh
$ npm install --save vnu-jar@dev
```

## Example
For Node.js 6+
```javascript
'use strict';

const exec = require ( 'child_process' ).exec;
const vnu = require ( 'vnu-jar' );

// Print path to vnu.jar
console.log ( vnu );

// Work with vnu.jar
// for example get vnu.jar version
exec ( `java -jar ${vnu} --version`, ( error, stdout ) => {

	if ( error ) {
		console.error ( `exec error: ${error}` );
		return;
	}

	console.log ( stdout );

} );

```
