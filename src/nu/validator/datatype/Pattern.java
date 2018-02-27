/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2018 Mozilla Foundation
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

import org.relaxng.datatype.DatatypeException;
import nu.validator.javascript.JavaScriptParser;
import nu.validator.javascript.JavaScriptParser.JavaScriptParseException;

public final class Pattern extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Pattern THE_INSTANCE = new Pattern();

    private Pattern() {
        super();
    }

    private static final JavaScriptParser javascriptParser = //
            new JavaScriptParser();

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        try {
            String contents = "/^(?:" + literal.toString() + ")$/u')";
            javascriptParser.parse(contents, "script");
        } catch (JavaScriptParseException e) {
            String message = e.getMessage();
            if (message.startsWith("Invalid regular expression: ")) {
                message = message //
                        .replace(" /^(?:", " \u201C") //
                        .replace(")$/: ", "\u201D: ");
            }
            throw newDatatypeException(message);
        }
    }

    @Override
    public String getName() {
        return "pattern";
    }

}
