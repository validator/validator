//
// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// (c) COPYRIGHT 1995-2000  World Wide Web Consortium (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssValue;

import java.util.Vector;

/**
 * <p/>
 * <EM>Value:</EM> none || &lt;key-press-combination&gt;+ || &lt;system-key-equivalent&gt; || inherit<BR>
 * <EM>Initial:</EM>none<BR>
 * <EM>Applies to:</EM>all enabled elements<BR>
 * <EM>Inherited:</EM>no<BR>
 * <EM>Percentages:</EM>no<BR>
 * <EM>Media:</EM>:interactive
 * @deprecated 
 */
@Deprecated
public class CssRubySpan extends CssProperty {

    CssValue rubyspan;
    Vector values = new Vector();

    CssIdent none = new CssIdent("none");

    /**
     * Create a new CssRubySpan
     */
    public CssRubySpan() {
        rubyspan = none;
    }

    /**
     * Create a new CssRubySpan
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssRubySpan(ApplContext ac, CssExpression expression,
                       boolean check) throws InvalidParamException {

        //String kc = new String();
        //int hyphenindex;
        //int counter = 0;
        //char op = expression.getOperator();
        CssValue val = expression.getValue();
        //String part = new String();
        //String rest = new String();
        //Vector ks = new Vector();
        setByUser();

        if (val.equals(none)) {
            rubyspan = none;
            expression.next();
            return;
        } else if (val.equals(inherit)) {
            rubyspan = inherit;
            expression.next();
            return;
        } else if (val instanceof CssFunction) {
            CssFunction attr = val.getFunction();
            CssExpression params = attr.getParameters();
            CssValue v = params.getValue();
            if (attr.getName().equals("attr")) {
                if ((params.getCount() != 1)
                        || !(v instanceof CssIdent)) {
                    throw new InvalidParamException("attr", params.getValue(),
                            getPropertyName(), ac);
                }
            } else throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
            rubyspan = val;
            expression.next();
            return;
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }

    }

    public CssRubySpan(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css3Style) style).cssRubySpan != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssRubySpan = this;

    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getRubySpan();
        } else {
            return ((Css3Style) style).cssRubySpan;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssRubySpan &&
                rubyspan.equals(((CssRubySpan) property).rubyspan));
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "ruby-span";
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return values;
    }

    /**
     * Returns true if this property is "softly" inherited
     */
    public boolean isSoftlyInherited() {
        // @@TODO
        return false;
        //values.equals(inherit);
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        if (rubyspan != null)
            return rubyspan.toString();
        else
            return values.firstElement().toString();
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return rubyspan == none;
    }

}
