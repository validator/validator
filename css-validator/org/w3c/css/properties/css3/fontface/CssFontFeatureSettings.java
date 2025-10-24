//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

import static org.w3c.css.properties.css3.CssFontFeatureSettings.parseFontFeatureSettings;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-font-feature-settings
 * @see org.w3c.css.properties.css3.CssFontFeatureSettings
 */
public class CssFontFeatureSettings extends org.w3c.css.properties.css.fontface.CssFontFeatureSettings {

    /**
     * Create a new CssFontFeatureSettings
     */
    public CssFontFeatureSettings() {
        value = initial;
    }

    /**
     * Creates a new CssFontFeatureSettings
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontFeatureSettings(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = parseFontFeatureSettings(ac, expression, getPropertyName());
    }

    public CssFontFeatureSettings(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

