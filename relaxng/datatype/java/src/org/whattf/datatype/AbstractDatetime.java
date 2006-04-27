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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

/**
 * Superclass for various datetime datatypes. 
 * 
 * @version $Id$
 * @author hsivonen
 */
abstract class AbstractDatetime extends AbstractDatatype {
    /**
     * Days in monts on non-leap years.
     */
    private static int[] DAYS_IN_MONTHS = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
            31, 30, 31 };

    /**
     * Package-private constructor.
     */
    AbstractDatetime() {
        super();
    }

    private void checkDate(String year, String month, String day)
            throws DatatypeException {
        checkDate(Integer.parseInt(year), Integer.parseInt(month),
                Integer.parseInt(day));
    }

    private void checkDate(int year, int month, int day)
            throws DatatypeException {
        if (year < 1) {
            throw new DatatypeException("Year cannot be less than 1.");
        }
        if (month < 1) {
            throw new DatatypeException("Month cannot be less than 1.");
        }
        if (month > 12) {
            throw new DatatypeException("Month cannot be greater than 12.");
        }
        if (day < 1) {
            throw new DatatypeException("Day cannot be less than 1.");
        }
        if (day > DAYS_IN_MONTHS[month - 1]) {
            if (!(day == 29 && month == 2 && isLeapYear(year))) {
                throw new DatatypeException("Day out of range.");
            }
        }

    }

    private boolean isLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    protected final void checkHour(String hour) throws DatatypeException {
        checkHour(Integer.parseInt(hour));
    }

    private void checkHour(int hour) throws DatatypeException {
        if (hour > 23) {
            throw new DatatypeException("Year cannot be greater than 23.");
        }
    }

    protected final void checkMinute(String minute) throws DatatypeException {
        checkMinute(Integer.parseInt(minute));
    }

    private void checkMinute(int minute) throws DatatypeException {
        if (minute > 59) {
            throw new DatatypeException("Minute cannot be greater than 59.");
        }
    }

    protected final void checkSecond(String second) throws DatatypeException {
        checkSecond(Integer.parseInt(second));
    }

    private void checkSecond(int second) throws DatatypeException {
        if (second > 59) {
            throw new DatatypeException("Second cannot be greater than 59.");
        }
    }

    private void checkTzd(String hours, String minutes) throws DatatypeException {
        checkTzd(Integer.parseInt(hours), Integer.parseInt(minutes));
    }

    private void checkTzd(int hours, int minutes) throws DatatypeException {
        if (minutes == 0 && (hours == 12 || hours == -12)) {
            return;
        }
        if (hours < -11 || hours > 11) {
            throw new DatatypeException("Time zone offset out of range.");
        }
        if (minutes > 59) {
            throw new DatatypeException("Minutes out of range in time zone designator.");
        }
    }
        
    protected abstract Pattern getPattern();

    public void checkValid(String literal, ValidationContext context)
            throws DatatypeException {
        Matcher m = getPattern().matcher(literal);
        if (m.matches()) {
            int count = m.groupCount();
            checkDate(m.group(1), m.group(2), m.group(3));
            if (count > 3) {
                checkHour(m.group(4));
                checkMinute(m.group(5));
                String seconds = m.group(6);
                if (seconds != null) {
                    checkSecond(seconds);
                }
                if (count > 6) {
                    String tzdHours = m.group(7);
                    if (tzdHours != null) {
                        checkTzd(tzdHours, m.group(8));
                    }
                }
            }
        } else {
            throw new DatatypeException(
                    "The literal did not satisfy the format for the datatype.");
        }
    }

}
