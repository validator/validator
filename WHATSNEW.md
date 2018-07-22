
22 July 2018.

This release updates the checker to allow the `decoding` attribute for the `img` element and the `allow` attribute for the `image` element. The release also includes a number of changes to align ARIA role checking with the current requirements in the **ARIA in HTML** spec. In addition, this release also ensures that the vnu.jar distribution is always runnable under Java9, even if built under Java9

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#18722

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
