//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang University, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.Arrays;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-lists-3-20201117/#propdef-list-style-type
 * @spec https://www.w3.org/TR/2021/CR-css-counter-styles-3-20210727/#typedef-counter-style
 */
public class CssListStyleType extends org.w3c.css.properties.css.CssListStyleType {

    public static final CssIdent[] allowed_values;

    public static CssIdent[] symbols_type;

    static {
        String[] _allowed_values = {"none", "decimal", "decimal-leading-zero",
                "arabic-indic", "armenian", "upper-armenian", "lower-armenian",
                "bengali", "cambodian", "khmer", "cjk-decimal", "devanagari",
                "georgian", "gujarati", "gurmukhi", "hebrew", "kannada", "lao",
                "malayalam", "mongolian", "myanmar", "oriya", "persian",
                "lower-roman", "upper-roman", "tamil", "telugu", "thai",
                "tibetan", "lower-alpha", "lower-latin", "upper-alpha",
                "upper-latin", "cjk-earthly-branch", "cjk-heavenly-stem",
                "lower-greek", "hiragana", "hiragana-iroha", "katakana",
                "katakana-iroha", "disc", "circle", "square", "disclosure-open",
                "disclosure-closed", "cjk-earthly-branch", "cjk-heavenly-stem",
                "japanese-informal", "japanese-formal",
                "korean-hangul-formal", "korean-hanja-informal", "cjk-ideographic",
                "korean-hanja-formal", "simp-chinese-informal", "simp-chinese-formal",
                "trad-chinese-informal", "trad-chinese-formal", "ethiopic-numeric"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        Arrays.sort(allowed_values);

        String[] _allowed_symbols_type_values = {"cyclic", "numeric",
                "alphabetic", "symbolic", "fixed"};
        symbols_type = new CssIdent[_allowed_symbols_type_values.length];
        i = 0;
        for (String s : _allowed_symbols_type_values) {
            symbols_type[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        int idx = Arrays.binarySearch(allowed_values, ident);
        return (idx < 0) ? null : allowed_values[idx];
    }

    public static CssIdent getSymbolsFunctionAllowedIdent(CssIdent ident) {
        for (CssIdent id : symbols_type) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssListStyleType
     */
    public CssListStyleType() {
        value = initial;
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssListStyleType(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssListStyleType(ApplContext ac, CssExpression expression,
                            boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_FUNCTION:
                value = parseSymbolsFunction(ac, val, this);
                break;
            case CssTypes.CSS_STRING:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (none.equals(id)) {
                    value = val;
                    break;
                }
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    break;
                }
                if (getAllowedIdent(id) == null) {
                    // it's still acceptable
                    // but the name should be listed in a
                    // @counter-style rule
                    // FIXME TODO check or add a warning
                    value = val;
                }
                value = val;
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    /**
     * @spec https://www.w3.org/TR/2015/CR-css-counter-styles-3-20150611/#funcdef-symbols
     */
    protected static CssFunction parseSymbolsFunction(ApplContext ac, CssValue value,
                                                      CssProperty caller)
            throws InvalidParamException {
        CssFunction function = value.getFunction();
        CssExpression exp = function.getParameters();
        CssValue val;
        char op;
        boolean got_symbol = false;

        if (!"symbols".equals(function.getName())) {
            throw new InvalidParamException("unrecognize", ac);
        }
        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (got_symbol) {
                        throw new InvalidParamException("value", val,
                                caller.getPropertyName(), ac);
                    }
                    if (getSymbolsFunctionAllowedIdent(val.getIdent()) == null) {
                        throw new InvalidParamException("value", val,
                                caller.getPropertyName(), ac);
                    }
                    // we are not storing values
                    break;
                case CssTypes.CSS_URL:
                case CssTypes.CSS_IMAGE:
                case CssTypes.CSS_STRING:
                    got_symbol = true;
                    break;
                default:
                    throw new InvalidParamException("value", val,
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            exp.next();
        }
        if (!got_symbol) {
            throw new InvalidParamException("unrecognize", ac);
        }
        return function;
    }

}
