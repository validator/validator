//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.css;

import org.w3c.css.atrules.css.AtRuleLayer;
import org.w3c.css.atrules.css.AtRuleMedia;
import org.w3c.css.atrules.css.AtRuleScope;
import org.w3c.css.atrules.css.AtRuleSupports;
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
        if (!openRuleStack.isEmpty() || (atRule instanceof AtRuleScope)) {
            // CSS Nesting: an at-rule nested in a style rule, or a @scope rule,
            // whose body can hold declarations
            openRuleStack.add(new OpenFrame(atRule));
            return;
        }
        CssRuleList rulelist = new CssRuleList();
        rulelist.addAtRule(atRule);
        atRuleList.add(rulelist);
        indent += "   ";
    }

    public void endOfAtRule() {
        if (!openRuleStack.isEmpty()
                && openRuleStack.get(openRuleStack.size() - 1).atRule != null) {
            closeAtRuleFrame();
            important = false;
            selectortext = "";
            doNotAddAtRule = false;
            return;
        }
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
            sb.append(s.toString().trim());
        }
        selectortext = sb.toString();
    }

    public void setProperty(ArrayList<CssProperty> properties) {
        if (!openRuleStack.isEmpty()) {
            // CSS Nesting: the declarations of the open rule, in order; those
            // following a nested rule stay after it (a nested declarations rule)
            OpenFrame frame = openRuleStack.get(openRuleStack.size() - 1);
            if ((properties == null) || properties.isEmpty() || (properties == frame.lastRun)) {
                return;
            }
            frame.lastRun = properties;
            CssStyleRule last = (frame.ownBody || frame.children.isEmpty()) ? null
                    : frame.children.get(frame.children.size() - 1);
            if (last == null) {
                if (frame.properties == null) {
                    frame.properties = new ArrayList<CssProperty>();
                }
                frame.properties.addAll(properties);
            } else if (last.isDeclarationsOnly()) {
                last.getProperties().addAll(properties);
            } else {
                frame.children.add(CssStyleRule.newDeclarations(indent,
                        new ArrayList<CssProperty>(properties)));
            }
            return;
        }
        this.properties = properties;
    }

    /**
     * CSS Nesting: opens an output frame for a style rule body. The rules
     * and at-rules ending while it is open become its nested rules, instead
     * of being appended to the flat rule list.
     */
    public void startStyleRule() {
        openRuleStack.add(new OpenFrame(null));
    }

    /**
     * CSS Nesting: the style rule opened by the last startStyleRule failed to
     * parse; discards its frame, and anything left open inside it.
     */
    public void abortStyleRule() {
        while (!openRuleStack.isEmpty()) {
            if (openRuleStack.remove(openRuleStack.size() - 1).atRule == null) {
                break;
            }
        }
        selectortext = "";
        doNotAddRule = false;
    }

    public void endOfRule() {
        if (!openRuleStack.isEmpty()
                && openRuleStack.get(openRuleStack.size() - 1).ownBody) {
            // the end of a block of an at-rule such as @keyframes, or of the body
            // of an at-rule such as @font-face, nested in a @scope rule
            OpenFrame frame = openRuleStack.get(openRuleStack.size() - 1);
            if (!doNotAddRule && (selectortext != null) && !selectortext.isEmpty()) {
                frame.children.add(new CssStyleRule(indent, selectortext,
                        (frame.properties != null) ? frame.properties
                                : new ArrayList<CssProperty>(), important));
                frame.properties = null;
            }
            selectortext = "";
            doNotAddRule = false;
            return;
        }
        if (!openRuleStack.isEmpty()) {
            // at-rules left open inside the rule (after an error) are closed first
            while ((openRuleStack.size() > 1)
                    && (openRuleStack.get(openRuleStack.size() - 1).atRule != null)) {
                closeAtRuleFrame();
            }
            OpenFrame frame = openRuleStack.get(openRuleStack.size() - 1);
            if (frame.atRule == null) {
                openRuleStack.remove(openRuleStack.size() - 1);
                // empty rules are not shown, as before nesting, but a rule
                // holding only nested rules is
                if (!doNotAddRule && ((frame.properties != null) || !frame.children.isEmpty())) {
                    CssStyleRule stylerule = new CssStyleRule(indent, selectortext,
                            (frame.properties != null) ? frame.properties
                                    : new ArrayList<CssProperty>(), important);
                    for (CssStyleRule child : frame.children) {
                        stylerule.addNestedRule(child);
                    }
                    attach(stylerule);
                }
                selectortext = "";
                doNotAddRule = false;
                return;
            }
        }
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

    /**
     * CSS Nesting: closes the at-rule frame on top of the stack into a node
     * of the enclosing rule.
     */
    private void closeAtRuleFrame() {
        OpenFrame frame = openRuleStack.remove(openRuleStack.size() - 1);
        if (doNotAddAtRule) {
            return;
        }
        CssStyleRule node = new CssStyleRule(indent, null,
                (frame.properties != null) ? frame.properties : new ArrayList<CssProperty>(),
                false);
        node.setAtRule(frame.atRule.toString(), frame.atRule.isEmpty());
        for (CssStyleRule child : frame.children) {
            node.addNestedRule(child);
        }
        // an empty block is not shown, a statement such as "@layer a;" is
        if (frame.atRule.isEmpty() || (frame.properties != null) || !frame.children.isEmpty()) {
            attach(node);
        }
    }

    /**
     * CSS Nesting: adds a closed rule to the enclosing frame, or to the flat
     * rule list at the top level.
     */
    private void attach(CssStyleRule rule) {
        if (!openRuleStack.isEmpty()) {
            openRuleStack.get(openRuleStack.size() - 1).children.add(rule);
        } else {
            CssRuleList rulelist;
            if (!atRuleList.isEmpty()) {
                rulelist = atRuleList.remove(atRuleList.size() - 1);
            } else {
                rulelist = new CssRuleList();
            }
            rulelist.addStyleRule(rule);
            atRuleList.add(rulelist);
        }
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
    // CSS Nesting: the style rules and at-rules open for the output, each
    // collecting, in order, its declarations and the rules nested in it
    private static final class OpenFrame {
        final AtRule atRule; // null for a style rule
        // true for an at-rule whose body is not rules and declarations, such
        // as @font-face (descriptors) or @keyframes (keyframe blocks)
        final boolean ownBody;
        ArrayList<CssProperty> properties = null;
        ArrayList<CssProperty> lastRun = null;
        final ArrayList<CssStyleRule> children = new ArrayList<CssStyleRule>();

        OpenFrame(AtRule atRule) {
            this.atRule = atRule;
            ownBody = (atRule != null) && !((atRule instanceof AtRuleMedia)
                    || (atRule instanceof AtRuleSupports) || (atRule instanceof AtRuleLayer)
                    || (atRule instanceof AtRuleScope));
        }
    }

    private final ArrayList<OpenFrame> openRuleStack = new ArrayList<OpenFrame>();
    String indent = new String();
    public String charset;
}
