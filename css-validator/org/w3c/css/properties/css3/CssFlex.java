//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.math.BigDecimal;

/**
 * @spec https://www.w3.org/TR/2018/CR-css-flexbox-1-20181119/#propdef-flex
 */
public class CssFlex extends org.w3c.css.properties.css.CssFlex {

    public CssIdent auto = CssIdent.getIdent("auto");

    private CssFlexGrow flexGrow;
    private CssFlexShrink flexShrink;
    private CssFlexBasis flexBasis;

    /**
     * Create a new CssFlexFlow
     */
    public CssFlex() {
        value = initial;
        flexGrow = new CssFlexGrow();
        flexShrink = new CssFlexShrink();
        flexBasis = new CssFlexBasis();
    }

    /**
     * Creates a new CssFlexFlow
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFlex(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue growVal = null;
        CssValue shrinkVal = null;
        CssValue basisVal = null;
        CssValue val;
        char op;
        boolean gotNumber = false;
        boolean gotCssWide = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        gotCssWide = true;
                        value = val;
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (none.equals(ident)) {
                        value = val;
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (basisVal == null) {
                        basisVal = CssFlexBasis.getAllowedIdent(ident);
                        if (basisVal == null) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        gotNumber = false;
                        break;
                    }
                    // unrecognized token...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_NUMBER:
                    if (growVal == null) {
                        val.getCheckableValue().checkPositiveness(ac, this);
                        growVal = val;
                        gotNumber = true;
                        break;
                    }
                    // we can get shrink only after grow
                    if (gotNumber && shrinkVal == null) {
                        val.getCheckableValue().checkPositiveness(ac, this);
                        shrinkVal = val;
                        break;
                    }
                    //
                default:
                    if (basisVal == null) {
                        // check flexBasis
                        CssExpression e = new CssExpression();
                        e.addValue(val);
                        try {
                            CssFlexBasis b = new CssFlexBasis(ac, e, check);
                            basisVal = val;
                        } catch (InvalidParamException ex) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        gotNumber = false;
                        break;
                    }
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
        // for addToStyle, redefinitions and equality check
        flexBasis = new CssFlexBasis();
        flexGrow = new CssFlexGrow();
        flexShrink = new CssFlexShrink();
        if (gotCssWide) {
            flexBasis.value = value;
            flexGrow.value = value;
            flexShrink.value = value;
        } else if (value == none) {
            flexBasis.value = CssWidth.auto;
            CssNumber z = new CssNumber();
            z.setValue(BigDecimal.ZERO);
            flexGrow.value = z;
            flexShrink.value = z;
        } else if (basisVal == auto && growVal == null && shrinkVal == null) {
            flexBasis.value = CssWidth.auto;
            CssNumber one = new CssNumber();
            one.setValue(BigDecimal.ONE);
            flexGrow.value = one;
            flexShrink.value = one;
            value = auto;
        } else if (basisVal == null && shrinkVal == null) {
            CssNumber one = new CssNumber();
            one.setValue(BigDecimal.ONE);
            flexShrink.value = one;
            CssNumber z = new CssNumber();
            z.setValue(BigDecimal.ZERO);
            flexBasis.value = z;
            value = growVal;
        } else {
            CssValueList v = new CssValueList();
            if (growVal != null) {
                v.add(growVal);
                flexGrow.value = growVal;
                if (shrinkVal != null) {
                    v.add(shrinkVal);
                    flexShrink.value = shrinkVal;
                }
            }
            if (basisVal != null) {
                v.add(basisVal);
                flexBasis.value = basisVal;
            }
            value = v;
        }
    }


    public CssFlex(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        flexBasis.addToStyle(ac, style);
        flexGrow.addToStyle(ac, style);
        flexShrink.addToStyle(ac, style);
    }
}

