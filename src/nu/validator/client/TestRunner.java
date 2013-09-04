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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

import org.xml.sax.SAXException;

import com.thaiopensource.xml.sax.CountingErrorHandler;

public class TestRunner {

    private SimpleDocumentValidator validator;

    private static final String PATH = "syntax/relaxng/tests/";

    private PrintWriter err;

    private PrintWriter out;

    private SystemErrErrorHandler errorHandler;

    private CountingErrorHandler countingErrorHandler;

    private boolean failed = false;

    private boolean verbose;

    /**
     * @param basePath
     */
    public TestRunner(boolean verbose) throws IOException {
        this.errorHandler = new SystemErrErrorHandler();
        this.countingErrorHandler = new CountingErrorHandler();
        this.verbose = verbose;
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
            errorHandler.reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException e) {
            } catch (SAXException e) {
            }
            if (errorHandler.isInError()) {
                failed = true;
            }
        }
    }

    private void checkInvalidFiles(List<File> files) {
        for (File file : files) {
            countingErrorHandler.reset();
            try {
                if (file.isDirectory()) {
                    recurseDirectory(file);
                } else {
                    checkHtmlFile(file);
                }
            } catch (IOException e) {
            } catch (SAXException e) {
            }
            if (!countingErrorHandler.getHadErrorOrFatalError()) {
                failed = true;
                try {
                    err.println(String.format(
                            "\"%s\": error: Document was supposed to be"
                                    + " invalid but was not.",
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
        validator.setUpMainSchema(schemaUrl, errorHandler);
        checkTestFiles(directory, State.EXPECTING_ANYTHING);
    }

    private void checkTestFiles(File directory, State state)
            throws SAXException {
        File[] files = directory.listFiles();
        List<File> validFiles = new ArrayList<File>();
        List<File> invalidFiles = new ArrayList<File>();
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
                } else {
                    validFiles.add(file);
                }
            }
        }
        if (validFiles.size() > 0) {
            validator.setUpValidatorAndParsers(errorHandler, false, false);
            checkFiles(validFiles);
        }
        if (invalidFiles.size() > 0) {
            validator.setUpValidatorAndParsers(countingErrorHandler, false,
                    false);
            checkInvalidFiles(invalidFiles);
        }
    }

    public boolean runTestSuite() throws SAXException, Exception {
        checkTestDirectoryAgainstSchema(new File(PATH
                + "html5core-plus-web-forms2/"),
                "http://s.validator.nu/html5/xhtml5core-plus-web-forms2.rnc");
        checkTestDirectoryAgainstSchema(new File(PATH + "html/"),
                "http://s.validator.nu/html5-all.rnc");
        checkTestDirectoryAgainstSchema(new File(PATH + "xhtml/"),
                "http://s.validator.nu/xhtml5-all.rnc");
        checkTestDirectoryAgainstSchema(new File(PATH + "html-its/"),
                "http://s.validator.nu/html5-all.rnc");
        checkTestDirectoryAgainstSchema(new File(PATH + "html-rdfa/"),
                "http://s.validator.nu/html5-all.rnc");
        checkTestDirectoryAgainstSchema(new File(PATH + "html-rdfalite/"),
                "http://s.validator.nu/html5-rdfalite.rnc");

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

    /**
     * @param args
     * @throws SAXException
     */
    public static void main(String[] args) throws SAXException, Exception {
        boolean verbose = ((args.length == 1) && "-v".equals(args[0]));
        TestRunner tr = new TestRunner(verbose);
        if (tr.runTestSuite()) {
            System.exit(0);
        } else {
            System.exit(-1);
        }
    }
}
