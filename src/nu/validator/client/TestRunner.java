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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private String schema = "http://s.validator.nu/html5-all.rnc";

    private boolean failed = false;

    private static boolean writeMessages;

    private static boolean verbose;

    private String baseDir = null;

    private Map<String, String> messages;

    public TestRunner() throws IOException {
        messages = new LinkedHashMap<String, String>();
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

    private boolean messageMatches(String testFilename) {
        // p{C} = Other = Control+Format+Private_Use+Surrogate+Unassigned
        // http://www.regular-expressions.info/unicode.html#category
        // http://www.unicode.org/reports/tr18/#General_Category_Property
        String messageReported = exception.getMessage().replaceAll("\\p{C}",
                "?");
        String messageExpected = messages.get(testFilename).replaceAll(
                "\\p{C}", "?");
        return messageReported.equals(messageExpected);
    }

    private void checkInvalidFiles(List<File> files) throws SAXException {
        String testFilename;
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
            if (exception != null) {
                testFilename = file.getAbsolutePath().substring(
                        baseDir.length() + 1);
                if (writeMessages) {
                    messages.put(testFilename, exception.getMessage());
                } else if (messages.get(testFilename) == null) {
                    try {
                        err.println(String.format(
                                "\"%s\": warning: No expected message in"
                                        + " messages file.",
                                file.toURI().toURL().toString()));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (!messageMatches(testFilename)) {
                    try {
                        err.println(String.format(
                                "\"%s\": error: Expected \"%s\""
                                        + " but instead encountered \"%s\".",
                                file.toURI().toURL().toString(),
                                messages.get(testFilename),
                                exception.getMessage()));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
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
        String testFilename;
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
            if (exception != null) {
                testFilename = file.getAbsolutePath().substring(
                        baseDir.length() + 1);
                if (writeMessages) {
                    messages.put(testFilename, exception.getMessage());
                } else if (messages.get(testFilename) == null) {
                    try {
                        err.println(String.format(
                                "\"%s\": warning: No expected message in"
                                        + " messages file.",
                                file.toURI().toURL().toString()));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (!messageMatches(testFilename)) {
                    try {
                        err.println(String.format(
                                "\"%s\": error: Expected \"%s\""
                                        + " but instead encountered \"%s\".",
                                file.toURI().toURL().toString(),
                                messages.get(testFilename),
                                exception.getMessage()));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
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
            throws SAXException, IOException {
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
        if (writeMessages) {
            File messagesFile = new File(baseDir + "/messages.json");
            FileWriter fw = new FileWriter(messagesFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(JSON.toString(messages));
            bw.close();
        }
    }

    public boolean runTestSuite(File messagesFile) throws SAXException,
            Exception {
        if (messagesFile != null) {
            baseDir = messagesFile.getAbsoluteFile().getParent();
            FileReader fr = new FileReader(messagesFile);
            messages = (HashMap<String, String>) JSON.parse(fr);
        } else {
            baseDir = System.getProperty("user.dir");
        }
        for (File directory : new File(baseDir).listFiles()) {
            if (directory.isDirectory()) {
                if (directory.getName().contains("rdfalite")) {
                    checkTestDirectoryAgainstSchema(directory,
                            "http://s.validator.nu/html5-rdfalite.rnc");
                } else if (directory.getName().contains("xhtml")) {
                    checkTestDirectoryAgainstSchema(directory,
                            "http://s.validator.nu/xhtml5-all.rnc");
                } else {
                    checkTestDirectoryAgainstSchema(directory, schema);
                }
            }
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
        String messagesFilename = null;
        System.setProperty("org.whattf.datatype.warn", "true");
        for (int i = 0; i < args.length; i++) {
            if ("--verbose".equals(args[i])) {
                verbose = true;
            } else if ("--errors-only".equals(args[i])) {
                System.setProperty("org.whattf.datatype.warn", "false");
            } else if ("--write-messages".equals(args[i])) {
                writeMessages = true;
            } else if (args[i].startsWith("--")) {
                System.out.println(String.format(
                        "\nError: There is no option \"%s\".", args[i]));
                usage();
                System.exit(-1);
            } else {
                if (args[i].endsWith(".json")) {
                    messagesFilename = args[i];
                } else {
                    System.out.println("\nError: You must specify a .json"
                            + " filename for validation mapping.");
                    usage();
                    System.exit(-1);
                }
            }
        }
        if (messagesFilename != null) {
            File messagesFile = new File(messagesFilename);
            if (!messagesFile.exists()) {
                System.out.println("\nError: \"" + messagesFilename
                        + "\" file not found.");
                System.exit(-1);
            } else if (!messagesFile.isFile()) {
                System.out.println("\nError: \"" + messagesFilename
                        + "\" is not a file.");
                System.exit(-1);
            } else {
                TestRunner tr = new TestRunner();
                if (tr.runTestSuite(messagesFile)) {
                    System.exit(0);
                } else {
                    System.exit(-1);
                }
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
        System.out.println("\n    java nu.validator.client.TestRunner [--errors-only] [--verbose] [MANIFEST.json]");
        System.out.println("\n...where MANIFEST.json contains name/value pairs in which the name is a");
        System.out.println(" pathname of a file to check and the value is the first error message or");
        System.out.println(" warning message the validator is expected to report when checking that file.");
        System.out.println("\n    \"html-foo\": \"http://s.validator.nu/html5-all.rnc\"");
    }
}
