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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import nu.validator.io.SystemIdIOException;

/**
 * Unit tests for SystemIdIOException.
 *
 * SystemIdIOException extends IOException to include the systemId (URL) that
 * caused the error. This allows error messages to include the URL for context.
 *
 * See: https://github.com/validator/validator/issues/1783
 */
public class SystemIdIOExceptionTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testConstructorWithSystemIdOnly();
        testConstructorWithSystemIdAndMessage();
        testConstructorWithSystemIdMessageAndCause();
        testDefaultConstructor();
        testIsInstanceOfIOException();
        testHttpRequestFailedPattern();

        System.out.println();
        System.out.println(
                "Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testConstructorWithSystemIdOnly() {
        String url = "https://example.com/test.html";
        SystemIdIOException ex = new SystemIdIOException(url);

        assertEquals("systemId-only constructor: getSystemId()", url,
                ex.getSystemId());
        assertNull("systemId-only constructor: getMessage()", ex.getMessage());
        assertNull("systemId-only constructor: getCause()", ex.getCause());
        pass("systemId-only constructor: created successfully");
    }

    private static void testConstructorWithSystemIdAndMessage() {
        String url = "https://example.com/test.html";
        String message = "Connection refused";
        SystemIdIOException ex = new SystemIdIOException(url, message);

        assertEquals("systemId+message constructor: getSystemId()", url,
                ex.getSystemId());
        assertEquals("systemId+message constructor: getMessage()", message,
                ex.getMessage());
        assertNull("systemId+message constructor: getCause()", ex.getCause());
        pass("systemId+message constructor: created successfully");
    }

    private static void testConstructorWithSystemIdMessageAndCause() {
        String url = "https://unregistered-domain-xyz.com/";
        String message = "HTTP request failed: Name or service not known";
        Exception cause = new ExecutionException("Name or service not known",
                new java.net.UnknownHostException(
                        "unregistered-domain-xyz.com"));

        SystemIdIOException ex = new SystemIdIOException(url, message, cause);

        assertEquals("full constructor: getSystemId()", url, ex.getSystemId());
        assertEquals("full constructor: getMessage()", message,
                ex.getMessage());
        assertEquals("full constructor: getCause()", cause, ex.getCause());
        pass("full constructor: created successfully");
    }

    private static void testDefaultConstructor() {
        SystemIdIOException ex = new SystemIdIOException();

        assertNull("default constructor: getSystemId()", ex.getSystemId());
        assertNull("default constructor: getMessage()", ex.getMessage());
        pass("default constructor: created successfully");
    }

    private static void testIsInstanceOfIOException() {
        SystemIdIOException ex = new SystemIdIOException(
                "https://example.com/");

        assertTrue("instanceof IOException",
                ex instanceof java.io.IOException);
        pass("instanceof IOException: verified");
    }

    /**
     * Test the exact pattern used in PrudentHttpEntityResolver when wrapping
     * HTTP client exceptions (ExecutionException, TimeoutException, etc.)
     */
    private static void testHttpRequestFailedPattern() {
        String systemId = "https://nonexistent-domain-12345.example/page.html";

        // Simulate what happens when DNS resolution fails
        ExecutionException httpClientException = new ExecutionException(
                "java.net.UnknownHostException: "
                        + "nonexistent-domain-12345.example: "
                        + "Name or service not known",
                new java.net.UnknownHostException(
                        "nonexistent-domain-12345.example: "
                                + "Name or service not known"));

        // This is the pattern used in PrudentHttpEntityResolver
        SystemIdIOException ex = new SystemIdIOException(systemId,
                "HTTP request failed: " + httpClientException.getMessage(),
                httpClientException);

        assertEquals("HTTP pattern: getSystemId() preserves URL", systemId,
                ex.getSystemId());
        assertTrue("HTTP pattern: getMessage() contains 'HTTP request failed'",
                ex.getMessage().contains("HTTP request failed"));
        assertTrue("HTTP pattern: getMessage() contains DNS error",
                ex.getMessage().contains("Name or service not known"));
        pass("HTTP request failed pattern: works correctly");
    }

    // Helper methods

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
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

    private static void assertEquals(String testName, Object expected,
            Object actual) {
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
}
