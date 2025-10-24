// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Rewritten by Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, Keio and ERCIM, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

/**
 * @since CSS1
 */
public class CssBackgroundPosition extends CssProperty {

    private static final CssIdent center, top, bottom, left, right;
    private static final CssPercentage defaultPercent0, defaultPercent50;
    private static final CssPercentage defaultPercent100;

    static {
        top = CssIdent.getIdent("top");
        bottom = CssIdent.getIdent("bottom");
        left = CssIdent.getIdent("left");
        right = CssIdent.getIdent("right");
        center = CssIdent.getIdent("center");

        defaultPercent0 = new CssPercentage(0);
        defaultPercent50 = new CssPercentage(50);
        defaultPercent100 = new CssPercentage(100);
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
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundPosition(ApplContext ac, CssExpression expression,
                                 boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    public CssBackgroundPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "background-position";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return (inherit == value);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBackground cssBackground = ((Css1Style) style).cssBackground;
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
            return ((Css1Style) style).getBackgroundPosition();
        } else {
            return ((Css1Style) style).cssBackground.position;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return ((property != null) &&
                (property instanceof CssBackgroundPosition) &&
                (value.equals(((CssBackgroundPosition) property).value)));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
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

    // placeholder for the different values

    public class CssBackgroundPositionValue extends CssValueList {
        public CssValue vertical = null;
        public CssValue horizontal = null;
        public CssValue vertical_offset = null;
        public CssValue horizontal_offset = null;

        public CssValue val_vertical = defaultPercent0;
        public CssValue val_horizontal = defaultPercent0;

        public boolean equals(CssBackgroundPositionValue v) {
            // check vertical compatibility (with optional values)
            if (!val_vertical.equals(v.val_vertical)) {
                return false;
            }
            if (vertical_offset != null) {
                if (!vertical_offset.equals(v.vertical_offset)) {
                    return false;
                }
            } else if (v.vertical_offset != null) {
                return false;
            }

            if (!val_horizontal.equals(v.val_horizontal)) {
                return false;
            }
            if (horizontal_offset != null) {
                if (!horizontal_offset.equals(v.horizontal_offset)) {
                    return false;
                }
            } else if (v.horizontal_offset != null) {
                return false;
            }
            // yeah!
            return true;
        }
    }

}
