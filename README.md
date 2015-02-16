# The Nu HTML Checker (v.Nu) [![Nu HTML Checker chat room][1]][2]

   [1]: https://goo.gl/1kHqwI
   [2]: https://gitter.im/validator/validator

The Nu HTML Checker (v.Nu) is a name for the backend of [html5.validator.nu][3],
[validator.w3.org/nu][4], and the HTML5 facet of the legacy [W3C Validator][5].
Its [source code is available][6], as are [instructions on how to build, test,
and run the code][7]. The checker is released as two separate packages:

   [3]: https://html5.validator.nu
   [4]: https://validator.w3.org/nu/
   [5]: http://validator.w3.org
   [6]: https://github.com/validator/validator
   [7]: https://validator.github.io/validator/#build-instructions

  * `vnu.jar` is a portable standalone version for [batch-checking documents
  from the command line][8] and from other scripts/apps, or for [deploying the
  checker as a self-contained service][9]

  * `vnu.war` is for [deploying the checker service through a servlet container
  such as Tomcat][10]

   [8]: https://validator.github.io/validator/#usage
   [9]: https://validator.github.io/validator/#standalone
   [10]: https://validator.github.io/validator/#servlet

To use the Nu HTML Checker on your own, [get the latest release][11] and see the
**Usage** and **Web-based checking** sections belowーor alternatively, consider
automating your HTML checking with a frontend such as:

   [11]: https://github.com/validator/validator/releases/latest

  * [Grunt plugin for HTML validation][12]

  * [gulp plugin for HTML validation][13]

  * [HTML5 Validator Integration for Travis CI][14] (auto-check documents pushed
  to a github repo)

  * [LMVTFY: Let Me Validate That For You][15] (auto-check HTML of
  JSFiddle/JSBin etc. links in github issue comments)

   [12]: https://github.com/jzaefferer/grunt-html
   [13]: https://github.com/watilde/gulp-html
   [14]: https://github.com/svenkreiss/html5validator
   [15]: https://github.com/cvrebert/lmvtfy/

## Usage

You can use the `vnu.jar` HTML checker as an executable for command-line
checking of documents by invoking it like this:

      java -jar ~/vnu.jar [--errors-only] [--no-stream]
           [--format gnu|xml|json|text] [--help] [--html] [--verbose]
           [--version] FILES

**Note:** In these instructions, replace _"~/vnu.jar"_ with the actual path to
the file on your system.

To check one or more documents from the command line:

      java -jar ~/vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -jar ~/vnu.jar FILE.html...

To check all documents in a particular directory:

      java -jar ~/vnu.jar some-directory-name/

To check all documents in a particular directory, skipping any documents whose
names don’t end with the extensions `.html`, `.htm`, `.xhtml`, or `.xht`:

      java -jar ~/vnu.jar --skip-non-html some-directory-name/

To check a Web document:

      java -jar ~/vnu.jar _URL_

      example: java -jar ~/vnu.jar http://example.com/foo

To check standard input:

      java -jar ~/vnu.jar -

      example: echo '<!doctype html><title>...' | java -jar ~/vnu.jar -

### Options

When used from the command line as described in this section, the `vnu.jar`
executable provides the following options:

#### --errors-only

    Specifies that only error-level messages and non-document-error messages are
    reported (so that warnings and info messages are not reported).

    default: [unset; all message reported, including warnings & info messages]

#### --format _format_

    Specifies the output format for reporting the results.

    default: "gnu"

    possible values: "gnu", "xml", "json", "text" [see information at URL below]

    https://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out

#### --help

    Shows detailed usage information.

#### --skip-non-html

    Skip documents that don’t have *.html, *.htm, *.xhtml, or *.xht extensions.

    default: [unset; all documents found are checked, regardless of extension]

#### --html

    Forces any *.xhtml or *.xht documents to be parsed using the HTML parser.

    default: [unset; XML parser is used for *.xhtml and *.xht documents]

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

The Nu HTML Checkerーalong with being usable as [a standalone command-line
client][16]ーcan be run as an HTTP service, similar to [html5.validator.nu][17]
and [validator.w3.org/nu][18], for browser-based checking of HTML documents over
the Web. To that end, the checker is released as two separate packages:

   [16]: https://validator.github.io/validator/#usage
   [17]: https://html5.validator.nu/
   [18]: http://validator.w3.org/nu/

  * `vnu.jar` for deploying the checker as a simple self-contained service
  * `vnu.war` for deploying the checker to a servlet container such as Tomcat

Both deployments expose a REST API that enables checking of HTML documents from
other clients, not just web browsers. And the `vnu.jar` package also includes a
simple HTTP client that enables you to either send documents to a
locally-running instance of the checker HTTP serviceーfor fast command-line
checkingーor to any remote instance of the checker HTTP service running anywhere
on the Web.

The [latest releases of the vnu.jar and vnu.war packages][19] are available from
the `validator` project at github. The following are detailed instructions on
using them.

   [19]: https://github.com/validator/validator/releases/latest

**Note:** Replace _"~/vnu.jar"_ or _"~/vnu.war"_ below with the actual paths to
those files on your system.

### Standalone web server

To run the checker as a standalone service (using a built-in Jetty server), open
a new terminal window and invoke `vnu.jar` like this:

      java -cp ~/vnu.jar nu.validator.servlet.Main 8888

Then open [http://localhost:8888][20] in a browser. (To have the checker listen
on a different port, replace `8888` with the port number.)

   [20]: http://localhost:8888

You’ll see a form similar to [validator.w3.org/nu][21] that allows you to enter
the URL of an HTML document and have the results for that document displayed in
the browser.

   [21]: http://validator.w3.org/nu/

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar nu.validator.servlet.Main 8888

### Deployment to servlet container

To run the checker inside of an existing servlet container such as Apache Tomcat
you will need to deploy the `vnu.war` file to that server following its
documentation. For example, on Apache Tomcat you could do this using the
[Manager][22] application or simply by copying the file to the `webapps`
directory (since that is the default `appBase` setting). Typically you would see
a message similar to the following in the `catalina.out` log file.

   [22]: http://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

    May 7, 2014 4:42:04 PM org.apache.catalina.startup.HostConfig deployWAR
    INFO: Deploying web application archive /var/lib/tomcat7/webapps/vnu.war

Assuming your servlet container is configured to receive HTTP requests sent to
`localhost` on port `80` and the context root of this application is `vnu`
(often the default behavior is to use the WAR file's filename as the context
root unless one is explicitly specified) you should be able to access the
application by connecting to [http://localhost/vnu/][23].

   [23]: http://localhost/vnu/

**Note:** You may want to customize the `/WEB-INF/web.xml` file inside the WAR
file (you can use any ZIP-handling program) to modify the servlet filter
configuration. For example, if you wanted to disable gzip decompression you
could comment out that filter like this:

    <!--
      <filter>
          <filter-name>gzip-filter</filter-name>
          <filter-class>org.mortbay.servlet.GzipFilter</filter-class>
      </filter> <filter-mapping>
          <filter-name>gzip-filter</filter-name> <url-pattern>*</url-pattern>
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
[html5.validator.nu/][24], use the nu.validator.client.host and
nu.validator.client.port options, like this:

   [24]: http://html5.validator.nu/

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
[wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters][25].

   [25]: https://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters

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

    https://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#parser"

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

    https://wiki.whatwg.org/wiki/Validator.nu_Common_Input_Parameters#out"

#### nu.validator.client.asciiquotes

    Specifies whether ASCII quotation marks are substituted for Unicode smart
    quotation marks in messages.

    default: "yes"

    possible values: "yes" or "no"

## Build instructions

Follow the steps below to build, test, and run the checker such that you can
open `http://localhost:8888/` in a Web browser to use the checker Web UI.

  1. Make sure you have git, python, and JDK 5 or later installed.

  2. Set the `JAVA_HOME` environment variable:

    export JAVA_HOME=@@/PATH/TO/JDK/ON/YOUR/SYSTEM@@

For example:

    * `export JAVA_HOME=/usr/lib/jvm/java-6-openjdk` (older Ubuntu)

    * `export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64` (newer Ubuntu)

    * `export JAVA_HOME=$(/usr/libexec/java_home)` (Mac OS X)

  3. Create a working directory:

    git clone https://github.com/validator/validator.git

  4. Change into your working directory:

    cd validator

  5. Start the build script:

    python ./build/build.py all; python ./build/build.py all

**Important:** Yes, you must run the script twice the first time you buildーto
work around known issues that cause it to fail to complete when run from scratch
in a fresh working directory. For subsequent builds you only have to run it
once. And note that the first time you run it, it will need time to download
~300MB of dependencies.

The steps above will build, test, and run the checker such that you can open
`http://localhost:8888/` in a Web browser to use the checker Web UI.

Use `python ./build/build.py --help` to see command-line options for controlling
the behavior of the script, as well as build-target names you can call
separately; e.g.:

  * `python ./build/build.py build` (to build only)

  * `python ./build/build.py build test` (to build and test)

  * `python ./build/build.py run` (to run only)

