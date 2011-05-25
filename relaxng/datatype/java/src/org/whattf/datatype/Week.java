/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2011 Mozilla Foundation
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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;

/**
 * This datatype shall accept strings that conform to the format of 
 * <a href='http://www.whatwg.org/specs/web-apps/current-work/#valid-week-string'>valid week string</a>.
 * <p>This datatype must not accept the empty string.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Week extends AbstractDatatype {

    /**
     * These years in the 400-year cycle have 53 weeks. The table was computed
     * using the formula given by Dr J R Stockton on 
     * http://www.merlyn.demon.co.uk/weekinfo.htm and was checked to match the
     * data on Wikipedia. However, computing the special years in years 
     * 0...400 failed using the JS Date object. This could mean the formula
     * is wrong, the JS Date object doesn't implement the proleptic Gregorian 
     * calendar for past dates or that ISO weeks aren't actually cyclical.
     */
    private static final int[] SPECIAL_YEARS = { 4, 9, 15, 20, 26, 32, 37, 43,
            48, 54, 60, 65, 71, 76, 82, 88, 93, 99, 105, 111, 116, 122, 128,
            133, 139, 144, 150, 156, 161, 167, 172, 178, 184, 189, 195, 201,
            207, 212, 218, 224, 229, 235, 240, 246, 252, 257, 263, 268, 274,
            280, 285, 291, 296, 303, 308, 314, 320, 325, 331, 336, 342, 348,
            353, 359, 364, 370, 376, 381, 387, 392, 398 };

    /**
     * The singleton instance.
     */
    public static final Week THE_INSTANCE = new Week();
    
    /**
     * The rexexp for this datatype.
     */
    private static final Pattern THE_PATTERN = Pattern.compile("^([0-9]{4,})-W([0-9]{2})$");

    /**
     * Constructor.
     */
    private Week() {
        super();
    }

    private void checkWeek(String year, String week)
            throws DatatypeException {
        checkWeek(Integer.parseInt(year), Integer.parseInt(week));
    }

    private void checkWeek(int year, int week)
            throws DatatypeException {
        if (year < 1) {
            throw newDatatypeException("Year cannot be less than 1.");
        }
        if (week < 1) {
            throw newDatatypeException("Week cannot be less than 1.");
        }
        if (week == 53) {
            if (Arrays.binarySearch(SPECIAL_YEARS, year % 400) < 0) {
                throw newDatatypeException("Week out of range.");                            
            }
        } else if (week > 53) {
            throw newDatatypeException("Week out of range.");            
        }
    }

    public final void checkValid(CharSequence literal)
            throws DatatypeException {
        Matcher m = THE_PATTERN.matcher(literal);
        if (m.matches()) {
            checkWeek(m.group(1), m.group(2));
        } else {
            throw newDatatypeException(
                    "The literal did not satisfy the format for week.");
        }
    }

    @Override
    public String getName() {
        return "week";
    }

}
