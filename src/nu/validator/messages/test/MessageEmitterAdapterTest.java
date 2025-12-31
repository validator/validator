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

package nu.validator.messages.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import nu.validator.messages.MessageEmitterAdapter;

import nu.validator.vendor.thaiopensource.relaxng.exceptions.BadAttributeValueException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.ImpossibleAttributeIgnoredException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.OnlyTextNotAllowedException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.OutOfContextElementException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.RequiredAttributesMissingException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.RequiredAttributesMissingOneOfException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.RequiredElementsMissingException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.RequiredElementsMissingOneOfException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.StringNotAllowedException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.TextNotAllowedException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.UnfinishedElementException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.UnfinishedElementOneOfException;
import nu.validator.vendor.thaiopensource.relaxng.exceptions.UnknownElementException;
import nu.validator.vendor.thaiopensource.xml.util.Name;

/**
 * Tests for MessageEmitterAdapter display message generation.
 *
 * These tests verify that filter patterns can match the displayed error
 * messages (what users see), not just the internal exception messages.
 *
 * See: https://github.com/validator/validator/issues/1070
 */
public class MessageEmitterAdapterTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Testing RequiredElementsMissing...");
        testRequiredElementsMissing();
        testRequiredElementsMissingWithName();

        System.out.println();
        System.out.println("Testing RequiredElementsMissingOneOf...");
        testRequiredElementsMissingOneOf();

        System.out.println();
        System.out.println("Testing UnfinishedElement...");
        testUnfinishedElement();
        testUnfinishedElementWithName();

        System.out.println();
        System.out.println("Testing UnfinishedElementOneOf...");
        testUnfinishedElementOneOf();

        System.out.println();
        System.out.println("Testing ImpossibleAttributeIgnored...");
        testImpossibleAttributeIgnored();

        System.out.println();
        System.out.println("Testing OutOfContextElement...");
        testOutOfContextElement();

        System.out.println();
        System.out.println("Testing UnknownElement...");
        testUnknownElement();

        System.out.println();
        System.out.println("Testing TextNotAllowed...");
        testTextNotAllowed();

        System.out.println();
        System.out.println("Testing OnlyTextNotAllowed...");
        testOnlyTextNotAllowed();

        System.out.println();
        System.out.println("Testing RequiredAttributesMissing...");
        testRequiredAttributesMissing();

        System.out.println();
        System.out.println("Testing RequiredAttributesMissingOneOf...");
        testRequiredAttributesMissingOneOf();

        System.out.println();
        System.out.println("Testing BadAttributeValue...");
        testBadAttributeValue();

        System.out.println();
        System.out.println("Testing StringNotAllowed...");
        testStringNotAllowed();

        System.out.println();
        System.out.println("Testing filter pattern matching...");
        testFilterPatternMatching();

        System.out.println();
        System.out.println("Results: " + passed + " passed, "
                + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testRequiredElementsMissing() throws Exception {
        // Test: Element "head" is missing a required child element
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        RequiredElementsMissingException ex = new RequiredElementsMissingException(
                null, head, null, head);

        String displayMsg = getDisplayMessage(ex);

        assertContains("RequiredElementsMissing (no name)", displayMsg,
                "is missing a required child element");
    }

    private static void testRequiredElementsMissingWithName() throws Exception {
        // Test: Element "head" is missing a required instance of child element "title"
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        RequiredElementsMissingException ex = new RequiredElementsMissingException(
                null, head, "title", head);

        String displayMsg = getDisplayMessage(ex);

        assertContains("RequiredElementsMissing (with name)", displayMsg,
                "is missing a required instance of child element");
        assertContains("RequiredElementsMissing (with name)", displayMsg,
                "title");
    }

    private static void testUnfinishedElement() throws Exception {
        // Test: Element "head" is missing a required child element
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        UnfinishedElementException ex = new UnfinishedElementException(
                null, head, null, null);

        String displayMsg = getDisplayMessage(ex);

        assertContains("UnfinishedElement (no name)", displayMsg,
                "is missing a required child element");
    }

    private static void testUnfinishedElementWithName() throws Exception {
        // Test: Element "head" is missing a required instance of child element "title"
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        UnfinishedElementException ex = new UnfinishedElementException(
                null, head, "title", null);

        String displayMsg = getDisplayMessage(ex);

        assertContains("UnfinishedElement (with name)", displayMsg,
                "is missing a required instance of child element");
        assertContains("UnfinishedElement (with name)", displayMsg,
                "title");
    }

    private static void testImpossibleAttributeIgnored() throws Exception {
        // Test: Attribute "foo" not allowed on element "div" at this point
        Name div = new Name("http://www.w3.org/1999/xhtml", "div");
        Name foo = new Name("", "foo");
        ImpossibleAttributeIgnoredException ex = new ImpossibleAttributeIgnoredException(
                null, div, null, foo);

        String displayMsg = getDisplayMessage(ex);

        assertContains("ImpossibleAttributeIgnored", displayMsg,
                "not allowed on element");
        assertContains("ImpossibleAttributeIgnored", displayMsg,
                "at this point");
    }

    private static void testRequiredElementsMissingOneOf() throws Exception {
        // Test: Element is missing one of required child elements
        Name select = new Name("http://www.w3.org/1999/xhtml", "select");
        Name body = new Name("http://www.w3.org/1999/xhtml", "body");
        Set<String> missingElements = new HashSet<>();
        missingElements.add("option");
        missingElements.add("optgroup");
        RequiredElementsMissingOneOfException ex = new RequiredElementsMissingOneOfException(
                null, select, missingElements, body);

        String displayMsg = getDisplayMessage(ex);

        assertContains("RequiredElementsMissingOneOf", displayMsg,
                "is missing a required instance of one or more of the following child elements");
    }

    private static void testUnfinishedElementOneOf() throws Exception {
        // Test: Element is missing one of required child elements (at end)
        Name select = new Name("http://www.w3.org/1999/xhtml", "select");
        Name body = new Name("http://www.w3.org/1999/xhtml", "body");
        Set<String> missingElements = new HashSet<>();
        missingElements.add("option");
        missingElements.add("optgroup");
        UnfinishedElementOneOfException ex = new UnfinishedElementOneOfException(
                null, select, missingElements, body);

        String displayMsg = getDisplayMessage(ex);

        assertContains("UnfinishedElementOneOf", displayMsg,
                "is missing a required instance of one or more of the following child elements");
    }

    private static void testOutOfContextElement() throws Exception {
        // Test: Element "div" not allowed as child of element "head"
        Name div = new Name("http://www.w3.org/1999/xhtml", "div");
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        OutOfContextElementException ex = new OutOfContextElementException(
                null, div, head);

        String displayMsg = getDisplayMessage(ex);

        assertContains("OutOfContextElement (contains element)", displayMsg,
                "not allowed as child of");
    }

    private static void testUnknownElement() throws Exception {
        // Test: Element "foo" not allowed
        Name foo = new Name("http://www.w3.org/1999/xhtml", "foo");
        Name body = new Name("http://www.w3.org/1999/xhtml", "body");
        UnknownElementException ex = new UnknownElementException(
                null, foo, body);

        String displayMsg = getDisplayMessage(ex);

        assertContains("UnknownElement", displayMsg,
                "not allowed");
    }

    private static void testTextNotAllowed() throws Exception {
        // Test: Text not allowed in element "select"
        Name select = new Name("http://www.w3.org/1999/xhtml", "select");
        TextNotAllowedException ex = new TextNotAllowedException(
                null, select);

        String displayMsg = getDisplayMessage(ex);

        assertContains("TextNotAllowed", displayMsg,
                "Text not allowed in");
    }

    private static void testOnlyTextNotAllowed() throws Exception {
        // Test: Text not allowed in element at this point
        Name div = new Name("http://www.w3.org/1999/xhtml", "div");
        Name body = new Name("http://www.w3.org/1999/xhtml", "body");
        OnlyTextNotAllowedException ex = new OnlyTextNotAllowedException(
                null, div, body);

        String displayMsg = getDisplayMessage(ex);

        // The message says element "is not allowed to have content that consists solely of text"
        assertContains("OnlyTextNotAllowed", displayMsg,
                "is not allowed to have content that consists solely of text");
    }

    private static void testRequiredAttributesMissing() throws Exception {
        // Test: Element "img" is missing required attribute "src"
        Name img = new Name("http://www.w3.org/1999/xhtml", "img");
        Name body = new Name("http://www.w3.org/1999/xhtml", "body");
        RequiredAttributesMissingException ex = new RequiredAttributesMissingException(
                null, img, "src", body);

        String displayMsg = getDisplayMessage(ex);

        assertContains("RequiredAttributesMissing", displayMsg,
                "is missing required attribute");
        assertContains("RequiredAttributesMissing (attr name)", displayMsg,
                "src");
    }

    private static void testRequiredAttributesMissingOneOf() throws Exception {
        // Test: Element is missing one of required attributes
        Name input = new Name("http://www.w3.org/1999/xhtml", "input");
        Name form = new Name("http://www.w3.org/1999/xhtml", "form");
        Set<String> missingAttrs = new HashSet<>();
        missingAttrs.add("name");
        missingAttrs.add("id");
        RequiredAttributesMissingOneOfException ex = new RequiredAttributesMissingOneOfException(
                null, input, missingAttrs, form);

        String displayMsg = getDisplayMessage(ex);

        assertContains("RequiredAttributesMissingOneOf", displayMsg,
                "is missing one or more of the following attributes");
    }

    private static void testBadAttributeValue() throws Exception {
        // Test: Bad value for attribute
        Name input = new Name("http://www.w3.org/1999/xhtml", "input");
        Name form = new Name("http://www.w3.org/1999/xhtml", "form");
        Name type = new Name("", "type");
        Map<String, String> exceptions = new HashMap<>();
        BadAttributeValueException ex = new BadAttributeValueException(
                null, input, form, type, "foobar", exceptions);

        String displayMsg = getDisplayMessage(ex);

        assertContains("BadAttributeValue", displayMsg,
                "Bad value");
    }

    private static void testStringNotAllowed() throws Exception {
        // Test: Bad character content
        Name option = new Name("http://www.w3.org/1999/xhtml", "option");
        Name select = new Name("http://www.w3.org/1999/xhtml", "select");
        Map<String, String> exceptions = new HashMap<>();
        StringNotAllowedException ex = new StringNotAllowedException(
                null, option, select, "invalid content", exceptions);

        String displayMsg = getDisplayMessage(ex);

        // StringNotAllowedException is for datatype validation on text content
        // The message should mention bad content or not allowed
        assertNotNull("StringNotAllowed has display message", displayMsg);
    }

    private static void testFilterPatternMatching() throws Exception {
        // Test that typical filter patterns would match display messages
        Name head = new Name("http://www.w3.org/1999/xhtml", "head");
        RequiredElementsMissingException ex = new RequiredElementsMissingException(
                null, head, "title", head);

        String displayMsg = getDisplayMessage(ex);

        // This is the pattern users would write based on what they see
        Pattern userPattern = Pattern.compile(
                ".*missing a required instance of child element.*");

        assertTrue("Filter pattern matches display message",
                userPattern.matcher(displayMsg).matches());

        // The OLD internal message pattern should NOT match the display message
        Pattern oldInternalPattern = Pattern.compile(
                ".*is missing required child element.*");

        assertFalse("Old internal pattern should NOT match display message",
                oldInternalPattern.matcher(displayMsg).matches());
    }

    // Helper to call the package-private getDisplayMessage via reflection
    private static String getDisplayMessage(Exception ex) throws Exception {
        MessageEmitterAdapter adapter = new MessageEmitterAdapter();
        java.lang.reflect.Method method = MessageEmitterAdapter.class
                .getDeclaredMethod("getDisplayMessage", Exception.class);
        method.setAccessible(true);
        return (String) method.invoke(adapter, ex);
    }

    private static void assertContains(String testName, String actual,
            String expected) {
        if (actual != null && actual.contains(expected)) {
            System.out.println("PASS: " + testName);
            passed++;
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected to contain: " + expected);
            System.out.println("  Actual: " + actual);
            failed++;
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passed++;
        } else {
            System.out.println("FAIL: " + testName);
            failed++;
        }
    }

    private static void assertFalse(String testName, boolean condition) {
        if (!condition) {
            System.out.println("PASS: " + testName);
            passed++;
        } else {
            System.out.println("FAIL: " + testName);
            failed++;
        }
    }

    private static void assertNotNull(String testName, Object value) {
        if (value != null) {
            System.out.println("PASS: " + testName);
            passed++;
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected: non-null");
            System.out.println("  Actual: null");
            failed++;
        }
    }
}
