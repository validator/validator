/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2010-2014 Mozilla Foundation
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

package nu.validator.datatype;

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
     * Days in months on non-leap years.
     */
    private static int[] DAYS_IN_MONTHS = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
            31, 30, 31 };

    /**
     * Constructor.
     */
    AbstractDatetime() {
        super();
    }

    private final static boolean WARN = System.getProperty("nu.validator.datatype.warn", "").equals("true");

    private void checkMonth(String year, String month)
            throws DatatypeException {
        try {
            checkMonth(Integer.parseInt(year), Integer.parseInt(month));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Year or month out of range.");
        }
    }

    private void checkYear(int year) throws DatatypeException {
        if (year < 1) {
            throw newDatatypeException("Year cannot be less than 1.");
        } else if (WARN && (year < 1000 || year >= 3000)) {
            throw newDatatypeException("Year may be mistyped.", WARN);
        }
    }

    private void checkMonth(int year, int month)
            throws DatatypeException {
        if (month < 1) {
            throw newDatatypeException("Month cannot be less than 1.");
        }
        if (month > 12) {
            throw newDatatypeException("Month cannot be greater than 12.");
        }
        checkYear(year);
    }

    private void checkDate(String year, String month, String day)
            throws DatatypeException {
        try {
            checkDate(Integer.parseInt(year), Integer.parseInt(month),
                    Integer.parseInt(day));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Year, month, or day out of range.");
        }
    }

    private void checkDate(int year, int month, int day)
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
        if (day > DAYS_IN_MONTHS[month - 1]) {
            if (!(day == 29 && month == 2 && isLeapYear(year))) {
                throw newDatatypeException("Day out of range.");
            }
        }
        checkYear(year);

    }

    private boolean isLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    private void checkYearlessDate(String month, String day)
            throws DatatypeException {
        try {
            checkYearlessDate(Integer.parseInt(month), Integer.parseInt(day));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Month or day out of range.");
        }
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
        try {
            checkWeek(Integer.parseInt(year), Integer.parseInt(week));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Year or week out of range.");
        }
    }

    private void checkWeek(int year, int week)
            throws DatatypeException {
        if (week< 1) {
            throw newDatatypeException("Week cannot be less than 1.");
        }
        if (week > 53) {
            throw newDatatypeException("Week cannot be greater than 53.");
        }
        checkYear(year);
    }

    protected final void checkHour(String hour) throws DatatypeException {
        try {
            checkHour(Integer.parseInt(hour));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Hour out of range.");
        }
    }

    private void checkHour(int hour) throws DatatypeException {
        if (hour > 23) {
            throw newDatatypeException("Hour cannot be greater than 23.");
        }
    }

    protected final void checkMinute(String minute) throws DatatypeException {
        try {
            checkMinute(Integer.parseInt(minute));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Minute out of range.");
        }
    }

    private void checkMinute(int minute) throws DatatypeException {
        if (minute > 59) {
            throw newDatatypeException("Minute cannot be greater than 59.");
        }
    }

    protected final void checkSecond(String second) throws DatatypeException {
        try {
            checkSecond(Integer.parseInt(second));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Seconds out of range.");
        }
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
        try {
            checkTzd(Integer.parseInt(hours), Integer.parseInt(minutes));
        } catch (NumberFormatException e) {
            throw newDatatypeException("Hours or minutes out of range.");
        }
    }

    private void checkTzd(int hours, int minutes) throws DatatypeException {
        if (hours < -23 || hours > 23) {
            throw newDatatypeException("Hours out of range in time zone designator.");
        }
        if (minutes > 59) {
            throw newDatatypeException("Minutes out of range in time zone designator.");
        }
        if (WARN) {
            if (hours < -12 || hours > 14) {
                throw newDatatypeException(
                        "Hours in time zone designator should be from \u201C-12:00\u201d to \u201d+14:00\u201d",
                        WARN);
            }
            if (minutes != 00 && minutes != 30 && minutes != 45) {
                throw newDatatypeException(
                        "Minutes in time zone designator should be either \u201c00\u201d, \u201c30\u201d, or \u201c45\u201d.",
                        WARN);
            }
        }
    }

    protected abstract Pattern getPattern();

    @Override
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
            if (month != null) {
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
            if (year != null) {
                try {
                    checkYear(Integer.parseInt(year));
                } catch (NumberFormatException e) {
                    throw newDatatypeException("Year out of range.");
                }
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
