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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import nu.validator.checker.CspEnforcementChecker;

/**
 * Unit tests for CspEnforcementChecker.
 *
 * The CspEnforcementChecker validates that page resources comply with
 * Content Security Policy directives specified in meta tags or HTTP headers.
 *
 * See: https://github.com/validator/validator/issues/207
 */
public class CspEnforcementCheckerTest {

    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Testing CSP meta tag detection...");
        testCspMetaTagDetection();
        testNoCspMetaTag();

        System.out.println();
        System.out.println("Testing inline script violations...");
        testInlineScriptViolation();
        testInlineScriptWithUnsafeInline();
        testInlineScriptWithNonce();

        System.out.println();
        System.out.println("Testing event handler violations...");
        testEventHandlerViolation();
        testEventHandlerWithUnsafeInline();

        System.out.println();
        System.out.println("Testing inline style violations...");
        testInlineStyleViolation();
        testInlineStyleWithUnsafeInline();

        System.out.println();
        System.out.println("Testing style attribute violations...");
        testStyleAttributeViolation();
        testStyleAttributeWithUnsafeInline();

        System.out.println();
        System.out.println("Testing external resource violations...");
        testExternalScriptViolation();
        testExternalScriptAllowed();
        testExternalStylesheetViolation();
        testExternalStylesheetAllowed();

        System.out.println();
        System.out.println("Testing default-src fallback...");
        testDefaultSrcFallback();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // CSP meta tag detection tests

    private static void testCspMetaTagDetection() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self'",
                "<script>alert('test');</script>");
        assertContains("CSP meta tag detected, inline script blocked",
                warnings, "Inline script violates");
    }

    private static void testNoCspMetaTag() throws Exception {
        List<String> warnings = validateWithoutCsp(
                "<script>alert('test');</script>");
        assertTrue("No CSP meta tag: no warnings", warnings.isEmpty());
    }

    // Inline script tests

    private static void testInlineScriptViolation() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self'",
                "<script>alert('test');</script>");
        assertContains("Inline script without unsafe-inline blocked",
                warnings, "Inline script violates");
    }

    private static void testInlineScriptWithUnsafeInline() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self' 'unsafe-inline'",
                "<script>alert('test');</script>");
        assertTrue("Inline script with unsafe-inline allowed", warnings.isEmpty());
    }

    private static void testInlineScriptWithNonce() throws Exception {
        List<String> warnings = validateWithCsp(
                "script-src 'self' 'nonce-abc123'",
                "<script nonce=\"abc123\">alert('test');</script>");
        assertTrue("Inline script with matching nonce allowed", warnings.isEmpty());
    }

    // Event handler tests

    private static void testEventHandlerViolation() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self'",
                "<button onclick=\"alert('click')\">Click</button>");
        assertContains("Event handler without unsafe-inline blocked",
                warnings, "onclick");
    }

    private static void testEventHandlerWithUnsafeInline() throws Exception {
        List<String> warnings = validateWithCsp(
                "script-src 'self' 'unsafe-inline'",
                "<button onclick=\"alert('click')\">Click</button>");
        assertTrue("Event handler with unsafe-inline allowed", warnings.isEmpty());
    }

    // Inline style tests

    private static void testInlineStyleViolation() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self'",
                "<style>body { color: red; }</style>");
        assertContains("Inline style without unsafe-inline blocked",
                warnings, "Inline style violates");
    }

    private static void testInlineStyleWithUnsafeInline() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self' 'unsafe-inline'",
                "<style>body { color: red; }</style>");
        assertTrue("Inline style with unsafe-inline allowed", warnings.isEmpty());
    }

    // Style attribute tests

    private static void testStyleAttributeViolation() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self'",
                "<p style=\"color: red;\">Text</p>");
        assertContains("Style attribute without unsafe-inline blocked",
                warnings, "style");
    }

    private static void testStyleAttributeWithUnsafeInline() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self' 'unsafe-inline'",
                "<p style=\"color: red;\">Text</p>");
        assertTrue("Style attribute with unsafe-inline allowed", warnings.isEmpty());
    }

    // External resource tests

    private static void testExternalScriptViolation() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self'",
                "<script src=\"https://evil.com/script.js\"></script>");
        assertContains("External script from disallowed origin blocked",
                warnings, "script");
    }

    private static void testExternalScriptAllowed() throws Exception {
        List<String> warnings = validateWithCsp("script-src 'self' https://cdn.example.com",
                "<script src=\"https://cdn.example.com/script.js\"></script>");
        assertTrue("External script from allowed origin passes", warnings.isEmpty());
    }

    private static void testExternalStylesheetViolation() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self'",
                "<link rel=\"stylesheet\" href=\"https://evil.com/style.css\">");
        assertContains("External stylesheet from disallowed origin blocked",
                warnings, "stylesheet");
    }

    private static void testExternalStylesheetAllowed() throws Exception {
        List<String> warnings = validateWithCsp("style-src 'self' https://cdn.example.com",
                "<link rel=\"stylesheet\" href=\"https://cdn.example.com/style.css\">");
        assertTrue("External stylesheet from allowed origin passes", warnings.isEmpty());
    }

    // Default-src fallback test

    private static void testDefaultSrcFallback() throws Exception {
        // When script-src is not specified, default-src should be used
        List<String> warnings = validateWithCsp("default-src 'self'",
                "<script>alert('test');</script>");
        assertContains("default-src fallback for script-src",
                warnings, "Inline script violates");
    }

    // Test infrastructure

    private static class TestLocator implements Locator {
        private int line = 1;
        private int column = 1;

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return "test.html";
        }

        @Override
        public int getLineNumber() {
            return line;
        }

        @Override
        public int getColumnNumber() {
            return column;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }

    private static class TestErrorHandler implements ErrorHandler {
        private final List<String> warnings = new ArrayList<>();

        @Override
        public void warning(SAXParseException exception) {
            warnings.add(exception.getMessage());
        }

        @Override
        public void error(SAXParseException exception) {
            // CSP violations are warnings, not errors
        }

        @Override
        public void fatalError(SAXParseException exception) {
        }

        public List<String> getWarnings() {
            return warnings;
        }
    }

    private static List<String> validateWithCsp(String cspPolicy, String bodyContent)
            throws SAXException {
        CspEnforcementChecker checker = new CspEnforcementChecker();
        TestErrorHandler errorHandler = new TestErrorHandler();
        checker.setErrorHandler(errorHandler);

        // Set document locator
        checker.setDocumentLocator(new TestLocator());

        // Start document
        checker.startDocument();

        // html element
        AttributesImpl htmlAtts = new AttributesImpl();
        htmlAtts.addAttribute("", "lang", "lang", "CDATA", "en");
        checker.startElement(XHTML_NS, "html", "html", htmlAtts);

        // head element
        checker.startElement(XHTML_NS, "head", "head", new AttributesImpl());

        // CSP meta tag
        AttributesImpl metaAtts = new AttributesImpl();
        metaAtts.addAttribute("", "http-equiv", "http-equiv", "CDATA",
                "Content-Security-Policy");
        metaAtts.addAttribute("", "content", "content", "CDATA", cspPolicy);
        checker.startElement(XHTML_NS, "meta", "meta", metaAtts);
        checker.endElement(XHTML_NS, "meta", "meta");

        checker.endElement(XHTML_NS, "head", "head");

        // body element with content
        checker.startElement(XHTML_NS, "body", "body", new AttributesImpl());
        parseBodyContent(checker, bodyContent);
        checker.endElement(XHTML_NS, "body", "body");

        checker.endElement(XHTML_NS, "html", "html");

        // End document triggers validation
        checker.endDocument();

        return errorHandler.getWarnings();
    }

    private static List<String> validateWithoutCsp(String bodyContent)
            throws SAXException {
        CspEnforcementChecker checker = new CspEnforcementChecker();
        TestErrorHandler errorHandler = new TestErrorHandler();
        checker.setErrorHandler(errorHandler);

        // Set document locator
        checker.setDocumentLocator(new TestLocator());

        checker.startDocument();

        AttributesImpl htmlAtts = new AttributesImpl();
        htmlAtts.addAttribute("", "lang", "lang", "CDATA", "en");
        checker.startElement(XHTML_NS, "html", "html", htmlAtts);

        checker.startElement(XHTML_NS, "head", "head", new AttributesImpl());
        checker.endElement(XHTML_NS, "head", "head");

        checker.startElement(XHTML_NS, "body", "body", new AttributesImpl());
        parseBodyContent(checker, bodyContent);
        checker.endElement(XHTML_NS, "body", "body");

        checker.endElement(XHTML_NS, "html", "html");

        checker.endDocument();

        return errorHandler.getWarnings();
    }

    private static void parseBodyContent(CspEnforcementChecker checker,
            String content) throws SAXException {
        // Simple parser for test content
        int pos = 0;
        while (pos < content.length()) {
            int tagStart = content.indexOf('<', pos);
            if (tagStart == -1) {
                // Text content
                String text = content.substring(pos);
                checker.characters(text.toCharArray(), 0, text.length());
                break;
            }
            if (tagStart > pos) {
                // Text before tag
                String text = content.substring(pos, tagStart);
                checker.characters(text.toCharArray(), 0, text.length());
            }

            int tagEnd = content.indexOf('>', tagStart);
            if (tagEnd == -1) break;

            String tagContent = content.substring(tagStart + 1, tagEnd);
            boolean isClosing = tagContent.startsWith("/");
            boolean isSelfClosing = tagContent.endsWith("/");

            if (isClosing) {
                String tagName = tagContent.substring(1).trim();
                checker.endElement(XHTML_NS, tagName, tagName);
            } else {
                // Parse tag name and attributes
                String[] parts = tagContent.split("\\s+", 2);
                String tagName = parts[0];
                if (tagName.endsWith("/")) {
                    tagName = tagName.substring(0, tagName.length() - 1);
                }

                AttributesImpl atts = new AttributesImpl();
                if (parts.length > 1) {
                    String attrsStr = parts[1];
                    if (attrsStr.endsWith("/")) {
                        attrsStr = attrsStr.substring(0, attrsStr.length() - 1);
                    }
                    parseAttributes(attrsStr, atts);
                }

                checker.startElement(XHTML_NS, tagName, tagName, atts);

                // Handle script and style content
                if ("script".equals(tagName) || "style".equals(tagName)) {
                    int closeTag = content.indexOf("</" + tagName + ">", tagEnd);
                    if (closeTag > tagEnd + 1) {
                        String innerContent = content.substring(tagEnd + 1, closeTag);
                        checker.characters(innerContent.toCharArray(), 0,
                                innerContent.length());
                        checker.endElement(XHTML_NS, tagName, tagName);
                        pos = closeTag + tagName.length() + 3;
                        continue;
                    }
                }

                if (isSelfClosing || isVoidElement(tagName)) {
                    checker.endElement(XHTML_NS, tagName, tagName);
                }
            }
            pos = tagEnd + 1;
        }
    }

    private static void parseAttributes(String attrsStr, AttributesImpl atts) {
        // Simple attribute parser
        int pos = 0;
        while (pos < attrsStr.length()) {
            // Skip whitespace
            while (pos < attrsStr.length() &&
                    Character.isWhitespace(attrsStr.charAt(pos))) {
                pos++;
            }
            if (pos >= attrsStr.length()) break;

            // Find attribute name
            int eqPos = attrsStr.indexOf('=', pos);
            if (eqPos == -1) break;

            String name = attrsStr.substring(pos, eqPos).trim();
            pos = eqPos + 1;

            // Skip whitespace after =
            while (pos < attrsStr.length() &&
                    Character.isWhitespace(attrsStr.charAt(pos))) {
                pos++;
            }

            // Get value
            String value;
            if (pos < attrsStr.length() && attrsStr.charAt(pos) == '"') {
                pos++; // skip opening quote
                int endQuote = attrsStr.indexOf('"', pos);
                if (endQuote == -1) break;
                value = attrsStr.substring(pos, endQuote);
                pos = endQuote + 1;
            } else {
                int space = attrsStr.indexOf(' ', pos);
                if (space == -1) space = attrsStr.length();
                value = attrsStr.substring(pos, space);
                pos = space;
            }

            atts.addAttribute("", name, name, "CDATA", value);
        }
    }

    private static boolean isVoidElement(String tagName) {
        return "meta".equals(tagName) || "link".equals(tagName) ||
                "br".equals(tagName) || "hr".equals(tagName) ||
                "img".equals(tagName) || "input".equals(tagName);
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName);
        }
    }

    private static void assertContains(String testName, List<String> warnings,
            String substring) {
        boolean found = false;
        for (String warning : warnings) {
            if (warning.toLowerCase().contains(substring.toLowerCase())) {
                found = true;
                break;
            }
        }
        if (found) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected warning containing: " + substring);
            System.out.println("  Actual warnings: " + warnings);
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
