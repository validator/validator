This is **not the recommended way** to
[[use the checker as a Web service|Service » HTTP interface]] unless you want
a simple filename for the document to be included in each message. Otherwise,
you probably want to check documents by
[[POSTing them as the entity bodies of requests|Service » Input » POST body]].

If do you want to include a simple filename in each error message or want for
some other reason to emulate a browser form-based file upload, you can do so
by following these requirements:  

* Issue an HTTP request to an instance of the checker Web service
  such as `https://validator.nu/` or `http://validator.w3.org/nu/`.
* For the request, use the POST method with a `multipart/form-data`
  entity body (`application/x-www-form-urlencoded` is *not* supported).
* Encode [[common parameters|Service » Common params]] as form fields.
* Include the document to check as the value of a file-upload
  field named `file`.
* **You must ensure that the `file` field is the last field in the submission.**

Since the `Content-Type` supplied by browsers for uploaded files is
unreliable, the client-supplied `Content-Type` is overridden with a
synthetic `Content-Type` if the filename is supplied *and* it has a
well-known extension. The well-known extensions are `html`, `htm`,
`xhtml`, `xht`, `atom`, `rng`, `xsl`, `xml` and `dbk`.

The parameter field values must decode as UTF-8.

Note also that for form-based file-upload requests to the checker
**the document’s character encoding must be specified**,
either using the `charset` parameter or using a `meta` element in
the document itself: `<meta charset=utf-8>`.

### Examples

The following are simple examples using the `curl` command.

To check the UTF8-encoded file `FILE.html` and get the checker results in
[[GNU error format|Output » GNU]]:
```
curl -F out=gnu -F charset=utf-8 -F file=@FILE.html \
  http://validator.w3.org/nu/
```
To check the UTF8-encoded file `/Users/foo/FILE.html` and get the
checker results as [[JSON|Output » JSON]]:
```
curl -F out=json -F charset=utf-8 -F file=@/Users/foo/FILE.html \
  http://validator.w3.org/nu/
```
See also: [[Service » HTTP interface]]