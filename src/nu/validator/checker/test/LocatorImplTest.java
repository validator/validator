/*
 * Copyright (c) 2025 Mozilla Foundation
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

package nu.validator.checker.test;

import org.xml.sax.Locator;

import nu.validator.checker.LocatorImpl;
import nu.validator.checker.TaintableLocatorImpl;

/**
 * Unit tests for LocatorImpl and TaintableLocatorImpl.
 *
 * These classes provide immutable wrappers around SAX Locator interface.
 */
public class LocatorImplTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing LocatorImpl...");
        testLocatorImplBasic();
        testLocatorImplWithNulls();

        System.out.println();
        System.out.println("Testing TaintableLocatorImpl...");
        testTaintableLocatorImplBasic();
        testTaintableLocatorImplTaintBehavior();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // LocatorImpl tests

    private static void testLocatorImplBasic() {
        MockLocator source = new MockLocator("file:///test.html", "public-id", 10, 25);
        LocatorImpl locator = new LocatorImpl(source);

        assertEquals("LocatorImpl: getSystemId", "file:///test.html", locator.getSystemId());
        assertEquals("LocatorImpl: getPublicId", "public-id", locator.getPublicId());
        assertEquals("LocatorImpl: getLineNumber", 10, locator.getLineNumber());
        assertEquals("LocatorImpl: getColumnNumber", 25, locator.getColumnNumber());
    }

    private static void testLocatorImplWithNulls() {
        MockLocator source = new MockLocator(null, null, -1, -1);
        LocatorImpl locator = new LocatorImpl(source);

        assertNull("LocatorImpl: null systemId", locator.getSystemId());
        assertNull("LocatorImpl: null publicId", locator.getPublicId());
        assertEquals("LocatorImpl: line -1", -1, locator.getLineNumber());
        assertEquals("LocatorImpl: column -1", -1, locator.getColumnNumber());
    }

    // TaintableLocatorImpl tests

    private static void testTaintableLocatorImplBasic() {
        MockLocator source = new MockLocator("file:///test.html", "public-id", 5, 15);
        TaintableLocatorImpl locator = new TaintableLocatorImpl(source);

        assertEquals("TaintableLocatorImpl: getSystemId", "file:///test.html",
                locator.getSystemId());
        assertEquals("TaintableLocatorImpl: getPublicId", "public-id",
                locator.getPublicId());
        assertEquals("TaintableLocatorImpl: getLineNumber", 5, locator.getLineNumber());
        assertEquals("TaintableLocatorImpl: getColumnNumber", 15, locator.getColumnNumber());
    }

    private static void testTaintableLocatorImplTaintBehavior() {
        MockLocator source = new MockLocator("file:///test.html", null, 1, 1);
        TaintableLocatorImpl locator = new TaintableLocatorImpl(source);

        assertFalse("TaintableLocatorImpl: initially not tainted", locator.isTainted());

        locator.markTainted();
        assertTrue("TaintableLocatorImpl: tainted after markTainted()", locator.isTainted());

        // Calling markTainted again should keep it tainted
        locator.markTainted();
        assertTrue("TaintableLocatorImpl: still tainted after second markTainted()",
                locator.isTainted());
    }

    // Mock Locator for testing

    private static class MockLocator implements Locator {
        private final String systemId;
        private final String publicId;
        private final int lineNumber;
        private final int columnNumber;

        MockLocator(String systemId, String publicId, int lineNumber, int columnNumber) {
            this.systemId = systemId;
            this.publicId = publicId;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public int getColumnNumber() {
            return columnNumber;
        }
    }

    // Test helpers

    private static void assertEquals(String testName, String expected, String actual) {
        if ((expected == null && actual == null)
                || (expected != null && expected.equals(actual))) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: " + expected);
            System.out.println("  Actual: " + actual);
            failed++;
        }
    }

    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: " + expected);
            System.out.println("  Actual: " + actual);
            failed++;
        }
    }

    private static void assertNull(String testName, Object value) {
        if (value == null) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: null");
            System.out.println("  Actual: " + value);
            failed++;
        }
    }

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
