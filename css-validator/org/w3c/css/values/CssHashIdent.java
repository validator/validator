//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @version $Revision$
 */
public class CssHashIdent extends CssValue implements Comparable<CssHashIdent> {

    /**
     * Get a cached CssIdent, useful for common values like "inherit"
     *
     * @param name, the ident name
     * @return a CssIdent
     */


    public static final int type = CssTypes.CSS_HASH_IDENT;

    public int compareTo(CssHashIdent other) {
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
    public CssHashIdent() {
    }

    /**
     * Create a new CssIdent
     *
     * @param s The identificator
     */
    public CssHashIdent(String s) {
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
        return ((value instanceof CssHashIdent) && (value.hashCode() == hashCode()));
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     * @return true is the two values are matching
     */
    public boolean equals(CssHashIdent value) {
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

    @Override
    public CssHashIdent getHashIdent() throws InvalidParamException {
        return this;
    }
}
