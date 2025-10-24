galimatias
==========

[![Build Status](https://travis-ci.org/smola/galimatias.png?branch=master)](https://travis-ci.org/smola/galimatias)
[![Coverage Status](https://coveralls.io/repos/smola/galimatias/badge.png?branch=master)](https://coveralls.io/r/smola/galimatias?branch=master)

galimatias is a URL parsing and normalization library written in Java.

### Design goals

- Parse URLs as browsers do, optionally enforcing compliance with old standards (i.e. RFC 3986,  RFC 2396).
- Stay as close as possible to WHATWG's [URL Standard](http://url.spec.whatwg.org/).
- Convenient fluent API with immutable URL objects.
- Interoperable with java.net.URL and java.net.URI.
- Minimal dependencies.

### Gotchas 

galimatias is not a generic URI parser. It can parse any URI, but only schemes defined in the URL Standard (i.e. http, https, ftp, ws, wss, gopher, file) will be parsed as hierarchical URIs. For example, in `git://github.com/smola/galimatias.git` you'll be able to extract scheme (i.e. `git`) and scheme data (i.e. `//github.com/smola/galimatias.git`), but not host (i.e. `github.com`). **This is intended.** We cannot guarantee that applying a set of generic rules won't break certain kind of URIs, so we do not try with them. **I will consider adding further support for other schemes if enough people provides solid use cases and testing. [You can check this issue](https://github.com/smola/galimatias/issues/8) if you are interested.**

But, why?
---------

galimatias started out of frustration with java.net.URL and java.net.URI. Both of them are good for basic use cases, but severely broken for others:

- **[java.net.URL.equals() is broken.](http://stackoverflow.com/a/3771123/205607)**

- **java.net.URI can parse only RFC 2396 URI syntax.** `java.net.URI` will only parse a URI if it's strictly compliant with RFC 2396. Most URLs found in the wild do not comply with any syntax standard, and RFC 2396 is outdated anyway.

- **java.net.URI is not protocol-aware.** `http://example.com`, `http://example.com/` and `http://example.com:80` are different entities.

- **Manipulation is a pain.** I haven't seen any URL manipulation code using `java.net.URL` or `java.net.URI` that is simple and concise.

- **Not IDN ready.** Java has IDN support with `java.net.IDN`, but this does not apply to `java.net.URL` or `java.net.URI`.

Setup with Maven
----------------

galimatias is available at Maven Central. Just add to your pom.xml `<dependencies>` section:

```xml
<dependency>
  <groupId>io.mola.galimatias</groupId>
  <artifactId>galimatias</artifactId>
  <version>0.2.1</version>
</dependency>
```

Development snapshots are also available at Sonatype OSS Snapshots repository.

Getting started
---------------

### Parse a URL

```java
// Parse
String urlString = //...
URL url;
try {
  url = URL.parse(urlString);
} catch (GalimatiasParseException ex) {
  // Do something with non-recoverable parsing error
}
```

### Convert to java.net.URL

```java
URL url = //...
java.net.URL javaURL;
try {
  javaURL = url.toJavaURL();
} catch (MalformedURLException ex) {
  // This can happen if scheme is not http, https, ftp, file or jar.
}
```

### Convert to java.net.URI

```java
URL url = //...
java.net.URI javaURI;
try {
  javaURI = url.toJavaURI();
} catch (URISyntaxException ex) {
  // This will happen in rare cases such as "foo://"
}
```

### Parse a URL with strict error handling

You can use a strict error handler that will throw an exception
on any invalid URL, even if it's a recovarable error.

```java
URLParsingSettings settings = URLParsingSettings.create()
  .withErrorHandler(StrictErrorHandler.getInstance());
URL url = URL.parse(settings, urlString);
```

Documentation
-------------

Check out the [Javadoc](http://galimatias.mola.io/apidocs/0.2.0/).

Contribute
----------

Did you find a bug? [Report it on GitHub](https://github.com/smola/galimatias/issues).

Did you write a patch? Send a pull request.

Something else? Email me at santi@mola.io.

License
-------

Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>

galimatias is released under the terms of the MIT License.
