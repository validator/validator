
24 March 2018.

This release adds two new major features. The first is an optional feature in the command-line checker, Web-based checker, and network API to check CSS documents (in addition to the checking of HTML documents we already do). And the second is a (non-optional) feature to check that `style` element contents and `style` attribute values in HTML documents are valid CSS.

The release also adds a new optional feature to the command-line checker to check SVG documents, as well as a new option in command-line checker for specifying a User-Agent string.

Along with those additions, some changes made in this release include support for the `autocapitalize` global attribute, the `slot` attribute (for Shadow DOM interaction), the `allowpaymentrequest` attribute for the `iframe` element, the CSP `prefetch-src` directive, and a variety of ARIA-related improvements. Also worth noting is that with this release, the checker now reports an error for documents in any character encoding other than UTF-8 and for all doctypes other than `<!DOCTYPE html>`.

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1830

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
