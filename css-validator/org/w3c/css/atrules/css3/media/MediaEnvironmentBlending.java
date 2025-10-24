// $Id$
//
// (c) COPYRIGHT MIT, ECRIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3.media;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-mediaqueries-5-20200731/#descdef-media-environment-blending
 */
public class MediaEnvironmentBlending extends MediaFeature {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"opaque", "additive", "subtractive"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new MediaEnvironmentBlending
     */
    public MediaEnvironmentBlending() {
    }

    /**
     * Create a new MediaEnvironmentBlending.
     *
     * @param expression The expression for this media feature
     * @throws InvalidParamException Values are incorrect
     */
    public MediaEnvironmentBlending(ApplContext ac, String modifier,
                                    CssExpression expression, boolean check)
            throws InvalidParamException {

        if (modifier != null) {
            throw new InvalidParamException("nomodifiermedia",
                    getFeatureName(), ac);
        }

        if (expression != null) {
            if (expression.getCount() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            if (expression.getCount() == 0) {
                throw new InvalidParamException("few-value", getFeatureName(), ac);
            }
            CssValue val = expression.getValue();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (getAllowedIdent(val.getIdent()) != null) {
                        value = val;
                        break;
                    }
                    // let it flow through the exception
                default:
                    throw new InvalidParamException("value", expression.getValue(),
                            getFeatureName(), ac);
            }
        } else {
            // TODO add a warning for value less mediafeature that makes no sense
        }
    }

    public MediaEnvironmentBlending(ApplContext ac, String modifier, CssExpression expression)
            throws InvalidParamException {
        this(ac, modifier, expression, false);
    }

    // just in case someone wants to call it externally...
    public void setModifier(ApplContext ac, String modifier)
            throws InvalidParamException {
        throw new InvalidParamException("nomodifiermedia",
                getFeatureName(), ac);
    }

    /**
     * Returns the value of this media feature.
     */

    public Object get() {
        return value;
    }

    /**
     * Returns the name of this media feature.
     */
    public final String getFeatureName() {
        return "environment-blending";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaEnvironmentBlending mo = (MediaEnvironmentBlending) other;
            return (((value == null) && (mo.value == null)) || ((value != null) && value.equals(mo.value)));
        } catch (ClassCastException cce) {
            return false;
        }

    }
}
