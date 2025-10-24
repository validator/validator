//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.atsc;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssBackgroundAttachment;
import org.w3c.css.properties.css.CssBackgroundConstants;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssValue;

//import org.w3c.css.values.CssNumber;


/**
 * <H4>
 * &nbsp;&nbsp; 'background-position'
 * </H4>
 * <p/>
 * <EM>Value:</EM> [&lt;percentage&gt; | &lt;length&gt;]{1,2} | [top | center
 * | bottom] || [left | center | right]<BR>
 * <EM>Initial:</EM> 0% 0%<BR>
 * <EM>Applies to:</EM> block-level and replaced elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> refer to the size of the element itself<BR>
 * <P> If a background image has been specified, the value of
 * 'background-position' specifies its initial position.
 * <P> With a value pair of '0% 0%', the upper left corner of the image is
 * placed in the upper left corner of the box that surrounds the content of
 * the element (i.e., not the box that surrounds the padding, border or
 * margin). A value pair of '100% 100%' places the lower right corner of the
 * image in the lower right corner of the element. With a value pair of '14%
 * 84%', the point 14% across and 84% down the image is to be placed at the
 * point 14% across and 84% down the element.
 * <P> With a value pair of '2cm 2cm', the upper left corner of the image is
 * placed 2cm to the right and 2cm below the upper left corner of the element.
 * <P> If only one percentage or length value is given, it sets the horizontal
 * position only, the vertical position will be 50%. If two values are given,
 * the horizontal position comes first. Combinations of length and percentage
 * values are allowed, e.g. '50% 2cm'. Negative positions are allowed.
 * <P> One can also use keyword values to indicate the position of the
 * background image. Keywords cannot be combined with percentage values, or
 * length values.  The possible combinations of keywords and their
 * interpretations are as follows:
 * <p/>
 * <UL>
 * <LI>
 * 'top left' and 'left top' both mean the same as '0% 0%'.
 * <LI>
 * 'top', 'top center' and 'center top' mean the same as '50% 0%'.
 * <LI>
 * 'right top' and 'top right' mean the same as '100% 0%'.
 * <LI>
 * 'left', 'left center' and 'center left' mean the same as '0% 50%'.
 * <LI>
 * 'center' and 'center center' mean the same as '50% 50%'.
 * <LI>
 * 'right', 'right center' and 'center right' mean the same as '100% 50%'.
 * <LI>
 * 'bottom left' and 'left bottom' mean the same as '0% 100%'.
 * <LI>
 * 'bottom', 'bottom center' and 'center bottom' mean the same as '50% 100%'.
 * <LI>
 * 'bottom right' and 'right bottom' mean the same as '100% 100%'.
 * </UL>
 * <p/>
 * examples:
 * <PRE>
 * BODY { background: url(banner.jpeg) right top }    / * 100%   0% * /
 * BODY { background: url(banner.jpeg) top center }   / *  50%   0% * /
 * BODY { background: url(banner.jpeg) center }       / *  50%  50% * /
 * BODY { background: url(banner.jpeg) bottom }       / *  50% 100% * /
 * </PRE>
 * <p/>
 * If the background image is fixed with regard to the canvas (see the
 * 'background-attachment' property above), the image is placed relative to
 * the canvas instead of the element. E.g.:
 * <PRE>
 * BODY {
 * background-image: url(logo.png);
 * background-attachment: fixed;
 * background-position: 100% 100%;
 * }
 * </PRE>
 * <p/>
 * In the example above, the image is placed in the lower right corner of the
 * canvas.
 *
 * @version $Revision$
 * @see CssBackgroundAttachment
 */
public class CssBackgroundPositionATSC extends CssProperty
        implements CssBackgroundConstants, CssOperator {

    CssValue first;
    CssValue second;

    /**
     * Create a new CssBackgroundPositionATSC
     */
    public CssBackgroundPositionATSC() {
        first = DefaultValue0;
        second = DefaultValue0;
    }

    /**
     * Creates a new CssBackgroundPositionATSC
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundPositionATSC(ApplContext ac, CssExpression expression,
                                     boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val = expression.getValue();
        char op = expression.getOperator();

        if (op != SPACE)
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);

        if (val.equals(inherit)) {
            if (expression.getCount() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            first = inherit;
            second = inherit;
            expression.next();
            return;
        }

        CssValue next = expression.getNextValue();

        if (val instanceof CssIdent) {
            int index1 = IndexOfIdent((String) val.get());
            // two keywords
            if (next instanceof CssIdent) {
                int index2 = IndexOfIdent((String) next.get());
                // one is vertical, the other is vertical
                // or the two are 'center'
                if ((isHorizontal(index1) && isVertical(index2)) ||
                        (isHorizontal(index2) && isVertical(index1))) {
                    first = val;
                    second = next;
                }
                // both are horizontal or vertical but not 'center'
                else if (check) {
                    throw new InvalidParamException("incompatible",
                            val, next, ac);
                } else {
                    first = val;
                }
            }
            // a keyword and a percentage/length
            else if (next instanceof CssLength || next instanceof CssPercentage
                    || next instanceof CssNumber) {
                if (next instanceof CssNumber) {
                    next = ((CssNumber) next).getLength();
                }
                if (isHorizontal(index1)) {
                    first = val;
                    second = next;
                }
                // if the keyword is the first value, it can only be an
                // horizontal one
                else {
                    throw new InvalidParamException("incompatible",
                            val, next, ac);
                }
            }
            // only one value
            else if (next == null) {
                first = val;
            }
            // the second value is invalid
            else if (check) {
                throw new InvalidParamException("value", next,
                        getPropertyName(), ac);
            } else {
                first = val;
            }
        } else if (val instanceof CssLength || val instanceof CssPercentage ||
                val instanceof CssNumber) {
            if (val instanceof CssNumber) {
                val = ((CssNumber) val).getLength();
            }
            // a percentage/length and an keyword
            if (next instanceof CssIdent) {
                int index = IndexOfIdent((String) next.get());
                // the keyword must be a vertical one
                if (isVertical(index)) {
                    first = val;
                    second = next;
                } else if (check) {
                    throw new InvalidParamException("incompatible",
                            val, next, ac);
                } else {
                    first = val;
                }
            } else if (next instanceof CssLength || next instanceof CssPercentage
                    || next instanceof CssNumber) {
                if (next instanceof CssNumber) {
                    next = ((CssNumber) next).getLength();
                }
                first = val;
                second = next;
            } else if (next == null || !check) {
                first = val;
            } else {
                throw new InvalidParamException("incompatible", val, next, ac);
            }
        } else if (check) {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }

        // we only move the cursor if we found valid values
        if (first != null) {
            expression.next();
        }
        if (second != null) {
            expression.next();
        }
    }

    public CssBackgroundPositionATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private boolean isHorizontal(int index) {
        return index == POSITION_LEFT || index == POSITION_RIGHT ||
                index == POSITION_CENTER;
    }

    private boolean isVertical(int index) {
        return index == POSITION_TOP || index == POSITION_BOTTOM ||
                index == POSITION_CENTER;
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return first;
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "background-position";
    }

    /**
     * Returns the horizontal value of the position
     */
    public CssValue getHorizontalPosition() {
        return first;
    }

    /**
     * Returns the vertical value of the position
     */
    public CssValue getVerticalPosition() {
        return second;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return first == inherit;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (first == inherit) {
            return inherit.toString();
        } else {
            String ret = "";
            if (first != null) {
                ret += first;
            }
            if (second != null) {
                if (!ret.equals("")) {
                    ret += " ";
                }
                ret += second;
            }

            return ret;
        }
    }
/*
    private void getPercentageFromIdent(int first, int second) {
	this.first = DefaultValue50;
	this.second = DefaultValue50;
	if (first == POSITION_LEFT || second == POSITION_LEFT)
	    this.first = DefaultValue0;
	if (first == POSITION_RIGHT || second == POSITION_RIGHT)
	    this.first = DefaultValue100;
	if (first == POSITION_TOP || second == POSITION_TOP)
	    this.second = DefaultValue0;
	if (first == POSITION_BOTTOM || second == POSITION_BOTTOM)
	    this.second = DefaultValue100;
    }
*/

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBackgroundATSC cssBackground = ((ATSCStyle) style).cssBackgroundATSC;
        if (cssBackground.position != null)
            style.addRedefinitionWarning(ac, this);
        cssBackground.position = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((ATSCStyle) style).getBackgroundPositionATSC();
        } else {
            return ((ATSCStyle) style).cssBackgroundATSC.position;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundPositionATSC &&
                first.equals(((CssBackgroundPositionATSC) property).first)
                && second.equals(((CssBackgroundPositionATSC) property).second));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return first.equals(DefaultValue0) && second.equals(DefaultValue0);
    }

    private int IndexOfIdent(String ident) throws InvalidParamException {
        int hash = ident.hashCode();
        for (int i = 0; i < POSITION.length; i++)
            if (hash_values[i] == hash)
                return i;

        return -1;
    }

    private static int[] hash_values;

    //    private static int INVALID = -1;
    private static CssPercentage DefaultValue0 = new CssPercentage(0);
//    private static CssPercentage DefaultValue50 = new CssPercentage(50);
//    private static CssPercentage DefaultValue100 = new CssPercentage(100);

    static {
        hash_values = new int[POSITION.length];
        for (int i = 0; i < POSITION.length; i++)
            hash_values[i] = POSITION[i].hashCode();
    }
}
