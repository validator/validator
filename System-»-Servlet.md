## The `Main` Class
The checker has its own `main()` method in a class called
`nu.validator.servlet.Main`. This makes  debugging and isolated
deployment an order of magnitude easier than doing XML situps to make
the application server load the right bits.

The `main()` method does the following thing:

1.  Initializes log4j
2.  Instantiates `VerifierServletTransaction` to trigger its static
    initializer early.
3.  Instantiates Jetty.
4.  Sets up an HTTP or AJP13 connector.
5.  Builds a servlet [filter](#the-filters "wikilink") chain.
6.  Adds the servlet to the server.
7.  Starts the server.

If you want to run the servlet in a larger application server, the only
mandatory step you need to take care of before the servlet loads is
initializing log4j. The [filter](#the-filters "wikilink") chain is
optional (but without it some non-core features do not work; see below).

## The Servlet
The checker is encapsulated in one servlet:
`nu.validator.servlet.VerifierServlet`. This servlet handles the generic
facet, the HTML5 facet and the parsetree facet and does URI dispatching
and decides which controller class to instantiate.

Servlets are by default required to be re-entrant, so for programming
convenience the servlet instantiates a controller object whose lifetime is
limited to one HTTP request.

## The Filters
Some non-core features are implemented as servlet filters. These
features are inbound and outbound gzip compression, support for HTML
form-based file uploads and textarea-based input and limiting the input
data size before performing decompression and before performing form
POST decoding.

The filter from outer (closer to container) to inner (closer to the
servlet) are:

#### `org.mortbay.servlet.GzipFilter`

Implements response compression.

#### `nu.validator.servletfilter.InboundSizeLimitFilter`

This filter throws a `nu.validator.io.StreamBoundException` if the
request entity body is too large. This filter throttles the input for
`nu.validator.servletfilter.InboundGzipFilter` and
`nu.validator.servlet.MultipartFormDataFilter`. If those filters are not
in use and the servlet container makes sure that POSTed content is
really limited by `Content-Length` if present, this one isn’t needed,
either.

#### `nu.validator.servletfilter.InboundGzipFilter`

Implements request decompression.

#### `nu.validator.servlet.MultipartFormDataFilter`

Implements support for HTML form-based file upload and textarea input by
exposing these to the servet as if the document were POSTed straight as
the entity body.

## The Controllers

#### `VerifierServletTransaction`

The bulk of the checker UI controller and random glue that holds it
all together is in `nu.validator.servlet.VerifierServletTransaction`.
This is probably the ugliest class in the checker; UI-related code
tends to be uglier than back end code and the class has grown
organically over time.

Most of the initialization of the checker is performed in the static
initializer of this class. The default `Main` triggers early
initialization by instantiating this class once before starting the HTTP
server.

#### `Html5ConformanceCheckerTransaction`
This is a subclass of `VerifierServletTransaction` that tweaks the
overall behavior just enough to implement the HTML5 facet of
the checker.

#### `ParseTreePrinter`
This is the controller for parsetree.validator.nu.
