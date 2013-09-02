# How to use the vnu.jar validator

The `vnu.jar` application is a portable standalone version of the validator.nu
validator. The [latest version][1] is available from the validator area at
github. The following are instructions on how to use it to validate documents.

   [1]: https://github.com/validator/validator.github.io/releases

**Note:** In the instructions, replace _"~/vnu.jar"_ with the actual path to the
`vnu.jar` file on your system.

## Usage

You can use the `vnu.jar` validator as an executable for command-line validation
of HTML documents by invoking it like this:

      java -jar ~/vnu.jar [--entities] [--errors-only] [--no-stream]
           [--format gnu|xml|json|text] [--help] [--html] [--schema URL]
           [--verbose] [--version] FILES

To validate one or more HTML documents from the command line:

      java -jar ~/vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...

To validate all HTML documents in a particular directory:

      java -jar ~/vnu.jar some-directory-name/

To validate a Web document:

      java -jar ~/vnu.jar _URL_

      example: java -jar ~/vnu.jar http://example.com/foo

To validate from standard input:

      java -jar ~/vnu.jar -

      example: echo '<!doctype html><title>...' | java -jar ~/vnu.jar -

### Options

When used from the command line as described in this section, the `vnu.jar`
executable provides the following options:

#### --entities

    Specifies that the XML parser should not load remote/external entities (such
    as DTDs) from the Internet.

    default: [unset; the XML parser will attempt to load external entities]

#### --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported.

    default: [unset; all message reported, including info- and warning-level]

#### --format _format_

    Specifies the output format for validation results.

    default: "gnu"

    possible values: "gnu", "xml", "json", "text"

    see also:
    [http://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out][2]

   [2]: http://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out

#### --help

    Shows detailed usage information.

#### --html

    Specifies that all documents should be parsed by the HTML parser as
    text/html (otherwise, *.xhtml documents are parsed by the XML parser).

    default: [unset; *.xhtml documents are parsed by the XML parser]

#### --no-stream

    Specifies that all documents parsed by the HTML parser will be parsed in
    buffered mode instead of streaming mode (causes some parse errors to be
    treated as non-fatal document errors instead of as fatal document errors).

    default: [unset; non-streamable parse errors cause fatal document errors]

#### --schema _URL_

    Specifies a URL for a known http://s.validator.nu/* schema to use for
    document validation.

    default: http://s.validator.nu/html5-all.rnc

#### --verbose

    Specifies that the validator output should be "verbose". (Currently this
    just means that the names of files being validated are written to stdout.)

    default: [unset; output is not verbose]

#### --version

    Shows the vnu.jar version number.

For details on using the `vnu.jar` validator to provide a service for
browser-based validation of HTML documents over the Web, see [Using vnu.jar for
Web-based validation][3].

   [3]: http://validator.github.io/web-based-usage.html

For more information... sources bugz etc.

