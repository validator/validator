This page describes the checker’s XML output format.

## Goal

The native XML output format for the checker for integration into
content management systems, etc. This format should be able to expose
everything there is to expose in the checker results. (Other XML
formats may not fit the checker exactly.)

Note: The format has been designed to support streaming generation and
consumption.

## Media Type

The Internet media type for this format is `application/xml`.

## Namespaces

The elements in this XML vocabulary are in the namespace
“`http://n.validator.nu/messages/`”. This vocabulary reuses elements
from the “`http://www.w3.org/1999/xhtml`” namespace for human-readable
messages. The semantics for the elements in the
“`http://www.w3.org/1999/xhtml`” namespace are defined in [HTML
5](http://www.whatwg.org/specs/web-apps/current-work/).

-   Perhaps the namespace URI should be a data: URI. If the ns URI does
    not contain any domain name, it cannot contain a domain name that
    someone is uncomfortable with. [hsivonen](User:Hsivonen "wikilink")
    14:24, 18 December 2006 (UTC)

The attributes in this XML vocabulary are not in a namespace. The
attribute values defined for this XML vocabulary must not have preceding
or trailing white space.

## Structure and Semantics

The format consists of an XML 1.0 document that has the element
`messages` as the root element.

The root elements may contain zero or more messages elements (`info`,
`error` and `non-document-error`), optionally followed by one `source`
element, optionally followed by one `language` element.

The root element may have an optional attribute `url`. The `url`
attribute, if present, must contain the URI (not IRI) of the document
being checked or the literal string “`data:…`” (the last character is
U+2026) to signify that the message is associated with a data URI
resource but the exact URI has been omitted. (If a client application
wishes to show IRIs to human users, it is up to the client application
to convert the URI into an IRI.)

### Message Elements

The element `info` means an informational message or warning that does
not affect the validity of the document being checked. The element
`error` signifies a problem that causes the validation/checking to fail.
`non-document-error` signifies an error that causes the checking to end
in an indeterminate state because the document being validated could not
be examined to the end. Examples of such errors include broken schemas,
bugs in the validator and IO errors. (Note that when a schema has parse
errors, they are first reported as `error`s and then a catch-all
`non-document-error` is also emitted.)

#### Locator Attributes

The elements `info`, `error` and `non-document-error` have five optional
attributes for indicating the context of the message: `url`,
`first-line`, `last-line`, `first-column` and `last-column`. The
`first-column` attribute must not be present unless the `first-line`
attribute is present as well. The `last-column` attribute must not be
present unless the `last-line` attribute is present as well. The
`first-line` attribute must not be present unless the `last-line`
attribute is present as well.

The `url` attribute, if present, must contain the URI (not IRI) of the
resource with which the message is associated or the literal string
“`data:…`” (the last character is U+2026) to signify that the message is
associated with a data URI resource but the exact URI has been omitted.
(If a client application wishes to show IRIs to human users, it is up to
the client application to convert the URI into an IRI.)

If the `url` attribute is absent on the message element but present on
the root element, the message is considered to be associated with the
resource designated by the attribute on the root element.

The `first-line`, `last-line`, `first-column` and `last-column`
attribute, if present, must contain a string consisting of characters in
the range U+0030 DIGIT ZERO to U+0039 DIGIT NINE which when interpreted
as a base-ten integer is a positive integer (not zero). The line and
column numbers are one-based. The first line is line 1. The first column
is column 1. Columns are counted by UTF-16 code units. A line break is
considered to occupy the last column on the line it terminates.

The source lines and columns are approximate. For example, if a message
is related to an attribute, the line and column may point to the first
character if the start tag, the character after the start tag or to the
attribute inside the tag depending on implementation. If a message is
related to character data, the line and column may be inaccurate within
a run of text e.g. due to buffering.

The `last-line` attribute indicates the last line (inclusive) onto which
the source range associated with the message falls.

The `first-line` attribute indicates the first line onto which the
source range associated with the message falls. If the attribute is
missing, it is assumed to have the same value as `last-line`.

The `last-column` attribute indicates the last column (inclusive) onto
which the source range associated with the message falls on the last
line onto which is falls.

The `first-column` attribute indicates the first column onto which the
source range associated with the message falls on the first line onto
which is falls.

#### The `type` Attribute

The `info`, `error` and `non-document-error` element may have an
attribute called `type` for indicating the type of the message in more
detail.

The permissible value on the `info` element is `warning`, which means
that the message seeks to warn about the user of a formally conforming
but in some way questionable issue. Otherwise, the message is taken to
generally informative.

The permissible value on the `error` element is `fatal`, which means
that the error is an XML well-formedness error or, in the case of HTML,
a condition that the implementor has opted to treat analogously to XML
well-formedness errors (e.g. due to usability or performance
considerations). Further errors are suppressed after a fatal error. In
the absence of the `type` attribute, the element means a spec violation
in general.

Permissible values on the `non-document-error` element are: `io`
(signifies an input/output error), `schema` (indicates that initializing
a schema-based validator failed) and `internal` (indicates that the
validator/checker found an error bug in itself, ran out of memory, etc.,
but was still able to emit a message). In the absence of the `type`
attribute, the element means a problem external to the document in
general.

#### Children of Message Elements

The `info`, `error` and `non-document-error` elements may contain the
following optional elements (in this order): `message`, `elaboration`
(only if `message` is present as well) and `extract`.

##### The `message` Element

The `message` element represents a paragraph of text that is the message
stated succinctly in natural language. Permissible element content
consists of an interleaving of zero or more text nodes, zero or more `a`
elements in the “`http://www.w3.org/1999/xhtml`” namespace and zero or
more `code` elements in the “`http://www.w3.org/1999/xhtml`” namespace.
The `code` elements in the “`http://www.w3.org/1999/xhtml`” namespace
may contain text. The `a` elements in the
“`http://www.w3.org/1999/xhtml`” namespace may contain an interleaving
of zero or more text nodes and zero or more `code` elements in the
“`http://www.w3.org/1999/xhtml`” namespace. The `a` elements in the
“`http://www.w3.org/1999/xhtml`” namespace must have the attribute
`href` and may have the attribute `title`.

##### The `elaboration` Element

The `elaboration` element provides additional human-readable guidance
related to the message. The content model of this element is block level
content (elements in the “`http://www.w3.org/1999/xhtml`” namespace) as
defined by [HTML 5](http://www.whatwg.org/specs/web-apps/current-work/).

##### The `extract` Element

The `extract` element represents an extract of the document source from
around the point in source designated for the message by the `line` and
`column` attributes on the message element. The `extract` element
contains an interleaving zero or more text nodes and exactly one `m`
element. The `m` element represents a highlighted part of the extract
that pinpoints the source position associated with the message. The `m`
element contains the highlighted part of the text. White space is
significant in the subtree rooted at `extract`.

### The `source` Element

The `source` element represents the source of the checked document as
decoded to Unicode with XML-unsafe characters replaced with the
REPLACEMENT CHARACTER and with line breaks replaced with U+00A0 LINE
FEED. The element may contain text that is the source. White space is
significant in the content.

The element has two optional attributes: `type` and `encoding`. The
`type` attribute represents the media type of the input without
parameters. The `encoding` attribute represents the `charset` media type
parameter.

### The `language` Element

The `"language"` element represents the detected language of the checked
document. Its value is a BCP 47 language tag. The absence of the
`"language"` element indicates that the language of the checked document
could not be determined with confidence.

## Processing Model

Clients that consume the message format are referred to as processors.
They must use a conforming XML 1.0 processor to parse the format.

If the root element is not an element named `messages`, the document is
deemed to be in an unknown format and not processable according to this
processing model.

If a processor encounters an element that it doesn’t recognize, it must
process the content of the element as if the start tag and the end tag
of the element were not there. If the processor encounter character data
as a child of the root or a message element element (after applying the
rule stated in the previous sentence), it must act as if the character
data was not there. If a processor encounters an attribute that it does
not recognize, it must ignore the entire attribute. If a processor
encounters an attribute that it does recognize but the value of the
attribute is not permissible under the previous section, the processor
must ignore the entire attribute. If an `info`, `error` or
`non-document-error` element does not have a `last-line` attribute with
a permissible value, a `last-column` attribute on the element must be
ignored if present. If an `info`, `error` or `non-document-error`
element does not have a `first-line` attribute with a permissible value,
a `first-column` attribute on the element must be ignored if present.

Processors must process elements in a way that is consistent with the
semantics of the elements.

### Determining Outcome

The outcome of the validation process may be success, failure or
indeterminate.

1.  If there are one or more `non-document-error` elements, the outcome
    is indeterminate.
2.  Else if there are one or more `error` elements, the outcome is
    failure.
3.  Else the outcome is success.

## Prior Art

The W3C has defined three XML output formats for the W3C Validator: [the
SOAP format](http://validator.w3.org/docs/api.html), [the Unicorn
format](http://www.w3.org/QA/2006/obs_framework/response/) and
[EARL](http://www.w3.org/TR/EARL10/). Relaxed has an XML format, but I’m
not aware of a spec for it.

I think there are two problems with the SOAP and Unicorn formats: they
are unnecessarily complex and they don’t support streaming output. For
example, they require a redundant declaration of the number of errors
before the errors themselves (which a client could count on its own if
it wants to know the number).

The EARL format assumes that each testable condition has a well-known
URI, which does not fit with grammar-based validation and now even with
vanilla Schematron.

The W3C Validator also provides simple pass/fail information as [HTTP
headers](http://validator.w3.org/docs/api.html#http_headers), which is
nice if you only care about a boolean pass/fail. However, this approach
also has the problem the it precludes streaming, because the validation
process has to finish before the HTTP headers can be written.

For these reasons, I am not particularly keen on reusing the output
formats of the W3C Validator unless it turns out that there are
significant [network
benefits](https://en.wikipedia.org/wiki/Network_effect) to be reaped from
plugging into an existing network of client software. It seems to me
that there isn’t a significant network of existing client software.

See also: [[Web Service Interface|Service » HTTP interface]]
