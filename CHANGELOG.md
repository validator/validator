With a few exceptions, this is a record of mainly just user-facing
changes—that is, either changes to the actual behavior of the checker, or
changes to any options/interfaces the checker exposes for developers.

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
