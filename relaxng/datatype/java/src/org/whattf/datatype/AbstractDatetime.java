/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2010-2012 Mozilla Foundation
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
     * Constructor.
     */
    AbstractDatetime() {
        super();
    }

    private void checkMonth(String year, String month)
            throws DatatypeException {
        checkMonth(Integer.parseInt(year), Integer.parseInt(month));
    }

    private void checkMonth(int year, int month)
            throws DatatypeException {
        if (year < 1) {
            throw newDatatypeException("Year cannot be less than 1.");
        }
        if (month < 1) {
            throw newDatatypeException("Month cannot be less than 1.");
        }
        if (month > 12) {
            throw newDatatypeException("Month cannot be greater than 12.");
        }
    }

    private void checkDate(String year, String month, String day)
            throws DatatypeException {
        checkDate(Integer.parseInt(year), Integer.parseInt(month),
                Integer.parseInt(day));
    }

    private void checkDate(int year, int month, int day)
            throws DatatypeException {
        if (year < 1) {
            throw newDatatypeException("Year cannot be less than 1.");
        }
        if (month < 1) {
            throw newDatatypeException("Month cannot be less than 1.");
        }
        if (month > 12) {
            throw newDatatypeException("Month cannot be greater than 12.");
        }
        if (day < 1) {
            throw newDatatypeException("Day cannot be less than 1.");
        }
        if (day > DAYS_IN_MONTHS[month - 1]) {
            if (!(day == 29 && month == 2 && isLeapYear(year))) {
                throw newDatatypeException("Day out of range.");
            }
        }

    }

    private boolean isLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    private void checkYearlessDate(String month, String day)
            throws DatatypeException {
        checkYearlessDate(Integer.parseInt(month), Integer.parseInt(day));
    }

    private void checkYearlessDate(int month, int day)
            throws DatatypeException {
        if (month < 1) {
            throw newDatatypeException("Month cannot be less than 1.");
        }
        if (month > 12) {
            throw newDatatypeException("Month cannot be greater than 12.");
        }
        if (day < 1) {
            throw newDatatypeException("Day cannot be less than 1.");
        }
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
        if (week< 1) {
            throw newDatatypeException("Week cannot be less than 1.");
        }
        if (week > 53) {
            throw newDatatypeException("Week cannot be greater than 53.");
        }
    }

    protected final void checkHour(String hour) throws DatatypeException {
        checkHour(Integer.parseInt(hour));
    }

    private void checkHour(int hour) throws DatatypeException {
        if (hour > 23) {
            throw newDatatypeException("Hour cannot be greater than 23.");
        }
    }

    protected final void checkMinute(String minute) throws DatatypeException {
        checkMinute(Integer.parseInt(minute));
    }

    private void checkMinute(int minute) throws DatatypeException {
        if (minute > 59) {
            throw newDatatypeException("Minute cannot be greater than 59.");
        }
    }

    protected final void checkSecond(String second) throws DatatypeException {
        checkSecond(Integer.parseInt(second));
    }

    private void checkSecond(int second) throws DatatypeException {
        if (second > 59) {
            throw newDatatypeException("Second cannot be greater than 59.");
        }
    }

    protected final void checkMilliSecond(String millisecond) throws DatatypeException {
        if (millisecond.length() > 3) {
            throw newDatatypeException("A fraction of a second must be one, two, or three digits.");
        }
    }

    private void checkTzd(String hours, String minutes) throws DatatypeException {
        if (hours.charAt(0) == '+') {
            hours = hours.substring(1);
        }
        checkTzd(Integer.parseInt(hours), Integer.parseInt(minutes));
    }

    private void checkTzd(int hours, int minutes) throws DatatypeException {
        if (hours < -23 || hours > 23) {
            throw newDatatypeException("Hours out of range in time zone designator.");
        }
        if (minutes > 59) {
            throw newDatatypeException("Minutes out of range in time zone designator.");
        }
    }
        
    protected abstract Pattern getPattern();

    public void checkValid(CharSequence literal)
            throws DatatypeException {
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String seconds;
        String milliseconds;
        String tzdHours;
        String tzdMinutes;
        Matcher m = getPattern().matcher(literal);
        if (m.matches()) {
            // valid month string
            year = m.group(1);
            month = m.group(2);
            if (year != null) {
                checkMonth(year, month);
                return;
            }
            // valid date string
            year = m.group(3);
            month = m.group(4);
            day = m.group(5);
            if (year != null) {
                checkDate(year, month, day);
                return;
            }
            // valid yearless date string
            month = m.group(6);
            day = m.group(7);
            if (year != null) {
                checkYearlessDate(month, day);
                return;
            }
            // valid time string
            hour = m.group(8);
            minute = m.group(9);
            seconds = m.group(10);
            milliseconds = m.group(11);
            if (hour != null) {
                checkHour(hour);
                checkMinute(minute);
                if (seconds != null) {
                    checkSecond(seconds);
                }
                if (milliseconds != null) {
                    checkMilliSecond(milliseconds);
                }
                return;
            }
            // valid local date and time string
            year = m.group(12);
            month = m.group(13);
            day = m.group(14);
            hour = m.group(15);
            minute = m.group(16);
            seconds = m.group(17);
            milliseconds = m.group(18);
            if (year != null) {
                checkDate(year, month, day);
                checkHour(hour);
                checkMinute(minute);
                if (seconds != null) {
                    checkSecond(seconds);
                }
                if (milliseconds != null) {
                    checkMilliSecond(milliseconds);
                }
                return;
            }
            // valid time-zone offset string
            tzdHours = m.group(19);
            tzdMinutes = m.group(20);
            if (tzdHours != null) {
                checkTzd(tzdHours, tzdMinutes);
                return;
            }
            // valid global date and time string
            year = m.group(21);
            month = m.group(22);
            day = m.group(23);
            hour = m.group(24);
            minute = m.group(25);
            seconds = m.group(26);
            milliseconds = m.group(27);
            tzdHours = m.group(28);
            tzdMinutes = m.group(29);
            if (year != null) {
                checkDate(year, month, day);
                checkHour(hour);
                checkMinute(minute);
                if (seconds != null) {
                    checkSecond(seconds);
                }
                if (milliseconds != null) {
                    checkMilliSecond(milliseconds);
                }
                if (tzdHours != null) {
                    checkTzd(tzdHours, tzdMinutes);
                }
                return;
            }
            // valid week string
            year = m.group(30);
            String week = m.group(31);
            if (year != null) {
                checkWeek(year, week);
            }
            //  valid year (valid non-negative integer)
            year = m.group(32);
            if (year != null && Integer.parseInt(year) < 1) {
                throw newDatatypeException("Year cannot be less than 1.");
            }
            // valid duration string
            milliseconds = m.group(33);
            if (milliseconds != null) {
                checkMilliSecond(milliseconds);
                return;
            }
            milliseconds = m.group(34);
            if (milliseconds != null) {
                checkMilliSecond(milliseconds);
                return;
            }
        } else {
            throw newDatatypeException(
                    "The literal did not satisfy the " + getName() + " format.");
        }
    }

}
