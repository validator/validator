// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#font-variant-numeric-prop
 */
public class CssFontVariantNumeric extends org.w3c.css.properties.css.CssFontVariantNumeric {

    public static final CssIdent normal, slashedZero, ordinal;
    public static final CssIdent[] numericFigValues;
    public static final CssIdent[] numericSpaValues;
    public static final CssIdent[] numericFraValues;

    static {
        String[] _numericFigValues = {"lining-nums", "oldstyle-nums"};
        String[] _numericSpaValues = {"proportional-nums", "tabular-nums"};
        String[] _numericFraValues = {"diagonal-fractions", "stacked-fractions"};

        normal = CssIdent.getIdent("normal");
        slashedZero = CssIdent.getIdent("slashed-zero");
        ordinal = CssIdent.getIdent("ordinal");
        numericFigValues = new CssIdent[_numericFigValues.length];
        int i = 0;
        for (String s : _numericFigValues) {
            numericFigValues[i++] = CssIdent.getIdent(s);
        }
        numericSpaValues = new CssIdent[_numericSpaValues.length];
        i = 0;
        for (String s : _numericSpaValues) {
            numericSpaValues[i++] = CssIdent.getIdent(s);
        }
        numericFraValues = new CssIdent[_numericFraValues.length];
        i = 0;
        for (String s : _numericFraValues) {
            numericFraValues[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getNumericFigValues(CssIdent ident) {
        for (CssIdent id : numericFigValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getNumericSpaValues(CssIdent ident) {
        for (CssIdent id : numericSpaValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getNumericFraValues(CssIdent ident) {
        for (CssIdent id : numericFraValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        CssIdent id;
        if (slashedZero.equals(ident)) {
            return slashedZero;
        }
        if (ordinal.equals(ident)) {
            return ordinal;
        }
        id = getNumericFigValues(ident);
        if (id == null) {
            id = getNumericFraValues(ident);
            if (id == null) {
                id = getNumericSpaValues(ident);
            }
        }
        return id;
    }

    /**
     * Create a new CssFontVariantNumeric
     */
    public CssFontVariantNumeric() {
        value = initial;
    }

    /**
     * Creates a new CssFontVariantNumeric
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontVariantNumeric(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 5) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        char op;

        CssValue fraValue = null;
        CssValue figValue = null;
        CssValue spaValue = null;
        CssValue zerValue = null;
        CssValue ordValue = null;
        boolean match;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident) || normal.equals(ident)) {
                    if (expression.getCount() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    value = val;
                } else {
                    // no inherit, nor normal, test the up-to-three values
                    match = false;
                    if (figValue == null) {
                        match = (getNumericFigValues(ident) != null);
                        if (match) {
                            figValue = val;
                        }
                    }
                    if (!match && fraValue == null) {
                        match = (getNumericFraValues(ident) != null);
                        if (match) {
                            fraValue = val;
                        }
                    }
                    if (!match && spaValue == null) {
                        match = (getNumericSpaValues(ident) != null);
                        if (match) {
                            spaValue = val;
                        }
                    }
                    if (!match && zerValue == null) {
                        match = slashedZero.equals(ident);
                        if (match) {
                            zerValue = val;
                        }
                    }
                    if (!match && ordValue == null) {
                        match = ordinal.equals(ident);
                        if (match) {
                            ordValue = val;
                        }
                    }
                    if (!match) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                }
            } else {
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
        // now set the right value
        if (expression.getCount() == 1) {
            // the last test is here in case value is already set
            // (normal or inherit)
            if (figValue != null) {
                value = figValue;
            } else if (fraValue != null) {
                value = fraValue;
            } else if (spaValue != null) {
                value = spaValue;
            } else if (zerValue != null) {
                value = zerValue;
            } else if (ordValue != null) {
                value = ordValue;
            }
        } else {
            // do this to keep the same order for comparisons
            ArrayList<CssValue> v = new ArrayList<CssValue>();
            if (figValue != null) {
                v.add(figValue);
            }
            if (fraValue != null) {
                v.add(fraValue);
            }
            if (spaValue != null) {
                v.add(spaValue);
            }
            if (zerValue != null) {
                v.add(zerValue);
            }
            if (ordValue != null) {
                v.add(ordValue);
            }
            value = new CssValueList(v);
        }

    }

    public CssFontVariantNumeric(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

