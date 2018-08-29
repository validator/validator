29 August 2018.

This release brings the checking support in the CSS backend up to date with CSS Align3, adds checking support for the `font-display` property, and allows unit-less numbers in the CSS `stroke-width` property and other from-SVG properties.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#18829

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
