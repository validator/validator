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

package nu.validator.source.test;

import nu.validator.source.SourceCode;
import nu.validator.source.SourceHandler;
import nu.validator.xml.TypedInputSource;

import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.SortedSet;

/**
 * Unit tests for SourceCode.
 *
 * Tests that out-of-bounds line access is handled gracefully without
 * throwing IndexOutOfBoundsException.
 */
public class SourceCodeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws SAXException {
        testIsWithinKnownSourceValidLocations();
        testIsWithinKnownSourceOutOfBounds();
        testEmitSourceDoesNotThrow();
        testEmitSourceWithErrors();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static SourceCode createSourceCode(String content) throws SAXException {
        SourceCode sourceCode = new SourceCode();
        TypedInputSource inputSource = new TypedInputSource(
                new StringReader(content));
        inputSource.setSystemId("test://test.html");
        inputSource.setType("text/html");
        sourceCode.initialize(inputSource);
        sourceCode.start();

        char[] chars = content.toCharArray();
        sourceCode.characters(chars, 0, chars.length);
        sourceCode.end();

        return sourceCode;
    }

    private static void testIsWithinKnownSourceValidLocations() throws SAXException {
        SourceCode sourceCode = createSourceCode("Line 1\nLine 2\nLine 3");

        assertTrue("isWithinKnownSource for line 1 (one-based)",
                sourceCode.isWithinKnownSource(1));
        assertTrue("isWithinKnownSource for line 2 (one-based)",
                sourceCode.isWithinKnownSource(2));
        assertTrue("isWithinKnownSource for line 3 (one-based)",
                sourceCode.isWithinKnownSource(3));
    }

    private static void testIsWithinKnownSourceOutOfBounds() throws SAXException {
        SourceCode sourceCode = createSourceCode("Line 1\nLine 2\nLine 3");

        // Line 4 is out of bounds (only 3 lines)
        assertFalse("isWithinKnownSource returns false for line 4",
                sourceCode.isWithinKnownSource(4));

        // Line 100 is way out of bounds
        assertFalse("isWithinKnownSource returns false for line 100",
                sourceCode.isWithinKnownSource(100));

        // Test Location-based method with out-of-bounds location
        assertFalse("isWithinKnownSource(Location) returns false for out-of-bounds",
                sourceCode.isWithinKnownSource(
                        sourceCode.newLocatorLocation(100, 1)));
    }

    private static void testEmitSourceDoesNotThrow() throws SAXException {
        SourceCode sourceCode = createSourceCode("Short");

        try {
            sourceCode.emitSource(createNoOpHandler());
            pass("emitSource completes without throwing");
        } catch (IndexOutOfBoundsException e) {
            fail("emitSource threw IndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private static void testEmitSourceWithErrors() throws SAXException {
        SourceCode sourceCode = createSourceCode("Line 1\nLine 2");

        // Add an error location at the end of the content
        sourceCode.addLocatorLocation(2, 7);
        sourceCode.rememberExactError(sourceCode.newLocatorLocation(2, 6));

        try {
            sourceCode.emitSource(createNoOpHandler());
            pass("emitSource with errors completes without throwing");
        } catch (IndexOutOfBoundsException e) {
            fail("emitSource with errors threw IndexOutOfBoundsException: "
                    + e.getMessage());
        }
    }

    private static SourceHandler createNoOpHandler() {
        return new SourceHandler() {
            @Override public void startSource(String type, String encoding)
                    throws SAXException {}
            @Override public void endSource() throws SAXException {}
            @Override public void characters(char[] ch, int start, int length)
                    throws SAXException {}
            @Override public void newLine() throws SAXException {}
            @Override public void startCharHilite(int line, int col)
                    throws SAXException {}
            @Override public void endCharHilite() throws SAXException {}
            @Override public void startRange(int line, int col)
                    throws SAXException {}
            @Override public void endRange() throws SAXException {}
            @Override public void setLineErrors(SortedSet<Integer> errors) {}
        };
    }

    // Helper methods

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName);
        }
    }

    private static void assertFalse(String testName, boolean condition) {
        assertTrue(testName, !condition);
    }

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }

    private static void fail(String testName) {
        System.out.println("FAIL: " + testName);
        failed++;
    }
}
