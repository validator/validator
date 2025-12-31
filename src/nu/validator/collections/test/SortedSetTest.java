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

package nu.validator.collections.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import nu.validator.collections.HeadBiasedSortedSet;
import nu.validator.collections.TailBiasedSortedSet;

/**
 * Unit tests for HeadBiasedSortedSet and TailBiasedSortedSet.
 *
 * These custom SortedSet implementations are used for performance-optimized
 * sorted collections in the checker. These tests verify correct sorting,
 * iteration, and edge case handling.
 */
public class SortedSetTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Testing HeadBiasedSortedSet...");
        testHeadBiasedBasicOperations();
        testHeadBiasedSorting();
        testHeadBiasedDuplicates();
        testHeadBiasedEmptySet();
        testHeadBiasedWithComparator();
        testHeadBiasedFromCollection();
        testHeadBiasedFromSortedSet();
        testHeadBiasedClear();
        testHeadBiasedIterator();

        System.out.println();
        System.out.println("Testing TailBiasedSortedSet...");
        testTailBiasedBasicOperations();
        testTailBiasedSorting();
        testTailBiasedDuplicates();
        testTailBiasedEmptySet();
        testTailBiasedWithComparator();
        testTailBiasedFromCollection();
        testTailBiasedFromSortedSet();
        testTailBiasedClear();
        testTailBiasedIterator();

        System.out.println();
        System.out.println("Testing equivalence between implementations...");
        testEquivalentBehavior();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // HeadBiasedSortedSet tests

    private static void testHeadBiasedBasicOperations() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        assertTrue("HeadBiased: empty set has size 0", set.size() == 0);
        assertTrue("HeadBiased: empty set isEmpty", set.isEmpty());

        set.add(5);
        assertTrue("HeadBiased: size after add is 1", set.size() == 1);
        assertFalse("HeadBiased: not empty after add", set.isEmpty());
        assertTrue("HeadBiased: contains added element", set.contains(5));

        set.add(3);
        set.add(7);
        assertTrue("HeadBiased: size after adding 3 elements is 3", set.size() == 3);
    }

    private static void testHeadBiasedSorting() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        set.add(5);
        set.add(1);
        set.add(9);
        set.add(3);
        set.add(7);

        List<Integer> result = new ArrayList<>();
        for (Integer i : set) {
            result.add(i);
        }
        List<Integer> expected = Arrays.asList(1, 3, 5, 7, 9);
        assertTrue("HeadBiased: elements are sorted", result.equals(expected));
        assertTrue("HeadBiased: first() returns smallest", set.first().equals(1));
        assertTrue("HeadBiased: last() returns largest", set.last().equals(9));
    }

    private static void testHeadBiasedDuplicates() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        assertTrue("HeadBiased: first add returns true", set.add(5));
        assertFalse("HeadBiased: duplicate add returns false", set.add(5));
        assertTrue("HeadBiased: size is still 1 after duplicate", set.size() == 1);
    }

    private static void testHeadBiasedEmptySet() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        try {
            set.first();
            fail("HeadBiased: first() on empty set should throw");
        } catch (NoSuchElementException e) {
            pass("HeadBiased: first() on empty set throws NoSuchElementException");
        }
        try {
            set.last();
            fail("HeadBiased: last() on empty set should throw");
        } catch (NoSuchElementException e) {
            pass("HeadBiased: last() on empty set throws NoSuchElementException");
        }
    }

    private static void testHeadBiasedWithComparator() {
        Comparator<String> lengthComparator = (a, b) -> Integer.compare(a.length(), b.length());
        HeadBiasedSortedSet<String> set = new HeadBiasedSortedSet<>(lengthComparator);
        set.add("aaa");
        set.add("a");
        set.add("aa");

        List<String> result = new ArrayList<>();
        for (String s : set) {
            result.add(s);
        }
        assertTrue("HeadBiased: custom comparator sorts by length",
                result.get(0).equals("a") && result.get(1).equals("aa")
                        && result.get(2).equals("aaa"));
        assertTrue("HeadBiased: comparator() returns the comparator",
                set.comparator() == lengthComparator);
    }

    private static void testHeadBiasedFromCollection() {
        List<Integer> list = Arrays.asList(5, 1, 9, 3, 7);
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>(list);
        assertTrue("HeadBiased: from collection has correct size", set.size() == 5);
        assertTrue("HeadBiased: from collection first is 1", set.first().equals(1));
        assertTrue("HeadBiased: from collection last is 9", set.last().equals(9));
    }

    private static void testHeadBiasedFromSortedSet() {
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(5, 1, 9, 3, 7));
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>(treeSet);
        assertTrue("HeadBiased: from SortedSet has correct size", set.size() == 5);
        assertTrue("HeadBiased: from SortedSet maintains order", set.first().equals(1));
    }

    private static void testHeadBiasedClear() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        set.clear();
        assertTrue("HeadBiased: size is 0 after clear", set.size() == 0);
        assertTrue("HeadBiased: isEmpty after clear", set.isEmpty());
    }

    private static void testHeadBiasedIterator() {
        HeadBiasedSortedSet<Integer> set = new HeadBiasedSortedSet<>();
        set.add(1);
        set.add(2);

        Iterator<Integer> iter = set.iterator();
        assertTrue("HeadBiased: iterator hasNext on non-empty", iter.hasNext());
        iter.next();
        iter.next();
        assertFalse("HeadBiased: iterator hasNext is false at end", iter.hasNext());

        try {
            iter.next();
            fail("HeadBiased: iterator next() at end should throw");
        } catch (NoSuchElementException e) {
            pass("HeadBiased: iterator next() at end throws NoSuchElementException");
        }

        try {
            iter.remove();
            fail("HeadBiased: iterator remove() should throw");
        } catch (UnsupportedOperationException e) {
            pass("HeadBiased: iterator remove() throws UnsupportedOperationException");
        }
    }

    // TailBiasedSortedSet tests

    private static void testTailBiasedBasicOperations() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        assertTrue("TailBiased: empty set has size 0", set.size() == 0);
        assertTrue("TailBiased: empty set isEmpty", set.isEmpty());

        set.add(5);
        assertTrue("TailBiased: size after add is 1", set.size() == 1);
        assertFalse("TailBiased: not empty after add", set.isEmpty());
        assertTrue("TailBiased: contains added element", set.contains(5));

        set.add(3);
        set.add(7);
        assertTrue("TailBiased: size after adding 3 elements is 3", set.size() == 3);
    }

    private static void testTailBiasedSorting() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        set.add(5);
        set.add(1);
        set.add(9);
        set.add(3);
        set.add(7);

        List<Integer> result = new ArrayList<>();
        for (Integer i : set) {
            result.add(i);
        }
        List<Integer> expected = Arrays.asList(1, 3, 5, 7, 9);
        assertTrue("TailBiased: elements are sorted", result.equals(expected));
        assertTrue("TailBiased: first() returns smallest", set.first().equals(1));
        assertTrue("TailBiased: last() returns largest", set.last().equals(9));
    }

    private static void testTailBiasedDuplicates() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        assertTrue("TailBiased: first add returns true", set.add(5));
        assertFalse("TailBiased: duplicate add returns false", set.add(5));
        assertTrue("TailBiased: size is still 1 after duplicate", set.size() == 1);
    }

    private static void testTailBiasedEmptySet() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        try {
            set.first();
            fail("TailBiased: first() on empty set should throw");
        } catch (NoSuchElementException e) {
            pass("TailBiased: first() on empty set throws NoSuchElementException");
        }
        try {
            set.last();
            fail("TailBiased: last() on empty set should throw");
        } catch (NoSuchElementException e) {
            pass("TailBiased: last() on empty set throws NoSuchElementException");
        }
    }

    private static void testTailBiasedWithComparator() {
        Comparator<String> lengthComparator = (a, b) -> Integer.compare(a.length(), b.length());
        TailBiasedSortedSet<String> set = new TailBiasedSortedSet<>(lengthComparator);
        set.add("aaa");
        set.add("a");
        set.add("aa");

        List<String> result = new ArrayList<>();
        for (String s : set) {
            result.add(s);
        }
        assertTrue("TailBiased: custom comparator sorts by length",
                result.get(0).equals("a") && result.get(1).equals("aa")
                        && result.get(2).equals("aaa"));
        assertTrue("TailBiased: comparator() returns the comparator",
                set.comparator() == lengthComparator);
    }

    private static void testTailBiasedFromCollection() {
        List<Integer> list = Arrays.asList(5, 1, 9, 3, 7);
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>(list);
        assertTrue("TailBiased: from collection has correct size", set.size() == 5);
        assertTrue("TailBiased: from collection first is 1", set.first().equals(1));
        assertTrue("TailBiased: from collection last is 9", set.last().equals(9));
    }

    private static void testTailBiasedFromSortedSet() {
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(5, 1, 9, 3, 7));
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>(treeSet);
        assertTrue("TailBiased: from SortedSet has correct size", set.size() == 5);
        assertTrue("TailBiased: from SortedSet maintains order", set.first().equals(1));
    }

    private static void testTailBiasedClear() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        set.clear();
        assertTrue("TailBiased: size is 0 after clear", set.size() == 0);
        assertTrue("TailBiased: isEmpty after clear", set.isEmpty());
    }

    private static void testTailBiasedIterator() {
        TailBiasedSortedSet<Integer> set = new TailBiasedSortedSet<>();
        set.add(1);
        set.add(2);

        Iterator<Integer> iter = set.iterator();
        assertTrue("TailBiased: iterator hasNext on non-empty", iter.hasNext());
        iter.next();
        iter.next();
        assertFalse("TailBiased: iterator hasNext is false at end", iter.hasNext());

        try {
            iter.next();
            fail("TailBiased: iterator next() at end should throw");
        } catch (NoSuchElementException e) {
            pass("TailBiased: iterator next() at end throws NoSuchElementException");
        }

        try {
            iter.remove();
            fail("TailBiased: iterator remove() should throw");
        } catch (UnsupportedOperationException e) {
            pass("TailBiased: iterator remove() throws UnsupportedOperationException");
        }
    }

    // Test equivalence between implementations

    private static void testEquivalentBehavior() {
        HeadBiasedSortedSet<Integer> headSet = new HeadBiasedSortedSet<>();
        TailBiasedSortedSet<Integer> tailSet = new TailBiasedSortedSet<>();

        // Add elements in different orders
        int[] elements = { 50, 10, 90, 30, 70, 20, 80, 40, 60 };
        for (int e : elements) {
            headSet.add(e);
            tailSet.add(e);
        }

        // Both should produce the same sorted output
        List<Integer> headResult = new ArrayList<>();
        List<Integer> tailResult = new ArrayList<>();
        for (Integer i : headSet) {
            headResult.add(i);
        }
        for (Integer i : tailSet) {
            tailResult.add(i);
        }

        assertTrue("Equivalence: both sets produce same sorted order",
                headResult.equals(tailResult));
        assertTrue("Equivalence: same size", headSet.size() == tailSet.size());
        assertTrue("Equivalence: same first", headSet.first().equals(tailSet.first()));
        assertTrue("Equivalence: same last", headSet.last().equals(tailSet.last()));
    }

    // Test helpers

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

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }

    private static void fail(String testName) {
        System.out.println("FAIL: " + testName);
        failed++;
    }
}
