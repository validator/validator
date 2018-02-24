/*
 * Copyright (c) 2011-2018 Mozilla Foundation
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.relaxng.datatype.DatatypeException;

public class FunctionBody extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final FunctionBody THE_INSTANCE = new FunctionBody();

    protected FunctionBody() {
        super();
    }

    private static Pattern TL = Pattern.compile("`([^`]*)`");

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        try {
            String contents = literal.toString();
            Matcher t = TL.matcher(contents);
            while (t.find()) {
                String tlContents = "";
                int n = t.group(1).length();
                if (n > 0) {
                    tlContents = String.format("%1$" + n + "s", "");
                }
                contents = t.replaceAll("'" + tlContents + "'");
            }
            Reader reader = new BufferedReader(
                    (new StringReader("function(event){" + contents + "}")));
            reader.mark(1);
            try {
                Context context = ContextFactory.getGlobal().enterContext();
                context.setOptimizationLevel(0);
                context.setLanguageVersion(Context.VERSION_ES6);
                // -1 for lineno arg prevents Rhino from appending
                // "(unnamed script#1)" to all error messages
                context.compileReader(reader, null, -1, null);
            } finally {
                Context.exit();
            }
        } catch (IOException | RhinoException e) {
            throw newDatatypeException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "ECMAScript FunctionBody";
    }

}
