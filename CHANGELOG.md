# 16 February 2015
  - added new "`sizes` attr required when `srcset` specifies width" check
  - added `--skip-non-html` option to CLI; http://goo.gl/sKjRD5
    This change alters the default CLI handling of non-HTML files.
    Before the CLI by default skipped any files without .html, .htm,
    .xhtml, or .xht extensions; instead now by default all files
    found are checked, regardless of extension. The `--skip-non-html`
    option provides the old default behavior: it make the checker skip
    any files without .html, .htm, .xhtml, or .xht extensions.
  - added `--javaversion` option to build script (to build for older VMs)
  - added `--stacksize` option to build script & removed harcoded stack size
  - fixed several bugs in `sizes` checking
  - fixed position reporting of bad character refs in `title` & `textarea`
  - fixed ARIA checking to allow li[role=separator] and time[role=timer]
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
