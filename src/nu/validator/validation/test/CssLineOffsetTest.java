/*
 * Copyright (c) 2026 Mozilla Foundation
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

package nu.validator.validation.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

/**
 * Tests that the line numbers in messages are right whatever kinds of
 * documents get checked in the same run: A CSS document gets checked inside
 * an HTML wrapper, and its messages are shifted up a line to make up for the
 * wrapper's first line, but no other document's messages are.
 *
 * See: https://github.com/validator/validator/issues/2166
 */
public class CssLineOffsetTest {

    private static int passed = 0;

    private static int failed = 0;

    // A stray end tag on line 4
    private static final String HTML = "<!doctype html>\n"
            + "<html lang=\"en\">\n<title>Test</title>\n</b>\n";

    // An unknown property on line 2
    private static final String CSS = "p { color: red; }\nb { colr: red; }\n";

    // An unknown element on line 3
    private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\">\n"
            + "<rect/>\n<foo/>\n</svg>\n";

    private static File dir;

    private static File write(String name, String content) throws Exception {
        File file = new File(dir, name);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * Checks the files in order in one run, the way the command-line checker
     * does, and returns the messages in GNU format.
     */
    private static String check(int lineOffset, File... files)
            throws Exception {
        SimpleDocumentValidator validator = new SimpleDocumentValidator(true,
                false, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessageEmitterAdapter errorHandler = new MessageEmitterAdapter(null,
                validator.getSourceCode(), false,
                new ImageCollector(validator.getSourceCode()), lineOffset,
                true, new GnuMessageEmitter(out, false));
        errorHandler.setHtml(true);
        errorHandler.start(null);
        validator.setAllowCss(true);
        validator.setUpMainSchema("http://s.validator.nu/html5-all.rnc",
                new SystemErrErrorHandler());
        validator.setUpValidatorAndParsers(errorHandler, false, false);
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(".css")) {
                validator.checkCssFile(file, true);
            } else if (name.endsWith(".svg")) {
                validator.checkXmlFile(file);
            } else {
                validator.checkHtmlFile(file, true);
            }
        }
        errorHandler.end("", "", "");
        return out.toString("UTF-8");
    }

    /**
     * Runs the command-line checker on the files, with the given options, and
     * returns its messages in GNU format.
     */
    private static String runCommandLine(String[] options, File... files)
            throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                new File(System.getProperty("java.home"), "bin/java").getPath(),
                "-cp", System.getProperty("java.class.path"),
                "nu.validator.client.SimpleCommandLineValidator",
                "--format", "gnu"));
        command.addAll(Arrays.asList(options));
        for (File file : files) {
            command.add(file.getPath());
        }
        Process process = new ProcessBuilder(command).redirectErrorStream(
                true).start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = process.getInputStream()) {
            in.transferTo(out);
        }
        process.waitFor();
        return out.toString("UTF-8");
    }

    private static void expect(String name, String output, String file,
            String lines) {
        String expected = file + "\":" + lines + ": error:";
        if (output.contains(expected)) {
            passed++;
            System.out.println("  PASS: " + name);
        } else {
            failed++;
            System.out.println("  FAIL: " + name + " (expected " + expected
                    + " in:\n" + output + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        dir = Files.createTempDirectory("css-line-offset-test").toFile();
        File html = write("test.html", HTML);
        File css = write("test.css", CSS);
        File svg = write("test.svg", SVG);

        System.out.println("Testing line numbers by document kind...");
        expect("HTML alone", check(0, html), "test.html", "4.1-4.4");
        expect("CSS alone", check(0, css), "test.css", "2.11-2.13");
        expect("SVG alone", check(0, svg), "test.svg", "3.1-3.6");

        System.out.println();
        System.out.println("Testing line numbers after a CSS document...");
        String mixed = check(0, css, html, svg, css);
        expect("HTML after CSS", mixed, "test.html", "4.1-4.4");
        expect("SVG after HTML after CSS", mixed, "test.svg", "3.1-3.6");
        expect("CSS after SVG", mixed, "test.css", "2.11-2.13");

        System.out.println();
        System.out.println("Testing a configured line offset...");
        String offset = check(10, css, html);
        expect("CSS with offset 10", offset, "test.css", "12.11-12.13");
        expect("HTML with offset 10", offset, "test.html", "14.1-14.4");

        System.out.println();
        System.out.println("Testing the command-line checker...");
        String alsoCss = runCommandLine(new String[] { "--also-check-css" },
                css, html);
        expect("--also-check-css: CSS", alsoCss, "test.css", "2.11-2.13");
        expect("--also-check-css: HTML", alsoCss, "test.html", "4.1-4.4");
        expect("--skip-non-css: CSS",
                runCommandLine(new String[] { "--skip-non-css" }, css),
                "test.css", "2.11-2.13");
        expect("--also-check-css --also-check-svg: SVG",
                runCommandLine(new String[] { "--also-check-css",
                        "--also-check-svg" }, svg),
                "test.svg", "3.1-3.6");

        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();

        System.out.println();
        System.out.println("Results: " + passed + " passed, "
                + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
