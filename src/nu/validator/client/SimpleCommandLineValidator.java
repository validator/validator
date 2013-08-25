/*
 * Copyright (c) 2013 Mozilla Foundation
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.validator.validation.SimpleValidator;
import nu.validator.xml.SystemErrErrorHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * Simple command-line validator for HTML/XHTML files.
 */
public class SimpleCommandLineValidator {

    private static SimpleValidator validator;

    private static SystemErrErrorHandler errorHandler;

    private static boolean verbose;

    public static void main(String[] args) throws SAXException, Exception {
        errorHandler = new SystemErrErrorHandler();
        String schemaUrl = "http://s.validator.nu/html5-all.rnc";
        verbose = false;
        boolean hasFileArgs = false;
        int fileArgsStart = 0;
        if (args.length == 0) {
            usage();
            return;
        }
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                hasFileArgs = true;
                fileArgsStart = i;
                break;
            } else {
                if ("-v".equals(args[i])) {
                    verbose = true;
                } else if ("-s".equals(args[i])) {
                    schemaUrl = args[++i];
                }
            }
        }
        if (hasFileArgs) {
            List<File> files = new ArrayList<File>();
            for (int i = fileArgsStart; i < args.length; i++) {
                files.add(new File(args[i]));
            }
            validator = new SimpleValidator();
            validateFilesAgainstSchema(files, schemaUrl);
        }
    }

    private static void validateFilesAgainstSchema(List<File> files,
            String schemaUrl) throws SAXException, Exception {
        validator.setUpSchema(schemaUrl);
        validator.setUpParser(errorHandler);
        checkFiles(files);
    }

    private static void checkFiles(List<File> files) throws SAXException,
            IOException {
        for (File file : files) {
            errorHandler.reset();
            if (file.isDirectory()) {
                recurseDirectory(file);
            } else {
                checkHtmlFile(file);
            }
        }
    }

    private static void recurseDirectory(File directory) throws SAXException,
            IOException {
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

    private static void checkHtmlFile(File file) throws IOException,
            SAXException {
        String path = file.getPath();
        if (path.matches("^http:/[^/].+$")) {
            path = "http://" + path.substring(path.indexOf("/") + 1);
            emitFilename(path);
            validator.checkHttpURL(new URL(path));
        } else if (path.matches("^https:/[^/].+$")) {
            path = "https://" + path.substring(path.indexOf("/") + 1);
            emitFilename(path);
            validator.checkHttpURL(new URL(path));
        } else if (!file.exists()) {
            if (verbose) {
                errorHandler.warning(new SAXParseException("File not found.",
                        null, file.toURI().toURL().toString(), -1, -1));
            }
            return;
        } else if (isHtml(file)) {
            emitFilename(path);
            validator.checkHtmlFile(file, true);
        } else if (isXhtml(file)) {
            emitFilename(path);
            validator.checkXmlFile(file);
        } else {
            if (verbose) {
                errorHandler.warning(new SAXParseException(
                        "File was not checked. Files must have a .html,"
                                + " .xhtml, .htm, or .xht extension.", null,
                        file.toURI().toURL().toString(), -1, -1));
            }
        }
    }

    private static boolean isXhtml(File file) {
        String name = file.getName();
        return (name.endsWith(".xhtml") || name.endsWith(".xht"));
    }

    private static boolean isHtml(File file) {
        String name = file.getName();
        return (name.endsWith(".html") || name.endsWith(".htm"));
    }

    private static void emitFilename(String name) {
        if (verbose) {
            System.out.println(name);
        }
    }

    private static void usage() {
        System.out.println("");
        System.out.println("To validate one or more files from the command line:");
        System.out.println("");
        System.out.println("  java -jar vnu.jar FILE.html FILE2.html FILE3.HTML FILE4.html...");
        System.out.println("");
        System.out.println("To validate all HTML files in a particular directory:");
        System.out.println("");
        System.out.println("  java -jar vnu.jar some-directory-name/");
        System.out.println("");
        System.out.println("To validate a Web document:");
        System.out.println("");
        System.out.println("  java -jar vnu.jar http://example.com/foo");
        System.out.println("");
        System.out.println("To start the validator as an HTTP service on port 8888:");
        System.out.println("");
        System.out.println("  java -cp vnu.jar nu.validator.servlet.Main 8888");
        System.out.println("");
        System.out.println("To validate one or more files with a running instance of the validator HTTP service:");
        System.out.println("");
        System.out.println("  java -cp vnu.jar nu.validator.client.HttpClient FILE.html FILE2.html...");
    }
}
