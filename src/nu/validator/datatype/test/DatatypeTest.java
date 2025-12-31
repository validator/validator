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

package nu.validator.datatype.test;

import nu.validator.datatype.NonEmptyString;
import nu.validator.datatype.SimpleColor;
import nu.validator.vendor.relaxng.datatype.DatatypeException;

/**
 * Unit tests for HTML5 datatype validators.
 *
 * Tests validation logic for various datatype classes including SimpleColor,
 * NonEmptyString, and other validators.
 */
public class DatatypeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing SimpleColor...");
        testSimpleColorValid();
        testSimpleColorInvalid();

        System.out.println();
        System.out.println("Testing NonEmptyString...");
        testNonEmptyStringValid();
        testNonEmptyStringInvalid();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // SimpleColor tests

    private static void testSimpleColorValid() {
        SimpleColor validator = SimpleColor.THE_INSTANCE;

        assertValid("SimpleColor: lowercase hex", validator, "#abcdef");
        assertValid("SimpleColor: uppercase hex", validator, "#ABCDEF");
        assertValid("SimpleColor: mixed case hex", validator, "#AbCdEf");
        assertValid("SimpleColor: all digits", validator, "#123456");
        assertValid("SimpleColor: black", validator, "#000000");
        assertValid("SimpleColor: white", validator, "#ffffff");
        assertValid("SimpleColor: red", validator, "#ff0000");
        assertValid("SimpleColor: green", validator, "#00ff00");
        assertValid("SimpleColor: blue", validator, "#0000ff");
    }

    private static void testSimpleColorInvalid() {
        SimpleColor validator = SimpleColor.THE_INSTANCE;

        assertInvalid("SimpleColor: empty string", validator, "");
        assertInvalid("SimpleColor: missing hash", validator, "abcdef");
        assertInvalid("SimpleColor: too short (3 digits)", validator, "#fff");
        assertInvalid("SimpleColor: too short (5 digits)", validator, "#12345");
        assertInvalid("SimpleColor: too long (7 digits)", validator, "#1234567");
        assertInvalid("SimpleColor: too long (8 digits)", validator, "#12345678");
        assertInvalid("SimpleColor: invalid hex digit 'g'", validator, "#gggggg");
        assertInvalid("SimpleColor: invalid hex digit 'z'", validator, "#12345z");
        assertInvalid("SimpleColor: spaces not allowed", validator, "# abcde");
        assertInvalid("SimpleColor: leading space", validator, " #abcdef");
        assertInvalid("SimpleColor: trailing space", validator, "#abcdef ");
        assertInvalid("SimpleColor: wrong start char", validator, "@abcdef");
        assertInvalid("SimpleColor: named color not allowed", validator, "red");
        assertInvalid("SimpleColor: rgb() not allowed", validator, "rgb(255,0,0)");
    }

    // NonEmptyString tests

    private static void testNonEmptyStringValid() {
        NonEmptyString validator = NonEmptyString.THE_INSTANCE;

        assertValid("NonEmptyString: single char", validator, "a");
        assertValid("NonEmptyString: word", validator, "hello");
        assertValid("NonEmptyString: sentence", validator, "Hello, World!");
        assertValid("NonEmptyString: whitespace only", validator, " ");
        assertValid("NonEmptyString: tab only", validator, "\t");
        assertValid("NonEmptyString: newline only", validator, "\n");
        assertValid("NonEmptyString: multiple spaces", validator, "   ");
        assertValid("NonEmptyString: unicode", validator, "\u00e9");
        assertValid("NonEmptyString: emoji", validator, "\uD83D\uDE00");
        assertValid("NonEmptyString: very long string", validator,
                "a".repeat(10000));
    }

    private static void testNonEmptyStringInvalid() {
        NonEmptyString validator = NonEmptyString.THE_INSTANCE;

        assertInvalid("NonEmptyString: empty string", validator, "");
    }

    // Test helpers

    private static void assertValid(String testName,
            nu.validator.vendor.relaxng.datatype.Datatype validator, String value) {
        try {
            validator.checkValid(value, null);
            pass(testName);
        } catch (DatatypeException e) {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: valid");
            System.out.println("  Got exception: " + e.getMessage());
            failed++;
        }
    }

    private static void assertInvalid(String testName,
            nu.validator.vendor.relaxng.datatype.Datatype validator, String value) {
        try {
            validator.checkValid(value, null);
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: invalid (exception)");
            System.out.println("  Got: valid (no exception)");
            failed++;
        } catch (DatatypeException e) {
            pass(testName);
        }
    }

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }
}
