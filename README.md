# The Nu Html Checker (vnu)

With the Nu Html Checker (vnu), you can:

- [catch unintended mistakes in your HTML, CSS, and SVG][1]
- [batch-check documents from the command line][2] and from other scripts/apps
- [deploy your own instance of the vnu checker as a service][3] (like [validator.w3.org/nu][4])

   [1]: https://validator.w3.org/nu/about.html#why-validate
   [2]: #usage
   [3]: #standalone-web-server
   [4]: https://validator.w3.org/nu/

## Usage

You can run the vnu checker with one of the invocations from the [vnu manual page](docs/vnu.1.md). For example:

    java -jar ~/vnu.jar [OPTIONS]... FILES|DIRECTORY|URL...

The [OPTIONS section of the vnu manual page](docs/vnu.1.md#options) has details on all available options.

## Sources

The vnu source code is available on [GitHub][5], as are [instructions on how to build, test, and run the code][6].

   [5]: https://github.com/validator/validator
   [6]: #build-instructions

## Binaries and packages

The Nu Html Checker (vnu) is released upstream in these formats:

- pre-compiled Linux, Windows, and macOS binaries that include an embedded Java runtime
- `vnu.jar` — a portable version you can use on any system that has Java 11 or above installed
- `vnu.war` — for [deploying the vnu checker service through a servlet container such as Tomcat][11]

   [11]: #servlet-container

> [!NOTE]
> The `vnu.jar` and `vnu.war` files require you to have Java 11 or above installed. The pre-compiled Linux, Windows, and macOS binaries don’t require you to have any version of Java already installed at all.

A [Dockerfile][7] (see **[Pulling the Docker image][36]** below) and [npm][8], [pip][9], and [brew][10] packages are also available.

   [7]: https://ghcr.io/validator/validator
   [8]: https://www.npmjs.com/package/vnu-jar
   [9]: https://github.com/svenkreiss/html5validator
   [10]: https://formulae.brew.sh/formula/vnu
   [36]: #pulling-the-docker-image

You can [get the latest release][12] or run one of the following:

- [`docker run -it --rm -p 8888:8888 ghcr.io/validator/validator:latest`][13]
- [`npm install vnu-jar`][14]
- [`npm install --registry=https://npm.pkg.github.com @validator/vnu-jar`][15]
- [`brew install vnu`][16]
- [`pip install html5validator`][17]

…and see the **[Usage](#usage)** and **[Web-based checking](#web-based-checking)** sections here. Or automate your checking with a frontend such as:

   [12]: https://github.com/validator/validator/releases/tag/latest
   [13]: https://github.com/validator/validator/pkgs/container/validator
   [14]: https://www.npmjs.com/package/vnu-jar
   [15]: https://github.com/validator/validator/packages/892707
   [16]: https://libraries.io/homebrew/vnu
   [17]: https://github.com/svenkreiss/html5validator

- [Grunt plugin for HTML validation][18] or [Gulp plugin for HTML validation][19] or [Maven plugin for HTML validation][20]
- [html5validator `pip` package][21] (for integration in Travis CI, CircleCI, CodeShip, Jekyll, Pelican, etc.)
- [LMVTFY: Let Me Validate That For You][22] (auto-check JSFiddle/JSBin, etc., links in GitHub issue comments)

   [18]: https://github.com/validator/grunt-html
   [19]: https://github.com/validator/gulp-html
   [20]: https://github.com/validator/maven-plugin
   [21]: https://github.com/svenkreiss/html5validator
   [22]: https://github.com/cvrebert/lmvtfy/

## Web-based checking

The Nu Html Checker (vnu) — along with being usable as [a standalone command-line client][24] — can be run as an HTTP service, similar to [validator.w3.org/nu][25], for browser-based checking of HTML documents, CSS stylesheets, and SVG images over the Web. To that end, the vnu checker is released as several separate packages:

   [24]: #usage
   [25]: https://validator.w3.org/nu/

- Linux, Windows, and macOS binaries for deploying a self-contained service on any system
- `vnu.jar` for deploying a self-contained service on a system with Java installed
- `vnu.war` for deploying to a servlet container such as Tomcat

All deployments expose a REST API that enables checking of HTML documents, CSS stylesheets, and SVG images from other clients, not just web browsers. And the Linux, Windows, and macOS binaries and `vnu.jar` package also include a simple HTTP client that enables you to either send documents to a locally-running instance of the vnu checker HTTP service — for fast command-line checking — or to any remote instance of the vnu checker HTTP service running anywhere on the Web.

The [latest releases of the Linux, Windows, and macOS binaries and vnu.jar and vnu.war packages][26] are available from the `validator` project at github. The following are detailed instructions on using them.

   [26]: https://github.com/validator/validator/releases/tag/latest

> [!NOTE]
> Throughout these instructions, replace `~/vnu.jar` with the actual path to that jar file on your system, and replace `vnu-runtime-image/bin/java` and `vnu-runtime-image\bin\java.exe` with the actual path to the vnu `java` or `java.exe` program on your system — or if you add the `vnu-runtime-image/bin` or `vnu-runtime-image\bin` directory your system `PATH` environment variable, you can invoke the vnu checker with just `java nu.validator.servlet.Main 8888`.

### Standalone web server

See [vnu-server](docs/vnu-server.1.md) for invocation manual page.

### Servlet container

To run the vnu checker inside of an existing servlet container such as Apache Tomcat you will need to deploy the `vnu.war` file to that server following its documentation. For example, on Apache Tomcat you could do this using the [Manager][30] application or simply by copying the file to the `webapps` directory (since that is the default `appBase` setting). Typically you would see a message similar to the following in the `catalina.out` log file.

   [30]: https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

    May 7, 2014 4:42:04 PM org.apache.catalina.startup.HostConfig deployWAR
    INFO: Deploying web application archive /var/lib/tomcat7/webapps/vnu.war

Assuming your servlet container is configured to receive HTTP requests sent to `localhost` on port `80` and the context root of this application is `vnu` (often the default behavior is to use the WAR file's filename as the context root unless one is explicitly specified) you should be able to access the application by connecting to [http://localhost/vnu/][31].

   [31]: http://localhost/vnu/

> [!NOTE]
> You may want to customize the `/WEB-INF/web.xml` file inside the WAR file (you can use any ZIP-handling program) to modify the servlet filter configuration. For example, if you wanted to disable the inbound-size-limit filter, you could comment out that filter like this:

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

The vnu checker is packaged with an HTTP client you can use from the command line to either send documents to a locally-running instance of the vnu checker HTTP service — for fast command-line checking — or to a remote instance anywhere on the Web.

To check documents locally using the packaged HTTP client, do this:

  1. Start up the vnu checker as a local HTTP service, as described in the [**Standalone web server**][37] section.

  2. Invoke the HTTP client like from the commandline according to [vnu-client](docs/vnu-client.1.md) manual page.

   [37]: #standalone-web-server

## Pulling the Docker image

You can pull the vnu Docker image from [https://ghcr.io/validator/validator][34] in the GitHub container registry.

   [34]: https://ghcr.io/validator/validator

To pull and run the latest version of the vnu checker:

      docker run -it --rm -p 8888:8888 ghcr.io/validator/validator:latest

To pull and run a specific tag/version of the vnu checker from the container registry — for example, the `17.11.1` version:

      docker run -it --rm -p 8888:8888 ghcr.io/validator/validator:17.11.1

To bind the vnu checker to a specific address (rather than have it listening on all interfaces):

      docker run -it --rm -p 128.30.52.73:8888:8888 ghcr.io/validator/validator:latest

To make the vnu checker run with a connection timeout and socket timeout different from the default 5 seconds, use the `CONNECTION_TIMEOUT_SECONDS` and `SOCKET_TIMEOUT_SECONDS` environment variables:

      docker run -it --rm \
         -e CONNECTION_TIMEOUT_SECONDS=15 \
         -e SOCKET_TIMEOUT_SECONDS=15 \
         -p 8888:8888 \
         validator/validator

To make the vnu checker run with particular Java system properties set, use the `JAVA_TOOL_OPTIONS` environment variable:

      docker run -it --rm \
         -e JAVA_TOOL_OPTIONS=-Dnu.validator.client.asciiquotes=yes  \
         -p 8888:8888 \
         validator/validator

To define a service named `vnu` for use with `docker compose`, create a Compose file named `docker-compose.yml` (for example), with contents such as the following:

      version: '2' services:
        vnu:
          image: validator/validator ports:
            - "8888:8888"
          network_mode: "host" #so "localhost" refers to the host machine.

## Build instructions

Follow the steps below to build, test, and run the vnu checker such that you can open `http://0.0.0.0:8888/` in a Web browser to use the vnu checker Web UI.

  1. Make sure you have git, python, and JDK 8 or above installed.

  2. Set the `JAVA_HOME` environment variable:

         export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64    <-- Ubuntu, etc.

         export JAVA_HOME=$(/usr/libexec/java_home)            <-- macOS

  3. Create a working directory:

         git clone https://github.com/validator/validator.git

  4. Change into your working directory:

         cd validator

  5. Start the `checker.py` Python script:

         python ./checker.py all

The first time you run the `checker.py` Python script, you’ll need to be online and the build will need time to download several megabytes of dependencies.

The steps above will build, test, and run the vnu checker such that you can open `http://0.0.0.0:8888/` in a Web browser to use the vnu checker Web UI.

> [!WARNING]
> Future checker releases will bind by default to the address `127.0.0.1`. Your checker deployment might become unreachable unless you use the `--bind-address` option to bind to a different address:

    python ./checker.py --bind-address=128.30.52.73 all

Use `python ./checker.py --help` to see command-line options for controlling the behavior of the script, as well as build-target names you can call separately; e.g.:

    python ./checker.py build       # to build only

    python ./checker.py build test  # to build and test

    python ./checker.py run         # to run only

    python ./checker.py jar         # to compile vnu.jar

    python ./checker.py update-shallow && \
      python ./checker.py dldeps && \
      python ./checker.py jar       # to compile vnu.jar faster

## Additional documentation

Additional documentation is available on the [vnu wiki](https://github.com/validator/validator/wiki/).
