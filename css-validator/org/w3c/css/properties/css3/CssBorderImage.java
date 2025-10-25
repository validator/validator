// $Id$
//
// (c) COPYRIGHT 1995-2012  World Wide Web Consortium (MIT, ERCIM, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2012/CR-css3-background-20120417/#border-image
 */

public class CssBorderImage extends org.w3c.css.properties.css.CssBorderImage {


    /**
     * Create new CssBorderImage
     */
    public CssBorderImage() {
        value = initial;
    }

    /**
     * Create new CssBorderImage
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBorderImage(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {
        int state = 0;
        // <?border-image-source?> || <?border-image-slice?> [ / <?border-image-width?> | / <?border-image-width?>? / <?border-image-outset?> ]? || <?border-image-repeat?>

        // state 0, we check for <?border-image-source?> || <?border-image-slice?> || <?border-image-repeat?>
        // state 1, we check only <?border-image-width?> ( first / after <?border-image-slice?>)
        // state 2, we check only for  <?border-image-outset?>
        CssExpression newexp;
        CssValue val = null;
        CssValue tval;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_URL:
                case CssTypes.CSS_IMAGE:
                    if (source != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    // right after the / in step 2 we must have a slice and outset or width
                    if (state > 0) {
                        if ((slice == null) ||
                                ((state == 1 && width == null) || (state == 2 && outset == null))) {
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        }
                    }
                    // work on this expression as it consumes only one token
                    source = new CssBorderImageSource(ac, expression, false);
                    // we must reset the operator
                    state = 0;
                    op = expression.getOperator();
                    break;
                case CssTypes.CSS_SWITCH:
                    state++;
                    if (slice == null || state > 2) {
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                    expression.next();
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        // TODO force individual values as inherit
                        expression.next();
                        break;
                    }
                    switch (state) {
                        case 0:
                            // state 0, we can only have slice or repeat or image
                            // slice
                            tval = CssBorderImageSlice.getMatchingIdent(id);
                            if (tval != null) {
                                if (slice != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                newexp = getSliceExpression(ac, expression);
                                if (newexp != null) {
                                    slice = new CssBorderImageSlice(ac, newexp, check);
                                    break;
                                }
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            // repeat
                            newexp = getRepeatExpression(ac, expression);
                            if (newexp != null) {
                                if (repeat != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                repeat = new CssBorderImageRepeat(ac, newexp, check);
                                break;
                            }
                            // TODO check for border-image! (none)
                            if (CssBorderImageSource.isMatchingIdent(id)) {
                                if (source != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                source = new CssBorderImageSource(ac, expression, false);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 1:
                            // it can be only width or repeat.
                            // width
                            tval = CssBorderImageWidth.getMatchingIdent(id);
                            if (tval != null) {
                                if (width != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                newexp = getWidthExpression(ac, expression);
                                if (newexp != null) {
                                    width = new CssBorderImageWidth(ac, newexp, check);
                                    break;
                                }
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                        case 2:
                            // outset has no ident so let 1 and 2 check repeat
                            newexp = getRepeatExpression(ac, expression);
                            if (newexp != null) {
                                if (repeat != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                // right after the / in step 2 we must have a slice or outset
                                if ((state == 1 && width == null) || (state == 2 && outset == null)) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                repeat = new CssBorderImageRepeat(ac, newexp, check);
                                state = 0;
                                break;
                            }
                            // TODO check for border-image! (none)
                            if (CssBorderImageSource.isMatchingIdent(id)) {
                                if (source != null) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                // right after the / in step 2 we must have a slice or outset
                                if ((state == 1 && width == null) || (state == 2 && outset == null)) {
                                    throw new InvalidParamException("value", val.toString(),
                                            getPropertyName(), ac);
                                }
                                source = new CssBorderImageSource(ac, expression, false);
                                state = 0;
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                    }
                    break;
                case CssTypes.CSS_PERCENTAGE:
                    // can appear only in slice and width (so 0 and 1)
                    switch (state) {
                        case 0:
                            if (slice != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getSliceExpression(ac, expression);
                            if (newexp != null) {
                                slice = new CssBorderImageSlice(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 1:
                            if (width != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getWidthExpression(ac, expression);
                            if (newexp != null) {
                                width = new CssBorderImageWidth(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 2:
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                    }
                    break;
                case CssTypes.CSS_LENGTH:
                    // can appear only in width and outset (so 1 and 2)
                    switch (state) {
                        case 0:
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 1:
                            if (width != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getWidthExpression(ac, expression);
                            if (newexp != null) {
                                width = new CssBorderImageWidth(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 2:
                            if (outset != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getOutsetExpression(ac, expression);
                            if (newexp != null) {
                                outset = new CssBorderImageOutset(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                    }
                    break;
                case CssTypes.CSS_NUMBER:
                    switch (state) {
                        case 0:
                            if (slice != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getSliceExpression(ac, expression);
                            if (newexp != null) {
                                slice = new CssBorderImageSlice(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 1:
                            if (width != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getWidthExpression(ac, expression);
                            if (newexp != null) {
                                width = new CssBorderImageWidth(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        case 2:
                            if (outset != null) {
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                            newexp = getOutsetExpression(ac, expression);
                            if (newexp != null) {
                                outset = new CssBorderImageOutset(ac, newexp, check);
                                break;
                            }
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                    }
                    break;
                default:
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (val.getType() == CssTypes.CSS_SWITCH) {
            // we can't end by a /
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }
        shorthand = true;
    }

    public CssBorderImage(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private CssExpression getRepeatExpression(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        CssExpression exp = null;
        CssValue val, tval;
        char op;

        val = expression.getValue();
        op = expression.getOperator();
        if (val.getType() == CssTypes.CSS_IDENT) {
            tval = CssBorderImageRepeat.getMatchingIdent(val.getIdent());
            if (tval == null) {
                return null;
            }
            exp = new CssExpression();
            exp.addValue(val);
            expression.next();
            if (!expression.end()) {
                // now get the potential second value
                // first check the operator
                if (op != SPACE) {
                    return exp;
                }
                val = expression.getValue();
                op = expression.getOperator();
                if (val.getType() == CssTypes.CSS_IDENT) {
                    tval = CssBorderImageRepeat.getMatchingIdent(val.getIdent());
                    if (tval != null) {
                        exp.addValue(val);
                        expression.next();
                        if (op != SPACE) {
                            return exp;
                        }
                    }
                }
            }
        }
        return exp;
    }

    private CssExpression getSliceExpression(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        CssExpression exp = new CssExpression();
        CssValue val, tval;
        char op;

        while (exp.getCount() < 5 && !expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                    exp.addValue(val);
                    break;
                case CssTypes.CSS_IDENT:
                    tval = CssBorderImageSlice.getMatchingIdent(val.getIdent());
                    if (tval == null) {
                        return exp;
                    }
                    exp.addValue(val);
                    break;
                default:
                    return exp;

            }
            expression.next();
            if (op != SPACE) {
                return exp;
            }
        }
        return exp;
    }

    private CssExpression getWidthExpression(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        CssExpression exp = new CssExpression();
        CssValue val, tval;
        char op;

        while (exp.getCount() < 4 && !expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                    exp.addValue(val);
                    break;
                case CssTypes.CSS_IDENT:
                    tval = CssBorderImageWidth.getMatchingIdent(val.getIdent());
                    if (tval == null) {
                        return exp;
                    }
                    exp.addValue(val);
                    break;
                default:
                    return exp;

            }
            expression.next();
            if (op != SPACE) {
                return exp;
            }
        }
        return exp;
    }

    private CssExpression getOutsetExpression(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        CssExpression exp = new CssExpression();
        CssValue val, tval;
        char op;

        while (exp.getCount() < 4 && !expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_NUMBER:
                    exp.addValue(val);
                    break;
                default:
                    return exp;

            }
            expression.next();
            if (op != SPACE) {
                return exp;
            }
        }
        return exp;
    }

    public String toString() {
        if (value != null) {
            return value.toString();
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        if (source != null) {
            first = false;
            sb.append(source);
        }
        if (slice != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(slice);
            if (width != null) {
                sb.append(" / ").append(width);
                if (outset != null) {
                    sb.append(" / ").append(outset);
                }
            } else if (outset != null) {
                sb.append(" / / ").append(outset);
            }
        }
        if (repeat != null) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(repeat);
        }
        return sb.toString();
    }
}
