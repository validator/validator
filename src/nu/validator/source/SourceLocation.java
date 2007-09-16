/*
 * Copyright (c) 2007 Mozilla Foundation
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

package nu.validator.source;

/**
 * Source location with zero-based indexes.
 * 
 * @version $Id$
 * @author hsivonen
 */
final class SourceLocation implements Comparable<SourceLocation>, Cloneable {

    private final SourceCode owner;
    private int line;
    private int column;
    
    /**
     * @param line
     * @param column
     */
    SourceLocation(final SourceCode owner, final int line, final int column) {
        this.owner = owner;
        this.line = line;
        this.column = column;
        if (column < 0) {
            throw new IllegalArgumentException("Column cannot be less than zero.");
        }
        if (line < -1) {
            throw new IllegalArgumentException("Line cannot be less than -1.");            
        }
    }

    public int compareTo(SourceLocation o) {
        if (this.line < o.line) {
            return -1;
        } else if (this.line > o.line) {
            return 1;
        } else {
            if (this.column < o.column) {
                return -1;
            } else if (this.column > o.column) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SourceLocation) {
            SourceLocation loc = (SourceLocation) obj;
            return this.line == loc.line && this.column == loc.column;
        } else {
            return false;
        }
    }

    /**
     * Returns the column.
     * 
     * @return the column
     */
    int getColumn() {
        return column;
    }

    /**
     * Returns the line.
     * 
     * @return the line
     */
    int getLine() {
        return line;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (line << 16) + column;
    }
    

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SourceLocation(owner, line, column);
    }
    
    boolean increment() {
        if (line == -1) {
            column = 0;
            line = 0;
            return true;
        }
        SourceLine sourceLine = owner.getLine(line);
        if (column == sourceLine.getBufferLength()) {
            line++;
            if (line == owner.getNumberOfLines()) {
                line--;
                column = sourceLine.getBufferLength();
                return false;
            } else {
                return true;
            }
        } else {
            column++;
            return true;
        }
    }
    
    boolean decrement() {
        if (line == -1) {
            return false;
        }
        column--;
        if (column == -1) {
            line--;
            column = 0;
        }
        return true;
    }
}
