## Code contributions

The [Grant of license](CONTRIBUTING.md#grant-of-license) section at the end of this file
has details about licensing code contributions to this project, and about "signing off"
on code contributions. Please make sure to read it.

## Questions or problems? [![Nu Html Checker chat room](https://goo.gl/1kHqwI)][2]

Along with using the [project issue tracker][1] you can get help in real time on the
[validator project channel on Gitter][2] or [#whatwg channel on irc.freenode.net][3].

   [1]: https://github.com/validator/validator/issues
   [2]: https://gitter.im/validator/validator
   [3]: https://webchat.freenode.net/?channels=whatwg

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

        python ./build/build.py all

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

## Grant of license

Please read the https://github.com/validator/validator/blob/master/DCO file.

By contributing to this project, you agree to license your contributions under
[the MIT license](https://github.com/validator/validator/blob/master/LICENSE)
and to waive any requirement to include an additional copyright notice.

When contributing pull requests, please add a "Signed-off-by" line to your
git commit messages to indicate that you have read all of the content of
https://github.com/validator/validator/blob/master/DCO and that you certify
your code contributions actually conform to the terms of that agreement.

To add a "Signed-off-by" line, invoke `git commit` with the `-s` option:

    git commit -s

To save yourself some time, you probably want to set `user.name` and `user.email`
values in a git config file (e.g., in `~/.gitconfig`), like this:

    [user]
       name = Zaphod Beeblebrox
       email = zaphodb@example.com

Running `git commit -s` will then add a "Signed-off-by" line in this form:

    Signed-off-by: Zaphod Beeblebrox <zaphodb@example.com>

(Of course you need to instead use your own real name and e-mail address.)
