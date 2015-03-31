## Grant of license

By contributing to this project, you agree to license your contributions under
[an MIT license](https://github.com/validator/validator/blob/master/LICENSE) and
to waive any requirement to include an additional copyright notice.

To record a grant of license for contributions to the project, please visit the
[project CLAHub page](https://www.clahub.com/agreements/validator/validator)
and complete the steps on the page.

## Questions or problems?

Along with using the [project issue tracker][1] you can get help in real time on the
[validator project channel on Gitter][2] or [#whatwg channel on irc.freenode.net][3].

   [1]: https://github.com/validator/validator/issues
   [2]: https://gitter.im/validator/validator
   [3]: http://webchat.freenode.net/?channels=whatwg

## Want to build, test, and run the code?

Follow the steps below to build, test, and run the checker such that you can open
http://localhost:8888/ in a Web browser to use the checker Web UI.

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

   **Important:** Yes, you must run the script twice the first time you build— to work
   around known issues that cause it to fail to complete when run from scratch in a
   fresh working directory. For subsequent builds you only have to run it once. And note
   that the first time you run it, it will need time to download ~300MB of dependencies.

The steps above will build, test, and run the checker such that you can open
http://localhost:8888/ in a Web browser to use the checker Web UI.

Use `python ./build/build.py --help` to see command-line options for controlling the
behavior of the script, as well as build-target names you can call separately; e.g.:

* `python ./build/build.py build` (to build only)
* `python ./build/build.py build test` (to build and test)
* `python ./build/build.py run` (to run only)

## Confused about the code? Don’t know where to look?

If you’d like to contribute a bug fix or feature enhancement but aren’t sure where in
the code to get started, here’s a brief annotated overview of the repository contents:

* `build` - scripts for building, testing, and running the checker
* `htmlparser` - (submodule) HTML parser
* `jing-trang` - (submodule) RelaxNG engine
* `resources` - config files, caching-related catalogs, and some example code
* `schema` - HTML+SVG+MathML RelaxNG schemas (used with jing in the checker backend)
* `site` - JS & CSS for the checker frontend + code for generating the frontend HTML
* `src/nu/validator`
  * `checker` - non-schema checkers; `TableChecker`, `schematronequiv/Assertions`, …
  * `client` - various clients; e.g., `SimpleCommandLineValidator`, `TestRunner`
  * `collections` - sorting of collections (utility code)
  * `datatype` - checking microsyntax of attribute values (datatype library)
  * `gnu/xml/aelfred2` - processing XML (XML parser)
  * `io` - variety of \*InputStream classes, `DataUri`, related exception classes
  * `json` - SAX-inspired streaming interface for writing JSON (utility code)
  * `localentities` - fetching resources from local cache; `LocalCacheEntityResolver`
  * `messages` - handling/emitting validation messages; `MessageEmitterAdapter`, etc.
  * `servlet` - core service logic; `VerifierServletTransaction`, etc.
  * `source` - handling/emitting "show source" output; `SourceHandler` & `SourceCode`
  * `spec` - parsing the HTML spec & emitting spec excerpts in validation messages
  * `validation` - entry point for 3rd-party code to use; `SimpleDocumentValidator`
  * `xml` - utility code of various kinds
* `tests` - (submodule) valid/invalid HTML docs for (regression) testing the checker
