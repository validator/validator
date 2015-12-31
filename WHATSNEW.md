
1 January 2016.
Java8 is now required to run the checker jar and service. This release fixes a
long-standing bug such that errors are no longer reported for ampersands in
cases that the spec does not define as invalid; for example, an error will no
longer be reported for a case like `<a href="foo/?bar=1&baz=2"">`. Also new in
this release is support for Content Security Policy syntax checking in values of
`content` attributes in `<meta http-equiv=content-security-policy content="…">`
elements as well as in Content-Security-Policy HTTP headers, and support for the
CSP-related `nonce` attribute. Support for Subresource Integrity-related syntax
checking has also been added, for the `integrity` attribute. Other changes
include a refinement of error-reporting for cases of URLs that contain invalid
characters, so that the error message now includes an explicit indication of
which specific characters in the URL are invalid. Various refinements to
checking of ARIA `role` attributes have also been made.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1611

The files in this release provide a portable standalone version of the Nu Html
Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of HTML documents from the command
line and from other scripts/apps, as documented at http://validator.github.io,
or as a self-contained service for browser-based checking of HTML documents over
the Web—similar to http://html5.validator.nu/ and http://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such
as Tomcat, as documented at https://validator.github.io/validator/#servlet.
