
18 June 2016.
This release makes `<link rel=stylesheet>` within the body a non-error, as
well as also making the `link` `rel` values `dns-prefetch`, `preconnect`,
`prefetch`, `preload`, and `prerender` non-errors (including in the body),
and making `a[rel=noopener]` and `area[rel=noopener]` non-errors. In
addition: `<style scoped>` and `<iframe seamless>` are now errors (because
they’ve been dropped from the HTML spec), using multiple `<meta charset>`
elements is now an error, `allow-presentation` & `allow-orientation-lock`
are now allowed values for `iframe[sandbox]`, and complete checking for the
(complicated) microsyntax of the `autocomplete` attribute is now performed.
Finally, comment checking has been **experimentally** changed in this
release (to match a recent change to the HTML spec) such that the checker
no longer emits errors for "`--`" (consecutive hyphens) in a comment but
does now emit specific error messages for "`<!--`" (nested comment) within
a comment, and "`--!>`" at the end of a comment (should be just "`-->`").

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#16618

The files in this release provide a portable standalone version of the Nu Html
Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line
and other scripts/apps, as documented at https://validator.github.io/validator/,
or as a self-contained service for browser-based checking of HTML documents over
the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/
and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such
as Tomcat, as documented at https://validator.github.io/validator/#servlet.
