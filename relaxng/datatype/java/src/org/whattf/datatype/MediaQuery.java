/*
 * Copyright (c) 2007 Mozilla Foundation
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;

public class MediaQuery extends AbstractDatatype {

    private enum State {
        INITIAL_WS, OPEN_PAREN_SEEN, IN_ONLY_OR_NOT, IN_MEDIA_TYPE, IN_MEDIA_FEATURE, WS_BEFORE_MEDIA_TYPE, WS_BEFORE_AND, IN_AND, WS_BEFORE_EXPRESSION, WS_BEFORE_COLON, WS_BEFORE_VALUE, IN_VALUE_DIGITS, IN_VALUE_STRING, WS_BEFORE_CLOSE_PAREN, IN_VALUE_UNIT, IN_VALUE_DIGITS_AFTER_DOT, RATIO_SECOND_INTEGER_START, IN_VALUE_BEFORE_DIGITS, IN_VALUE_DIGITS_AFTER_DOT_TRAIL, AFTER_CLOSE_PAREN
    }

    private enum ValueType {
        LENGTH, RATIO, INTEGER, RESOLUTION, SCAN
    }

    private static final Pattern COMMA = Pattern.compile(",");

    private static final Set<String> LENGTH_UNITS = new HashSet<String>();

    static {
        LENGTH_UNITS.add("em");
        LENGTH_UNITS.add("ex");
        LENGTH_UNITS.add("px");
        LENGTH_UNITS.add("gd");
        LENGTH_UNITS.add("rem");
        LENGTH_UNITS.add("vw");
        LENGTH_UNITS.add("vh");
        LENGTH_UNITS.add("vm");
        LENGTH_UNITS.add("ch");
        LENGTH_UNITS.add("in");
        LENGTH_UNITS.add("cm");
        LENGTH_UNITS.add("mm");
        LENGTH_UNITS.add("pt");
        LENGTH_UNITS.add("pc");
    }
    
    private static final Set<String> MEDIA_TYPES = new HashSet<String>();

    static {
        MEDIA_TYPES.add("all");
        MEDIA_TYPES.add("aural");
        MEDIA_TYPES.add("braille");
        MEDIA_TYPES.add("handheld");
        MEDIA_TYPES.add("print");
        MEDIA_TYPES.add("projection");
        MEDIA_TYPES.add("screen");
        MEDIA_TYPES.add("tty");
        MEDIA_TYPES.add("tv");
        MEDIA_TYPES.add("embossed");
    }

    private static final Map<String, ValueType> FEATURES_TO_VALUE_TYPES = new HashMap<String, ValueType>();

    static {
        FEATURES_TO_VALUE_TYPES.put("width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("device-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-device-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-device-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("device-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-device-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-device-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("device-aspect-ratio", ValueType.RATIO);
        FEATURES_TO_VALUE_TYPES.put("min-device-aspect-ratio", ValueType.RATIO);
        FEATURES_TO_VALUE_TYPES.put("max-device-aspect-ratio", ValueType.RATIO);
        FEATURES_TO_VALUE_TYPES.put("color", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("min-color", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("max-color", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("color-index", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("min-color-index", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("max-color-index", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("monochrome", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("min-monochrome", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("max-monochrome", ValueType.INTEGER);
        FEATURES_TO_VALUE_TYPES.put("resolution", ValueType.RESOLUTION);
        FEATURES_TO_VALUE_TYPES.put("min-resolution", ValueType.RESOLUTION);
        FEATURES_TO_VALUE_TYPES.put("max-resolution", ValueType.RESOLUTION);
        FEATURES_TO_VALUE_TYPES.put("scan", ValueType.SCAN);
        FEATURES_TO_VALUE_TYPES.put("grid", ValueType.INTEGER);
    }

    public MediaQuery() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        String[] queries = COMMA.split(literal, -1);
        // XXX not exactly the best perf but does not matter really
        for (int i = 0; i < queries.length; i++) {
            checkQuery(queries[i]);
        }
    }

    private void checkQuery(String query) throws DatatypeException {
        boolean zero = true;
        ValueType valueExpectation = null;
        query = toAsciiLowerCase(query);
        StringBuilder sb = new StringBuilder();
        State state = State.INITIAL_WS;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            switch (state) {
                case INITIAL_WS:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('(' == c) {
                        state = State.OPEN_PAREN_SEEN;
                        continue;
                    } else if ('o' == c || 'n' == c) {
                        sb.append(c);
                        state = State.IN_ONLY_OR_NOT;
                        continue;
                    } else if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        state = State.IN_MEDIA_TYPE;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected \u201C(\u201D or letter at start of a media query part but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_ONLY_OR_NOT:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c)) {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if ("only".equals(kw) || "not".equals(kw)) {
                            state = State.WS_BEFORE_MEDIA_TYPE;
                            continue;
                        } else {
                            throw new DatatypeException(
                                    "Bad media query: Expected \u201Conly\u201D or \u201Cnot\u201D but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_MEDIA_TYPE:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        state = State.IN_MEDIA_TYPE;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_MEDIA_TYPE:
                    if (('a' <= c && 'z' >= c) || c == '-') {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c)) {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (isMediaType(kw)) {
                            state = State.WS_BEFORE_AND;
                            continue;
                        } else {
                            throw new DatatypeException(
                                    "Bad media query: Expected a CSS media type but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter, hyphen or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_AND:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('a' == c) {
                        sb.append(c);
                        state = State.IN_AND;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_AND:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c)) {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if ("and".equals(kw)) {
                            state = State.WS_BEFORE_EXPRESSION;
                            continue;
                        } else {
                            throw new DatatypeException(
                                    "Bad media query: Expected \u201Cand\u201D but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_EXPRESSION:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('(' == c) {
                        state = State.OPEN_PAREN_SEEN;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected \u201C(\u201D or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case OPEN_PAREN_SEEN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        state = State.IN_MEDIA_FEATURE;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter at start of a media feature part but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_MEDIA_FEATURE:
                    if (('a' <= c && 'z' >= c) || c == '-') {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ':') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        valueExpectation = valueExpectationFor(kw);
                        if (valueExpectation != null) {
                            if (c == ':') {
                                state = State.WS_BEFORE_VALUE;
                                continue;
                            } else {
                                state = State.WS_BEFORE_COLON;
                                continue;
                            }
                        } else {
                            throw new DatatypeException(
                                    "Bad media query: Expected a CSS media feature but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter, hyphen, colon or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_COLON:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (':' == c) {
                        state = State.WS_BEFORE_VALUE;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected whitespace or colon but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_VALUE:
                    if (isWhitespace(c)) {
                        continue;
                    } else {
                        zero = true;
                        switch (valueExpectation) {
                            case SCAN:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_STRING;
                                    continue;
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            default:
                                if ('1' <= c && '9' >= c) {
                                    zero = false;
                                    state = State.IN_VALUE_DIGITS;
                                    continue;
                                } else if ('0' == c) {
                                    state = State.IN_VALUE_DIGITS;
                                    continue;
                                } else if ('-' == c || '+' == c) {
                                    state = State.IN_VALUE_BEFORE_DIGITS;
                                    continue;
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a digit or a sign but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                        }
                    }
                case IN_VALUE_STRING:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("progressive".equals(kw) || "interlace".equals(kw))) {
                            throw new DatatypeException(
                                    "Bad media query: Expected \u201Cprogressive\u201D or \u201Cinterlace\u201C as the scan mode value but saw \u201C"
                                            + kw + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter, whitespace or \u201C)\u201D but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_VALUE_BEFORE_DIGITS:
                    if ('0' == c) {
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else if ('1' <= c && '9' >= c) {
                        zero = false;
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else {
                        switch (valueExpectation) {
                            case LENGTH:
                            case RESOLUTION:
                                if ('.' == c) {
                                    state = State.IN_VALUE_DIGITS_AFTER_DOT;
                                    continue;
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a dot or a digit but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case INTEGER:
                            case RATIO:
                                throw new DatatypeException(
                                        "Bad media query: Expected a digit but saw \u201C"
                                                + c + "\u201D instead.");
                            default:
                                throw new RuntimeException("Impossible state.");
                        }
                    }
                case IN_VALUE_DIGITS:
                    if ('0' == c) {
                        continue;
                    } else if ('1' <= c && '9' >= c) {
                        zero = false;
                        continue;
                    } else {
                        switch (valueExpectation) {
                            case LENGTH:
                            case RESOLUTION:
                                if ('.' == c) {
                                    state = State.IN_VALUE_DIGITS_AFTER_DOT;
                                    continue;
                                } else if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_UNIT;
                                    continue;
                                } else if (isWhitespace(c) || c == ')') {
                                    if (!zero) {
                                        if (valueExpectation == ValueType.LENGTH) {
                                            throw new DatatypeException("Bad media query: Non-zero lengths require a unit.");
                                        } else {
                                            throw new DatatypeException("Bad media query: Non-zero resolutions require a unit.");
                                        }
                                    }
                                    if (c == ')') {
                                        state = State.AFTER_CLOSE_PAREN;
                                        continue;
                                    } else {
                                        state = State.WS_BEFORE_CLOSE_PAREN;
                                        continue;
                                    }
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a letter, a dot or a digit but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case INTEGER:
                                if (c == ')') {
                                    state = State.AFTER_CLOSE_PAREN;
                                    continue;
                                } else if (isWhitespace(c)) {
                                    state = State.WS_BEFORE_CLOSE_PAREN;
                                    continue;
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a digit, whitespace or \u201C)\u201D but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case RATIO:
                                if (c == '/') {
                                    valueExpectation = ValueType.INTEGER;
                                    state = State.RATIO_SECOND_INTEGER_START;
                                    continue;
                                }
                            default:
                                throw new RuntimeException("Impossible state.");
                        }
                    }
                case IN_VALUE_DIGITS_AFTER_DOT:
                    if ('0' == c) {
                        state = State.IN_VALUE_DIGITS_AFTER_DOT_TRAIL;
                        continue;
                    } else if ('1' <= c && '9' >= c) {
                        state = State.IN_VALUE_DIGITS_AFTER_DOT_TRAIL;
                        zero = false;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a digit but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_VALUE_DIGITS_AFTER_DOT_TRAIL:
                    if ('0' == c) {
                        continue;
                    } else if ('1' <= c && '9' >= c) {
                        zero = false;
                        continue;
                    } else {
                        switch (valueExpectation) {
                            case LENGTH:
                            case RESOLUTION:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_UNIT;
                                    continue;
                                } else if (isWhitespace(c) || c == ')') {
                                    if (!zero) {
                                        if (valueExpectation == ValueType.LENGTH) {
                                            throw new DatatypeException("Bad media query: Non-zero lengths require a unit.");
                                        } else {
                                            throw new DatatypeException("Bad media query: Non-zero resolutions require a unit.");
                                        }
                                    }
                                    if (c == ')') {
                                        state = State.AFTER_CLOSE_PAREN;
                                        continue;
                                    } else {
                                        state = State.WS_BEFORE_CLOSE_PAREN;
                                        continue;
                                    }
                                } else {
                                    throw new DatatypeException(
                                            "Bad media query: Expected a letter, a digit, whitespace or \u201C)\u201D but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            default:
                                throw new RuntimeException("Impossible state.");
                        }
                    }
                case IN_VALUE_UNIT:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (valueExpectation == ValueType.LENGTH) {
                            if (!isLengthUnit(kw)) {
                                throw new DatatypeException(
                                        "Bad media query: Expected a length unit but saw \u201C"
                                                + c + "\u201D instead.");         
                            }
                        } else {
                            if (!("dpi".equals(kw) || "dpcm".equals(kw))) {
                                throw new DatatypeException(
                                        "Bad media query: Expected a resolution unit but saw \u201C"
                                                + c + "\u201D instead.");                                         
                            }
                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a letter, a dot or a digit but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case RATIO_SECOND_INTEGER_START:
                    valueExpectation = ValueType.INTEGER;
                    if ('1' <= c && '9' >= c) {
                        zero = false;
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else if ('0' == c) {
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else if ('-' == c || '+' == c) {
                        state = State.IN_VALUE_BEFORE_DIGITS;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected a digit or a sign but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case AFTER_CLOSE_PAREN:
                    if (isWhitespace(c)) {
                        state = State.WS_BEFORE_AND;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected whitespace but saw \u201C"
                                        + c + "\u201D instead.");                        
                    }
                case WS_BEFORE_CLOSE_PAREN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (c == ')') {
                        state = State.AFTER_CLOSE_PAREN;
                        continue;
                    } else {
                        throw new DatatypeException(
                                "Bad media query: Expected whitespace or \u201C)\u201D but saw \u201C"
                                        + c + "\u201D instead.");                        
                    }
            }
        }
        switch (state) {
            case AFTER_CLOSE_PAREN:
            case WS_BEFORE_AND:
                return;
            case IN_MEDIA_TYPE:
                String kw = sb.toString();
                sb.setLength(0);
                if (isMediaType(kw)) {
                    return;
                } else {
                    throw new DatatypeException(
                            "Bad media query: Expected a CSS media type but the query ended.");
                }
            default:
                throw new DatatypeException("Bad media query .");
        }
    }

    private ValueType valueExpectationFor(String feature) {
        return FEATURES_TO_VALUE_TYPES.get(feature);
    }

    private boolean isMediaType(String type) {
        return MEDIA_TYPES.contains(type);
    }
    
    private boolean isLengthUnit(String unit) {
        return LENGTH_UNITS.contains(unit);
    }

}
