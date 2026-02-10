# NAME

vnu – The Nu Html Checker

# SYNOPSIS

## On Linux or macOS
`vnu-runtime-image/bin/vnu [OPTIONS]... FILES|DIRECTORY|URL...`

## On Windows
`vnu-runtime-image\bin\vnu.bat [OPTIONS]... FILES|DIRECTORY|URL...`

## On any system with JBang installed
`jbang vnu@validator/validator [OPTIONS]... FILES|DIRECTORY|URL`

## On any system with Java11+ installed
`java -jar ~/vnu.jar [OPTIONS]... FILES|DIRECTORY|URL...`

`vnu [OPTIONS]... FILES|DIRECTORY|URL...`

## Where:

- **FILES** are documents to check. To read from stdin, use "**-**" (that is, a dash) in place of **FILES**.
- **DIRECTORY** the path to a directory containing all files to check
- **URL** the URL to a document
- **OPTIONS** are zero or more of:

    --asciiquotes --errors-only --Werror --exit-zero-always --stdout
    --filterfile FILENAME --filterpattern PATTERN --format gnu|xml|json|text
    --help --skip-non-css --css --skip-non-svg --svg --skip-non-html --html
    --xml --also-check-css --also-check-svg --user-agent USER_AGENT
    --no-langdetect --no-stream --verbose --version --entities --schema SCHEMA
    --skip-info-messages --additional-request-header "HEADER: VALUE"

# DESCRIPTION

The Nu Html Checker (vnu) helps you catch unintended mistakes in your HTML,
CSS, and SVG. It enables you to batch-check documents from the command line
and from other scripts/apps, and to deploy your own instance of the checker
as a service (like https://validator.w3.org/nu/).

# OPTIONS

When used from the command line, the vnu checker provides these options:

## --asciiquotes

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.

    default: [unset; Unicode smart quotation marks are used in messages]

## --skip-info-messages

    Specifies that only error-level messages and non-document-error messages 
    and warnings are reported — but not info messages.

    default: [unset; all messages reported, including warnings & info messages]


## --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported (so that warnings and info messages are not reported).

    default: [unset; all messages reported, including warnings & info messages]

## --Werror

    Makes vnu exit non-zero if any warnings are encountered (even if
    there are no errors).

    default: [unset; checker exits zero if only warnings are encountered]

## --exit-zero-always

    Makes vnu exit zero even if errors are reported for any documents.

    default: [unset; checker exits 1 if errors are reported for any documents]

## --stdout

    Makes vnu report errors and warnings to stdout rather than stderr.

    default: [unset; checker reports errors and warnings to stderr]

## --filterfile _FILENAME_

    Specifies a filename. Each line of the file contains either a regular
    expression or starts with "#" to indicate the line is a comment. Any error
    message or warning message that matches a regular expression in the file is
    filtered out (dropped/suppressed).

    default: [unset; checker does no message filtering]

## --filterpattern _REGEXP_

    Specifies a regular expression. Any error message or warning message that
    matches the regular expression is filtered out (dropped/suppressed).

    As with all other checker options, this option may only be specified once.
    So to filter multiple error messages or warning messages, you must provide a
    single regular expression that will match all the messages. The typical way
    to do that for regular expressions is to OR multiple patterns together using
    the "|" character.

    default: [unset; checker does no message filtering]

## --format _format_

    Specifies the output format for reporting the results.

    default: "gnu"

    possible values: "gnu", "xml", "json", "text" [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#out

## --help

    Shows detailed usage information.

## --skip-non-css

    Check documents as CSS but skip documents that don’t have *.css extensions.

    default: [unset; all documents found are checked]

## --css

    Force all documents to be checked as CSS, regardless of extension.

    default: [unset]

## --skip-non-svg

    Check documents as SVG but skip documents that don’t have *.svg extensions.

    default: [unset; all documents found are checked]

## --svg

    Force all documents to be checked as SVG, regardless of extension.

    default: [unset]

## --skip-non-html

    Skip documents that don’t have *.html, *.htm, *.xhtml, or *.xht extensions.

    default: [unset; all documents found are checked, regardless of extension]

## --html

    Forces any *.xhtml or *.xht documents to be parsed using the HTML parser.

    default: [unset; XML parser is used for *.xhtml and *.xht documents]

## --xml

    Forces any *.html documents to be parsed using the XML parser.

    default: [unset; HTML parser is used for *.html documents]

## --also-check-css

    Check CSS documents (in addition to checking HTML documents).

    default: [unset; no documents are checked as CSS]

## --also-check-svg

    Check SVG documents (in addition to checking HTML documents).

    default: [unset; no documents are checked as SVG]

## --user-agent _USER_AGENT_

    Specifies the value of the User-Agent request header to send when checking
    HTTPS/HTTP URLs.

    default: "Validator.nu/LV"

## --additional-request-header "_HEADER_: _VALUE_"

    Specifies a custom HTTP request header and value to send when checking
    HTTPS/HTTP URLs. This option can be specified multiple times to add multiple
    headers. The header name and value must be separated by a colon and space.

    example: --additional-request-header "X-API-Key: abc123"

    default: [unset; no additional request headers are sent]

## --no-langdetect

    Disables language detection, so that documents are not checked for missing
    or mislabeled html[lang] attributes.

    default: [unset; language detection & html[lang] checking are performed]

## --no-stream

    Forces all documents to be be parsed in buffered mode instead of streaming
    mode (causes some parse errors to be treated as non-fatal document errors
    instead of as fatal document errors).

    default: [unset; non-streamable parse errors cause fatal document errors]

## --verbose

    Specifies "verbose" output. (Currently this just means that the names of
    files being checked are written to stdout.)

    default: [unset; output is not verbose]

## --version

    Shows the vnu version number.

# EXAMPLES

The examples in this section assume you have the `vnu-runtime-image/bin` or
`vnu-runtime-image\bin` directory in your system `PATH` environment variable.
If you’re using the jar file instead, replace `vnu` in the examples with
`java -jar ~/vnu.jar`.


To check one or more documents from the command line:

      vnu-runtime-image/bin/vnu      FILE.html FILE2.html FILE3.html...

      vnu-runtime-image\bin\vnu.bat  FILE.html FILE2.html FILE3.html...

      java -jar ~/vnu.jar            FILE.html FILE2.html FILE3.html...


To check all documents in a particular directory `DIRECTORY_PATH` as HTML, but skip any documents whose names don’t end with the extensions `.html`, `.htm`, `.xhtml`, or `.xht`:

      vnu --skip-non-html DIRECTORY_PATH

To check all documents in a particular directory as CSS:

      vnu --css DIRECTORY_PATH

To check all documents in a particular directory as CSS, but skip any documents whose names don’t end with the extension `.css`:

      vnu --skip-non-css DIRECTORY_PATH

To check all documents in a particular directory, with documents whose names end in the extension `.css` being checked as CSS, and all other documents being checked as HTML:

      vnu --also-check-css DIRECTORY_PATH

To check all documents in a particular directory as SVG:

      vnu --svg DIRECTORY_PATH

To check all documents in a particular directory as SVG, but skip any documents whose names don’t end with the extension `.svg`:

      vnu --skip-non-svg DIRECTORY_PATH

To check all documents in a particular directory, with documents whose names end in the extension `.svg` being checked as SVG, and all other documents being checked as HTML:

      vnu --also-check-svg DIRECTORY_PATH

To check the document at URL `http://example.com/foo`

      vnu http://example.com/foo

To check the content from the standard input:

      echo '<!doctype html><title>...' | vnu -

      echo '<!doctype html><title>...' | java -jar ~/vnu.jar -


# NOTES

Throughout these examples, replace `~/vnu.jar` with the actual path to that
jar file on your system, and replace `vnu-runtime-image/bin/vnu` and
`vnu-runtime-image\bin\vnu.bat` with the actual path to the `vnu` or
`vnu.bat` program on your system — or if you add the `vnu-runtime-image/bin`
or `vnu-runtime-image\bin` directory your system `PATH` environment variable,
you can invoke the vnu checker with just `vnu`.


If you get a `StackOverflowError` error when invoking vnu, try adjusting
the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar ...

      vnu-runtime-image/bin/java -Xss512k -m vnu/nu.validator.client.SimpleCommandLineValidator ...

# SEE ALSO

[vnu-client(1)](vnu-client.1.md), [vnu-server(1)](vnu-server.1.md)
