/*
 * Copyright (c) 2007-2017 Mozilla Foundation
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
 * Immutable source location with zero-based indexes. 
 * Cannot point to a line break.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Location implements Comparable<Location>, Cloneable {
    
    private final SourceCode owner;
    private final int line;
    private final int column;
    
    /**
     * @param line
     * @param column
     */
    Location(final SourceCode owner, int line, int column) {
        this.owner = owner;
        if (line < 0) {
            line = 0;
            column = 0;
        } else if (column < 0) {
            line--;
            if (line < 0) {
                line = 0;
                column = 0;                
            } else {
                try {
                    column = owner.getLine(line).getBufferLength();
                } catch (IndexOutOfBoundsException e) {
                    column = 0;
                }
            }
        }
        this.line = line;
        this.column = column;
    }

    @Override
    public int compareTo(Location o) {
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
        if (obj instanceof Location) {
            Location loc = (Location) obj;
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
    public int getColumn() {
        return column;
    }

    /**
     * Returns the line.
     * 
     * @return the line
     */
    public int getLine() {
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
    
    Location next() {
        return step(1);
    }
    
    Location prev() {
        return step(-1);
    }
    
    Location step(int offset) {
        int newLine = line;
        int newColumn = column;
        if (offset > 0) {
            for (int i = 0; i < offset; i++) {
                if (newLine == owner.getNumberOfLines()) {
                    break;
                }
                newColumn++;
                Line sourceLine = owner.getLine(newLine);
                if (newColumn > sourceLine.getBufferLength()) {
                    newLine++;
                    newColumn = 0;
                }
            }
            return new Location(owner, newLine, newColumn);
        } if (offset < 0) {
            offset = -offset;
            for (int i = 0; i < offset; i++) {
                if (newLine == 0 && newColumn == 0) {
                    break;
                }
                newColumn--;
                if (newColumn == -1) {
                    newLine--;
                    newColumn = owner.getLine(newLine).getBufferLength();
                }
            }            
            return new Location(owner, newLine, newColumn);
        } else {
            return this;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return line + ", " + column;
    }
}
