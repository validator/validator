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

import nu.validator.datatype.CustomElementName;
import nu.validator.datatype.HashName;
import nu.validator.datatype.Id;
import nu.validator.datatype.MetaCharset;
import nu.validator.datatype.NonEmptyString;
import nu.validator.datatype.SimpleColor;
import nu.validator.datatype.Time;
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
        System.out.println("Testing Time...");
        testTimeValid();
        testTimeInvalid();

        System.out.println();
        System.out.println("Testing Id...");
        testIdValid();
        testIdInvalid();

        System.out.println();
        System.out.println("Testing HashName...");
        testHashNameValid();
        testHashNameInvalid();

        System.out.println();
        System.out.println("Testing CustomElementName...");
        testCustomElementNameValid();
        testCustomElementNameInvalid();

        System.out.println();
        System.out.println("Testing MetaCharset...");
        testMetaCharsetValid();
        testMetaCharsetInvalid();
        testMetaCharsetLooping();

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

    // Time tests

    private static void testTimeValid() {
        Time validator = Time.THE_INSTANCE;

        // Basic valid times (HH:MM)
        assertValid("Time: midnight", validator, "00:00");
        assertValid("Time: noon", validator, "12:00");
        assertValid("Time: end of day", validator, "23:59");
        assertValid("Time: early morning", validator, "06:30");
        assertValid("Time: evening", validator, "18:45");

        // With seconds (HH:MM:SS)
        assertValid("Time: with seconds", validator, "12:30:45");
        assertValid("Time: midnight with seconds", validator, "00:00:00");
        assertValid("Time: max seconds", validator, "23:59:59");

        // With milliseconds (HH:MM:SS.mmm)
        assertValid("Time: with 1 decimal", validator, "12:30:45.1");
        assertValid("Time: with 2 decimals", validator, "12:30:45.12");
        assertValid("Time: with 3 decimals", validator, "12:30:45.123");
    }

    private static void testTimeInvalid() {
        Time validator = Time.THE_INSTANCE;

        assertInvalid("Time: empty string", validator, "");
        assertInvalid("Time: invalid hour 24", validator, "24:00");
        assertInvalid("Time: invalid hour 25", validator, "25:00");
        assertInvalid("Time: invalid minute 60", validator, "12:60");
        assertInvalid("Time: invalid second 60", validator, "12:30:60");
        assertInvalid("Time: single digit hour", validator, "1:00");
        assertInvalid("Time: single digit minute", validator, "12:0");
        assertInvalid("Time: missing colon", validator, "1200");
        assertInvalid("Time: extra colon", validator, "12:00:");
        assertInvalid("Time: AM/PM not allowed", validator, "12:00 PM");
        assertInvalid("Time: text not allowed", validator, "noon");
        assertInvalid("Time: negative hour", validator, "-01:00");
    }

    // Id tests

    private static void testIdValid() {
        Id validator = Id.THE_INSTANCE;

        assertValid("Id: single char", validator, "a");
        assertValid("Id: simple id", validator, "myId");
        assertValid("Id: with hyphen", validator, "my-id");
        assertValid("Id: with underscore", validator, "my_id");
        assertValid("Id: with numbers", validator, "id123");
        assertValid("Id: starting with number", validator, "123");
        assertValid("Id: special chars", validator, "id!@#$%");
        assertValid("Id: unicode", validator, "\u00e9l\u00e8ve");
        assertValid("Id: emoji", validator, "\uD83D\uDE00");
        assertValid("Id: long id", validator, "a".repeat(1000));
    }

    private static void testIdInvalid() {
        Id validator = Id.THE_INSTANCE;

        assertInvalid("Id: empty string", validator, "");
        assertInvalid("Id: single space", validator, " ");
        assertInvalid("Id: leading space", validator, " id");
        assertInvalid("Id: trailing space", validator, "id ");
        assertInvalid("Id: space in middle", validator, "my id");
        assertInvalid("Id: tab", validator, "my\tid");
        assertInvalid("Id: newline", validator, "my\nid");
        assertInvalid("Id: carriage return", validator, "my\rid");
    }

    // HashName tests

    private static void testHashNameValid() {
        HashName validator = HashName.THE_INSTANCE;

        assertValid("HashName: simple", validator, "#foo");
        assertValid("HashName: single char after hash", validator, "#a");
        assertValid("HashName: with hyphen", validator, "#my-name");
        assertValid("HashName: with numbers", validator, "#id123");
        assertValid("HashName: only numbers", validator, "#123");
        assertValid("HashName: unicode", validator, "#caf\u00e9");
        assertValid("HashName: with spaces after hash", validator, "# space");
    }

    private static void testHashNameInvalid() {
        HashName validator = HashName.THE_INSTANCE;

        assertInvalid("HashName: empty string", validator, "");
        assertInvalid("HashName: just hash", validator, "#");
        assertInvalid("HashName: no hash", validator, "foo");
        assertInvalid("HashName: wrong prefix", validator, "@foo");
    }

    // CustomElementName tests

    private static void testCustomElementNameValid() {
        CustomElementName validator = CustomElementName.THE_INSTANCE;

        assertValid("CustomElementName: simple", validator, "my-element");
        assertValid("CustomElementName: with numbers", validator, "my-element-123");
        assertValid("CustomElementName: multiple hyphens", validator, "my-custom-element");
        assertValid("CustomElementName: hyphen at end", validator, "element-");
        assertValid("CustomElementName: underscore", validator, "my_element-test");
        assertValid("CustomElementName: dot", validator, "my.element-test");
        assertValid("CustomElementName: unicode", validator, "my-\u00e9l\u00e8ment");
        assertValid("CustomElementName: single letter then hyphen", validator, "x-foo");
    }

    private static void testCustomElementNameInvalid() {
        CustomElementName validator = CustomElementName.THE_INSTANCE;

        assertInvalid("CustomElementName: empty string", validator, "");
        assertInvalid("CustomElementName: no hyphen", validator, "myelement");
        assertInvalid("CustomElementName: starts with number", validator, "1-element");
        assertInvalid("CustomElementName: starts with hyphen", validator, "-element");
        assertInvalid("CustomElementName: starts with uppercase", validator, "My-element");
        assertInvalid("CustomElementName: contains uppercase", validator, "my-Element");
        assertInvalid("CustomElementName: prohibited annotation-xml", validator, "annotation-xml");
        assertInvalid("CustomElementName: prohibited color-profile", validator, "color-profile");
        assertInvalid("CustomElementName: prohibited font-face", validator, "font-face");
        assertInvalid("CustomElementName: prohibited font-face-format", validator, "font-face-format");
        assertInvalid("CustomElementName: prohibited font-face-name", validator, "font-face-name");
        assertInvalid("CustomElementName: prohibited font-face-src", validator, "font-face-src");
        assertInvalid("CustomElementName: prohibited font-face-uri", validator, "font-face-uri");
        assertInvalid("CustomElementName: prohibited missing-glyph", validator, "missing-glyph");
    }

    // MetaCharset tests

    private static void testMetaCharsetValid() {
        MetaCharset validator = MetaCharset.THE_INSTANCE;

        assertValid("MetaCharset: simple valid",
                validator, "text/html; charset=utf-8");
        assertValid("MetaCharset: no space after semicolon",
                validator, "text/html;charset=utf-8");
        assertValid("MetaCharset: multiple spaces",
                validator, "text/html;   charset=utf-8");
        assertValid("MetaCharset: tab after semicolon",
                validator, "text/html;\tcharset=utf-8");
        assertValid("MetaCharset: uppercase CHARSET",
                validator, "text/html; CHARSET=utf-8");
        assertValid("MetaCharset: mixed case",
                validator, "text/html; ChArSeT=utf-8");
    }

    private static void testMetaCharsetInvalid() {
        MetaCharset validator = MetaCharset.THE_INSTANCE;

        assertInvalid("MetaCharset: empty string",
                validator, "");
        assertInvalid("MetaCharset: missing text/html",
                validator, "charset=utf-8");
        assertInvalid("MetaCharset: wrong content type",
                validator, "text/plain; charset=utf-8");
        assertInvalid("MetaCharset: missing charset",
                validator, "text/html;");
        assertInvalid("MetaCharset: wrong encoding",
                validator, "text/html; charset=iso-8859-1");
        assertInvalid("MetaCharset: empty encoding",
                validator, "text/html; charset=");
    }

    /**
     * Tests for issue #877: The algorithm should loop to find a valid
     * "charset=" pattern when the first occurrence is not followed by "=".
     * https://github.com/validator/validator/issues/877
     */
    private static void testMetaCharsetLooping() {
        MetaCharset validator = MetaCharset.THE_INSTANCE;

        // These are the test cases from issue #877
        // All should extract "utf-8" (we test with utf-8 since that's
        // the only valid encoding for HTML5)
        assertValid("MetaCharset: charset space charset=",
                validator, "text/html; charset charset=utf-8");
        assertValid("MetaCharset: charsetxxxxxcharset=",
                validator, "text/html; charsetxxxxxcharset=utf-8");
        assertValid("MetaCharset: charsetcharset=",
                validator, "text/html; charsetcharset=utf-8");

        // Additional edge cases
        assertValid("MetaCharset: charset without = then valid charset=",
                validator, "text/html; charsetfoo charset=utf-8");
        assertValid("MetaCharset: multiple invalid charsets then valid",
                validator, "text/html; charset; charset; charset=utf-8");
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
