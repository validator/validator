<!-- -*- fill-column: 92 -*- vim: set textwidth=92 : -->

The purpose of this page is to enable collaborative creation of brief advisory text for each
HTML microsyntax so that when the content of an attribute value or the text content of an
element does not conform to a given microsyntax, conformance checkers can display the
advisory text about the syntax to guide the author to fix the content.

Please keep descriptions short: one paragraph in length.

### a-rel

A whitespace-separated list of link types, with no duplicate keywords in the list. Each link
type must be listed as allowed on `<a>` and `<area>` in the [HTML
specification](https://html.spec.whatwg.org/multipage/semantics.html#linkTypes), or must be
listed as allowed on `<a>` and `<area>` on the [Microformats
wiki](http://microformats.org/wiki/existing-rel-values#HTML5_link_type_extensions).
<strong>You can register link types on the [Microformats
wiki](http://microformats.org/wiki/existing-rel-values#HTML5_link_type_extensions)
yourself.</strong>

### autocomplete-any

A list of [autofill detail
tokens](https://html.spec.whatwg.org/multipage/forms.html#autofill-detail-tokens).

### browsing-context

A browsing context name is any string that does not start with an underscore (`_`).

### browsing-context-or-keyword

A browsing context name or keyword is either any string that does not start with an
underscore (`_`) or a string that case-insensitively matches one of: `_blank`, `_self`,
`_parent`, or `_top`.

### cdo-cdc-pair

Any text content that does not contain the character sequence "`<!--`" without a later
occurrence of the character sequence "`-->`".

### charset

An preferred encoding name according to the [Encoding
Standard](https://encoding.spec.whatwg.org/). Example: `utf-8`

### circle

A circle is specified as three comma-separated (no spaces allowed) integers the last one of
which is non-negative. An integer consists of one or more digits (`0`–`9`), optionally
preceded by a hyphen (`-`). Examples of circles: `5,5,10` and `-5,0,20`

### color

A [CSS color](https://drafts.csswg.org/css-color/#typedef-color); for example,
`lightseagreen`, `#b0e0e6`, `rgb(255,0,51)`, `hsl(120, 60%,70%)`, `rgba(255,0,0,0.7)`,
`hsla(240,100%,50%, 0.4)`, `hwb(120deg, 44%, 50%)`, `device-cmyk(0 81% 81% 30%)`,
`gray(50%)`, `color(blue blend(lime 50%))`,

### content-security-policy

A valid [Content Security
Policy](https://developer.mozilla.org/en-US/docs/Web/Security/CSP/Using_Content_Security_Policy#Writing_a_policy).

### custom-element-name

A [valid custom element
name](https://html.spec.whatwg.org/multipage/scripting.html#valid-custom-element-name).
Must contain a hyphen, must (after parsing) begin with a lowercase ASCII letter, and must
(after parsing) not contain any uppercase ASCII letters. (Remember that HTML parsers
lowercase element names but don’t lowercase attribute values.)

### date

A date in the form “*YYYY*`-`*MM*`-`*DD*”. Example: `2002-09-29`.

### datetime

An ISO 8601 date and time in the UTC time zone; i.e., “*YYYY*`-`*MM*`-`*DD*`T`*hh*`:`*mm*”
optionally followed by “`:`*ss*” for the seconds, optionally followed by “`.`” and one, two,
or three digits for the fraction of a second, and finally followed by “`Z`”. Examples:
`1996-01-01T12:05Z`, `1996-01-01T12:05:25.6Z`.

### datetime-local

An ISO 8601 date and time with no time zone information; i.e.,
“*YYYY*`-`*MM*`-`*DD*`T`*hh*`:`*mm*” optionally followed by “`:`*ss*” for the seconds,
optionally followed by `.` and one or more digits for the fraction of a second. Examples:
`1996-01-01T12:05`, `1996-01-01T12:05:25.6`.

### datetime-tz

A [global date and time
string](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-global-date-and-time-string);
that is, a <b>date</b>, followed by a “`T`” or a single space, followed by a <b>time</b>,
followed by <b>time-zone information</b>, where: the <b>date</b> must be in the form
“*YYYY*`-`*MM*`-`*DD*” • the <b>time</b> must begin in the form “*hh*`:`*mm*”, followed by
“`:`*ss*”, optionally followed by “`.`” and one, two, or three or digits • the <b>time-zone
information</b> must be either “`Z`” or in the form “`+`*hh*`:`*mm*” or the form
“`-`*hh*`:`*mm*” • Examples: `1996-01-01T12:05:25-02:00`, `1996-01-01T12:05:25Z`

### email-address

A [valid e-mail
address](https://html.spec.whatwg.org/multipage/forms.html#valid-e-mail-address) matching
the following regular expression:

```
^[a-zA-Z0-9.!#$%&'*+\/=?^_\`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$
```

### email-address-list

A set of comma-separated tokens, where each token is itself a valid [e-mail
address](#email-address).

### float

First, optionally, `-` (U+002D). Then, a series of one or more characters in the range
`0`…`9`. Then, optionally, a single `.` (U+002E) followed by a series of one or more
characters in the range `0`…`9`. Then, optionally, either `e` or `E`, optionally followed by
`-` (U+002D) or `+` (U+002B), followed by a series of one or more characters in the range
`0`…`9`. For example, `-42.42E+42` is valid but `.5` or `+2` are not.

### float-non-negative

A series of one or more characters in the range `0`…`9`. Then, optionally, a single `.`
(U+002E) followed by a series of one or more characters in the range `0`…`9`. Then,
optionally, either `e` or `E`, optionally followed by `-` (U+002D) or `+` (U+002B), followed
by a series of one or more characters in the range `0`…`9`. Or, alternatively to the
foregoing: First, `-` (U+002D). Then, a series of one or more zeros. Then, optionally, a
single `.` (U+002E) followed by one or more zeros. Then, optionally, either `e` or `E`,
optionally followed by `-` (U+002D) or `+` (U+002B), followed by a series of one or more
characters in the range `0`…`9`. For example, `42.42E+42` and `-000.000` are valid but `.5`
or `-0.01` are not.

### float-positive

A series of one or more characters in the range `0`…`9`. Then, optionally, a single `.`
(U+002E) followed by a series of one or more characters in the range `0`…`9`. One of the
digits so far has to be non-zero. Then, optionally, either `e` or `E`, optionally followed
by `-` (U+002D) or `+` (U+002B), followed by a series of one or more characters in the range
`0`…`9`. For example, `42.42E+42` is valid but `0.0` or `-2` are not.

### hash-name

A `#` (number sign) character followed by any string.

### ID

An ID consists of at least one character but must not contain any whitespace.

### image-candidate-strings

A comma-separated list of strings, each of which consists of a URL optionally followed by
either a pixel density descriptor or a width descriptor.

### image-candidate-strings-width-required

A comma-separated list of strings, each of which must consist of a URL followed by a width
descriptor.

### integer

One or more digits (`0`–`9`), optionally preceded by a hyphen (`-`). For example: `42` and
`-273` are valid, but `+42` is not.

### integer-non-negative

One or more digits (`0`–`9`). For example: `42` and `0` are valid, but `-273` is not.

### integer-positive

One or more digits (`0`–`9`), with at least one which is non-zero. For example: `42` is
valid, but `00` is not.

### integrity-metadata

A whitespace-separated list of values, where each value is consists of one of `sha256` or
`sha384` or `sha512`, followed by a hyphen (`-`), followed by a base64-encoded cryptographic
hash.

### iri

An absolute URL. For example: `http://example.org/hello`, but not `/hello`.  Spaces should
be escaped as `%20`.

### iri-ref

Any URL. For example: `/hello`, `#canvas`, or `http://example.org/`. Characters should be
represented in [NFC](http://www.macchiato.com/unicode/nfc-faq) and spaces should be escaped
as `%20`. Common non-alphanumeric characters other than `* ! $ & ' () ~ + - . / : ; = ? @ _`
generally must be [percent-encoded](https://en.wikipedia.org/wiki/Percent-encoding). For
example, the pipe character (`|`) must be encoded as `%7C`.

### iri-ref-http-or-https

Any [URL](#iri-ref) whose scheme is `http` or `https`.

### language

An [RFC 5646](https://tools.ietf.org/html/rfc5646) language tag consists of hyphen-separated
ASCII-alphanumeric subtags. There is a primary tag identifying a natural language by its
shortest ISO 639 language code (e.g. `en` for English) and zero or more additional subtags
adding precision. The most common additional subtag type is a region subtag which most
commonly is a two-letter ISO 3166 country code (e.g. `GB` for the United Kingdom). IANA
maintains a [registry of permissible
subtags](https://www.iana.org/assignments/language-subtag-registry).

### link-rel

A whitespace-separated list of link types listed as allowed on `<link>` in the [HTML
specification](https://html.spec.whatwg.org/multipage/semantics.html#linkTypes) or listed as
an allowed on `<link>` on the [Microformats
wiki](http://microformats.org/wiki/existing-rel-values#HTML5_link_type_extensions) without
duplicate keywords in the list. <strong>You can register link types on the [Microformats
wiki](http://microformats.org/wiki/existing-rel-values#HTML5_link_type_extensions)
yourself.</strong>

### media-query

One or more media queries, combined in a comma-separated list. Each media query consists of
an optional <strong>media type</strong> and zero or more expressions that check for the
conditions of particular <strong>media features</strong>. A media type is one of the
following: `all`, `print`, `screen`, or `speech`. The following media types are deprecated:
`braille`, `embossed`, `handheld`, `projection`, `tty`, and `tv`. For information about
valid media features and about the exact syntax of media queries, see the [Media
Queries](https://drafts.csswg.org/mediaqueries/) specification.

### meta-charset

The string `text/html;`, optionally followed by whitespace, followed by `charset=`, followed
by a preferred encoding name according to the [Encoding
Standard](https://encoding.spec.whatwg.org/). Example: `text/html; charset=utf-8`

### mime-type

A [media-type as defined in RFC 2616](https://tools.ietf.org/html/rfc2616#section-3.7); that
is, typically, a required *type*, followed by a "`/`" character, followed by a required
*subtype*, optionally followed by one or more instances of a "`;`" character followed by a
*parameter*. Examples: `text/css`, `text/css;charset=utf-8`.

### mime-type-list

(WF2)

### month

An ISO 8601 date with year and month; i.e., “*YYYY*`-`*MM*”. Example: `2007-11`.

### non-empty-string

Any string that is not the empty string.

### pattern

(WF2)

### polyline

...

### rectangle

...

### sandbox-allow-list

An unordered set of unique space-separated keywords; the allowed keywords are `allow-forms`,
`allow-modals`, `allow-pointer-lock`, `allow-popups`, `allow-popups-to-escape-sandbox`,
`allow-same-origin`,` allow-scripts`, and `allow-top-navigation`.

### script

Any text content that does not contain the character sequence "`<!--`" without a later
occurrence of the character sequence "`-->`" and that does not contain any occurrence of the
string "`</script`" followed by a space character, "`>`", or "`/`". For further details, see
[Restrictions for contents of script
elements](https://html.spec.whatwg.org/multipage/scripting.html#restrictions-for-contents-of-script-elements).

### script-documentation

Zero or more *code comments*, each of which is either a single-line comment starting with
"`//`" or a multi-line comment starting with "`/*" and ending with "*/`". The content must
also meet the constraints of the [script](#script) microsyntax. For further details, see
[Inline documentation for external
scripts](https://html.spec.whatwg.org/multipage/scripting.html#inline-documentation-for-external-scripts).

### simple-color

A string of seven characters that starts with `#` and ends with six characters each of which
is `0`…`9`, `a`…`f` or `A`…`F`.

### source-size-list

A comma-separated list of zero or more source sizes (\<media-condition\> \<length\>)
optionally followed by a default size (\<length\>) but at least one of them.

### string-without-line-breaks

Any string that does not contain the carriage return character or the line feed character.

### svg-pathdata

A list of zero or more path-data expressions, where each expression consists of a one-letter
command followed by numbers that serve as arguments for the command (in most cases, pairs of
coordinates). Commas and/or whitespace must be used to separate the arguments for a command
from one another—but <strong>commas must not be used to separate commands</strong>, though
whitespace can optionally be used to do so. Examples: "`M 100 100 L 300 100 L 200 300 z`" or
"`M100,100L300,100,200,300z`". For more information, see the [section on path data in the
SVG 1.1 specification](http://www.w3.org/TR/SVG11/paths.html#PathData).

### time

A time (hour, minute, seconds, fractional seconds) is encoded according to ISO 8601 with no
time zone: two digits (`0`–`9`) for the hour, a colon, two digits for the minute, optionally
a colon and two digits for the second, and optionally (if the seconds are present) a period
(`.`) and one, two, or three digits for the fraction of a second. All the numbers must be in
base ten and zero-padded if necessary. For instance: `23:59:00.000` or `00:00:05`.

### time-datetime

One of the following:
[month](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-month-string),
[date](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-date-string),
[yearless
date](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-yearless-date-string),
[time](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-time-string), [local
date and
time](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-local-date-and-time-string),
[time-zone
offset](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-time-zone-offset-string),
[global date and
time](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-global-date-and-time-string),
[week](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-week-string),
[non-negative
integer](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-non-negative-integer),
or
[duration](https://html.spec.whatwg.org/multipage/infrastructure.html#valid-duration-string).
For more information and examples, see the [section on the datetime value in the HTML
specification](https://html.spec.whatwg.org/multipage/semantics.html#datetime-value).

### week

A week consists of a year and a week number encoded according to ISO 8601: four or more
digits (`0`–`9`) representing the year, a hyphen (`-`), a literal `W`, and two digits for
the week, zero-padded if necessary. The week number must be a number greater than or equal
to `01`. Week `01` of a given year is the week containing the 4<sup>th</sup> of January;
weeks start on Monday. For instance: `2005-W52` is the week that ended on Sunday the first
of January, 2006.
