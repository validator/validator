# NAME

vnu-server – Standalone web server of the Nu Html Checker

# SYNOPSIS

`java -cp ~/vnu.jar              -D[property=value]... nu.validator.servlet.Main [port]`

`vnu-runtime-image/bin/java      -D[property=value]... nu.validator.servlet.Main [port]`

`vnu-runtime-image\bin\java.exe  -D[property=value]... nu.validator.servlet.Main [port]`

# DESCRIPTION

vnu-server is the standalone web server of the Nu Html Checker. It uses a
built-in Jetty server. Once the server is launched you can access it at
[http://0.0.0.0:8888][1] in a browser.

   [1]: http://0.0.0.0:8888

> [!WARNING]
> Future checker releases will bind by default to the address `127.0.0.1`.
Your checker deployment might become unreachable unless you use the
`nu.validator.servlet.bind-address` system property to bind the checker to
a different address:

When you open [http://0.0.0.0:8888][2] (or whatever URL corresponds to the
`nu.validator.servlet.bind-address` value you’re using), you’ll see a form
similar to [validator.w3.org/nu][3] that allows you to enter the URL of an
HTML document, CSS stylesheet, or SVG image, and have the results of
checking that resource displayed in the browser.

   [2]: http://0.0.0.0:8888
   [3]: https://validator.w3.org/nu/

# OPTIONS

## port

    Port number at which the server is reachable.
    Default: 8888

# PROPERTIES

## nu.validator.servlet.bind-address=ip_addess

    Binds the validator service to the specified IP address.
    default: 0.0.0.0 [causes the checker to listen on all interfaces]
    possible values: The IP address of any network interface

    example: nu.validator.servlet.bind-address=127.0.0.1

## nu.validator.servlet.connection-timeout=timeout

    Specifies the connection timeout.
    default: 5000
    possible values: number of milliseconds

    example: nu.validator.servlet.connection-timeout=5000

## nu.validator.servlet.socket-timeout=timeout

    Specifies the socket timeout.
    default: 5000
    possible values: number of milliseconds

    example: nu.validator.servlet.socket-timeout=5000

## nu.validator.servlet.allow-forbidden-hosts=true_or_false

    Allows requests to "forbidden" hosts (localhost, 127.0.0.1, etc.).
    default: false
    possible values: true or false

    example: nu.validator.servlet.allow-forbidden-hosts=true

# EXAMPLES

Start a vnu server accessible at `http://128.30.52.73:8888`.

    java -cp ~/vnu.jar -Dnu.validator.servlet.bind-address=128.30.52.73 nu.validator.servlet.Main 8888

    vnu-runtime-image/bin/java -Dnu.validator.servlet.bind-address=128.30.52.73 nu.validator.servlet.Main 8888

    vnu-runtime-image\bin\java.exe -Dnu.validator.servlet.bind-address=128.30.52.73 nu.validator.servlet.Main 8888

# NOTES

If you get a `StackOverflowError` error when using the checker, try adjusting
the thread stack size by providing the `-Xss` option to java:

      java -Xss512k -cp ~/vnu.jar nu.validator.servlet.Main 8888

      vnu-runtime-image/bin/java -Xss512k -m vnu/nu.validator.servlet.Main 8888

# SEE ALSO

[vnu-client(1)](vnu-client.1.md), [vnu(1)](vnu.1.md)
