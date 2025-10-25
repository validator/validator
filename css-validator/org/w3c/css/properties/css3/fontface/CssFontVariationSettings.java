//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

import static org.w3c.css.properties.css3.CssFontVariationSettings.parseFontVariationSettings;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-font-variation-settings
 * @see org.w3c.css.properties.css3.CssFontVariationSettings
 */
public class CssFontVariationSettings extends org.w3c.css.properties.css.fontface.CssFontVariationSettings {

    /**
     * Create a new CssFontVariationSettings
     */
    public CssFontVariationSettings() {
        value = initial;
    }

    /**
     * Creates a new CssFontVariationSettings
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontVariationSettings(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = parseFontVariationSettings(ac, expression, getPropertyName());
    }

    public CssFontVariationSettings(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

