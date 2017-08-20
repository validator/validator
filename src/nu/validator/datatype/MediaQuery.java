/*
 * Copyright (c) 2007-2017 Mozilla Foundation
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.relaxng.datatype.DatatypeException;

public class MediaQuery extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MediaQuery THE_INSTANCE = new MediaQuery();

    private static final boolean WARN = System.getProperty("nu.validator.datatype.warn", "").equals("true");

    private enum State {
        INITIAL_WS, OPEN_PAREN_SEEN, IN_ONLY_OR_NOT, IN_MEDIA_TYPE, IN_MEDIA_FEATURE, WS_BEFORE_MEDIA_TYPE, WS_BEFORE_MEDIA_FEATURE, WS_BEFORE_AND, IN_AND, WS_BEFORE_EXPRESSION, WS_BEFORE_COLON, WS_BEFORE_VALUE, IN_VALUE_DIGITS, BEFORE_CALC_OPEN_PAREN, IN_CALC, IN_VALUE_SCAN, IN_VALUE_ORIENTATION, WS_BEFORE_CLOSE_PAREN, IN_VALUE_UNIT, IN_VALUE_DIGITS_AFTER_DOT, RATIO_SECOND_INTEGER_START, IN_VALUE_BEFORE_DIGITS, IN_VALUE_DIGITS_AFTER_DOT_TRAIL, AFTER_CLOSE_PAREN, IN_VALUE_ONEORZERO, //
        IN_VALUE_HOVER, IN_VALUE_ANYHOVER, IN_VALUE_POINTER, IN_VALUE_ANYPOINTER
    }

    private enum ValueType {
        LENGTH, RATIO, INTEGER, RESOLUTION, SCAN, ORIENTATION, NONZEROINTEGER, ONEORZERO, //
        HOVER, ANYHOVER, POINTER, ANYPOINTER
    }

    private static final Set<String> LENGTH_UNITS = new HashSet<>();

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

    private static final Set<String> MEDIA_TYPES = new HashSet<>();

    static {
        MEDIA_TYPES.add("all");
        MEDIA_TYPES.add("print");
        MEDIA_TYPES.add("screen");
        MEDIA_TYPES.add("speech");
    }

    private enum MediaType {
        ALL, PRINT, SCREEN, SPEECH, INVALID;
        private static MediaType toCaps(String str) {
            try {
                return valueOf(toAsciiUpperCase(str));
            } catch (Exception ex) {
                return INVALID;
            }
        }
    }

    private static final Set<String> OLD_MEDIA_TYPES = new HashSet<>();

    static {
        OLD_MEDIA_TYPES.add("aural");
        OLD_MEDIA_TYPES.add("braille");
        OLD_MEDIA_TYPES.add("embossed");
        OLD_MEDIA_TYPES.add("handheld");
        OLD_MEDIA_TYPES.add("projection");
        OLD_MEDIA_TYPES.add("tty");
        OLD_MEDIA_TYPES.add("tv");
    }

    private static final Set<String> OLD_MEDIA_FEATURES = new HashSet<>();

    static {
        OLD_MEDIA_FEATURES.add("device-width");
        OLD_MEDIA_FEATURES.add("min-device-width");
        OLD_MEDIA_FEATURES.add("max-device-width");
        OLD_MEDIA_FEATURES.add("device-height");
        OLD_MEDIA_FEATURES.add("min-device-height");
        OLD_MEDIA_FEATURES.add("max-device-height");
        OLD_MEDIA_FEATURES.add("device-aspect-ratio");
        OLD_MEDIA_FEATURES.add("min-device-aspect-ratio");
        OLD_MEDIA_FEATURES.add("max-device-aspect-ratio");
    }

    private static final Map<String, ValueType> FEATURES_TO_VALUE_TYPES = new HashMap<>();

    static {
        FEATURES_TO_VALUE_TYPES.put("width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-width", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("min-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("max-height", ValueType.LENGTH);
        FEATURES_TO_VALUE_TYPES.put("aspect-ratio", ValueType.RATIO);
        FEATURES_TO_VALUE_TYPES.put("min-aspect-ratio", ValueType.RATIO);
        FEATURES_TO_VALUE_TYPES.put("max-aspect-ratio", ValueType.RATIO);
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
        FEATURES_TO_VALUE_TYPES.put("orientation", ValueType.ORIENTATION);
        FEATURES_TO_VALUE_TYPES.put("grid", ValueType.ONEORZERO);
        FEATURES_TO_VALUE_TYPES.put("hover", ValueType.HOVER);
        FEATURES_TO_VALUE_TYPES.put("any-hover", ValueType.ANYHOVER);
        FEATURES_TO_VALUE_TYPES.put("pointer", ValueType.POINTER);
        FEATURES_TO_VALUE_TYPES.put("any-pointer", ValueType.ANYPOINTER);
    }

    private static final Map<String, ValueType> NONSTANDARD_FEATURES_TO_VALUE_TYPES = new HashMap<>();

    static {
        NONSTANDARD_FEATURES_TO_VALUE_TYPES.put(
                "-webkit-min-device-pixel-ratio", ValueType.INTEGER);
    }

    protected MediaQuery() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        List<String> warnings = new ArrayList<>();
        List<CharSequenceWithOffset> queries = split(literal, ',');
        for (CharSequenceWithOffset query : queries) {
            warnings = checkQuery(query.getSequence(), query.getOffset(),
                    warnings);
        }
        if (!warnings.isEmpty() && WARN) {
            StringBuilder sb = new StringBuilder();
            for (String s : warnings) {
                sb.append(s).append(" ");
            }
            throw newDatatypeException(sb.toString().trim(), WARN);
        }
    }

    private List<String> checkQuery(CharSequence query, int offset,
            List<String> warnings) throws DatatypeException {
        int unmatchedParen = -1;
        int unmatchedCalcParen = -1;
        boolean zero = true;
        String type = null;
        String feature = null;
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
                        throw newDatatypeException(offset + i,
                                "Expected \u201C(\u201D or letter at start of a"
                                        + " media query part but saw ",
                                c, " instead.");
                    }
                case IN_ONLY_OR_NOT:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c)) {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if ("only".equals(kw)) {
                            if (isMediaCondition()) {
                                throw newDatatypeException(offset + i,
                                        "Expected a CSS media condition (not a"
                                                + " CSS media type) but saw"
                                                + " \u201Conly\u201D instead.");
                            }
                            state = State.WS_BEFORE_MEDIA_TYPE;
                            continue;
                        } else if ("not".equals(kw)) {
                            if (isMediaCondition()) {
                                state = State.WS_BEFORE_MEDIA_FEATURE;
                            } else {
                                state = State.WS_BEFORE_MEDIA_TYPE;
                            }
                            continue;
                        } else {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Conly\u201D or"
                                            + " \u201Cnot\u201D but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter or whitespace but saw \u201C"
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_MEDIA_FEATURE:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('(' == c) {
                        state = State.OPEN_PAREN_SEEN;
                        continue;
                    } else if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        state = State.IN_MEDIA_TYPE;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected \u201C(\u201D or"
                                        + " whitespace but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_MEDIA_TYPE:
                    if (('a' <= c && 'z' >= c) || c == '-') {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c)) {
                        /*
                         * store media type for later media-feature
                         * applicability check
                         */
                        type = sb.toString();
                        sb.setLength(0);
                        if (isMediaCondition()) {
                            errNotMediaCondition(type);
                        }
                        if (isMediaType(type)) {
                            state = State.WS_BEFORE_AND;
                            continue;
                        } else if (isOldMediaType(type)) {
                            throw newDatatypeException(offset + i,
                                    "Deprecated media type \u201C" + type
                                            + "\u201D. For guidance, see the"
                                            + " Media Types section in the"
                                            + " current Media Queries"
                                            + " specification.");
                        } else {
                            throw newDatatypeException(offset + i,
                                    "Expected a CSS media type but saw \u201C"
                                            + type + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, hyphen or whitespace but"
                                        + " saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case WS_BEFORE_AND:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('a' == c) {
                        sb.append(c);
                        state = State.IN_AND;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected whitespace or \u201Cand\u201D but saw"
                                        + " \u201C" + c + "\u201D instead.");
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
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cand\u201D but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case WS_BEFORE_EXPRESSION:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('(' == c) {
                        state = State.OPEN_PAREN_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected \u201C(\u201D or whitespace but saw"
                                        + " \u201C" + c + "\u201D instead.");
                    }
                case OPEN_PAREN_SEEN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('(' == c) {
                        unmatchedParen++;
                        continue;
                    } else if ('-' == c || ('a' <= c && 'z' >= c)) {
                        sb.append(c);
                        state = State.IN_MEDIA_FEATURE;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter at start of a media feature"
                                        + " part but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_MEDIA_FEATURE:
                    if (('a' <= c && 'z' >= c) || c == '-') {
                        sb.append(c);
                        continue;
                    } else if (c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!isMediaCondition()) {
                            checkApplicability(offset + i, kw, type, warnings);
                        }
                        checkIfValueRequired(offset + i, kw);
                        state = State.AFTER_CLOSE_PAREN;
                        continue;
                    } else if (isWhitespace(c) || c == ':') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!isMediaCondition()) {
                            checkApplicability(offset + i, kw, type, warnings);
                        }
                        feature = kw;
                        valueExpectation = valueExpectationFor(kw) != null
                                ? valueExpectationFor(kw)
                                : nonstandardValueExpectationFor(kw);
                        if (valueExpectation != null) {
                            if (c == ':') {
                                state = State.WS_BEFORE_VALUE;
                                continue;
                            } else {
                                state = State.WS_BEFORE_COLON;
                                continue;
                            }
                        } else {
                            throw newDatatypeException(offset + i,
                                    "Expected a CSS media feature but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, hyphen, colon or whitespace"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case WS_BEFORE_COLON:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (':' == c) {
                        state = State.WS_BEFORE_VALUE;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected whitespace or colon but saw \u201C"
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
                                    state = State.IN_VALUE_SCAN;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case ORIENTATION:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_ORIENTATION;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case ONEORZERO:
                                if (c == '0' || c == '1') {
                                    sb.append(c);
                                    state = State.IN_VALUE_ONEORZERO;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected \u201C0\u201D or"
                                                    + " \u201C1\u201D as \u201c"
                                                    + feature
                                                    + "\u201d value but found"
                                                    + " \u201C" + c
                                                    + "\u201D instead.");
                                }
                            case HOVER:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_HOVER;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case ANYHOVER:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_ANYHOVER;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case POINTER:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_POINTER;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case ANYPOINTER:
                                if ('a' <= c && 'z' >= c) {
                                    sb.append(c);
                                    state = State.IN_VALUE_ANYPOINTER;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter but saw \u201C"
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
                                } else if ('+' == c) {
                                    state = State.IN_VALUE_BEFORE_DIGITS;
                                    continue;
                                } else if ('.' == c
                                        && valueExpectation == ValueType.LENGTH) {
                                    state = State.IN_VALUE_DIGITS_AFTER_DOT;
                                    continue;
                                } else if ('c' == c) {
                                    state = State.BEFORE_CALC_OPEN_PAREN;
                                    continue;
                                } else if (valueExpectation == ValueType.LENGTH) {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, a dot or a plus"
                                                    + " sign but saw \u201C" + c
                                                    + "\u201D instead.");
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit or a plus sign"
                                                    + " but saw \u201C" + c
                                                    + "\u201D instead.");
                                }
                        }
                    }
                case IN_VALUE_SCAN:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("progressive".equals(kw)
                                || "interlace".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cprogressive\u201D or"
                                            + " \u201Cinterlace\u201D as the"
                                            + " scan mode value but saw \u201C"
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or"
                                        + " \u201C)\u201D but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_VALUE_ORIENTATION:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("portrait".equals(kw)
                                || "landscape".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cportrait\u201D or"
                                            + " \u201Clandscape\u201D as the"
                                            + " \u201corientation\u201d value"
                                            + " but saw" + " \u201C" + kw
                                            + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or \u201C)\u201D"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_VALUE_ONEORZERO:
                    if (isWhitespace(c) || c == ')') {
                        sb.setLength(0);
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        sb.append(c);
                        String kw = sb.toString();
                        throw newDatatypeException(offset + i,
                                "Expected \u201C0\u201D or \u201C1\u201D as"
                                        + " \u201c" + feature
                                        + "\u201d value but saw \u201C" + kw
                                        + "\u201D instead.");
                    }
                case IN_VALUE_HOVER:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("none".equals(kw) //
                                || "hover".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cnone\u201D or"
                                            + " \u201Chover\u201D as the"
                                            + " \u201chover\u201d value"
                                            + " but saw" + " \u201C" + kw
                                            + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or \u201C)\u201D"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_VALUE_ANYHOVER:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("none".equals(kw) //
                                || "hover".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cnone\u201D or"
                                            + " \u201Chover\u201D as the"
                                            + " \u201cany-hover\u201d value"
                                            + " but saw" + " \u201C" + kw
                                            + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or \u201C)\u201D"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_VALUE_POINTER:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("none".equals(kw) //
                                || "coarse".equals(kw) //
                                || "fine".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cnone\u201D or"
                                            + " \u201Ccoarse\u201D or"
                                            + " \u201Cfine\u201D as the"
                                            + " \u201cpointer\u201d value"
                                            + " but saw" + " \u201C" + kw
                                            + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or \u201C)\u201D"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_VALUE_ANYPOINTER:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("none".equals(kw) //
                                || "coarse".equals(kw) //
                                || "fine".equals(kw))) {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Cnone\u201D or"
                                            + " \u201Ccoarse\u201D or"
                                            + " \u201Cfine\u201D as the"
                                            + " \u201cany-pointer\u201d value"
                                            + " but saw" + " \u201C" + kw
                                            + "\u201D instead.");

                        }
                        if (c == ')') {
                            state = State.AFTER_CLOSE_PAREN;
                            continue;
                        } else {
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, whitespace or \u201C)\u201D"
                                        + " but saw \u201C" + c
                                        + "\u201D instead.");
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
                                    throw newDatatypeException(offset + i,
                                            "Expected a dot or a digit but saw"
                                                    + " \u201C" + c
                                                    + "\u201D instead.");
                                }
                            case INTEGER:
                            case RATIO:
                                throw newDatatypeException(offset + i,
                                        "Expected a digit but saw \u201C" + c
                                                + "\u201D instead.");
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
                                            throw newDatatypeException(
                                                    offset + i,
                                                    "Non-zero lengths require"
                                                            + " a unit.");
                                        } else {
                                            throw newDatatypeException(
                                                    offset + i,
                                                    "Non-zero resolutions"
                                                            + " require a unit.");
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
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter, a dot or a"
                                                    + " digit but saw \u201C"
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
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, whitespace or"
                                                    + " \u201C)\u201D but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case NONZEROINTEGER:
                                if (c == ')') {
                                    if (zero) {
                                        throw newDatatypeException(offset + i,
                                                "Expected a non-zero positive"
                                                        + " integer.");
                                    }
                                    state = State.AFTER_CLOSE_PAREN;
                                    continue;
                                } else if (isWhitespace(c)) {
                                    state = State.WS_BEFORE_CLOSE_PAREN;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, whitespace or"
                                                    + " \u201C)\u201D but saw"
                                                    + " \u201C" + c
                                                    + "\u201D instead.");
                                }
                            case RATIO:
                                if (isWhitespace(c)) {
                                    continue;
                                } else if (c == '/') {
                                    if (zero) {
                                        throw newDatatypeException(offset + i,
                                                "Expected non-zero positive"
                                                        + " integer in ratio"
                                                        + " value.");
                                    }
                                    valueExpectation = ValueType.NONZEROINTEGER;
                                    state = State.RATIO_SECOND_INTEGER_START;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, whitespace or"
                                                    + " \u201C/\u201D for "
                                                    + feature
                                                    + " value but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            default:
                                throw new RuntimeException("Impossible state.");
                        }
                    }
                case BEFORE_CALC_OPEN_PAREN:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if ('(' == c) {
                        unmatchedCalcParen = 1;
                        String kw = sb.toString();
                        sb.setLength(0);
                        if ("alc".equals(kw)) {
                            state = State.IN_CALC;
                            continue;
                        } else {
                            throw newDatatypeException(offset + i,
                                    "Expected \u201ccalc\u201d but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected \u201ccalc\u201d but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case IN_CALC:
                    if (')' == c) {
                        if (unmatchedCalcParen == 1) {
                            unmatchedCalcParen = -1;
                            state = State.WS_BEFORE_CLOSE_PAREN;
                            continue;
                        } else {
                            unmatchedCalcParen--;
                            continue;
                        }
                    } else if ('(' == c) {
                        unmatchedCalcParen++;
                        continue;
                    } else {
                        continue;
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
                        throw newDatatypeException(offset + i,
                                "Expected a digit but saw \u201C" + c
                                        + "\u201D instead.");
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
                                            throw newDatatypeException(
                                                    offset + i,
                                                    "Non-zero lengths require a"
                                                            + " unit.");
                                        } else {
                                            throw newDatatypeException(
                                                    offset + i,
                                                    "Non-zero resolutions require"
                                                            + " a unit.");
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
                                    throw newDatatypeException(offset + i,
                                            "Expected a letter, a digit,"
                                                    + " whitespace or"
                                                    + " \u201C)\u201D"
                                                    + " but saw \u201C" + c
                                                    + "\u201D instead.");
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
                                throw newDatatypeException(offset + i,
                                        "Expected a length unit but saw \u201C"
                                                + c + "\u201D instead.");
                            }
                        } else {
                            if (!("dpi".equals(kw) || "dpcm".equals(kw))) {
                                throw newDatatypeException(offset + i,
                                        "Expected a resolution unit but saw"
                                                + " \u201C" + c
                                                + "\u201D instead.");
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter, a dot or a digit but saw"
                                        + " \u201C" + c + "\u201D instead.");
                    }
                case RATIO_SECOND_INTEGER_START:
                    valueExpectation = ValueType.NONZEROINTEGER;
                    if (isWhitespace(c)) {
                        continue;
                    } else if ('1' <= c && '9' >= c) {
                        zero = false;
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else if ('0' == c) {
                        zero = true;
                        state = State.IN_VALUE_DIGITS;
                        continue;
                    } else if ('+' == c) {
                        state = State.IN_VALUE_BEFORE_DIGITS;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a digit, whitespace or a plus sign"
                                        + " for " + feature
                                        + " value but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case AFTER_CLOSE_PAREN:
                    if (isWhitespace(c)) {
                        state = State.WS_BEFORE_AND;
                        continue;
                    } else if (')' == c) {
                        if (unmatchedParen == 1) {
                            unmatchedParen = -1;
                        } else {
                            unmatchedParen--;
                        }
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected whitespace but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case WS_BEFORE_CLOSE_PAREN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (c == ')') {
                        state = State.AFTER_CLOSE_PAREN;
                        continue;
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected whitespace or \u201C)\u201D but saw"
                                        + " \u201C" + c + "\u201D instead.");
                    }
            }
        }
        switch (state) {
            case AFTER_CLOSE_PAREN:
            case WS_BEFORE_AND:
                return warnings;
            case IN_MEDIA_TYPE:
                String kw = sb.toString();
                sb.setLength(0);
                if (isMediaCondition()) {
                    errNotMediaCondition(kw);
                }
                if (isMediaType(kw)) {
                    return warnings;
                } else if (isOldMediaType(kw)) {
                    throw newDatatypeException("Deprecated media type \u201C"
                            + kw + "\u201D. For guidance, see the Media Types"
                            + " section in the current Media Queries"
                            + " specification.");
                } else {
                    throw newDatatypeException("Expected a CSS media type but"
                            + " the query ended.");
                }
            default:
                throw newDatatypeException("Media query ended prematurely.");
        }
    }

    private boolean isMediaFeature(String feature) {
        return FEATURES_TO_VALUE_TYPES.containsKey(feature);
    }

    private boolean isOldMediaFeature(String type) {
        return OLD_MEDIA_FEATURES.contains(type);
    }

    private ValueType valueExpectationFor(String feature) {
        return FEATURES_TO_VALUE_TYPES.get(feature);
    }

    private ValueType nonstandardValueExpectationFor(String feature) {
        return NONSTANDARD_FEATURES_TO_VALUE_TYPES.get(feature);
    }

    private boolean isMediaType(String type) {
        return MEDIA_TYPES.contains(type);
    }

    private boolean isOldMediaType(String type) {
        return OLD_MEDIA_TYPES.contains(type);
    }

    private boolean isLengthUnit(String unit) {
        return LENGTH_UNITS.contains(unit);
    }

    private List<String> checkApplicability(int index, String feature,
            String type, List<String> warnings) throws DatatypeException {
        if (!isMediaType(type)) {
            return warnings;
        }
        if (isOldMediaFeature(feature)) {
            throw newDatatypeException(index,
                    "Deprecated media feature \u201C" + feature
                            + "\u201D. For guidance, see the Deprecated Media"
                            + " Features section in the current Media Queries"
                            + " specification.");
        }
        if (!isMediaFeature(feature)) {
            throw newDatatypeException(index,
                    "Expected a CSS media feature but saw \u201C" + feature
                            + "\u201D instead.");
        }
        switch (MediaType.toCaps(type)) {
            case SPEECH:
                warnings.add("The media feature \u201c" + feature
                        + "\u201d is not applicable to the media type"
                        + " \u201cspeech\u201d.");
                return warnings;
            default:
                return warnings;
        }
    }

    private void errNotMediaCondition(String type) throws DatatypeException {
        if (isMediaType(type) || isOldMediaType(type)) {
            throw newDatatypeException("Expected a CSS media condition but saw"
                    + " CSS media type ", type, " instead.");
        } else {
            throw newDatatypeException(
                    "Expected a CSS media condition but saw" + " ", type,
                    " instead.");
        }
    }

    private void checkIfValueRequired(int index, String feature)
            throws DatatypeException {
        if (feature.startsWith("min-") || feature.startsWith("max-")) {
            throw newDatatypeException(index,
                    "Expected a value for the media feature \u201C" + feature
                            + "\u201D.");
        }
    }

    protected boolean isMediaCondition() {
        return false;
    }

    @Override
    public String getName() {
        return "media query";
    }

}
