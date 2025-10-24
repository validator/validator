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
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2012/CR-css3-images-20120417/#image-resolution
 */
public class CssImageResolution extends org.w3c.css.properties.css.CssImageResolution {

    public static final CssIdent fromImage = CssIdent.getIdent("from-image");
    public static final CssIdent snap = CssIdent.getIdent("snap");

    /**
     * Create a new CssImageResolution
     */
    public CssImageResolution() {
        value = initial;
    }

    /**
     * Creates a new CssImageResolution
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssImageResolution(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;
        CssValue snapVal = null;
        CssValue fromVal = null;
        CssValue resVal = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_RESOLUTION:
                    if (resVal != null) {
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                    resVal = val;
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value", inherit,
                                    getPropertyName(), ac);
                        }
                        value = inherit;
                        break;
                    }
                    if (fromVal == null) {
                        if (fromImage.equals(val)) {
                            fromVal = fromImage;
                            break;
                        }
                    }
                    if (snapVal == null) {
                        if (snap.equals(val)) {
                            if ((fromVal != null || resVal != null) && (expression.getRemainingCount() > 1)) {
                                // snap can't be between from-image and resolution
                                throw new InvalidParamException("value", val.toString(),
                                        getPropertyName(), ac);
                            }
                        }
                        snapVal = snap;
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (value != inherit) {
            // some checks...
            // snap can't be there alone.
            if (snapVal != null && resVal == null && fromVal == null) {
                throw new InvalidParamException("few-value", getPropertyName(), ac);
            }
            ArrayList<CssValue> v = new ArrayList<CssValue>();
            if (fromVal != null) {
                v.add(fromVal);
            }
            if (resVal != null) {
                v.add(resVal);
            }
            if (snapVal != null) {
                v.add(snapVal);
            }
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
        }
    }

    public CssImageResolution(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

