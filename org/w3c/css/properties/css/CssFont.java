// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @version $Revision$
 * @since CSS1
 */
public class CssFont extends CssProperty {

    // @since CSS1
    public CssFontFamily fontFamily;
    public CssFontSize fontSize;
    public CssFontStyle fontStyle;
    public CssFontVariant fontVariant;
    public CssFontWeight fontWeight;

    public CssLineHeight lineHeight;

    public boolean compound = false;

    /**
     * Create a new CssFontSize
     */
    public CssFont() {
    }

    /**
     * Creates a new CssFontSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFont(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssFont(ApplContext ac, CssExpression expression)
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
        return "font";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return inherit.equals(value);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (value != null) {
            return value.toString();
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        if (fontStyle != null) {
            sb.append(fontStyle);
            first = false;
        }
        if (fontVariant != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontVariant);
        }
        if (fontWeight != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontWeight);
        }
        // no need to test, if we are here we should have one!
        if (fontSize != null) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(fontSize);
        }
        if (lineHeight != null) {
            sb.append('/');
            sb.append(lineHeight);
        }
        // should always be there...
        if (fontFamily != null) {
            sb.append(' ');
            sb.append(fontFamily);
        }
        return sb.toString();
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssFont cssFont = ((Css1Style) style).cssFont;
        // if we already have a font shorthand defined
        // raise a warning
        if (cssFont.compound) {
            style.addRedefinitionWarning(ac, this);
        }
        cssFont.compound = true;
        // and now test all the individual property redefinitions
        if (fontFamily != null) {
            fontFamily.addToStyle(ac, style);
        }
        if (fontSize != null) {
            fontSize.addToStyle(ac, style);
        }
        if (fontVariant != null) {
            fontVariant.addToStyle(ac, style);
        }
        if (fontWeight != null) {
            fontWeight.addToStyle(ac, style);
        }
        if (lineHeight != null) {
            lineHeight.addToStyle(ac, style);
        }
    }


    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssFont &&
                value.equals(((CssFont) property).value));
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
        if (fontStyle != null) {
            fontStyle.setSelectors(selector);
        }
        if (fontVariant != null) {
            fontVariant.setSelectors(selector);
        }
        if (fontWeight != null) {
            fontWeight.setSelectors(selector);
        }
        if (fontSize != null) {
            fontSize.setSelectors(selector);
        }
        if (lineHeight != null) {
            lineHeight.setSelectors(selector);
        }
        if (fontFamily != null) {
            fontFamily.setSelectors(selector);
        }
    }

    /**
     * Set this property to be important.
     * Overrides this method for a macro
     */
    public void setImportant() {
        super.setImportant();
        if (fontStyle != null)
            fontStyle.important = true;
        if (fontVariant != null)
            fontVariant.important = true;
        if (fontWeight != null)
            fontWeight.important = true;
        if (fontSize != null)
            fontSize.important = true;
        if (lineHeight != null)
            lineHeight.important = true;
        if (fontFamily != null)
            fontFamily.important = true;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getFont();
        } else {
            return ((Css1Style) style).cssFont;
        }
    }
}

