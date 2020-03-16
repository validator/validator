16 March 2020.

First release in more than one year. This release provides a number of improvements in ARIA role checking and in CSS checking, as well as support for checking a number of HTML features that weren’t supported previously.  It’s the first release to provide binary runtime images, as an alternative to the jar/war distributions. It's also the last release for which the Web-based checker will be configured to listen on all interfaces; after this release, the checker will bind by default to 127.0.0.1 — so to make the checker listen on a different address, you’ll need to specify the address.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#20316

The files in this release provide a portable standalone version of the Nu Html Checker in a number different forms: as a Java jar file, as a Java war file, and a binary runtime images that can be used even on a system that doesn’t have Java installed.

Use the binary images and jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
