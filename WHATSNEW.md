
06 October 2017.

This release add a `--Werror` option to the vnu.jar command-line checker, which when set causes the checker to exit non-zero if any warnings are encountered (even if there are no errors). The release also fixes an internal code mismatch that caused message-filtering failures, fixes a memory leak in the language detector, drops reporting of HTML4-specific parse errors for HTML4-doctype docs, and allows DPUB `role` attributes on more elements.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#17110

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
