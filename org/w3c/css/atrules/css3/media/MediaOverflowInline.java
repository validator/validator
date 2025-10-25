//
//
// (c) COPYRIGHT MIT, ECRIM, Keio, Beihang 2019
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
 * @spec https://www.w3.org/TR/2020/WD-mediaqueries-5-20200731/#descdef-media-overflow-inline
 */
public class MediaOverflowInline extends MediaFeature {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"none", "scroll"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new MediaOverflowinline
     */
    public MediaOverflowInline() {
    }

    /**
     * Create a new MediaOverflowInline.
     *
     * @param expression The expression for this media feature
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public MediaOverflowInline(ApplContext ac, String modifier,
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
                    if (getAllowedValue(val.getIdent()) != null) {
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

    public MediaOverflowInline(ApplContext ac, String modifier, CssExpression expression)
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
        return "overflow-inline";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaOverflowInline ms = (MediaOverflowInline) other;
            return (((value == null) && (ms.value == null)) || ((value != null) && value.equals(ms.value)));
        } catch (ClassCastException cce) {
            return false;
        }

    }
}
