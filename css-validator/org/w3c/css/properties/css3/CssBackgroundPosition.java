// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Rewritten by Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, Keio and ERCIM, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-position
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

    public static boolean isMatchingIdent(CssIdent ident) {
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
        value = new CssBackgroundPositionValue();
    }

    /**
     * Creates a new CssBackgroundPosition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssBackgroundPosition(ApplContext ac, CssExpression expression,
                                 boolean check) throws InvalidParamException {
        setByUser();
        CssValue val;
        ArrayList<CssValue> values;
        CssBackgroundPositionValue b_val = null;
        char op;

        values = new ArrayList<CssValue>();
        // we just accumulate values and check at validation
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                if (CssIdent.isCssWide(val.getIdent())) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    value = val;
                    expression.next();
                    return;
                }
            }
            if (b_val == null) {
                b_val = new CssBackgroundPositionValue();
            }
            // we will check later
            b_val.add(val);
            expression.next();

            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (op == COMMA) {
                    check(b_val, ac, getPropertyName());
                    values.add(b_val);
                    b_val = null;
                } else if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (b_val != null) {
            check(b_val, ac, getPropertyName());
            values.add(b_val);
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssBackgroundPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        if (!(value instanceof CssBackgroundPositionValue)) {
            return false;
        }
        CssBackgroundPositionValue v = (CssBackgroundPositionValue) value;
        return ((v.val_vertical == defaultPercent0) &&
                (v.val_horizontal == defaultPercent0) &&
                (v.vertical_offset == null) &&
                (v.horizontal_offset == null));
    }

    public void check(CssBackgroundPositionValue v, ApplContext ac, String caller)
            throws InvalidParamException {
        int nb_keyword = 0;
        int nb_percentage = 0;
        int nb_length = 0;
        int nb_values = v.value.size();

        if (nb_values > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        // basic check
        for (CssValue aValue : v.value) {
            switch (aValue.getType()) {
                case CssTypes.CSS_NUMBER:
                    aValue.getLength();
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
        if ((nb_keyword > 2) || (nb_length > 2) || (nb_percentage > 2)) {
            throw new InvalidParamException("unrecognize", ac);
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
                        // If three or four values are given, then each
                        // <percentage> or<length> represents an offset and
                        // must be preceded by a keyword
                        throw new InvalidParamException("unrecognize",
                                ac);

                }
                break;
            // we got one keyword... let's have fun...
            case 1:
                switch (nb_values) {
                    case 1:
                        CssIdent ident = v.value.get(0).getIdent();
                        // ugly as we need to set values for equality tests
                        v.val_vertical = defaultPercent50;
                        v.val_horizontal = defaultPercent50;
                        ident = getMatchingIdent(ident);
                        if (ident != null) {
                            if (isVertical(ident)) {
                                v.vertical = v.value.get(0);
                                v.val_vertical = identToPercent(ident);
                            } else {
                                // horizontal || center
                                v.horizontal = v.value.get(0);
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
                            ident = getMatchingIdent(val0.getIdent());
                            if (ident == null) {
                                throw new InvalidParamException("unrecognize",
                                        ident, getPropertyName(), ac);
                            }
                            if (isVertical(ident)) {
                                throw new InvalidParamException("incompatible",
                                        ident, v.value.get(1), ac);
                            }
                            v.horizontal = val0;
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
                            ident = getMatchingIdent(value1.getIdent());
                            if (ident == null) {
                                throw new InvalidParamException("unrecognize",
                                        ident, getPropertyName(), ac);
                            }
                            if (isHorizontal(ident)) {
                                throw new InvalidParamException("incompatible",
                                        val0, value1, ac);
                            }
                            v.vertical = value1;
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
                // ok so we have two keywords, with possible offsets
                // we must check that every possible offset is right
                // after a keyword and also that the two keywords are
                // not incompatible
                boolean got_ident = false;
                CssIdent id1 = null;
                CssIdent id2 = null;
                CssValue off1 = null;
                CssValue off2 = null;
                for (CssValue aValue : v.value) {
                    switch (aValue.getType()) {
                        case CssTypes.CSS_IDENT:
                            CssIdent id = aValue.getIdent();
                            if (getMatchingIdent(id) == null) {
                                throw new InvalidParamException("unrecognize",
                                        aValue, getPropertyName(), ac);
                            }
                            got_ident = true;
                            if (id1 == null) {
                                id1 = id;
                            } else {
                                id2 = id;
                                // we got both, let's check.
                                if (((isVertical(id1) && isVertical(id2))) ||
                                        (isHorizontal(id1) && isHorizontal(id2))) {
                                    throw new InvalidParamException("incompatible",
                                            id1, id2, ac);
                                }
                            }
                            break;
                        case CssTypes.CSS_NUMBER:
                            aValue.getCheckableValue().checkEqualsZero(ac, caller);
                        case CssTypes.CSS_PERCENTAGE:
                        case CssTypes.CSS_LENGTH:
                            if (!got_ident) {
                                throw new InvalidParamException("unrecognize",
                                        aValue, getPropertyName(), ac);
                            }
                            if (id2 == null) {
                                off1 = aValue;
                            } else {
                                off2 = aValue;
                            }
                            got_ident = false;
                            break;
                        default:
                            // should never happen
                    }
                }

                if (isVertical(id1) || isHorizontal(id2)) {
                    // if an offset is present and value is 'center'
                    if (((off1 != null) && !isVertical(id1)) ||
                            ((off2 != null) && !isHorizontal(id2))) {
                        throw new InvalidParamException("incompatible",
                                id1, id2, ac);
                    }
                    v.horizontal = id2;
                    v.val_horizontal = identToPercent(id2);
                    v.horizontal_offset = off2;
                    v.vertical = id1;
                    v.val_vertical = identToPercent(id1);
                    v.vertical_offset = off1;
                } else {
                    if (((off2 != null) && !isVertical(id2)) ||
                            ((off1 != null) && !isHorizontal(id1))) {
                        throw new InvalidParamException("incompatible",
                                id1, id2, ac);
                    }
                    v.horizontal = id1;
                    v.val_horizontal = identToPercent(id1);
                    v.horizontal_offset = off1;
                    v.vertical = id2;
                    v.val_vertical = identToPercent(id2);
                    v.vertical_offset = off2;
                }
        }
    }

    public static CssPercentage identToPercent(CssIdent ident) {
        if (center.equals(ident)) {
            return defaultPercent50;
        } else if (top.equals(ident) || left.equals(ident)) {
            return defaultPercent0;
        } else if (bottom.equals(ident) || right.equals(ident)) {
            return defaultPercent100;
        }
        return defaultPercent0; // FIXME throw an exception ?
    }

    public static boolean isHorizontal(CssIdent ident) {
        return (left.equals(ident) || right.equals(ident));
    }

    public static boolean isVertical(CssIdent ident) {
        return (top.equals(ident) || bottom.equals(ident));
    }

    public static CssValue checkBackgroundPosition(ApplContext ac, CssExpression expression,
                                                   CssProperty caller)
            throws InvalidParamException {
        return checkSyntax(ac, expression, caller.getPropertyName());
    }


    public static CssValue checkSyntax(ApplContext ac, CssExpression expression, String caller)
            throws InvalidParamException {
        ArrayList<CssValue> v = new ArrayList<CssValue>();
        int nb_values = expression.getCount();
        CssValue val, ret;
        int nb_length, nb_percentage, nb_keyword;
        char op;

        nb_length = nb_percentage = nb_keyword = 0;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getLength();
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
                    throw new InvalidParamException("unrecognize", val,
                            ac);
            }
            v.add(val);
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        ret = new CssValueList(v);
        // basic checks
        if ((nb_keyword > 2) || (nb_length > 2) || (nb_percentage > 2)) {
            throw new InvalidParamException("value", ret,
                    caller, ac);
        }
        // test based on the number of keywords
        switch (nb_keyword) {
            case 0:
                if (nb_keyword > 2) {
                    throw new InvalidParamException("value", ret,
                            caller, ac);
                }
                break;
            case 1:
                switch (nb_values) {
                    case 1:
                        // one value, one keyword... easy :)
                        if (getMatchingIdent(v.get(0).getIdent()) == null) {
                            throw new InvalidParamException("value", v.get(0),
                                    caller, ac);
                        }
                        break;
                    case 2:
                        // one ident, two values... first MUST be horizontal
                        // and second vertical
                        CssValue val0 = v.get(0);
                        CssIdent ident;
                        if (val0.getType() == CssTypes.CSS_IDENT) {
                            ident = getMatchingIdent(val0.getIdent());
                            if (ident == null) {
                                throw new InvalidParamException("value", val0,
                                        caller, ac);
                            }
                            if (isVertical(ident)) {
                                throw new InvalidParamException("incompatible",
                                        ident, v.get(1), ac);
                            }
                        } else {
                            CssValue value1 = v.get(1);
                            if (value1.getType() != CssTypes.CSS_IDENT) {
                                throw new InvalidParamException("value", value1,
                                        caller, ac);
                            }
                            ident = getMatchingIdent(value1.getIdent());
                            if (ident == null) {
                                throw new InvalidParamException("value", value1,
                                        caller, ac);
                            }
                            if (isHorizontal(ident)) {
                                throw new InvalidParamException("incompatible",
                                        val0, value1, ac);
                            }
                        }
                        break;
                    default:
                        // one ident, 3 or 4 values is not allowed
                        throw new InvalidParamException("value", ret,
                                caller, ac);
                }
                break;
            default:
                // ok so we have two keywords, with possible offsets
                // we must check that every possible offset is right
                // after a keyword and also that the two keywords are
                // not incompatible
                boolean got_ident = false;
                CssIdent id1 = null;
                CssIdent id2 = null;
                CssValue off1 = null;
                CssValue off2 = null;
                for (CssValue aValue : v) {
                    switch (aValue.getType()) {
                        case CssTypes.CSS_IDENT:
                            if (getMatchingIdent(aValue.getIdent()) == null) {
                                throw new InvalidParamException("value", aValue,
                                        caller, ac);
                            }
                            got_ident = true;
                            if (id1 == null) {
                                id1 = aValue.getIdent();
                            } else {
                                id2 = aValue.getIdent();
                                // we got both, let's check.
                                if (((isVertical(id1) && isVertical(id2))) ||
                                        (isHorizontal(id1) && isHorizontal(id2))) {
                                    throw new InvalidParamException("incompatible",
                                            id1, id2, ac);
                                }
                            }
                            break;
                        case CssTypes.CSS_NUMBER:
                        case CssTypes.CSS_PERCENTAGE:
                        case CssTypes.CSS_LENGTH:
                            if (!got_ident) {
                                throw new InvalidParamException("unrecognize",
                                        aValue, caller, ac);
                            }
                            if (id2 == null) {
                                off1 = aValue;
                            } else {
                                off2 = aValue;
                            }
                            got_ident = false;
                            break;
                        default:
                            // should never happen
                    }
                }

                if (isVertical(id1) || isHorizontal(id2)) {
                    // if an offset is present and value is 'center'
                    if (((off1 != null) && !isVertical(id1)) ||
                            ((off2 != null) && !isHorizontal(id2))) {
                        throw new InvalidParamException("incompatible",
                                id1, id2, ac);
                    }
                } else {
                    if (((off2 != null) && !isVertical(id2)) ||
                            ((off1 != null) && !isHorizontal(id1))) {
                        throw new InvalidParamException("incompatible",
                                id1, id2, ac);
                    }
                }
        }
        return ret;
    }
}
