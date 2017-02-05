
06 February 2017.

This is an important bug-fix follow-up to the 17.2.0 release. It fixes a bug in the language detector that when running the vnu.jar command-line checker on a list of documents caused it to sometimes misidentify the language of the 2nd, 3rd, 4th, etc., documents. The bug also caused the memory used by the checker to increase as the number of documents checked at the same time increased, and caused performance to degrade. The release also fixes a longstanding bug around code for identifying overlapping cells in the table-integrity checker. Along with those bug fixes this release also adds an `--exit-zero-always` option to the vnu.jar command-line checker, and changes the checker behavior to allow the `aria-required` attribute everywhere the `required` attribute is allowed.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1721

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
