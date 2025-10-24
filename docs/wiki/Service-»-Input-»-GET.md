To use [[the checker as a Web service|Service » HTTP interface]]
via GET:

* Issue an HTTP request with a URL for an existing checker instance
  such as `http://validator.w3.org/nu/` or `https://validator.nu/`.
* Use the GET method for the request.
* Include the URL of the document as a query-string parameter named "`doc`".
* Encode [[other parameters|Service » Common params]] as query-string
  parameters.

Only "`&`" is supported as a query parameter separator. "`;`" is not
supported.

Percent-encoded octets in the query string must decode to UTF-8.

### Examples

The following example shows how to use the
[Unirest Library in Java](https://github.com/Mashape/unirest-java)
to send the document at `http://www.example.com` to a local instance
of the checker running at `http://localhost:8080/vnu` and get the
checker results as [[JSON|Output » JSON]]:

```java
String response = null;
Map<String, Object> queryConf = new HashMap<String, Object>();
queryConf.put("doc", "http://www.example.com/");
queryConf.put("out", "json");

HttpResponse<String> uniResponse = Unirest.get("http://localhost:8080/vnu")
    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36")
    .header("Content-Type", "text/html; charset=UTF-8")
    .queryString(queryConf)
    .asString();
response = uniResponse.getBody();
```

See also: [[Service » HTTP interface]]
