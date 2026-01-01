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

package nu.validator.xml.test;

import nu.validator.xml.CharacterUtil;

/**
 * Unit tests for CharacterUtil scrubbing methods.
 */
public class CharacterUtilTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing CharacterUtil...");
        testScrubCharacterData();
        testPrudentlyScrubCharacterData();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testScrubCharacterData() {
        // Test that normal ASCII text passes through unchanged
        assertScrubEquals("ASCII text unchanged", "Hello, World!", "Hello, World!");

        // Test allowed whitespace characters
        assertScrubEquals("Tab preserved", "Hello\tWorld", "Hello\tWorld");
        assertScrubEquals("Newline preserved", "Hello\nWorld", "Hello\nWorld");
        assertScrubEquals("Carriage return preserved", "Hello\rWorld", "Hello\rWorld");

        // Test that null character is removed
        assertScrubEquals("Null character removed", "Hello\u0000World", "HelloWorld");

        // Test other control characters (U+0001 - U+0008)
        assertScrubEquals("Control char U+0001 removed", "Hello\u0001World", "HelloWorld");
        assertScrubEquals("Control char U+0008 removed", "Hello\u0008World", "HelloWorld");

        // Test vertical tab and form feed (U+000B, U+000C) - should be removed
        assertScrubEquals("Vertical tab removed", "Hello\u000BWorld", "HelloWorld");
        assertScrubEquals("Form feed removed", "Hello\u000CWorld", "HelloWorld");

        // Test control characters U+000E - U+001F
        assertScrubEquals("Control char U+000E removed", "Hello\u000EWorld", "HelloWorld");
        assertScrubEquals("Control char U+001F removed", "Hello\u001FWorld", "HelloWorld");

        // Test regular Unicode characters are preserved
        assertScrubEquals("Unicode preserved", "Caf\u00e9", "Caf\u00e9");
        assertScrubEquals("CJK preserved", "\u4e2d\u6587", "\u4e2d\u6587");
        assertScrubEquals("Emoji preserved", "\uD83D\uDE00", "\uD83D\uDE00");

        // Test empty string
        assertScrubEquals("Empty string", "", "");
    }

    private static void testPrudentlyScrubCharacterData() {
        // Test that normal ASCII text passes through unchanged
        assertPrudentScrubEquals("ASCII text unchanged", "Hello, World!", "Hello, World!");

        // Test allowed whitespace characters
        assertPrudentScrubEquals("Tab preserved", "Hello\tWorld", "Hello\tWorld");
        assertPrudentScrubEquals("Newline preserved", "Hello\nWorld", "Hello\nWorld");
        assertPrudentScrubEquals("Carriage return preserved", "Hello\rWorld", "Hello\rWorld");

        // Test BOM (U+FEFF) is removed by prudent scrubbing
        assertPrudentScrubEquals("BOM removed", "\uFEFFHello", "Hello");
        assertPrudentScrubEquals("BOM in middle removed", "Hello\uFEFFWorld", "HelloWorld");

        // Test C1 control characters (U+007F - U+009F) are removed
        assertPrudentScrubEquals("DEL U+007F removed", "Hello\u007FWorld", "HelloWorld");
        assertPrudentScrubEquals("C1 U+0080 removed", "Hello\u0080World", "HelloWorld");
        assertPrudentScrubEquals("C1 U+009F removed", "Hello\u009FWorld", "HelloWorld");

        // Test noncharacters in Arabic Presentation Forms-A (U+FDD0 - U+FDDF)
        assertPrudentScrubEquals("Nonchar U+FDD0 removed", "Hello\uFDD0World", "HelloWorld");
        assertPrudentScrubEquals("Nonchar U+FDDF removed", "Hello\uFDDFWorld", "HelloWorld");

        // Test regular Unicode characters are preserved
        assertPrudentScrubEquals("Unicode preserved", "Caf\u00e9", "Caf\u00e9");

        // Test empty string
        assertPrudentScrubEquals("Empty string", "", "");
    }

    private static void assertScrubEquals(String testName, String input, String expected) {
        String actual = CharacterUtil.scrubCharacterData(input);
        if (expected.equals(actual)) {
            pass("scrubCharacterData: " + testName);
        } else {
            System.out.println("FAIL: scrubCharacterData: " + testName);
            System.out.println("  Input: " + escapeForDisplay(input));
            System.out.println("  Expected: " + escapeForDisplay(expected));
            System.out.println("  Actual: " + escapeForDisplay(actual));
            failed++;
        }
    }

    private static void assertPrudentScrubEquals(String testName, String input, String expected) {
        String actual = CharacterUtil.prudentlyScrubCharacterData(input);
        if (expected.equals(actual)) {
            pass("prudentlyScrubCharacterData: " + testName);
        } else {
            System.out.println("FAIL: prudentlyScrubCharacterData: " + testName);
            System.out.println("  Input: " + escapeForDisplay(input));
            System.out.println("  Expected: " + escapeForDisplay(expected));
            System.out.println("  Actual: " + escapeForDisplay(actual));
            failed++;
        }
    }

    private static String escapeForDisplay(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F || (c >= 0x80 && c <= 0x9F) || c == 0xFEFF) {
                sb.append(String.format("\\u%04X", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }
}
