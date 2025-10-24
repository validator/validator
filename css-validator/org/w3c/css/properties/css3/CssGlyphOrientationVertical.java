//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssAngle;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.math.BigDecimal;

/**
 * @spec https://www.w3.org/TR/2019/REC-css-writing-modes-3-20191210/#propdef-glyph-orientation-vertical
 * @deprecated 
 */
@Deprecated
public class CssGlyphOrientationVertical extends org.w3c.css.properties.css.CssGlyphOrientationVertical {

    public CssIdent auto = CssIdent.getIdent("auto");
    public static CssNumber[] allowed_number_values;
    public static CssAngle[] allowed_angle_values;

    static {
        allowed_number_values = new CssNumber[]{new CssNumber(), new CssNumber()};
        allowed_number_values[0].setIntValue(0);
        allowed_number_values[1].setIntValue(90);
        allowed_angle_values = new CssAngle[]{new CssAngle(BigDecimal.ZERO),
                new CssAngle(BigDecimal.valueOf(90))};
        allowed_angle_values[0].setUnit("deg");
        allowed_angle_values[1].setUnit("deg");

    }

    public static CssNumber getAllowedNumberValue(CssCheckableValue v) {
        if (v.getRawType() == CssTypes.CSS_NUMBER) {
            for (CssNumber n : allowed_number_values) {
                if (n.equals(v)) {
                    return n;
                }
            }
        }
        return null;
    }

    public static CssAngle getAllowedAngleValue(CssCheckableValue v) {
        if (v.getRawType() == CssTypes.CSS_ANGLE) {
            for (CssAngle a : allowed_angle_values) {
                if (a.equals(v)) {
                    return a;
                }
            }
        }
        return null;
    }

    /**
     * Create a new CssGlyphOrientationVertical
     */
    public CssGlyphOrientationVertical() {
        value = initial;
    }

    // TODO FIXME add the 'text-orientation' longhand equivalent to
    // output redefinition warnings

    /**
     * Creates a new CssGlyphOrientationVertical
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssGlyphOrientationVertical(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        ac.getFrame().addWarning("deprecatedproperty", getPropertyName());

        CssValue val;
        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (val.getRawType() != CssTypes.CSS_NUMBER) {
                    ac.getFrame().addWarning("dynamic", toString());
                    value = val;
                    break;
                }
                value = getAllowedNumberValue(val.getCheckableValue());
                if (value == null) {
                    throw new InvalidParamException("value",
                            new String[]{val.toString(), getPropertyName()}, ac);
                }
                break;
            case CssTypes.CSS_ANGLE:
                if (val.getRawType() != CssTypes.CSS_ANGLE) {
                    ac.getFrame().addWarning("dynamic", toString());
                    value = val;
                    break;
                }
                value = getAllowedNumberValue(val.getCheckableValue());
                if (value == null) {
                    throw new InvalidParamException("value",
                            new String[]{val.toString(), getPropertyName()}, ac);
                }
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id) || auto.equals(id)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssGlyphOrientationVertical(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

