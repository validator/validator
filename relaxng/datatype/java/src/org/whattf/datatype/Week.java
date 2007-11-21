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

package org.whattf.datatype;

import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

/**
 * This datatype shall accept strings that conform to the format specified for 
 * <a href='http://whatwg.org/specs/web-forms/current-work/#week'><code>week</code></a> 
 * inputs in Web Forms 2.0.
 * <p>This datatype must not accept the empty string.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Week extends AbstractDatatype {

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
            // TODO still in doubt about the concurrency contract
            // not using instance variables just in case

            // Can this year have ISO week #53?
            // Using ICU4J to find out, because the calculation is
            // non-trivial.
            TimeZone tz = TimeZone.getTimeZone("GMT");
            tz.setRawOffset(0);
            // Using fixed time zone and locale to make possible 
            // bugs more tractable.
            GregorianCalendar gc = new GregorianCalendar(tz, Locale.FRANCE);
            // Say no to the Julian calendar
            gc.setGregorianChange(new Date(Long.MIN_VALUE));
            gc.setLenient(false);
            gc.setFirstDayOfWeek(Calendar.MONDAY); // ISO week start
            gc.setMinimalDaysInFirstWeek(4); // ISO week rule
            gc.set(year, 6, 1);
            if (gc.getActualMaximum(Calendar.WEEK_OF_YEAR) != 53) {
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
