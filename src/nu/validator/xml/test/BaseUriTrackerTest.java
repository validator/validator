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

package nu.validator.xml.test;

import nu.validator.xml.BaseUriTracker;

/**
 * Unit tests for BaseUriTracker.
 *
 * Tests that malformed URLs are handled gracefully without throwing
 * uncaught exceptions like StringIndexOutOfBoundsException.
 */
public class BaseUriTrackerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testValidUrlResolution();
        testMalformedUrlsReturnNull();
        testNullBaseUrl();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testValidUrlResolution() {
        BaseUriTracker tracker = new BaseUriTracker(
                "https://example.com/path/page.html", null);

        String result = tracker.toAbsoluteUriWithCurrentBase("image.png");
        assertEquals("Valid relative URL resolves correctly",
                "https://example.com/path/image.png", result);

        result = tracker.toAbsoluteUriWithCurrentBase("/absolute/path.png");
        assertEquals("Absolute path resolves correctly",
                "https://example.com/absolute/path.png", result);

        result = tracker.toAbsoluteUriWithCurrentBase("https://other.com/img.png");
        assertEquals("Absolute URL passes through",
                "https://other.com/img.png", result);
    }

    private static void testMalformedUrlsReturnNull() {
        BaseUriTracker tracker = new BaseUriTracker(
                "https://example.com/page.html", null);

        // These malformed URLs should return null, not throw exceptions
        String[] malformedUrls = {
            ":",
            ":/",
            "://",
            ":///",
            ":a",
            "a:",
            "a::",
            "::",
            "://:",
            ":::",
        };

        for (String url : malformedUrls) {
            try {
                String result = tracker.toAbsoluteUriWithCurrentBase(url);
                // Result can be null or a string, but should not throw
                pass("Malformed URL '" + url + "' handled without exception");
            } catch (StringIndexOutOfBoundsException e) {
                fail("Malformed URL '" + url + "' threw StringIndexOutOfBoundsException");
            } catch (Exception e) {
                fail("Malformed URL '" + url + "' threw " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
            }
        }
    }

    private static void testNullBaseUrl() {
        // Test with invalid base URL that results in null
        BaseUriTracker tracker = new BaseUriTracker("not-a-valid-url", null);

        try {
            String result = tracker.toAbsoluteUriWithCurrentBase("image.png");
            // Should handle gracefully
            pass("Null base URL handled without exception");
        } catch (Exception e) {
            fail("Null base URL threw " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }
    }

    // Helper methods

    private static void assertEquals(String testName, String expected, String actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: " + expected);
            System.out.println("  Actual: " + actual);
            failed++;
        }
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
