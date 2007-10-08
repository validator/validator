/*
 * Copyright (c) 2006 Henri Sivonen
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

package org.whattf.checker.table;

import org.xml.sax.Locator;

/**
 * Represents a contiguous range of columns that was established by a single 
 * element and that does not yet have cells in it.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class ColumnRange {

    /**
     * The locator associated with the element that established this column range.
     */
    private final Locator locator;

    /**
     * The local name of the element that established this column range.
     */
    private final String element;

    /**
     * The leftmost column that is part of this range.
     */
    private int left;

    /**
     * The first column to the right that is not part of this range.
     */
    private int right;

    /**
     * The next range in the linked list of ranges.
     */
    private ColumnRange next;

    /**
     * Constructor
     * @param element the local name of the establishing element
     * @param locator a locator associated with the establishing element; 
     * <em>must be suitable for retaining out-of-SAX-event!</em>
     * @param left the leftmost column that is part of this range
     * @param right the first column to the right that is not part of this range
     */
    public ColumnRange(String element, Locator locator, int left, int right) {
        super();
        assert right > left;
        this.element = element;
        this.locator = locator;
        this.left = left;
        this.right = right;
        this.next = null;
    }

    /**
     * Returns the element.
     * 
     * @return the element
     */
    String getElement() {
        return element;
    }

    /**
     * Returns the locator.
     * 
     * @return the locator
     */
    Locator getLocator() {
        return locator;
    }

    /**
     * Hit testing.
     * @param column column index
     * @return -1 if the column is to the left of this range, 
     * 0 if the column is in this range and 
     * 1 if the column is to the right of this range
     */
    int hits(int column) {
        if (column < left) {
            return -1;
        } if (column >= right) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Removes a column from the range possibly asking it to be destroyed or 
     * splitting it.
     * @param column a column index
     * @return <code>null</code> if this range gets destroyed, 
     * <code>this</code> if the range gets resized and
     * the new right half range if the range gets split
     */
    ColumnRange removeColumn(int column) {
        // first, let's see if this is a 1-column range that should 
        // be destroyed
        if (isSingleCol()) {
            return null;
        } else if (column == left) {
            left++;
            return this;
        } else if (column + 1 == right) {
            right--;
            return this;
        } else {
            ColumnRange created = new ColumnRange(this.element, this.locator,
                    column + 1, this.right);
            created.next = this.next;
            this.next = created;
            this.right = column;
            return created;
        }
    }

    /**
     * Returns the next.
     * 
     * @return the next
     */
    ColumnRange getNext() {
        return next;
    }

    /**
     * Sets the next.
     * 
     * @param next the next to set
     */
    void setNext(ColumnRange next) {
        this.next = next;
    }

    boolean isSingleCol() {
        return left + 1 == right;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (isSingleCol()) {
            return Integer.toString(right);
        } else {
            return (left + 1) + "\u2026" + (right);
        }
    }

}
