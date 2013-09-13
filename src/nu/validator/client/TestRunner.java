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
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.mortbay.util.ajax.JSON;

import org.relaxng.datatype.DatatypeException;
import com.thaiopensource.relaxng.exceptions.BadAttributeValueException;

import org.whattf.datatype.Html5DatatypeException;

import nu.validator.validation.SimpleDocumentValidator;

public class TestRunner implements ErrorHandler {

    private boolean inError = false;

    private boolean emitMessages = false;

    private boolean exceptionIsWarning = false;

    private Exception exception = null;

    private SimpleDocumentValidator validator;

    private PrintWriter err;

    private PrintWriter out;

    private boolean failed = false;

    private static boolean verbose;

    private static final HashMap<String, String> DEFAULT_VALIDATION_MAP = new HashMap<String, String>();

    static {
        DEFAULT_VALIDATION_MAP.put("tests/html",
                "http://s.validator.nu/html5-all.rnc");
        DEFAULT_VALIDATION_MAP.put("tests/html-aria",
                "http://s.validator.nu/html5-all.rnc");
        DEFAULT_VALIDATION_MAP.put("tests/html-its",
                "http://s.validator.nu/html5-all.rnc");
        DEFAULT_VALIDATION_MAP.put("tests/html-rdfa",
                "http://s.validator.nu/html5-all.rnc");
        DEFAULT_VALIDATION_MAP.put("tests/html-rdfalite",
                "http://s.validator.nu/html5-rdfalite.rnc");
        DEFAULT_VALIDATION_MAP.put("tests/xhtml",
                "http://s.validator.nu/xhtml5-all.rnc");
    }

    public TestRunner() throws IOException {
        validator = new SimpleDocumentValidator();
        try {
            this.err = new PrintWriter(new OutputStreamWriter(System.err,
                    "UTF-8"));
            this.out = new PrintWriter(new OutputStreamWriter(System.out,
                    "UTF-8"));
        } catch (Exception e) {
            // If this happens, the JDK is too broken anyway
            throw new RuntimeException(e);
        }
    }

    private void checkHtmlFile(File file) throws IOException, SAXException {
        if (!file.exists()) {
            if (verbose) {
                out.println(String.format("\"%s\": warning: File not found.",
                        file.toURI().toURL().toString()));
                out.flush();
            }
            return;
        }
        if (verbose) {
            out.println(file);
            out.flush();
        }
        if (isHtml(file)) {
            validator.checkHtmlFile(file, true);
        } else if (isXhtml(file)) {
            validator.checkXmlFile(file);
        } else {
            if (verbose) {
                out.println(String.format(
                        "\"%s\": warning: File was not checked."
                                + " Files must have a .html, .xhtml, .htm,"
                                + " or .xht extension.",
                        file.toURI().toURL().toString()));
                out.flush();
            }
        }
    }

    private boolean isXhtml(File file) {
        String name = file.getName();
        return name.endsWith(".xhtml") || name.endsWith(".xht");
    }

    private boolean isHtml(File file) {
        String name = file.getName();
        return name.endsWith(".html") || name.endsWith(".htm");
    }

    private boolean isCheckableFile(File file) {
        return file.isFile() && (isHtml(file) || isXhtml(file));
    }

    private void recurseDirectory(File directory) throws SAXException,
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

    private void checkFiles(List<File> files) {
        for (File file : files) {
            reset();
            emitMessages = true;
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException e) {
            } catch (SAXException e) {
            }
            if (inError) {
                failed = true;
            }
        }
    }

    private void checkInvalidFiles(List<File> files) {
        for (File file : files) {
            reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException e) {
            } catch (SAXException e) {
            }
            if (!inError) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected an error but did not"
                                    + " encounter any.",
                            file.toURI().toURL().toString()));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkHasWarningFiles(List<File> files) {
        for (File file : files) {
            reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException e) {
            } catch (SAXException e) {
            }
            if (inError) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected a warning but encountered"
                                    + " an error first.",
                            file.toURI().toURL().toString()));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (!exceptionIsWarning) {
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected a warning but did not"
                                    + " encounter any.",
                            file.toURI().toURL().toString()));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (inError) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected a warning only but"
                                    + " encountered at least one error.",
                            file.toURI().toURL().toString()));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private enum State {
        EXPECTING_INVALID_FILES, EXPECTING_VALID_FILES, EXPECTING_ANYTHING
    }

    private void checkTestDirectoryAgainstSchema(File directory,
            String schemaUrl) throws SAXException, Exception {
        validator.setUpMainSchema(schemaUrl, this);
        checkTestFiles(directory, State.EXPECTING_ANYTHING);
    }

    private void checkTestFiles(File directory, State state)
            throws SAXException {
        File[] files = directory.listFiles();
        List<File> validFiles = new ArrayList<File>();
        List<File> invalidFiles = new ArrayList<File>();
        List<File> hasWarningFiles = new ArrayList<File>();
        if (files == null) {
            if (verbose) {
                try {
                    out.println(String.format(
                            "\"%s\": warning: No files found in directory.",
                            directory.toURI().toURL().toString()));
                    out.flush();
                } catch (MalformedURLException mue) {
                    throw new RuntimeException(mue);
                }
            }
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                if (state != State.EXPECTING_ANYTHING) {
                    checkTestFiles(file, state);
                } else if ("invalid".equals(file.getName())) {
                    checkTestFiles(file, State.EXPECTING_INVALID_FILES);
                } else if ("valid".equals(file.getName())) {
                    checkTestFiles(file, State.EXPECTING_VALID_FILES);
                } else {
                    checkTestFiles(file, State.EXPECTING_ANYTHING);
                }
            } else if (isCheckableFile(file)) {
                if (state == State.EXPECTING_INVALID_FILES) {
                    invalidFiles.add(file);
                } else if (state == State.EXPECTING_VALID_FILES) {
                    validFiles.add(file);
                } else if (file.getPath().indexOf("novalid") > 0) {
                    invalidFiles.add(file);
                } else if (file.getPath().indexOf("haswarn") > 0) {
                    hasWarningFiles.add(file);
                } else {
                    validFiles.add(file);
                }
            }
        }
        if (validFiles.size() > 0) {
            validator.setUpValidatorAndParsers(this, false, false);
            checkFiles(validFiles);
        }
        if (invalidFiles.size() > 0) {
            validator.setUpValidatorAndParsers(this, false, false);
            checkInvalidFiles(invalidFiles);
        }
        if (hasWarningFiles.size() > 0) {
            validator.setUpValidatorAndParsers(this, false, false);
            checkHasWarningFiles(hasWarningFiles);
        }
    }

    public boolean runTestSuite(HashMap<String, String> validationMap)
            throws SAXException, Exception {
        String directory;
        String schemaUrl;
        for (Map.Entry<String, String> entry : validationMap.entrySet()) {
            directory = entry.getKey();
            schemaUrl = entry.getValue();
            checkTestDirectoryAgainstSchema(new File(directory), schemaUrl);
        }

        if (verbose) {
            if (failed) {
                out.println("Failure!");
                out.flush();
            } else {
                out.println("Success!");
                out.flush();
            }
        }
        return !failed;
    }

    private void emitMessage(SAXParseException e, String messageType) {
        String systemId = e.getSystemId();
        err.write((systemId == null) ? "" : '\"' + systemId + '\"');
        err.write(":");
        err.write(Integer.toString(e.getLineNumber()));
        err.write(":");
        err.write(Integer.toString(e.getColumnNumber()));
        err.write(": ");
        err.write(messageType);
        err.write(": ");
        err.write(e.getMessage());
        err.write("\n");
        err.flush();
    }

    public void warning(SAXParseException e) throws SAXException {
        if (emitMessages) {
            emitMessage(e, "warning");
        } else if (exception == null) {
            exception = e;
            exceptionIsWarning = true;
        }
    }

    public void error(SAXParseException e) throws SAXException {
        if (emitMessages) {
            emitMessage(e, "error");
        } else if (exception == null) {
            exception = e;
            if (e instanceof BadAttributeValueException) {
                BadAttributeValueException ex = (BadAttributeValueException) e;
                Map<String, DatatypeException> datatypeErrors = ex.getExceptions();
                for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                    DatatypeException dex = entry.getValue();
                    if (dex instanceof Html5DatatypeException) {
                        Html5DatatypeException ex5 = (Html5DatatypeException) dex;
                        if (ex5.isWarning()) {
                            exceptionIsWarning = true;
                            return;
                        }
                    }
                }
            }
        }
        inError = true;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        inError = true;
        if (emitMessages) {
            emitMessage(e, "fatal error");
            return;
        } else if (exception == null) {
            exception = e;
        }
    }

    public void reset() {
        exception = null;
        inError = false;
        emitMessages = false;
        exceptionIsWarning = false;
    }

    public static void main(String[] args) throws SAXException, Exception {
        verbose = false;
        HashMap<String, String> validationMap = null;
        System.setProperty("org.whattf.datatype.warn", "true");
        for (int i = 0; i < args.length; i++) {
            if ("--verbose".equals(args[i])) {
                verbose = true;
            } else if ("--errors-only".equals(args[i])) {
                System.setProperty("org.whattf.datatype.warn", "false");
            } else if ("--default-map".equals(args[i])) {
                validationMap = DEFAULT_VALIDATION_MAP;
            } else if (args[i].startsWith("--")) {
                System.out.println(String.format(
                        "\nError: There is no option \"%s\".", args[i]));
                usage();
                System.exit(-1);
            } else {
                if (args[i].endsWith(".json")) {
                    FileReader fr = new FileReader(args[i]);
                    validationMap = (HashMap<String, String>) JSON.parse(fr);
                } else {
                    System.out.println("\nError: You must specify a .json"
                            + " filename for validation mapping.");
                    usage();
                    System.exit(-1);
                }
            }
        }
        if (validationMap != null) {
            TestRunner tr = new TestRunner();
            if (tr.runTestSuite(validationMap)) {
                System.exit(0);
            } else {
                System.exit(-1);
            }
        } else {
            System.out.println("\nError: You must specify a .json"
                    + " filename for validation mapping.");
            usage();
            System.exit(-1);
        }
    }

    private static void usage() {
        System.out.println("\nUsage:");
        System.out.println("\n    java nu.validator.client.TestRunner [--errors-only] [--verbose] MAP.json");
        System.out.println("\n...where MAP.json is a \"validation map\" containing name/value pairs");
        System.out.println("pairs in which the name is a directory name and the value is an");
        System.out.println("http://s.validator.nu/* schema URL; for example:");
        System.out.println("\n    \"html-foo\": \"http://s.validator.nu/html5-all.rnc\"");
    }
}
