// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html
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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background
 * @see org.w3c.css.properties.css.CssBackgroundColor
 * @see org.w3c.css.properties.css.CssBackgroundImage
 * @see org.w3c.css.properties.css.CssBackgroundRepeat
 * @see org.w3c.css.properties.css.CssBackgroundAttachment
 * @see org.w3c.css.properties.css.CssBackgroundPosition
 * @see org.w3c.css.properties.css.CssBackgroundSize
 */
public class CssBackground extends org.w3c.css.properties.css.CssBackground {

    /**
     * Create a new CssBackground
     */
    public CssBackground() {
        value = initial;
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssBackground(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssBackground(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {

        setByUser();
        CssValue val;
        ArrayList<CssValue> values;
        CssExpression single_layer = null;
        CssBackgroundValue b_val = null;
        char op;

        values = new ArrayList<CssValue>();
        // we just accumulate values and check at validation
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
            if (single_layer == null) {
                single_layer = new CssExpression();
            }
            // we will check later
            single_layer.addValue(val);
            single_layer.setOperator(op);
            expression.next();

            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (op == COMMA) {
                    single_layer.setOperator(SPACE);
                    b_val = check(ac, single_layer, check, false);
                    values.add(b_val);
                    single_layer = null;
                } else if ((op != SPACE)) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (single_layer != null) {
            b_val = check(ac, single_layer, check, true);
            values.add(b_val);
        }
        if (values.size() == 1) {
            value = values.get(0);
        } else {
            value = new CssLayerList(values);
        }
        transform_into_individual_values();
    }

    private CssValue getCssBackgroundRepeatValue(ApplContext ac,
                                                 CssExpression expression,
                                                 boolean check)
            throws InvalidParamException {
        char op = expression.getOperator();
        CssExpression exp = new CssExpression();

        exp.addValue(expression.getValue());
        repeat = new CssBackgroundRepeat(ac, exp, check);
        // now check if we can add a second value ;)
        if ((op == SPACE) && !expression.end()) {
            expression.next();
            if (!expression.end()) {
                CssValue val = expression.getValue();
                if ((val.getType() == CssTypes.CSS_IDENT) &&
                        CssBackgroundRepeat.isMatchingIdent((val.getIdent()))) {
                    exp.addValue(val);
                    exp.starts();
                    try {
                        repeat = new CssBackgroundRepeat(ac, exp, check);
                    } catch (InvalidParamException ipe) {
                        expression.precedent();
                    }
                } else {
                    expression.precedent();
                }
            }
        }
        return repeat.value;
    }

    private CssValue getCssBackgroundSizeValue(ApplContext ac,
                                               CssExpression expression,
                                               boolean check)
            throws InvalidParamException {
        char op = expression.getOperator();
        CssExpression exp = new CssExpression();

        exp.addValue(expression.getValue());
        CssBackgroundSize bg_size;
        bg_size = new CssBackgroundSize(ac, exp, check);
        // now check if we can add a second value ;)
        // TODO really dirty.. must check the use of 'check'
        // here, and possibly adjust the parsing model in
        // other classes :(
        if ((op == SPACE) && !expression.end()) {
            expression.next();
            if (!expression.end()) {
                exp.addValue(expression.getValue());
                exp.starts();
                try {
                    bg_size = new CssBackgroundSize(ac, exp, check);
                } catch (InvalidParamException ipe) {
                    // roll back
                    expression.precedent();
                }
            }
        }
        return bg_size.value;
    }


    private CssValue getCssBackgroundPositionValue(ApplContext ac,
                                                   CssExpression expression,
                                                   boolean check)
            throws InvalidParamException {
        CssExpression exp = new CssExpression();
        char op = expression.getOperator();
        exp.addValue(expression.getValue());
        int last_val = -1;

        CssBackgroundPosition bg_pos;
        bg_pos = new CssBackgroundPosition(ac, exp, check);
        // good we have a valid value, try something better..
        expression.mark();
        // we MUST try all the cases, as we can have something
        // invalid using 3 values (incompatible definitions)
        // but valid using 4 values...
        // example top 12% is invalid, top 12% center is valid...
        for (int i = 0; i < 3; i++) {
            if ((op == SPACE) && !expression.end()) {
                expression.next();
                if (expression.end()) {
                    break;
                }
                exp.addValue(expression.getValue());
                exp.starts();
                try {
                    bg_pos = new CssBackgroundPosition(ac, exp, check);
                    last_val = i;
                } catch (InvalidParamException ipe) {
                }

            }
        }
        expression.reset();
        while (last_val >= 0) {
            expression.next();
            last_val--;
        }
        return bg_pos.value;
    }


    public CssBackgroundValue check(ApplContext ac, CssExpression expression,
                                    boolean check, boolean is_final)
            throws InvalidParamException {
        // <bg-layer> = <bg-image> || <bg-position> || / <bg-size> || <repeat-style> ||
        //              <attachment> || <bg-origin>
        // bg_image is CSS_URL | IDENT
        // bg-position is IDENT | NUMBER | LENGTH | PERCENTAGE
        // bg-size is IDENT | NUMBER | LENGTH | PERCENTAGE
        // repeat-style is IDENT
        // attachment is IDENT
        // bg-origin is IDENT
        // + color as CSS_COLOR or IDENT on final-layer

        CssValue val, res;
        char op;
        CssExpression exp;
        CssBackgroundValue v = new CssBackgroundValue();
        boolean next_is_size, got_size, prev_is_position;

        next_is_size = false;
        got_size = false;
        prev_is_position = false;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    prev_is_position = false;
                    // we already got one, fail...
                    if (v.color != null || next_is_size || !is_final) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    exp = new CssExpression();
                    exp.addValue(val);

                    CssBackgroundColor bg_color;
                    bg_color = new CssBackgroundColor(ac, exp, check);
                    v.color = bg_color.value;
                    break;

                case CssTypes.CSS_URL:
                case CssTypes.CSS_IMAGE:
                    prev_is_position = false;
                    // we already got one, fail...
                    if (v.bg_image != null || next_is_size) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    exp = new CssExpression();
                    exp.addValue(val);

                    CssBackgroundImage bg_image;
                    bg_image = new CssBackgroundImage(ac, exp, check);
                    res = bg_image.value;
                    // we only have one vale so it should always be the case
                    if (res != null) {
                        v.bg_image = res;
                    } else {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    break;
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    prev_is_position = false;
                    // ok, so now we have a background position or size.
                    // and...
                    // in <bg_layer>: where '<bg-position>' must occur before
                    //  '/ <bg-size>' if both are present.
                    if (next_is_size) {
                        // size, we have up to two values
                        if (v.bg_size != null) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        res = getCssBackgroundSizeValue(ac, expression, check);
                        op = expression.getOperator();
                        // we only have one vale so it should always be the case
                        if (res instanceof CssValue) {
                            v.bg_size = (CssValue) res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        got_size = true;
                        next_is_size = false;
                    } else {
                        // position with it's up to 4 values...
                        if (got_size) {
                            throw new InvalidParamException("bg_order", val,
                                    getPropertyName(), ac);
                        }
                        if (v.bg_position != null) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        res = getCssBackgroundPositionValue(ac, expression, check);
                        op = expression.getOperator();
                        prev_is_position = true;
                        // we only have one value so it should always be the case
                        if (res != null) {
                            v.bg_position = (CssValue) res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }

                    }
                    break;
                case CssTypes.CSS_IDENT:
                    prev_is_position = false;
                    // inherit is already taken care of...
                    CssIdent ident_val = val.getIdent();
                    if (CssBackgroundAttachment.isMatchingIdent(ident_val)) {
                        if (v.attachment != null || next_is_size) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        exp = new CssExpression();
                        exp.addValue(val);

                        CssBackgroundAttachment attachment;
                        attachment = new CssBackgroundAttachment(ac, exp, check);
                        res = attachment.value;
                        // we only have one vale so it should always be the case
                        if (res != null) {
                            v.attachment = res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (CssBackgroundImage.isMatchingIdent(ident_val)) {
                        if (v.bg_image != null || next_is_size) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        // a bit of an overkill, as we know it can be only
                        // 'none'.. but it is more flexible if ever it changes
                        exp = new CssExpression();
                        exp.addValue(val);

                        bg_image = new CssBackgroundImage(ac, exp, check);
                        res = bg_image.value;
                        // we only have one vale so it should always be the case
                        if (res != null) {
                            v.bg_image = res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    // Kludge ahead, we are testing for <box> here, and it
                    // matches both origin and clip.
                    if (CssBackgroundOrigin.isMatchingIdent(ident_val)) {
                        if (next_is_size) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        if (v.origin != null) {
                            // ok, we have an origin, so we are looking for a clip
                            if (v.clip != null) {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            exp = new CssExpression();
                            exp.addValue(val);

                            CssBackgroundClip clip;
                            clip = new CssBackgroundClip(ac, exp, check);
                            res = clip.value;
                            // we only have one vale so it should always be the case
                            if (res != null) {
                                v.clip = res;
                            } else {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            break;
                        }
                        exp = new CssExpression();
                        exp.addValue(val);

                        CssBackgroundOrigin origin;
                        origin = new CssBackgroundOrigin(ac, exp, check);
                        res = origin.value;
                        // we only have one vale so it should always be the case
                        if (res != null) {
                            v.origin = res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (CssBackgroundRepeat.isMatchingIdent(ident_val)) {
                        if (v.repeat_style != null || next_is_size) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        res = getCssBackgroundRepeatValue(ac, expression, check);
                        op = expression.getOperator();

                        // we only have one vale so it should always be the case
                        if (res != null) {
                            v.repeat_style = (CssValue) res;
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (next_is_size) {
                        if (CssBackgroundSize.isMatchingIdent(ident_val)) {
                            // size, we have up to two values
                            if (v.bg_size != null) {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            res = getCssBackgroundSizeValue(ac, expression, check);
                            op = expression.getOperator();
                            // we only have one vale so it should always be the case
                            if (res != null) {
                                v.bg_size = res;
                            } else {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            got_size = true;
                            next_is_size = false;
                            break;
                        }
                    } else {
                        if (CssBackgroundPosition.isMatchingIdent(ident_val)) {
                            // position with it's up to 4 values...
                            if (got_size) {
                                throw new InvalidParamException("bg_order",
                                        val, getPropertyName(), ac);
                            }
                            if (v.bg_position != null) {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            res = getCssBackgroundPositionValue(ac, expression, check);
                            op = expression.getOperator();
                            prev_is_position = true;
                            // we only have one vale so it should always be the case
                            if (res != null) {
                                v.bg_position = res;
                            } else {
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                            }
                            break;
                        }
                    }
                    // last one remaining... value!
                    // or else, it will fail :)
                    if (is_final) {
                        if (v.color != null || next_is_size) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        exp = new CssExpression();
                        exp.addValue(val);
                        bg_color = new CssBackgroundColor(ac, exp, check);
                        v.color = (CssValue) bg_color.get();
                        break;
                    }
                    // unrecognized or unwanted ident
                    // let it fail now
                case CssTypes.CSS_FUNCTION:
                    prev_is_position = false;
                    // function can only be a value here
                    // we already got one, fail...
                    if (v.color != null || next_is_size || !is_final) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    exp = new CssExpression();
                    exp.addValue(val);

                    bg_color = new CssBackgroundColor(ac, exp, check);
                    v.color = bg_color.value;
                    break;
                // the infamous switch...
                // note that we should check that we got something first.
                case CssTypes.CSS_SWITCH:
                    if (!prev_is_position) {
                        throw new InvalidParamException("operator", val,
                                getPropertyName(), ac);
                    }
                    next_is_size = true;
                    break;
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }

            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        align_bg_values(v);
        return v;
    }

    private void align_bg_values(CssBackgroundValue v) {
        // <bg-layer> = <bg-image> || <bg-position> || / <bg-size> || <repeat-style> ||
        //              <attachment> || <bg-origin>
        Object value;
        if (v.bg_image == null) {
            v.bg_image_value = (new CssBackgroundImage()).value;
        } else {
            v.bg_image_value = v.bg_image;
        }

        if (v.bg_position == null) {
            v.bg_position_value = (new CssBackgroundPosition()).value;
        } else {
            v.bg_position_value = v.bg_position;
        }

        if (v.bg_size == null) {
            v.bg_size_value = (new CssBackgroundSize()).value;
        } else {
            v.bg_size_value = v.bg_size;
        }

        if (v.repeat_style == null) {
            v.repeat_style_value = (new CssBackgroundRepeat()).value;
        } else {
            v.repeat_style_value = v.repeat_style;
        }

        if (v.attachment == null) {
            v.attachment_value = (new CssBackgroundAttachment()).value;
        } else {
            v.attachment_value = v.attachment;
        }

        if (v.origin == null) {
            CssValue css_val = (new CssBackgroundOrigin()).value;
            v.origin_value = (new CssBackgroundOrigin()).value;
            // If 'background-origin' is present and its value matches a
            // possible value for 'background-clip' then it also sets
            //  'background-clip' to that value.
            try {
                if (v.clip == null && (css_val.getType() == CssTypes.CSS_IDENT) &&
                        CssBackgroundClip.isMatchingIdent(css_val.getIdent())) {
                    v.clip_value = v.origin_value;
                }
            } catch (InvalidParamException e) {
                // should never happen if defaults are right
            }
        } else {
            v.origin_value = v.origin;
        }

        if (v.clip != null) {
            v.clip_value = v.clip;
        }


        if (v.color == null) {
            v.color_value = (new CssBackgroundColor()).getColor();
        } else {
            v.color_value = v.color;
        }
    }

    /**
     * Transform the compound value into the equivalent individual
     * values (used for conflict check, like color and background-color
     * Note that the value verification already took place, so no need
     * for extra check
     */
    private void transform_into_individual_values() {
        if (value instanceof CssBackgroundValue) {
            CssBackgroundValue v = (CssBackgroundValue) value;
            if (v.color != null) {
                color = new CssBackgroundColor();
                color.set(v.color_value);
            }
            if (v.bg_image != null) {
                image = new CssBackgroundImage();
                image.value = v.bg_image_value;
            }
            if (v.repeat_style != null) {
                repeat = new CssBackgroundRepeat();
                repeat.value = v.repeat_style_value;
            }
            if (v.attachment != null) {
                attachment = new CssBackgroundAttachment();
                attachment.value = v.attachment_value;
            }
            if (v.bg_position != null) {
                position = new CssBackgroundPosition();
                position.value = v.bg_position_value;
            }
            if (v.bg_size != null) {
                size = new CssBackgroundSize();
                size.value = v.bg_size_value;
            }
        } else if (value instanceof CssLayerList) {
            ArrayList vlist = (ArrayList) value.get();
            int len = vlist.size();
            ArrayList<CssValue> images = new ArrayList<CssValue>(len);
            ArrayList<CssValue> repeats = new ArrayList<CssValue>(len);
            ArrayList<CssValue> positions = new ArrayList<CssValue>(len);
            ArrayList<CssValue> attachments = new ArrayList<CssValue>(len);
            ArrayList<CssValue> sizes = new ArrayList<CssValue>(len);

            for (int i = 0; i < len; i++) {
                CssBackgroundValue v = (CssBackgroundValue) vlist.get(i);
                images.add(v.bg_image_value);
                repeats.add(v.repeat_style_value);
                positions.add(v.bg_position_value);
                attachments.add(v.attachment_value);
                sizes.add(v.bg_size_value);
                if (v.color != null) {
                    color = new CssBackgroundColor();
                    color.set(v.color_value);
                }
            }
            image = new CssBackgroundImage();
            image.value = new CssLayerList(images);

            repeat = new CssBackgroundRepeat();
            repeat.value = new CssLayerList(repeats);

            attachment = new CssBackgroundAttachment();
            attachment.value = new CssLayerList(attachments);

            position = new CssBackgroundPosition();
            position.value = new CssLayerList(positions);

            size = new CssBackgroundSize();
            size.value = new CssLayerList(sizes);
        } else {
            // FIXME TODO use inherit?
            image = null;
            repeat = null;
            attachment = null;
            color = null;
            size = null;
            position = null;
        }
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns the color
     */
    public CssValue getColor() {
        if (color == null) {
            return null;
        } else {
            return color.getColor();
        }
    }


// placeholder for the different values

    public class CssBackgroundValue extends CssValueList {

        CssValue bg_image = null;
        CssValue bg_position = null;
        CssValue bg_size = null;
        CssValue repeat_style = null;
        CssValue attachment = null;
        CssValue origin = null;
        CssValue clip = null;
        CssValue color = null;

        CssValue bg_image_value = null;
        CssValue bg_position_value = null;
        CssValue bg_size_value = null;
        CssValue repeat_style_value = null;
        CssValue attachment_value = null;
        CssValue origin_value = null;
        // If 'background-origin' is present and its value matches a possible
        // value for 'background-clip' then it also sets 'background-clip' to
        // that value.
        CssValue clip_value = null;
        CssValue color_value = null;

        public boolean equals(CssBackgroundValue v) {
            if (bg_image_value == null) {
                if (v.bg_image_value != null) {
                    return false;
                }
            } else if (!bg_image_value.equals(v.bg_image_value)) {
                return false;
            }
            if (bg_position_value == null) {
                if (v.bg_position_value != null) {
                    return false;
                }
            } else if (!bg_position_value.equals(v.bg_position_value)) {
                return false;
            }
            if (bg_size_value == null) {
                if (v.bg_size_value != null) {
                    return false;
                }
            } else if (!bg_size_value.equals(v.bg_size_value)) {
                return false;
            }
            if (repeat_style_value == null) {
                if (v.repeat_style_value != null) {
                    return false;
                }
            } else if (!repeat_style_value.equals(v.repeat_style_value)) {
                return false;
            }
            if (attachment_value == null) {
                if (v.attachment_value != null) {
                    return false;
                }
            } else if (!attachment_value.equals(v.attachment_value)) {
                return false;
            }
            if (origin_value == null) {
                if (v.origin_value != null) {
                    return false;
                }
            } else if (!origin_value.equals(v.origin_value)) {
                return false;
            }
            if (clip_value == null) {
                if (v.clip_value != null) {
                    return false;
                }
            } else if (!clip_value.equals(v.clip_value)) {
                return false;
            }
            if (color_value == null) {
                if (v.color_value != null) {
                    return false;
                }
            } else if (!color_value.equals(v.color_value)) {
                return false;
            }
            // at last!
            return true;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (bg_image != null) {
                sb.append(bg_image).append(' ');
            }
            if (bg_position != null) {
                sb.append(bg_position).append(' ');
                if (bg_size != null) {
                    sb.append('/').append(bg_size).append(' ');
                }
            }
            if (repeat_style != null) {
                sb.append(repeat_style).append(' ');
            }
            if (attachment != null) {
                sb.append(attachment).append(' ');
            }
            if (origin != null) {
                sb.append(origin).append(' ');
            }
            if (clip != null) {
                sb.append(clip).append(' ');
            }
            if (color != null) {
                sb.append(color);
            } else {
                int sb_length = sb.length();
                if (sb_length > 0) {
                    sb.setLength(sb_length - 1);
                }
            }
            return sb.toString();
        }
    }
}
