# The Nu Html Checker (v.Nu) [![Chat room][1]][2] [![Download latest][3]][4]

   [1]: https://goo.gl/1kHqwI
   [2]: https://gitter.im/validator/validator
   [3]: https://goo.gl/3PC2Qn
   [4]: https://github.com/validator/validator/releases/latest

The Nu Html Checker (v.Nu) is a name for the backend of [checker.html5.org][5],
[html5.validator.nu][6], and [validator.w3.org/nu][7]. Its [source code is
available][8], as are [instructions on how to build, test, and run the code][9].
It is released as two packages:

   [5]: https://checker.html5.org/
   [6]: https://html5.validator.nu
   [7]: http://validator.w3.org/nu/
   [8]: https://github.com/validator/validator
   [9]: https://validator.github.io/validator/#build-instructions

  * `vnu.jar` is a portable standalone version for [batch-checking documents
  from the command line][10] and from other scripts/apps, or for [deploying the
  checker as a self-contained service][11]

  * `vnu.war` is for [deploying the checker service through a servlet container
  such as Tomcat][12]

   [10]: https://validator.github.io/validator/#usage
   [11]: https://validator.github.io/validator/#standalone
   [12]: https://validator.github.io/validator/#servlet

**Note:** The _vnu.jar_ and _vnu.war_ packages require a Java 8 environment;
they won’t run in Java 7 or older environment.

To use the Nu Html Checker on your own, [get the latest release][13] and see the
**Usage** and **Web-based checking** sections belowーor alternatively, consider
automating your HTML checking with a frontend such as:

   [13]: https://github.com/validator/validator/releases/latest

  * [Grunt plugin for HTML validation][14]

  * [Gulp plugin for HTML validation][15]

  * [HTML5 Validator Integration for Travis CI][16] (auto-check documents pushed
  to a github repo)

  * [LMVTFY: Let Me Validate That For You][17] (auto-check HTML of
  JSFiddle/JSBin etc. links in github issue comments)

   [14]: https://github.com/jzaefferer/grunt-html
   [15]: https://github.com/watilde/gulp-html
   [16]: https://github.com/svenkreiss/html5validator
   [17]: https://github.com/cvrebert/lmvtfy/


## vnu.jar in NPM
You can work with `vnu.jar` in CommonJS modules.

### Install latest release version
```sh
$ npm install --save vnu-jar
```

### Install latest dev version
```sh
$ npm install --save vnu-jar@dev
```

### Example
For Node.js 6+
```javascript
'use strict';

const exec = require ( 'child_process' ).exec;
const vnu = require ( 'vnu-jar' );

// Print path to vnu.jar
console.log ( vnu );

// Work with vnu.jar
// for example get vnu.jar version
exec ( `java -jar ${vnu} --version`, ( error, stdout ) => {

	if ( error ) {
		console.error ( `exec error: ${error}` );
		return;
	}

	console.log ( stdout );

} );

```


## Usage

You can use the `vnu.jar` HTML checker as an executable for command-line
checking of documents by invoking it like this:

      java -jar vnu.jar [--errors-only] [--no-stream]
           [--format gnu|xml|json|text] [--help] [--html] [--no-langdetect]
           [--skip-non-html] [--verbose] [--version] FILES

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

    https://github.com/validator/validator/wiki/Service:-Common-parameters#out

#### --help

    Shows detailed usage information.

#### --skip-non-html

    Skip documents that don’t have *.html, *.htm, *.xhtml, or *.xht extensions.

    default: [unset; all documents found are checked, regardless of extension]

#### --html

    Forces any *.xhtml or *.xht documents to be parsed using the HTML parser.

    default: [unset; XML parser is used for *.xhtml and *.xht documents]

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
client][18]ーcan be run as an HTTP service, similar to [checker.html5.org][19],
[html5.validator.nu][20], and [validator.w3.org/nu][21], for browser-based
checking of HTML documents over the Web. To that end, the checker is released as
two separate packages:

   [18]: https://validator.github.io/validator/#usage
   [19]: https://checker.html5.org/
   [20]: https://html5.validator.nu/
   [21]: http://validator.w3.org/nu/

  * `vnu.jar` for deploying the checker as a simple self-contained service
  * `vnu.war` for deploying the checker to a servlet container such as Tomcat

Both deployments expose a REST API that enables checking of HTML documents from
other clients, not just web browsers. And the `vnu.jar` package also includes a
simple HTTP client that enables you to either send documents to a
locally-running instance of the checker HTTP serviceーfor fast command-line
checkingーor to any remote instance of the checker HTTP service running anywhere
on the Web.

The [latest releases of the vnu.jar and vnu.war packages][22] are available from
the `validator` project at github. The following are detailed instructions on
using them.

   [22]: https://github.com/validator/validator/releases/latest

**Note:** Replace _"~/vnu.jar"_ or _"~/vnu.war"_ below with the actual paths to
those files on your system.

### Standalone web server

To run the checker as a standalone service (using a built-in Jetty server), open
a new terminal window and invoke `vnu.jar` like this:

        java -cp ~/vnu.jar nu.validator.servlet.Main 8888

Then open [http://localhost:8888][23] in a browser. (To have the checker listen
on a different port, replace `8888` with the port number.)

   [23]: http://localhost:8888

You’ll see a form similar to [validator.w3.org/nu][24] that allows you to enter
the URL of an HTML document and have the results for that document displayed in
the browser.

   [24]: http://validator.w3.org/nu/

**Note:** If you get a `StackOverflowError` error when using the vnu.jar file,
try adjusting the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar nu.validator.servlet.Main 8888

### Deployment to servlet container

To run the checker inside of an existing servlet container such as Apache Tomcat
you will need to deploy the `vnu.war` file to that server following its
documentation. For example, on Apache Tomcat you could do this using the
[Manager][25] application or simply by copying the file to the `webapps`
directory (since that is the default `appBase` setting). Typically you would see
a message similar to the following in the `catalina.out` log file.

   [25]: http://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

    May 7, 2014 4:42:04 PM org.apache.catalina.startup.HostConfig deployWAR
    INFO: Deploying web application archive /var/lib/tomcat7/webapps/vnu.war

Assuming your servlet container is configured to receive HTTP requests sent to
`localhost` on port `80` and the context root of this application is `vnu`
(often the default behavior is to use the WAR file's filename as the context
root unless one is explicitly specified) you should be able to access the
application by connecting to [http://localhost/vnu/][26].

   [26]: http://localhost/vnu/

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
[html5.validator.nu/][27], use the nu.validator.client.host and
nu.validator.client.port options, like this:

   [27]: http://html5.validator.nu/

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
[github.com/validator/validator/wiki/Service:-Common-parameters][28].

   [28]: https://github.com/validator/validator/wiki/Service:-Common-parameters

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

    https://github.com/validator/validator/wiki/Service:-Common-parameters#parser

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

    https://github.com/validator/validator/wiki/Service:-Common-parameters#out

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

## Build instructions

Follow the steps below to build, test, and run the checker such that you can
open `http://localhost:8888/` in a Web browser to use the checker Web UI.

  1. Make sure you have git, python, and JDK 8 installed.

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

