# The Nu Html Checker (v.Nu) [![Chat room][1]][2] [![Download latest][3]][4]

   [1]: https://img.shields.io/badge/gitter-chat%20%E2%86%92-brightgreen.svg
   [2]: https://gitter.im/validator/validator
   [3]: https://img.shields.io/badge/download-latest%20%E2%86%92-blue.svg
   [4]: https://github.com/validator/validator/releases/latest

The Nu Html Checker (v.Nu) helps you [catch unintended mistakes in your HTML,
CSS, and SVG][5]. It enables you to [batch-check documents from the command
line][6] and from other scripts/apps, and to [deploy your own instance of the
checker as a service][7] (like [validator.w3.org/nu][8]). Its [source code is
available][9], as are [instructions on how to build, test, and run the
code][10].

   [5]: https://validator.w3.org/nu/about.html#why-validate
   [6]: https://validator.github.io/validator/#usage
   [7]: https://validator.github.io/validator/#standalone
   [8]: https://validator.w3.org/nu/
   [9]: https://github.com/validator/validator
   [10]: https://validator.github.io/validator/#build-instructions

A [Dockerfile][11] (see the **Pulling from Docker Hub** section below) and
[npm][12], [pip][13], and [brew][14] packages of it are also available.

   [11]: https://hub.docker.com/r/validator/validator/
   [12]: https://www.npmjs.com/package/vnu-jar
   [13]: https://github.com/svenkreiss/html5validator
   [14]: https://formulae.brew.sh/formula/vnu

It is released upstream in these formats:

  * pre-compiled Linux, Windows, and macOS binaries that include an embedded
  Java runtime

  * `vnu.jar` — a portable version you can use on any system that has Java 8 or
  above installed

  * `vnu.war` — for [deploying the checker service through a servlet container
  such as Tomcat][15]

   [15]: https://validator.github.io/validator/#servlet

**Note:** The _vnu.jar_ and _vnu.war_ files require you to have Java 8 or above
installed. The pre-compiled Linux, Windows, and macOS binaries don’t require you
to have any version of Java already installed at all.

You can [get the latest release][16] or run [`docker run -it --rm -p 8888:8888
validator/validator:latest`][17], [`npm install vnu-jar`][18],
[`brew install vnu`][19], or [`pip install html5validator`][20] and see the
**Usage** and **Web-based checking** sections below. Or automate your document
checking with a frontend such as:

   [16]: https://github.com/validator/validator/releases/latest
   [17]: https://hub.docker.com/r/validator/validator/
   [18]: https://www.npmjs.com/package/vnu-jar
   [19]: https://libraries.io/homebrew/vnu
   [20]: https://github.com/svenkreiss/html5validator

  * [Grunt plugin for HTML validation][21] or [Gulp plugin for HTML
  validation][22] or [Maven plugin for HTML validation][23]

  * [html5validator `pip` package][24] (for HTML checking integration in Travis
  CI, CircleCI, CodeShip, Jekyll, Pelican, etc.)

  * [LMVTFY: Let Me Validate That For You][25] (auto-check HTML of
  JSFiddle/JSBin, etc., links in GitHub issue comments)

   [21]: https://github.com/validator/grunt-html
   [22]: https://github.com/validator/gulp-html
   [23]: https://github.com/validator/maven-plugin
   [24]: https://github.com/svenkreiss/html5validator
   [25]: https://github.com/cvrebert/lmvtfy/

## Usage

Run the checker with one of the following invocations:

• `vnu-runtime-image/bin/vnu OPTIONS FILES` (Linux or macOS)

• `vnu-runtime-image\bin\vnu.bat OPTIONS FILES` (Windows)

• `java -jar ~/vnu.jar OPTIONS FILES` (any system with Java8+ installed)

…where _`FILES`_ are the documents to check, and _`OPTIONS`_ are zero or more of
the following options:

    --errors-only --Werror --exit-zero-always --stdout --asciiquotes
    --user-agent USER_AGENT --no-langdetect --no-stream --filterfile FILENAME
    --filterpattern PATTERN --css --skip-non-css --also-check-css --svg
    --skip-non-svg --also-check-svg --html --skip-non-html
    --format gnu|xml|json|text --help --verbose --version

The [Options][26] section below provides details on each option, and the rest of
this section provides some specific examples.

   [26]: https://validator.github.io/validator/#options

**Note:** Throughout these examples, replace `~/vnu.jar` with the actual path to
that jar file on your system, and replace `vnu-runtime-image/bin/vnu` and
`vnu-runtime-image\bin\vnu.bat` with the actual path to the `vnu` or `vnu.bat`
program on your system — or if you add the `vnu-runtime-image/bin` or
`vnu-runtime-image\bin` directory your system `PATH` environment variable, you
can invoke the checker with just `vnu`.

To check one or more documents from the command line:

      vnu-runtime-image/bin/vnu      FILE.html FILE2.html FILE3.html...

      vnu-runtime-image\bin\vnu.bat  FILE.html FILE2.html FILE3.html...

      java -jar ~/vnu.jar            FILE.html FILE2.html FILE3.html...

**Note:** If you get a `StackOverflowError` error when invoking the checker, try
adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -jar ~/vnu.jar ...

      vnu-runtime-image/bin/java -Xss512k \
          -m vnu/nu.validator.client.SimpleCommandLineValidator ...

To check all documents in a particular directory `DIRECTORY_PATH` as HTML:

      java -jar ~/vnu.jar            DIRECTORY_PATH

      vnu-runtime-image/bin/vnu      DIRECTORY_PATH

      vnu-runtime-image\bin\vnu.bat  DIRECTORY_PATH

#### More examples

**Note:** The examples in this section assume you have the
`vnu-runtime-image/bin` or `vnu-runtime-image\bin` directory in your system
`PATH` environment variable. If you’re using the jar file instead, replace `vnu`
in the examples with `java -jar ~/vnu.jar`.

To check all documents in a particular directory `DIRECTORY_PATH` as HTML, but
skip any documents whose names don’t end with the extensions `.html`, `.htm`,
`.xhtml`, or `.xht`:

      vnu --skip-non-html DIRECTORY_PATH

To check all documents in a particular directory as CSS:

      vnu --css DIRECTORY_PATH

To check all documents in a particular directory as CSS, but skip any documents
whose names don’t end with the extension `.css`:

      vnu --skip-non-css DIRECTORY_PATH

To check all documents in a particular directory, with documents whose names end
in the extension `.css` being checked as CSS, and all other documents being
checked as HTML:

      vnu --also-check-css DIRECTORY_PATH

To check all documents in a particular directory as SVG:

      vnu --svg DIRECTORY_PATH

To check all documents in a particular directory as SVG, but skip any documents
whose names don’t end with the extension `.svg`:

      vnu --skip-non-svg DIRECTORY_PATH

To check all documents in a particular directory, with documents whose names end
in the extension `.svg` being checked as SVG, and all other documents being
checked as HTML:

      vnu --also-check-svg DIRECTORY_PATH

To check a Web document:

      vnu _URL_

      example: vnu http://example.com/foo

To check standard input:

      vnu -

      example:

      echo '<!doctype html><title>...' | vnu -

      echo '<!doctype html><title>...' | java -jar ~/vnu.jar -

### Options

When used from the command line as described in this section, the checker
provides the following options:

#### --asciiquotes

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.

    default: [unset; Unicode smart quotation marks are used in messages]

#### --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported (so that warnings and info messages are not reported).

    default: [unset; all messages reported, including warnings & info messages]

#### --Werror

    Makes the checker exit non-zero if any warnings are encountered (even if
    there are no errors).

    default: [unset; checker exits zero if only warnings are encountered]

#### --exit-zero-always

    Makes the checker exit zero even if errors are reported for any documents.

    default: [unset; checker exits 1 if errors are reported for any documents]

#### --stdout

    Makes the checker report errors and warnings to stdout rather than stderr.

    default: [unset; checker reports errors and warnings to stderr]

#### --filterfile _FILENAME_

    Specifies a filename. Each line of the file contains either a regular
    expression or starts with "#" to indicate the line is a comment. Any error
    message or warning message that matches a regular expression in the file is
    filtered out (dropped/suppressed).

    default: [unset; checker does no message filtering]

#### --filterpattern _REGEXP_

    Specifies a regular expression. Any error message or warning message that
    matches the regular expression is filtered out (dropped/suppressed).

    As with all other checker options, this option may only be specified once.
    So to filter multiple error messages or warning messages, you must provide a
    single regular expression that will match all the messages. The typical way
    to do that for regular expressions is to OR multiple patterns together using
    the "|" character.

    default: [unset; checker does no message filtering]

#### --format _format_

    Specifies the output format for reporting the results.

    default: "gnu"

    possible values: "gnu", "xml", "json", "text" [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#out

#### --help

    Shows detailed usage information.

#### --skip-non-css

    Check documents as CSS but skip documents that don’t have *.css extensions.

    default: [unset; all documents found are checked]

#### --css

    Force all documents to be checked as CSS, regardless of extension.

    default: [unset]

#### --skip-non-svg

    Check documents as SVG but skip documents that don’t have *.svg extensions.

    default: [unset; all documents found are checked]

#### --svg

    Force all documents to be checked as SVG, regardless of extension.

    default: [unset]

#### --skip-non-html

    Skip documents that don’t have *.html, *.htm, *.xhtml, or *.xht extensions.

    default: [unset; all documents found are checked, regardless of extension]

#### --html

    Forces any *.xhtml or *.xht documents to be parsed using the HTML parser.

    default: [unset; XML parser is used for *.xhtml and *.xht documents]

#### --also-check-css

    Check CSS documents (in addition to checking HTML documents).

    default: [unset; no documents are checked as CSS]

#### --also-check-svg

    Check SVG documents (in addition to checking HTML documents).

    default: [unset; no documents are checked as SVG]

#### --user-agent _USER_AGENT_

    Specifies the value of the User-Agent request header to send when checking
    HTTPS/HTTP URLs.

    default: "Validator.nu/LV"

#### --no-langdetect

    Disables language detection, so that documents are not checked for missing
    or mislabeled html[lang] attributes.

    default: [unset; language detection & html[lang] checking are performed]

#### --no-stream

    Forces all documents to be be parsed in buffered mode instead of streaming
    mode (causes some parse errors to be treated as non-fatal document errors
    instead of as fatal document errors).

    default: [unset; non-streamable parse errors cause fatal document errors]

#### --verbose

    Specifies "verbose" output. (Currently this just means that the names of
    files being checked are written to stdout.)

    default: [unset; output is not verbose]

#### --version

    Shows the checker version number.

## Web-based checking

The Nu Html Checker — along with being usable as [a standalone command-line
client][27] — can be run as an HTTP service, similar to
[validator.w3.org/nu][28], for browser-based checking of HTML documents, CSS
stylesheets, and SVG images over the Web. To that end, the checker is released
as several separate packages:

   [27]: https://validator.github.io/validator/#usage
   [28]: https://validator.w3.org/nu/

  * Linux, Windows, and macOS binaries for deploying the checker as a simple
  self-contained service on any system

  * `vnu.jar` for deploying the checker as a simple self-contained service on a
  system with Java installed

  * `vnu.war` for deploying the checker to a servlet container such as Tomcat

All deployments expose a REST API that enables checking of HTML documents, CSS
stylesheets, and SVG images from other clients, not just web browsers. And the
Linux, Windows, and macOS binaries and `vnu.jar` package also include a simple
HTTP client that enables you to either send documents to a locally-running
instance of the checker HTTP service — for fast command-line checking — or to
any remote instance of the checker HTTP service running anywhere on the Web.

The [latest releases of the Linux, Windows, and macOS binaries and vnu.jar and
vnu.war packages][29] are available from the `validator` project at github. The
following are detailed instructions on using them.

   [29]: https://github.com/validator/validator/releases/latest

**Note:** Throughout these instructions, replace `~/vnu.jar` with the actual
path to that jar file on your system, and replace `vnu-runtime-image/bin/java`
and `vnu-runtime-image\bin\java.exe` with the actual path to the checker `java`
or `java.exe` program on your system — or if you add the `vnu-runtime-image/bin`
or `vnu-runtime-image\bin` directory your system `PATH` environment variable,
you can invoke the checker with just `java nu.validator.servlet.Main 8888`.

### Standalone web server

To run the checker as a standalone service (using a built-in Jetty server), open
a new terminal window and invoke the checker like this:

    java -cp ~/vnu.jar              nu.validator.servlet.Main 8888

    vnu-runtime-image/bin/java      nu.validator.servlet.Main 8888

    vnu-runtime-image\bin\java.exe  nu.validator.servlet.Main 8888

Then open [http://0.0.0.0:8888][30] in a browser. (To listen on a different
port, replace `8888` with the port number.)

   [30]: http://0.0.0.0:8888

**Warning:** Future checker releases will bind by default to the address
`127.0.0.1`. Your checker deployment might become unreachable unless you use the
`nu.validator.servlet.bind-address` system property to bind the checker to a
different address:

    java -cp ~/vnu.jar \
        -Dnu.validator.servlet.bind-address=128.30.52.73 \
        nu.validator.servlet.Main 8888

    vnu-runtime-image/bin/java \
        -Dnu.validator.servlet.bind-address=128.30.52.73 \
        nu.validator.servlet.Main 8888

    vnu-runtime-image\bin\java.exe  \
        -Dnu.validator.servlet.bind-address=128.30.52.73 \
        nu.validator.servlet.Main 8888

When you open [http://0.0.0.0:8888][31] (or whatever URL corresponds to the
`nu.validator.servlet.bind-address` value you’re using), you’ll see a form
similar to [validator.w3.org/nu][32] that allows you to enter the URL of an HTML
document, CSS stylesheet, or SVG image, and have the results of checking that
resource displayed in the browser.

   [31]: http://0.0.0.0:8888
   [32]: https://validator.w3.org/nu/

**Note:** If you get a `StackOverflowError` error when using the checker, try
adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar nu.validator.servlet.Main 8888

      vnu-runtime-image/bin/java -Xss512k -m vnu/nu.validator.servlet.Main 8888

### Deployment to servlet container

To run the checker inside of an existing servlet container such as Apache Tomcat
you will need to deploy the `vnu.war` file to that server following its
documentation. For example, on Apache Tomcat you could do this using the
[Manager][33] application or simply by copying the file to the `webapps`
directory (since that is the default `appBase` setting). Typically you would see
a message similar to the following in the `catalina.out` log file.

   [33]: https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

    May 7, 2014 4:42:04 PM org.apache.catalina.startup.HostConfig deployWAR
    INFO: Deploying web application archive /var/lib/tomcat7/webapps/vnu.war

Assuming your servlet container is configured to receive HTTP requests sent to
`localhost` on port `80` and the context root of this application is `vnu`
(often the default behavior is to use the WAR file's filename as the context
root unless one is explicitly specified) you should be able to access the
application by connecting to [http://localhost/vnu/][34].

   [34]: http://localhost/vnu/

**Note:** You may want to customize the `/WEB-INF/web.xml` file inside the WAR
file (you can use any ZIP-handling program) to modify the servlet filter
configuration. For example, if you wanted to disable the inbound-size-limit
filter, you could comment out that filter like this:

    <!--
      <filter>
          <filter-name>inbound-size-limit-filter</filter-name>
          <filter-class>nu.validator.servlet.InboundSizeLimitFilter</filter-class>
      </filter>
      <filter-mapping>
          <filter-name>inbound-size-limit-filter</filter-name>
          <url-pattern>/*</url-pattern>
      </filter-mapping>
    -->

### HTTP client (for fast command-line checking)

The checker is packaged with an HTTP client you can use from the command line to
either send documents to a locally-running instance of the checker HTTP service
— for fast command-line checking — or to a remote instance anywhere on the Web.

To check documents locally using the packaged HTTP client, do this:

  1. Start up the checker as a local HTTP service, as described in the
  **Standalone web server** section.

  2. Open a new terminal window and invoke the HTTP client like this:

    java -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html...

    vnu-runtime-image/bin/java nu.validator.client.HttpClient FILE.html...

To send documents to an instance of the checker on the Web, such as
[html5.validator.nu/][35], use the nu.validator.client.host and
nu.validator.client.port options, like this:

   [35]: https://html5.validator.nu/

    java -cp ~/vnu.jar -Dnu.validator.client.port=80 \
        -Dnu.validator.client.host=html5.validator.nu \
        nu.validator.client.HttpClient FILE.html...

…or like this:

    vnu-runtime-image/bin/java -Dnu.validator.client.port=80 \
        -Dnu.validator.client.host=html5.validator.nu \
        nu.validator.client.HttpClient FILE.html...

Other options are documented below.

### HTTP client options

When using the packaged HTTP client for sending documents to an instance of the
checker HTTP service for checking, you can set Java system properties to control
configuration options for the checker behavior.

For example, you can suppress warning-level messages and only show error-level
ones by setting the value of the `nu.validator.client.level` system property to
`error`, like this:

    java -Dnu.validator.client.level=error \
           -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html...

…or like this:

    vnu-runtime-image/bin/java -Dnu.validator.client.level=error \
           -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html...

Most of the properties listed below map to the common input parameters for the
checker service, as documented at
[github.com/validator/validator/wiki/Service-»-Common-params][36].

   [36]: https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params

#### nu.validator.client.host

    Specifies the hostname of the checker for the client to connect to.

    default: "127.0.0.1"

#### nu.validator.client.port

    Specifies the hostname of the checker for the client to connect to.

    default: "8888"

    example: java -Dnu.validator.client.port=8080 -jar ~/vnu.jar FILE.html

#### nu.validator.client.level

    Specifies the severity level of messages to report; to suppress
    warning-level messages, and only show error-level ones, set this property to
    "error".

    default: [unset]

    possible values: "error"

    example: java -Dnu.validator.client.level=error -jar ~/vnu.jar FILE.html

#### nu.validator.client.parser

    Specifies which parser to use.

    default: "html"; or, for *.xhtml input files, "xml"

    possible values: [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#parser

#### nu.validator.client.charset

    Specifies the encoding of the input document.

    default: [unset]

#### nu.validator.client.content-type

    Specifies the content-type of the input document.

    default: "text/html"; or, for *.xhtml files, "application/xhtml+xml"

#### nu.validator.client.out

    Specifies the output format for messages.

    default: "gnu"

    possible values: [see information at URL below]

    https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params#out

#### nu.validator.client.asciiquotes

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.

    default: "yes"

    possible values: "yes" or "no"

### HTTP servlet options

#### nu.validator.servlet.bind-address

    Binds the validator service to the specified IP address.

    default: 0.0.0.0 [causes the checker to listen on all interfaces]

    possible values: The IP address of any network interface

    example: -Dnu.validator.servlet.bind-address=127.0.0.1

#### nu.validator.servlet.connection-timeout

    Specifies the connection timeout.

    default: 5000

    possible values: number of milliseconds

    example: -Dnu.validator.servlet.connection-timeout=5000

#### nu.validator.servlet.socket-timeout

    Specifies the socket timeout.

    default: 5000

    possible values: number of milliseconds

    example: -Dnu.validator.servlet.socket-timeout=5000

## Pulling from Docker Hub

You can pull the checker from the
[https://hub.docker.com/r/validator/validator/][37] repo at Docker Hub.

   [37]: https://hub.docker.com/r/validator/validator/

To pull and run the latest version of the checker:

      docker run -it --rm -p 8888:8888 validator/validator:latest

To pull and run a specific Docker-Hub tag/version of the checker — for example,
the `17.11.1` version:

      docker run -it --rm -p 8888:8888 validator/validator:17.11.1

To bind the checker to a specific address (rather than have it listening on all
interfaces):

      docker run -it --rm -p 128.30.52.73:8888:8888 validator/validator:latest

To make the checker run with a connection timeout and socket timeout different
than the default 5 seconds, use the `CONNECTION_TIMEOUT_SECONDS` and
`SOCKET_TIMEOUT_SECONDS` environment variables:

      docker run -it --rm \
         -e CONNECTION_TIMEOUT_SECONDS=15 \
         -e SOCKET_TIMEOUT_SECONDS=15 \
         -p 8888:8888 \
         validator/validator

To make the checker run with particular Java system properties set, use the
`JAVA_TOOL_OPTIONS` environment variable:

      docker run -it --rm \
         -e JAVA_TOOL_OPTIONS=-Dnu.validator.client.asciiquotes=yes  \
         -p 8888:8888 \
         validator/validator

To define a service named `vnu` for use with `docker compose`, create a Compose
file named `docker-compose.yml` (for example), with contents such as the
following:

      version: '2' services:
        vnu:
          image: validator/validator ports:
            - "8888:8888"
          network_mode: "host" #so "localhost" refers to the host machine.

## Build instructions

Follow the steps below to build, test, and run the checker such that you can
open `http://0.0.0.0:8888/` in a Web browser to use the checker Web UI.

  1. Make sure you have git, python, and JDK 8 or above installed.

  2. Set the `JAVA_HOME` environment variable:

        export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64    <-- Ubuntu, etc.

        export JAVA_HOME=$(/usr/libexec/java_home)            <-- MacOS

  3. Create a working directory:

        git clone https://github.com/validator/validator.git

  4. Change into your working directory:

        cd validator

  5. Start the checker Python script:

        python ./checker.py all

The first time you run the checker Python script, you’ll need to be online and
the build will need time to download several megabytes of dependencies.

The steps above will build, test, and run the checker such that you can open
`http://0.0.0.0:8888/` in a Web browser to use the checker Web UI.

**Warning:** Future checker releases will bind by default to the address
`127.0.0.1`. Your checker deployment might become unreachable unless you use the
`--bind-address` option to bind the checker to a different address:

        python ./checker.py --bind-address=128.30.52.73 all

Use `python ./checker.py --help` to see command-line options for controlling the
behavior of the script, as well as build-target names you can call separately;
e.g.:

  * python ./checker.py build  # to build only

  * python ./checker.py build  # test to build and test

  * python ./checker.py run    # to run only

  * python ./checker.py jar    # to compile vnu.jar

  * python ./checker.py update-shallow && \
      python ./checker.py dldeps && \
      python ./checker.py jar  # compile vnu.jar faster
