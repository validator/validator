The checker exposes an HTTP interface that allows it to be called
as a Web service. Input and output modes can
be chosen completely orthogonally. Responses and requests can be
optionally compressed (independently of each other).

(Please use the Web service API reasonably. See the [Terms of
Service](https://about.validator.nu/#tos).)

## Input Modes

For most Web-service use cases, you probably want to
[[POST the document as the HTTP entity body|Service » Input » POST body]] of the request.

### Implemented

- [[URL as a GET parameter|Service » Input » GET]]; service
  retrieves document by URL over HTTP or HTTPS.
- [[POSTed as the HTTP entity body|Service » Input » POST body]];
  parameters in query string as with GET.
- [[POSTed as a textarea value|Service » Input » textarea]]; parameters specified as form fields.
- [[POSTed as a form-based file upload|Service » Input » file upload]]; parameters specified as form fields.

### Not Implemented

-   Document in a `data:` URI as a GET parameter.
-   `application/x-www-form-urlencoded`

## Output Modes

When using the checker as a Web service back end, the
[[XML|Output » XML]] and [[JSON|Output » JSON]] output
formats are recommended for forward compatibility. The available JSON
tooling probably makes consuming JSON easier. The XML format contains
XHTML elaborations that are not available in JSON. Both formats are
streaming, but streaming XML parsers are more readily available. XML
cannot represent some input strings faithfully.

### Implemented

* HTML with microformat-style `class` annotations (default output;
  should not be assumed to be forward-compatibly stable).
* XHTML with microformat-style `class` annotations (append
  `&out=xhtml` to URL; should not be assumed to be forward-compatibly
  stable).
* [[XML|Output » XML]] (append `&out=xml` to URL).
* [[JSON|Output » JSON]] (append `&out=json` to URL).
* [[GNU error format|Output » GNU]] (append `&out=gnu` to URL).
* Human-readable plain text (append `&out=text` to URL; should not be
  assumed to be forward-compatibly stable for machine parsing—use the
  GNU format for that).

## Compression

The checker supports compression in order to save bandwidth.

### Request Compression

The checker supports HTTP request compression. To use it, compress the
request entity body using gzip and specify `Content-Encoding: gzip` as a
*request* header.

### Response Compression

The checker supports HTTP response compression. Please use it. Response
compression is orthogonal to the input methods and output formats.

The standard HTTP gzip mechanism is used. To indicated that you prepared
to handle gzipped responses, include the `Accept-Encoding: gzip` request
header. When the header is present, the checker will gzip compress the
response. You should also be prepared to receive an uncompressed,
though, since in the future it may make sense to turn off compression
under heavy CPU load.

## Sample Code

There a [sample Python program](https://about.validator.nu/html5check.py)
that shows how to deal with compression and redirects. (It may not be
exemplary Python, though.)

## CORS Example

You can also hit the API using
[CORS](https://developer.mozilla.org/en-US/docs/HTTP_access_control)
over AJAX. Basic example using jQuery:
```js
// easy way to get current pages HTML
$.get('#', function(html) {

    // emulate form post
    var formData = new FormData();
    formData.append('out', 'json');
    formData.append('content', html);

    // make ajax call
    $.ajax({
        url: "http://html5.validator.nu/",
        data: formData,
        dataType: "json",
        type: "POST",
        processData: false,
        contentType: false,
        success: function(data) {
            console.log(data.messages); // data.messages is an array
        },
        error: function() {
           console.warn(arguments);
        }
    });
});
```
## Sample Messages

There are [documents for provoking different message types](http://hsivonen.com/test/moz/messages-types/).

<table>
<tr>
<td>No message
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html&out=gnu">GNU</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fno-message.html&out=text">Text</a>
</tr>

<tr>
<td>Info
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg">HTML</a>
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg&out=xhtml">XHTML</a>
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg&out=xml">XML</a>
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg&out=json">JSON</a>
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg&out=gnu">GNU</a>
<td><a href="https://validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Finfo.svg&out=text">Text</a>
</tr>

<tr>
<td>Warning
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html&out=gnu">GNU</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fwarning.html&out=text">Text</a>
</tr>

<tr>
<td>Error (precise location)
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html&out=gnu">GNU</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Fprecise-error.html&out=text">Text</a>
</tr>

<tr>
<td>Error (range location)
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html&out=gnu">GNU</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Frange-error.html&out=text">Text</a>
</tr>

<tr>
<td>Fatal
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml&out=gnu">GNU</a>
<td><a href-"https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2Ffatal.xhtml&out=text">Text</a>
</tr>

<tr>
<td>IO
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html">HTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html&out=xhtml">XHTML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html&out=xml">XML</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html&out=json">JSON</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html&out=gnu">GNU</a>
<td><a href="https://html5.validator.nu/?doc=http%3A%2F%2Fhsivonen.com%2Ftest%2Fmoz%2Fmessages-types%2F404.html&out=text">Text</a>
</tr>
</table>