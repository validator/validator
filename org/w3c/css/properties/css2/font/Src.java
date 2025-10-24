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
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssURL;
import org.w3c.css.values.CssValue;

import java.util.Vector;

/**
 * @version $Revision$
 */
public class Src extends CssProperty
        implements CssOperator {

    Vector values = new Vector();

    /**
     * Create a new CssSrc
     */
    public Src() {
    }

    /**
     * Create a new CssSrc
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public Src(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;

        setByUser();
        do {
            val = expression.getValue();
            op = expression.getOperator();
            if (val instanceof CssURL) {
                values.addElement(val);
                expression.next();
                if (!expression.end() && (op == SPACE)
                        && (expression.getValue() instanceof CssFunction)) {
                    val = expression.getValue();
                    // @@ HACK
                    values.addElement(" ");
                    values.addElement(recognizeFormat(ac, (CssFunction) val));
                    op = expression.getOperator();
                    expression.next();
                }
            } else if (val instanceof CssFunction) {
                values.addElement(recognizeFontFaceName(ac, (CssFunction) val));
                expression.next();
            } else {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            // @@HACK
            values.addElement(", ");
        } while (op == COMMA);
    }

    public Src(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return null;
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "src";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        String ret = "";
        int i = 0;
        while (i != (values.size() - 1)) {
            ret += values.elementAt(i++);
        }
        return ret;
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.src != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.src = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getSrc();
        } else {
            return ((Css2Style) style).src;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return false;
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

    private CssFunction recognizeFormat(ApplContext ac, CssFunction val)
            throws InvalidParamException {
        if (val.getName().equals("format")) {
            CssExpression params = val.getParameters();
            char op;
            params.starts();
            do {
                op = params.getOperator();
                if (params.getValue() instanceof CssString) {
                    // nothing
                } else {
                    throw new InvalidParamException("format",
                            val,
                            getPropertyName(), ac);
                }
                params.next();
            } while (op == COMMA);
            if (!params.end()) {
                throw new InvalidParamException("format",
                        val,
                        getPropertyName(), ac);
            }
            params.starts();
            return val;
        } else {
            throw new InvalidParamException("format",
                    val,
                    getPropertyName(), ac);
        }
    }

    private CssFunction recognizeFontFaceName(ApplContext ac, CssFunction func)
            throws InvalidParamException {
        if (func.getName().equals("local")) {
            CssExpression params = func.getParameters();
            char op;
            params.starts();

            if (params.getValue() instanceof CssString) {
                if (params.getCount() == 1) {
                    return func;
                } else {
                    throw new InvalidParamException("local",
                            func,
                            getPropertyName(), ac);
                }
            }

            do {
                op = params.getOperator();
                if (params.getValue() instanceof CssIdent) {
                    // nothing
                } else {
                    throw new InvalidParamException("local",
                            func,
                            getPropertyName(), ac);
                }
                params.next();
            } while (op == COMMA);
            if (!params.end()) {
                throw new InvalidParamException("local",
                        func,
                        getPropertyName(), ac);
            }
            params.starts();
            return func;
        } else {
            throw new InvalidParamException("local",
                    func,
                    getPropertyName(), ac);
        }
    }
}
