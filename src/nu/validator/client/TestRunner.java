/*
 * Copyright (c) 2013-2017 Mozilla Foundation
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonWriter;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nu.validator.vendor.relaxng.datatype.DatatypeException;

import nu.validator.vendor.thaiopensource.relaxng.exceptions.BadAttributeValueException;

import nu.validator.datatype.Html5DatatypeException;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.validation.SimpleDocumentValidator;

public class TestRunner extends MessageEmitterAdapter {

    private static final String DEFAULT_SCHEMA = "http://s.validator.nu/html5-all.rnc";

    private static final String RDFALITE_SCHEMA = "http://s.validator.nu/html5-rdfalite.rnc";

    private static final String XHTML_SCHEMA = "http://s.validator.nu/xhtml5-all.rnc";

    private boolean inError = false;

    private boolean emitMessages = false;

    private boolean exceptionIsWarning = false;

    private boolean expectingError = false;

    private Exception exception = null;

    private String infoMessage = null;

    private SimpleDocumentValidator validator;

    private PrintWriter err;

    private PrintWriter out;

    private boolean failed = false;

    private File messagesFile;

    private String[] ignoreList = null;

    private boolean writeMessages;

    private boolean verbose;

    private boolean hasUnhandledWarning = false;

    private boolean hasUnhandledInfo = false;

    private File baseDir = null;

    private Map<String, javax.json.JsonValue> expectedMessages;

    private JsonObjectBuilder reportedMessages;

    public TestRunner() throws IOException {
        reportedMessages = Json.createObjectBuilder();
        validator = new SimpleDocumentValidator(true, false, true);
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

    public void setMessagesFile(File file) {
        this.messagesFile = file;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setWriteMessages(boolean writeMessages) {
        this.writeMessages = writeMessages;
    }

    public boolean isWriteMessages() {
        return this.writeMessages;
    }

    public void setIgnoreList(String[] ignoreList) {
        this.ignoreList = ignoreList;
    }

    private URL getFileURL(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }

    private String getRelativePathname(File file, File baseDir) throws MalformedURLException {
        String filePath = this.getFileURL(file).getPath();

        // Note: baseDirPath endswith "/"
        //
        // https://docs.oracle.com/javase/8/docs/api/java/io/File.html#toURI--
        // > If it can be determined that the file denoted by this abstract
        // > pathname is a directory, then the resulting URI will end with
        // > a slash.
        String baseDirPath = this.getFileURL(baseDir).getPath();

        return filePath.substring(baseDirPath.length());
    }

    private void checkHtmlFile(File file) throws IOException, SAXException {
        if (file.getName().contains("missing-lang")) {
            System.setProperty("nu.validator.checker.ignoreMissingLang", "false");
        }
        if (!file.exists()) {
            if (verbose) {
                out.println(String.format("\"%s\": warning: File not found.",
                        this.getFileURL(file)));
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
                        this.getFileURL(file)));
                out.flush();
            }
        }
        System.setProperty("nu.validator.checker.ignoreMissingLang", "true");
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
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                recurseDirectory(file);
            } else {
                checkHtmlFile(file);
            }
        }
    }

    private boolean isIgnorable(File file) throws IOException {
        String testPathname = this.getRelativePathname(file, baseDir);
        if (ignoreList != null) {
            for (String substring : ignoreList) {
                if (testPathname.contains(substring)) {
                    if (verbose) {
                        out.println(String.format(
                                "\"%s\": warning: File ignored.",
                                this.getFileURL(file)));
                        out.flush();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void checkFiles(List<File> files) throws IOException {
        for (File file : files) {
            if (isIgnorable(file)) {
                continue;
            }
            reset();
            emitMessages = true;
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException | SAXException e) {
                if (verbose) {
                    out.println(String.format(
                            "\"%s\": error: Exception while processing file: %s",
                            this.getFileURL(file), e.getMessage()));
                    e.printStackTrace(out);
                    out.flush();
                }
                failed = true;
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
        String messageExpected = ((JsonString) expectedMessages.get(testFilename)).getString().replaceAll(
                "\\p{C}", "?");
        // NOTE: The string replacements below are a hack to "normalize"
        // error messages reported for bad values of the ins/del datetime
        // attribute, to work around the fact that in Java 8, parts of
        // those error messages don't always get emitted in the same order
        // that they do in Java 7 and earlier.
        Pattern p;
        p = Pattern.compile("(Bad datetime with timezone: .+) (Bad date: .+)");
        messageExpected = p.matcher(messageExpected).replaceAll("$2 $1");
        messageReported = p.matcher(messageReported).replaceAll("$2 $1");
        return messageReported.equals(messageExpected);
    }

    private void checkInvalidFiles(List<File> files) throws IOException {
        String testFilename;
        expectingError = true;
        for (File file : files) {
            if (isIgnorable(file)) {
                continue;
            }
            reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException | SAXException e) {
                err.println(String.format(
                        "\"%s\": error: Exception while processing file: %s",
                        file.getPath(),
                        e.getMessage()));
                err.flush();
            }
            if (exception != null) {
                testFilename = this.getRelativePathname(file, baseDir);
                if (writeMessages) {
                    reportedMessages.add(testFilename, exception.getMessage());
                } else if (expectedMessages != null
                        && expectedMessages.get(testFilename) == null) {
                    try {
                        err.println(String.format(
                                "\"%s\": warning: No expected message in"
                                        + " messages file.",
                                this.getFileURL(file)));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (expectedMessages != null
                        && !messageMatches(testFilename)) {
                    failed = true;
                    try {
                        err.println(String.format(
                                "\"%s\": error: Expected \"%s\""
                                        + " but instead encountered \"%s\".",
                                this.getFileURL(file),
                                expectedMessages.get(testFilename),
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
                            this.getFileURL(file)));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkHasWarningFiles(List<File> files) throws IOException {
        String testFilename;
        expectingError = false;
        for (File file : files) {
            if (isIgnorable(file)) {
                continue;
            }
            reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException | SAXException e) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Exception while checking file: %s",
                            this.getFileURL(file),
                            e.getMessage()));
                    err.flush();
                } catch (MalformedURLException e1) {
                    throw new RuntimeException(e1);
                }
            }
            if (exception != null) {
                testFilename = this.getRelativePathname(file, baseDir);
                if (writeMessages) {
                    reportedMessages.add(testFilename, exception.getMessage());
                } else if (expectedMessages != null
                        && expectedMessages.get(testFilename) == null) {
                    try {
                        err.println(String.format(
                                "\"%s\": warning: No expected message in"
                                        + " messages file.",
                                this.getFileURL(file)));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (expectedMessages != null
                        && !messageMatches(testFilename)) {
                    try {
                        err.println(String.format(
                                "\"%s\": error: Expected \"%s\""
                                        + " but instead encountered \"%s\".",
                                this.getFileURL(file),
                                expectedMessages.get(testFilename),
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
                            this.getFileURL(file)));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (!exceptionIsWarning) {
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected a warning but did not"
                                    + " encounter any.",
                            this.getFileURL(file)));
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
                            this.getFileURL(file)));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkHasInfoFiles(List<File> files) throws IOException {
        String testFilename;
        expectingError = false;
        for (File file : files) {
            if (isIgnorable(file)) {
                continue;
            }
            reset();
            hasUnhandledInfo = false;
            infoMessage = null;
            emitMessages = true;
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException | SAXException e) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Exception while checking file: %s",
                            this.getFileURL(file),
                            e.getMessage()));
                    err.flush();
                } catch (MalformedURLException e1) {
                    throw new RuntimeException(e1);
                }
            }
            if (infoMessage != null) {
                testFilename = this.getRelativePathname(file, baseDir);
                if (writeMessages) {
                    reportedMessages.add(testFilename, infoMessage);
                } else if (expectedMessages != null
                        && expectedMessages.get(testFilename) == null) {
                    try {
                        err.println(String.format(
                                "\"%s\": info: No expected message in"
                                        + " messages file.",
                                this.getFileURL(file)));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (expectedMessages != null
                        && !infoMessage.equals(((javax.json.JsonString) expectedMessages.get(testFilename)).getString())) {
                    try {
                        err.println(String.format(
                                "\"%s\": error: Expected \"%s\""
                                        + " but instead encountered \"%s\".",
                                this.getFileURL(file),
                                expectedMessages.get(testFilename),
                                infoMessage));
                        err.flush();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Message matched expectations - clear the unhandled flag
                    hasUnhandledInfo = false;
                }
            } else if (!hasUnhandledInfo) {
                try {
                    err.println(String.format(
                            "\"%s\": error: Expected an info message but did not"
                                    + " encounter any.",
                            this.getFileURL(file)));
                    err.flush();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                failed = true;
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
        List<File> validFiles = new ArrayList<>();
        List<File> invalidFiles = new ArrayList<>();
        List<File> hasWarningFiles = new ArrayList<>();
        List<File> hasInfoFiles = new ArrayList<>();
        if (files == null) {
            if (verbose) {
                try {
                    out.println(String.format(
                            "\"%s\": warning: No files found in directory.",
                            this.getFileURL(directory)));
                    out.flush();
                } catch (MalformedURLException mue) {
                    throw new RuntimeException(mue);
                }
            }
            return;
        }
        for (File file : files) {
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
                } else if (file.getPath().indexOf("novalid") >= 0) {
                    invalidFiles.add(file);
                } else if (file.getPath().indexOf("haswarn") >= 0) {
                    hasWarningFiles.add(file);
                } else if (file.getPath().indexOf("hasinfo") >= 0) {
                    hasInfoFiles.add(file);
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
        if (hasInfoFiles.size() > 0) {
            validator.setUpValidatorAndParsers(this, false, false);
            checkHasInfoFiles(hasInfoFiles);
        }
    }

    public boolean runTestSuite() throws SAXException, Exception {
        if (messagesFile != null && !writeMessages) {
            baseDir = messagesFile.getCanonicalFile().getParentFile();
            try (FileInputStream fis = new FileInputStream(messagesFile);
                 JsonReader reader = Json.createReader(fis)) {
                javax.json.JsonObject jsonObject = reader.readObject();
                final Map<String, javax.json.JsonValue> expectedMessagesMap = new HashMap<>();
                for (Map.Entry<String, javax.json.JsonValue> entry : jsonObject.entrySet()) {
                    expectedMessagesMap.put(entry.getKey(), entry.getValue());
                }
                expectedMessages = expectedMessagesMap;
            }
        } else if (messagesFile != null) {
            baseDir = messagesFile.getCanonicalFile().getParentFile();
        } else {
            baseDir = new File(System.getProperty("user.dir"));
        }
        File[] directories = baseDir.listFiles();
        if (directories != null) {
            for (File directory : directories) {
                if (directory.isDirectory()) {
                    if (directory.getName().contains("rdfalite")) {
                        checkTestDirectoryAgainstSchema(directory, RDFALITE_SCHEMA);
                    } else if (directory.getName().contains("xhtml")) {
                        checkTestDirectoryAgainstSchema(directory, XHTML_SCHEMA);
                    } else {
                        checkTestDirectoryAgainstSchema(directory, DEFAULT_SCHEMA);
                    }
                }
            }
        }
        if (writeMessages) {
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(messagesFile), "utf-8");
                 BufferedWriter bw = new BufferedWriter(out)) {
                JsonWriter jsonWriter = Json.createWriter(bw);
                jsonWriter.writeObject(reportedMessages.build());
                jsonWriter.close();
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
        err.write((systemId != null) ? '\"' + systemId + '\"' : "");
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

    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (DEFAULT_FILTER_PATTERN.matcher(e.getMessage()).matches()) {
            return;
        }
        if (emitMessages) {
            hasUnhandledWarning = true;
            emitMessage(e, "warning");
        } else if (exception == null && !expectingError) {
            exception = e;
            exceptionIsWarning = true;
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (DEFAULT_FILTER_PATTERN.matcher(e.getMessage()).matches()) {
            return;
        }
        if (e.getMessage() != null && e.getMessage().contains("Typo for")) {
            if (emitMessages) {
                if (infoMessage == null) {
                    infoMessage = e.getMessage();
                }
                // Don't call emitMessage; let checkHasInfoFiles handle
                // printing on mismatch.
            }
            // Don't set hasUnhandledInfo; typo info messages are expected.
            return;
        }
        if (emitMessages) {
            emitMessage(e, "error");
        } else if (exception == null) {
            exception = e;
            if (e instanceof BadAttributeValueException) {
                BadAttributeValueException ex = (BadAttributeValueException) e;
                Map<String, DatatypeException> datatypeErrors = ex.getExceptions();
                for (Map.Entry<String, DatatypeException> entry :
                        datatypeErrors.entrySet()) {
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

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        inError = true;
        if (emitMessages) {
            emitMessage(e, "fatal error");
            return;
        } else if (exception == null) {
            exception = e;
        }
    }

    @Override
    public void info(String message) throws SAXException {
        if (emitMessages) {
            hasUnhandledInfo = true;
            err.println("info: " + message);
            err.flush();
        }
    }

    public void reset() {
        exception = null;
        inError = false;
        emitMessages = false;
        exceptionIsWarning = false;
    }

    public static void main(String[] args) throws SAXException, Exception {
        if (args.length < 1) {
            usage();
            System.exit(0);
        }
        String messagesFilename = null;
        System.setProperty("nu.validator.checker.ignoreMissingLang", "true");
        System.setProperty("nu.validator.datatype.warn", "true");
        for (String arg : args) {
            if ("--verbose".equals(arg)) {
            } else if ("--errors-only".equals(arg)) {
                System.setProperty("nu.validator.datatype.warn", "false");
            } else if ("--write-messages".equals(arg)) {
            } else if (arg.startsWith("--ignore=")) {
            } else if (arg.startsWith("--")) {
                System.out.println(String.format(
                        "\nError: There is no option \"%s\".", arg));
                usage();
                System.exit(1);
            } else {
                if (arg.endsWith(".json")) {
                    messagesFilename = arg;
                } else {
                    System.out.println("\nError: Expected the name of a messages"
                            + " file with a .json extension.");
                    usage();
                    System.exit(1);
                }
            }
        }
        File messagesFileToUse = null;
        boolean writeMessagesFlag = parseWriteMessagesFlag(args);
        if (messagesFilename != null) {
            messagesFileToUse = new File(messagesFilename);
            if (!writeMessagesFlag) {
                if (!messagesFileToUse.exists()) {
                    System.out.println("\nError: \"" + messagesFilename
                            + "\" file not found.");
                    System.exit(1);
                } else if (!messagesFileToUse.isFile()) {
                    System.out.println("\nError: \"" + messagesFilename
                            + "\" is not a file.");
                    System.exit(1);
                }
            }
        }
        TestRunner tr = new TestRunner();
        tr.setMessagesFile(messagesFileToUse);
        tr.setVerbose(parseVerboseFlag(args));
        tr.setWriteMessages(writeMessagesFlag);
        tr.setIgnoreList(parseIgnoreList(args));
        if (messagesFileToUse == null && tr.isWriteMessages()) {
            System.out.println("\nError: Expected the name of a messages"
                    + " file with a .json extension.");
            usage();
            System.exit(1);
        }
        if (tr.runTestSuite()) {
            if (tr.hasUnhandledWarning) {
                System.exit(1);
            }
            if (!tr.isWriteMessages() && tr.hasUnhandledInfo) {
                System.exit(1);
            }
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    private static boolean parseVerboseFlag(String[] args) {
        for (String arg : args) {
            if ("--verbose".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static boolean parseWriteMessagesFlag(String[] args) {
        for (String arg : args) {
            if ("--write-messages".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static String[] parseIgnoreList(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--ignore=")) {
                return arg.substring(9, arg.length()).split(",");
            }
        }
        return null;
    }

    private static void usage() {
        System.out.println("\nUsage:");
        System.out.println("\n    java nu.validator.client.TestRunner [--errors-only] [--write-messages]");
        System.out.println("          [--verbose] [MESSAGES.json]");
        System.out.println("\n...where the MESSAGES.json file contains name/value pairs in which the name is");
        System.out.println("a pathname of a document to check and the value is the first error message or");
        System.out.println("warning message the validator is expected to report when checking that document.");
        System.out.println("Use the --write-messages option to create the file.");
    }
}
