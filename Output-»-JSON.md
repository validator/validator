This page describe the checker’s JSON output format.

Italicized words, such as *object*, refer to JSON data types. “The
`"foo"` *datatype*” refers to an object of type *datatype* that is the
value associated with the key `"foo"` in the parent *object*.

## Media Type

The Internet media type for this format is `application/json`. (Unless
the callback extension is used, in which case the media type is
`application/javascript`.)

## Root Object

The root object is a JSON *object*. It has one mandatory key,
`"messages"`, and three optional keys, `"url"`, `"source"`, and
`"language"`.

The values for these keys are described below.

### The `"messages"` *array*

This *array* is an ordered collection of zero or more message
*object*s.

#### Message *object*s

A message *object* has one mandatory key, `"type"`, and seven optional
keys, `"subtype"`, `"message"`, `"extract"`, `"offset"`, `"url"`,
`"line"` and `"column"`.

##### The `"type"` *string*

The `"type"` *string* denotes the general class of the message. The
permissible values are `"info"`, `"error"` and `"non-document-error"`.

`"info"` means an informational message or warning that does not affect
the validity of the document being checked. `"error"` signifies a
problem that causes the validation/checking to fail.
`"non-document-error"` signifies an error that causes the checking to
end in an indeterminate state because the document being validated could
not be examined to the end. Examples of such errors include broken
schemas, bugs in the validator and IO errors. (Note that when a schema
has parse errors, they are first reported as `error`s and then a
catch-all `non-document-error` is also emitted.)

##### The `"subType"` *string*

The permissible value with `"type":"info"` is `"warning"`, which means
that the message seeks to warn about the user of a formally conforming
but in some way questionable issue. Otherwise, the message is taken to
generally informative.

The permissible value with `"type":"error"` is `"fatal"`, which means
that the error is an XML well-formedness error or, in the case of HTML,
a condition that the implementor has opted to treat analogously to XML
well-formedness errors (e.g. due to usability or performance
considerations). Further errors are suppressed after a fatal error. In
the absence of the `"subtype"` key, a `"type":"error"` message means a
spec violation in general.

Permissible values with `"type":"non-document-error"` are: `"io"`
(signifies an input/output error), `"schema"` (indicates that
initializing a schema-based validator failed) and `"internal"`
(indicates that the validator/checker found an error bug in itself, ran
out of memory, etc., but was still able to emit a message). In the
absence of the `"subtype"` key, a `"type":"non-document-error"` message
means a problem external to the document in general.

##### The `"message"` *string*

The `"message"` *string* represents a paragraph of text (suitable for
rendering to the user as plain text without further processing) that is
the message stated succinctly in natural language.

##### The `"extract"` *string*

The `"extract"` *string* represents an extract of the document source
from around the point in source designated for the message by the
`"line"` and `"column"` *number*s.

##### The `"offset"` *number*

The `"offset"` *number* is an UTF-16 code unit index into the
`"extract"` *string*. The index identifies the same UTF-16 code unit in
the extract that the `"line"` and `"column"` *number*s identify in the
full source. The first code unit has the index `0`.

##### The `"url"` *string*

The `"url"` *string*, if present, must contain the URI (not IRI) of the
resource with which the message is associated or the literal string
“`data:…`” (the last character is U+2026) to signify that the message is
associated with a data URI resource but the exact URI has been omitted.
(If a client application wishes to show IRIs to human users, it is up to
the client application to convert the URI into an IRI.)

If the `"url"` *string* is absent on the message element but present on
the root element, the message is considered to be associated with the
resource designated by the attribute on the root element.

##### The `"firstLine"`, `"firstColumn"`, `"lastLine"` and `"lastColumn"` *number*s

The `"firstLine"`, `"firstColumn"`, `"lastLine"` and `"lastColumn"`
*number*s indicate a range of source code associated with the message.
The line and column numbers are one-based. The first line is line 1. The
first column is column 1. Columns are counted by UTF-16 code units. A
line break is considered to occupy the last column on the line it
terminates.

The source lines and columns are approximate. For example, if a message
is related to an attribute, the line and column may point to the first
character if the start tag, the character after the start tag or to the
attribute inside the tag depending on implementation. If a message is
related to character data, the line and column may be inaccurate within
a run of text e.g. due to buffering.

The `"lastLine"` *number* indicates the last line (inclusive) onto which
the source range associated with the message falls.

The `"firstLine"` *number* indicates the first line onto which the
source range associated with the message falls. If the attribute is
missing, it is assumed to have the same value as `"lastLine"`.

The `"lastColumn"` *number* indicates the last column (inclusive) onto
which the source range associated with the message falls on the last
line onto which is falls.

The `"firstColumn"` *number* indicates the first column onto which the
source range associated with the message falls on the first line onto
which is falls.

### The `"url"` *string*

The `"url"` *string*, if present, must contain the URI (not IRI) of the
document being checked or the literal string “`data:…`” (the last
character is U+2026) to signify that the message is associated with a
data URI resource but the exact URI has been omitted. (If a client
application wishes to show IRIs to human users, it is up to the client
application to convert the URI into an IRI.)

### The `"source"` *object*

A `"source"` *object* has one mandatory key, `"code"`, and two optional
keys, `"type"` and `"encoding"`.

#### The `"code"` *string*

The `"code"` *string* represents the source of the checked document as
decoded to Unicode lone surrogates replaced with the REPLACEMENT
CHARACTER and with line breaks replaced with U+00A0 LINE FEED.

#### The `"type"` *string*

The `"type"` *string* represents the media type of the input without
parameters.

#### The `"encoding"` *string*

The `"encoding"` *string* represents the `charset` media type parameter
of the input.

### The `"language"` *string*

The `"language"` *string* represents the detected language of the checked
document. Its value is a BCP 47 language tag. The absence of the
`"language"` *string* indicates that the language of the checked document
could not be determined with confidence.

## Example

```json
{
    "url": "http://example.org/",
    "messages": [
        {
            "type"   : "info",
            "subtype": "warning",
            "lastLine"   : 20,
            "lastColumn" : 15,
            "url"    : "http://example.com/",
            "message": "Trailing slash for void element",
            "extract": "<br/>",
            "hiliteStart" : 3,
            "hiliteLength" : 1
        },
        {
            "type"   : "error",
            "subtype": "fatal",
            "lastLine"   : 42,
            "lastColumn" : 17,
            "url"    : "http://example.com/",
            "message": "Missing end tag for the “foo” element"
        }
    ],
    "source": {
        "code"    : "...",
        "type"    : "text/html",
        "encoding": "UTF-8"
    },
    "language": "en"
}
```

## Processing Model

Clients that consume the message format are referred to as processors.
They must use a parser conforming to [RFC
4627](https://tools.ietf.org/html/rfc4627) to parse the format.

If the root is not an *object* with the key `"messages"`, the JSON text
is deemed to be in an unknown format and not processable according to
this processing model.

If the processor encounters a key–value pair in an *object* with a known
key and an unknown value where a value enumerated in this specification
is expected, the processor must ignore the key–value pair. If a
processor encounters an *object* that is missing a required key
(possibly because it was ignored under the previous rule), the processor
must ignore the entire *object*. If a message *object* does not have a
`"line"` *number* with a permissible value, a `"column"` *number* on the
*object* must be ignored if present.

Processors must process the items in a way that is consistent with the
semantics of the items.

### Determining Outcome

The outcome of the validation process may be success, failure or
indeterminate.

1.  If there are one or more `non-document-error` messages, the outcome
    is indeterminate.
2.  Else if there are one or more `error` messages, the outcome is
    failure.
3.  Else the outcome is success.

## Callback

The format described here may
[[optionally be wrapped in a JavaScript function call|Service » Common params]].

See also: [[Web Service Interface|Service » HTTP interface]]
