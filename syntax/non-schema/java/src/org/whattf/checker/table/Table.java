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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.whattf.checker.AttributeUtil;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;


/**
 * Represents an XHTML table for table integrity checking. Handles 
 * table-significant parse events and keeps track of columns.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class Table {

    /**
     * An enumeration for keeping track of the parsing state of a table.
     */
    private enum State {
        
        /**
         * The table element start has been seen. No child elements have been seen. 
         * A start of a column, a column group, a row or a row group or the end of 
         * the table is expected.
         */
        IN_TABLE_AT_START,
        
        /**
         * The table element is the open element and rows have been seen. A row in 
         * an implicit group, a row group or the end of the table is expected.
         */
        IN_TABLE_AT_POTENTIAL_ROW_GROUP_START,
        
        /**
         * A column group is open. It can end or a column can start.
         */
        IN_COLGROUP,
        
        /**
         * A column inside a column group is open. It can end.
         */
        IN_COL_IN_COLGROUP,
        
        /**
         * A column that is a child of table is open. It can end.
         */
        IN_COL_IN_IMPLICIT_GROUP,
        
        /**
         * The open element is an explicit row group. It may end or a row may start.
         */
        IN_ROW_GROUP,
        
        /**
         * A row in a an explicit row group is open. It may end or a cell may start.
         */
        IN_ROW_IN_ROW_GROUP,
        
        /**
         * A cell inside a row inside an explicit row group is open. It can end.
         */
        IN_CELL_IN_ROW_GROUP,
        
        /**
         * A row in an implicit row group is open. It may end or a cell may start.
         */
        IN_ROW_IN_IMPLICIT_ROW_GROUP,
        
        /**
         * The table itself is the currently open element, but an implicit row group 
         * been started by previous rows. A row may start, an explicit row group may 
         * start or the table may end. 
         */
        IN_IMPLICIT_ROW_GROUP,
        
        /**
         * A cell inside an implicit row group is open. It can close.
         */
        IN_CELL_IN_IMPLICIT_ROW_GROUP,
        
        /**
         * The table itself is the currently open element. Columns and/or column groups 
         * have been seen but rows or row groups have not been seen yet. A column, a 
         * column group, a row or a row group can start. The table can end.
         */
        IN_TABLE_COLS_SEEN
    }
    
    /**
     * Keeps track of the handler state between SAX events.
     */
    private State state = State.IN_TABLE_AT_START;

    /**
     * The number of suppressed element starts.
     */
    private int suppressedStarts = 0;

    /**
     * Indicates whether the width of the table was established by column markup.
     */
    private boolean hardWidth = false;

    /**
     * The column count established by column markup or by the first row.
     */
    private int columnCount = -1;
    
    /**
     * The actual column count as stretched by the widest row.
     */
    private int realColumnCount = 0;

    /**
     * A colgroup span that hasn't been actuated yet in case the element has 
     * col children. The absolute value counts. The negative sign means that 
     * the value was implied.
     */
    private int pendingColGroupSpan = 0;

    /**
     * A set of the IDs of header cells.
     */
    private final Set<String> headerIds = new HashSet<String>();

    /**
     * A list of cells that refer to headers (in the document order).
     */
    private final List<Cell> cellsReferringToHeaders = new LinkedList<Cell>();

    /**
     * The owning checker.
     */
    private final TableChecker owner;

    /**
     * The current row group (also implicit groups have an explicit object).
     */
    private RowGroup current;

    /**
     * The head of the column range list.
     */
    private ColumnRange first = null;

    /**
     * The tail of the column range list.
     */
    private ColumnRange last = null;

    /**
     * The range under inspection.
     */
    private ColumnRange currentColRange = null;

    /**
     * The previous range that was inspected.
     */
    private ColumnRange previousColRange = null;

    /**
     * Constructor.
     * @param owner reference back to the checker
     */
    public Table(TableChecker owner) {
        super();
        this.owner = owner;
    }

    private boolean needSuppressStart() {
        if (suppressedStarts > 0) {
            suppressedStarts++;
            return true;
        } else {
            return false;
        }
    }

    private boolean needSuppressEnd() {
        if (suppressedStarts > 0) {
            suppressedStarts--;
            return true;
        } else {
            return false;
        }
    }

    void startRowGroup(String type) throws SAXException {
        if (needSuppressStart()) {
            return;
        }
        switch (state) {
            case IN_IMPLICIT_ROW_GROUP:
                current.end();
            // fall through
            case IN_TABLE_AT_START:
            case IN_TABLE_COLS_SEEN:
            case IN_TABLE_AT_POTENTIAL_ROW_GROUP_START:
                current = new RowGroup(this, type);
                state = State.IN_ROW_GROUP;
                break;
            default:
                suppressedStarts = 1;
                break;
        }
    }

    void endRowGroup() throws SAXException {
        if (needSuppressEnd()) {
            return;
        }
        switch (state) {
            case IN_ROW_GROUP:
                current.end();
                current = null;
                state = State.IN_TABLE_AT_POTENTIAL_ROW_GROUP_START;
                break;
            default:
                throw new IllegalStateException("Bug!");
        }
    }

    void startRow() {
        if (needSuppressStart()) {
            return;
        }
        switch (state) {
            case IN_TABLE_AT_START:
            case IN_TABLE_COLS_SEEN:
            case IN_TABLE_AT_POTENTIAL_ROW_GROUP_START:
                current = new RowGroup(this, null);
                // fall through
            case IN_IMPLICIT_ROW_GROUP:
                state = State.IN_ROW_IN_IMPLICIT_ROW_GROUP;
                break;
            case IN_ROW_GROUP:
                state = State.IN_ROW_IN_ROW_GROUP;
                break;
            default:
                suppressedStarts = 1;
                return;
        }
        currentColRange = first;
        previousColRange = null;
        current.startRow();
    }

    void endRow() throws SAXException {
        if (needSuppressEnd()) {
            return;
        }
        switch (state) {
            case IN_ROW_IN_ROW_GROUP:
                state = State.IN_ROW_GROUP;
                break;
            case IN_ROW_IN_IMPLICIT_ROW_GROUP:
                state = State.IN_IMPLICIT_ROW_GROUP;
                break;
            default:
                throw new IllegalStateException("Bug!");
        }
        current.endRow();
    }

    void startCell(boolean header, Attributes attributes) throws SAXException {
        if (needSuppressStart()) {
            return;
        }
        switch (state) {
            case IN_ROW_IN_ROW_GROUP:
                state = State.IN_CELL_IN_ROW_GROUP;
                break;
            case IN_ROW_IN_IMPLICIT_ROW_GROUP:
                state = State.IN_CELL_IN_IMPLICIT_ROW_GROUP;
                break;
            default:
                suppressedStarts = 1;
                return;
        }
        if (header) {
            int len = attributes.getLength();
            for (int i = 0; i < len; i++) {
                if ("ID".equals(attributes.getType(i))) {
                    String val = attributes.getValue(i);
                    if (!"".equals(val)) {
                        headerIds.add(val);
                    }
                }
            }
        }
        String[] headers = AttributeUtil.split(attributes.getValue("",
                "headers"));
        Cell cell = new Cell(
                Math.abs(AttributeUtil.parsePositiveInteger(attributes.getValue(
                        "", "colspan"))),
                Math.abs(AttributeUtil.parseNonNegativeInteger(attributes.getValue(
                        "", "rowspan"))), headers, header,
                owner.getDocumentLocator(), owner.getErrorHandler());
        if (headers.length > 0) {
            cellsReferringToHeaders.add(cell);
        }
        current.cell(cell);
    }

    void endCell() {
        if (needSuppressEnd()) {
            return;
        }
        switch (state) {
            case IN_CELL_IN_ROW_GROUP:
                state = State.IN_ROW_IN_ROW_GROUP;
                break;
            case IN_CELL_IN_IMPLICIT_ROW_GROUP:
                state = State.IN_ROW_IN_IMPLICIT_ROW_GROUP;
                break;
            default:
                throw new IllegalStateException("Bug!");
        }
    }

    void startColGroup(int span) {
        if (needSuppressStart()) {
            return;
        }
        switch (state) {
            case IN_TABLE_AT_START:
                hardWidth = true;
                columnCount = 0;
            // fall through
            case IN_TABLE_COLS_SEEN:
                pendingColGroupSpan = span;
                state = State.IN_COLGROUP;
                break;
            default:
                suppressedStarts = 1;
                break;
        }
    }

    void endColGroup() {
        if (needSuppressEnd()) {
            return;
        }
        switch (state) {
            case IN_COLGROUP:
                if (pendingColGroupSpan != 0) {
                    int right = columnCount + Math.abs(pendingColGroupSpan);
                    Locator locator = new LocatorImpl(
                            owner.getDocumentLocator());
                    ColumnRange colRange = new ColumnRange("colgroup", locator,
                            columnCount, right);
                    appendColumnRange(colRange);
                    columnCount = right;
                }
                realColumnCount = columnCount;
                state = State.IN_TABLE_COLS_SEEN;
                break;
            default:
                throw new IllegalStateException("Bug!");
        }
    }

    void startCol(int span) throws SAXException {
        if (needSuppressStart()) {
            return;
        }
        switch (state) {
            case IN_TABLE_AT_START:
                hardWidth = true;
                columnCount = 0;
            // fall through
            case IN_TABLE_COLS_SEEN:
                state = State.IN_COL_IN_IMPLICIT_GROUP;
                break;
            case IN_COLGROUP:
                if (pendingColGroupSpan > 0) {
                    warn("A col element causes a span attribute with value "
                            + pendingColGroupSpan
                            + " to be ignored on the parent colgroup.");
                }
                pendingColGroupSpan = 0;
                state = State.IN_COL_IN_COLGROUP;
                break;
            default:
                suppressedStarts = 1;
                return;
        }
        int right = columnCount + Math.abs(span);
        Locator locator = new LocatorImpl(owner.getDocumentLocator());
        ColumnRange colRange = new ColumnRange("col", locator,
                columnCount, right);
        appendColumnRange(colRange);
        columnCount = right;
        realColumnCount = columnCount;
    }

    /**
     * Appends a column range to the linked list of column ranges.
     * 
     * @param colRange the range to append
     */
    private void appendColumnRange(ColumnRange colRange) {
        if (last == null) {
            first = colRange;
            last = colRange;
        } else {
            last.setNext(colRange);
            last = colRange;
        }
    }

    void warn(String message) throws SAXException {
        owner.warn(message);
    }

    void err(String message) throws SAXException {
        owner.err(message);
    }

    void endCol() {
        if (needSuppressEnd()) {
            return;
        }
        switch (state) {
            case IN_COL_IN_IMPLICIT_GROUP:
                state = State.IN_TABLE_COLS_SEEN;
                break;
            case IN_COL_IN_COLGROUP:
                state = State.IN_COLGROUP;
                break;
            default:
                throw new IllegalStateException("Bug!");
        }
    }

    void end() throws SAXException {
        switch (state) {
            case IN_IMPLICIT_ROW_GROUP:
                current.end();
                current = null;
                break;
            case IN_TABLE_AT_START:
            case IN_TABLE_AT_POTENTIAL_ROW_GROUP_START:
            case IN_TABLE_COLS_SEEN:
                break;
            default:
                throw new IllegalStateException("Bug!");
        }

        // Check referential integrity
        for (Iterator<Cell> iter = cellsReferringToHeaders.iterator(); iter.hasNext();) {
            Cell cell = iter.next();
            String[] headings = cell.getHeadings();
            for (int i = 0; i < headings.length; i++) {
                String heading = headings[i];
                if (!headerIds.contains(heading)) {
                    cell.err("The \u201Cheaders\u201D attribute on the element \u201C"
                            + cell.elementName()
                            + "\u201D refers to the ID \u201C"
                            + heading
                            + "\u201D, but there is no \u201Cth\u201D element with that ID in the same table.");
                }
            }
        }

        // Check that each column has non-extended cells
        ColumnRange colRange = first;
        while (colRange != null) {
            if (colRange.isSingleCol()) {
                owner.getErrorHandler().error(
                        new SAXParseException("Table column " + colRange
                                + " established by element \u201C"
                                + colRange.getElement()
                                + "\u201D has no cells beginning in it.",
                                colRange.getLocator()));
            } else {
                owner.getErrorHandler().error(
                        new SAXParseException("Table columns in range "
                                + colRange + " established by element \u201C"
                                + colRange.getElement()
                                + "\u201D have no cells beginning in them.",
                                colRange.getLocator()));
            }
            colRange = colRange.getNext();
        }
    }

    /**
     * Returns the columnCount.
     * 
     * @return the columnCount
     */
    int getColumnCount() {
        return columnCount;
    }

    /**
     * Sets the columnCount.
     * 
     * @param columnCount
     *            the columnCount to set
     */
    void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Returns the hardWidth.
     * 
     * @return the hardWidth
     */
    boolean isHardWidth() {
        return hardWidth;
    }
    
    /**
     * Reports a cell whose positioning has been decided back to the table 
     * so that column bookkeeping can be done. (Called from 
     * <code>RowGroup</code>--not <code>TableChecker</code>.)
     * 
     * @param cell a cell whose position has been calculated
     */
    void cell(Cell cell) {
        int left = cell.getLeft();
        int right = cell.getRight();
        // first see if we've got a cell past the last col
        if (right > realColumnCount) {
            // are we past last col entirely?
            if (left == realColumnCount) {
                // single col?
                if (left + 1 != right) {
                    appendColumnRange(new ColumnRange(cell.elementName(), cell, left + 1, right));
                }
                realColumnCount = right;
                return;
            } else {
                // not past entirely
                appendColumnRange(new ColumnRange(cell.elementName(), cell, realColumnCount, right));                
                realColumnCount = right;
            }
        }
        while (currentColRange != null) {
            int hit = currentColRange.hits(left);
            if (hit == 0) {
                ColumnRange newRange = currentColRange.removeColumn(left);
                if (newRange == null) {
                    // zap a list item
                    if (previousColRange != null) {
                        previousColRange.setNext(currentColRange.getNext());
                    }
                    if (first == currentColRange) {
                        first = currentColRange.getNext();
                    }
                    if (last == currentColRange) {
                        last = previousColRange;
                    }
                    currentColRange = currentColRange.getNext();
                } else {
                    if (last == currentColRange) {
                        last = newRange;
                    }
                    currentColRange = newRange;
                }
                return;
            } else if (hit == -1) {
                return;
            } else if (hit == 1) {
                previousColRange = currentColRange;
                currentColRange = currentColRange.getNext();                                
            }
        }
    }
}
