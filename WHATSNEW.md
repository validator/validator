
26 June 2017.

This release adds a new major feature to filter out (drop/ignore/suppress) errors/warnings by regex (details at https://github.com/validator/validator/wiki/Message-filtering), replaces a case of _“Attribute "foo" not allowed on element "param" **in this context**”_ wording in error messages with _“Attribute "foo" not allowed on element "param" **at this point**”_ (for consistent wording between the command-line checker and the web-based checker), disallows the "contextmenu" attribute and `type=contextmenu` and `type=toolbar` for the `menu` element, allows `link[rel=serviceworker]`, allows all floating-point numbers in attribute values to start with a decimal point, allows `a[href]` in SVG wherever `a[xlink:href]` is allowed, and allows the "focusable" and "tabindex" attributes on SVG elements.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1770

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
