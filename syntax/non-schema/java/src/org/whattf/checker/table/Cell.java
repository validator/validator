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

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A table cell for table integrity checking.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class Cell implements Locator {

    // See
    // http://mxr-test.landfill.bugzilla.org/mxr-test/seamonkey/source/content/html/content/src/nsHTMLTableCellElement.cpp
    // for the source of these magic numbers.

    /**
     * Magic number from Gecko.
     */
    private static final int MAX_COLSPAN = 1000;

    /**
     * Magic number from Gecko.
     */
    private static final int MAX_ROWSPAN = 8190;

    /**
     * The column in which this cell starts. (Zero before positioning.)
     */
    private int left;

    /**
     * The first row in the row group onto which this cell does not span.
     * (rowspan before positioning)
     * 
     * <p>However, <code>Integen.MAX_VALUE</code> is a magic value that means 
     * <code>rowspan=0</code>.
     */
    private int bottom;

    /**
     * The first column into which this cell does not span. 
     * (colspan before positioning.)
     */
    private int right;

    /**
     * The value of the <code>headers</code> attribute split on white space.
     */
    private final String[] headers;

    /**
     * Whether this is a <code>th</code> cell.
     */
    private final boolean header;

    /**
     * Source column.
     */
    private final int columnNumber;

    /**
     * Source line.
     */
    private final int lineNumber;

    /**
     * Source public id.
     */
    private final String publicId;

    /**
     * Source system id.
     */
    private final String systemId;

    /**
     * The error handler.
     */
    private final ErrorHandler errorHandler;

    Cell(int colspan, int rowspan, String[] headers, boolean header,
            Locator locator, ErrorHandler errorHandler) throws SAXException {
        super();
        this.errorHandler = errorHandler;
        if (locator == null) {
            this.columnNumber = -1;
            this.lineNumber = -1;
            this.publicId = null;
            this.systemId = null;
        } else {
            this.columnNumber = locator.getColumnNumber();
            this.lineNumber = locator.getLineNumber();
            this.publicId = locator.getPublicId();
            this.systemId = locator.getSystemId();
        }
        if (rowspan > MAX_ROWSPAN) {
            warn("A rowspan attribute has the value " + rowspan
                    + ", which exceeds the magic Gecko limit of " + MAX_ROWSPAN
                    + ".");
        }
        if (colspan > MAX_COLSPAN) {
            warn("A colspan attribute has the value " + colspan
                    + ", which exceeds the magic browser limit of "
                    + MAX_COLSPAN + ".");
        }
        if (rowspan == Integer.MAX_VALUE) {
            throw new SAXException(
                    "Implementation limit reached. Table row counter overflowed.");
        }
        this.left = 0;
        this.right = colspan;
        this.bottom = (rowspan == 0 ? Integer.MAX_VALUE : rowspan);
        this.headers = headers;
        this.header = header;
    }

    /**
     * Returns the headers.
     * 
     * @return the headers
     */
    public String[] getHeadings() {
        return headers;
    }

    /**
     * Returns the header.
     * 
     * @return the header
     */
    public boolean isHeader() {
        return header;
    }

    public void warn(String message) throws SAXException {
        if (errorHandler != null) {
            errorHandler.warning(new SAXParseException(message, publicId,
                    systemId, lineNumber, columnNumber));
        }
    }

    public void err(String message) throws SAXException {
        if (errorHandler != null) {
            errorHandler.error(new SAXParseException(message, publicId,
                    systemId, lineNumber, columnNumber));
        }
    }

    /**
     * Emit errors if this cell and the argument overlap horizontally.
     * @param laterCell another cell
     * @throws SAXException if the <code>ErrorHandler</code> throws
     */
    public void errOnHorizontalOverlap(Cell laterCell) throws SAXException {
        if (!((laterCell.right <= left) || (right <= laterCell.left))) {
            this.err("Table cell is overlapped by later table cell.");
            laterCell.err("Table cell overlaps an earlier table cell.");
        }
    }

    public void setPosition(int top, int left) throws SAXException {
        this.left = left;
        this.right += left;
        if (this.right < 1) {
            throw new SAXException(
                    "Implementation limit reached. Table column counter overflowed.");
        }
        if (this.bottom != Integer.MAX_VALUE) {
            this.bottom += top;
            if (this.bottom < 1) {
                throw new SAXException(
                        "Implementation limit reached. Table row counter overflowed.");
            }
        }
    }

    public boolean shouldBeCulled(int row) {
        return row >= bottom;
    }

    public int freeSlot(int potentialSlot) {
        if (potentialSlot < left || potentialSlot >= right) {
            return potentialSlot;
        } else {
            return right;
        }
    }

    /**
     * Returns the bottom.
     * 
     * @return the bottom
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * Returns the left.
     * 
     * @return the left
     */
    int getLeft() {
        return left;
    }

    /**
     * Returns the right.
     * 
     * @return the right
     */
    int getRight() {
        return right;
    }

    public void errIfNotRowspanZero(String rowGroupType) throws SAXException {
        if (this.bottom != Integer.MAX_VALUE) {
            err("Table cell spans past the end of its "
                    + (rowGroupType == null ? "implicit row group"
                            : "row group established by a \u201C" + rowGroupType
                                    + "\u201D element")
                    + "; clipped to the end of the row group.");
        }
    }

    /**
     * Returns the columnNumber.
     * 
     * @return the columnNumber
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Returns the lineNumber.
     * 
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the publicId.
     * 
     * @return the publicId
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * Returns the systemId.
     * 
     * @return the systemId
     */
    public String getSystemId() {
        return systemId;
    }

    public String elementName() {
        return header ? "th" : "td";
    }

}
