// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css.supports;

import org.w3c.css.properties.css.CssProperty;

import java.util.ArrayList;

public class SupportsFeature {
    boolean and = false;
    boolean not = false;
    boolean or = false;
    CssProperty property = null;
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
        } else if (features != null) {
            for (SupportsFeature sf : features) {
                sb.append(sf);
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
