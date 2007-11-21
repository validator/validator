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

import java.util.regex.Pattern;

/**
 * This datatype shall accept strings that conform to the format specified for 
 * <a href='http://whatwg.org/specs/web-apps/current-work/#datetime'><code>datetime</code></a> 
 * attribute of the <code>ins</code> and <code>del</code> elements in Web Applications 1.0.
 * <p>If the time zone designator is not "<code>Z</code>", the absolute value of the time 
 * zone designator must not exceed 12 hours.
 * <p>This datatype must not accept the empty string.
 * <p>Note that allowing a numeric time zone designator is not the only difference with 
 * <a href='#datetime'><code>datetime</code></a>. This type requires seconds to be explicitly 
 * present.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class DatetimeTz extends AbstractDatetime {

    /**
     * The singleton instance.
     */
    public static final DatetimeTz THE_INSTANCE = new DatetimeTz();

    /**
     * The rexexp for this datatype.
     */
    private static final Pattern THE_PATTERN = Pattern.compile("^([0-9]{4,})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(?:\\.[0-9]+)?(?:Z|(?:([+-][0-9]{2}):([0-9]{2})))$");

    /**
     * Constructor.
     */
    private DatetimeTz() {
        super();
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
        return "datetime with timezone";
    }
}
