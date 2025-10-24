This method—POSTing a document as the HTTP entity body of a request—is the
**recommended way** to [[use the checker as a Web service|Service » HTTP interface]].

To use this method to check a document:
* Issue an HTTP request with a URL for an existing checker
  instance such as  `https://validator.w3.org/nu/` or `https://validator.nu/`.
* Use the POST method for the request.
* Include the document to check as as the entity body of the request.
* Include the `Content-Type` request header to communicate the MIME
  type of the entity body; e.g., `Content-type: text/html; charset=utf-8`.
* Encode [[common parameters|Service » Common params]] as query-string
  parameters; that is, just as you would with a [[GET request|Service » Input » GET]].

Only "`&`" is supported as a query-parameter separator. "`;`" is not
supported.

Percent-encoded octets in the query string must decode to UTF-8.

### Examples

First, two simple examples using the `curl` command.

To check the file `FILE.html` and get the checker results in
[[GNU error format|Output » GNU]]:
```
curl -H "Content-Type: text/html; charset=utf-8" \
    --data-binary @FILE.html \
    https://validator.w3.org/nu/?out=gnu
```
To check the file `/Users/foo/FILE.html` and get the checker results as
[[JSON|Output » JSON]]:
```
curl -H "Content-Type: text/html; charset=utf-8" \
    --data-binary @/Users/foo/FILE.html \
    https://validator.w3.org/nu/?out=json
```

The following example shows how to use the
[Unirest Library in Java](https://github.com/Mashape/unirest-java)
to send a string as a document to a local instance
of the checker running at `http://localhost:8080/vnu` and get the
checker results in [[GNU error format|Output » GNU]]

```java
String response = null;
String source = "your html here";
HttpResponse<String> uniResponse = Unirest.post("http://localhost:8080/vnu")
    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36")
    .header("Content-Type", "text/html; charset=UTF-8")
    .queryString("out", "gnu")
    .body(source)
    .asString();
response = uniResponse.getBody();
```

See also: [[Service » HTTP interface]]