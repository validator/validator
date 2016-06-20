/*
 * Copyright (c) 2015-2016 Mozilla Foundation
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
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Map;

import javax.script.*;

public class CssParser {

    private static Invocable cssparser;

    static {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                CssParser.class.getClassLoader().getResourceAsStream(
                        "nu/validator/localentities/files/parse-css-js")));
        try {
            ScriptEngine engine = new ScriptEngineManager(null).getEngineByName(
                    "nashorn");
            engine.eval(br);
            cssparser = (Invocable) engine;
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> tokenize(CharSequence cs) throws ParseException {
        try {
            return (Map<String, Object>) cssparser.invokeFunction("tokenize",
                    cs.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            throw new ParseException(e.getMessage(), -1);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseARule(CharSequence cs)
            throws ParseException {
        try {
            return (Map<String, Object>) cssparser.invokeFunction("parseARule",
                    cs.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            throw new ParseException(e.getMessage(), -1);
        }
        return null;
    }

}
