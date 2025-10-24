//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/colors.html#propdef-background-position
 */
public class CssBackgroundPosition extends org.w3c.css.properties.css.CssBackgroundPosition {

    public static CssIdent[] allowed_values;
    public static CssIdent center, top, bottom, left, right;
    private static CssPercentage defaultPercent0, defaultPercent50;
    private static CssPercentage defaultPercent100;

    static {
        top = CssIdent.getIdent("top");
        bottom = CssIdent.getIdent("bottom");
        left = CssIdent.getIdent("left");
        right = CssIdent.getIdent("right");
        center = CssIdent.getIdent("center");
        allowed_values = new CssIdent[5];
        allowed_values[0] = top;
        allowed_values[1] = bottom;
        allowed_values[2] = left;
        allowed_values[3] = right;
        allowed_values[4] = center;

        defaultPercent0 = new CssPercentage(0);
        defaultPercent50 = new CssPercentage(50);
        defaultPercent100 = new CssPercentage(100);
    }

    public static boolean checkMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssBackgroundPosition
     */
    public CssBackgroundPosition() {
        super();
    }

    /**
     * Creates a new CssBackgroundPosition
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundPosition(ApplContext ac, CssExpression expression,
                                 boolean check) throws InvalidParamException {

        int nb_val = expression.getCount();

        if (check && nb_val > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val;
        CssBackgroundPositionValue b_val = null;
        char op;

        // we just accumulate values and check at validation
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (inherit.equals(val)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = inherit;
                expression.next();
                return;
            }
            if (b_val == null) {
                b_val = new CssBackgroundPositionValue();
            }
            // we will check later
            b_val.add(val);
            expression.next();

            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }

        }
        // if we reach the end in a value that can come in pair
        if (b_val != null) {
            check(b_val, ac);
            value = b_val;
        }
    }

    // check the value

    public void check(CssBackgroundPositionValue v, ApplContext ac)
            throws InvalidParamException {
        int nb_keyword = 0;
        int nb_percentage = 0;
        int nb_length = 0;
        int nb_values = v.value.size();

        if (nb_values > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        // basic check
        for (CssValue aValue : v.value) {
            switch (aValue.getType()) {
                case CssTypes.CSS_NUMBER:
                    aValue = ((CssNumber) aValue).getLength();
                case CssTypes.CSS_LENGTH:
                    nb_length++;
                    break;
                case CssTypes.CSS_PERCENTAGE:
                    nb_percentage++;
                    break;
                case CssTypes.CSS_IDENT:
                    nb_keyword++;
                    break;
                default:
                    throw new InvalidParamException("unrecognize", aValue,
                            ac);
            }
        }

        // this is unnecessary complex, blame it on the CSS3 spec.
        switch (nb_keyword) {
            case 0:
                // no keyword, so it's easy, it depends on the number
                // of values :)
                switch (nb_values) {
                    case 1:
                        // If only one value is specified, the second value
                        // is assumed to be 'center'.
                        v.horizontal = v.value.get(0);
                        if (v.horizontal.getType() == CssTypes.CSS_NUMBER) {
                            v.horizontal = defaultPercent0;
                        }
                        v.val_horizontal = v.horizontal;
                        v.val_vertical = defaultPercent50;
                        break;
                    case 2:
                        v.horizontal = v.value.get(0);
                        if (v.horizontal.getType() == CssTypes.CSS_NUMBER) {
                            v.horizontal = defaultPercent0;
                        }
                        v.val_horizontal = v.horizontal;
                        v.vertical = v.value.get(1);
                        if (v.vertical.getType() == CssTypes.CSS_NUMBER) {
                            v.vertical = defaultPercent0;
                        }
                        v.val_vertical = v.vertical;
                        break;
                    default:
                        // should never happen
                        throw new InvalidParamException("unrecognize",
                                ac);

                }
                break;
            // we got one keyword... let's have fun...
            case 1:
                switch (nb_values) {
                    case 1:
                        CssIdent ident = (CssIdent) v.value.get(0);
                        // ugly as we need to set values for equality tests
                        v.val_vertical = defaultPercent50;
                        v.val_horizontal = defaultPercent50;
                        ident = getMatchingIdent(ident);
                        if (ident != null) {
                            if (isVertical(ident)) {
                                v.vertical = ident;
                                v.val_vertical = identToPercent(ident);
                            } else {
                                // horizontal || center
                                v.horizontal = ident;
                                v.val_horizontal = identToPercent(ident);
                            }
                            break;
                        }
                        throw new InvalidParamException("unrecognize",
                                ident, getPropertyName(), ac);
                    case 2:
                        // one ident, two values... first MUST be horizontal
                        // and second vertical
                        CssValue val0 = v.value.get(0);
                        if (val0.getType() == CssTypes.CSS_IDENT) {
                            ident = getMatchingIdent((CssIdent) val0);
                            if (ident == null) {
                                throw new InvalidParamException("unrecognize",
                                        ident, getPropertyName(), ac);
                            }
                            if (isVertical(ident)) {
                                throw new InvalidParamException("incompatible",
                                        ident, v.value.get(1), ac);
                            }
                            v.horizontal = ident;
                            v.val_horizontal = identToPercent(ident);
                            // and the vertical value...
                            v.vertical = v.value.get(1);
                            if (v.vertical.getType() == CssTypes.CSS_NUMBER) {
                                v.vertical = defaultPercent0;
                            }
                            v.val_vertical = v.vertical;
                        } else {
                            CssValue value1 = v.value.get(1);
                            if (value1.getType() != CssTypes.CSS_IDENT) {
                                throw new InvalidParamException("unrecognize",
                                        value1, getPropertyName(), ac);
                            }
                            ident = getMatchingIdent((CssIdent) value1);
                            if (ident == null) {
                                throw new InvalidParamException("unrecognize",
                                        ident, getPropertyName(), ac);
                            }
                            if (isHorizontal(ident)) {
                                throw new InvalidParamException("incompatible",
                                        val0, value1, ac);
                            }
                            v.vertical = ident;
                            v.val_vertical = identToPercent(ident);
                            // and the first value
                            v.horizontal = val0;
                            if (v.horizontal.getType() == CssTypes.CSS_NUMBER) {
                                v.horizontal = defaultPercent0;
                            }
                            v.val_horizontal = v.horizontal;
                        }
                        break;
                    default:
                        // one ident, 3 or 4 values is not allowed
                        throw new InvalidParamException("unrecognize",
                                ac);
                }
                break;
            default:
                // we got two keywords for two values, yeah!
                CssIdent id1 = (CssIdent) v.value.get(0);
                CssIdent id2 = (CssIdent) v.value.get(1);


                if (((isVertical(id1) && isVertical(id2))) ||
                        (isHorizontal(id1) && isHorizontal(id2))) {
                    throw new InvalidParamException("incompatible",
                            id1, id2, ac);
                }
                if (isVertical(id1) || isHorizontal(id2)) {
                    v.horizontal = id2;
                    v.val_horizontal = identToPercent(id2);
                    v.vertical = id1;
                    v.val_vertical = identToPercent(id1);
                } else {
                    v.horizontal = id1;
                    v.val_horizontal = identToPercent(id1);
                    v.vertical = id2;
                    v.val_vertical = identToPercent(id2);
                }
        }
    }

    public CssBackgroundPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
