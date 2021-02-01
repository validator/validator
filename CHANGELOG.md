With a few exceptions, this is a record of mainly just user-facing
changes — that is, either changes to the actual behavior of the checker,
or changes to any options/interfaces the checker exposes for developers.

# NN.NN.NN
NN XXX NNNN
  - Allow `loading` attribute for the `iframe` element (lazy loading)
  - Allow `autocomplete=username` with `input[type=email]`
  - Allow more values for SVG `feBlend` element `mode` attribute
  - CLI: Add nu.validator.htmlparser.tools|test classes to vnu.jar
  - CLI/Docker: Enable setting timeouts & bind address w/ environment vars
  - CSS: Support color-adjust
  - Docker: Migrate Dockerfile to “distroless” image (reduces image size)
  - ARIA: Allow `aria-required` with `input[type=file]`
  - Update doc-fetching backend to Apache HTTP Components HttpClient 4.5.9
  - Make the checker use HTTP 1.0 for all requests it make, not HTTP 1.1
  - Make the checker ignore error for malformed/missing HTTP closing chunk

# 20.6.30
30 June 2020
  - CLI: Add new `--stdout` option, to report all messages to stdout
  - CLI: Ensure the `--version` option reports the actual version
  - CLI: Actually check SVG files, rather than just seeming to (bug fix)
  - CSS: Improvements in `calc()`, `min()`/`max()` checking, more
  - Allow `height` and `width` attributes for SVG `symbol` element
  - Allow `capture` attribute for the `input[type=file]` element
  - Allow `disabled` attribute for the `link[rel=stylesheet]` element
  - Treat custom elements as labelable elements for `label[for]` handling
  - ARIA: Fix bug that disallowed implicit combobox/listbox for `select`
  - ARIA: Improve error message for `alt`-less `img` with ARIA attributes
  - ARIA: Allow `section[role=doc-glossary]`

# 20.3.16
16 March 2020
  - Disallow `accept-charset` values other than `UTF-8`
  - Disallow object[typemustmatch]
  - Allow SVG feDropShadow element (from Filter Effects spec)
  - Allow `loading` attribute for the `img` element (lazy loading)
  - Allow `rel=modulepreload` for the `link` element
  - Allow `integrity` attribute on link[rel=preload|modulepreload]
  - Allow `integrity` attribute on script[type=module]
  - Allow `autofocus` as a global attribute
  - Allow `inputmode` as a global attribute
  - Allow `nonce` as a global attribute
  - Allow `allow-downloads` in iframe[sandbox]
  - Allow heading content within `legend` element
  - Allow negated media features in `media` and `sizes` attributes
  - Align `autocomplete` checking with current spec
  - CSS: Improve support for color values
  - ARIA: Allow implicit roles to satisfy owned-by requirements
  - ARIA: Add proper ARIA checking for the `math` element
  - ARIA: Align all role checking with current HTML-ARIA spec
  - Add option to specify additional script for Web-based checker
  - CLI: Make --errors-only option take precedence over --Werror
  - CLI: Enable checking standard input as SVG or CSS
  - Include binary runtime images in release (alternative to jar/war)
  - Dockerfile: Switch to using binary runtime image rather than jar
  - Add checker.py script to repo (for building/testing/running checker)
  - Add option to bind to specific IP address (rather than all interfaces)

# 18.11.5
05 November 2018
  - Fix bugs that can cause the command-line checker to emit broken JSON output
  - Allow `dppx` and `x` and units in media queries

# 18.8.29
29 August 2018
  - CSS: Allow unit-less values for stroke-width and other from-SVG props
  - CSS: Bring checking up to date w/ CSS Align3; support font-display

# 18.7.23
23 July 2018
  - Disable logging in the language detector

# 18.7.22
22 July 2018
  - Allow the `decoding` attribute for the `img` element
  - Allow the `allow` attribute for the `iframe` element (initial support)
  - Align ARIA checking further with ARIA in HTML spec requirements
  - Restore the language-detection feature to vnu.jar command-line checker
  - Ensure vnu.jar is always runnable under Java8, even if built under Java9

# 18.3.0
24 March 2018
  - Add new major (optional) feature to command-line checker, Web-based checker,
    and network API to check CSS documents (in addition to HTML documents)
  - Add new major feature to check that `style` element contents and `style`
    attribute values in HTML documents are valid CSS
  - Add new (optional) feature to command-line checker to check SVG documents
  - Add new option to command-line checker for specifying User-Agent string
  - Add Dockerfile to Docker Hub https://hub.docker.com/r/validator/validator/
  - Add ability to Web-based checker to check SVG documents by file upload
  - Emit error (not warning) for HTML4/XHTML1 strict doctypes
  - Further restrict `script[type]` and `style[type]`
  - Allow the `autocapitalize` global attribute
  - Allow the `slot` attribute (for Shadow DOM interaction)
  - Allow the `allowpaymentrequest` attribute for the `iframe` element
  - Allow only one non-hidden `main` element per document
  - Allow only `html`, `body`, `div`, `form`, custom elements as `main` ancestor
  - Allow `</caption>` end tag to be omitted
  - Allow `role=none` where `role=presentation` is allowed
  - Allow `role=rowgroup` element to be contained in `role=table` element
  - Allow `role=row` element to be contained in `role=table` element
  - Allow more values for `aria-haspopup` per current ARIA spec
  - Allow more ARIA states & properties for `role=menuitem`
  - Allow CSP `prefetch-src` directive (updated to Salvation 2.4.0)
  - Disallow all character encodings other than UTF-8
  - Disallow `script[charset]`
  - Disallow nested interactive ARIA roles
  - Disallow the `dropzone` attribute
  - Disallow the `menuitem` element
  - Fix bug that caused the checker to incorrectly treat `input[type]` values
    case-sensitively when doing particular checks

# 17.11.1
07 October 2017
  - Fix bug that made the vnu.jar `--Werror` option not work as expected
  - Make vnu.jar exit 0 if all errors have been filtered out

# 17.11.0
06 October 2017
  - Allow DPUB roles on more elements (per ARIA in HTML spec updates)
  - Add `--Werror` option to the vnu.jar command-line checker. The option
    causes the checker to exit non-zero if any warnings are encountered
    (even if there are no errors).
  - Fix mismatch that caused message-filtering failures
  - Fix memory leak in language detector (patch from @tgyurci)
  - Stop reporting HTML4-specific parse errors for HTML4-doctype docs

# 17.9.0
20 August 2017
  - Allow `script[nomodule]`
  - Allow `hover`, `any-hover`, `pointer`, and `any-pointer` media features
  - Allow `@scope`, `@updateviacache`, `@workertype` for `link[rel=serviceworker]`
  - Allow `&;` (don’t report it as “`&` did not start a character reference”)
  - Add `acceptlanguage` query parameter, to specify an Accept-Language
    request-header value for checker to send when fetching remote documents
  - Update CSP checking to Salvation 2.3.0
    https://github.com/shapesecurity/salvation/releases/tag/v2.3.0

# 17.7.0
26 June 2017
  - Add new major feature to filter out (drop/ignore/suppress) errors/warnings
    by regex. See https://github.com/validator/validator/wiki/Message-filtering
  - Replace a case of “Attribute "foo" not allowed on element "param" **in this
    context**” wording in error message with “Attribute "foo" not allowed on
    element "param" **at this point**” (for consistent working for that error
    between the command-line checker and the web-based checker).
  - Disallow the `contextmenu` attribute and `type=contextmenu` and `type=toolbar`
    for the `menu` element.
  - Allow `link[rel=serviceworker]`
  - Allow floating-point numbers in attribute values to start with decimal point
  - Allow `a[href]` in SVG wherever `a[xlink:href]` is allowed
  - Allow the `focusable` and `tabindex` attributes on SVG elements
  - Fix bug that disallowed `progress` & `meter` as `label[for]` targets
  - Default to text/html for checking file uploads
  - Emit warnings for use of `rel={copyright,previous}`
  - Prevent Bulgarian ➡ Russian misidentifications in language detector
  - Skip figcaption elements when running the language detector

# 17.3.0
26 March 2017
  - Allow `color` attribute with `link[rel="mask-icon"]`
  - Allow `meta[name]` to have `itemref`/`itemscope`/`itemtype`/`itemid`
  - Allow `allow-top-navigation-by-user-activation` in `iframe[sandbox]`
  - Stop hiding “sectioning roots” headings in “Heading-level outline”
  - Change error for `role=none` with `img[alt=""]` to warning
  - Fix from @xfq for longstanding bug in “Show source” behavior in Web UI
  - Fix from @xfq for controlling some runtime params for HTTP behavior
  - Fix from @zcorpan to drop unneeded warning for `<menu type=toolbar>`
  - Make “Corrupt GZIP trailer” a non-error
  - Add `--asciiquotes` option to vnu.jar command-line checker
  - Skip lang detection of elements w/ lang attributes not matching `html[lang]`
  - Drop Bulgarian lang detection, to prevent Russian misidentification
  - Update Estonian/Catalan lang profiles, to prevent Russian misidentification
  - Update ICU4J to 58.2

# 17.2.1
06 February 2017
  - Fix bug in language detector that when running the vnu.jar command-line
    checker on a list of documents caused it to sometimes misidentify the
    language of the 2nd, 3rd, 4th, etc., documents. The bug also caused the
    memory used by the checker to increase as the number of documents
    checked at the same time increased, and caused performance to degrade.
  - Allow `aria-required` attribute everywhere `required` attribute is allowed
  - Add `--exit-zero-always` option to vnu.jar command-line checker
  - Fix longstanding bug around code for identifying overlapping cells in
    table-integrity checker (the bug somewhat frequently gets hit when checking
    Wikipedia pages but otherwise in the wild gets hit only extremely rarely)

# 17.2.0
30 January 2017
  - Fix bug that broke vnu.jar command-line checking of URLs
  - Fix bug in `rel="shortcut icon"` checking
  - Add `nu.client.EmbeddedValidator` for use as library by other Java apps
  - Disallow `tfoot` before `tbody`

# 17.1.0
15 January 2017
  This release adds the following changes to the vnu.jar command-line
  checker that had already been made available in the Web-based checker in
  the 17.0.1 release.
  - Allow **custom elements** (names containing “-”; e.g., `<foo-bar>`)
  - Allow anything in `template` element subtrees (exclude from checking)

# 17.0.1
08 January 2017
  - New language-detection feature; warns for missing/wrong `html[lang]`
  - New option `--no-langdetect` for `vnu.jar` disables language detection
  - Allow **custom elements** (names containing “-”; e.g., `<foo-bar>`)
  - Allow the `is` attribute (for custom elements)
  - Allow **ARIA 1.1** roles/states/properties
  - Warn for viewport values that restrict resizing
  - Allow `div` in `dl`, to group `dt`+`dd` sets
  - Allow anything in `template` element subtrees (exclude from checking)
  - Allow `link[rel=preload]` in body
  - Disallow `sizes` attribute on non-icon `link`
  - Allow `<link rel=apple-touch-icon sizes=…>`
  - Allow comments before doctype (warning dropped)
  - Allow `<video playsinline>`
  - Allow `<iframe allowusermedia>`
  - Warn for `sandbox="allow-scripts allow-same-origin"`
  - New option to check error pages (404s and other non-200 responses)
  - Allow `link[nonce]`
  - Disallow `input[datetime]`
  - Disallow `mediagroup` attribute
  - Allow `menu[type=popup]`, disallow `menu[type=context]`
  - Disallow non-http/https URLs in `a[ping]`
  - Allow `referrerpolicy` attribute
  - Warn for `html[manifest]` (obsolete)
  - Disallow `keygen` (obsolete)
  - Warn for `about:legacy-compat` in doctype
  - Align SVG+ARIA checking with ARIA requirements in current SVG spec
  - Allow `h1`-`h6` & `hgroup` in `legend`
  - Ignore SSL errors when checking remote documents
  - Allow `script[type=module]` (supported in Edge but not in other UAs yet)
  - Disallow content in `iframe` (must be empty)
  - Make `vnu.jar` check `.xhtml` files using XML-specific RelaxNG grammar
  - Allow `th[abbr]`
  - Allow any value in SVG `class` attribute (not just XML-compatible names)
  - Disallow HTML4/XHTML1 Transitional doctype
  - Allow CSP `require-sri-for` directive (updated to Salvation 2.2.0)
  - Allow any element or text as content of SVG `desc`
  - Allow SVG `vector-effect=non-scaling-stroke`
  - Allow only text in `rp`
  - Disallow multiple `meta[name=description]`
  - Disallow URLs with port values greater than 65535
  - Disallow `<input name=isindex>`
  - Disallow empty `autocomplete` attribute

# 16.6.29
29 June 2016
  - JSON/gnu message formats updated to ensure doc URL is always included
  - `<!-->` (IE conditional comment end) is now (again) a non-error
  - `<template>` contents are now hidden from outline views

# 16.6.20
20 June 2016
  - fixes problem that made the release jars unusable with Scala `sbt test`
  - adds “Heading-level outline” in Web UI; shows simple h1-h6 hierarchy

# 16.6.18
18 June 2016
  - link[rel=stylesheet] in body now non-error (body-OK)
  - rel=dns-prefetch|preconnect|prefetch|preload|prerender non-error+body-OK
  - style[scoped] now error
  - iframe[seamless] now error
  - `--` (consecutive hyphens) within a comment now non-error
  - new specific error for `--!>` at end of a comment
  - new specific error for `<!--` within a comment
  - multiple meta[charset] now error
  - `autocomplete` checking now aligned with current spec
  - label[form] now error
  - a|area[rel=noopener] now non-error
  - allow-presentation/allow-orientation-lock in iframe[sandbox] non-error
  - label-less empty option now non-error if datalist child
  - section[role=navigation|complementary|banner] now non-error

# 16.3.3
3 March 2016
  - Made `minlength` a non-error for `input[type=password]`/`input[type=text]`
  - Made multiple values in `integrity` a non-error
  - Made `<time>` with element children an error if no `datetime` specified
  - Improved CSP checking (now using Salvation 2.0.1)
  - [WebUI] Dropped “Using the schema…”/“The Content-Type was…” Info msgs
  - [WebUI] Added some autofocus of URL field and Message Filtering button
  - [WebUI] Footer now tells whether document was served w/ charset param
  - [WebUI] Fixed bug/regression in Image Report image display
  - [build] Fixed bug caused by Rhino team building their jar with Java6
  - [build] Fixed some problems with running build script on Windows
  - [build] Made build script work with Python 3 (not just Python 2)

# 16.1.1
1 January 2016
  - Java8 is now required to run the checker (and to build it).
  - Made the `<meta http-equiv=content-security-policy content="...">`
    element a non-error & added syntax checking of its `content` attribute
    and checking of the value of the Content-Security-Policy HTTP header.
  - Made the Content Security Policy `nonce` attribute a non-error.
  - Aligned `on*` event-handler-attribute checking with spec.
  - Aligned `iframe[sandbox]` checking with spec.
  - Made `minlength` attribute a non-error.
  - Dropped “heading needed” warning for cases where `aria-label` found.
  - Made unescaped ampersand a non-error in, e.g. `href="foo?bar=1&baz=2"`.
  - Added error for `img[alt=""]` w/ `role` != `presentation`.
  - Made `role=switch` a non-error.
  - Made `role=group` for `header` & `footer` a non-error.
  - Made `role=search` for `form` a non-error.
  - Made the Subresource Integrity `integrity` attribute a non-error and
    added syntax checking for it.
  - Refined bad-URL error message to indicate which character is invalid.
  - Refined Web UI "Message Filtering" to show total error/warning counts
  - Refined Web UI to show green if there are 0 errors or warnings to show
  - Fixed "`input[type=hidden]` label descendants" bug (@takenspc patch)
  - Refined Web UI to remove background colors and increase font size
  - Made a variety of refinements and fixes to ARIA role checking.
  - Made `ol>li[role=menuitem]` & `ol>li[role=tab]` non-errors.
  - Added warnings for use of ARIA roles with implicit-semantics elements.
  - Made nesting of `time` elements a non-error.
  - Made checker ignore `input[type=hidden]` label descendants in checks.
  - Improved errors for obsolete media types/features.
  - Fixed bug in checking of the content model for the `ruby` element.


# 15.6.29
29 June 2015
  - Added error messages for deprecated CSS media types/features.
  - Changed checking of the `accept` attribute for input[type=file] to
    allow file extensions in the value (per spec).
  - Added error for documents that have  multiple `autofocus` attributes.
  - Made the `<rb>` and `<rtc>` elements non-errors.
  - Made use of `data-*` attributes for SVG & MathML elements non-errors.
  - Made use of HTML content in the SVG `<desc>`, `<title>`, and
    `<metadata>` elements a non-error (per spec).
  - Changed error message for the `inputmode` attribute to a warning.
  - Fixed a bug that caused spurious error to be emitted for ID references
    in `aria-controls` and `aria-labelledby` with trailing whitespace.
  - Fixed a bug that prevented the command-line checker from being able
    to check URLs when run in a Windows environment. (patch from @mariusj)
  - Added option to disable log4j when using Java API. (patch from @abrin)

# 15.4.12
12 April 2015
  - Fixed regression that caused spurious errors for `input[type=email]`.
  - Fixed regression in war caused by missing jar needed for gzip handling.

# 15.3.28
28 March 2015
  - Renamed from “Nu HTML Checker” to “Nu Html Checker”.
  - Improved error messages for `input[type]` attribute mismatches.
  - Added support for checking `object[typemustmatch]` per-spec.
  - Added error message for `title` element that only has whitespace.
  - Dropped all `meta[name]` checking. Any arbitrary `meta[name]` value
    is now accepted unchecked.
  - Made a couple select/option error messages more precise.
  - Added `useragent` parameter, for allowing you to specify any arbitrary
    user-agent string for the checker to use when fetching remote documents.
  - Added option to limit maximum number of errors shown. Exposed thru
    `nu.validator.messages.limit` Java system prop & `--messages-limit`
    option for the build script. Controls limit on maximum number of
    messages the checker service will report for a single document before
    stopping with a “Too many messages” fatal error.
  - Make the API/CLI (command-line interface) emit source extracts &
    “hilite” info.  When you set the `--format` option to `json`, `xml`,
    `xhtml`, or `html` (but not `gnu` or `text`), the output now includes:
      - an extract from the doc source (`extract` field in JSON output)
      - which extract part to hilight (JSON `hiliteLength` & `hiliteStart`)
      - error range starting line/column (JSON `firstColumn` & `firstLine`)
  - Added full support for checking documents at SNI origins.
  - Fixed regression that caused CLI/API to parse .xhtml docs as text/html
    instead of using the XML parser.
  - Changed backend handling for the case when the “promiscuous-ssl” option
    is on (that is, when you’re requesting the doc-fetching backend ignore
    any SSL/TLS cert errors). This should be a transparent change.
  - Now available from (Maven) Central Repository (nu.validator.validator).
  - Made a number of look&feel refinements to the Web frontend.
  - Replaced all Jena IRI code dependencies with dependency on galimatias.
  - Updated doc-fetching backend to Apache HTTP Components HttpClient 4.4.
  - Upgraded to Jetty 9.2.9 & upgraded many other build/run dependencies to
    latest versions; e.g., log4j 1.2.17, Apache Commons Codec 1.10.
  - Dropped some dependencies that aren’t actually needed.
  - Changed build to cut dependency download size from ~300MB down to ~16MB.
  - Made change to force java to always use Saxon instead of Xalan.
  - Renamed all org.whattf classes to nu.validator.
  - Did large reorganization/consolidation of sources.
  - Added `--javaversion` option, to generate class files targeted for
    particular VM versions (compiles for Java6/1.6 by default).

# 16 February 2015
  - added new "`sizes` attr required when `srcset` specifies width" check
  - added `--skip-non-html` option to CLI; http://goo.gl/sKjRD5
    This change alters the default CLI handling of non-HTML files.
    Before the CLI by default skipped any files without .html, .htm,
    .xhtml, or .xht extensions; instead now by default all files
    found are checked, regardless of extension. The `--skip-non-html`
    option provides the old default behavior: it make the checker skip
    any files without .html, .htm, .xhtml, or .xht extensions.
  - added `--javaversion` option to build script and changed default build
    behavior to now generate vnu.jar/vnu.war builds that can run in Java6
    VMs (as well as in any newer VMs). To generate a vnu.jar/vnu.war build
    with a newer/different VM target, use, e.g., `--javaversion=1.8`.
  - added `--stacksize` option to build script & removed harcoded stack size
  - fixed several bugs in `sizes` checking
  - fixed position reporting of bad character refs in `title` & `textarea`
  - fixed ARIA checking to allow `li[role=separator]` & `time[role=timer]`
  - refined content-type check to treat `.csl` uploads as application/xml
  - refined "unexpected content-type" error msg to include URL of document
  - refined a few things in TestRunner
  - updated Rhino dependency to rhino1_7R5

# 07 February 2015
  - made SVG `<style>` not require the `type` attribute
  - added initial (liberal) support for ARIA in SVG
  - dropped error for `X-UA-Compatible: IE=Edge` HTTP header. Thx @zcorpan
  - dropped error for `meta[http-equiv=X-UA-Compatible][content=IE=Edge]`
  - added version info to jar manifest file
  - made nu.validator.client.TestRunner exit non-zero for test failures
  - made build script explicitly request Python2. Thx @kurosawa-takeshi
  - code cleanup to build script and some Java sources. Thx @cvrebert

# 06 October 2014
  - brought reporting of bad IDs in `form` attr into compliance with spec
    (see https://github.com/validator/validator.github.io/issues/8
    and thanks again to https://github.com/cavweb20)

# 01 September 2014
  - fixed bug that broke json & xml message output
    (see https://github.com/validator/validator.github.io/issues/5
    and thanks to https://github.com/cavweb20)

# 25 August 2014
  - added support for the `<picture>` element
  - improved ARIA support for various table elements
  - made refinements to outline handling
  - added experimental warnings for some heading/outline issues
  - improved checking for `meta@name` and `link@rel` values
  - CLI now exits with `1` if any errors are found
  - CLI no longer says `XHTML element` in error messages
  - switched to galimatias for URL checking
  - updated to latest ICU4J
  - release now includes WAR file

# 02 February 2014
  - initial `vnu.jar` release
