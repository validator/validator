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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import nu.validator.json.Serializer;
import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.JsonMessageEmitter;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

/**
 * Tests that messages get the same locations and extracts when the HTML parser
 * builds a tree first (the --no-stream option) as when it streams: A parser
 * message's range starts after the markup before the error, not at the start
 * of the document or at the end of the previous message. And a message about
 * an element that's reported at its end tag still gets a location.
 *
 * See: https://github.com/validator/validator/issues/2168
 */
public class NoStreamLocationTest {

    private static int passed = 0;

    private static int failed = 0;

    // A trailing slash on line 4, and a stray end tag on line 6
    private static final String HTML = "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n<title>Test</title>\n<br />\n"
            + "<p>Some text</p>\n</span>\n";

    // Misnested end tag on line 4, foster-parented text on line 5 (which
    // only a tree-building parse recovers from), and a stray end tag on line 6
    private static final String FOSTER = "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n<title>T</title>\n<p><b><i>x</b>y</i></p>\n"
            + "<table><tr><td>a</td></tr>b</table>\n</span>\n";

    // A doctype error on line 1, and a comment before a stray end tag on line 4
    private static final String COMMENT = "<!DOCTYPE html PUBLIC \"x\">\n"
            + "<html lang=\"en\">\n<title>T</title>\n<!-- note --></i>\n";

    // A <div> in a <dl> without a <dt> or <dd>, reported at its end tag on
    // line 4
    private static final String END_TAG = "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n<title>T</title>\n<dl><div></div></dl>\n";

    private static File dir;

    private static File write(String name, String content) throws Exception {
        File file = new File(dir, name);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static String check(File file, boolean noStream, boolean json)
            throws Exception {
        SimpleDocumentValidator validator = new SimpleDocumentValidator(true,
                false, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessageEmitter emitter = json
                ? new JsonMessageEmitter(new Serializer(out), null, false, "")
                : new GnuMessageEmitter(out, false);
        MessageEmitterAdapter errorHandler = new MessageEmitterAdapter(null,
                validator.getSourceCode(), false,
                new ImageCollector(validator.getSourceCode()), 0, true,
                emitter);
        errorHandler.setHtml(true);
        errorHandler.start(null);
        validator.setUpMainSchema("http://s.validator.nu/html5-all.rnc",
                new SystemErrErrorHandler());
        validator.setUpValidatorAndParsers(errorHandler, noStream, false);
        validator.checkHtmlFile(file, true);
        errorHandler.end("", "", "");
        return out.toString("UTF-8");
    }

    private static void expect(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  PASS: " + name);
        } else {
            failed++;
            System.out.println("  FAIL: " + name + " (" + detail + ")");
        }
    }

    private static void expectContains(String name, String output,
            String file, String lines) {
        String expected = file + "\":" + lines + ": ";
        expect(name, output.contains(expected),
                "expected " + expected + " in:\n" + output);
    }

    private static void expectSame(String name, File file, boolean json)
            throws Exception {
        String streamed = check(file, false, json);
        String buffered = check(file, true, json);
        expect(name, streamed.equals(buffered), "streamed:\n" + streamed
                + "\nwith --no-stream:\n" + buffered);
    }

    public static void main(String[] args) throws Exception {
        dir = Files.createTempDirectory("no-stream-location-test").toFile();
        File html = write("test.html", HTML);
        File foster = write("foster.html", FOSTER);
        File comment = write("comment.html", COMMENT);
        File endTag = write("end-tag.html", END_TAG);

        System.out.println("Testing --no-stream ranges...");
        String gnu = check(html, true, false);
        expectContains("trailing slash", gnu, "test.html", "4.1-4.6");
        expectContains("stray end tag", gnu, "test.html", "6.1-6.7");
        String fostered = check(foster, true, false);
        expectContains("misnested end tag", fostered, "foster.html",
                "4.11-4.14");
        expectContains("foster-parented text", fostered, "foster.html",
                "5.27-5.35");
        expectContains("stray end tag after a table", fostered,
                "foster.html", "6.1-6.7");
        String commented = check(comment, true, false);
        expectContains("stray end tag after a comment", commented,
                "comment.html", "4.14-4.17");
        expectContains("message at an end tag", check(endTag, true, false),
                "end-tag.html", "4.10-4.15");

        System.out.println();
        System.out.println("Testing that --no-stream matches streaming...");
        expectSame("GNU output", html, false);
        expectSame("JSON output, with extracts", html, true);
        expectSame("GNU output, comment and doctype", comment, false);
        expectSame("JSON output, comment and doctype", comment, true);
        expectSame("GNU output, message at an end tag", endTag, false);

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
