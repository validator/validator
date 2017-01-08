
08 January 2017.

This is a major release which adds two important new features: **language detection** and support for **custom elements**. The custom-elements feature makes the checker allow element names containing hyphens (e.g., `<foo-bar>`). The language-detection feature guesses the language of a document by analyzing its content, compares the detected language to the value of the `lang` attribute of the `html` element, and then reports a warning if the `lang` value doesn’t match the detected language (or if the `lang` attribute is missing). For `vnu.jar`, a new `--no-langdetect` option has been added to disable that language-detection feature. An option has also been added to allow checking of remote error pages (404s and other non-200 responses). Other important changes in this release include: ARIA 1.1 roles/states/properties are now allowed, as well as `div` in `dl` (to group `dt`+`dd` sets), `link[rel=preload]` and `link[nonce]` and `referrerpolicy`, `h1`-`h6` & `hgroup` in `legend`, `script[type=module]`, `<video playsinline>`, and `<iframe allowusermedia>`.  Also with the release, any content is now allowed in `template` subtrees (they are now excluded from checking), viewport values that restrict resizing now cause a warning, comments before the doctype no longer cause a warning, and `vnu.jar` now by default ignores any SSL certificate errors when checking remote documents (use the `-Dnu.validator.xml.promiscuous-ssl=false` Java system property to override that default).

More: https://github.com/validator/validator/blob/master/CHANGELOG.md#1700

The files in this release provide a portable standalone version of the Nu Html Checker in two different forms: as a Java jar file, and as a Java war file.

Use the jar file either for batch checking of documents from the command line and other scripts/apps, as documented at https://validator.github.io/validator/, or as a self-contained service for browser-based checking of HTML documents over the Web—similar to https://checker.html5.org/ and https://html5.validator.nu/ and https://validator.w3.org/nu/.

Use the war file to deploy the Nu Html Checker through a servlet container such as Tomcat, as documented at https://validator.github.io/validator/#servlet.
