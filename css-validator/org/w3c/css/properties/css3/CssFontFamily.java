// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-family
 */
public class CssFontFamily extends org.w3c.css.properties.css.CssFontFamily {

    public static final ArrayList<CssIdent> genericNames;

    public static final String[] _genericNames = {
            "serif", "sans-serif", "cursive", "fantasy", "monospace",
            "system-ui", "emoji", "math", "fangsong", "ui-serif",
            "ui-sans-serif", "ui-monospace", "ui-rounded"};

    static {
        genericNames = new ArrayList<CssIdent>(_genericNames.length);
        for (String s : _genericNames) {
            genericNames.add(CssIdent.getIdent(s));
        }
    }

    static public CssIdent getGenericFontName(CssIdent ident) {
        int pos = genericNames.indexOf(ident);
        if (pos >= 0) {
            return genericNames.get(pos);
        }
        return null;
    }

    private void checkExpression(ApplContext ac, ArrayList<CssValue> curval,
                                 ArrayList<CssIdent> values, boolean check) {
        CssIdent val;
        if (values.size() > 1) {
            // create a value out of that. We could even create
            // a CssString for the output (TODO ?)
            StringBuilder sb = new StringBuilder("\"");
            boolean addSpace = false;
            for (CssIdent id : values) {
                if (addSpace) {
                    sb.append(' ');
                } else {
                    addSpace = true;
                }
                sb.append(id);
            }
            sb.append('"');
            ac.getFrame().addWarning("with-space", 1);
            curval.add(new CssString(sb.toString()));
        } else {
            val = values.get(0);
            // could be done in the consistency check, but...
            if (null != getGenericFontName(val)) {
                hasGenericFontFamily = true;
            }
            if (CssIdent.isCssWide(val)) {
                // should we warn?;
            }
            curval.add(val);
        }
    }

    // final consistency check
    private void checkValues(ApplContext ac, ArrayList<CssValue> values)
            throws InvalidParamException {
        // we need to check that we don't have 'inherit' in multiple values
        if (values.size() > 1) {
            for (CssValue val : values) {
                if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
            }
        }
    }

    /**
     * Creates a new CssFontFamily
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontFamily(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        ArrayList<CssValue> values = new ArrayList<CssValue>();

        while (!expression.end()) {
            char op = expression.getOperator();
            CssValue val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                    // check it's not a quoted reserved keyword
                    String s = val.toString();
                    if (s.length() > 2) {
                        // we remove quotes and check it's not reserved.
                        CssIdent id = new CssIdent(s.substring(1, s.length() - 1));
                        if (getGenericFontName(id) != null) {
                            ac.getFrame().addWarning("generic-family.quote", 2);
                        }
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    ArrayList<CssIdent> idval = new ArrayList<CssIdent>();
                    idval.add(val.getIdent());
                    // we add idents if separated by spaces...
                    while (op == SPACE && expression.getRemainingCount() > 1) {
                        expression.next();
                        op = expression.getOperator();
                        val = expression.getValue();
                        if (val.getType() == CssTypes.CSS_IDENT) {
                            idval.add(val.getIdent());
                        } else {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                    }
                    checkExpression(ac, values, idval, check);
                    break;
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        checkValues(ac, values);
        value = (values.size() > 1) ? new CssLayerList(values) : values.get(0);
    }

    public CssFontFamily(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public CssFontFamily() {
        value = initial;
    }
}

