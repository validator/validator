# The Nu Markup Checker (v.Nu) [![Build Status](http://goo.gl/b6xEQs)](http://goo.gl/ehNisw)

The Nu Markup Checker (v.Nu) is a name for the backend of
[html5.validator.nu][1], [validator.w3.org/nu][2], and the HTML5 facet of the
legacy [W3C Validator][3]. Its source code is available from [a set of github
repositories][4]. The checker is released as two separate packages:

   [1]: http://html5.validator.nu
   [2]: http://validator.w3.org/nu/
   [3]: http://validator.w3.org
   [4]: https://github.com/validator/

  * `vnu.jar` is a portable standalone version for [batch-checking documents
  from the command line][5] and from other scripts/apps, or for [deploying the
  checker as a self-contained service][6]

  * `vnu.war` is for [deploying the checker service through a servlet container
  such as Tomcat][7]

   [5]: http://validator.github.io/#usage
   [6]: http://validator.github.io/service.html#standalone
   [7]: http://validator.github.io/service.html#servlet

To use the Nu Markup Checker on your own, [get the latest release][8] and see
the **Usage** section below— or alternatively, consider automating your HTML
checking with a frontend such as:

   [8]: https://github.com/validator/validator.github.io/releases/latest

  * [Grunt plugin for HTML validation][9]

  * [HTML5 Validator Integration for Travis CI][10] (auto-check documents pushed
  to a github repo)

  * [LMVTFY: Let Me Validate That For You][11] (auto-check HTML of
  JSFiddle/JSBin etc. links in github issue comments)

   [9]: https://github.com/jzaefferer/grunt-html
   [10]: https://github.com/svenkreiss/html5validator
   [11]: https://github.com/cvrebert/lmvtfy/

## Usage

You can use the `vnu.jar` markup checker as an executable for command-line
checking of HTML documents by invoking it like this:

      java -jar ~/vnu.jar [--errors-only] [--no-stream]
           [--format gnu|xml|json|text] [--help] [--html] [--verbose]
           [--version] FILES

**Note:** In these instructions, replace _"~/vnu.jar"_ with the actual path to
the file on your system.

To check one or more HTML documents from the command line:

      java -jar ~/vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -jar ~/vnu.jar FILE.html...

To check all HTML documents in a particular directory:

      java -jar ~/vnu.jar some-directory-name/

To check a Web document:

      java -jar ~/vnu.jar _URL_

      example: java -jar ~/vnu.jar http://example.com/foo

To check standard input:

      java -jar ~/vnu.jar -

      example: echo '<!doctype html><title>...' | java -jar ~/vnu.jar -

### Options

When used from the command line as described in this section, the `vnu.jar`
executable provides the following options:

#### --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported (so that warnings and info messages are not reported).

    default: [unset; all message reported, including warnings & info messages]

#### --format _format_

    Specifies the output format for reporting the results.

    default: "gnu"

    possible values: "gnu", "xml", "json", "text"

    see also:
    [wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out][12]

   [12]: https://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out

#### --help

    Shows detailed usage information.

#### --html

    Forces all documents to be parsed by the HTML parser, as text/html
    (otherwise, *.xhtml documents are parsed using an XML parser).

    default: [unset; *.xhtml documents are parsed using an XML parser]

#### --no-stream

    Forces all documents to be be parsed in buffered mode instead of streaming
    mode (causes some parse errors to be treated as non-fatal document errors
    instead of as fatal document errors).

    default: [unset; non-streamable parse errors cause fatal document errors]

#### --verbose

    Specifies "verbose" output. (Currently this just means that the names of
    files being checked are written to stdout.)

    default: [unset; output is not verbose]

#### --version

    Shows the vnu.jar version number.

To provide browser-based checking of documents over the Web, see [Web-based
checking with vnu.war or vnu.jar][13].

   [13]: http://validator.github.io/service.html

