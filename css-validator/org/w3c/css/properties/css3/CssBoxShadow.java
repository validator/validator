// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2012 by Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2012  World Wide Web Consortium (MIT, ERCIM, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-box-shadow
 */

public class CssBoxShadow extends org.w3c.css.properties.css.CssBoxShadow {

    public static CssIdent inset;

    static {
        inset = CssIdent.getIdent("inset");
    }

    /**
     * Create new CssBoxShadow
     */
    public CssBoxShadow() {
        value = none;
    }

    /**
     * Create new CssBoxShadow
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBoxShadow(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        CssExpression single_layer = null;
        ArrayList<CssValue> values;
        CssBoxShadowValue boxShadowValue;

        setByUser();
        CssValue val = expression.getValue();
        char op;

        if (expression.getCount() == 1) {
            // it can be only 'none' or 'inherit'
            if (val.getType() == CssTypes.CSS_IDENT) {
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id) || none.equals(val)) {
                    value = val;
                    expression.next();
                    return;
                }
            }
            // if it is another ident, or not an ident, fail.
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        // ok, so we have one or multiple layers here...
        values = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (single_layer == null) {
                single_layer = new CssExpression();
            }
            single_layer.addValue(val);
            single_layer.setOperator(op);
            expression.next();

            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (op == COMMA) {
                    single_layer.setOperator(SPACE);
                    boxShadowValue = check(ac, single_layer, check);
                    values.add(boxShadowValue);
                    single_layer = null;
                } else if ((op != SPACE)) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (single_layer != null) {
            boxShadowValue = check(ac, single_layer, check);
            values.add(boxShadowValue);
        }
        if (values.size() == 1) {
            value = values.get(0);
        } else {
            value = new CssLayerList(values);
        }

    }

    public CssBoxShadowValue check(ApplContext ac, CssExpression expression,
                                   boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 6) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        CssBoxShadowValue value = new CssBoxShadowValue();
        boolean length_ok = true;
        int got_length = 0;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                    if (!length_ok) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    got_length++;
                    switch (got_length) {
                        case 1:
                            value.horizontal_offset = val;
                            break;
                        case 2:
                            value.vertical_offset = val;
                            break;
                        case 3:
                            val.getCheckableValue().checkPositiveness(ac, getPropertyName());
                            value.blur_radius = val;
                            break;
                        case 4:
                            value.spread_distance = val;
                            break;
                        default:
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    // if we got 2 to 4 length tokens we must not have others
                    if (got_length != 0) {
                        length_ok = false;
                    }
                    CssIdent id = val.getIdent();
                    // checked before, not allowed here
                    if (CssIdent.isCssWide(id)) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    if (inset.equals(id) && value.shadow_mod == null) {
                        value.shadow_mod = val;
                        // inset has been relaxed
                        break;
                    }
                    // if not a known ident, it must be a color
                    // and let's use the CSS3 color.
                    // so let it flow as if it was a color
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    // this one is a pain... need to remove function for colors.
                case CssTypes.CSS_FUNCTION:
                    if (value.color != null) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    if (got_length != 0) {
                        length_ok = false;
                    }
                    CssExpression exp = new CssExpression();
                    exp.addValue(val);
                    CssColor color = new CssColor(ac, exp, check);
                    value.color = color.getValue();
                    break;
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", val,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        // we need 2 to 4 length. > 4 is taken care of in the first switch
        if (got_length < 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        return value;
    }

    public CssBoxShadow(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return none == value;
    }

// placeholder for the different values

    public class CssBoxShadowValue extends CssValueList {

        CssValue horizontal_offset;
        CssValue vertical_offset;
        CssValue blur_radius;
        CssValue spread_distance;
        CssValue color;
        CssValue shadow_mod;

        public boolean equals(CssBoxShadowValue v) {
            if (!v.horizontal_offset.equals(horizontal_offset)) {
                return false;
            }
            if (!v.vertical_offset.equals(vertical_offset)) {
                return false;
            }
            if (blur_radius != null && !blur_radius.equals(v.blur_radius)) {
                return false;
            }
            if (blur_radius == null && v.blur_radius != null) {
                return false;
            }
            if (spread_distance != null && !spread_distance.equals(v.spread_distance)) {
                return false;
            }
            if (spread_distance == null && v.spread_distance != null) {
                return false;
            }
            if (color != null && !color.equals(v.color)) {
                return false;
            }
            if (color == null && v.color != null) {
                return false;
            }
            if (shadow_mod != null && !shadow_mod.equals(v.shadow_mod)) {
                return false;
            }
            if (shadow_mod == null && v.color != shadow_mod) {
                return false;
            }
            // at last!
            return true;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(horizontal_offset).append(' ').append(vertical_offset);
            if (blur_radius != null) {
                sb.append(' ').append(blur_radius);
            }
            if (spread_distance != null) {
                sb.append(' ').append(spread_distance);
            }
            if (color != null) {
                sb.append(' ').append(color);
            }
            if (shadow_mod != null) {
                sb.append(' ').append(shadow_mod);
            }
            return sb.toString();
        }
    }

}
