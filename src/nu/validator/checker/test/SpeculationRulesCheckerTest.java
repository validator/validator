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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import nu.validator.checker.SpeculationRulesChecker;

/**
 * Unit tests for SpeculationRulesChecker JSON validation.
 *
 * The SpeculationRulesChecker validates the content of script elements with
 * type="speculationrules" according to the HTML specification. These tests
 * verify correct validation of various speculation rules JSON structures.
 *
 * See: https://github.com/validator/validator/issues/2014 (optional source property)
 * See: https://github.com/validator/validator/issues/2006 (forbidden attributes)
 */
public class SpeculationRulesCheckerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Testing valid speculation rules...");
        testValidListRule();
        testValidDocumentRule();
        testValidBothTypes();
        testValidWithEagerness();
        testValidNestedPredicates();
        testValidInferredSourceFromUrls();
        testValidInferredSourceFromWhere();

        System.out.println();
        System.out.println("Testing invalid speculation rules...");
        testInvalidJson();
        testNotJsonObject();
        testMissingPrefetchOrPrerender();
        testInvalidProperty();
        testInvalidSourceValue();
        testListRuleMissingUrls();
        testDocumentRuleMissingWhere();
        testListRuleWithWhere();
        testDocumentRuleWithUrls();
        testInvalidEagerness();
        testEmptyUrlsArray();
        testMissingSourceAndUrlsAndWhere();

        System.out.println();
        System.out.println("Testing document rule predicates...");
        testEmptyPredicate();
        testMultiplePredicates();
        testEmptyAndArray();
        testHrefMatchesNotString();
        testSelectorMatchesEmpty();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // Valid speculation rules tests

    private static void testValidListRule() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": [\"/page1.html\"]}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid list rule: no errors", errors.isEmpty());
    }

    private static void testValidDocumentRule() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"href_matches\": \"/*\"}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid document rule: no errors", errors.isEmpty());
    }

    private static void testValidBothTypes() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": [\"/a.html\"]}]," +
                "\"prerender\": [{\"source\": \"list\", \"urls\": [\"/b.html\"]}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid both types: no errors", errors.isEmpty());
    }

    private static void testValidWithEagerness() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": [\"/page.html\"], \"eagerness\": \"moderate\"}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid with eagerness: no errors", errors.isEmpty());
    }

    private static void testValidNestedPredicates() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": " +
                "{\"and\": [{\"href_matches\": \"/*\"}, {\"not\": {\"selector_matches\": \".no-prerender\"}}]}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid nested predicates: no errors", errors.isEmpty());
    }

    private static void testValidInferredSourceFromUrls() throws Exception {
        // Per issue #2014, source is optional and can be inferred from urls
        String json = "{\"prefetch\": [{\"urls\": [\"/page.html\"]}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid inferred source from urls: no errors", errors.isEmpty());
    }

    private static void testValidInferredSourceFromWhere() throws Exception {
        // Per issue #2014, source is optional and can be inferred from where
        String json = "{\"prerender\": [{\"where\": {\"href_matches\": \"/*\"}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertTrue("Valid inferred source from where: no errors", errors.isEmpty());
    }

    // Invalid speculation rules tests

    private static void testInvalidJson() throws Exception {
        String json = "not valid json";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Invalid JSON: error about valid JSON", errors, "valid JSON");
    }

    private static void testNotJsonObject() throws Exception {
        String json = "[1, 2, 3]";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Not JSON object: error about JSON object", errors, "JSON object");
    }

    private static void testMissingPrefetchOrPrerender() throws Exception {
        String json = "{}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Missing prefetch/prerender: error about properties", errors, "prefetch");
    }

    private static void testInvalidProperty() throws Exception {
        String json = "{\"prefetch\": [], \"unknown\": true}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Invalid property: error about only prefetch/prerender", errors, "only");
    }

    private static void testInvalidSourceValue() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"invalid\", \"urls\": [\"/page.html\"]}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Invalid source value: error about list or document", errors, "list");
    }

    private static void testListRuleMissingUrls() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\"}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("List rule missing urls: error about urls property", errors, "urls");
    }

    private static void testDocumentRuleMissingWhere() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\"}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Document rule missing where: error about where property", errors, "where");
    }

    private static void testListRuleWithWhere() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": [\"/a.html\"], \"where\": {}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("List rule with where: error about where not allowed", errors, "where");
    }

    private static void testDocumentRuleWithUrls() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"href_matches\": \"/*\"}, \"urls\": []}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Document rule with urls: error about urls not allowed", errors, "urls");
    }

    private static void testInvalidEagerness() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": [\"/a.html\"], \"eagerness\": \"invalid\"}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Invalid eagerness: error about valid values", errors, "eager");
    }

    private static void testEmptyUrlsArray() throws Exception {
        String json = "{\"prefetch\": [{\"source\": \"list\", \"urls\": []}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Empty urls array: error about at least one URL", errors, "at least one");
    }

    private static void testMissingSourceAndUrlsAndWhere() throws Exception {
        String json = "{\"prefetch\": [{}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Missing source, urls, where: error", errors, "source");
    }

    // Document rule predicate tests

    private static void testEmptyPredicate() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Empty predicate: error about predicate properties", errors, "predicate");
    }

    private static void testMultiplePredicates() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"href_matches\": \"/*\", \"selector_matches\": \"a\"}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Multiple predicates: error about only one", errors, "only one");
    }

    private static void testEmptyAndArray() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"and\": []}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("Empty and array: error about at least one item", errors, "at least one");
    }

    private static void testHrefMatchesNotString() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"href_matches\": 123}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("href_matches not string: error about string", errors, "string");
    }

    private static void testSelectorMatchesEmpty() throws Exception {
        String json = "{\"prerender\": [{\"source\": \"document\", \"where\": {\"selector_matches\": \"\"}}]}";
        List<String> errors = validateSpeculationRules(json);
        assertContains("selector_matches empty: error about non-empty", errors, "non-empty");
    }

    // Test infrastructure

    /**
     * An ErrorHandler that collects error messages for testing.
     */
    private static class TestErrorHandler implements ErrorHandler {
        private final List<String> errors = new ArrayList<>();

        @Override
        public void warning(SAXParseException exception) {
            errors.add(exception.getMessage());
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add(exception.getMessage());
        }

        @Override
        public void fatalError(SAXParseException exception) {
            errors.add(exception.getMessage());
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    private static List<String> validateSpeculationRules(String json) throws SAXException {
        SpeculationRulesChecker checker = new SpeculationRulesChecker();
        TestErrorHandler errorHandler = new TestErrorHandler();
        checker.setErrorHandler(errorHandler);

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "type", "type", "CDATA", "speculationrules");

        // Simulate parsing a script element with speculationrules type
        checker.startElement("http://www.w3.org/1999/xhtml", "script", "script", atts);
        checker.characters(json.toCharArray(), 0, json.length());
        checker.endElement("http://www.w3.org/1999/xhtml", "script", "script");

        return errorHandler.getErrors();
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName);
        }
    }

    private static void assertContains(String testName, List<String> errors, String substring) {
        boolean found = false;
        for (String error : errors) {
            if (error.toLowerCase().contains(substring.toLowerCase())) {
                found = true;
                break;
            }
        }
        if (found) {
            pass(testName);
        } else {
            System.out.println("FAIL: " + testName);
            System.out.println("  Expected error containing: " + substring);
            System.out.println("  Actual errors: " + errors);
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
