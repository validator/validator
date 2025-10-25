//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.parser.Errors;
import org.w3c.css.properties.css.CssCustomProperty;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warnings;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class contains a style sheet with all rules, errors and warnings.
 *
 * @version $Revision$
 */
public class StyleSheet {

    private CssCascadingOrder cascading;
    private HashMap<String, CssSelectors> rules;
    private Errors errors;
    private Warnings warnings;
    private String type;
    private ArrayList<CssRuleList> atRuleList;
    private boolean doNotAddRule;
    private boolean doNotAddAtRule;
    private static final boolean debug = false;
    private HashMap<String, CssCustomProperty> customProperties;

    /**
     * Create a new StyleSheet.
     */
    public StyleSheet() {
        rules = new HashMap<>();
        errors = new Errors();
        warnings = new Warnings();
        cascading = new CssCascadingOrder();
        atRuleList = new ArrayList<>();
        customProperties = new HashMap<>();
    }

    public void setWarningLevel(int warningLevel) {
        warnings.setWarningLevel(warningLevel);
    }

    /**
     * Get a style in a specific context.
     * No resolution are perfomed when this function is called
     *
     * @param context The context for the style
     * @return The style for the specific context.
     */
    public CssStyle getStyle(CssSelectors context) {
        if (debug) {
            Util.verbose("StyleSheet.getStyle(" + context + ')');
        }
        if (getContext(context) != null) {
            CssSelectors realContext = (CssSelectors) getContext(context);
            CssStyle style = realContext.getStyle();
            style.setStyleSheet(this);
            style.setSelector(realContext);
            return style;
        } else {
            rules.put(context.toString(), context);
            context.getStyle().setStyleSheet(this);
            context.getStyle().setSelector(context);
            return context.getStyle();
        }

    }

    /**
     * Add a property to this style sheet.
     *
     * @param selector The context where the property is defined
     * @param property The property to add
     */
    public void addProperty(CssSelectors selector, CssProperty property) {
        if (debug) {
            Util.verbose("add property "
                    + getContext(selector)
                    + " " + property);
        }
        getContext(selector).addProperty(property, warnings);
    }

    /**
     * lookup a custom property
     * @param s, the name of the property
     * @return a CssCustomProperty or null if not found
     */
    public CssCustomProperty getCustomProperty(String s) {
        return customProperties.get(s);
    }

    // we are not adding custom property in addProperty, as we want to be
    public CssCustomProperty addCustomProperty(String s, CssCustomProperty p, boolean force) {
        if (force) {
            return customProperties.put(s, p);
        }
        return customProperties.putIfAbsent(s, p);
    }

    public void remove(CssSelectors selector) {
        rules.remove(selector);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        if (type == null) {
            return "text/css";
        } else {
            return type;
        }
    }

    /**
     * Add some errors to this style.
     *
     * @param errors Some errors.
     */
    public void addErrors(Errors errors) {
        if (errors.getErrorCount() != 0) {
            getErrors().addErrors(errors);
        }
    }

    /**
     * Add some warnings to this style.
     *
     * @param warnings Some warnings.
     */
    public void addWarnings(Warnings warnings) {
        if (warnings.getWarningCount() != 0)
            getWarnings().addWarnings(warnings);
    }

    /**
     * Returns all errors.
     */
    public final Errors getErrors() {
        return errors;
    }

    /**
     * Returns all warnings.
     */
    public final Warnings getWarnings() {
        return warnings;
    }

    /**
     * Returns all rules
     */
    public final HashMap<String, CssSelectors> getRules() {
        return rules;
    }

    /**
     * Returns the property for a context.
     *
     * @param property The default value returned if there is no property.
     * @param style    The current style sheet where we can find all properties
     * @param selector The current context
     * @return the property with the right value
     */
    public final CssProperty CascadingOrder(CssProperty property,
                                            StyleSheet style,
                                            CssSelectors selector) {
        return cascading.order(property, style, selector);
    }

    /**
     * Find all conflicts for this style sheet.
     */
    public void findConflicts(ApplContext ac) {
        HashMap<String, CssSelectors> rules = getRules();
        CssSelectors[] all = new CssSelectors[rules.size()];
        all = rules.values().toArray(all);
        Arrays.sort(all);

        for (CssSelectors selector : all) {
            selector.markAsFinal();
        }
        for (CssSelectors selector : all) {
            selector.findConflicts(ac, warnings, all);
        }
    }

    /**
     * Returns the unique context for a context
     *
     * @param selector the context to find.
     */
    protected CssSelectors getContext(CssSelectors selector) {
        if (rules.containsKey(selector.toString())) {
            return rules.get(selector.toString());
        } else {
            if (selector.getNext() != null) {
                CssSelectors next = getContext(selector.getNext());
                selector.setNext(next);
            }
            rules.put(selector.toString(), selector);
            return selector;
        }
    }

    //part added by Sijtsche de Jong

    public void addCharSet(String charset) {
        this.charset = charset;
    }

    public void newAtRule(AtRule atRule) {
        CssRuleList rulelist = new CssRuleList();
        rulelist.addAtRule(atRule);
        atRuleList.add(rulelist);
        indent += "   ";
    }

    public void endOfAtRule() {
        if (!doNotAddAtRule) {
            CssRuleList rulelist = new CssRuleList();
            atRuleList.add(rulelist); //for the new set of rules
        }
        important = false;
        selectortext = "";
        if (indent.length() >= 3) {
            indent = indent.substring(3);
        } else {
            // raise a warning? This should never happen.
        }
        doNotAddAtRule = false;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public void setSelectorList(ArrayList<CssSelectors> selectors) {
        StringBuilder sb = new StringBuilder();
        for (CssSelectors s : selectors) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.toString());
        }
        selectortext = sb.toString();
    }

    public void setProperty(ArrayList<CssProperty> properties) {
        this.properties = properties;
    }

    public void endOfRule() {
        CssRuleList rulelist;
        if (!doNotAddRule) {
            CssStyleRule stylerule = new CssStyleRule(indent, selectortext,
                    properties, important);
            if (!atRuleList.isEmpty()) {
                rulelist = atRuleList.remove(atRuleList.size() - 1);
            } else {
                rulelist = new CssRuleList();
            }
            rulelist.addStyleRule(stylerule);
            atRuleList.add(rulelist);
        }
        selectortext = "";
        doNotAddRule = false;
    }

    public void removeThisRule() {
        doNotAddRule = true;
    }

    public void removeThisAtRule() {
        doNotAddAtRule = true;
    }

    public ArrayList<CssRuleList> newGetRules() {
        return atRuleList;
    }

    String selectortext;
    boolean important;
    ArrayList<CssProperty> properties;
    String indent = new String();
    public String charset;
}
