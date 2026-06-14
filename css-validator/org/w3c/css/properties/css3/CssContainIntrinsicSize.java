//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-4-20210520/#intrinsic-size-override
 * @spec https://drafts.csswg.org/css-sizing-4/#intrinsic-size-override may 12, 2026
 */
public class CssContainIntrinsicSize extends org.w3c.css.properties.css.CssContainIntrinsicSize {

    private static CssIdent auto, none;

    private CssContainIntrinsicWidth widthValue;
    private CssContainIntrinsicHeight heightValue;

    static {
        auto = CssIdent.getIdent("auto");
        none = CssIdent.getIdent("none");
    }

    /**
     * Create a new CssContainIntrinsicSize
     */
    public CssContainIntrinsicSize() {
        value = initial;
    }

    /**
     * Creates a new CssContainIntrinsicSize
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssContainIntrinsicSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        boolean got_auto = false;
        boolean got_length = false;
        boolean got_width = false;
        CssValue val;
        char op;

        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> v = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                    if (got_width && got_length) {
                        throw new InvalidParamException("value", val, getPropertyName(), ac);
                    }
                    if (got_length) {
                        // second length, we dump width and will parse height
                        widthValue = new CssContainIntrinsicWidth();
                        widthValue.value = value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
                        v = new ArrayList<>();
                        got_width = true;
                        got_auto = false; // not really needed, but let the state be correct
                    }
                    val.getCheckableValue().checkPositiveness(ac, this);
                    v.add(val);
                    got_length = true;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    if (auto.equals(id)) {
                        if (!got_width) {
                            if (got_length) {
                                // second length, we dump width and will parse height
                                widthValue = new CssContainIntrinsicWidth();
                                widthValue.value = value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
                                v = new ArrayList<>();
                                v.add(val);
                                got_width = true;
                                got_length = false;
                                got_auto = true;
                                break;
                            } else {
                                if (!got_auto) {
                                    v.add(val);
                                    got_auto = true;
                                    break;
                                }
                                // else fail in  default:
                            }
                        } else {
                            if (!got_auto) {
                                v.add(val);
                                got_auto = true;
                                break;
                            }
                            // else fail in  default:
                        }
                    }
                    if (none.equals(id)) {
                        if (got_length) {
                            // second length, we dump width and will parse height
                            widthValue = new CssContainIntrinsicWidth();
                            widthValue.value = value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
                            v = new ArrayList<>();
                            got_width = true;
                            got_auto = false; // not really needed, but let the state be correct
                        }
                        v.add(val);
                        got_length = true;
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", val,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        if (got_width) {
            heightValue = new CssContainIntrinsicHeight();
            heightValue.value = value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
            CssValueList vl = new CssValueList();
            vl.add(widthValue.value);
            vl.add(heightValue.value);
            value = vl;
        } else {
            // we got only one value
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
            heightValue = new CssContainIntrinsicHeight();
            heightValue.value = value;
            widthValue = new CssContainIntrinsicWidth();
            widthValue.value = value;
        }
    }

    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        Css3Style s3 = (Css3Style) style;
        if (widthValue != null) {
            if (s3.cssContainIntrinsicWidth != null) {
                style.addRedefinitionWarning(ac, this);
            }
            s3.cssContainIntrinsicWidth = widthValue;
        }
        if (heightValue != null) {
            if (s3.cssContainIntrinsicHeight != null) {
                style.addRedefinitionWarning(ac, this);
            }
            s3.cssContainIntrinsicHeight = heightValue;
        }
    }

    public static CssValue parseContainIntrinsic(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {

        boolean got_auto = false;
        boolean got_length = false;
        CssValue val;
        char op;

        if (expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> v = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                    if (got_length) {
                        throw new InvalidParamException("value", val, caller.getPropertyName(), ac);
                    }
                    val.getCheckableValue().checkPositiveness(ac, caller);
                    v.add(val);
                    got_length = true;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    caller.getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    // auto can only be first, if present
                    if (!got_auto && !got_length && auto.equals(id)) {
                        v.add(val);
                        got_auto = true;
                        break;
                    }
                    if (!got_length && none.equals(id)) {
                        v.add(val);
                        got_length = true;
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", val,
                        caller.getPropertyName(), ac);
            }
            expression.next();
        }
        // sanity check, we can't have only auto
        if (!got_length) {
            throw new InvalidParamException("value", v.get(0),
                    caller.getPropertyName(), ac);
        }
        return (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssContainIntrinsicSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

