## Grant of license

By contributing to this project, you agree to license your contributions under
[an MIT license](https://github.com/validator/validator/blob/master/LICENSE) and
to waive any requirement to include an additional copyright notice.

## Questions or problems?

Along with using the [project issue tracker][1] you can get help in real time on
the [#whatwg channel on irc.freenode.net][2].

   [1]: https://github.com/validator/validator/issues
   [2]: http://webchat.freenode.net/?channels=whatwg

## Want to build, test, and run the code?

Follow the steps below to build, test, and run the checker such that you can open
http://localhost:8888/ in a Web browser to use the checker Web UI.

1. Make sure you have git, python, and JDK 5 or later installed.

2. Set the `JAVA_HOME` environment variable:

   * `export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64` (Ubuntu)
   * `export JAVA_HOME=$(/usr/libexec/java_home)` (Mac OS X)

3. Start the build script:
```
     python ./build/build.py all; python ./build/build.py all
```

**Important:** Yes, you need to run the build script twice the very first
time you build— to work around some known issues that cause the build to
fail the first time during startup when you build from scratch in a fresh
working directory.  For subsequent builds, you only need to run the build
script once.  Also note that the first time you run it, the build script
will spend a lot of time downloading a bunch of dependencies (~300MB).

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

* `src/nu/validator`
  * `client` - various clients; e.g., `SimpleCommandLineValidator`, `TestRunner`
  * `localentities` - fetching resources from local cache; `LocalCacheEntityResolver`
  * `messages` - handling/emitting validation messages; `MessageEmitterAdapter`, etc.
  * `servlet` - core service logic; `VerifierServletTransaction`, etc.
  * `source` - handling/emitting "show source" output; `SourceHandler` & `SourceCode`
  * `spec` - parsing the HTML spec & emitting spec excerpts in validation messages
  * `validation` - entry point for 3rd-party code to use; `SimpleDocumentValidator`

* `sample` - sample validation-client scripts (Python) for, e.g., batch checking
* `schema` - RelaxNG schema-driver files + SVG & MathML RelaxNG schemas
* `site` - JavaScript & CSS for the checker Web UI + “About” page HTML source
* `test-harness` - script (Python) for "full-stack" testing of the checker
* `xml-src` - templates used for building HTML source of checker Web UI
* `entity-map.txt` - catalog used in fetching certain resources from local cache
* `presets.txt` - catalog for determining which resources to use during checking
