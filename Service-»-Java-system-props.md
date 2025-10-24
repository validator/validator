The checker reads these Java system properties (set with the `-D` JVM
command line switch).

#### `nu.validator.servlet.log4j-properties`
Path to log4j config when the `nu.validator.servlet.Main` is used for
bootstrapping.

#### `nu.validator.servlet.presetconfpath`
Path to schema preset configuration file (`presets.txt`).

#### `nu.validator.servlet.cachepathprefix`
Path to the local entity cache (`local-entities/`). (This is where local
copies of schemas and DTDs are kept.)

#### `nu.validator.servlet.cacheconfpath`
Path to the configuration file that declared locally cached entities
(`entity-map.txt`).

#### `nu.validator.servlet.version`
The version number to send in HTTP `User-Agent`.

#### `nu.validator.servlet.service-name`
The name of the service (shown in the HTML UI).

#### `org.whattf.datatype.lang-registry`
URI (http or file) to the IANA language tag registry.

#### `nu.validator.servlet.about-page`
URI of the about page. (Linked from the HTML UI; must be reachable from
a browser.)

#### `nu.validator.servlet.style-sheet`
URI of the style sheet. (Linked from the HTML UI; must be reachable from
a browser.)

#### `nu.validator.servlet.script`
URI of the UI script. (Linked from the HTML UI; must be reachable from a
browser.)

#### `nu.validator.spec.microsyntax-descriptions`
URI (http or file) to the microsyntax description wiki page.

#### `nu.validator.spec.html5-load`
URI (http or file) to the HTML 5 spec (dereferenced by the validator at
startup).

#### `nu.validator.spec.html5-link`
URI of the HTML 5 spec. (Linked from the HTML UI; must be reachable from
a browser.)

#### `nu.validator.servlet.max-file-size`
Number of bytes that the validator is willing to consume.

#### `nu.validator.xml.promiscuous-ssl`
Set to `true` to turn off SSL/TLS certificate trust checking.