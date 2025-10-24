// $Id$
// Author: Jean-Guilhem Rouel
// Revised by: Yves Lafon
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005-2008.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.mobile;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/CR-css-mobile-20081210/#properties
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/colors.html#propdef-background
 */
public class CssBackground extends org.w3c.css.properties.css.CssBackground {

    public CssBackgroundColor color;
    public CssBackgroundImage image;
    public CssBackgroundRepeat repeat;
    public CssBackgroundAttachment attachment;
    public CssBackgroundPosition position;

    public boolean same;

    /**
     * Create a new CssBackground
     */
    public CssBackground() {
        super();
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBackground(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {
        CssValue val;
        char op = SPACE;
        boolean find = true;
        CssExpression background_position_expression = null;

        // too many values
        if (check && expression.getCount() > 6) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        boolean manyValues = (expression.getCount() > 1);

        while (find) {
            val = expression.getValue();
            if (val == null) {
                break;
            }
            op = expression.getOperator();

            // if there are many values, we can't have inherit as one of them
            if (manyValues && val.equals(inherit)) {
                throw new InvalidParamException("unrecognize", null, null, ac);
            }

            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                    if (check) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    find = false;
                    break;
                case CssTypes.CSS_URL:
                    if (image == null) {
                        image = new CssBackgroundImage(ac, expression);
                        continue;
                    }
                    find = false;
                    break;
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    if (color == null) {
                        color = new CssBackgroundColor(ac, expression);
                        continue;
                    }
                    find = false;
                    break;
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_LENGTH:
                    if (background_position_expression == null) {
                        background_position_expression = new CssExpression();
                    }
                    background_position_expression.addValue(val);
                    expression.next();
                    find = true;
                    break;
                case CssTypes.CSS_IDENT:
                    // the hard part, as ident can be from different subproperties
                    find = false;
                    CssIdent identval = (CssIdent) val;
                    if (inherit.equals(identval) && !manyValues) {
                        find = true;
                        same = true;
                        expression.next();
                        break;
                    }
                    // check background-image ident
                    if (CssBackgroundImage.checkMatchingIdent(identval)) {
                        if (image == null) {
                            image = new CssBackgroundImage(ac, expression);
                            find = true;
                        }
                        break;
                    }
                    // check background-repeat ident
                    if (CssBackgroundRepeat.checkMatchingIdent(identval)) {
                        if (repeat == null) {
                            repeat = new CssBackgroundRepeat(ac, expression);
                            find = true;
                        }
                        break;
                    }
                    // check background-attachment ident
                    if (CssBackgroundAttachment.checkMatchingIdent(identval)) {
                        if (attachment == null) {
                            attachment = new CssBackgroundAttachment(ac, expression);
                            find = true;
                        }
                        break;
                    }
                    // check background-position ident
                    if (CssBackgroundPosition.checkMatchingIdent(identval)) {
                        if (background_position_expression == null) {
                            background_position_expression = new CssExpression();
                        }
                        background_position_expression.addValue(val);
                        expression.next();
                        find = true;
                        break;
                    }

                    if (color == null) {
                        try {
                            color = new CssBackgroundColor(ac, expression);
                            find = true;
                            break;
                        } catch (InvalidParamException e) {
                            // nothing to do, image will test this value
                        }
                    }

                default:
                    if (check) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    find = false;
            }
            if (check && !find) {
                throw new InvalidParamException("unrecognize", ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (background_position_expression != null) {
            position = new CssBackgroundPosition(ac,
                    background_position_expression,
                    check);
        }
    }

    public CssBackground(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return color;
    }

    /**
     * Returns the color
     */
    public final CssValue getColor() {
        if (color == null) {
            return null;
        } else {
            return color.getColor();
        }
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {

        if (same) {
            return inherit.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            boolean addspace = false;

            if (color != null) {
                sb.append(color);
                addspace = true;
            }
            if (image != null) {
                if (addspace) {
                    sb.append(' ');
                }
                sb.append(image);
                addspace = true;
            }
            if (repeat != null) {
                if (addspace) {
                    sb.append(' ');
                }
                sb.append(repeat);
                addspace = true;
            }
            if (attachment != null) {
                if (addspace) {
                    sb.append(' ');
                }
                sb.append(attachment);
                addspace = true;
            }
            if (position != null) {
                if (addspace) {
                    sb.append(' ');
                }
                sb.append(position);
            }
            return sb.toString();
        }
    }

    /**
     * Set this property to be important.
     * Overrides this method for a macro
     */

    public void setImportant() {
        important = true;
        if (color != null) {
            color.important = true;
        }
        if (image != null) {
            image.important = true;
        }
        if (repeat != null) {
            repeat.important = true;
        }
        if (attachment != null) {
            attachment.important = true;
        }
        if (position != null) {
            position.important = true;
        }
    }

    /**
     * Returns true if this property is important.
     * Overrides this method for a macro
     */
    public boolean getImportant() {
        if (same) {
            return important;
        }
        return ((color == null || color.important) &&
                (image == null || image.important) &&
                (repeat == null || repeat.important) &&
                (attachment == null || attachment.important) &&
                (position == null || position.important));
    }

    /**
     * Set the context.
     * Overrides this method for a macro
     *
     * @see org.w3c.css.css.CssCascadingOrder#order
     * @see org.w3c.css.css.StyleSheetParser#handleRule
     */
    public void setSelectors(CssSelectors selector) {
        super.setSelectors(selector);
        if (color != null) {
            color.setSelectors(selector);
        }
        if (image != null) {
            image.setSelectors(selector);
        }
        if (repeat != null) {
            repeat.setSelectors(selector);
        }
        if (attachment != null) {
            attachment.setSelectors(selector);
        }
        if (position != null) {
            position.setSelectors(selector);
        }
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        ((Css1Style) style).cssBackground.same = same;
        ((Css1Style) style).cssBackground.byUser = byUser;

        if (color != null) {
            color.addToStyle(ac, style);
        }
        if (image != null) {
            image.addToStyle(ac, style);
        }
        if (repeat != null) {
            repeat.addToStyle(ac, style);
        }
        if (attachment != null) {
            attachment.addToStyle(ac, style);
        }
        if (position != null) {
            position.addToStyle(ac, style);
        }
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBackground();
        } else {
            return ((Css1Style) style).cssBackground;
        }
    }

}
