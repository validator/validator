//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

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
 * <H4>
 * <A NAME="background">5.3.7 &nbsp;&nbsp; 'background'</A>
 * </H4>
 * <p/>
 * <EM>Value:</EM> &lt;background-color&gt; || &lt;background-image&gt; ||
 * &lt;background-repeat&gt; || &lt;background-attachment&gt; ||
 * &lt;background-position&gt;<BR>
 * <EM>Initial:</EM> not defined for shorthand properties<BR>
 * <EM>Applies to:</EM> all elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> allowed on &lt;background-position&gt;<BR>
 * <p/>
 * The 'background' property is a shorthand property for setting the individual
 * background properties (i.e., 'background-color', 'background-image',
 * 'background-repeat', 'background-attachment' and 'background-position') at
 * the same place in the style sheet.
 * <p/>
 * Possible values on the 'background' properties are the set of all possible
 * values on the individual properties.
 * <PRE>
 * BODY { background: red }
 * P { background: url(chess.png) gray 50% repeat fixed }
 * </PRE>
 * <P> The 'background' property always sets all the individual background
 * properties.  In the first rule of the above example, only a value for
 * 'background-color' has been given and the other individual properties are
 * set to their initial value. In the second rule, all individual properties
 * have been specified.
 *
 * @version $Revision$
 * @see org.w3c.css.properties.css.CssBackgroundColor
 * @see org.w3c.css.properties.css.CssBackgroundImage
 * @see org.w3c.css.properties.css.CssBackgroundRepeat
 * @see org.w3c.css.properties.css.CssBackgroundAttachment
 * @see org.w3c.css.properties.css.CssBackgroundPosition
 */
public class CssBackground extends org.w3c.css.properties.css.CssBackground {

    public CssBackgroundColor color;
    public CssBackgroundImage image;
    public CssBackgroundRepeat repeat;
    public CssBackgroundAttachment attachment;
    public CssBackgroundPosition position;

    public boolean same;

    /**
     * Duplicate this property.
     *
     * @see org.w3c.css.css.CssCascadingOrder#order
     */
    public CssProperty duplicate() {
        CssBackground cloned = (CssBackground) super.duplicate();
        if (cloned != null) {
            if (color != null) {
                cloned.color = (CssBackgroundColor) color.duplicate();
            }
            if (image != null) {
                cloned.image = (CssBackgroundImage) image.duplicate();
            }
            if (repeat != null) {
                cloned.repeat = (CssBackgroundRepeat) repeat.duplicate();
            }
            if (attachment != null) {
                cloned.attachment = (CssBackgroundAttachment) attachment.duplicate();
            }
            if (position != null) {
                cloned.position = (CssBackgroundPosition) position.duplicate();
            }
        }
        return cloned;
    }

    /**
     * Create a new CssBackground
     */
    public CssBackground() {
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @throws InvalidParamException The expression is incorrect
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
                    if (getImage() == null) {
                        setImage(new CssBackgroundImage(ac, expression));
                        continue;
                    }
                    find = false;
                    break;
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    if (getColor2() == null) {
                        setColor(new CssBackgroundColor(ac, expression));
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
                        if (getImage() == null) {
                            setImage(new CssBackgroundImage(ac, expression));
                            find = true;
                        }
                        break;
                    }
                    // check background-repeat ident
                    if (CssBackgroundRepeat.checkMatchingIdent(identval)) {
                        if (getRepeat() == null) {
                            setRepeat(new CssBackgroundRepeat(ac, expression));
                            find = true;
                        }
                        break;
                    }
                    // check background-attachment ident
                    if (CssBackgroundAttachment.checkMatchingIdent(identval)) {
                        if (getAttachment() == null) {
                            setAttachment(new CssBackgroundAttachment(ac, expression));
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

                    if (getColor2() == null) {
                        try {
                            setColor(new CssBackgroundColor(ac, expression));
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
            setPosition(new CssBackgroundPosition(ac, background_position_expression, check));
        }
    }

    public CssBackground(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * @return Returns the attachment.
     */
    public CssBackgroundAttachment getAttachment() {
        return attachment;
    }

    /**
     * @param attachment The attachment to set.
     */
    public void setAttachment(CssBackgroundAttachment attachment) {
        this.attachment = attachment;
    }

    /**
     * @return Returns the image.
     */
    public CssBackgroundImage getImage() {
        return image;
    }

    /**
     * @param image The image to set.
     */
    public void setImage(CssBackgroundImage image) {
        this.image = image;
    }

    /**
     * @return Returns the repeat.
     */
    public CssBackgroundRepeat getRepeat() {
        return repeat;
    }

    /**
     * @param repeat The repeat to set.
     */
    public void setRepeat(CssBackgroundRepeat repeat) {
        this.repeat = repeat;
    }

    /**
     * @return Returns the same.
     */
    public boolean isSame() {
        return same;
    }

    /**
     * @param same The same to set.
     */
    public void setSame(boolean same) {
        this.same = same;
    }

    /**
     * Returns the color
     */
    public final CssBackgroundColor getColor2() {
        return color;
    }

    /**
     * @param color The color to set.
     */
    public void setColor(CssBackgroundColor color) {
        this.color = color;
    }

    /**
     * @return Returns the position.
     */
    public CssBackgroundPosition getPosition() {
        return position;
    }

    /**
     * @param position The position to set.
     */
    public void setPosition(CssBackgroundPosition position) {
        this.position = position;
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
