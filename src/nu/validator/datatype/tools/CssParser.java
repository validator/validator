/*
 * Copyright (c) 2015 Mozilla Foundation
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

package nu.validator.datatype.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ScriptableObject;

public class CssParser {

    private static ScriptableObject scope;

    private static Function tokenizer;

    private static Function ruleParser;

    static {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            CssParser.class.getClassLoader().getResourceAsStream(
                                    "nu/validator/localentities/files/parse-css-js")));
            br.mark(1);
            Context context = ContextFactory.getGlobal().enterContext();
            context.setOptimizationLevel(1);
            context.setLanguageVersion(Context.VERSION_1_6);
            scope = context.initStandardObjects();
            context.evaluateReader(scope, br, null, -1, null);
            tokenizer = (Function) scope.get("tokenize", scope);
            ruleParser = (Function) scope.get("parseARule", scope);
        } catch (IOException e) {
        }
    }

    public String[] tokenize(CharSequence cs) throws ParseException {
        try {
            Context context = ContextFactory.getGlobal().enterContext();
            context.setOptimizationLevel(0);
            context.setLanguageVersion(Context.VERSION_1_6);
            return (String[]) Context.jsToJava(
                    tokenizer.call(context, scope, scope, new Object[] { cs }),
                    String[].class);
        } catch (JavaScriptException e) {
            throw new ParseException(e.details(), -1);
        }
    }

    public String parseARule(CharSequence cs) throws ParseException {
        try {
            Context context = ContextFactory.getGlobal().enterContext();
            context.setOptimizationLevel(0);
            context.setLanguageVersion(Context.VERSION_1_6);
            return (String) Context.jsToJava(ruleParser.call(context, scope, scope,
                    new Object[] { tokenizer.call(context, scope, scope,
                            new Object[] { cs }) }), String.class);
        } catch (JavaScriptException e) {
            throw new ParseException(e.details(), -1);
        }
    }

}
