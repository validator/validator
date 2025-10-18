# NAME

vnu-client – HTTP client to a server instance of the Nu Html Checker

# SYNOPSIS

`java -cp ~/vnu.jar              -D[property=value]... nu.validator.client.HttpClient [FILE]...`

`vnu-runtime-image/bin/java      -D[property=value]... nu.validator.client.HttpClient [FILE]...`

`vnu-runtime-image\bin\java.exe  -D[property=value]... nu.validator.client.HttpClient [FILE]...`

# DESCRIPTION

vnu-client is the HTTP client to run from the command line to either send
documents to a locally-running instance of the checker HTTP service
(vnu-server) — for fast command-line checking — or to a remote instance
anywhere on the Web.

# OPTIONS

## FILE

    File you wish to send to the server for check.

# PROPERTIES

When using the packaged HTTP client for sending documents to an instance of
the checker HTTP service for checking, you can set Java system properties to
control configuration options for the checker behavior.

Most of the properties listed below map to the common input parameters for the
checker service, as documented at [github.com/validator/validator/wiki/Service-»-Common-params][1].

   [1]: https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params

## nu.validator.client.host=hostname

    Specifies the hostname of the checker for the client to connect to.
    default: "127.0.0.1"

    example: nu.validator.client.host=127.0.0.1

## nu.validator.client.port=port

    Specifies the port of the checker for the client to connect to.
    default: "8888"

    example: nu.validator.client.port=8080

## nu.validator.client.path=path

    Specifies the path of the checker for the client to connect to.
    default: "/"

    example: nu.validator.client.path=/vnu

## nu.validator.client.level=error

    Specifies the severity level of messages to report; to suppress
    warning-level messages, and only show error-level ones, set this property to
    "error".
    default: [unset]
    possible values: "error"

    example: nu.validator.client.level=error

## nu.validator.client.parser=parser

    Specifies which parser to use.
    default: "html"; or, for *.xhtml input files, "xml"
    possible values: [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#parser

## nu.validator.client.charset=encoding

    Specifies the encoding of the input document.
    default: [unset]

## nu.validator.client.content-type=content-type

    Specifies the content-type of the input document.
    default: "text/html"; or, for *.xhtml files, "application/xhtml+xml"

## nu.validator.client.out=output-format

    Specifies the output format for messages.
    default: "gnu"
    possible values: [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#out

## nu.validator.client.asciiquotes=boolean

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.
    default: "yes"
    possible values: "yes" or "no"

# EXAMPLES

To send `FILE.html` to an instance of the checker at `http://html5.validator.nu/`, invoke:

    java -cp ~/vnu.jar -Dnu.validator.client.port=80 -Dnu.validator.client.host=html5.validator.nu nu.validator.client.HttpClient FILE.html

    vnu-runtime-image/bin/java -Dnu.validator.client.port=80 -Dnu.validator.client.host=html5.validator.nu nu.validator.client.HttpClient FILE.html

To suppress warning-level messages and only show error-level ones by setting the value of the `nu.validator.client.level` system property to `error`, like this:

    java -Dnu.validator.client.level=error -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html

    vnu-runtime-image/bin/java -Dnu.validator.client.level=error -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html

# SEE ALSO

[vnu-server(1)](vnu-server.1.md), [vnu(1)](vnu.1.md)
