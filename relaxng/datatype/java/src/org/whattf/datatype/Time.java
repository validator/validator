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

/**
 * 
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Time extends AbstractDatetime {

    /**
     * The singleton instance.
     */
    public static final Time THE_INSTANCE = new Time();

    /**
     * The rexexp for this datatype.
     */
    private static final Pattern THE_PATTERN = Pattern.compile("^([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.[0-9]+)?)?$");

    /**
     * Constructor.
     */
    private Time() {
        super();
    }

    public void checkValid(CharSequence literal)
            throws DatatypeException {
        Matcher m = getPattern().matcher(literal);
        if (m.matches()) {
            checkHour(m.group(1));
            checkMinute(m.group(2));
            String seconds = m.group(3);
            if (seconds != null) {
                checkSecond(seconds);
            }
        } else {
            throw newDatatypeException(
                    "The literal did not satisfy the format for time.");
        }
    }

    /**
     * Returns the regexp for this datatype.
     * 
     * @return the regexp for this datatype
     * @see org.whattf.datatype.AbstractDatetime#getPattern()
     */
    protected final Pattern getPattern() {
        return THE_PATTERN;
    }

    @Override
    public String getName() {
        return "time";
    }
}
