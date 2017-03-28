/*
 * Copyright (c) 2017 Mozilla Foundation
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

public class Color extends AbstractDatatype {

    public static final Color THE_INSTANCE = new Color();

    private static final String HEX_COLOR = "^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$";

    public Color() {
        super();
    }

    private final static boolean WARN = System.getProperty("nu.validator.datatype.warn", "").equals("true");
    
    private boolean checkHexColor(CharSequence color) {
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(HEX_COLOR);
        matcher = pattern.matcher(color);
        return matcher.lookingAt();
    }

    /**
     * @see nu.validator.datatype.AbstractDatatype#checkValid(java.lang.CharSequence)
     */
    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (checkHexColor(literal)) {
            return;
        } // else if <rgb()> | <rgba()> | <hsl()> | <hsla()> | etc.
    }

    /**
     * @see nu.validator.datatype.AbstractDatatype#getName()
     */
    @Override
    public String getName() {
        return "color";
    }

}
