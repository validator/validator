This document describes the entire request/response transaction through
the checker.

## The Servlet

The [[servlet|System » Servlet]] has entry points for `GET`,
`POST`, `OPTIONS` and `TRACE`. The super class implements HEAD by
calling the `GET` entry point.

The implementation for `TRACE` simply returns 405 Method Not Allowed as
a tinfoil hat measure.

The implementations for `GET` and `OPTIONS` special-case /robots.txt and
then delegate to `doPost` which is written to handle `GET` and `OPTIONS`
in addition to `POST`.

Path info and server name are tested against configuration values in the
order: generic, HTML5 and parsetree. When a match is found, a controller
class is instantiated for the appropriate facet and its `service()`
method called for `GET` and `POST` or an in-servlet response without a
controller is constructed for `OPTIONS`.

## The Controller

### `service()`

The entry point is the `service()` method. It starts by setting up a
bunch of variables that pertain to the whole transaction (and, from the
transaction point of view, are practically constants). In particular, it
sets up the output pipeline to match the requested output format.

If validation is going to be performed (i.e. there is POSTed input or a
GET parameter document URI), cache control is set to non-cacheable.
Else, if the output format is (X)HTML, the `Last-Modified` date is set
to the modification date of `presets.txt`. Else, a 400 Bad Request
response is sent.

Schema URLs and the parser mode setting are initialized for user input
if provided (in the generic facet only).

Other configurable parameters are acted on in both the HTML5 and the
generic facet.

The output pipeline is set up so that the `errorHandler` field points to
a handler that writes into the output.

If the output format is (X)HTML, the controller calls into
`PageEmitter`, which will result in a call back to `validate()`. For
other formats, `validate()` is called directly.

### `validate()`

The entity resolver is initalized.

An attempt to initialize the validator chain is made. It succeeds in the
generic facet if the user has explicitly chosen schemas.

`loadDocAndSetupParser()` is called. This takes different code paths for
the HTML5 facet and the generic facet.

The error handler is connected to the parser.

The `SourceCode` instance is intitialized from the document
`InputSource`.

If the user has opted for normalization checking or if the schema is on
autodetect, normalization checking in the parser is enabled.

The parser is wrapped in `WiretapXMLReaderWrapper` that reports each
parse event to `SourceCode` for location tracking before the parse event
proceeds ahead in the pipeline.

If the parser is the HTML parser, the error handler is set to HTML
reporting mode (no namespace cruft). If the parser is the XML parser and
there are namespaces to filter out, the XML parser is wrapped in a
`NamespaceDroppingXMLReaderWrapper`.

The error handler for the lowest level of parsin (HTML tokenizer or XML
parser underneath filters) is set to the “exact” variant that identifies
individual UTF-16 code units instead of whole SAX event-long source text
runs.

In the XML parser case, the error handler is made unchangeable in order
to work around problems in `org.xml.sax.helpers.XMLFilterImpl`.

The parser is wrapped in `AttributesPermutingXMLReaderWrapper` which
changes the order in which the validation layer sees attributes. This
improves the user experience with co-occurrence constraints.

If the character encoding override is in effect, the character encoding
information on the input source is changed.

The parser is started. The parse causes errors to be pushed to the error
handler.

Various exceptions are caught and they are logged or reported as
appropriate.

### `loadDocAndSetupParser()` (Generic)

Behavior depends on the parser setting:

#### Any HTML

If the preset is an HTML-unsafe preset, an error is reported and an
exception thrown. When JavaScript is on, the UI script should prevent
user from hitting this case from the (X)HTML UI.

The entity resolver is set to allow HTML but not XML types (subject to
lax type setting).

`loadDocumentInput()` is called.

A new HTML parser is instantiated. The doctype expectation is set
according to the chosen parser mode.

An attempt to initialize the validator chain is made. It succeeds in the
parser is set to a specific HTML flavor. The controller is set as a
handler for the doctype callback from the parser.

#### Either XML

The entity resolver is set to allow XML but not HTML types (subject to
lax type setting).

`loadDocumentInput()` is called.

An XML parser is initialized according to the chosen external entity
mode.

If the validator is still `null` at this point, the
`BufferingRootNamespaceSniffer` is added to the filter pipeline with the
controller as the callback handler.

#### Auto

The entity resolver is set to allow both XML and HTML types.

`loadDocumentInput()` is called.

If the `Content-Type` of the document is `text/html` proceed as in the
auto-HTML case.

Else, proceed as in the no external entities XML case.

### `loadDocAndSetupParser()` (HTML5)

The entity resolver is set to `Accept` `application/xhtml+xml` and
`text/html`.

`loadDocumentInput()` is called.

If the `Content-Type` of the document is `text/html` set up the
validator chain with the HTML5 preset and instantiate the HTML parser
with the HTML5 doctype expectation.

Else set up the validator chain with the XHTML5+SVG1.1+MathML2 preset
instantiate the XML parser set not to load external entities.

### `loadDocumentInput()`

If the request method was `GET` or `HEAD` load the document using the
entity resolver and set up the result as the validator input.

Else (`POST` case) set up the validator input from the request body and
the `Content-Type` and (optionally) `Content-Location` headers. (Note:
Servlet filters emulate straight `POST` in the HTML form submission
case, so the servlet always treat the request as a straight `POST`.)
