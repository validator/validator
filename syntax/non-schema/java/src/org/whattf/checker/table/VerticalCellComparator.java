/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2013 Mozilla Foundation
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

import java.util.Comparator;

/**
 * Compares cells first by their <code>bottom</code> field and then by their 
 * <code>left</code> field. The cells can never be equal unless they are the
 * same object.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class VerticalCellComparator implements Comparator<Cell> {

    public static final VerticalCellComparator THE_INSTANCE = new VerticalCellComparator();
    
    private VerticalCellComparator() {
        super();
    }

    public final int compare(Cell cell0, Cell cell1) {
        if (cell0.getBottom() < cell1.getBottom()) {
            return -1;
        } else if (cell0.getBottom() > cell1.getBottom()) {
            return 1;
        } else if (cell0 == cell1) {
            return 0;
        } else {
            if (cell0.getLeft() < cell1.getLeft()) {
                return -1;
            } else if (cell0.getLeft() > cell1.getLeft()) {
                return 1;
            } else {
                throw new IllegalStateException(
                        "Two cells in effect cannot start on the same column, so this should never happen!!\n"
                                + "cell0 from line "
                                + cell0.getLineNumber()
                                + ", bottom="
                                + cell0.getBottom()
                                + ", left="
                                + cell0.getLeft()
                                + "\n"
                                + "cell1 from line "
                                + cell1.getLineNumber()
                                + ", bottom="
                                + cell1.getBottom()
                                + ", left="
                                + cell1.getLeft());
            }
        }
    }
}
