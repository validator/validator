/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2017 Mozilla Foundation
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

package nu.validator.checker.table;

import java.util.LinkedList;

import nu.validator.checker.AttributeUtil;
import nu.validator.checker.Checker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Checks XHTML table integrity: overlapping cells, spanning past the end 
 * of row group, etc.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class TableChecker extends Checker {

    private static final int MAX_COLSPAN = 1000;

    /**
     * Constructor.
     */
    public TableChecker() {
        super();
    }

    /**
     * Holds the current table. (Premature optimization to avoid 
     * peeking the top of the stack all the time.)
     */
    private Table current;

    /**
     * A stack for holding the tables that are open and ancestors of 
     * the current table. Grows from the tail.
     */
    private final LinkedList<Table> stack = new LinkedList<>();

    /**
     * Pushes the current table onto the stack and creates a new one.
     */
    private void push() {
        if (current != null) {
            stack.addLast(current);
        }
        current = new Table(this);
    }

    /**
     * Ends the current table, discards it and pops the top of the 
     * stack to be the new current table.
     * 
     * @throws SAXException if ending the table throws
     */
    private void pop() throws SAXException {
        if (current == null) {
            throw new IllegalStateException("Bug!");
        }
        current.end();
        if (stack.isEmpty()) {
            current = null;
        } else {
            current = stack.removeLast();
        }
    }

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(uri)) {
            if ("table".equals(localName)) {
                push();
            } else if (current != null) {
                if ("td".equals(localName)) {
                    current.startCell(false, atts);
                } else if ("th".equals(localName)) {
                    current.startCell(true, atts);
                } else if ("tr".equals(localName)) {
                    current.startRow();
                } else if ("tbody".equals(localName)
                        || "thead".equals(localName)
                        || "tfoot".equals(localName)) {
                    current.startRowGroup(localName);
                } else if ("col".equals(localName)) {
                    current.startCol(clampSpan(atts));
                } else if ("colgroup".equals(localName)) {
                    current.startColGroup(clampSpan(atts));
                }
            }
        }
    }

    public int clampSpan(Attributes atts) throws SAXException {
        int span = AttributeUtil.parseNonNegativeInteger(
                atts.getValue("", "span"));
        if (span > MAX_COLSPAN) {
            err("The value of the \u201Cspan\u201D attribute must be less than"
                    + " or equal to " + MAX_COLSPAN + ".");
            span = MAX_COLSPAN;
        }
        return span;
    }

    /**
     * @see nu.validator.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(uri)) {
            if ("table".equals(localName)) {
                pop();
            } else if (current != null) {
                if ("td".equals(localName)) {
                    current.endCell();
                } else if ("th".equals(localName)) {
                    current.endCell();
                } else if ("tr".equals(localName)) {
                    current.endRow();
                } else if ("tbody".equals(localName)
                        || "thead".equals(localName)
                        || "tfoot".equals(localName)) {
                    current.endRowGroup();
                } else if ("col".equals(localName)) {
                    current.endCol();
                } else if ("colgroup".equals(localName)) {
                    current.endColGroup();
                }
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#reset()
     */
    @Override
    public void reset() {
        stack.clear();
        current = null;
    }

}
