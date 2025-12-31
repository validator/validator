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

import org.xml.sax.SAXException;

import nu.validator.checker.NormalizationChecker;

/**
 * Unit tests for NormalizationChecker.startsWithComposingChar().
 *
 * Tests detection of Unicode composing characters (characters with
 * NFC_Quick_Check=maybe or non-zero canonical combining class).
 */
public class NormalizationCheckerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing startsWithComposingChar...");
        testEmptyString();
        testAsciiCharacters();
        testNonAsciiBaseCharacters();
        testCombiningCharacters();
        testSurrogatePairs();
        testMalformedSurrogates();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyString() {
        assertFalse("empty string", "");
    }

    private static void testAsciiCharacters() {
        assertFalse("ASCII letter 'a'", "a");
        assertFalse("ASCII letter 'Z'", "Z");
        assertFalse("ASCII digit '0'", "0");
        assertFalse("ASCII space", " ");
        assertFalse("ASCII punctuation", "!");
        assertFalse("ASCII word", "hello");
    }

    private static void testNonAsciiBaseCharacters() {
        // Regular base characters from various scripts
        assertFalse("Latin e-acute (precomposed)", "\u00e9"); // e with acute
        assertFalse("Greek alpha", "\u03b1");
        assertFalse("Cyrillic a", "\u0430");
        assertFalse("CJK character", "\u4e00");
        assertFalse("Arabic alef", "\u0627");
        assertFalse("Hebrew aleph", "\u05d0");
        assertFalse("Thai ko kai", "\u0e01");
        assertFalse("Devanagari ka", "\u0915");
    }

    private static void testCombiningCharacters() {
        // Combining diacritical marks (canonical combining class > 0)
        assertTrue("Combining acute accent (U+0301)", "\u0301");
        assertTrue("Combining grave accent (U+0300)", "\u0300");
        assertTrue("Combining diaeresis (U+0308)", "\u0308");
        assertTrue("Combining tilde (U+0303)", "\u0303");
        assertTrue("Combining cedilla (U+0327)", "\u0327");
        assertTrue("Combining macron (U+0304)", "\u0304");

        // Combining character followed by other text
        assertTrue("Combining acute then 'a'", "\u0301a");
        assertTrue("Combining diaeresis then text", "\u0308hello");

        // Hebrew combining marks
        assertTrue("Hebrew point patah (U+05B7)", "\u05b7");

        // Arabic combining marks
        assertTrue("Arabic fathah (U+064E)", "\u064e");
    }

    private static void testSurrogatePairs() {
        // Musical symbols that are combining characters (in SMP)
        // U+1D165 MUSICAL SYMBOL COMBINING STEM (surrogate pair: D834 DD65)
        assertTrue("Musical combining stem", "\uD834\uDD65");

        // U+1D16D MUSICAL SYMBOL COMBINING AUGMENTATION DOT
        assertTrue("Musical combining augmentation dot", "\uD834\uDD6D");

        // Regular SMP characters that are NOT combining
        // U+1F600 GRINNING FACE (emoji, not a combining character)
        assertFalse("Emoji grinning face", "\uD83D\uDE00");

        // U+10000 LINEAR B SYLLABLE B008 A (first SMP character)
        assertFalse("Linear B syllable", "\uD800\uDC00");
    }

    private static void testMalformedSurrogates() {
        // High surrogate alone should throw
        assertThrowsSAXException("Lone high surrogate", "\uD800");

        // High surrogate followed by non-surrogate should throw
        assertThrowsSAXException("High surrogate + non-surrogate", "\uD800a");
    }

    // Test helper methods

    private static void assertTrue(String testName, String input) {
        try {
            boolean result = NormalizationChecker.startsWithComposingChar(input);
            if (result) {
                pass("startsWithComposingChar: " + testName);
            } else {
                System.out.println("FAIL: startsWithComposingChar: " + testName);
                System.out.println("  Expected: true");
                System.out.println("  Actual: false");
                failed++;
            }
        } catch (SAXException e) {
            System.out.println("FAIL: startsWithComposingChar: " + testName);
            System.out.println("  Expected: true");
            System.out.println("  Got exception: " + e.getMessage());
            failed++;
        }
    }

    private static void assertFalse(String testName, String input) {
        try {
            boolean result = NormalizationChecker.startsWithComposingChar(input);
            if (!result) {
                pass("startsWithComposingChar: " + testName);
            } else {
                System.out.println("FAIL: startsWithComposingChar: " + testName);
                System.out.println("  Expected: false");
                System.out.println("  Actual: true");
                failed++;
            }
        } catch (SAXException e) {
            System.out.println("FAIL: startsWithComposingChar: " + testName);
            System.out.println("  Expected: false");
            System.out.println("  Got exception: " + e.getMessage());
            failed++;
        }
    }

    private static void assertThrowsSAXException(String testName, String input) {
        try {
            NormalizationChecker.startsWithComposingChar(input);
            System.out.println("FAIL: startsWithComposingChar: " + testName);
            System.out.println("  Expected: SAXException");
            System.out.println("  Got: no exception");
            failed++;
        } catch (SAXException e) {
            pass("startsWithComposingChar: " + testName + " (throws SAXException)");
        }
    }

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }
}
