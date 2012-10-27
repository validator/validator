/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2012 Mozilla Foundation
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

package org.whattf.datatype;

import java.util.regex.Pattern;

public class TimeDatetime extends AbstractDatetime {
    /**
     * The singleton instance.
     */
    public static final TimeDatetime THE_INSTANCE = new TimeDatetime();

    public int i;

    /**
     * The rexexp for this datatype.
     *
     * See also AbstractDatetime#checkValid, which references the capturing
     * groups in this regexp.
     *
     * Capturing groups:
     *
     *  valid month string
     *    1 = year
     *    2 = month
     *
     *  valid date string
     *    3 = year
     *    4 = month
     *    5 = day
     *
     *  valid yearless date string
     *    6 = month
     *    7 = day
     *
     *  valid time string
     *    8 = hours
     *    9 = minutes
     *   10 = seconds
     *   11 = milliseconds
     *
     *  valid local date and time string
     *   12 = year
     *   13 = month
     *   14 = day
     *   15 = hours
     *   16 = minutes
     *   17 = seconds
     *   18 = milliseconds
     *
     *  valid time-zone offset string
     *   19 = timezone hours
     *   20 = timezone minutes
     *
     *  valid global date and time string
     *   21 = year
     *   22 = month
     *   23 = day
     *   24 = hours
     *   25 = minutes
     *   26 = seconds
     *   27 = milliseconds
     *   28 = timezone hours
     *   29 = timezone minutes
     *
     *  valid week string
     *   30 = year
     *   31 = week
     *
     *  valid year (valid non-negative integer)
     *   32 = year
     *
     *  valid duration string
     *   33 = milliseconds
     *   34 = milliseconds
     *
     */
    private static final Pattern THE_PATTERN = Pattern.compile("^[ \\t\\r\\n\\f]*(?:(?:([0-9]{4,})-([0-9]{2}))|(?:([0-9]{4,})-([0-9]{2})-([0-9]{2}))|(?:([0-9]{2})-([0-9]{2}))|(?:([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]+))?)?)|(?:([0-9]{4,})-([0-9]{2})-([0-9]{2})(?:T| )([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]+))?)?)|(?:Z|(?:[+-]([0-9]{2}):?([0-9]{2})))|(?:([0-9]{4,})-([0-9]{2})-([0-9]{2})(?:T| )([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]+))?)?(?:Z|(?:[+-]([0-9]{2}):?([0-9]{2}))))|(?:([0-9]{4,})-W([0-9]{2}))|(?:([0-9]{4,}))|(?:P(?:(?:[0-9]+D)|(?:(?:[0-9]+D)?T[0-9]+H)|(?:(?:[0-9]+D)?T(?:[0-9]+H)?[0-9]+M)|(?:(?:[0-9]+D)?T(?:(?:[0-9]+)H)?(?:(?:[0-9]+)M)?(?:[0-9]+(?:\\.([0-9]+))?S))))|(?:[ \\t\\r\\n\\f]*[0-9]+(?:(?:[ \\t\\r\\n\\f]*(?:[Ww]|[Dd]|[Hh]|[Mm]))|(?:(?:\\.([0-9]+))?[ \\t\\r\\n\\f]*[Ss])))+)[ \\t\\r\\n\\f]*$");
    private TimeDatetime() {
        super();
    }

    @Override
    protected Pattern getPattern() {
        return THE_PATTERN;
    }

    @Override
    public String getName() {
        return "time-datetime";
    }

}
