## Code contributions

The [Grant of license](CONTRIBUTING.md#grant-of-license) section at the end of this file has details about licensing code contributions to this project, and about "signing off" on code contributions. Please make sure to read it.

## Questions or problems? [![Nu Html Checker chat room](https://goo.gl/1kHqwI)][2]

Along with using the [project issue tracker][1] you can get help in real time in the [#whatwg chat room][2].

   [1]: https://github.com/validator/validator/issues
   [2]: https://matrix.to/#/#whatwg:matrix.org

## Want to build, test, and run the code?

Follow the steps below to build, test, and run the checker such that you can open http://localhost:8888/ in a Web browser to use the checker Web UI.

1. Make sure you have git, python, and JDK 8 or later installed.

2. Set the `JAVA_HOME` environment variable:

        export JAVA_HOME=@@/PATH/TO/JDK/ON/YOUR/SYSTEM@@

   For example:

   * `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64` (newer Ubuntu)
   * `export JAVA_HOME=$(/usr/libexec/java_home)` (Mac OS X)

3. Create a working directory:

        git clone https://github.com/validator/validator.git

4. Change into your working directory:

        cd validator

5. Start the build script:

        python ./checker.py all

The steps above will build, test, and run the checker such that you can open http://localhost:8888/ in a Web browser to use the checker Web UI.

Use `python ./checker.py --help` to see command-line options for controlling the behavior of the script, as well as build-target names you can call separately; e.g.:

* `python ./checker.py build` (to build only)
* `python ./checker.py build test` (to build and test)
* `python ./checker.py run` (to run only)

## Confused about the code? Don’t know where to look?

If you’d like to contribute a bug fix or feature enhancement but aren’t sure where in the code to get started, here’s a brief annotated overview of the repository contents:

* `assets` — CSS and JavaScript sources for https://validator.github.io/validator/
* `checker.py` — build script
* `build` – scripts for building, testing, and running the checker
* `css-validator` – (subtree) CSS validator source code
* `docs` – vnu manual pages + makefile to generate actual (roff) man pages from them
* `docs/wiki` – (subtree) https://github.com/validator/validator/wiki sources
* `galimatias` – (subtree) URL parser
* `htmlparser` – (subtree) HTML parser
* `langdetect` – (subtree) language-detection library
* `jing-trang` – (subtree) RelaxNG engine
* `package.json` – build file for the npm package
* `resources` – config files, caching-related catalogs, and some example code
* `schema` – HTML+SVG+MathML RelaxNG schemas (used with jing in the checker backend)
* `site` – JS & CSS for the checker frontend + code for generating the frontend HTML
* `src/nu/validator`
  * `checker` – non-schema checkers; `TableChecker`, `schematronequiv/Assertions`, …
  * `client` – various clients; e.g., `SimpleCommandLineValidator`, `TestRunner`
  * `collections` – sorting of collections (utility code)
  * `datatype` – checking microsyntax of attribute values (datatype library)
  * `gnu/xml/aelfred2` – processing XML (XML parser)
  * `io` – variety of \*InputStream classes, `DataUri`, related exception classes
  * `json` – SAX-inspired streaming interface for writing JSON (utility code)
  * `localentities` – fetching resources from local cache; `LocalCacheEntityResolver`
  * `messages` – handling/emitting validation messages; `MessageEmitterAdapter`, etc.
  * `servlet` – core service logic; `VerifierServletTransaction`, etc.
  * `site` — the web-based checker’s frontend/UI code
  * `source` – handling/emitting "show source" output; `SourceHandler` & `SourceCode`
  * `spec` – parsing the HTML spec & emitting spec excerpts in validation messages
  * `validation` – entry point for 3rd-party code to use; `SimpleDocumentValidator`
  * `xml` – utility code of various kinds
* `tests` – (subtree) valid/invalid HTML docs for (regression) testing the checker
* `vnu-jar.js` — driver file for the npm package
* `_config.yml` — Jekyll config file https://validator.github.io/validator/
* `_layouts` — Jekyll layout file for https://validator.github.io/validator/

## Grant of license

Please read the https://github.com/validator/validator/blob/main/DCO file.

By contributing to this project, you agree to license your contributions under [the MIT license](https://github.com/validator/validator/blob/main/LICENSE) and to waive any requirement to include an additional copyright notice.

When contributing pull requests, please add a "Signed-off-by" line to your git commit messages to indicate that you have read all of the content of https://github.com/validator/validator/blob/main/DCO and that you certify your code contributions actually conform to the terms of that agreement.

To add a "Signed-off-by" line, invoke `git commit` with the `-s` option:

    git commit -s

To save yourself some time, you probably want to set `user.name` and `user.email` values in a git config file (e.g., in `~/.gitconfig`), like this:

    [user]
       name = Zaphod Beeblebrox
       email = zaphodb@example.com

Running `git commit -s` will then add a "Signed-off-by" line in this form:

    Signed-off-by: Zaphod Beeblebrox <zaphodb@example.com>

(Of course you need to instead use your own real name and e-mail address.)
