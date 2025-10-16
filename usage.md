## NAME

vnu – The Nu Html Checker

## SYNOPSIS

    vnu                  OPTIONS FILES
    vnu.bat              OPTIONS FILES
    java -jar ~/vnu.jar  OPTIONS FILES

...where FILES are documents to check, and OPTIONS are zero or more of:

    --asciiquotes --errors-only --Werror --exit-zero-always --stdout
    --filterfile FILENAME --filterpattern PATTERN --format gnu|xml|json|text
    --help --skip-non-css --css --skip-non-svg --svg --skip-non-html --html
    --xml --also-check-css --also-check-svg --user-agent USER_AGENT
    --no-langdetect --no-stream --verbose --version --entities --schema SCHEMA

To read from stdin, use "-" (that is, a dash) in place of FILES.

## DESCRIPTION

The Nu Html Checker (vnu) helps you catch unintended mistakes in your HTML,
CSS, and SVG. It enables you to batch-check documents from the command line
and from other scripts/apps, and to deploy your own instance of the checker
as a service (like https://validator.w3.org/nu/).

## OPTIONS

For details on all options and usage, try the "--help" option or see:

  https://validator.github.io/validator/
