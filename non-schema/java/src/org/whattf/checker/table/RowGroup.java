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

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xml.sax.SAXException;

/**
 * Represents a row group (explicit or implicit) for table integrity checking.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class RowGroup {

    /**
     * Runtime type constant.
     */
    private final Cell[] EMPTY_CELL_ARRAY = {};

    /**
     * Keeps track of the current slot row of the insertion point.
     */
    private int currentRow = -1;

    /**
     * The column slot of the insertion point.
     */
    private int insertionPoint = 0;

    /**
     * The index of the next uninspected item in <code>cellsOnCurrentRow</code>.
     */
    private int nextOldCell = 0;

    /**
     * The owning table.
     */
    private final Table owner;

    /**
     * The set of cells from previous rows that are still in effect extending
     * downwards.
     */
    private final SortedSet<Cell> cellsIfEffect = new TreeSet<Cell>(
            VerticalCellComparator.THE_INSTANCE);

    /**
     * A temporary copy of <code>cellsIfEffect</code> sorted differently.
     */
    private Cell[] cellsOnCurrentRow;

    /**
     * Whether the current row has had cells.
     */
    private boolean rowHadCells;

    /**
     * The local name of the element that established this row group or
     * <code>null</code> if this is an implicit row group.
     */
    private final String type;

    RowGroup(Table owner, String type) {
        super();
        this.owner = owner;
        this.type = type;
    }

    public void cell(Cell cell) throws SAXException {
        rowHadCells = true;
        findInsertionPoint();
        cell.setPosition(currentRow, insertionPoint);
        owner.cell(cell);
        if (cell.getBottom() > currentRow + 1) {
            cellsIfEffect.add(cell);
        }
        insertionPoint = cell.getRight();
        for (int i = nextOldCell; i < cellsOnCurrentRow.length; i++) {
            cellsOnCurrentRow[i].errOnHorizontalOverlap(cell);
        }
    }

    /**
     * 
     */
    private void findInsertionPoint() {
        for (;;) {
            if (nextOldCell == cellsOnCurrentRow.length) {
                break;
            }
            Cell other = cellsOnCurrentRow[nextOldCell];
            int newInsertionPoint = other.freeSlot(insertionPoint);
            if (newInsertionPoint == insertionPoint) {
                break;
            }
            nextOldCell++;
            insertionPoint = newInsertionPoint;
        }
    }

    public void end() throws SAXException {
        for (Cell cell : cellsIfEffect) {
            cell.errIfNotRowspanZero(type);
        }
    }

    public void endRow() throws SAXException {
        if (!rowHadCells) {
            owner.err("Row "
                    + (currentRow + 1)
                    + " of "
                    + (type == null ? "an implicit row group"
                            : "a row group established by a \u201C" + type
                                    + "\u201D element")
                    + " has no cells beginning on it.");
        }

        findInsertionPoint();
        cellsOnCurrentRow = null;

        int columnCount = owner.getColumnCount();
        if (owner.isHardWidth()) {
            if (insertionPoint > columnCount) {
                owner.err("A table row was "
                        + insertionPoint
                        + " columns wide and exceeded the column count established using column markup ("
                        + columnCount + ").");
            } else if (insertionPoint < columnCount) {
                owner.err("A table row was "
                        + insertionPoint
                        + " columns wide, which is less than the column count established using column markup ("
                        + columnCount + ").");
            }
        } else if (columnCount == -1) {
            // just saw the first row
            owner.setColumnCount(insertionPoint);
        } else {
            if (insertionPoint > columnCount) {
                owner.warn("A table row was "
                        + insertionPoint
                        + " columns wide and exceeded the column count established by the first row ("
                        + columnCount + ").");
            } else if (insertionPoint < columnCount) {
                owner.warn("A table row was "
                        + insertionPoint
                        + " columns wide, which is less than the column count established by the first row ("
                        + columnCount + ").");
            }
        }

        // Get rid of cells that don't span to the next row
        for (Iterator<Cell> iter = cellsIfEffect.iterator(); iter.hasNext();) {
            Cell cell = iter.next();
            if (cell.shouldBeCulled(currentRow + 1)) {
                iter.remove();
            }
        }
    }

    public void startRow() {
        currentRow++;
        insertionPoint = 0;
        nextOldCell = 0;
        rowHadCells = false;
        cellsOnCurrentRow = cellsIfEffect.toArray(EMPTY_CELL_ARRAY);
        // the array should already be in the right order most of the time
        Arrays.sort(cellsOnCurrentRow, HorizontalCellComparator.THE_INSTANCE);
    }

}
