/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias.cli;

import io.mola.galimatias.ErrorHandler;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import io.mola.galimatias.URLParsingSettings;
import io.mola.galimatias.canonicalize.RFC2396Canonicalizer;
import io.mola.galimatias.canonicalize.RFC3986Canonicalizer;

/**
 * A command line interface to Galimatias URL parser.
 */
public class CLI {

    private static void printError(GalimatiasParseException error) {
        System.out.println("\t\tError: " + error.getMessage());
        if (error.getPosition() != -1) {
            System.out.println("\t\tPosition: " + error.getPosition());
        }
    }

    private static void printResult(URL url) {
        System.out.println("\tResult:");
        System.out.println("\t\tURL: " + url.toString());
        System.out.println("\t\tURL type: " + ((url.isHierarchical())? "hierarchical" : "opaque"));
        System.out.println("\t\tScheme: " + url.scheme());
        if (url.schemeData() != null) {
            System.out.println("\t\tScheme data: " + url.schemeData());
        }
        if (url.username() != null) {
            System.out.println("\t\tUsername: " + url.username());
        }
        if (url.password() != null) {
            System.out.println("\t\tPassword: " + url.password());
        }
        if (url.host() != null) {
            System.out.println("\t\tHost: " + url.host());
        }
        if (url.port() != -1) {
            System.out.println("\t\tPort: " + url.port());
        }
        if (url.path() != null) {
            System.out.println("\t\tPath: " + url.path());
        }
        if (url.query() != null) {
            System.out.println("\t\tQuery: " + url.query());
        }
        if (url.fragment() != null) {
            System.out.println("\t\tFragment: " + url.fragment());
        }
    }

    private static class PrintErrorHandler implements ErrorHandler {

        @Override
        public void error(GalimatiasParseException error) throws GalimatiasParseException {
            System.out.println("\tRecoverable error found;");
            System.out.println("\t\tError: " + error.getMessage());
            if (error.getPosition() != -1) {
                System.out.println("\t\tPosition: " + error.getPosition());
            }
        }

        @Override
        public void fatalError(GalimatiasParseException error) {

        }
    }

    private static ErrorHandler errorHandler = new PrintErrorHandler();

    public static void main(String[] args) {

        URLParsingSettings settings = URLParsingSettings.create().withErrorHandler(errorHandler);
        URL url = null;
        String whatwgUrlSerialized = "";
        String rfc3986UrlSerialized = "";
        String rfc2396UrlSerialized = "";

        boolean parseErrors;

        URL base = null;
        String input = "";
        if (args.length < 1) {
            System.err.println("Need a URL as input");
            System.exit(1);
        } else if (args.length == 1) {
            input = args[0];
        } else if (args.length == 2) {
            try {
                base = URL.parse(args[0]);
            } catch (GalimatiasParseException ex) {
                assert false; // shouldn't get in here unless serious bug
            }
            input = args[1];
        } else {
            System.err.println("Too many args");
            System.exit(1);
        }

        if (base == null) {
            try {
                base = URL.parse("http://example.org/foo/bar");
            } catch (GalimatiasParseException ex) {
                assert false; // shouldn't get in here unless serious bug
            }
        }

        System.out.println("Base: " + base.toString());
        System.out.println("Analyzing URL: " + input);

        try {
            System.out.println("Parsing...");
            url = URL.parse(settings, base, input);
            whatwgUrlSerialized = url.toString();
            printResult(url);
        } catch (GalimatiasParseException ex) {
            System.out.println("Parsing with WHATWG rules resulted in fatal error");
            printError(ex);
            return;
        }

        try {
            System.out.println("Canonicalizing with RFC 3986 rules...");
            rfc3986UrlSerialized = new RFC3986Canonicalizer().canonicalize(url).toString();
            if (whatwgUrlSerialized.equals(rfc3986UrlSerialized)) {
                System.out.println("\tResult identical to WHATWG rules");
            } else {
                printResult(url);
            }
        } catch (GalimatiasParseException ex) {
            System.out.println("Canonicalizing with RFC 3986 rules resulted in fatal error");
            printError(ex);
        }

        try {
            System.out.println("Canonicalizing with RFC 2396 rules...");
            rfc2396UrlSerialized = new RFC2396Canonicalizer().canonicalize(url).toString();
            if (rfc3986UrlSerialized.equals(rfc2396UrlSerialized)) {
                System.out.println("\tResult identical to RFC 3986 rules");
            } else {
                printResult(url);
            }
        } catch (GalimatiasParseException ex) {
            System.out.println("Canonicalizing with RFC 2396 rules resulted in fatal error");
            printError(ex);
        }

    }

}
