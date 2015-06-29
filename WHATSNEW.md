
29 June 2015.
This release fixes a bug that caused spurious error to be emitted for ID
references in `aria-controls` and `aria-labelledby` with trailing
whitespace, along with fixing a bug that prevented the command-line checker
from being able to check URLs when run in a Windows environment. It
adds spec-conformant support for `<rb>` and `<rtc>` elements and updates
checking of the `accept` attribute for input[type=file] to allow file
extensions in the value (per spec). It makes the use of `data-*` attributes
for SVG & MathML elements non-errors, as well as the use of HTML content in
the SVG `<desc>`, `<title>`, and `<metadata>` elements. And it adds error
messages for deprecated CSS media types/features (per spec).

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#15629

The files in this release provide a portable standalone version of the Nu Html
Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of HTML documents from the command
line and from other scripts/apps, as documented at http://validator.github.io,
or as a self-contained service for browser-based checking of HTML documents over
the Web—similar to http://html5.validator.nu/ and http://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such
as Tomcat, as documented at https://validator.github.io/validator/#servlet.
