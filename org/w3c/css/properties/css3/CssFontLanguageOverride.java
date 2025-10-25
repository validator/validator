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
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.external.OpenTypeLanguageSystemTag;

import java.util.Arrays;

/**
 * @spec http://www.w3.org/TR/2011/WD-css3-fonts-20111004/#propdef-font-language-override
 */
public class CssFontLanguageOverride extends org.w3c.css.properties.css.CssFontLanguageOverride {

    public static final CssIdent normal;

    static {
        normal = CssIdent.getIdent("normal");
    }

    /**
     * Create a new CssFontLanguageOverride
     */
    public CssFontLanguageOverride() {
        value = initial;
    }

    /**
     * Creates a new CssFontLanguageOverride
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontLanguageOverride(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_STRING:
                CssString s = val.getString();
                int l = s.toString().length();
                // limit of 4characters + two surrounding quotes
                if ((l < 4) || (l > 6)) {
                    throw new InvalidParamException("value",
                            expression.getValue().toString(),
                            getPropertyName(), ac);
                }
                // we extract the 2, 3 or 4 letters from the quotes...
                String tag = s.toString().substring(1, l - 1).toUpperCase();
                // align to 4
                switch (tag.length()) {
                    case 1:
                        tag = tag.concat("   ");
                        break;
                    case 2:
                        tag = tag.concat("  ");
                        break;
                    case 3:
                        tag = tag.concat(" ");
                        break;
                    default:
                }
                // valid values are specified here.
                int idx = Arrays.binarySearch(OpenTypeLanguageSystemTag.tags, tag);
                if (idx < 0) {
                    // TODO specific error code
                    throw new InvalidParamException("value",
                            expression.getValue().toString(),
                            getPropertyName(), ac);
                }
                // check if deprecated
                idx = Arrays.binarySearch(OpenTypeLanguageSystemTag.deprecated_tags, tag);
                if (idx >= 0) {
                    ac.getFrame().addWarning("deprecated", tag);
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident) || normal.equals(ident)) {
                    value = val;
                    break;
                }
                // unrecognized, let it fail
            default:
                throw new InvalidParamException("value",
                        val.toString(), getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontLanguageOverride(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

