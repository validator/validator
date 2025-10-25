//
// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssAngle;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-azimuth
 * @deprecated
 */
@Deprecated
public class CssAzimuth extends org.w3c.css.properties.css.CssAzimuth {

    CssIdent identValue;
    CssAngle angleValue;
    boolean isBehind;

    private static int[] hash_values;

    private final static String[] azValues = {
            "left-side", "far-left", "left",
            "center-left", "center", "center-right",
            "right", "far-right", "right-side"
    };

    private static CssIdent behind;
    private static CssIdent leftwards;
    private static CssIdent rightwards;

    private static CssIdent singleValues[];

    static {
        hash_values = new int[azValues.length];
        for (int i = 0; i < azValues.length; i++) {
            hash_values[i] = azValues[i].hashCode();
        }
        behind = new CssIdent("behind");
        leftwards = new CssIdent("leftwards");
        rightwards = new CssIdent("rightwards");

        singleValues = new CssIdent[3];
        singleValues[0] = inherit;
        singleValues[1] = leftwards;
        singleValues[2] = rightwards;
    }

    /**
     * Create a new CssAzimuth
     */
    public CssAzimuth() {
        identValue = initial;
    }

    // check if the ident is in the allowed values
    // return true is the ident was found.
    private boolean checkIdent(CssIdent ident) {
        int hash = ident.hashCode();
        for (int azHash : hash_values) {
            if (azHash == hash) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new CssAzimuth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssAzimuth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();

        // same as CSS21 plus a warning
        ac.getFrame().addWarning("deprecatedproperty", getPropertyName());

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                // find out if we can create an angle out of that value
                // (ie: equal 0)
                // per http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#angles
            case CssTypes.CSS_ANGLE:
                if (check && expression.getCount() > 1) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                angleValue = val.getAngle();
                // FIXME is the following really true? not per spec...
//                if (!angleValue.isDegree()) {
//                    throw new InvalidParamException("degree", ac);
//                }
                // TODO check unit according to css level
                expression.next();
                break;
            case CssTypes.CSS_IDENT:
                int count = expression.getCount();
                CssIdent ident = val.getIdent();
                char op = expression.getOperator();

                // inherit, leftwards, rightwards
                for (CssIdent singleId : singleValues) {
                    if (singleId.equals(ident)) {
                        if ((count > 1) && check) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        identValue = singleId;
                        expression.next();
                        return;
                    }
                }
                // do it 1 or two times...
                if (behind.equals(ident)) {
                    isBehind = true;
                } else if (checkIdent(ident)) {
                    identValue = ident;
                } else {
                    throw new InvalidParamException("unrecognize", ac);
                }
                expression.next();

                if (expression.getCount() > 1) {
                    val = expression.getValue();
                    if (val.getType() != CssTypes.CSS_IDENT) {
                        throw new InvalidParamException("value", val, ac);
                    }
                    ident = val.getIdent();

                    if (op != CssOperator.SPACE) {
                        throw new InvalidParamException("operator", val,
                                getPropertyName(), ac);
                    }
                    if (behind.equals(ident)) {
                        if (isBehind) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        isBehind = true;
                        expression.next();
                    } else if (checkIdent(ident)) {
                        // the first one was not behind, so we have an issue...
                        if (!isBehind) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        identValue = ident;
                    } else {
                        // catches unknown values but also single values
                        // inherit, leftwards, rightwards
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    expression.next();
                }
                break;
            default:
                throw new InvalidParamException("value", val, ac);
        }
    }

    public CssAzimuth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        if (identValue != null) {
            return identValue;
        } else {
            return angleValue;
        }
    }


    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return inherit.equals(identValue);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (isBehind) {
            StringBuilder sb = new StringBuilder();
            sb.append(behind);
            if (identValue != null) {
                sb.append(' ').append(identValue);
            }
            return sb.toString();
        }
        if (identValue != null) {
            return identValue.toString();
        }
        return angleValue.toString();
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        CssAzimuth other;
        try {
            other = (CssAzimuth) property;
            // TODO compute a float value to do equality of angle and ident
            return ((other.isBehind == isBehind) &&
                    ((identValue != null && identValue.equals(other.identValue))
                            || (identValue == null) && (other.identValue == null)) &&
                    ((angleValue != null && angleValue.equals(other.angleValue))
                            || (angleValue == null) && (other.angleValue == null)));
        } catch (ClassCastException ex) {
            return false;
        }
    }
}

