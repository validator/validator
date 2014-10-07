[![Build Status](http://goo.gl/q852Kn)](http://goo.gl/EWWeWZ)

## Server/client code for the v.nu markup checker

* `src/nu/validator`
  * `client` - various clients; e.g., `SimpleCommandLineValidator`
  * `localentities` - `LocalCacheEntityResolver`
  * `messages` - handling/emitting validation messages; `MessageEmitterAdapter`, etc.
  * `servlet` - core service logic; `VerifierServletTransaction` etc.
  * `source` - handling/emitting "show source" output; `SourceHandler` & `SourceCode`
  * `spec` - parsing the HTML spec & emitting spec excerpt in validation messages
  * `validation` - entry point for 3rd-party libs; `SimpleDocumentValidator`
* `sample` - sample validation client scripts (Python)
* `schema` - SVG and MathML RelaxNG schemas + HTML schema driver files
* `site` - JavaScript and CSS for the validator Web UI + "About" page source
* `test-harness` - script for full-stack testing of the validator (Python)
* `xml-src` - template used for building HTML source of validator Web UI
* `entity-map.txt` - catalog for resolving URLs to local file locations
* `presets.txt` - catalog for mapping schema URL sets to identifiers
