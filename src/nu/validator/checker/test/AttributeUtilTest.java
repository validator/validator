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

import java.util.Arrays;

import nu.validator.checker.AttributeUtil;

/**
 * Unit tests for AttributeUtil static utility methods.
 *
 * Tests integer parsing with lenient error handling, whitespace-aware string
 * splitting, and ASCII-case-insensitive string comparison.
 */
public class AttributeUtilTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing parseInteger...");
        testParseIntegerBasic();
        testParseIntegerWithWhitespace();
        testParseIntegerWithTrailingGarbage();
        testParseIntegerNegative();
        testParseIntegerEdgeCases();

        System.out.println();
        System.out.println("Testing parseNonNegativeInteger...");
        testParseNonNegativeInteger();

        System.out.println();
        System.out.println("Testing parsePositiveInteger...");
        testParsePositiveInteger();

        System.out.println();
        System.out.println("Testing split...");
        testSplitBasic();
        testSplitWithVariousWhitespace();
        testSplitEdgeCases();

        System.out.println();
        System.out.println("Testing lowerCaseLiteralEqualsIgnoreAsciiCaseString...");
        testAsciiCaseInsensitiveEquals();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // parseInteger tests

    private static void testParseIntegerBasic() {
        assertEquals("parseInteger: simple positive", 42, AttributeUtil.parseInteger("42"));
        assertEquals("parseInteger: zero", 0, AttributeUtil.parseInteger("0"));
        assertEquals("parseInteger: large number", 123456789, AttributeUtil.parseInteger("123456789"));
    }

    private static void testParseIntegerWithWhitespace() {
        assertEquals("parseInteger: leading space", 42, AttributeUtil.parseInteger(" 42"));
        assertEquals("parseInteger: leading tab", 42, AttributeUtil.parseInteger("\t42"));
        assertEquals("parseInteger: leading newline", 42, AttributeUtil.parseInteger("\n42"));
        assertEquals("parseInteger: leading CR", 42, AttributeUtil.parseInteger("\r42"));
        assertEquals("parseInteger: multiple leading whitespace", 42,
                AttributeUtil.parseInteger("  \t\n\r 42"));
    }

    private static void testParseIntegerWithTrailingGarbage() {
        // The regex uses matches() which requires full string match after whitespace+number
        // Actually looking at the pattern: "^[ \t\n\r]*(-?[0-9]+)" with matches()
        // This means trailing garbage is NOT allowed (matches requires full match)
        assertEquals("parseInteger: trailing text fails", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("42abc"));
        assertEquals("parseInteger: trailing space fails", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("42 "));
    }

    private static void testParseIntegerNegative() {
        assertEquals("parseInteger: negative number", -42, AttributeUtil.parseInteger("-42"));
        assertEquals("parseInteger: negative with leading space", -42,
                AttributeUtil.parseInteger(" -42"));
        assertEquals("parseInteger: negative zero", 0, AttributeUtil.parseInteger("-0"));
    }

    private static void testParseIntegerEdgeCases() {
        assertEquals("parseInteger: null returns MIN_VALUE", Integer.MIN_VALUE,
                AttributeUtil.parseInteger(null));
        assertEquals("parseInteger: empty string returns MIN_VALUE", Integer.MIN_VALUE,
                AttributeUtil.parseInteger(""));
        assertEquals("parseInteger: only whitespace returns MIN_VALUE", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("   "));
        assertEquals("parseInteger: non-numeric returns MIN_VALUE", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("abc"));
        assertEquals("parseInteger: plus sign not allowed", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("+42"));
        // Test overflow - Integer.parseInt will throw NumberFormatException
        assertEquals("parseInteger: overflow returns MIN_VALUE", Integer.MIN_VALUE,
                AttributeUtil.parseInteger("99999999999999999999"));
    }

    // parseNonNegativeInteger tests

    private static void testParseNonNegativeInteger() {
        assertEquals("parseNonNegativeInteger: positive", 42,
                AttributeUtil.parseNonNegativeInteger("42"));
        assertEquals("parseNonNegativeInteger: zero", 0,
                AttributeUtil.parseNonNegativeInteger("0"));
        assertEquals("parseNonNegativeInteger: negative returns -1", -1,
                AttributeUtil.parseNonNegativeInteger("-42"));
        assertEquals("parseNonNegativeInteger: null returns -1", -1,
                AttributeUtil.parseNonNegativeInteger(null));
        assertEquals("parseNonNegativeInteger: with leading whitespace", 42,
                AttributeUtil.parseNonNegativeInteger("  42"));
    }

    // parsePositiveInteger tests

    private static void testParsePositiveInteger() {
        assertEquals("parsePositiveInteger: positive", 42,
                AttributeUtil.parsePositiveInteger("42"));
        assertEquals("parsePositiveInteger: one", 1,
                AttributeUtil.parsePositiveInteger("1"));
        assertEquals("parsePositiveInteger: zero returns -1", -1,
                AttributeUtil.parsePositiveInteger("0"));
        assertEquals("parsePositiveInteger: negative returns -1", -1,
                AttributeUtil.parsePositiveInteger("-42"));
        assertEquals("parsePositiveInteger: null returns -1", -1,
                AttributeUtil.parsePositiveInteger(null));
    }

    // split tests

    private static void testSplitBasic() {
        assertArrayEquals("split: single word", new String[] { "hello" },
                AttributeUtil.split("hello"));
        assertArrayEquals("split: two words", new String[] { "hello", "world" },
                AttributeUtil.split("hello world"));
        assertArrayEquals("split: three words", new String[] { "a", "b", "c" },
                AttributeUtil.split("a b c"));
    }

    private static void testSplitWithVariousWhitespace() {
        assertArrayEquals("split: multiple spaces", new String[] { "hello", "world" },
                AttributeUtil.split("hello   world"));
        assertArrayEquals("split: tabs", new String[] { "hello", "world" },
                AttributeUtil.split("hello\tworld"));
        assertArrayEquals("split: newlines", new String[] { "hello", "world" },
                AttributeUtil.split("hello\nworld"));
        assertArrayEquals("split: mixed whitespace", new String[] { "a", "b", "c" },
                AttributeUtil.split("a \t\n\r b   c"));
        assertArrayEquals("split: leading whitespace", new String[] { "hello", "world" },
                AttributeUtil.split("  hello world"));
        assertArrayEquals("split: trailing whitespace", new String[] { "hello", "world" },
                AttributeUtil.split("hello world  "));
        assertArrayEquals("split: leading and trailing", new String[] { "hello" },
                AttributeUtil.split("  hello  "));
    }

    private static void testSplitEdgeCases() {
        assertArrayEquals("split: null returns empty", new String[] {},
                AttributeUtil.split(null));
        assertArrayEquals("split: empty string returns empty", new String[] {},
                AttributeUtil.split(""));
        assertArrayEquals("split: only whitespace returns empty", new String[] {},
                AttributeUtil.split("   \t\n  "));
    }

    // lowerCaseLiteralEqualsIgnoreAsciiCaseString tests

    private static void testAsciiCaseInsensitiveEquals() {
        assertTrue("asciiEquals: exact match",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", "hello"));
        assertTrue("asciiEquals: uppercase input",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", "HELLO"));
        assertTrue("asciiEquals: mixed case input",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", "HeLLo"));
        assertFalse("asciiEquals: different strings",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", "world"));
        assertFalse("asciiEquals: different lengths",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", "hi"));
        assertFalse("asciiEquals: null string",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello", null));
        assertTrue("asciiEquals: empty strings",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("", ""));

        // ASCII-only case conversion (not Unicode)
        assertTrue("asciiEquals: with digits",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("abc123", "ABC123"));
        assertTrue("asciiEquals: with special chars",
                AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("hello-world", "HELLO-WORLD"));
    }

    // Test helpers

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

    private static void assertArrayEquals(String testName, String[] expected, String[] actual) {
        if (Arrays.equals(expected, actual)) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: " + Arrays.toString(expected));
            System.out.println("  Actual: " + Arrays.toString(actual));
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
