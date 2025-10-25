// Initial Author: Chris Rebert <css.validator@chrisrebert.com>
//
// (c) COPYRIGHT World Wide Web Consortium (MIT, ERCIM, Keio University, and Beihang University), 2015.
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
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2018/WD-filter-effects-1-20181218/#propdef-filter
 * @spec http://www.w3.org/TR/SVG/filters.html#FilterProperty
 * @see <https://msdn.microsoft.com/library/ms530752%28v=vs.85%29.aspx>
 * @see <http://davidwalsh.name/css-image-filters-internet-explorer>
 */
public class CssFilter extends org.w3c.css.properties.css.CssFilter {

    // defined function
    static final String blur = "blur";
    static final String brightness = "brightness";
    static final String contrast = "contrast";
    static final String drop_shadow = "drop-shadow";
    static final String grayscale = "grayscale";
    static final String hue_rotate = "hue-rotate";
    static final String invert = "invert";
    static final String opacity = "opacity";
    static final String sepia = "sepia";
    static final String saturate = "saturate";

    public static final CssIdent[] legacy_ie_idents;

    static {
        String[] _allowed_values = {"gray", "fliph", "flipv", "xray"};
        legacy_ie_idents = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            legacy_ie_idents[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getLegacyIEIdent(CssIdent ident) {
        for (CssIdent id : legacy_ie_idents) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssFilter
     */
    public CssFilter() {
        value = initial;
    }

    public CssFilter(ApplContext ac, CssExpression expression) throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Emit warnings instead of errors for legacy proprietary IE filters
     */
    private static boolean allowLegacyIEValues(ApplContext ac) {
        return ac.getTreatVendorExtensionsAsWarnings();
    }

    /**
     * Create a new CssFilter
     *
     * @param expression The expressions for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFilter(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        value = parseFilter(ac, expression, check, getPropertyName());
    }

    public static CssValue parseFilter(ApplContext ac, CssExpression expression,
                                       boolean check, String caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val, value = null;
        boolean singleVal = false;

        while (!expression.end()) {
            val = expression.getValue();

            switch (val.getType()) {
                case CssTypes.CSS_URL:
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    parseFunctionValues(ac, val, caller);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        value = val;
                        singleVal = true;
                        break;
                    }
                    if (none.equals(ident)) {
                        value = val;
                        singleVal = true;
                        break;
                    }
                    if (allowLegacyIEValues(ac)) {
                        if (getLegacyIEIdent(ident) != null) {
                            singleVal = true;
                            ac.getFrame().addWarning("vendor-extension", expression.toStringFromStart());
                            value = val;
                            break;
                        }
                        // not found? let it flow and fail.
                    }
                default:
                    throw new InvalidParamException("value", val.toString(), caller, ac);
            }
            expression.next();
        }
        if (singleVal && values.size() > 1) {
            throw new InvalidParamException("value",
                    value.toString(),
                    caller, ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        return value;
    }

    protected static void parseFunctionValues(ApplContext ac, CssValue func, CssProperty caller)
            throws InvalidParamException {
        parseFunctionValues(ac, func, caller.getPropertyName());
    }

    protected static void parseFunctionValues(ApplContext ac, CssValue func, String caller)
            throws InvalidParamException {
        CssFunction function = func.getFunction();
        String fname = function.getName().toLowerCase();

        switch (fname) {
            case blur:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_LENGTH, caller);
                break;
            case brightness:
            case contrast:
            case grayscale:
            case invert:
            case opacity:
            case saturate:
            case sepia:
                parseAtMostOneNonNegativeNumPercent(ac, function.getParameters(), caller);
                break;
            case drop_shadow:
                parseDropShadowFunction(ac, function.getParameters(), caller);
                break;
            case hue_rotate:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_ANGLE, caller);
                break;
            default:
                // unrecognized function
                throw new InvalidParamException("value",
                        func.toString(),
                        caller, ac);
        }
    }

    // parse one value of type (CssTypes.XXX)
    private static void parseOneX(ApplContext ac, CssExpression expression,
                                  int type, String caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        val = expression.getValue();
        // special case, 0 can be a length or an angle...
        if (val.getType() == CssTypes.CSS_NUMBER) {
            if (type == CssTypes.CSS_LENGTH || type == CssTypes.CSS_ANGLE) {
                // if not zero, it will fail
                if (val.getNumber().isZero()) {
                    expression.next();
                    return;
                }
            }
        }
        if (val.getType() != type) {
            throw new InvalidParamException("value",
                    val.toString(), caller, ac);
        }
        expression.next();
    }

    // parse one value of type (CssTypes.XXX)
    private static void parseAtMostOneNonNegativeNumPercent(ApplContext ac, CssExpression expression,
                                                      String caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        // empty? -> OK
        if (expression.getCount() == 0) {
            return;
        }
        CssValue val = expression.getValue();
        char op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
                val.getCheckableValue().checkPositiveness(ac, caller);
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(), caller, ac);
        }
        if (op != SPACE) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        expression.next();

    }

    private static void parseDropShadowFunction(ApplContext ac, CssExpression expression,
                                                String caller)
            throws InvalidParamException {
        if (expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        int nb_length = 0;
        boolean got_color = false;
        CssValue val;
        char op;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                    // color should be last | no more than 3 length.
                    if (got_color || nb_length == 3) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                drop_shadow, ac);
                    }
                    nb_length++;
                    expression.next();
                    break;
                default:
                    // something else is _after_ at least 2 length
                    if (nb_length < 2) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                drop_shadow, ac);
                    }
                    try {
                        CssColor tcolor = new CssColor(ac, expression, false);
                        got_color = true;
                        // note: parsing color does expression.next();
                    } catch (Exception e) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                drop_shadow, ac);
                    }
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
    }
}
