//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.HashMap;

/**
 * @version $Revision$
 */
public class CssIdent extends CssValue implements Comparable<CssIdent> {

    public static HashMap<String, CssIdent> cachedValues;

    public static CssIdent[] css_wide;

    static {
        cachedValues = new HashMap<String, CssIdent>();
        cachedValues.put("none", new CssIdent("none"));
        String[] _css_wide_ids = {"inherit", "initial", "unset", "revert"};

        css_wide = new CssIdent[_css_wide_ids.length];
        int i = 0;
        for (String s : _css_wide_ids) {
            css_wide[i++] = CssIdent.getIdent(s);
        }
    }

    public static boolean isCssWide(CssIdent cssIdent) {
        for (CssIdent id : css_wide) {
            if (id.equals(cssIdent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a cached CssIdent, useful for common values like "inherit"
     *
     * @param name, the ident name
     * @return a CssIdent
     */
    public static CssIdent getIdent(String name) {
        CssIdent val = cachedValues.get(name);
        if (val != null) {
            return val;
        }
        val = new CssIdent(name);
        cachedValues.put(name, val);
        return val;
    }

    public static final int type = CssTypes.CSS_IDENT;

    public int compareTo(CssIdent other) {
        int hash, ohash;
        hash = hashCode();
        ohash = other.hashCode();
        if (hash == ohash) {
            return 0;
        }
        return (hash < ohash) ? 1 : -1;
    }

    private int hashcode = 0;

    public final int getType() {
        return type;
    }

    /**
     * Create a new CssIdent
     */
    public CssIdent() {
    }

    /**
     * Create a new CssIdent
     *
     * @param s The identificator
     */
    public CssIdent(String s) {
        value = s;
    }

    /**
     * Set the value of this ident.
     *
     * @param s  the string representation of the identificator.
     * @param ac For errors and warnings reports.
     */
    public void set(String s, ApplContext ac) {
        value = s;
        hashcode = 0;
    }

    /**
     * Returns the internal value.
     */
    public Object get() {
        return value;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value;
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return ((value instanceof CssIdent) && (value.hashCode() == hashCode()));
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     * @return true is the two values are matching
     */
    public boolean equals(CssIdent value) {
        return (value.hashCode() == hashCode());
    }

    /**
     * Returns a hashcode for this ident.
     */
    public int hashCode() {
        // we cache, as we use toLowerCase and don't store the resulting string
        if (hashcode == 0) {
            hashcode = value.toLowerCase().hashCode();
        }
        return hashcode;
    }

    /**
     * Does this value contain a "\9" CSS declaration hack?
     */
    public boolean hasBackslash9Hack() {
        // TODO check that the comparison is right if the c18n of ident changes
        return value.endsWith("\\000009");
    }

    @Override
    public CssIdent getIdent() throws InvalidParamException {
        return this;
    }

    private String value;
}
