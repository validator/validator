/*
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

package nu.validator.javascript;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.*;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class JavaScriptParser {

    private static ScriptEngine engine;

    private static Invocable inv;

    private static ScriptObjectMirror acorn;

    private static String OPTS = "\"ecmaVersion\":2018," //
            + "\"allowImportExportEverywhere\":true,";

    private static final Object parseLock = new Object();

    static {
        try {
            engine = new ScriptEngineManager(null).getEngineByName("nashorn");
            engine.eval(new BufferedReader(new InputStreamReader(
                    JavaScriptParser.class.getClassLoader().getResourceAsStream(
                            "nu/validator/localentities/files/acorn-js"),
                    StandardCharsets.UTF_8)));
            inv = (Invocable) engine;
            acorn = (ScriptObjectMirror) engine.get("acorn");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static Pattern LINE_COLUMN = //
            Pattern.compile("^.*(\\([0-9]+:[0-9]+\\))$");

    public void parse(String script, String type)
            throws JavaScriptParseException {
        try {
            String opts = "{" + OPTS + "\"sourceType\":\"" + type + "\"}";
            engine.put("options", opts);
            JSObject options = (JSObject) engine.eval("JSON.parse(options)");
            synchronized (parseLock) {
                inv.invokeMethod(acorn, "parse", script, options);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ScriptException se) {
            Throwable cause = se.getCause();
            if (cause instanceof NashornException) {
                NashornException ne = (NashornException) cause;
                Object obj = ne.getEcmaError();
                if (obj instanceof JSObject) {
                    error((JSObject) obj, script);
                } else {
                    ne.printStackTrace();
                }
            } else {
                se.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void error(JSObject error, String script)
            throws JavaScriptParseException {
        try {
            JSObject loc = (JSObject) error.getMember("loc");
            int beginLine = ((Double) loc.getMember("line")).intValue();
            int beginColumn = ((Double) loc.getMember("column")).intValue() + 1;
            int p = ((Double) error.getMember("raisedAt")).intValue();
            Object endPos = inv.invokeMethod(acorn, "getLineInfo", script, p);
            Map<String, Object> end = (Map<String, Object>) endPos;
            int endLine = ((Double) end.get("line")).intValue();
            int endColumn = ((Double) end.get("column")).intValue();
            String message = (String) error.getMember("message");
            Matcher m = LINE_COLUMN.matcher(message);
            if (m.matches()) {
                message = message.substring(0, message.indexOf(m.group(1)));
            }
            throw new JavaScriptParseException(message.trim() + ".", //
                    beginLine, beginColumn, endLine, endColumn);
        } catch (ScriptException se) {
            se.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public class JavaScriptParseException extends Exception {

        private final int beginLine;

        private final int beginColumn;

        private final int endLine;

        private final int endColumn;

        public int getBeginLine() {
            return beginLine;
        }

        public int getBeginColumn() {
            return beginColumn;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getEndColumn() {
            return endColumn;
        }

        public JavaScriptParseException(String message, //
                int beginLine, int beginColumn, int endLine, int endColumn) {
            super(message);
            this.beginLine = beginLine;
            this.beginColumn = beginColumn;
            this.endLine = endLine;
            this.endColumn = endColumn;
        }
    }
}
