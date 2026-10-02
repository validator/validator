//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.atrules.css.AtRuleFontFace;
import org.w3c.css.atrules.css.AtRulePage;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.selectors.AttributeSelector;
import org.w3c.css.selectors.NestingSelector;
import org.w3c.css.selectors.PseudoClassSelector;
import org.w3c.css.selectors.PseudoElementSelector;
import org.w3c.css.selectors.PseudoFactory;
import org.w3c.css.selectors.Selector;
import org.w3c.css.selectors.SelectorsList;
import org.w3c.css.selectors.TypeSelector;
import org.w3c.css.selectors.UniversalSelector;
import org.w3c.css.selectors.attributes.AttributeExact;
import org.w3c.css.selectors.combinators.ChildCombinator;
import org.w3c.css.selectors.combinators.ColumnCombinator;
import org.w3c.css.selectors.combinators.DescendantCombinator;
import org.w3c.css.selectors.combinators.NextSiblingCombinator;
import org.w3c.css.selectors.combinators.SubsequentSiblingCombinator;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionIs;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssProfile;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Messages;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warnings;
import org.w3c.css.values.CssExpression;

import java.util.ArrayList;

/**
 * This class manages all contextual selector.
 * <p/>
 * <p/>
 * Note:<BR>
 * Invoke a <code>set</code> function to change the selector clears all
 * properties !
 *
 * @version $Revision$
 */
public final class CssSelectors extends SelectorsList
        implements CssSelectorsConstant, Comparable<CssSelectors> {

    ApplContext ac;

    /**
     * At rule statement
     */
    AtRule atRule;

    /**
     * The element.
     */
    String element;

    String connector = DESCENDANT_COMBINATOR;

    /**
     * The next context.
     */
    protected CssSelectors next = null;

    // true if the element is a block-level element
    private boolean isBlock;

    CssStyle properties;

    // all hashCode (for performance)
    private int hashElement;

    //private int hashGeneral;

    // The CssStyle to use
    private static Class<?> style;

    // see isEmpty and addProperty
    private boolean Init;

    private String cachedRepresentation = null;
    private boolean isFinal = false;

    /**
     * Create a new CssSelectors with no previous selector.
     */
    public CssSelectors(ApplContext ac) {
        super(ac);
        style = ac.getCssSelectorsStyle();
        try {
            properties = (CssStyle) style.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.ac = ac;
    }

    private CssSelectors(Class<?> style) {
        super();
        CssSelectors.style = style;
        try {
            properties = (CssStyle) style.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.ac = null;
    }

    /**
     * Create a new CssSelectors with a previous selector.
     *
     * @param next the next selector
     */
    public CssSelectors(CssSelectors next) {
        this(CssSelectors.style);
        this.next = next;
    }

    /**
     * Create a new CssSelectors with a previous selector.
     *
     * @param next the next selector
     */
    public CssSelectors(ApplContext ac, CssSelectors next) {
        this(ac);
        this.next = next;
    }

    /**
     * Set the style for all contexts. Don't forget to invoke this method if you
     * want a style !
     *
     * @param style0 the style
     */
    public void setStyle(Class<?> style0) {
        Util.verbose("Style is : " + style0);
        style = style0;
    }

    /**
     * Set the attribute atRule
     *
     * @param atRule the new value for the attribute
     */
    public void setAtRule(AtRule atRule) {
        this.atRule = atRule;
    }

    /**
     * Returns the attribute atRule
     *
     * @return the value of the attribute
     */
    public final AtRule getAtRule() {
        return atRule;
    }

    /**
     * Get the element.
     */
    public final String getElement() {
        return element;
    }

    /**
     * Returns <code>true</code> if the element is a block level element (HTML
     * only)
     */
    public final boolean isBlockLevelElement() {
        return isBlock;
    }

    public void addPseudoClass(String pseudo) throws InvalidParamException {
        if (pseudo == null) {
            return;
        }

        if (ac.getTreatVendorExtensionsAsWarnings()) {
            if (ac.getCssVersion() != CssVersion.CSS1) {
                if (pseudo.startsWith("-")) {
                    // a vendor extension
                    addPseudoClass(new PseudoClassSelector(pseudo));
                    ac.getFrame().addWarning("vendor-ext-pseudo-class", ":" + pseudo);
                    return;
                }
            }
        }

        // is it a pseudo-class?
        String[] ps = PseudoFactory.getPseudoClass(ac.getCssVersion(), ac.getCssProfile());
        if (ps != null) {
            for (String p : ps) {
                if (pseudo.equals(p)) {
                    addPseudoClass(new PseudoClassSelector(pseudo));
                    return;
                }
            }
        }
        CssVersion version = ac.getCssVersion();
        // it's not a pseudo-class, maybe one pseudo element exception
        ps = PseudoFactory.getPseudoElementExceptions(version);
        if (ps != null) {
            for (String p : ps) {
                if (pseudo.equals(p)) {
                    addPseudoClass(new PseudoClassSelector(pseudo));
                    return;
                }
            }
        }
        throw new InvalidParamException("pseudo", ":" + pseudo, ac);
    }

    public void addPseudoElement(String pseudo) throws InvalidParamException {
        if (pseudo == null) {
            return;
        }

        CssVersion version = ac.getCssVersion();

        if (ac.getTreatVendorExtensionsAsWarnings()) {
            if (version != CssVersion.CSS1) {
                if (pseudo.startsWith("-")) {
                    // a vendor extension
                    addPseudoElement(new PseudoElementSelector(pseudo));
                    ac.getFrame().addWarning("vendor-ext-pseudo-element", "::" + pseudo);
                    return;
                }
            }
        }
        // is it a pseudo-element?
        String[] ps = PseudoFactory.getPseudoElement(version);
        if (ps != null) {
            for (String s : ps) {
                if (pseudo.equals(s)) {
                    addPseudoElement(new PseudoElementSelector(pseudo));
                    return;
                }
            }
        }
        // the ident isn't a valid pseudo-something
        throw new InvalidParamException("pseudo", "::" + pseudo, ac);
    }

    public void setPseudoFun(ApplContext ac, String pseudo, String param)
            throws InvalidParamException {

        CssVersion version = ac.getCssVersion();
        String[] ps = PseudoFactory.getPseudoFunction(version);
        if (ps != null) {
            for (String s : ps) {
                if (pseudo.equals(s)) {
                    addPseudoFunction(PseudoFactory.newPseudoFunction(pseudo, param, ac));
                    return;
                }
            }
            throw new InvalidParamException("pseudo", ":" + pseudo, ac);
        }
    }

    public void setPseudoFun(ApplContext ac, String pseudo, ArrayList<CssSelectors> selector_list)
            throws InvalidParamException {

        CssVersion version = ac.getCssVersion();
        String[] ps = PseudoFactory.getPseudoFunction(version);
        if (ps != null) {
            for (String s : ps) {
                if (pseudo.equals(s)) {
                    addPseudoFunction(PseudoFactory.newPseudoFunction(pseudo, selector_list, ac));
                    return;
                }
            }
            throw new InvalidParamException("pseudo", ":" + pseudo, ac);
        }
    }

    public void setPseudoFun(ApplContext ac, String pseudo, CssExpression expression)
            throws InvalidParamException {

        CssVersion version = ac.getCssVersion();
        String[] ps = PseudoFactory.getPseudoFunction(version);
        if (ps != null) {
            for (String s : ps) {
                if (pseudo.equals(s)) {
                    addPseudoFunction(PseudoFactory.newPseudoFunction(pseudo, expression, ac));
                    return;
                }
            }
            throw new InvalidParamException("pseudo", ":" + pseudo, ac);
        }
    }

    public void setPseudoFun(ApplContext ac, String pseudo, CssExpression expression,
                             ArrayList<CssSelectors> selector_list)
            throws InvalidParamException {

        CssVersion version = ac.getCssVersion();
        String[] ps = PseudoFactory.getPseudoFunction(version);
        if (ps != null) {
            for (String s : ps) {
                if (pseudo.equals(s)) {
                    addPseudoFunction(PseudoFactory.newPseudoFunction(pseudo, expression,
                            selector_list, ac));
                    return;
                }
            }
            throw new InvalidParamException("pseudo", ":" + pseudo, ac);
        }
    }


    public void addType(TypeSelector type) throws InvalidParamException {
        super.addType(type);
        element = type.getName();
        hashElement = element.hashCode();
    }

    public void addDescendantCombinator()
            throws InvalidParamException {
        super.addDescendantCombinator();
        connector = DESCENDANT_COMBINATOR;
    }

    public void addChildCombinator()
            throws InvalidParamException {
        super.addChildCombinator();
        connector = CHILD_COMBINATOR;
    }

    public void addNextSiblingCombinator()
            throws InvalidParamException {
        super.addNextSiblingCombinator();
        connector = NEXT_SIBLING_COMBINATOR;
    }

    public void addSubsequentSiblingCombinator()
            throws InvalidParamException {
        super.addSubsequentSiblingCombinator();
        connector = SUBSEQUENT_SIBLING_COMBINATOR;
    }

    public void addColumnCombinator()
            throws InvalidParamException {
        super.addColumnCombinator();
        connector = COLUMN_COMBINATOR;
    }


    public void addNesting() throws InvalidParamException {
        addSelector(new NestingSelector());
    }

    /**
     * Resolves a nested rule's selector against the parent rule's selectors
     *
     * @param ac         the context
     * @param atRule     the at-rule the resolved selectors belong to
     * @param parents    the parent rule's (resolved) selectors
     * @param selector   the nested selector, as parsed as a relative selector
     * @param hasNesting <code>true</code> if the nested selector contains the
     *                   nesting selector, including in pseudo-class arguments
     * @return the resolved selectors, one per parent selector
     * @spec https://www.w3.org/TR/2026/WD-css-nesting-1-20260122/#nest-selector
     */
    public static ArrayList<CssSelectors> resolveNesting(ApplContext ac, AtRule atRule,
                                                         ArrayList<CssSelectors> parents,
                                                         CssSelectors selector,
                                                         boolean hasNesting)
            throws InvalidParamException {
        ArrayList<ArrayList<Selector>> nested = getCompounds(selector);
        // the relative selector production adds a leading compound holding only the optional combinator
        ArrayList<Selector> lead = nested.remove(0);
        Selector combinator = lead.isEmpty() ? null : lead.get(0);
        ArrayList<CssSelectors> resolved = new ArrayList<>(parents.size());
        for (CssSelectors parent : parents) {
            ArrayList<ArrayList<Selector>> parentCompounds = getCompounds(parent);
            ArrayList<ArrayList<Selector>> compounds = new ArrayList<>();
            // relative selectors, and selectors without nesting selector, are prefixed by the parent
            if (combinator != null || !hasNesting) {
                compounds.addAll(copyCompounds(parentCompounds));
                compounds.get(compounds.size() - 1).add((combinator != null) ? combinator : new DescendantCombinator());
            }
            // FIXME the nesting selector is not resolved in pseudo-class arguments, as these are stored as strings
            for (ArrayList<Selector> compound : nested) {
                if (containsNesting(compound)) {
                    substituteNesting(compounds, compound, parent, parentCompounds);
                } else {
                    compounds.add(new ArrayList<>(compound));
                }
            }
            resolved.add(fromCompounds(ac, atRule, compounds));
        }
        return resolved;
    }

    /**
     * Copies selectors, assigning them to a new at-rule
     *
     * @param ac        the context
     * @param atRule    the at-rule the copies belong to
     * @param selectors the selectors to copy
     * @return the copies
     */
    public static ArrayList<CssSelectors> copy(ApplContext ac, AtRule atRule,
                                               ArrayList<CssSelectors> selectors)
            throws InvalidParamException {
        ArrayList<CssSelectors> copies = new ArrayList<>(selectors.size());
        for (CssSelectors selector : selectors) {
            copies.add(fromCompounds(ac, atRule, copyCompounds(getCompounds(selector))));
        }
        return copies;
    }

    // replaces the nesting selector in a compound, with the parent's compounds when that is
    // equivalent, otherwise with :is(parent)
    private static void substituteNesting(ArrayList<ArrayList<Selector>> compounds,
                                          ArrayList<Selector> compound,
                                          CssSelectors parent,
                                          ArrayList<ArrayList<Selector>> parentCompounds) {
        ArrayList<Selector> parentLast = parentCompounds.get(parentCompounds.size() - 1);
        ArrayList<Selector> others = new ArrayList<>();
        Selector combinator = null;
        for (Selector s : compound) {
            if (isCombinator(s)) {
                combinator = s;
            } else if (!(s instanceof NestingSelector)) {
                others.add(s);
            }
        }
        Selector type = getTypeSelector(others);
        Selector parentType = getTypeSelector(parentLast);
        boolean isExact = (compounds.isEmpty() || parentCompounds.size() == 1)
                && !(type != null && parentType != null)
                && !containsPseudoElement(parentLast);
        if (!isExact) {
            ArrayList<CssSelectors> is = new ArrayList<>(1);
            is.add(parent);
            ArrayList<Selector> substituted = new ArrayList<>(compound.size());
            for (Selector s : compound) {
                substituted.add((s instanceof NestingSelector) ? new PseudoFunctionIs("is", is) : s);
            }
            compounds.add(substituted);
            return;
        }
        ArrayList<ArrayList<Selector>> parentCopy = copyCompounds(parentCompounds);
        ArrayList<Selector> merged = parentCopy.remove(parentCopy.size() - 1);
        compounds.addAll(parentCopy);
        // the type selector must come first
        if (type != null) {
            merged.add(0, type);
            others.remove(type);
        }
        merged.addAll(others);
        if (combinator != null) {
            merged.add(combinator);
        }
        compounds.add(merged);
    }

    // returns the compounds of a selector, first to last, each with its trailing combinator
    private static ArrayList<ArrayList<Selector>> getCompounds(CssSelectors selector) {
        ArrayList<ArrayList<Selector>> compounds = new ArrayList<>();
        for (CssSelectors s = selector; s != null; s = s.next) {
            compounds.add(0, new ArrayList<>(s.getSelectors()));
        }
        return compounds;
    }

    private static ArrayList<ArrayList<Selector>> copyCompounds(ArrayList<ArrayList<Selector>> compounds) {
        ArrayList<ArrayList<Selector>> copy = new ArrayList<>(compounds.size());
        for (ArrayList<Selector> compound : compounds) {
            copy.add(new ArrayList<>(compound));
        }
        return copy;
    }

    private static CssSelectors fromCompounds(ApplContext ac, AtRule atRule,
                                              ArrayList<ArrayList<Selector>> compounds)
            throws InvalidParamException {
        CssSelectors selector = null;
        for (ArrayList<Selector> compound : compounds) {
            selector = new CssSelectors(ac, selector);
            selector.setAtRule(atRule);
            for (Selector s : compound) {
                if (s instanceof TypeSelector) {
                    selector.addType((TypeSelector) s);
                } else if (s instanceof DescendantCombinator) {
                    selector.addDescendantCombinator();
                } else if (s instanceof ChildCombinator) {
                    selector.addChildCombinator();
                } else if (s instanceof NextSiblingCombinator) {
                    selector.addNextSiblingCombinator();
                } else if (s instanceof SubsequentSiblingCombinator) {
                    selector.addSubsequentSiblingCombinator();
                } else if (s instanceof ColumnCombinator) {
                    selector.addColumnCombinator();
                } else {
                    selector.addSelector(s);
                }
            }
        }
        return selector;
    }

    private static boolean isCombinator(Selector s) {
        return (s instanceof DescendantCombinator) || (s instanceof ChildCombinator)
                || (s instanceof NextSiblingCombinator) || (s instanceof SubsequentSiblingCombinator)
                || (s instanceof ColumnCombinator);
    }

    private static boolean containsNesting(ArrayList<Selector> compound) {
        for (Selector s : compound) {
            if (s instanceof NestingSelector) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsPseudoElement(ArrayList<Selector> compound) {
        for (Selector s : compound) {
            if (s instanceof PseudoElementSelector) {
                return true;
            }
        }
        return false;
    }

    private static Selector getTypeSelector(ArrayList<Selector> compound) {
        for (Selector s : compound) {
            if ((s instanceof TypeSelector) || (s instanceof UniversalSelector)) {
                return s;
            }
        }
        return null;
    }

    public void addAttribute(AttributeSelector attribute)
            throws InvalidParamException {
        int _s = size();
        Selector s;
        for (int i = 0; i < _s; i++) {
            s = getSelector(i);
            // add warnings if some selectors are incompatible
            // e.g. [lang=en][lang=fr]
            if (s instanceof AttributeSelector) {
                ((AttributeSelector) s).applyAttribute(ac, attribute);
            }
        }
        super.addAttribute(attribute);
    }

    /**
     * Adds a property to this selector.
     *
     * @param property The property.
     * @param warnings For warning report.
     */
    public void addProperty(CssProperty property, Warnings warnings) {
        Init = true;
        if (properties != null) {
            properties.setProperty(ac, property, warnings);
        } else {
            System.err.println("[ERROR] Invalid state in "
                    + "org.w3c.css.parser.CssSelectors#addProperty");
            System.err.println("[ERROR] Please report BUG");
        }
    }

    public CssStyle getStyle() {
        return properties;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        // I'm in reverse order, so compute the next before the current
        if (isToStringCached()) {
            return cachedRepresentation;
        }
        StringBuilder sbrep = new StringBuilder();
        if (next != null) {
            sbrep.append(next.toString());
        }
        sbrep.append(super.toString());
        cachedRepresentation = sbrep.toString();
        return cachedRepresentation;
    }

    /**
     * return XML escaped string
     */
    public String getEscaped() {
        return Messages.escapeString(toString());
    }

    public boolean isToStringCached() {
        if (cachedRepresentation == null) {
            return false;
        }
        if (isFinal) {
            return true;
        }
        if (next != null) {
            return super.isToStringCached() && next.isToStringCached();
        }
        return super.isToStringCached();
    }

    /**
     * Comparison is done on the string representation
     *
     * @param selectors
     * @return
     */
    public int compareTo(CssSelectors selectors) {
        return toString().compareTo(selectors.toString());
    }
    /*
		  we are doing this in two steps, as it is possible to have some
		   calls to toString() and do modifications, then at some point
		   everything is frozen (like when StyleSheet.findConflict is called)
		   marking as final (ie: no more modifications) triggers more
		   optimizations. Things could be better optimized if we were sure
		   that no calls to toString were done before everything is frozen
		*/

    /*
     * Mark as final, ie: no more modification to the structure.
     */
    public void markAsFinal() {
        // if something has been changed, reset to force recomputing
        if (!isFinal) {
            if (!isToStringCached()) {
                cachedRepresentation = null;
                if (next != null) {
                    next.markAsFinal();
                }
            }
            isFinal = true;
        }
    }

    /**
     * Get a hashCode.
     */
	/*public int hashCode() {
		if (hashGeneral == 0) {
			if (atRule instanceof AtRuleFontFace) {
			hashGeneral = atRule.hashCode();
			} else {
			String s = toString();
			hashGeneral = s.hashCode();
			for (int i = 0; i < s.length(); i++) {
				hashGeneral += (int) s.charAt(i);
			}
			}
		}
		return hashGeneral;
		}*/

    /**
     * Returns <code>true</code> if the selector is equals to an another.
     *
     * @param selector The selector to compare
     */
    public boolean equals(Object selector) {
        if ((selector == null) || !(selector instanceof CssSelectors)) {
            return false;
        }
        CssSelectors s = (CssSelectors) selector;

        if ((atRule instanceof AtRulePage)
                || (atRule instanceof AtRuleFontFace)) {
            return atRule.equals(s.atRule);
        }
        if (hashCode() == s.hashCode()) {
            if (atRule == null) {
                return (s.getAtRule() == null);
            } else {
                return atRule.canApply(s.getAtRule());
            }
        } else {
            return false;
        }
    }

    /**
     * Set the previous selector.
     *
     * @param next the previous selector.
     */
    public void setNext(CssSelectors next) {
        this.next = next;
        Invalidate();
    }

    /**
     * Get the previous selector.
     */
    public CssSelectors getNext() {
        return next;
    }

    /**
     * Returns <code>true</code> if there is no property in this document.
     */
    public boolean isEmpty() {
        return !Init;
    }

    public void addAttribute(String attName, String value)
            throws InvalidParamException {
        CssProfile profile = ac.getCssProfile();
        if (profile == CssProfile.MOBILE) {
            throw new InvalidParamException("notformobile", "attributes",
                    ac);
        } else {
            addAttribute(new AttributeExact(attName, value));
            Invalidate();
        }
    }

    void Invalidate() {
        // invalidate all pre-computation in this selectors
        setSpecificity(0);
        //hashGeneral = 0;

        if (Init) {
            // yes I invalidate all properties too !
            try {
                properties = (CssStyle) style.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    final boolean canApply(ArrayList<Selector> attrs, ArrayList<Selector> attrs2) {
        if (attrs.size() > 0) {
            int other_idx;
            Selector other;
            for (Selector selector : attrs) {
                other_idx = attrs2.indexOf(selector);
                if (other_idx == -1) {
                    return false;
                }
                other = attrs2.get(other_idx);
                if (!selector.canApply(other)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the selector can match this selector.
     * <p/>
     * <p/>
     * Examples:<br>
     * <OL>
     * <LI><code>H1.canApply(HTML BODY H1)</code> returns <code>true</code>
     * <LI><code>H1.canApply(HTML BODY H1 EM)</code> returns
     * <code>false</code>
     * <LI><code>(H1 EM).canApply(HTML BODY H2 EM)</code> returns
     * <code>false</code>
     * <LI><code>(HTML EM).canApply(HTML BODY H2 EM)</code> returns
     * <code>true</code>
     * </OL>
     * <p/>
     * <p/>
     * Note:<BR>
     * In principle, if you work with a HTML document, your selector should
     * start with HTML BODY. Because you are always in this context when you
     * parse the text in a HTML document.
     *
     * @param selector the selector to match
     * @see org.w3c.css.css.CssCascadingOrder#order
     */
    public boolean canApply(CssSelectors selector) {
        if ((atRule instanceof AtRulePage)
                || (atRule instanceof AtRuleFontFace)) {
            return atRule.canApply(selector.atRule);
        }
        // current work - don't touch
        Util.verbose(getSpecificity() + " canApply this " + this
                + " selector: " + selector);
        Util.verbose("connector " + connector);
        Util.verbose(getSelectors().toString());
        Util.verbose(selector.getSelectors().toString());

        if ((hashElement != selector.hashElement) && hashElement != 0) {
            // here we are in this case :
            // H1 and HTML BODY H1 EM
            // don't do anything !
            // the cascading order algorithm resolves this case like this :
            //
            // if (for all contexts) !canApply(selector)
            //       go and see canApply(selector.getNext())
            //
            // for further informations,
            //                     see org.w3c.css.css.CssCascadingOrder#order
            Util.verbose("canApply RETURNS FALSE");
            return false;
        } else {
            if (next == null || selector.next == null) {
                boolean result = canApply(getSelectors(), selector.getSelectors());
                Util.verbose("canApply RETURNS " + result);
                return result;
            } else {
                return next.canMatch(selector.next);
            }
        }
    }

    /**
     * Returns true if the selector can match another selector. called by
     * canApply
     *
     * @param selector The selector to compare
     */
    private boolean canMatch(CssSelectors selector) {
        boolean result = canApply(getSelectors(), selector.getSelectors());
        // current work
        Util.verbose("canMatch this " + this + " selector: " + selector);
        Util.verbose("connector " + connector);
        Util.verbose(getSelectors().toString());
        Util.verbose(selector.getSelectors().toString());
        Util.verbose("canMatch for attributes :" + result);

        if ((hashElement != selector.hashElement) && hashElement != 0) {
            if ((connector.equals(DESCENDANT_COMBINATOR)) && (selector.next != null)) {
                // here we are in this case :
                // H1 and HTML BODY H1 EM
                // H1 can't match EM but EM have next
                return canMatch(selector.next);
            } else {
                // here we are in this case :
                // H1 and HTML
                // H1 can't match HTML and HTML don't have next
                Util.verbose("canMatch RETURN FALSE");
                return false;
            }
        }

        if (next == null || selector.next == null) {
            // here we are in this case :
            // H1 and BODY HTML H1
            // or :
            // HTML BODY and BODY (this case won't appear in principle)
            Util.verbose("canMatch RETURN " + result);
            return canApply(getSelectors(), selector.getSelectors());
        } else {
            // here we are in this case :
            // BODY H1 and HTML BODY H1
            return next.canMatch(selector.next);
        }
    }

    public void findConflicts(ApplContext ac, Warnings warnings,
                              CssSelectors[] allSelectors) {
        CssStyle style = getStyle();
        style.findConflicts(ac, warnings, this, allSelectors);
    }

    public static String toArrayString(ArrayList<CssSelectors> selectors) {
        if (selectors == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CssSelectors s : selectors) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
