/*
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.svgresearch;

public class SortStruct implements Comparable<SortStruct>{
    private final int count;
    private final String label;
    /**
     * @param count
     * @param label
     */
    public SortStruct(int count, String label) {
        this.count = count;
        this.label = label;
    }
    /**
     * Returns the count.
     * 
     * @return the count
     */
    public int getCount() {
        return count;
    }
    /**
     * Returns the label.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    public int compareTo(SortStruct other) {
        if (this.count < other.count) {
            return 1;
        } else if (this.count > other.count) {
            return -1;
        } else {
            return this.label.compareTo(other.label);
        }
    }
}
