/*
 * Copyright (c) 2013-2015 Mozilla Foundation
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.client;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import nu.validator.htmlparser.sax.XmlSerializer;
import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.JsonMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.messages.XmlMessageEmitter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.validation.SimpleDocumentValidator.SchemaReadException;
import nu.validator.xml.SystemErrErrorHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * Simple command-line validator for HTML/XHTML files.
 */
public class SimpleCommandLineValidator {

    private static Package pkg = SimpleCommandLineValidator.class.getPackage();

    private static String version = pkg.getImplementationVersion();

    private static SimpleDocumentValidator validator;

    private static OutputStream out;

    private static MessageEmitterAdapter errorHandler;

    private static boolean verbose;

    private static boolean errorsOnly;

    private static boolean loadEntities;

    private static boolean noStream;

    private static boolean skipNonHTML;

    private static boolean forceHTML;

    private static boolean asciiQuotes;

    private static int lineOffset;

    private static enum OutputFormat {
        HTML, XHTML, TEXT, XML, JSON, RELAXED, SOAP, UNICORN, GNU
    }

    private static OutputFormat outputFormat;

    public static void main(String[] args) throws SAXException, Exception {
        out = System.err;
        System.setProperty("nu.validator.datatype.warn", "true");
        errorsOnly = false;
        skipNonHTML = false;
        forceHTML = false;
        loadEntities = false;
        noStream = false;
        lineOffset = 0;
        asciiQuotes = false;
        verbose = false;

        String outFormat = null;
        String schemaUrl = null;
        boolean hasFileArgs = false;
        boolean readFromStdIn = false;
        int fileArgsStart = 0;
        if (args.length == 0) {
            usage();
            System.exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-")) {
                readFromStdIn = true;
                break;
            } else if (!args[i].startsWith("--")) {
                hasFileArgs = true;
                fileArgsStart = i;
                break;
            } else {
                if ("--verbose".equals(args[i])) {
                    verbose = true;
                } else if ("--errors-only".equals(args[i])) {
                    errorsOnly = true;
                    System.setProperty("nu.validator.datatype.warn", "false");
                } else if ("--format".equals(args[i])) {
                    outFormat = args[++i];
                } else if ("--version".equals(args[i])) {
                    if (version != null) {
                        System.out.println(version);
                    } else {
                        System.out.println("[uknown version]");
                    }
                    System.exit(0);
                } else if ("--help".equals(args[i])) {
                    help();
                    System.exit(0);
                } else if ("--skip-non-html".equals(args[i])) {
                    skipNonHTML = true;
                } else if ("--html".equals(args[i])) {
                    forceHTML = true;
                } else if ("--entities".equals(args[i])) {
                    loadEntities = true;
                } else if ("--no-stream".equals(args[i])) {
                    noStream = true;
                } else if ("--schema".equals(args[i])) {
                    schemaUrl = args[++i];
                    if (!schemaUrl.startsWith("http:")) {
                        System.err.println("error: The \"--schema\" option"
                                + " requires a URL for a schema.");
                        System.exit(1);
                    }
                }
            }
        }
        if (schemaUrl == null) {
            schemaUrl = "http://s.validator.nu/html5-rdfalite.rnc";
        }
        if (outFormat == null) {
            outputFormat = OutputFormat.GNU;
        } else {
            if ("text".equals(outFormat)) {
                outputFormat = OutputFormat.TEXT;
            } else if ("gnu".equals(outFormat)) {
                outputFormat = OutputFormat.GNU;
            } else if ("xml".equals(outFormat)) {
                outputFormat = OutputFormat.XML;
            } else if ("json".equals(outFormat)) {
                outputFormat = OutputFormat.JSON;
            } else {
                System.err.printf("Error: Unsupported output format \"%s\"."
                        + " Must be \"gnu\", \"xml\", \"json\","
                        + " or \"text\".\n", outFormat);
                System.exit(1);
            }
        }
        if (readFromStdIn) {
            InputSource is = new InputSource(System.in);
            validator = new SimpleDocumentValidator();
            setup(schemaUrl);
            validator.checkHtmlInputSource(is);
            end();
        } else if (hasFileArgs) {
            validator = new SimpleDocumentValidator();
            setup(schemaUrl);
            checkFiles(args, fileArgsStart);
            end();
        } else {
            System.err.printf("\nError: No documents specified.\n");
            usage();
            System.exit(1);
        }
    }

    private static void setup(String schemaUrl) throws SAXException, Exception {
        setErrorHandler();
        errorHandler.setHtml(true);
        errorHandler.start(null);
        try {
            validator.setUpMainSchema(schemaUrl, new SystemErrErrorHandler());
        } catch (SchemaReadException e) {
            System.out.println(e.getMessage() + " Terminating.");
            System.exit(1);
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError"
                    + " while evaluating HTML schema.");
            System.out.println("The checker requires a java thread stack size"
                    + " of at least 512k.");
            System.out.println("Consider invoking java with the -Xss"
                    + " option. For example:");
            System.out.println("\n  java -Xss512k -jar ~/vnu.jar FILE.html");
            System.exit(1);
        }
        validator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);
    }

    private static void end() throws SAXException {
        errorHandler.end("Document checking completed. No errors found.",
                "Document checking completed.");
        if (errorHandler.getErrors() > 0 || errorHandler.getFatalErrors() > 0) {
            System.exit(1);
        }
    }

    private static void checkFiles(String[] args, int fileArgsStart)
            throws IOException, SAXException {
        for (int i = fileArgsStart; i < args.length; i++) {
            if (args[i].startsWith("http://") || args[i].startsWith("https://")) {
                emitFilename(args[i]);
                try {
                    validator.checkHttpURL(new URL(args[i]));
                } catch (IOException e) {
                    errorHandler.error(new SAXParseException(e.toString(),
                            null, args[i], -1, -1));
                }
            } else {
                File file = new File(args[i]);
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            }
        }
    }

    private static void recurseDirectory(File directory) throws SAXException,
            IOException {
        if (directory.canRead()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            }
        }
    }

    private static void checkHtmlFile(File file) throws IOException {
        try {
            String path = file.getPath();
            if (!file.exists()) {
                if (verbose) {
                    errorHandler.warning(new SAXParseException(
                            "File not found.", null,
                            file.toURI().toURL().toString(), -1, -1));
                }
                return;
            } else if (isXhtml(file)) {
                emitFilename(path);
                if (forceHTML) {
                    validator.checkHtmlFile(file, true);
                } else {
                    validator.checkXmlFile(file);
                }
            } else if (isHtml(file)) {
                emitFilename(path);
                validator.checkHtmlFile(file, true);
            } else {
                if (verbose) {
                    errorHandler.warning(new SAXParseException(
                            "File was not checked. Files must have .html,"
                                    + " .xhtml, .htm, or .xht extensions.",
                            null, file.toURI().toURL().toString(), -1, -1));
                }
            }
        } catch (SAXException e) {
            if (!errorsOnly) {
                System.err.printf("\"%s\":-1:-1: warning: %s\n",
                        file.toURI().toURL().toString(), e.getMessage());
            }
        }
    }

    private static boolean isXhtml(File file) {
        String name = file.getName();
        return (name.endsWith(".xhtml") || name.endsWith(".xht"));
    }

    private static boolean isHtml(File file) {
        String name = file.getName();
        return (name.endsWith(".html") || name.endsWith(".htm") || !skipNonHTML);
    }

    private static void emitFilename(String name) {
        if (verbose) {
            System.out.println(name);
        }
    }

    private static void setErrorHandler() {
        SourceCode sourceCode = validator.getSourceCode();
        ImageCollector imageCollector = new ImageCollector(sourceCode);
        boolean showSource = false;
        if (outputFormat == OutputFormat.TEXT) {
            errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                    imageCollector, lineOffset, true, new TextMessageEmitter(
                            out, asciiQuotes));
        } else if (outputFormat == OutputFormat.GNU) {
            errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                    imageCollector, lineOffset, true, new GnuMessageEmitter(
                            out, asciiQuotes));
        } else if (outputFormat == OutputFormat.XML) {
            errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                    imageCollector, lineOffset, true, new XmlMessageEmitter(
                            new XmlSerializer(out)));
        } else if (outputFormat == OutputFormat.JSON) {
            String callback = null;
            errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                    imageCollector, lineOffset, true, new JsonMessageEmitter(
                            new nu.validator.json.Serializer(out), callback));
        } else {
            throw new RuntimeException("Bug. Should be unreachable.");
        }
        errorHandler.setErrorsOnly(errorsOnly);
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("    java -jar vnu.jar [--errors-only] [--no-stream]");
        System.out.println("         [--format gnu|xml|json|text] [--help] [--html]");
        System.out.println("         [--skip-non-html] [--verbose] [--version] FILES");
        System.out.println("");
        System.out.println("    java -cp vnu.jar nu.validator.servlet.Main 8888");
        System.out.println("");
        System.out.println("    java -cp vnu.jar nu.validator.client.HttpClient FILES");
        System.out.println("");
        System.out.println("For detailed usage information, use \"java -jar vnu.jar --help\" or see:");
        System.out.println("");
        System.out.println("  http://validator.github.io/");
        System.out.println("");
        System.out.println("To read from stdin, use \"-\" as the filename, like this: \"java -jar vnu.jar - \".");
    }

    private static void help() {
        try (InputStream help = SimpleCommandLineValidator.class.getClassLoader().getResourceAsStream(
                "nu/validator/localentities/files/cli-help")) {
            System.out.println("");
            for (int b = help.read(); b != -1; b = help.read()) {
                System.out.write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
