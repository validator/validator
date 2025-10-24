The checker accepts the following parameters regardless of input
method. How these parameters are communicated depends on the input
method.

**Note:** The `validator.nu` deployment of the checker has a generic facet at
`https://validator.nu/` and an (X)HTML5 facet at `https://html5.validator.nu/`.
Some parameters apply only to the generic facet. All parameters listed
here are optional.

## Parameters for all facets

### `out`

* *none*: HTML
* `xhtml`: XHTML
* `xml`: [[XML|Output » XML]]
* `json`: [[JSON|Output » JSON]]
* `gnu`: [[GNU error format|Output » GNU]]
* `text`: Human-readable text (not for machine parsing)

### `showsource`

The only supported value is `yes` which means that source is shown (if
supported by the output format).

### `level`

The only supported value is `error` which means that only errors and
non-document errors are reported. That is, informative messages
*including warnings* are not reported.

### `nsfilter`

Space-separated list of XML namespace URIs. Elements (and their
children) and attributes in those namespaces are filtered out between
the XML parser and the validation layer. The filtered elements
participate in ID uniqueness checking. The filter doesn’t apply to HTML
parser.

### `checkerrorpages`

The only supported value is `yes`, which means that the checker will
retrieve a document and check it even if the response from the server
is a 404 or other non-200 status. Otherwise the checker will not
retrieve the document but will instead emit a message indicating the
document was not retrieved, along with the HTTP status code.

## Parameters for the generic facet only

### `schema`

This parameter takes a space-separated list of schema IRIs (`http` or
`https`). The schemas can be RELAX NG 1.0 schemas, Schematron 1.5
schemas or identifiers for built-in non-schema-based checkers.

### `laxtype`

The only supported value is `yes` which means that RFC 3023 character
encoding defaults are disrespected and `text/html` is accepted as an XML
MIME type.

### `parser`

Value       | Meaning
----------- | -------
*none*      | Choice of HTML or XML parser is based on `Content-Type`.
`html`      | HTML parser.
`html5`     | HTML parser. (The `html5` value is just an alias for the `html` value.)
`xml`       | XML parser, will not load external entities.
`xmldtd`    | XML parser, will load external entities.

## Format-specific parameters

These parameters are specific to only some output formats.

### `asciiquotes`

The only supported value is `yes` which means that ASCII quotes and
apostrophes will be substituted for the Unicode smart quotes. This
parameter only applies with `out=text` and `out=gnu`. The output may
still contain Unicode characters in general.

### `callback`

When this parameter is present, its value is taken to be the name of a
JavaScript callback function to which the JSON object is given as an
argument. This parameter only applies with `out=json`. The value must
not be a JavaScript reserved word.

See also: [[Web Service Interface|Service » HTTP interface]]