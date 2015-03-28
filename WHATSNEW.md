
28 March 2015
What’s new: This release drops all `meta[name]` checking and adds: improved
error messages for `input[type]` attribute mismatches; support for checking
`object[typemustmatch]`; new error message for `title` elements that only
have whitespace; new `useragent` request param, to allow you to specify any
arbitrary user-agent string for the checker to use when fetching remote
documents; new `nu.validator.messages.limit` Java system prop to control
limit on maximum number of messages the checker service will report for a
single doc before stopping with a "Too many messages" fatal error. This
release also adds full support for checking documents at SNI origins &
changes the API/CLI (command-line interface) to emit source extracts +
“hilite” info when you set the `--format` option to `json`, `xml`, `xhtml`,
or `html` & fixes regression that caused CLI/API to parse .xhtml docs as
text/html instead of using the XML parser.
More: https://github.com/validator/validator/blob/master/CHANGELOG.md#15328

The files in this release provide a portable standalone version of the Nu Html
Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of HTML documents from the command
line and from other scripts/apps, as documented at http://validator.github.io,
or as a self-contained service for browser-based checking of HTML documents over
the Web—similar to http://html5.validator.nu/ and http://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such
as Tomcat, as documented at https://validator.github.io/validator/#servlet.
