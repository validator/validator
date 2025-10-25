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
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec http://www.w3.org/TR/2012/WD-css3-text-20120814/#text-decoration-skip0
 */
public class CssTextDecorationSkip extends org.w3c.css.properties.css.CssTextDecorationSkip {
    // objects || spaces || ink || edges
    public static final CssIdent edges, spaces, ink, objects;

    static {
        edges = CssIdent.getIdent("edges");
        spaces = CssIdent.getIdent("spaces");
        ink = CssIdent.getIdent("ink");
        objects = CssIdent.getIdent("objects");
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        if (edges.equals(ident)) {
            return edges;
        }
        if (spaces.equals(ident)) {
            return spaces;
        }
        if (ink.equals(ident)) {
            return ink;
        }
        if (objects.equals(ident)) {
            return objects;
        }
        return null;
    }


    /**
     * Create a new CssTextDecorationSkip
     */
    public CssTextDecorationSkip() {
        value = initial;
    }

    /**
     * Creates a new CssTextDecorationSkip
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextDecorationSkip(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue edgValue = null;
        CssValue spaValue = null;
        CssValue inkValue = null;
        CssValue objValue = null;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }

        CssIdent ident = val.getIdent();
        if (CssIdent.isCssWide(ident) || none.equals(ident)) {
            value = val;
            if (check && expression.getCount() != 1) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
        } else {
            int nbgot = 0;
            do {
                if (edgValue == null && edges.equals(ident)) {
                    edgValue = val;
                } else if (spaValue == null && spaces.equals(ident)) {
                    spaValue = val;
                } else if (inkValue == null && ink.equals(ident)) {
                    inkValue = val;
                } else if (objValue == null && objects.equals(ident)) {
                    objValue = val;
                } else {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                nbgot++;
                if (expression.getRemainingCount() == 1 || (!check && nbgot == 4)) {
                    // if we have both, exit
                    // (needed only if check == false...
                    break;
                }
                if (op != CssOperator.SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
                val = expression.getValue();
                op = expression.getOperator();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                ident = val.getIdent();
            } while (!expression.end());
            // now construct the value
            ArrayList<CssValue> v = new ArrayList<CssValue>(nbgot);
            if (edgValue != null) {
                v.add(edgValue);
            }
            if (spaValue != null) {
                v.add(spaValue);
            }
            if (inkValue != null) {
                v.add(inkValue);
            }
            if (objValue != null) {
                v.add(objValue);
            }
            value = (nbgot > 1) ? new CssValueList(v) : v.get(0);
        }
        expression.next();
    }

    public CssTextDecorationSkip(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

