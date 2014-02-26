# How to use the vnu.jar markup checker

The `vnu.jar` application is a portable standalone version of the validator.nu
markup checker. The [latest vnu.jar release][1] is available from the
`validator` project at github. The following are instructions on how to use it
to check the markup of documents.

   [1]: https://github.com/validator/validator.github.io/releases

**Note:** In the instructions, replace _"~/vnu.jar"_ with the actual path to the
`vnu.jar` file on your system.

Alternatively, there’s also now a [Grunt plugin for HTML validation][2] that
uses `vnu.jar` as its backend. You can install that plugin with `npm install
grunt-html --save-dev`.

   [2]: https://github.com/jzaefferer/grunt-html

## Usage

You can use the `vnu.jar` markup checker as an executable for command-line
checking of HTML documents by invoking it like this:

      java -jar ~/vnu.jar [--errors-only] [--no-stream]
           [--format gnu|xml|json|text] [--help] [--html] [--verbose]
           [--version] FILES

To check one or more HTML documents from the command line:

      java -jar ~/vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...

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
    [http://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out][3]

   [3]: http://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out

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

For details on using the `vnu.jar` markup checker to provide a service for
browser-based checking of HTML documents over the Web, see [Using vnu.jar for
Web-based markup checking][4].

   [4]: http://validator.github.io/service.html

