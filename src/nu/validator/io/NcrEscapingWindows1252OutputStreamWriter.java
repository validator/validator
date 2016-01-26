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

package nu.validator.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

public class NcrEscapingWindows1252OutputStreamWriter extends Writer {

    private static final int SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;

    private static int[] CODE_POINTS = {
        0x0152,
        0x0153,
        0x0160,
        0x0161,
        0x0178,
        0x017D,
        0x017E,
        0x0192,
        0x02C6,
        0x02DC,
        0x2013,
        0x2014,
        0x2018,
        0x2019,
        0x201A,
        0x201C,
        0x201D,
        0x201E,
        0x2020,
        0x2021,
        0x2022,
        0x2026,
        0x2030,
        0x2039,
        0x203A,
        0x20AC,
        0x2122
    };
    
    private static int[] BYTES = {
        0x8C,
        0x9C,
        0x8A,
        0x9A,
        0x9F,
        0x8E,
        0x9E,
        0x83,
        0x88,
        0x98,
        0x96,
        0x97,
        0x91,
        0x92,
        0x82,
        0x93,
        0x94,
        0x84,
        0x86,
        0x87,
        0x95,
        0x85,
        0x89,
        0x8B,
        0x9B,
        0x80,
        0x99  
    };
    
    private OutputStream out;
    
    private int prev;
    
    public NcrEscapingWindows1252OutputStreamWriter(OutputStream out) {
        super();
        this.out = out;
        this.prev = 0;
    }

    @Override
    public void write(char[] buf, int offset, int count) throws IOException {
        int end = offset + count;
        for (int i = offset; i < end; i++) {
            this.write(buf[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * @see java.io.Writer#write(int)
     */
    @Override
    public void write(int c) throws IOException {
        int i = -1;
        c &= 0xFFFF; // per API contract
        if (c < 0x80) {
            out.write(c);
            prev = 0;
            return;
        } else if (c < 0xA0) {
            prev = 0;
            // silent loss
            return;
        } else if (c < 0x100) {
            out.write(c);
            prev = 0;
            return;            
        } else if ((i = Arrays.binarySearch(CODE_POINTS, c)) >= 0) {
            out.write(BYTES[i]);
            prev = 0;
            return;                        
        } if ((c & 0xFC00) == 0xDC00) {
            // Got a low surrogate. See if prev was high surrogate
            if (prev != 0) {
                int intVal = (prev << 10) + c + SURROGATE_OFFSET;
                prev = 0;
                this.writeNcr(intVal);
                return;
            } else {
                prev = 0;
                // silent loss
                return;
            }
        } else if ((c & 0xFC00) == 0xD800) {
            // silent loss if prev already was surrogate
            prev = c;
            return;
        } else {
            this.writeNcr(c);
            prev = 0;
            return;
        }
    }

    private void writeNcr(int c) throws IOException {
        out.write('&');
        out.write('#');
        
        String str = Integer.toString(c);
        for (int i = 0; i < str.length(); i++) {
            out.write(str.charAt(i));
        }
        
        out.write(';');
    }

}
