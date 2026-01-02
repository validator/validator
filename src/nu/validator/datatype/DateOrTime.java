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

package nu.validator.datatype;

import java.util.regex.Pattern;

public class DateOrTime extends AbstractDatetime {
    /**
     * The singleton instance.
     */
    public static final DateOrTime THE_INSTANCE = new DateOrTime();

    /**
     * The regexp for this datatype.
     * Uses dummy groups to align captures with AbstractDatetime.checkValid():
     * - Date-only: groups 3-5 (year, month, day)
     * - Time-only: groups 8-11 (hour, minute, seconds, ms)
     * - Local datetime: groups 12-18
     * - Global datetime: groups 21-29
     */
    private static final Pattern THE_PATTERN = Pattern.compile(
            "^(?:"
            // Date only: 2 dummy + groups 3-5
            + "(.){0}(.){0}([0-9]{4,})-([0-9]{2})-([0-9]{2})"
            + "|"
            // Time only: 2 dummy + groups 8-11 (hour, minute, seconds, ms)
            + "(.){0}(.){0}([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]{1,3}))?)?"
            + ")$");
    
    private DateOrTime() {
        super();
    }

    @Override
    protected Pattern getPattern() {
        return THE_PATTERN;
    }

    @Override
    public String getName() {
        return "date or time";
    }

}
