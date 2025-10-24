<!-- -*- fill-column: 92 -*- vim: set textwidth=92 : -->

The HTML checker provides various mechanisms to filter out (drop/ignore/suppress)
errors/warnings you don’t care about/don’t want to see/don’t consider to be a problem:

* You can filter messages reported by https://validator.w3.org/nu/
  * Use the **Message filtering** button in the results page shown after document checking

* [You can filter messages with the command-line checker]
  * [Use the `--filterfile` option]
  * [Use the `--filterpattern` option]

* [You can filter messages in the network API and Web checker]
  * [Use the `filterurl` query param]
  * [Use the `filterpattern` query param]

* [You can filter messages globally for your own checker service]
  * [Use the `resources/message-filters.txt` file]
  * [Use the `--filter-file` option to the build script]
  * [Use the `nu.validator.servlet.filterfile` Java system property]

## You can filter messages with the command-line checker

Use the `--filterfile` and `--filterpattern` options to filter out error messages and
warning messages emitted by the vnu.jar command-line checker.

### Use the `--filterfile` option

Use the `--filterfile` command-line option to specify a filename. Each line of the specified
file contains either a regular expression or starts with `#` to indicate the line is a
comment. Any error message or warning message that matches a regular expression in the file
is filtered out (dropped/suppressed).

    java -jar ~/vnu.jar --filterfile ~/my-message-filters.txt FILE.html

### Use the `--filterpattern` option

Use the `--filterpattern` command-line option to specify a regular expression. Any error
message or warning message matching the expression is filtered out (dropped/suppressed).

As with all other vnu.jar command-line checker options, this option may only be specified
once. So to filter multiple error messages or warning messages, you must provide a single
regular expression that will match all the messages. The typical way to do that for regular
expressions is to OR multiple patterns together using the "`|`" character.

    java -jar ~/vnu.jar \
      --filterpattern=".*Unicode Normalization.*|.*appears to be written in.*" FILE.html

## You can filter messages in the network API and Web checker

Use the `filterurl` and `filterpattern` query parameters to filter out error messages and
warning messages per-request when using the checker network API or the Web-base checker UI.

### Use the `filterurl` query param

Use the `filterurl` query parameter to specify a URL for a filter file. Each line of the
specified file contains either a regular expression or starts with `#` to indicate the line
is a comment. Any error message or warning message that matches a regular expression in the
file is filtered out (dropped/suppressed).

    curl -s -H 'Content-Type: text/html; charset=utf-8' \
      --data-binary @FILE.html \
      "https://checker.html5.org/?out=json&filterurl=https://example.com/filters.txt

### Use the `filterpattern` query param

Use the `filterpattern` query parameter to specify a **percent-encoded**  regular-expression
pattern. Any error message or warning message that matches the pattern is filtered out
(dropped/suppressed).

    curl -s -H 'Content-Type: text/html; charset=utf-8' \
      --data-binary @FILE.html \
      "https://checker.html5.org/?out=json&filterpattern=.*Unicode%20Normalization.*

## You can filter messages globally for your own checker service

When running your own instance of the checker, use the `resources/message-filters.txt` file
or the `--filter-file` option to the build script or the `nu.validator.servlet.filterfile`
Java system property to globally filter out particular error messages and warning messages.

### Use the `resources/message-filters.txt` file

To filter out (drop/ignore/suppress) errors/warnings you don’t care about/don’t want to
see/don’t consider a problem, use the `resources/message-filters.txt` file. Each line of the
file contains either a regular expression or starts with `#` to indicate the line is a
comment. Any error message or warning message that matches a regular expression in the file
is filtered out (dropped/suppressed) globally for all requests made by all users of the service.

### Use the `--filter-file` option to the build script

Use the `--filter-file` build option to specify a different pathname to override the default
`resources/message-filter.txt` location.

    ./build/build.sh --filter-file=/usr/local/validator/message-filters.txt run

### Use the `nu.validator.servlet.filterfile` Java system property

Use the `nu.validator.servlet.filterfile` Java system property to specify a different
pathname to override the default `resources/message-filter.txt` location.

    java -Dnu.validator.servlet.filterfile=/usr/local/validator/message-filters.txt \
      -cp ~/vnu.jar nu.validator.servlet.Main 8888

[You can filter messages with the command-line checker]:
#you-can-filter-messages-with-the-command-line-checker

[Use the `--filterfile` option]:
#use-the---filterfile-option

[Use the `--filterpattern` option]:
#use-the---filterpattern-option

[You can filter messages in the network API and Web checker]:
#you-can-filter-messages-in-the-network-api-and-web-checker

[Use the `filterurl` query param]:
#use-the-filterurl-query-param

[Use the `filterpattern` query param]:
#use-the-filterpattern-query-param

[You can filter messages globally for your own checker service]:
#you-can-filter-messages-globally-for-your-own-checker-service

[Use the `resources/message-filters.txt` file]:
#use-the-resourcesmessage-filterstxt-file

[Use the `--filter-file` option to the build script]:
#use-the---filter-file-option-to-the-build-script

[Use the `nu.validator.servlet.filterfile` Java system property]:
#use-the-nuvalidatorservletfilterfile-java-system-property
