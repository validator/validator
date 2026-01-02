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
import nu.validator.datatype.Date;
import nu.validator.datatype.HashName;
import nu.validator.datatype.Html5DatatypeLibrary;
import nu.validator.datatype.Id;
import nu.validator.datatype.MetaCharset;
import nu.validator.datatype.MimeType;
import nu.validator.datatype.Month;
import nu.validator.datatype.NonEmptyString;
import nu.validator.datatype.SimpleColor;
import nu.validator.datatype.Time;
import nu.validator.datatype.Week;
import nu.validator.vendor.relaxng.datatype.Datatype;
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
        System.out.println("Testing Date...");
        testDateValid();
        testDateInvalid();

        System.out.println();
        System.out.println("Testing Month...");
        testMonthValid();
        testMonthInvalid();

        System.out.println();
        System.out.println("Testing Week...");
        testWeekValid();
        testWeekInvalid();

        System.out.println();
        System.out.println("Testing MimeType...");
        testMimeTypeValid();
        testMimeTypeInvalid();

        System.out.println();
        System.out.println("Testing IriRef (URL)...");
        testIriRefValid();
        testIriRefInvalid();

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

    // Date tests

    private static void testDateValid() {
        Date validator = Date.THE_INSTANCE;

        assertValid("Date: basic date", validator, "2024-01-15");
        assertValid("Date: first day of year", validator, "2024-01-01");
        assertValid("Date: last day of year", validator, "2024-12-31");
        assertValid("Date: leap year Feb 29", validator, "2024-02-29");
        assertValid("Date: year 1", validator, "0001-01-01");
        assertValid("Date: 5-digit year", validator, "10000-06-15");
        assertValid("Date: end of February non-leap", validator, "2023-02-28");
        assertValid("Date: April 30", validator, "2024-04-30");
        assertValid("Date: June 30", validator, "2024-06-30");
    }

    private static void testDateInvalid() {
        Date validator = Date.THE_INSTANCE;

        assertInvalid("Date: empty string", validator, "");
        assertInvalid("Date: invalid month 00", validator, "2024-00-15");
        assertInvalid("Date: invalid month 13", validator, "2024-13-15");
        assertInvalid("Date: invalid day 00", validator, "2024-01-00");
        assertInvalid("Date: invalid day 32", validator, "2024-01-32");
        assertInvalid("Date: Feb 30", validator, "2024-02-30");
        assertInvalid("Date: Feb 29 non-leap", validator, "2023-02-29");
        assertInvalid("Date: April 31", validator, "2024-04-31");
        assertInvalid("Date: June 31", validator, "2024-06-31");
        assertInvalid("Date: year 0", validator, "0000-01-01");
        assertInvalid("Date: 2-digit year", validator, "24-01-15");
        assertInvalid("Date: 3-digit year", validator, "124-01-15");
        assertInvalid("Date: single digit month", validator, "2024-1-15");
        assertInvalid("Date: single digit day", validator, "2024-01-5");
        assertInvalid("Date: slashes instead of dashes", validator, "2024/01/15");
        assertInvalid("Date: no separators", validator, "20240115");
        assertInvalid("Date: with time", validator, "2024-01-15T12:00");
    }

    // Month tests

    private static void testMonthValid() {
        Month validator = Month.THE_INSTANCE;

        assertValid("Month: basic month", validator, "2024-01");
        assertValid("Month: first month", validator, "2024-01");
        assertValid("Month: last month", validator, "2024-12");
        assertValid("Month: year 1", validator, "0001-06");
        assertValid("Month: 5-digit year", validator, "10000-06");
    }

    private static void testMonthInvalid() {
        Month validator = Month.THE_INSTANCE;

        assertInvalid("Month: empty string", validator, "");
        assertInvalid("Month: invalid month 00", validator, "2024-00");
        assertInvalid("Month: invalid month 13", validator, "2024-13");
        assertInvalid("Month: year 0", validator, "0000-01");
        assertInvalid("Month: 2-digit year", validator, "24-01");
        assertInvalid("Month: single digit month", validator, "2024-1");
        assertInvalid("Month: with day", validator, "2024-01-15");
        assertInvalid("Month: no separator", validator, "202401");
    }

    // Week tests

    private static void testWeekValid() {
        Week validator = Week.THE_INSTANCE;

        assertValid("Week: basic week", validator, "2024-W01");
        assertValid("Week: first week", validator, "2024-W01");
        assertValid("Week: week 52", validator, "2024-W52");
        assertValid("Week: week 53 in year with 53 weeks", validator, "2004-W53");
        assertValid("Week: week 53 in 2020", validator, "2020-W53");
        assertValid("Week: year 1", validator, "0001-W01");
        assertValid("Week: 5-digit year", validator, "10000-W26");
    }

    private static void testWeekInvalid() {
        Week validator = Week.THE_INSTANCE;

        assertInvalid("Week: empty string", validator, "");
        assertInvalid("Week: invalid week 00", validator, "2024-W00");
        assertInvalid("Week: invalid week 54", validator, "2024-W54");
        assertInvalid("Week: week 53 in year without 53 weeks", validator, "2023-W53");
        assertInvalid("Week: year 0", validator, "0000-W01");
        assertInvalid("Week: missing W prefix", validator, "2024-01");
        assertInvalid("Week: lowercase w", validator, "2024-w01");
        assertInvalid("Week: single digit week", validator, "2024-W1");
    }

    // MimeType tests

    private static void testMimeTypeValid() {
        MimeType validator = MimeType.THE_INSTANCE;

        assertValid("MimeType: text/html", validator, "text/html");
        assertValid("MimeType: text/plain", validator, "text/plain");
        assertValid("MimeType: application/json", validator, "application/json");
        assertValid("MimeType: image/png", validator, "image/png");
        assertValid("MimeType: with charset param", validator, "text/html; charset=utf-8");
        assertValid("MimeType: with quoted param", validator, "text/html; charset=\"utf-8\"");
        assertValid("MimeType: multiple params", validator, "text/html; charset=utf-8; boundary=something");
        assertValid("MimeType: param with spaces", validator, "text/html ; charset=utf-8");
        assertValid("MimeType: vendor type", validator, "application/vnd.ms-excel");
        assertValid("MimeType: x- prefix", validator, "application/x-custom");
        assertValid("MimeType: with + suffix", validator, "application/atom+xml");
        assertValid("MimeType: multipart/form-data", validator, "multipart/form-data");
    }

    private static void testMimeTypeInvalid() {
        MimeType validator = MimeType.THE_INSTANCE;

        assertInvalid("MimeType: empty string", validator, "");
        assertInvalid("MimeType: no subtype", validator, "text");
        assertInvalid("MimeType: no subtype with slash", validator, "text/");
        assertInvalid("MimeType: no supertype", validator, "/html");
        assertInvalid("MimeType: just slash", validator, "/");
        assertInvalid("MimeType: trailing semicolon", validator, "text/html;");
        assertInvalid("MimeType: param without value", validator, "text/html; charset");
        assertInvalid("MimeType: param with = but no value", validator, "text/html; charset=");
        assertInvalid("MimeType: unclosed quote", validator, "text/html; charset=\"utf-8");
        assertInvalid("MimeType: trailing whitespace", validator, "text/html ");
    }

    // IriRef (URL) tests

    private static void testIriRefValid() {
        Html5DatatypeLibrary library = new Html5DatatypeLibrary();
        Datatype validator;
        try {
            validator = library.createDatatype("iri-ref");
        } catch (DatatypeException e) {
            System.out.println("FAIL: Could not create iri-ref datatype: " + e.getMessage());
            failed++;
            return;
        }

        // Absolute URLs
        assertValid("IriRef: http URL", validator, "http://example.com/");
        assertValid("IriRef: https URL", validator, "https://example.com/");
        assertValid("IriRef: http with port", validator, "http://example.com:8080/");
        assertValid("IriRef: http with path", validator, "http://example.com/path/to/file");
        assertValid("IriRef: http with query", validator, "http://example.com/?foo=bar");
        assertValid("IriRef: http with fragment", validator, "http://example.com/#section");
        assertValid("IriRef: http with all parts", validator, "http://user:pass@example.com:8080/path?q=1#frag");
        assertValid("IriRef: ftp URL", validator, "ftp://ftp.example.com/file");
        assertValid("IriRef: mailto URL", validator, "mailto:user@example.com");
        assertValid("IriRef: file URL", validator, "file:///path/to/file");

        // Relative URLs
        assertValid("IriRef: relative path", validator, "path/to/file");
        assertValid("IriRef: absolute path", validator, "/path/to/file");
        assertValid("IriRef: fragment only", validator, "#section");
        assertValid("IriRef: query only", validator, "?query=value");
        assertValid("IriRef: relative with query", validator, "file.html?q=1");
        assertValid("IriRef: parent directory", validator, "../file.html");
        assertValid("IriRef: current directory", validator, "./file.html");

        // Data URLs
        assertValid("IriRef: data URL text", validator, "data:text/plain,Hello");
        // Minimal valid base64 PNG (1x1 transparent pixel)
        assertValid("IriRef: data URL base64", validator,
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        // Special schemes
        assertValid("IriRef: javascript (allowed)", validator, "javascript:void(0)");
        assertValid("IriRef: webcal (http alias)", validator, "webcal://example.com/cal");
        assertValid("IriRef: feed (http alias)", validator, "feed://example.com/rss");

        // Unicode in URLs
        assertValid("IriRef: unicode in path", validator, "http://example.com/caf%C3%A9");
        assertValid("IriRef: IDN domain", validator, "http://xn--nxasmq5b.com/");
    }

    private static void testIriRefInvalid() {
        Html5DatatypeLibrary library = new Html5DatatypeLibrary();
        Datatype validator;
        try {
            validator = library.createDatatype("iri-ref");
        } catch (DatatypeException e) {
            System.out.println("FAIL: Could not create iri-ref datatype: " + e.getMessage());
            failed++;
            return;
        }

        assertInvalid("IriRef: empty string", validator, "");
        assertInvalid("IriRef: whitespace only", validator, "   ");
        assertInvalid("IriRef: tabs only", validator, "\t\t");
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
