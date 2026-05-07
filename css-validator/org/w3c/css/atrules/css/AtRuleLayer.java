//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

import java.util.ArrayList;

public class AtRuleLayer extends AtRule {

    String name = null;
    ArrayList<String> layernames = null;

    public String keyword() {
        return "layer";
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return false;
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addLayerName(String name) {
        if (layernames == null) {
            layernames = new ArrayList<>();
        }
        layernames.add(name);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append('@');
        ret.append(keyword());
        ret.append(' ');
        if (name != null) {
            ret.append(name);
        } else if (layernames != null) {
            boolean first = true;
            for (String layer_name : layernames) {
                if (!first) {
                    ret.append(", ");
                } else {
                    first = false;
                }
                ret.append(layer_name);
            }
        }
        return ret.toString();
    }

    public String getNameString() {
        StringBuilder ret = new StringBuilder();
        if (name != null) {
            return "layer";
        } else {
            ret.append("layer(").append(name).append(")");
            return ret.toString();
        }
    }

    public String lookupPrefix() {
        return "";
    }

    public AtRuleLayer(String name) {
        this.name = name;
    }

    public AtRuleLayer() {
    }
}

