This page describes the checker’s GNU output format.

This format is an adaptation of the standard
[GNU error format](https://www.gnu.org/prep/standards/standards.html#Errors).

## Media Type

This format has semantics beyond the semantics of `text/plain`. However,
for compatibility and given the lack of a specific media type, this
format uses the media type `text/plain; charset=utf-8`.

## Character Encoding

This format is defined in terms of Unicode characters. For transport as
bytes, the Unicode characters are encoded as UTF-8.

## General Format

The format consists of messages represented as text lines.

Each line consists of the URI of the file that the message pertains to,
U+003A COLON, optionally a position descriptor, U+003A COLON if there
was a position descriptor, U+0020 SPACE, type descriptor, U+003A COLON,
U+0020 SPACE, message and U+000A LINE FEED.

When there are no lines, there are characters—not even a single U+000A
LINE FEED.

## URI of the File

The URI of the file is its IRI converted to the URI form with U+0022
QUOTATION MARK before and after or nothing (not even quotes) if the IRI
of the document is not available. (Literal U+0022 QUOTATION MARK never
appears in the URI. `%22` may appear instead.)

## Position Descriptor

The position descriptor indicates the source position that the message
pertains to in terms of lines and columns. The first line is line number
1. The first character on a line occupies column number 1. Columns are
counted as UTF-16 code units without tab expansion. (The GNU spec
doesn't specify how non-ASCII is counted and specifies tab expansion to
stops at every 8 columns.)

The position descriptor takes one of these formats:

-   *line number*
-   *line number*, U+002E FULL STOP, *column number*
-   *start line number*, U+002D HYPEN-MINUS, *end line number*
-   *start line number*, U+002E FULL STOP, *start column number*, U+002D
    HYPEN-MINUS, *end line number*, U+002E FULL STOP, *end column
    number*

Start and end are inclusive. The numbers consist of one or more
characters in the range from U+0030 DIGIT ZERO to U+0039 DIGIT NINE
interpreted as a decimal number.

## Type Descriptor

The type descriptor consists of a supertype descriptor optionally
followed by U+0020 SPACE and a subtype descriptor.

The supertype descriptor denotes the general class of the message. The
permissible values are `info`, `error` and `non-document-error`.

`info` means an informational message or warning that does not affect
the validity of the document being checked. `error` signifies a problem
that causes the validation/checking to fail. `non-document-error`
signifies an error that causes the checking to end in an indeterminate
state because the document being validated could not be examined to the
end. Examples of such errors include broken schemas, bugs in the
validator and IO errors. (Note that when a schema has parse errors, they
are first reported as `error`s and then a catch-all `non-document-error`
is also emitted.)

When the supertype descriptor is `info` the permissible value for the
subtype descriptor is `warning`, which means that the message seeks to
warn about the user of a formally conforming but in some way
questionable issue. Otherwise, the message is taken to generally
informative.

When the supertype descriptor is `error` the permissible value for the
subtype descriptor is `fatal`, which means that the error is an XML
well-formedness error or, in the case of HTML, a condition that the
implementor has opted to treat analogously to XML well-formedness errors
(e.g. due to usability or performance considerations). Further errors
are suppressed after a fatal error. In the absence of the `"subtype"`
key, a `"type":"error"` message means a spec violation in general.

When the supertype descriptor is `non-document-error` the permissible
value for the subtype descriptor are `io` (signifies an input/output
error), `schema` (indicates that initializing a schema-based validator
failed) and `internal` (indicates that the validator/checker found an
error bug in itself, ran out of memory, etc., but was still able to emit
a message). In the absence of the subtype descriptor key, a
`non-document-error` message means a problem external to the document in
general.

## Message

The message is a human-readable string that does not contain U+000A LINE
FEED or U+000D CARRIAGE RETURN. It may be the empty string.
[[ASCII quotes can be requested|Service » Common params]]

## Processing Model

Clients that consume the message format are referred to as processors.

If the input contains a line that is not in the format described above,
the input is deemed to be in an unknown format and not processable
according to this processing model.

For forward compatibility, processors must treat unknown subtype
descriptors as if there were no subtype descriptor when deciding the
semantics according to the previous paragraphs.

Processors must process the lines in a way that is consistent with the
semantics of the lines.

### Determining Outcome

The outcome of the validation process may be success, failure or
indeterminate.

1.  If there are one or more `non-document-error` messages, the outcome
    is indeterminate.
2.  Else if there are one or more `error` messages, the outcome is
    failure.
3.  Else the outcome is success.

Note that `info` messages can be suppressed by setting the input
parameter [[`level`|Service » Common params]] to `error`
in which case success is equivalent to this format containing no lines.

See also: [[Web Service Interface|Service » HTTP interface]]