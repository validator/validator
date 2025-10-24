// $Id$
// Author: Sijtsche de Jong
// (c) COPYRIGHT MIT, ERCIM and Keio, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.util.Messages;

import java.util.ArrayList;

public class CssRuleList {

    AtRule atRule;
    ArrayList<CssStyleRule> rulelist;
    public String pseudopage;
    String indent;

    public CssRuleList() {
        atRule = null;
        rulelist = new ArrayList<CssStyleRule>();
        indent = new String();
    }

    public void addStyleRule(CssStyleRule stylerule) {
        rulelist.add(stylerule);
    }

    public ArrayList<CssStyleRule> getStyleRules() {
        return rulelist;
    }

    public void addAtRule(AtRule atRule) {
        this.atRule = atRule;
    }

    public String getAtRule() {
        return (atRule != null) ? atRule.toString() : "";
    }
    
    public AtRule getAtRuleObject() {
        return atRule;
    }

    public String getAtRuleEscaped() {
        return Messages.escapeString(atRule.toString());
    }

    public boolean isEmpty() {
        return atRule.isEmpty() /*&& rulelist.isEmpty() */;
    }

    public String toString() {
        String atRuleString = atRule.toString();
        StringBuilder ret = new StringBuilder();
        if (null != atRule && atRule.isEmpty()) {
            if (atRuleString.length() != 0) {
                ret.append(atRuleString);
                ret.append("\n\n");
            }
        } else {
            if (atRuleString.length() != 0) {
                ret.append(atRuleString);
                ret.append(" {\n\n");
            }
            for (CssStyleRule styleRule : rulelist) {
                ret.append(styleRule);
            }
            if (atRuleString.length() != 0) {
                ret.append("}\n");
            }
        }
        return ret.toString();
    }

}
