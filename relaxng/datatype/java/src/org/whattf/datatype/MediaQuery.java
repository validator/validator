/*
 * Copyright (c) 2007-2010 Mozilla Foundation
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

import java.util.Arrays;
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

    private static final boolean WARN = System.getProperty(
            "org.whattf.datatype.warn", "").equals("true") ? true : false;

    private enum State {
        INITIAL_WS, OPEN_PAREN_SEEN, IN_ONLY_OR_NOT, IN_MEDIA_TYPE, IN_MEDIA_FEATURE, WS_BEFORE_MEDIA_TYPE, WS_BEFORE_AND, IN_AND, WS_BEFORE_EXPRESSION, WS_BEFORE_COLON, WS_BEFORE_VALUE, IN_VALUE_DIGITS, IN_VALUE_SCAN, IN_VALUE_ORIENTATION, WS_BEFORE_CLOSE_PAREN, IN_VALUE_UNIT, IN_VALUE_DIGITS_AFTER_DOT, RATIO_SECOND_INTEGER_START, IN_VALUE_BEFORE_DIGITS, IN_VALUE_DIGITS_AFTER_DOT_TRAIL, AFTER_CLOSE_PAREN, IN_VALUE_ONEORZERO
    }

    private enum ValueType {
        LENGTH, RATIO, INTEGER, RESOLUTION, SCAN, ORIENTATION, NONZEROINTEGER, ONEORZERO
    }

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
        MEDIA_TYPES.add("speech");
    }

    private enum MediaType {
        ALL, AURAL, BRAILLE, HANDHELD, PRINT, PROJECTION, SCREEN, TTY, TV, EMBOSSED, SPEECH, INVALID;
        private static MediaType toCaps(String str) {
            try {
                return valueOf(toAsciiUpperCase(str));
            } catch (Exception ex) {
                return INVALID;
            }
        }
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
    }

    private static final String[] visualFeatures = { "aspect-ratio", "color",
            "color-index", "device-aspect-ratio", "max-aspect-ratio",
            "max-color", "max-color-index", "max-device-aspect-ratio",
            "max-monochrome", "max-resolution", "min-aspect-ratio",
            "min-color", "min-color-index", "min-device-aspect-ratio",
            "min-monochrome", "min-resolution", "monochrome", "orientation",
            "resolution", };

    private static final String[] bitmapFeatures = { "aspect-ratio",
            "device-aspect-ratio", "max-aspect-ratio",
            "max-device-aspect-ratio", "max-resolution", "min-aspect-ratio",
            "min-device-aspect-ratio", "min-resolution", "orientation",
            "resolution", };

    private static final String scanWarning = "The media feature \u201cscan\u201d is applicable only to the media type \u201ctv\u201d. ";

    private MediaQuery() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        List<String> warnings = new ArrayList<String>();
        List<CharSequenceWithOffset> queries = split(literal, ',');
        for (CharSequenceWithOffset query : queries) {
            warnings = checkQuery(query.getSequence(), query.getOffset(),
                    warnings);
        }
        if (!warnings.isEmpty() && WARN) {
            StringBuilder sb = new StringBuilder();
            for (String s : warnings) {
                sb.append(s + " ");
            }
            throw newDatatypeException(sb.toString().trim(), WARN);
        }
    }

    private List<String> checkQuery(CharSequence query, int offset,
            List<String> warnings) throws DatatypeException {
        boolean containsAural = false;
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
                        throw newDatatypeException(
                                offset + i,
                                "Expected \u201C(\u201D or letter at start of a media query part but saw ",
                                c, " instead.");
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
                            throw newDatatypeException(offset + i,
                                    "Expected \u201Conly\u201D or \u201Cnot\u201D but saw \u201C"
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
                        if (isMediaType(type)) {
                            if ("aural".equals(type)) {
                                containsAural = true;
                            }
                            state = State.WS_BEFORE_AND;
                            continue;
                        } else {
                            throw newDatatypeException(offset + i,
                                    "Expected a CSS media type but saw \u201C"
                                            + type + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, hyphen or whitespace but saw \u201C"
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter or whitespace but saw \u201C"
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
                                "Expected \u201C(\u201D or whitespace but saw \u201C"
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter at start of a media feature part but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_MEDIA_FEATURE:
                    if (('a' <= c && 'z' >= c) || c == '-') {
                        sb.append(c);
                        continue;
                    } else if (c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        checkApplicability(offset + i, kw, type, warnings);
                        checkIfValueRequired(offset + i, kw);
                        state = State.AFTER_CLOSE_PAREN;
                        continue;
                    } else if (isWhitespace(c) || c == ':') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        checkApplicability(offset + i, kw, type, warnings);
                        feature = kw;
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
                            throw newDatatypeException(offset + i,
                                    "Expected a CSS media feature but saw \u201C"
                                            + kw + "\u201D instead.");
                        }
                    } else {
                        throw newDatatypeException(offset + i,
                                "Expected a letter, hyphen, colon or whitespace but saw \u201C"
                                        + c + "\u201D instead.");
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
                                    throw newDatatypeException(
                                            offset + i,
                                            "Expected \u201C0\u201D or \u201C1\u201D as \u201c"
                                                    + feature
                                                    + "\u201d value but found \u201C"
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
                                } else if (valueExpectation == ValueType.LENGTH) {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, a dot or a plus sign but saw \u201C"
                                                    + c + "\u201D instead.");
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit or a plus sign but saw \u201C"
                                                    + c + "\u201D instead.");
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
                        if (!("progressive".equals(kw) || "interlace".equals(kw))) {
                            throw newDatatypeException(
                                    offset + i,
                                    "Expected \u201Cprogressive\u201D or \u201Cinterlace\u201D as the scan mode value but saw \u201C"
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
                                "Expected a letter, whitespace or \u201C)\u201D but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case IN_VALUE_ORIENTATION:
                    if ('a' <= c && 'z' >= c) {
                        sb.append(c);
                        continue;
                    } else if (isWhitespace(c) || c == ')') {
                        String kw = sb.toString();
                        sb.setLength(0);
                        if (!("portrait".equals(kw) || "landscape".equals(kw))) {
                            throw newDatatypeException(
                                    offset + i,
                                    "Expected \u201Cportrait\u201D or \u201Clandscape\u201D as the \u201corientation\u201d value but saw \u201C"
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
                                "Expected a letter, whitespace or \u201C)\u201D but saw \u201C"
                                        + c + "\u201D instead.");
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
                                "Expected \u201C0\u201D or \u201C1\u201D as \u201c"
                                        + feature
                                        + "\u201d value but saw \u201C" + kw
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
                                            "Expected a dot or a digit but saw \u201C"
                                                    + c + "\u201D instead.");
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
                                            throw newDatatypeException(offset
                                                    + i,
                                                    "Non-zero lengths require a unit.");
                                        } else {
                                            throw newDatatypeException(offset
                                                    + i,
                                                    "Non-zero resolutions require a unit.");
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
                                            "Expected a letter, a dot or a digit but saw \u201C"
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
                                            "Expected a digit, whitespace or \u201C)\u201D but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case NONZEROINTEGER:
                                if (c == ')') {
                                    if (zero) {
                                        throw newDatatypeException(offset + i,
                                                "Expected a non-zero positive integer.");
                                    }
                                    state = State.AFTER_CLOSE_PAREN;
                                    continue;
                                } else if (isWhitespace(c)) {
                                    state = State.WS_BEFORE_CLOSE_PAREN;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit, whitespace or \u201C)\u201D but saw \u201C"
                                                    + c + "\u201D instead.");
                                }
                            case RATIO:
                                if (c == '/') {
                                    if (zero) {
                                        throw newDatatypeException(offset + i,
                                                "Expected non-zero positive integer in ratio value.");
                                    }
                                    valueExpectation = ValueType.NONZEROINTEGER;
                                    state = State.RATIO_SECOND_INTEGER_START;
                                    continue;
                                } else {
                                    throw newDatatypeException(offset + i,
                                            "Expected a digit or \u201C/\u201D for"
                                                    + feature
                                                    + " value but saw \u201C"
                                                    + c + "\u201D instead.");
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
                                            throw newDatatypeException(offset
                                                    + i,
                                                    "Non-zero lengths require a unit.");
                                        } else {
                                            throw newDatatypeException(offset
                                                    + i,
                                                    "Non-zero resolutions require a unit.");
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
                                            "Expected a letter, a digit, whitespace or \u201C)\u201D but saw \u201C"
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
                                throw newDatatypeException(offset + i,
                                        "Expected a length unit but saw \u201C"
                                                + c + "\u201D instead.");
                            }
                        } else {
                            if (!("dpi".equals(kw) || "dpcm".equals(kw))) {
                                throw newDatatypeException(offset + i,
                                        "Expected a resolution unit but saw \u201C"
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
                        throw newDatatypeException(offset + i,
                                "Expected a letter, a dot or a digit but saw \u201C"
                                        + c + "\u201D instead.");
                    }
                case RATIO_SECOND_INTEGER_START:
                    valueExpectation = ValueType.NONZEROINTEGER;
                    if ('1' <= c && '9' >= c) {
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
                                "Expected a digit or a plus sign for "
                                        + feature + " value but saw \u201C" + c
                                        + "\u201D instead.");
                    }
                case AFTER_CLOSE_PAREN:
                    if (isWhitespace(c)) {
                        state = State.WS_BEFORE_AND;
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
                                "Expected whitespace or \u201C)\u201D but saw \u201C"
                                        + c + "\u201D instead.");
                    }
            }
        }
        switch (state) {
            case AFTER_CLOSE_PAREN:
            case WS_BEFORE_AND:
                if (containsAural && WARN) {
                    warnings.add("The media type \u201caural\u201d is deprecated. Use \u201cspeech\u201d instead. ");
                }
                return warnings;
            case IN_MEDIA_TYPE:
                String kw = sb.toString();
                sb.setLength(0);
                if (isMediaType(kw)) {
                    if ("aural".equals(kw) && WARN) {
                        warnings.add("The media type \u201caural\u201d is deprecated. Use \u201cspeech\u201d instead. ");
                    }
                    return warnings;
                } else {
                    throw newDatatypeException("Expected a CSS media type but the query ended.");
                }
            default:
                throw newDatatypeException("Media query ended prematurely.");
        }
    }

    private boolean isMediaFeature(String feature) {
        return FEATURES_TO_VALUE_TYPES.containsKey(feature);
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

    private List<String> checkApplicability(int index, String feature,
            String type, List<String> warnings) throws DatatypeException {
        if (!isMediaType(type)) {
            return warnings;
        }
        if (!isMediaFeature(feature)) {
            throw newDatatypeException(index,
                    "Expected a CSS media feature but saw \u201C" + feature
                            + "\u201D instead.");
        }
        if ("scan".equals(feature) && !"tv".equals(type)) {
            warnings.add(scanWarning);
            return warnings;
        }
        switch (MediaType.toCaps(type)) {
            case SPEECH:
                warnings.add("The media feature \u201c"
                        + feature
                        + "\u201d is not applicable to the media type \u201cspeech\u201d. ");
                return warnings;
            case BRAILLE:
            case EMBOSSED:
                if (Arrays.binarySearch(visualFeatures, feature) > -1) {
                    warnings.add("The visual media feature \u201c"
                            + feature
                            + "\u201d is not applicable to the tactile media type \u201c"
                            + type + "\u201d. ");
                }
                return warnings;
            case TTY:
                if (Arrays.binarySearch(bitmapFeatures, feature) > -1) {
                    warnings.add("The bitmap media feature \u201c"
                            + feature
                            + "\u201d is not applicable to the media type \u201ctty\u201d. ");
                }
                return warnings;
            default:
                return warnings;
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

    @Override public String getName() {
        return "media query";
    }

}
