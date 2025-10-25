// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * Attribute<br />
 * Created: Sep 1, 2005 3:39:15 PM<br />
 */
public abstract class AttributeSelector implements Selector {
    private String prefix;
    private String name;
    private String modifier;
    private String _prefixed_name = null;
    private String _ending_string = null;

    static final String[] allowed_modifier = {"i", "s"};

    static final boolean isValidModifier(String modifier) {
        for (String s : allowed_modifier) {
            if (s.equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new empty attribute selector
     */
    public AttributeSelector() {
    }

    /**
     * Creates a new attribute selector given its name
     *
     * @param name the name of this attribute
     */
    public AttributeSelector(String name) {
        this(name, null, null);
    }

    /**
     * Creates a new attribute selector given its name
     *
     * @param name the name of this attribute
     */
    public AttributeSelector(String name, String prefix, String modifier) {
        this.name = name;
        this.prefix = prefix;
        this.modifier = modifier;
    }

    public AttributeSelector(String name, String prefix) {
        this(name, prefix, null);
    }

    /**
     * Sets the name of this attribute selector
     *
     * @param name the name of this attribute
     */
    public void setName(String name) {
        this.name = name;
        _prefixed_name = null;
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the namespace prefix of this attribute selector
     *
     * @param prefix the name of this attribute
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        _prefixed_name = null;
    }


    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the modifier of this attribute selector
     *
     * @param modifier the name of this attribute
     */
    public void setModifier(String modifier, ApplContext ac)
            throws InvalidParamException {
        if (!isValidModifier(modifier)) {
            throw new InvalidParamException("value", modifier, getPrefixedName(), ac);
        }
        this.modifier = modifier;
    }


    public String getModifier() {
        return modifier;
    }

    public abstract void applyAttribute(ApplContext ac, AttributeSelector attr);

    public String getPrefixedName() {
        if (_prefixed_name == null) {
            if (prefix == null) {
                _prefixed_name = name;
            } else {
                StringBuilder sb = new StringBuilder();
                _prefixed_name = sb.append(prefix).append('|').append(name).toString();
            }
        }
        return _prefixed_name;
    }

    public String getEndingString() {
        if (_ending_string == null) {
            if (modifier == null) {
                _ending_string = "]";
            } else {
                StringBuilder sb = new StringBuilder();
                _ending_string = sb.append(" ").append(modifier).append(']').toString();
            }
        }
        return _ending_string;
    }

    /**
     * @see Selector#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(getPrefixedName()).append(getEndingString());
        return sb.toString();
    }
}
