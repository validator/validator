// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.Arrays;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font
 */
public class CssFont extends org.w3c.css.properties.css.CssFont {

    public CssFontStretch fontStretch = null;

    public static final CssIdent normal;
    public static final CssIdent[] systemFonts;
    static final String[] _systemFonts = {"caption", "icon", "menu",
            "message-box", "small-caption", "status-bar"};

    static {
        normal = CssIdent.getIdent("normal");
        systemFonts = new CssIdent[_systemFonts.length];
        int i = 0;
        for (String s : _systemFonts) {
            systemFonts[i++] = CssIdent.getIdent(s);
        }
        Arrays.sort(systemFonts);
    }

    public static final CssIdent getSystemFont(CssIdent ident) {
        int idx = Arrays.binarySearch(systemFonts, ident);
        if (idx >= 0) {
            return systemFonts[idx];
        }
        return null;
    }

    /**
     * Create a new CssFontSize
     */
    public CssFont() {
        value = initial;
    }

    /**
     * Creates a new CssFontSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFont(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        CssValue val;
        char op;
        CssExpression nex;

        boolean gotNormal = false;
        int state = 0;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (getSystemFont(id) != null) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException(
                                    "system-font-keyword-not-sole-value",
                                    val.toString(), val.toString(), ac);
                        }
                        value = val;
                        break;
                    }

                    // first font-style
                    if (state == 0) {
                        // now check for possible font values
                        // first the strange case of 'normal'
                        // which sets up to four values...
                        // we keep it around for the final check
                        if (normal.equals(id)) {
                            gotNormal = true;
                            break;
                        }
                        if (CssFontStyle.getMatchingIdent(id) != null) {
                            if (fontStyle != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            CssValue nextVal = expression.getNextValue();
                            if (nextVal != null && (nextVal.getType() == CssTypes.CSS_ANGLE)) {
                                nex = new CssExpression();
                                nex.addValue(val);
                                expression.next();
                                nex.addValue(nextVal);
                                try {
                                    fontStyle = new CssFontStyle(ac, nex, false);
                                } catch (Exception ex) {
                                    throw new InvalidParamException("value",
                                            val.toString(),
                                            getPropertyName(), ac);
                                }
                            } else {
                                fontStyle = new CssFontStyle();
                                fontStyle.value = val;
                            }
                            break;
                        }
                        // font-variant
                        if (org.w3c.css.properties.css21.CssFontVariant.getAllowedFontVariant(id) != null) {
                            if (fontVariant != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontVariant = new CssFontVariant();
                            fontVariant.value = val;
                            break;
                        }
                        // font-weight
                        if (CssFontWeight.getAllowedValue(id) != null) {
                            if (fontWeight != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontWeight = new CssFontWeight();
                            fontWeight.value = val;
                            break;
                        }
                        // font-stretch
                        if (CssFontStretch.getAllowedValue(id) != null) {
                            if (fontStretch != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontStretch = new CssFontStretch();
                            fontStretch.value = val;
                            break;
                        }
                    }

                    // check if we moved past and we now got
                    // a font-size
                    if (state == 0) {
                        if (CssFontSize.getAllowedValue(id) != null) {
                            // we got a FontSize, so no more style/variant/weight
                            state = 1;
                            if (fontSize != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontSize = new CssFontSize();
                            fontSize.value = val;
                            break;
                        }
                    }
                    // still nothing? It must be a font-family then...
                    // let the fun begin ;)
                    if (state == 1) {
                        fontFamily = new CssFontFamily(ac, expression, check);
                        state = 2;
                        // expression.next is called, so continue instead
                        // of next
                        continue;
                    }
                    // unrecognized
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_SWITCH:
                    // sanity check, it must happen only after a fontSize
                    // and we should not have two of them.
                    if (fontSize == null || state != 1 || lineHeight != null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    expression.next();
                    if (expression.end()) {
                        throw new InvalidParamException("value",
                                expression.toString(),
                                getPropertyName(), ac);
                    }
                    // let's parse a line-height
                    nex = new CssExpression();
                    nex.addValue(expression.getValue());
                    lineHeight = new CssLineHeight(ac, nex, false);
                    state = 1;
                    break;

                case CssTypes.CSS_NUMBER:
                    // TODO FIXME case of var with unknown numerical value...
                    if (!val.getCheckableValue().isZero()) {
                        // must be a font-weight
                        if (state == 0 && fontWeight == null) {
                            nex = new CssExpression();
                            nex.addValue(val);
                            fontWeight = new CssFontWeight(ac, nex, false);
                            break;
                        }
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    // val is zero, it is a length.
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_LENGTH:
                    if (state == 0 && fontSize == null) {
                        nex = new CssExpression();
                        nex.addValue(val);
                        fontSize = new CssFontSize(ac, nex, false);
                        state = 1;
                        break;
                    }
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_STRING:
                    // font-family can happen only after 'font-size' and possible '/ line-height'
                    // in both cases state is 1
                    if (state == 1) {
                        fontFamily = new CssFontFamily(ac, expression, check);
                        state = 2;
                        continue;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            expression.next();
        }
        if (gotNormal) {
            if (fontSize == null) {
                fontSize = new CssFontSize();
                fontSize.value = normal;
            }
            if (fontVariant == null) {
                fontVariant = new CssFontVariant();
                fontVariant.value = normal;
            }
            if (fontWeight == null) {
                fontWeight = new CssFontWeight();
                fontWeight.value = normal;
            }
            if (fontStretch == null) {
                fontStretch = new CssFontStretch();
                fontStretch.value = normal;
            }
        }
    }

    public CssFont(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        if (fontStyle != null) {
            sb.append(fontStyle);
            first = false;
        }
        if (fontVariant != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontVariant);
        }
        if (fontWeight != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontWeight);
        }
        if (fontStretch != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontStretch);
        }

        // no need to test, if we are here we should have one!
        if (fontSize != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontSize);
        }
        if (lineHeight != null) {
            sb.append('/');
            sb.append(lineHeight);
        }
        // should always be there...
        if (fontFamily != null) {
            sb.append(' ');
            sb.append(fontFamily);
        }
        return sb.toString();
    }

    @Override
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        if (fontStretch != null) {
            fontStretch.addToStyle(ac, style);
        }
    }
}

