// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.Arrays;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/fonts.html#propdef-font
 */
public class CssFont extends org.w3c.css.properties.css.CssFont {

    public static final CssIdent normal;
    public static final CssIdent[] systemFonts;

    static {
        String[] _systemFonts = {"caption", "icon", "menu",
                "message-box", "small-caption", "status-bar"};

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
    }

    /**
     * Creates a new CssFontSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFont(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        CssValue val;
        char op;

        boolean gotNormal = false;
        int state = 0;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = inherit;
                        break;
                    }
                    CssIdent ident;
                    ident = getSystemFont((CssIdent) val);
                    if (ident != null) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException(
                                    "system-font-keyword-not-sole-value",
                                    val.toString(), val.toString(), ac);
                        }
                        value = ident;
                        break;
                    }

                    // first font-style
                    ident = (CssIdent) val;
                    if (state == 0) {
                        // now check for possible font values
                        // first the strange case of 'normal'
                        // which sets up to three values...
                        // we keep it around for the final check
                        if (normal.equals((CssIdent) val)) {
                            gotNormal = true;
                            break;
                        }
                        int pos = CssFontStyle.allowed_values.indexOf(ident);
                        if (pos >= 0) {
                            if (fontStyle != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontStyle = new CssFontStyle();
                            fontStyle.value = CssFontStyle.allowed_values.get(pos);
                            break;
                        }
                        // font-variant
                        CssIdent v = CssFontVariant.getAllowedFontVariant(ident);
                        if (v != null) {
                            if (fontVariant != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontVariant = new CssFontVariant();
                            fontVariant.value = v;
                            break;
                        }
                        // font-weight
                        v = CssFontWeight.getAllowedValue(ident);
                        if (v != null) {
                            if (fontWeight != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontWeight = new CssFontWeight();
                            fontWeight.value = v;
                            break;
                        }
                    }

                    // check if we moved past and we now got
                    // a font-size
                    if (state == 0) {
                        CssIdent v = CssFontSize.getAllowedValue(ident);
                        if (v != null) {
                            // we got a FontSize, so no more style/variant/weight
                            state = 1;
                            if (fontSize != null) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            fontSize = new CssFontSize();
                            fontSize.value = v;
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
                    lineHeight = new CssLineHeight(ac, expression, false);
                    state = 1;
                    // expression.next is called, so continue instead
                    // of next
                    continue;

                case CssTypes.CSS_NUMBER:
                    CssCheckableValue c = (CssCheckableValue) val;
                    if (!c.isZero()) {
                        // must be a font-weight
                        if (state == 0 && fontWeight == null) {
                            fontWeight = new CssFontWeight(ac, expression, false);
                            // expression.next is called, so continue instead
                            // of next
                            continue;
                        }
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    // val is zero, it is a length.
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_LENGTH:
                    if (state == 0 && fontSize == null) {
                        fontSize = new CssFontSize(ac, expression, false);
                        state = 1;
                        // expression.next is called, so continue instead
                        // of next
                        continue;
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
                        // expression.next is called, so continue instead
                        // of next
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
        }
    }

    public CssFont(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

