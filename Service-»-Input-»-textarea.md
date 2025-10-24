This is **not the recommended way** to
[[use the checker as a Web service|Service » HTTP interface]]. Instead,
you probably want to check documents by
[[POSTing them as the entity bodies of requests|Service » Input » POST body]].

However, if for some reason you want to emulate a browser form-based textarea
submission, you can do so by following these requirements:  

* Issue an HTTP request to an existing checker instance
  such as `https://validator.nu/` or `http://validator.w3.org/nu/`.
* For the request, use the POST method with an `multipart/form-data` entity body
  (`application/x-www-form-urlencoded` is *not* supported).
* Encode [[common parameters|Service » Common params]] as form fields.
* Include the document to check as the value of a form field named `content`.
* **You must ensure that the `content` field is the last field in the submission.**

The request should explicitly set either the `parser` parameter or the `css`
parameter. `Content-Type` is synthesized by selecting `text/css` if the
value of the `css` parameter is `yes`, and otherwise selecting `text/html` or
`application/xml` depending on the `parser` value. For all cases, the `charset`
MIME type parameter is clamped to `utf-8`.

All field values including the document source must decode as UTF-8.

### Examples

The following are simple examples using the `curl` command.

To check the file `FILE.html` and get the checker results in
[[GNU error format|Output » GNU]]:
```
curl -F out=gnu -F "content=<FILE.html" http://validator.w3.org/nu/
```
To check the file `/Users/foo/FILE.html` and get the checker results as
[[JSON|Output » JSON]]:
```
curl -F out=json -F "content=</Users/foo.FILE.html" \
  http://validator.w3.org/nu/
```
See also: [[Service » HTTP interface]]
