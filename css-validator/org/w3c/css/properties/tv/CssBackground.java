//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.tv;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.properties.css2.CssBackgroundColor;
import org.w3c.css.properties.css2.CssBackgroundImage;
import org.w3c.css.properties.css2.CssBackgroundPosition;
import org.w3c.css.properties.css2.CssBackgroundRepeat;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2003/CR-css-tv-20030514#section-properties
 * @spec
 * @see org.w3c.css.properties.css21.CssBackgroundColor
 * @see org.w3c.css.properties.css21.CssBackgroundImage
 * @see org.w3c.css.properties.css21.CssBackgroundRepeat
 * @see org.w3c.css.properties.css21.CssBackgroundPosition
 */
public class CssBackground extends org.w3c.css.properties.css.CssBackground {

    CssBackgroundColor color;
    CssBackgroundImage image;
    CssBackgroundRepeat repeat;
    CssBackgroundPosition position;

    boolean same;

    /**
     * Create a new CssBackground
     */
    public CssBackground() {
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
        if (check && expression.getCount() > 5) {
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
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (color != null) {
            sb.append(color);
            first = false;
        }
        if (image != null) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(image);
            first = false;
        }
        if (repeat != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append(repeat);
        }
        if (attachment != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append(attachment);
        }
        if (position != null) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(position);
        }
        return sb.toString();
    }

    /**
     * Set this property to be important.
     * Overrides this method for a macro
     */
    public void setImportant() {
        super.setImportant();
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
        return important && ((color == null || color.important) &&
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

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return false; // FIXME
    }

    /**
     * Update the source file and the line.
     * Overrides this method for a macro
     *
     * @param line   The line number where this property is defined
     * @param source The source file where this property is defined
     */
    public void setInfo(int line, String source) {
        super.setInfo(line, source);
        if (color != null) {
            color.setInfo(line, source);
        }
        if (image != null) {
            image.setInfo(line, source);
        }
        if (repeat != null) {
            repeat.setInfo(line, source);
        }
        if (attachment != null) {
            attachment.setInfo(line, source);
        }
        if (position != null) {
            position.setInfo(line, source);
        }
    }
}
