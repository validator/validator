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

package nu.validator.io.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import nu.validator.io.DataUri;

/**
 * Unit tests for DataUri parsing.
 *
 * The DataUri class parses data: URIs according to RFC 2397, extracting the
 * content type and providing access to the decoded data. These tests verify
 * correct parsing of various data URI formats.
 */
public class DataUriTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testStartsWithData();
        testSimpleTextPlain();
        testExplicitTextPlain();
        testTextHtml();
        testWithCharset();
        testBase64Encoded();
        testBase64EncodedImage();
        testEmptyData();
        testWithParameters();
        testCaseInsensitiveScheme();
        testInvalidUris();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testStartsWithData() {
        assertTrue("startsWithData: 'data:' returns true",
                DataUri.startsWithData("data:text/plain,hello"));
        assertTrue("startsWithData: 'DATA:' returns true (case insensitive)",
                DataUri.startsWithData("DATA:text/plain,hello"));
        assertTrue("startsWithData: 'DaTa:' returns true (mixed case)",
                DataUri.startsWithData("DaTa:text/plain,hello"));
        assertFalse("startsWithData: 'http:' returns false",
                DataUri.startsWithData("http://example.com"));
        assertFalse("startsWithData: null returns false",
                DataUri.startsWithData(null));
        assertFalse("startsWithData: 'dat' returns false (too short)",
                DataUri.startsWithData("dat"));
        assertFalse("startsWithData: empty string returns false",
                DataUri.startsWithData(""));
    }

    private static void testSimpleTextPlain() {
        try {
            DataUri dataUri = new DataUri("data:,Hello%20World");
            assertEquals("Simple text/plain: content type is text/plain;charset=US-ASCII",
                    "text/plain;charset=US-ASCII", dataUri.getContentType());
            assertEquals("Simple text/plain: content is 'Hello World'",
                    "Hello World", readContent(dataUri));
            pass("Simple text/plain: parsed successfully");
        } catch (IOException e) {
            fail("Simple text/plain: unexpected exception: " + e.getMessage());
        }
    }

    private static void testExplicitTextPlain() {
        try {
            DataUri dataUri = new DataUri("data:text/plain,Hello");
            assertEquals("Explicit text/plain: content type",
                    "text/plain", dataUri.getContentType());
            assertEquals("Explicit text/plain: content",
                    "Hello", readContent(dataUri));
            pass("Explicit text/plain: parsed successfully");
        } catch (IOException e) {
            fail("Explicit text/plain: unexpected exception: " + e.getMessage());
        }
    }

    private static void testTextHtml() {
        try {
            DataUri dataUri = new DataUri("data:text/html,%3Ch1%3EHello%3C%2Fh1%3E");
            assertEquals("text/html: content type",
                    "text/html", dataUri.getContentType());
            assertEquals("text/html: content",
                    "<h1>Hello</h1>", readContent(dataUri));
            pass("text/html: parsed successfully");
        } catch (IOException e) {
            fail("text/html: unexpected exception: " + e.getMessage());
        }
    }

    private static void testWithCharset() {
        try {
            DataUri dataUri = new DataUri("data:text/plain;charset=utf-8,Hello");
            assertEquals("With charset: content type",
                    "text/plain;charset=utf-8", dataUri.getContentType());
            assertEquals("With charset: content",
                    "Hello", readContent(dataUri));
            pass("With charset: parsed successfully");
        } catch (IOException e) {
            fail("With charset: unexpected exception: " + e.getMessage());
        }
    }

    private static void testBase64Encoded() {
        try {
            // "Hello" in base64 is "SGVsbG8="
            DataUri dataUri = new DataUri("data:text/plain;base64,SGVsbG8=");
            assertEquals("Base64: content type",
                    "text/plain", dataUri.getContentType());
            assertEquals("Base64: decoded content",
                    "Hello", readContent(dataUri));
            pass("Base64: parsed successfully");
        } catch (IOException e) {
            fail("Base64: unexpected exception: " + e.getMessage());
        }
    }

    private static void testBase64EncodedImage() {
        try {
            // A minimal 1x1 transparent PNG in base64
            String pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
            DataUri dataUri = new DataUri("data:image/png;base64," + pngBase64);
            assertEquals("Base64 PNG: content type",
                    "image/png", dataUri.getContentType());
            // Just verify we can read bytes without error
            byte[] content = readBytes(dataUri);
            assertTrue("Base64 PNG: has content", content.length > 0);
            pass("Base64 PNG: parsed successfully");
        } catch (IOException e) {
            fail("Base64 PNG: unexpected exception: " + e.getMessage());
        }
    }

    private static void testEmptyData() {
        try {
            DataUri dataUri = new DataUri("data:text/plain,");
            assertEquals("Empty data: content type",
                    "text/plain", dataUri.getContentType());
            assertEquals("Empty data: content is empty",
                    "", readContent(dataUri));
            pass("Empty data: parsed successfully");
        } catch (IOException e) {
            fail("Empty data: unexpected exception: " + e.getMessage());
        }
    }

    private static void testWithParameters() {
        try {
            DataUri dataUri = new DataUri("data:text/plain;charset=utf-8;foo=bar,Hello");
            assertTrue("With parameters: content type contains charset",
                    dataUri.getContentType().contains("charset=utf-8"));
            assertEquals("With parameters: content",
                    "Hello", readContent(dataUri));
            pass("With parameters: parsed successfully");
        } catch (IOException e) {
            fail("With parameters: unexpected exception: " + e.getMessage());
        }
    }

    private static void testCaseInsensitiveScheme() {
        try {
            DataUri dataUri = new DataUri("DATA:text/plain,Hello");
            assertEquals("Case insensitive scheme: content type",
                    "text/plain", dataUri.getContentType());
            pass("Case insensitive scheme: parsed successfully");
        } catch (IOException e) {
            fail("Case insensitive scheme: unexpected exception: " + e.getMessage());
        }
    }

    private static void testInvalidUris() {
        // Test fragment (not allowed per RFC 2397)
        try {
            new DataUri("data:text/plain,Hello#fragment");
            fail("Fragment: should throw MalformedURLException");
        } catch (MalformedURLException e) {
            pass("Fragment: throws MalformedURLException as expected");
        } catch (IOException e) {
            fail("Fragment: unexpected IOException: " + e.getMessage());
        }

        // Test non-data scheme
        try {
            new DataUri("http://example.com");
            fail("Non-data scheme: should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            pass("Non-data scheme: throws IllegalArgumentException as expected");
        } catch (IOException e) {
            fail("Non-data scheme: unexpected IOException: " + e.getMessage());
        }
    }

    // Helper methods

    private static String readContent(DataUri dataUri) throws IOException {
        return new String(readBytes(dataUri), "UTF-8");
    }

    private static byte[] readBytes(DataUri dataUri) throws IOException {
        InputStream is = dataUri.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        return baos.toByteArray();
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
