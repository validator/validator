05 November 2018.

This release fixes a bug that can happen with the command-line checker when you’ve chosen JSON output and you’re batch-checking a directory tree that contains a mix of .html/.xhtml/.svg files, as well as bug that can happen if the size of the JSON error output exceeds 8KB.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#18115

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
