//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variation-settings
 */
public class CssFontVariationSettings extends org.w3c.css.properties.css.CssFontVariationSettings {

    static final CssIdent normal = CssIdent.getIdent("normal");

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

    public static final CssValue parseFontVariationSettings(ApplContext ac,
                                                            CssExpression expression,
                                                            String caller)
            throws InvalidParamException {

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssValue> layervalues = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id) || normal.equals(id)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(), caller, ac);
                        }
                        return val;
                    }
                    throw new InvalidParamException("value",
                            val.toString(), caller, ac);
                case CssTypes.CSS_NUMBER:
                    if (layervalues.size() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(), caller, ac);
                    }
                    layervalues.add(val);
                    break;
                case CssTypes.CSS_STRING:
                    CssString s = val.getString();
                    int l = s.toString().length();
                    // limit of 4characters + two surrounding quotes
                    if (s.toString().length() != 6) {
                        throw new InvalidParamException("value",
                                expression.getValue().toString(),
                                caller, ac);
                    }
                    // FIXME TODO check
                    layervalues.add(val);
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(), caller, ac);
            }
            expression.next();
            if (!layervalues.isEmpty()) {
                if (layervalues.size() == 2) {
                    values.add(new CssValueList(layervalues));
                    layervalues = new ArrayList<>();
                    if (!expression.end()) {
                        if (op != COMMA) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                    }
                } else {
                    if (op != SPACE) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                }
            }
        }
        // sanity check
        if (layervalues.size() == 1) {
            throw new InvalidParamException("value",
                    layervalues.get(0).toString(),
                    caller, ac);
        }
        if (!values.isEmpty()) {
            return (values.size() == 1) ? values.get(0) : new CssLayerList(values);
        }
        return null;
    }

    public CssFontVariationSettings(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

