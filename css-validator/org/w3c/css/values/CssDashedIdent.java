//
// @author Yves Lafon
//
// (c) COPYRIGHT W3C 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

public class CssDashedIdent extends CssValue implements Comparable<CssDashedIdent> {

    /**
     * Get a cached CssIdent, useful for common values like "inherit"
     *
     * @param name, the ident name
     * @return a CssIdent
     */


    public static final int type = CssTypes.CSS_DASHED_IDENT;

    public int compareTo(CssDashedIdent other) {
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
     * Create a new CssDashedIdent
     */
    public CssDashedIdent() {
    }

    /**
     * Create a new CssIdent
     *
     * @param s The identificator
     */
    public CssDashedIdent(String s) {
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
        return ((value instanceof CssDashedIdent) && (value.hashCode() == hashCode()));
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     * @return true is the two values are matching
     */
    public boolean equals(CssDashedIdent value) {
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
        return value.endsWith("\\9");
    }

    private String value;

    public CssDashedIdent getDashedHashIdent() throws InvalidParamException {
        return this;
    }
}
