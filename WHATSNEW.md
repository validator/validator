
26 March 2017.

This release makes the “color” attribute allowed with `link[rel="mask-icon"]`, adds an `--asciiquotes` option to the vnu.jar command-line checker, improves the language detector to help prevent language misidentifications (especially for Russian and Chinese documents), adds better support for IDNs (by updating to ICU4J 58.2), changes the checker behavior to not fail for “Corrupt GZIP trailer” cases, fixes a bug that disallowed Microdata global attributes for `meta[name]`, and makes `allow-top-navigation-by-user-activation` an allowed value for `iframe[sandbox]` (while adding a new error if that value is used at the same time as `allow-top-navigation`).

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1730

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
