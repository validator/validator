/*
 * Copyright (c) 2015-2018 Mozilla Foundation
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.relaxng.datatype.DatatypeException;

public class SourceSizeList extends AbstractDatatype {

    private static enum State {
        IN_SIZE, AFTER_SLASH, IN_COMMENT, IN_COMMENT_AFTER_ASTERISK
    }

    private static final int CLIP_LIMIT = 25;

    private static final Set<String> LENGTH_UNITS = new LinkedHashSet<>();

    private static final StringBuilder VALID_UNITS = new StringBuilder();

    static {
        /* font-relative lengths */
        LENGTH_UNITS.add("em");
        LENGTH_UNITS.add("ex");
        LENGTH_UNITS.add("ch");
        LENGTH_UNITS.add("rem");
        LENGTH_UNITS.add("cap");
        LENGTH_UNITS.add("ic");
        /* viewport-percentage lengths */
        LENGTH_UNITS.add("vw");
        LENGTH_UNITS.add("svw");
        LENGTH_UNITS.add("lvw");
        LENGTH_UNITS.add("dvw");
        LENGTH_UNITS.add("vh");
        LENGTH_UNITS.add("svh");
        LENGTH_UNITS.add("lvh");
        LENGTH_UNITS.add("dvh");
        LENGTH_UNITS.add("vi");
        LENGTH_UNITS.add("svi");
        LENGTH_UNITS.add("lvi");
        LENGTH_UNITS.add("dvi");
        LENGTH_UNITS.add("vb");
        LENGTH_UNITS.add("svb");
        LENGTH_UNITS.add("lvb");
        LENGTH_UNITS.add("dvb");
        LENGTH_UNITS.add("vmin");
        LENGTH_UNITS.add("svmin");
        LENGTH_UNITS.add("lvmin");
        LENGTH_UNITS.add("dvmin");
        LENGTH_UNITS.add("vmax");
        LENGTH_UNITS.add("svmax");
        LENGTH_UNITS.add("lvmax");
        LENGTH_UNITS.add("dvmax");
        /* absolute lengths */
        LENGTH_UNITS.add("cm");
        LENGTH_UNITS.add("mm");
        LENGTH_UNITS.add("q");
        LENGTH_UNITS.add("in");
        LENGTH_UNITS.add("pc");
        LENGTH_UNITS.add("pt");
        LENGTH_UNITS.add("px");

        for (CharSequence units : LENGTH_UNITS) {
            VALID_UNITS.append(" \u201c").append(units).append("\u201d,");
        }
        VALID_UNITS.setLength(VALID_UNITS.length() - 1);
    }

    private static final Set<String> FUNCTION_NAMES = new LinkedHashSet<>();

    private static final StringBuilder VALID_FUNCTION_NAMES = new StringBuilder();

    static {
        /* Basic Arithmetic */
        FUNCTION_NAMES.add("calc");
        /* Comparison Functions */
        FUNCTION_NAMES.add("min");
        FUNCTION_NAMES.add("max");
        FUNCTION_NAMES.add("clamp");
        /* Trigonometric Functions */
        FUNCTION_NAMES.add("sin");
        FUNCTION_NAMES.add("cos");
        FUNCTION_NAMES.add("tan");
        FUNCTION_NAMES.add("asin");
        FUNCTION_NAMES.add("acos");
        FUNCTION_NAMES.add("atan");
        FUNCTION_NAMES.add("atan2");
        /* Exponential Functions */
        FUNCTION_NAMES.add("pow");
        FUNCTION_NAMES.add("sqrt");
        FUNCTION_NAMES.add("hypot");
        FUNCTION_NAMES.add("log");
        FUNCTION_NAMES.add("exp");
        /* Sign-Related Functions */
        FUNCTION_NAMES.add("abs");
        FUNCTION_NAMES.add("sign");

        for (CharSequence name : FUNCTION_NAMES) {
            VALID_FUNCTION_NAMES.append(" \u201c").append(name).append(
                    "\u201d,");
        }
        VALID_FUNCTION_NAMES.setLength(VALID_FUNCTION_NAMES.length() - 1);
    }

    private static final CssNumberToken CSS_NUMBER_TOKEN = CssNumberToken.THE_INSTANCE;

    private static final MediaCondition MEDIA_CONDITION = MediaCondition.THE_INSTANCE;

    public static final SourceSizeList THE_INSTANCE = new SourceSizeList();

    protected SourceSizeList() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            err("Must not be empty.");
        }
        int offset = 0;
        boolean isFirst = true;
        String currentChunk;
        StringBuilder unparsedSize = new StringBuilder();
        StringBuilder extract = new StringBuilder();
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            extract.append(c);
            if (',' == c) {
                currentChunk = literal.subSequence(offset, i).toString();
                checkForInvalidComments(currentChunk, extract);
                unparsedSize.append(currentChunk);
                unparsedSize = removeComments(unparsedSize, extract);
                checkSize(unparsedSize, literal, extract, isFirst, false);
                isFirst = false;
                unparsedSize.setLength(0);
                offset = i + 1;
            }
        }
        currentChunk = literal.subSequence(offset, literal.length()).toString();
        checkForInvalidComments(currentChunk, extract);
        unparsedSize.append(currentChunk);
        unparsedSize = removeComments(unparsedSize, extract);
        checkSize(unparsedSize, literal, extract, isFirst, true);
    }

    private void checkSize(StringBuilder unparsedSize, CharSequence literal,
            StringBuilder extract, boolean isFirst, boolean isLast)
                    throws DatatypeException {
        String size;
        trimWhitespace(unparsedSize);
        if (unparsedSize.length() == 0) {
            errEmpty(isFirst, isLast, extract);
            return;
        }
        if (')' == unparsedSize.charAt(unparsedSize.length() - 1)) {
            checkFunction(unparsedSize, extract, isLast);
            return;
        }
        int sizeValueStart = lastSpaceIndex(unparsedSize);
        size = unparsedSize.substring(lastSpaceIndex(unparsedSize),
                unparsedSize.length());
        try {
            if (Float.parseFloat(size) == 0) {
                return;
            }
        } catch (NumberFormatException e) {
        }
        String units = getUnits(size);
        String num = size.substring(0, size.length() - units.length());
        boolean sizeIsLessThanZero = false;
        try {
            CSS_NUMBER_TOKEN.checkValid(num);
            if (Float.parseFloat(num) < 0) {
                sizeIsLessThanZero = true;
            }
        } catch (DatatypeException e) {
            errFromOtherDatatype(e.getMessage(), extract);
        } catch (NumberFormatException e) {
            errNotNumber(num, extract);
        }
        if (sizeIsLessThanZero) {
            errNotPositive(size, extract);
        }
        if (!LENGTH_UNITS.contains(units)) {
            errNotUnits(units, extract);
        }
        unparsedSize.setLength(sizeValueStart);
        trimTrailingWhitespace(unparsedSize);
        if (unparsedSize.length() == 0) {
            if (!isLast) {
                errNoMediaCondition(unparsedSize, extract);
            }
            return;
        }
        try {
            MEDIA_CONDITION.checkValid(unparsedSize);
        } catch (DatatypeException e) {
            errFromOtherDatatype(e.getMessage(), extract);
        }
    }

    private void checkFunction(StringBuilder sb, CharSequence extract,
            boolean isLast) throws DatatypeException {
        int firstParenPosition = sb.length() - 1;
        int unMatchedParenCount = 1;
        while (unMatchedParenCount > 0) {
            if (firstParenPosition == 0) {
                errMismatchedParens(sb, extract);
            }
            char c = sb.charAt(--firstParenPosition);
            if ('(' == c) {
                unMatchedParenCount--;
            } else if (')' == c) {
                unMatchedParenCount++;
            }
        }

        String functionName = extractFunctionNameFromTail(
                sb.subSequence(0, firstParenPosition)).toString();
        int nameStartPosition = firstParenPosition - functionName.length();

        if (functionName.isEmpty()) {
            errNotFunctionName("", extract);
        } else if (!isLast && nameStartPosition == 0) {
            errNoMediaCondition(sb, extract);
        } else if (nameStartPosition != 0
                && !isWhitespace(sb.charAt(nameStartPosition - 1))) {
            errNoWhitespaceBeforeFunction(extract);
        } else if (!FUNCTION_NAMES.contains(toAsciiLowerCase(functionName))) {
            errNotFunctionName(functionName, extract);
        }
    }

    private CharSequence extractFunctionNameFromTail(CharSequence sequence) {
        for (int i = sequence.length() - 1; i >= 0; i--) {
            char c = sequence.charAt(i);
            if (isWhitespace(c) || c == ')') {
                return sequence.subSequence(i + 1, sequence.length());
            }
        }
        return sequence;
    }

    private void checkForInvalidComments(String currentChunk,
            StringBuilder extract) throws DatatypeException {
        if (currentChunk.contains("+/")) {
            errNotNumber("+/", extract);
        }
        if (currentChunk.contains("-/")) {
            errNotNumber("-/", extract);
        }
        for (String units : LENGTH_UNITS) {
            if (currentChunk.contains("/" + units)) {
                errNotUnits("/" + units, extract);
            }
        }
    }

    private StringBuilder removeComments(StringBuilder sb, CharSequence extract)
            throws DatatypeException {
        if (sb.indexOf("/*") == -1) {
            return sb;
        }
        StringBuilder sb2 = new StringBuilder();
        State state = State.IN_SIZE;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            switch (state) {
                case IN_SIZE:
                    if ('/' == sb.charAt(i)) {
                        sb2.append('/');
                        state = State.AFTER_SLASH;
                        continue;
                    } else {
                        sb2.append(c);
                        continue;
                    }
                case AFTER_SLASH:
                    if ('*' == sb.charAt(i)) {
                        sb2.setLength(sb2.length() - 1);
                        state = State.IN_COMMENT;
                        continue;
                    } else {
                        sb2.append(c);
                        continue;
                    }
                case IN_COMMENT:
                    if ('*' == sb.charAt(i)) {
                        state = State.IN_COMMENT_AFTER_ASTERISK;
                        continue;
                    } else {
                        continue;
                    }
                case IN_COMMENT_AFTER_ASTERISK:
                    if ('/' == sb.charAt(i)) {
                        state = State.IN_SIZE;
                        continue;
                    } else {
                        continue;
                    }
            }
        }
        if (state == State.IN_COMMENT
                || state == State.IN_COMMENT_AFTER_ASTERISK) {
            trimWhitespace(sb);
            errUnclosedComment(sb, extract);
        }
        return sb2;
    }

    private int lastSpaceIndex(StringBuilder sb) {
        for (int i = sb.length(); i > 0; i--) {
            char c = sb.charAt(i - 1);
            if (isWhitespace(c)) {
                return i;
            }
        }
        return 0;
    }

    private void trimWhitespace(StringBuilder sb) {
        trimTrailingWhitespace(sb);
        trimLeadingWhitespace(sb);
    }

    private void trimTrailingWhitespace(StringBuilder sb) {
        for (int i = sb.length(); i > 0; i--) {
            if (isWhitespace(sb.charAt(i - 1))) {
                sb.setLength(i - 1);
            } else {
                return;
            }
        }
    }

    private void trimLeadingWhitespace(StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++) {
            if (!isWhitespace(sb.charAt(i))) {
                sb.delete(0, i);
                return;
            }
        }
    }

    private String getUnits(String val) {
        for (int i = val.length(); i > 0; i--) {
            char c = val.charAt(i - 1);
            if ((c >= '0' && c <= '9')) {
                return val.substring(i, val.length());
            }
        }
        return "";
    }

    private void err(String message) throws DatatypeException {
        throw newDatatypeException(message);
    }

    private void errFromOtherDatatype(CharSequence msg, CharSequence extract)
            throws DatatypeException {
        err(msg.subSequence(0, msg.length() - 1) + " at " + clip(extract)
                + ".");
    }

    private void errEmpty(boolean isFirst, boolean isLast, CharSequence extract)
            throws DatatypeException {
        if (isFirst) {
            if (isLast) {
                err("Must contain one or more source sizes.");
            } else {
                err("Starts with empty source size.");
            }
        } else {
            err("Empty source size at " + clip(extract) + ".");
        }
    }

    private void errNoMediaCondition(CharSequence sourceSize,
            CharSequence extract) throws DatatypeException {
        err("Expected media condition before " + code(sourceSize) + " at "
                + clip(extract) + ".");
    }

    private void errNotPositive(CharSequence size, CharSequence extract)
            throws DatatypeException {
        err("Expected positive size value but found " + code(size) + " at "
                + clip(extract) + ".");
    }

    private void errNotNumber(CharSequence num, CharSequence extract)
            throws DatatypeException {
        err("Expected number but found " + code(num) + " at " + clip(extract)
                + ".");
    }

    private void errNotUnits(CharSequence units, StringBuilder extract)
            throws DatatypeException {
        String msg = "Expected units (one of" + VALID_UNITS + ") but found ";
        if ("".equals(units)) {
            msg += "no units";
        } else {
            msg += code(units);
        }
        msg += " at " + clip(extract) + ".";
        err(msg);
    }

    private void errNotFunctionName(CharSequence name, CharSequence extract)
            throws DatatypeException {
        String msg = "Expected function (one of" + VALID_FUNCTION_NAMES
                + ") but found ";
        if ("".equals(name)) {
            msg += "no name";
        } else {
            msg += code(name);
        }
        msg += " at " + clip(extract) + ".";
        err(msg);
    }

    private void errNoWhitespaceBeforeFunction(CharSequence extract)
            throws DatatypeException {
        err("There must be a whitespace between media query and a function at "
                + clip(extract) + ".");
    }

    private void errMismatchedParens(CharSequence cs, CharSequence extract)
            throws DatatypeException {
        err("Mismatched parentheses in " + clip(extract) + ".");
    }

    private void errUnclosedComment(CharSequence cs, CharSequence extract)
            throws DatatypeException {
        err("Unclosed comment in " + code(cs) + " at " + clip(extract) + ".");
    }

    private CharSequence clip(CharSequence cs) {
        int len = cs.length();
        if (len > CLIP_LIMIT) {
            cs = "\u2026" + cs.subSequence(len - CLIP_LIMIT, len);
        }
        return code(cs);
    }

    private CharSequence code(CharSequence cs) {
        return "\u201c" + cs + "\u201d";
    }

    @Override
    public String getName() {
        return "source size list";
    }
}
