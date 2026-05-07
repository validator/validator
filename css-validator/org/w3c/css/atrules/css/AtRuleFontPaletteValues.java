// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

public class AtRuleFontPaletteValues extends AtRule {
    String name = null;

    /**
     * Create a new AtRuleFontPaletteValues
     */
    public AtRuleFontPaletteValues() {
    }

    public AtRuleFontPaletteValues(String name) {
        this.name = name;
    }

    /**
     * Returns the at rule keyword
     */
    public String keyword() {
        return "font-palette-values";
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRuleFontPaletteValues);
    }

    /**
     * Return true if other is an instance of AtRUleFontPaletteValues
     */
    public boolean equals(Object other) {
        return (other instanceof AtRuleFontPaletteValues);
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return (atRule instanceof AtRuleFontPaletteValues);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(keyword());
        if (name !=null ) {
            sb.append(' ').append(name);
        }
        return sb.toString();
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isEmpty() {
        return false;
    }
}
