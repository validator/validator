// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.selectors.PseudoFunctionSelector;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.MissingResourceException;

/**
 * PseudoFunctionLang<br />
 * Created: Sep 2, 2005 4:24:48 PM<br />
 */
public class PseudoFunctionLang extends PseudoFunctionSelector {

    public PseudoFunctionLang(String name, CssExpression exp, ApplContext ac)
            throws InvalidParamException {
        setName(name);
        setParam(parseLang(ac, exp, functionName()));
    }

    /**
     * verify a language tag per BCP47
     *
     * @param ac     the ApplContext
     * @param exp    the CssExpression containing the value
     * @param caller the property/selector/context calling for verification
     * @throws InvalidParamException if invalid
     */
    public static final CssValue parseLang(ApplContext ac, CssExpression exp, String caller)
            throws InvalidParamException {
        String lang;
        if (exp.getCount() != 1) {
            throw new InvalidParamException("unrecognize", caller, ac);
        }
        CssValue val = exp.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                lang = val.getIdent().toString();
                break;
            case CssTypes.CSS_STRING:
                lang = val.getString().toString();
                if (lang.charAt(0) == '"' || lang.charAt(0) == '\'') {
                    lang = lang.substring(1, lang.lastIndexOf(lang.charAt(0)));
                }
                break;
            default:
                throw new InvalidParamException("value", val.toString(), caller, ac);
        }

        try {
            // FIXME validate ranges and not only TAGS.
            if (lang.contains("*")) {
                return val;
            }
            // use Locale builder parsing to check BCP 47 values
            Builder builder = new Builder();
            builder.setLanguageTag(lang);
            Locale l = builder.build();
            lang = l.getISO3Language();
            return val;
        } catch (MissingResourceException|IllformedLocaleException ex) {
            throw new InvalidParamException("value", lang, caller, ac);
        }
    }

}
