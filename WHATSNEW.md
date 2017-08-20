
20 August 2017.

This release adds a new feature to the checker network API to allow you to specify an `Accept-Language` request-header value for the checker to send when fetching remote documents to check. This release also allows `script[nomodule]`, allows the `hover`, `any-hover`, `pointer`, and `any-pointer` media features, allows the `scope`, `updateviacache`, `workertype` attributes for `link[rel=serviceworker]`, allows the string "`&;`" in content (that is, doesn’t report it as an “`&` did not start a character reference” error), and updates CSP checking to [Salvation 2.3.0](https://github.com/shapesecurity/salvation/releases/tag/v2.3.0).

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1790

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
