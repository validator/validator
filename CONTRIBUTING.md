## Code contributions

The [Grant of license](CONTRIBUTING.md#grant-of-license) section at the end of this file has details about licensing code contributions to this project, and about "signing off" on code contributions. Please make sure to read it.

## Questions or problems?

Along with using the [project issue tracker][1] you can get help in real time in the [#whatwg chat room][2].

   [1]: https://github.com/validator/validator/issues
   [2]: https://matrix.to/#/#whatwg:matrix.org

## Want to build, test, and run the code?

Follow the steps below to build, test, and run the checker such that you can open http://localhost:8888/ in a Web browser to use the checker Web UI.

1. Make sure you have git, python, a 64-bit JDK 17 or above, [ant](https://ant.apache.org/manual/install.html), and [maven](https://maven.apache.org/install.html) installed.

2. Set the `JAVA_HOME` environment variable:

        export JAVA_HOME=@@/PATH/TO/JDK/ON/YOUR/SYSTEM@@

   For example:

   * `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64` (newer Ubuntu)
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

## Running e2e tests

The project includes Playwright end-to-end tests for the web-based checker UI, located in the `e2e/` directory. To set up and run those tests, do the following:

1. Install [pnpm](https://pnpm.io/installation), if you don't have it.

2. Install dependencies and Playwright:

        pnpm install
        pnpm exec playwright install chromium

3. Run the e2e tests:

        python ./checker.py e2e-tests

The e2e tests are also automatically run when you run `python ./checker.py test` (if Playwright is available).

## Bundled HTML spec

A copy of the WHATWG HTML spec is bundled into the checker — so it can emit excerpts of the spec in error/warning messages — and is tracked in the repo at `resources/spec/html5spec-single.html`. Ordinary builds and CI use that committed copy; nothing is downloaded at build time.

The committed copy is refreshed by `.github/workflows/refresh-html5spec.yml` — which runs after each successful `Build` workflow run on `main` (and on `workflow_dispatch`), and opens a PR if the spec has changed upstream. You shouldn’t normally need to refresh it by hand. If you do want to test against the latest upstream spec locally:

    ant -f build/build.xml dl-html5spec

That writes directly to the tracked file — so it’ll dirty your working tree. Either revert it with `git checkout -- resources/spec/html5spec-single.html` when you’re done, or else open a spec-refresh PR if the changes look intentional.

## Commit-message title prefixes

The project follows a convention of requiring commit-message titles to start with “type” prefixes; in particular:

Type     | Purpose
-------  |--------
`fix:`   | For cases where your change fixes existing problems in the code.
`feat:`  | For cases where your change allows/enables new checker support for a spec feature (typically, new features that’ve been added to the HTML spec, the ARIA in HTML spec, or the ARIA spec).
`build:` | For cases where your change is to sources in the `build/` directory or to the `checker.py` script.
`test:`  | For any test-only changes you make to tests in the `tests/` directory.

About the `test:` prefix: You normally want to include appropriate test changes in your `feat:` and `fix:` commits — so the only case when you’d typically want to use the `test:` prefix is when your change really is a test-only change, with no related code changes.

The other type prefixes commonly used by the project include `docs:`, `chore:`, and `ci:`. Those are all used by the project in the normal way they’re used in other projects — with no further project-specific meanings.

## Commit hooks

The repository contains a file called `.pre-commit-config.yaml` that defines “commit hook” behavior to be run locally in your environment each time you commit a change to the sources.

To enable that “commit hook” behavior, first follow the installation instructions at https://pre-commit.com/#install, and then run both of these commands:

    pre-commit install
    pre-commit install --hook-type commit-msg

## Confused about the code? Don’t know where to look?

If you’d like to contribute a bug fix or feature enhancement but aren’t sure where in the code to get started, here’s a brief annotated overview of the repository contents:

* `assets` — CSS and JavaScript sources for https://validator.github.io/validator/
* `checker.py` — build script
* `build` – scripts for building, testing, and running the checker
* `css-validator` – (subtree) CSS validator source code
* `docs` – vnu manual pages + makefile to generate actual (roff) man pages from them
* `docs/wiki` – (subtree) https://github.com/validator/validator/wiki sources
* `e2e` – Playwright end-to-end tests for the web-based checker UI
* `galimatias` – URL parser
* `htmlparser` – (subtree) HTML parser
* `langdetect` – language-detection library
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
* `tests` – valid/invalid HTML docs for (regression) testing the checker
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
