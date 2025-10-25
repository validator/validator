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
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variant
 */
public class CssFontVariant extends org.w3c.css.properties.css.CssFontVariant {

    public static final CssIdent normal;

    static {
        normal = CssIdent.getIdent("normal");
    }

    // we can't parse font-variant-* in one pass
    // as the values might be spread all over the shorthand :(
    // so we need to care for all the possible values, then reconstruct
    // the whole thing.

    // best bet should be to test things twice, building CssExpression for
    // only 5 properties instead of 18 variables...

    CssFontVariantAlternates altValue = null;
    CssFontVariantCaps capValue = null;
    CssFontVariantEastAsian asiValue = null;
    CssFontVariantLigatures ligValue = null;
    CssFontVariantNumeric numValue = null;
    CssFontVariantPosition posValue = null;

    /**
     * Creates a new CssFontVariant
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontVariant(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 18) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssExpression ligExp = null;
        CssExpression altExp = null;
        CssExpression capExp = null;
        CssExpression numExp = null;
        CssExpression asiExp = null;
        CssExpression posExp = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_FUNCTION:
                    // functions are only present in font-variant-alternates
                    // defer checking to the class
                    if (altExp == null) {
                        altExp = new CssExpression();
                    }
                    altExp.addValue(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id) || normal.equals(id)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        // normal also resets the values of individual
                        // properties, so...
                        altValue = new CssFontVariantAlternates();
                        altValue.value = val;
                        capValue = new CssFontVariantCaps();
                        capValue.value = val;
                        asiValue = new CssFontVariantEastAsian();
                        asiValue.value = val;
                        ligValue = new CssFontVariantLigatures();
                        ligValue.value = val;
                        numValue = new CssFontVariantNumeric();
                        numValue.value = val;
                        break;
                    } else if (none.equals(id)) {
                        value = val;
                        altValue = new CssFontVariantAlternates();
                        capValue = new CssFontVariantCaps();
                        asiValue = new CssFontVariantEastAsian();
                        ligValue = new CssFontVariantLigatures();
                        ligValue.value = val;
                        numValue = new CssFontVariantNumeric();
                        break;
                    } else {
                        // we test the possible values
                        // of the 5 possible properties
                        if (CssFontVariantCaps.getAllowedValue(id) != null) {
                            if (capExp == null) {
                                capExp = new CssExpression();
                            }
                            capExp.addValue(id);
                            break;
                        }
                        if (CssFontVariantNumeric.getAllowedValue(id) != null) {
                            if (numExp == null) {
                                numExp = new CssExpression();
                            }
                            numExp.addValue(id);
                            break;
                        }
                        if (CssFontVariantLigatures.getAllowedValue(id) != null) {
                            if (ligExp == null) {
                                ligExp = new CssExpression();
                            }
                            ligExp.addValue(id);
                            break;
                        }
                        if (CssFontVariantEastAsian.getAllowedValue(id) != null) {
                            if (asiExp == null) {
                                asiExp = new CssExpression();
                            }
                            asiExp.addValue(id);
                            break;
                        }
                        if (CssFontVariantPosition.getAllowedValue(id) != null) {
                            if (posExp == null) {
                                posExp = new CssExpression();
                            }
                            posExp.addValue(id);
                            break;
                        }
                        if (CssFontVariantAlternates.getAllowedIdent(id) != null) {
                            if (altExp == null) {
                                altExp = new CssExpression();
                            }
                            altExp.addValue(id);
                            break;
                        }
                    }
                    // unrecognized... let it fail.
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        // value not yet set, we must reassign values
        if (value == null) {
            CssFontVariantAlternates altValue = null;
            CssFontVariantCaps capValue = null;
            CssFontVariantEastAsian asiValue = null;
            CssFontVariantLigatures ligValue = null;
            CssFontVariantNumeric numValue = null;
            CssFontVariantPosition posValue = null;
            ArrayList<CssValue> vlist = new ArrayList<CssValue>(5);
            if (altExp != null) {
                altValue = new CssFontVariantAlternates(ac, altExp, check);
                vlist.add(altValue.value);
            }
            if (capExp != null) {
                capValue = new CssFontVariantCaps(ac, capExp, check);
                vlist.add(capValue.value);
            }
            if (asiExp != null) {
                asiValue = new CssFontVariantEastAsian(ac, asiExp, check);
                vlist.add(asiValue.value);
            }
            if (ligExp != null) {
                ligValue = new CssFontVariantLigatures(ac, ligExp, check);
                vlist.add(ligValue.value);
            }
            if (posExp != null) {
                posValue = new CssFontVariantPosition(ac, posExp, check);
                vlist.add(posValue.value);
            }
            if (numExp != null) {
                numValue = new CssFontVariantNumeric(ac, numExp, check);
                vlist.add(numValue.value);
            }
            value = (vlist.size() > 1) ? new CssValueList(vlist) : vlist.get(0);
        }
    }

    public CssFontVariant(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public CssFontVariant() {
        value = initial;
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        if (altValue != null) {
            altValue.addToStyle(ac, style);
        }
        if (capValue != null) {
            capValue.addToStyle(ac, style);
        }
        if (asiValue != null) {
            asiValue.addToStyle(ac, style);
        }
        if (ligValue != null) {
            ligValue.addToStyle(ac, style);
        }
        if (numValue != null) {
            numValue.addToStyle(ac, style);
        }
        if (posValue != null) {
            posValue.addToStyle(ac, style);
        }
    }
}

