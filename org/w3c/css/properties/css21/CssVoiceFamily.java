// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-voice-family
 */
public class CssVoiceFamily extends org.w3c.css.properties.css.CssVoiceFamily {

    public static final CssIdent[] genericVoices;

    static {
        String[] _genericVoices = {"male", "female", "child"};

        genericVoices = new CssIdent[_genericVoices.length];
        int i = 0;
        for (String s : _genericVoices) {
            genericVoices[i++] = CssIdent.getIdent(s);
        }
    }

    static CssIdent getGenericVoiceName(CssIdent ident) {
        for (CssIdent id : genericVoices) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssVoiceFamily
     */
    public CssVoiceFamily() {
    }

    /**
     * Creates a new CssVoiceFamily
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssVoiceFamily(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();

        while (!expression.end()) {
            char op = expression.getOperator();
            CssValue val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                    String s = val.toString();
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    ArrayList<CssIdent> idval = new ArrayList<CssIdent>();
                    idval.add((CssIdent) val);
                    // we add idents if separated by spaces...
                    while (op == SPACE && expression.getRemainingCount() > 1) {
                        expression.next();
                        op = expression.getOperator();
                        val = expression.getValue();
                        if (val.getType() == CssTypes.CSS_IDENT) {
                            idval.add((CssIdent) val);
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
        value = (values.size() > 1) ? new CssLayerList(values) : values.get(0);
        checkValues(ac, values);
    }

    public CssVoiceFamily(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private void checkExpression(ApplContext ac, ArrayList<CssValue> curval,
                                 ArrayList<CssIdent> values, boolean check) {
        CssIdent val;
        if (values.size() > 1) {
            // create a value out of that.
            StringBuilder sb = new StringBuilder();
            boolean addSpace = false;
            for (CssIdent id : values) {
                if (addSpace) {
                    sb.append(' ');
                } else {
                    addSpace = true;
                }
                sb.append(id);
            }
            ac.getFrame().addWarning("with-space", 1);
            val = new CssIdent(sb.toString());
        } else {
            val = values.get(0);
            // could be done in the consistency check, but...
            if (null != getGenericVoiceName(val)) {
                hasGenericVoiceFamily = true;
            }
            if (inherit.equals(val)) {
                val = inherit;
            }
        }
        curval.add(val);
    }

    // final consistency check
    private void checkValues(ApplContext ac, ArrayList<CssValue> values)
            throws InvalidParamException {
        // we need to check that we don't have 'inherit' in multiple values
        if (values.size() > 1) {
            for (CssValue val : values) {
                if (inherit.equals(val)) {
                    throw new InvalidParamException("unrecognize", ac);
                }
            }
        }
        if (inherit != value && !hasGenericVoiceFamily) {
            ac.getFrame().addWarning("no-generic-family", getPropertyName());
        }
    }
}

