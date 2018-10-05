# The Nu Html Checker (v.Nu) [![Chat room][1]][2] [![Download latest][3]][4]

   [1]: https://goo.gl/1kHqwI
   [2]: https://gitter.im/validator/validator
   [3]: https://goo.gl/3PC2Qn
   [4]: https://github.com/validator/validator/releases/latest

The Nu Html Checker (v.Nu) is the backend of [checker.html5.org][5],
[html5.validator.nu][6], and [validator.w3.org/nu][7]. Its [source code is
available][8], as are [instructions on how to build, test, and run the code][9].
A [Dockerfile][10] (see the **Pulling from Docker Hub** section below) and
[npm][11], [pip][12], and [brew][13] packages of it are also available, and it’s
released upstream in these formats:

   [5]: https://checker.html5.org/
   [6]: https://html5.validator.nu
   [7]: https://validator.w3.org/nu/
   [8]: https://github.com/validator/validator
   [9]: https://validator.github.io/validator/#build-instructions
   [10]: https://hub.docker.com/r/validator/validator/
   [11]: https://www.npmjs.com/package/vnu-jar
   [12]: https://github.com/svenkreiss/html5validator
   [13]: https://libraries.io/homebrew/vnu

  * `vnu.jar` is a portable standalone version for [batch-checking documents
  from the command line][14] and from other scripts/apps, or for [deploying the
  checker as a self-contained service][15]

  * `vnu.war` is for [deploying the checker service through a servlet container
  such as Tomcat][16]

   [14]: https://validator.github.io/validator/#usage
   [15]: https://validator.github.io/validator/#standalone
   [16]: https://validator.github.io/validator/#servlet

**Note:** The _vnu.jar_ and _vnu.war_ files require an environment with Java 8
or above; they won’t run in Java 7 or older environment.

You can [get the latest release][17] or run [`docker run -it --rm -p 8888:8888
validator/validator:latest`][18], [`npm install vnu-jar`][19],
[`brew install vnu`][20], or [`pip install html5validator`][21] and see the
**Usage** and **Web-based checking** sections below. Or automate your document
checking with a frontend such as:

   [17]: https://github.com/validator/validator/releases/latest
   [18]: https://hub.docker.com/r/validator/validator/
   [19]: https://www.npmjs.com/package/vnu-jar
   [20]: https://libraries.io/homebrew/vnu

   [21]: https://github.com/svenkreiss/html5validator

  * [Grunt plugin for HTML validation][22] or [Gulp plugin for HTML
  validation][23]

  * [html5validator `pip` package][24] (for HTML checking integration in Travis
  CI, CircleCI, CodeShip, [Jekyll][25], [Pelican][26] etc.)

  * [LMVTFY: Let Me Validate That For You][27] (auto-check HTML of
  JSFiddle/JSBin etc. links in github issue comments)

   [22]: https://github.com/validator/grunt-html
   [23]: https://github.com/watilde/gulp-html
   [24]: https://github.com/svenkreiss/html5validator
   [25]: https://jekyllrb.com/

   [26]: https://blog.getpelican.com/
   [27]: https://github.com/cvrebert/lmvtfy/

## Usage

Use the `vnu.jar` checker as an executable for command-line checking of
documents by invoking it like this:

      java -jar ~/vnu.jar [--errors-only] [--Werror] [--exit-zero-always]
           [--asciiquotes] [--user-agent USER_AGENT] [--no-langdetect]
           [--no-stream] [--filterfile FILENAME] [--filterpattern PATTERN]
           [--css] [--skip-non-css] [--also-check-css] [--svg] [--skip-non-svg]
           [--also-check-svg] [--html] [--skip-non-html] [--format
           gnu|xml|json|text] [--help] [--verbose] [--version] FILES

**Note:** In these instructions, replace _"~/vnu.jar"_ with the actual path to
the file on your system.

To check one or more documents from the command line:

      java -jar ~/vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -jar ~/vnu.jar FILE.html...

To check all documents in a particular directory as HTML:

      java -jar ~/vnu.jar some-directory-name/

To check all documents in a particular directory as HTML, but skip any documents
whose names don’t end with the extensions `.html`, `.htm`, `.xhtml`, or `.xht`:

      java -jar ~/vnu.jar --skip-non-html some-directory-name/

To check all documents in a particular directory as CSS:

      java -jar ~/vnu.jar --force-css some-directory-name/

To check all documents in a particular directory as CSS, but skip any documents
whose names don’t end with the extension `.css`:

      java -jar ~/vnu.jar --skip-non-css some-directory-name/

To check all documents in a particular directory, with documents whose names end
in the extension `.css` being checked as CSS, and all other documents being
checked as HTML:

      java -jar ~/vnu.jar --also-check-css some-directory-name/

To check all documents in a particular directory as SVG:

      java -jar ~/vnu.jar --force-svg some-directory-name/

To check all documents in a particular directory as SVG, but skip any documents
whose names don’t end with the extension `.svg`:

      java -jar ~/vnu.jar --skip-non-svg some-directory-name/

To check all documents in a particular directory, with documents whose names end
in the extension `.svg` being checked as SVG, and all other documents being
checked as HTML:

      java -jar ~/vnu.jar --also-check-svg some-directory-name/

To check a Web document:

      java -jar ~/vnu.jar _URL_

      example: java -jar ~/vnu.jar http://example.com/foo

To check standard input:

      java -jar ~/vnu.jar -

      example: echo '<!doctype html><title>...' | java -jar ~/vnu.jar -

### Options

When used from the command line as described in this section, the `vnu.jar`
executable provides the following options:

#### --asciiquotes

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.

    default: [unset; Unicode smart quotation marks are used in messages]

#### --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported (so that warnings and info messages are not reported).

    default: [unset; all message reported, including warnings & info messages]

#### --Werror

    Makes the checker exit non-zero if any warnings are encountered (even if
    there are no errors).

    default: [unset; checker exits zero if only warnings are encountered]

#### --exit-zero-always

    Makes the checker exit zero even if errors are reported for any documents.

    default: [unset; checker exits 1 if errors are reported for any documents]

#### --filterfile _FILENAME_

    Specifies a filename. Each line of the file contains either a regular
    expression or starts with "#" to indicate the line is a comment. Any error
    message or warning message that matches a regular expression in the file is
    filtered out (dropped/suppressed).

    default: [unset; checker does no message filtering]

#### --filterpattern _REGEXP_

    Specifies a regular expression. Any error message or warning message that
    matches the regular expression is filtered out (dropped/suppressed).

    As with all other vnu.jar options, this option may only be specified once.
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

    Shows the vnu.jar version number.

## Web-based checking with vnu.war or vnu.jar

The Nu Html Checkerーalong with being usable as [a standalone command-line
client][28]ーcan be run as an HTTP service, similar to [checker.html5.org][29],
[html5.validator.nu][30], and [validator.w3.org/nu][31], for browser-based
checking of HTML documents over the Web. To that end, the checker is released as
two separate packages:

   [28]: https://validator.github.io/validator/#usage
   [29]: https://checker.html5.org/
   [30]: https://html5.validator.nu/
   [31]: https://validator.w3.org/nu/

  * `vnu.jar` for deploying the checker as a simple self-contained service
  * `vnu.war` for deploying the checker to a servlet container such as Tomcat

Both deployments expose a REST API that enables checking of HTML documents from
other clients, not just web browsers. And the `vnu.jar` package also includes a
simple HTTP client that enables you to either send documents to a
locally-running instance of the checker HTTP serviceーfor fast command-line
checkingーor to any remote instance of the checker HTTP service running anywhere
on the Web.

The [latest releases of the vnu.jar and vnu.war packages][32] are available from
the `validator` project at github. The following are detailed instructions on
using them.

   [32]: https://github.com/validator/validator/releases/latest

**Note:** Replace _"~/vnu.jar"_ or _"~/vnu.war"_ below with the actual paths to
those files on your system.

### Standalone web server

To run the checker as a standalone service (using a built-in Jetty server), open
a new terminal window and invoke `vnu.jar` like this:

        java -cp ~/vnu.jar nu.validator.servlet.Main 8888

Then open [http://localhost:8888][33] in a browser. (To have the checker listen
on a different port, replace `8888` with the port number.)

   [33]: http://localhost:8888

You’ll see a form similar to [validator.w3.org/nu][34] that allows you to enter
the URL of an HTML document and have the results for that document displayed in
the browser.

   [34]: https://validator.w3.org/nu/

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar nu.validator.servlet.Main 8888

### Deployment to servlet container

To run the checker inside of an existing servlet container such as Apache Tomcat
you will need to deploy the `vnu.war` file to that server following its
documentation. For example, on Apache Tomcat you could do this using the
[Manager][35] application or simply by copying the file to the `webapps`
directory (since that is the default `appBase` setting). Typically you would see
a message similar to the following in the `catalina.out` log file.

   [35]: https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

    May 7, 2014 4:42:04 PM org.apache.catalina.startup.HostConfig deployWAR
    INFO: Deploying web application archive /var/lib/tomcat7/webapps/vnu.war

Assuming your servlet container is configured to receive HTTP requests sent to
`localhost` on port `80` and the context root of this application is `vnu`
(often the default behavior is to use the WAR file's filename as the context
root unless one is explicitly specified) you should be able to access the
application by connecting to [http://localhost/vnu/][36].

   [36]: http://localhost/vnu/

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

You can also use `vnu.jar` from the command line to either send documents to a
locally-running instance of the checker HTTP serviceーfor fast command-line
checkingーor to a remote instance anywhere on the Web.

To check documents locally, do this:

  1. Start up the checker as a local HTTP service, as described in the
  Standalone web server section.

  2. Open a new terminal window and invoke `vnu.jar` like this:

        java -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html...

To send documents to an instance of the checker on the Web, such as
[html5.validator.nu/][37], use the nu.validator.client.host and
nu.validator.client.port options, like this:

   [37]: https://html5.validator.nu/

        java -cp ~/vnu.jar -Dnu.validator.client.port=80 \
         -Dnu.validator.client.host=html5.validator.nu \
         nu.validator.client.HttpClient FILE.html...

Other options are documented below.

### HTTP client options

When using `vnu.jar` for sending documents to an instance of the checker HTTP
service for checking, you can set Java system properties to control
configuration options for the checker behavior.

For example, you can suppress warning-level messages and only show error-level
ones by setting the value of the `nu.validator.client.level` system property to
`error`, like this:

       java -Dnu.validator.client.level=error\
           -cp ~/vnu.jar nu.validator.client.HttpClient FILE.html...

Most of the properties listed below map to the validator.nu common input
parameters documented at
[github.com/validator/validator/wiki/Service:-Common-parameters][38].

   [38]: https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params

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
[https://hub.docker.com/r/validator/validator/][39] repo at Docker Hub.

   [39]: https://hub.docker.com/r/validator/validator/

To pull and run the latest version of the checker:

      docker run -it --rm -p 8888:8888 validator/validator:latest

To pull and run a specific Docker-Hub tag/version of the checker — for example,
the `17.11.1` version:

      docker run -it --rm -p 8888:8888 validator/validator:17.11.1

To run the checker with a connection timeout and socket timeout different than
the default 5 seconds, use the `CONNECTION_TIMEOUT_SECONDS` and
`SOCKET_TIMEOUT_SECONDS` environment variables:

      docker run -it --rm \
         -e CONNECTION_TIMEOUT_SECONDS=15 \
         -e SOCKET_TIMEOUT_SECONDS=15 \
         -p 8888:8888 \
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
open `http://localhost:8888/` in a Web browser to use the checker Web UI.

  1. Make sure you have git, python, and JDK 8 or above installed.

  2. Set the `JAVA_HOME` environment variable:

        export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64    <-- Ubuntu, etc.

        export JAVA_HOME=$(/usr/libexec/java_home)            <-- MacOS

  3. Create a working directory:

        git clone https://github.com/validator/validator.git

  4. Change into your working directory:

        cd validator

  5. Start the build script:

        python ./build/build.py all

The first time you run the build script, you’ll need to be online and the build
will need time to download several megabytes of dependencies.

The steps above will build, test, and run the checker such that you can open
`http://localhost:8888/` in a Web browser to use the checker Web UI.

Use `python ./build/build.py --help` to see command-line options for controlling
the behavior of the script, as well as build-target names you can call
separately; e.g.:

  * `python ./build/build.py build` (to build only)

  * `python ./build/build.py build test` (to build and test)

  * `python ./build/build.py run` (to run only)

  * `python ./build/build.py jar` (to compile `vnu.jar`)

