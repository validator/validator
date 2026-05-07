// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css.supports;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css3.fontface.CssSrc;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

public class SupportsFeature {

    // this is has to move somewhere else
    boolean and = false;
    boolean not = false;
    boolean or = false;
    CssProperty property = null;
    CssSelectors selectors = null;
    CssIdent atrule = null;
    CssValue value = null;
    ArrayList<SupportsFeature> features = null;

    public SupportsFeature() {
    }

    public SupportsFeature(CssProperty property) {
        this.property = property;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }

    public boolean getAnd() {
        return and;
    }

    public void setOr(boolean or) {
        this.or = or;
    }

    public boolean getOr() {
        return or;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public boolean getNot() {
        return not;
    }

    public void setProperty(CssProperty property) {
        this.property = property;
    }

    public CssProperty getProperty() {
        return property;
    }

    public void setSelectors(CssSelectors selectors) {
        this.selectors = selectors;
    }

    public CssSelectors getSelectors() {
        return selectors;
    }

    private void parseFontTech(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        if (expression.getCount() != 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                if (CssSrc.isSingleTech(val.getIdent())) {
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(), this, ac);
        }
    }

    private void parseFontFormat(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        if (expression.getCount() != 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_STRING:
                break;
            case CssTypes.CSS_IDENT:
                if (CssSrc.getMatchingFontFormat(val.getIdent()) != null) {
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(), this, ac);
        }
    }

    public void setValue(ApplContext ac, CssValue value)
            throws InvalidParamException {
        // TODO parse
        switch (value.getRawType()) {
            case CssTypes.CSS_FUNCTION:
                CssFunction f = value.getFunction();
                switch (f.getName()) {
                    case "font-tech":
                        parseFontTech(ac, f.getParameters());
                        break;
                    case "font-format":
                        parseFontFormat(ac, f.getParameters());
                        break;
                    default:
                        throw new InvalidParamException("value",
                                value.toString(), this, ac);
                }
                break;
            default:
                throw new InvalidParamException("value",
                        value.toString(), this, ac);
        }
        this.value = value;
    }

    public void setAtRule(CssIdent atrule) {
        this.atrule = atrule;
    }

    public CssIdent getAtRule() {
        return atrule;
    }

    public CssValue getValue() {
        return value;
    }

    public void addFeature(SupportsFeature sf) {
        if (features == null) {
            features = new ArrayList<SupportsFeature>();
        }
        features.add(sf);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean printAnd = false;

        if (and) {
            sb.append(" and ");
        } else if (not) {
            sb.append(" not ");
        } else if (or) {
            sb.append(" or ");
        }
        sb.append('(');
        if (property != null) {
            sb.append(property.getPropertyName());
            sb.append(": ");
            sb.append(property);
        } else if (selectors != null) {
            sb.append("selector(").append(selectors).append(')');
        } else if (value != null) {
            sb.append(value);
        } else if (atrule != null) {
            sb.append("at-rule(").append(atrule).append(')');
        } else if (features != null) {
            for (SupportsFeature sf : features) {
                sb.append(sf);
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
