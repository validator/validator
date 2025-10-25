//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-src
 */
public class CssSrc extends org.w3c.css.properties.css.fontface.CssSrc {

    public static final CssIdent[] allowed_font_format;
    public static final CssIdent[] allowed_font_technologies;
    public static final CssIdent[] allowed_font_feature_technologies;
    public static final CssIdent[] allowed_color_font_technologies;

    public static final CssIdent supports = CssIdent.getIdent("supports");

    static {
        int i = 0;
        String[] _allowed_font_format_values = {"woff", "truetype", "opentype",
                "woff2", "embedded-opentype", "collection", "svg"};
        allowed_font_format = new CssIdent[_allowed_font_format_values.length];
        for (String s : _allowed_font_format_values) {
            allowed_font_format[i++] = CssIdent.getIdent(s);
        }

        i = 0;
        String[] _allowed_font_technologies = {"variations", "palettes"};
        allowed_font_technologies = new CssIdent[_allowed_font_technologies.length];
        for (String s : _allowed_font_technologies) {
            allowed_font_technologies[i++] = CssIdent.getIdent(s);
        }

        i = 0;
        String[] _allowed_font_feature_technologies = {"opentype", "aat", "graphite"};
        allowed_font_feature_technologies = new CssIdent[_allowed_font_feature_technologies.length];
        for (String s : _allowed_font_feature_technologies) {
            allowed_font_feature_technologies[i++] = CssIdent.getIdent(s);
        }

        i = 0;
        String[] _allowed_color_font_technologies = {"COLRv0", "COLRv1", "SVG",
                "sbix", "CBDT"};
        allowed_color_font_technologies = new CssIdent[_allowed_color_font_technologies.length];
        for (String s : _allowed_color_font_technologies) {
            allowed_color_font_technologies[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingFontFormat(CssIdent ident) {
        for (CssIdent id : allowed_font_format) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getMatchingColorFontTechnology(CssIdent ident) {
        for (CssIdent id : allowed_color_font_technologies) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getMatchingFontTechnology(CssIdent ident) {
        for (CssIdent id : allowed_font_technologies) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getMatchingFontFeatureTechnology(CssIdent ident) {
        for (CssIdent id : allowed_font_feature_technologies) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssSrc
     */
    public CssSrc() {
        value = initial;
    }

    /**
     * Creates a new CssSrc
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssSrc(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        char op;

        CssExpression nex;
        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            nex = new CssExpression();

            while (!expression.end() && expression.getOperator() == SPACE) {
                nex.addValue(expression.getValue());
                expression.next();
            }
            // the weird ending case
            if (!expression.end()) {
                nex.addValue(expression.getValue());
            }
            values.add(parseSrcLayer(ac, nex, check));
            if (!expression.end()) {
                op = expression.getOperator();
                if (op != COMMA) {
                    throw new InvalidParamException("operator", op,
                            getPropertyName(), ac);
                }
                expression.next();
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    protected CssValue parseSrcLayer(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> values = new ArrayList<>();
        boolean gotUrl = false;
        boolean gotFormat = false;
        CssValue val;

        while (!expression.end()) {
            val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_URL:
                    if (gotUrl) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    gotUrl = true;
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    CssFunction f = val.getFunction();
                    String funcName = f.getName().toLowerCase();
                    switch (funcName) {
                        case "local":
                            if (expression.getCount() > 1) {
                                throw new InvalidParamException("unrecognize", ac);
                            }
                            parseLocalFunction(ac, f);
                            values.add(f);
                            break;
                        case "format":
                            if (gotFormat) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            parseFormatFunction(ac, f);
                            gotFormat = true;
                            values.add(val);
                            break;
                        default:
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                    }
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    protected void parseFormatFunction(ApplContext ac, CssFunction f)
            throws InvalidParamException {
        CssExpression exp = f.getParameters();
        char op;
        CssValue val;

        val = exp.getValue();
        op = exp.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_STRING:
                break;
            case CssTypes.CSS_IDENT:
                if (getMatchingFontFormat(val.getIdent()) != null) {
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        f.getName(), ac);
        }
        exp.next();

        if (!exp.end()) {
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        f.getName(), ac);
            }
            val = exp.getValue();
            op = exp.getOperator();

            // we need 'supports' ...
            if ((val.getType() != CssTypes.CSS_IDENT) || !supports.equals(val.getIdent())) {
                throw new InvalidParamException("value",
                        val.toString(),
                        f.getName(), ac);
            }
            exp.next();
            if ((op != SPACE) || exp.end()) {
                throw new InvalidParamException("operator", op,
                        f.getName(), ac);
            }
            while (!exp.end()) {
                val = exp.getValue();
                op = exp.getOperator();
                switch (val.getType()) {
                    case CssTypes.CSS_FUNCTION:
                        CssFunction subf = val.getFunction();
                        String subfname = subf.getName().toLowerCase();
                        switch (subfname) {
                            case "features":
                                parseFontFeatureTechnology(ac, subf);
                                break;
                            case "color":
                                parseColorFontTechnology(ac, subf);
                                break;
                            default:
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        f.getName(), ac);
                        }
                        break;
                    case CssTypes.CSS_IDENT:
                        CssIdent ident = val.getIdent();
                        if (getMatchingFontTechnology(ident) != null) {
                            break;
                        }
                        // unrecognized ident
                    default:
                        throw new InvalidParamException("value",
                                val.toString(),
                                f.getName(), ac);
                }
                exp.next();
                if (op != COMMA && !exp.end()) {
                    throw new InvalidParamException("operator", op,
                            f.getName(), ac);
                }
            }
        }

    }

    protected void parseFontFeatureTechnology(ApplContext ac, CssFunction f)
            throws InvalidParamException {
        CssExpression exp = f.getParameters();
        char op;
        CssValue val;

        if (exp.getCount() > 1) {
            throw new InvalidParamException("value", exp.toStringFromStart(),
                    f.getName(), ac);
        }
        val = exp.getValue();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(),
                    f.getName(), ac);
        }
        if (getMatchingFontFeatureTechnology(val.getIdent()) == null) {
            throw new InvalidParamException("value",
                    val.toString(),
                    f.getName(), ac);
        }
        exp.next();
    }

    protected void parseColorFontTechnology(ApplContext ac, CssFunction f)
            throws InvalidParamException {
        CssExpression exp = f.getParameters();
        char op;
        CssValue val;

        if (exp.getCount() > 1) {
            throw new InvalidParamException("value", exp.toStringFromStart(),
                    f.getName(), ac);
        }
        val = exp.getValue();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(),
                    f.getName(), ac);
        }
        if (getMatchingColorFontTechnology(val.getIdent()) == null) {
            throw new InvalidParamException("value",
                    val.toString(),
                    f.getName(), ac);
        }
        exp.next();
    }

    protected void parseLocalFunction(ApplContext ac, CssFunction f)
            throws InvalidParamException {
        CssExpression exp = f.getParameters();
        char op;
        CssValue val;

        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                    // quoted string must be unique
                    if (exp.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            f.getName(), ac);
            }
            // idents must be separated by spaces
            if (op != SPACE && exp.getRemainingCount() > 1) {
                throw new InvalidParamException("operator", op,
                        f.getName(), ac);
            }
            exp.next();
        }
    }


    public CssSrc(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

