// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.css.StyleSheetOrigin;
import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Messages;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssValue;

/**
 * <DL>
 * <DT>
 * <STRONG>property</STRONG>
 * <DD>
 * a stylistic parameter that can be influenced through CSS. This specification
 * defines a list of properties and their corresponding values.
 * </DL>
 * <p/>
 * If you want to add some properties to the parser, you should subclass this
 * class.
 *
 * @version $Revision$
 */
public abstract class CssProperty
        implements Cloneable, StyleSheetOrigin {

    /**
     * In most case there should be one value
     */
    public CssValue value = null;

    /**
     * True if this property is important. false otherwise.
     */
    public boolean important;

    /**
     * The origin of this property.
     * the author's style sheets override the reader's style sheet which
     * override the UA's default values. An imported style sheet has the same
     * origin as the style sheet from which it is imported.
     *
     * @see StyleSheetOrigin#BROWSER
     * @see StyleSheetOrigin#READER
     * @see StyleSheetOrigin#AUTHOR
     */
    public int origin;

    /**
     * A unique number for this property.
     * Used by the cascading order algorithm to sort by order specified.
     * If two rules have the same weight, the latter specified wins.
     */
    public long order;

    /**
     * the position of the first character of this value.
     */
    public int line;

    /**
     * the origin file.
     */
    public String sourceFile;

    /**
     * the context.
     */
    public CssSelectors context;

    // internal counter
    private static long increment;

    /**
     * for validator only, true if the property comes from a file
     */
    public boolean byUser = false;

    /**
     * This keyword is used a lot of time in CSS2
     */
    public static final CssIdent inherit;

    /**
     * Used in all CSS versions
     */
    public static final CssIdent transparent;

    /**
     * Value introduced in CSS3
     */
    public static final CssIdent initial;
    public static final CssIdent none;
    public static final CssIdent currentColor;
    public static final CssIdent unset;
    public static final CssIdent revert;

    static {
        inherit = CssIdent.getIdent("inherit");
        transparent = CssIdent.getIdent("transparent");
        initial = CssIdent.getIdent("initial");
        none = CssIdent.getIdent("none");
        unset = CssIdent.getIdent("unset");
        revert = CssIdent.getIdent("revert");
        currentColor = CssIdent.getIdent("currentColor");
    }

    /**
     * Create a new CssProperty.
     */
    public CssProperty() {
        order = increment++;
    }

    /**
     * @return true if the property is inherited.
     */
    public boolean inherited() {
        return CssProperties.getInheritance(this);
    }

    /**
     * @return true if this property is "softly" inherited
     *         e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return false;
    }

    /**
     * @return the value of this property.
     *         It is not very usable, implements your own function.
     */
    public abstract Object get();

    /**
     * @return the name of this property IN LOWER CASE.
     */
    public abstract String getPropertyName();

    /**
     * @return the name of this property IN LOWER CASE. escaped
     */
    public String getPropertyNameEscaped() {
        return Messages.escapeString(getPropertyName());
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     * @return a boolean, true if equal
     */
    public abstract boolean equals(CssProperty property);

    /**
     * Returns a string representation of values.
     * <BR>
     * So if you want have something like this :
     * <SAMP> property-name : property-value1 properpty-value2 ...</SAMP>
     * <BR>
     * You should write something like this :
     * <code>property.getPropertyName() + " : " + property.toString()</code>
     */
    public abstract String toString();

    public String getEscaped() {
        return Messages.escapeString(toString());
    }

    /**
     * Set this property to be important.
     * Overrides this method for a macro
     */
    public void setImportant() {
        important = true;
    }

    /**
     * Returns true if this property is important.
     * Overrides this method for a macro
     *
     * @return a <EM>boolean</EM> true if important
     */
    public boolean getImportant() {
        return important;
    }

    /**
     * Calculate an hashCode for this property.
     */
    public final int hashCode() {
        return getPropertyName().hashCode();
    }

    /**
     * Update the source file and the line.
     * Overrides this method for a macro
     *
     * @param line   The line number where this property is defined
     * @param source The source file where this property is defined
     */
    public void setInfo(int line, String source) {
        this.line = line;
        this.sourceFile = source;
    }

    /**
     * Fix the origin of this property
     * Overrides this method for a macro
     *
     * @param origin, an <EM>int</EM>
     * @see #BROWSER
     * @see #READER
     * @see #AUTHOR
     */
    public void setOrigin(int origin) {
        this.origin = origin;
    }

    /**
     * Returns the attribute origin
     *
     * @return the value of the attribute
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * Is the value of this property is a default value.
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * Set the context.
     * Overrides this method for a macro
     *
     * @see org.w3c.css.css.CssCascadingOrder#order
     * @see org.w3c.css.css.StyleSheetParser#handleRule
     */
    public void setSelectors(CssSelectors context) {
        this.context = context;
    }

    /**
     * Returns the context.
     *
     * @see org.w3c.css.css.CssCascadingOrder
     */
    public CssSelectors getSelectors() {
        return context;
    }

    /**
     * Duplicate this property.
     *
     * @see org.w3c.css.css.CssCascadingOrder#order
     */
    public CssProperty duplicate() {
        try {
            return (CssProperty) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public abstract void addToStyle(ApplContext ac, CssStyle style);

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public abstract CssProperty getPropertyInStyle(CssStyle style,
                                                   boolean resolve);

    /**
     * Returns the source file.
     */
    public final String getSourceFile() {
        return sourceFile;
    }

    /**
     * Returns the line number in the source file.
     */
    public final int getLine() {
        return line;
    }

    /**
     * Calculate the explicit weight and the origin.
     * Declarations marked '!important' carry more weight than unmarked
     * (normal) declarations.
     *
     * @see org.w3c.css.css.CssCascadingOrder
     */
    public final int getExplicitWeight() {
        // browser < reader < author < browser !important < reader !important
        //                                                < author !important
        // here, I use a little trick :
        //  1 < 2 < 3 < 4 ( 1 + 3 ) < 5 ( 2 + 3 ) < 6 ( 3 + 3 )
        return origin + ((important) ? AUTHOR : 0);
    }

    /**
     * Calculate the order specified.
     *
     * @see org.w3c.css.css.CssCascadingOrder
     * @see #order
     */
    public final long getOrderSpecified() {
        return order;
    }

    /**
     * Mark this property comes from the user
     */
    public final void setByUser() {
        byUser = true;
    }

    /**
     * Returns the attribute byUser
     *
     * @return the value of the attribute
     */
    public boolean isByUser() {
        return byUser;
    }

}




