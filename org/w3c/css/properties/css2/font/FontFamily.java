//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */

package org.w3c.css.properties.css2.font;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssValue;

import java.util.Enumeration;
import java.util.Vector;

/**
 *
 */
public class FontFamily extends FontProperty implements CssOperator {

    Vector family_name = new Vector();

    /**
     * Create a new FontFamily
     */
    public FontFamily() {
    }

    /**
     * Create a new FontFamily
     *
     * @param expression the font name
     * @throws InvalidParamException The expression is incorrect
     */
    public FontFamily(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        boolean family = true;
        CssValue val = expression.getValue();
        char op;

        setByUser();
        //@@ and if name is already in the vector ?


        while (family) {
            val = expression.getValue();
            op = expression.getOperator();

            if ((op != COMMA) && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            if (val instanceof CssString) {
                if (op == COMMA) { // "helvetica", "roman"
                    String name = (String) val.get();
                    family_name.addElement(trimToOneSpace(name));
                    expression.next();
                } else { // "helvetica" CssValue
                    String name = (String) val.get();
                    family_name.addElement(trimToOneSpace(name));
                    family = false;
                    expression.next();
                }
            } else if (val instanceof CssIdent) {
                if (op == COMMA) {
                    family_name.addElement(val.toString());
                    expression.next();
                } else {
                    CssValue next = expression.getNextValue();

                    if (next instanceof CssIdent) {
                        CssIdent New = new CssIdent(val.get() + " "
                                + next.get());
                        expression.remove();
                        op = expression.getOperator();
                        expression.remove();
                        expression.insert(New);
                        expression.setCurrentOperator(op);
                    } else {
                        family_name.addElement(val.toString());
                        expression.next();
                        family = false;
                    }
                }
            } else
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }

    }

    public FontFamily(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns all fonts name
     */
    public Enumeration elements() {
        return family_name.elements();
    }

    /**
     * Returns the size
     */
    public int size() {
        return family_name.size();
    }

    /**
     * Returns the font (null if no font)
     */
    public Object get() {
        if (family_name.size() == 0) {
            return null;
        }
        return family_name.firstElement();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        String r = "";
        for (Enumeration e = elements(); e.hasMoreElements(); )
            r += ", " + convertString(e.nextElement().toString());
        if (r.length() < 3) {
            return null;
        }
        return r.substring(2);
    }

    String convertString(String value) {
        char cfirst = value.charAt(0);
        char clast = value.charAt(value.length() - 1);

        if (cfirst == clast
                && (cfirst == '\'' || cfirst == '"')
        ) {
            // is already well escaped
            return value;
        }

        if (value.indexOf('"') != -1) {
            return '\'' + value + '\'';
        } else if (value.indexOf('\'') != -1) {
            return '"' + value + '"';
        } else {
            return value;
        }
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "font-family";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;

        if (style0.fontFamily != null) {
            style.addRedefinitionWarning(ac, this);
        }
        style0.fontFamily = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getFaceFontFamily();
        } else {
            return ((Css2Style) style).fontFamily;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return false; //@@ FIXME
    }

    private static String trimToOneSpace(String name) {
        int count = name.length();
        char[] dst = new char[count];
        char[] src = new char[count];
        int index = -1;

        name.getChars(0, count, src, 0);
        for (int i = 0; i < count; i++)
            if (i == 0 || !Character.isWhitespace(src[i]) ||
                    (Character.isWhitespace(src[i]) &&
                            !Character.isWhitespace(dst[index])))
                dst[++index] = src[i];

        return new String(dst, 0, index + 1);
    }

}
