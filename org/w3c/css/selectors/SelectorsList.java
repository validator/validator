// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

import org.w3c.css.selectors.attributes.AttributeAny;
import org.w3c.css.selectors.attributes.AttributeBegin;
import org.w3c.css.selectors.attributes.AttributeExact;
import org.w3c.css.selectors.combinators.ChildCombinator;
import org.w3c.css.selectors.combinators.ColumnCombinator;
import org.w3c.css.selectors.combinators.DescendantCombinator;
import org.w3c.css.selectors.combinators.NextSiblingCombinator;
import org.w3c.css.selectors.combinators.SubsequentSiblingCombinator;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Messages;

import java.util.ArrayList;

/**
 * SelectorsList<br />
 * A class to manage a list of selectors. The following selectors exists:
 * <ul>
 * <li>Universal: *</li>
 * <li>Type: E</li>
 * <li>Descendant: E F</li>
 * <li>Child: E > F</li>
 * <li>Adjacent: E + F</li>
 * <li>Attribute:
 * <ul>
 * <li>Any: E[foo]</li>
 * <li>Begin: E[lang|=en]</li>
 * <li>Exact: E[lang=en]</li>
 * <li>One Of: E[lang~=en]</li>
 * <li>Start: E[foo^=bar]</li>
 * <li>Substring: E[foo*=bar]</li>
 * <li>Suffix: E[foo$=bar]</li>
 * </ul></li>
 * <li>ID: E#myid</li>
 * <li>Class: E.myclass</li>
 * <li>Pseudo-class: E:first-child, ...</li>
 * <li>Pseudo-element: E:first-line, ...</li>
 * <li>Pseudo-function:
 * <ul>
 * <li>contains</li>
 * <li>lang</li>
 * <li>not</li>
 * <li>nth-child</li>
 * <li>nth-last-child</li>
 * <li>nth-of-type</li>
 * <li>nth-last-of-type</li>
 * <li>...</li>
 * </ul></li>
 * </ul>
 * <p/>
 * Created: Sep 1, 2005 3:34:47 PM<br />
 */
public class SelectorsList {

    // the list of selectors
    private ArrayList<Selector> selectors;

    private ApplContext ac;

    private int specificity;

    private String stringrep = null;

    /**
     * Creates a new empty SelectorsList
     */
    public SelectorsList() {
        selectors = new ArrayList<Selector>();
    }

    /**
     * Creates a new SelectorsList given an context
     *
     * @param ac the context in which the selectors appear
     */
    public SelectorsList(ApplContext ac) {
        this.ac = ac;
        selectors = new ArrayList<Selector>();
    }

    /**
     * Returns the selectors list
     *
     * @return Returns the selectors list.
     */
    public ArrayList<Selector> getSelectors() {
        return selectors;
    }

    /**
     * Sets the selectors list
     *
     * @param selectors The selectors list to set.
     */
    public void setSelectors(ArrayList<Selector> selectors) {
        this.selectors = selectors;
        stringrep = null;
    }

    /**
     * Return the nth selector in this SelectorsList
     *
     * @param index the index of the selector to retreive
     * @return the nth selector
     */
    public Selector getSelector(int index) {
        return selectors.get(index);
    }

    /**
     * The number of selectors in this SelectorsList
     *
     * @return the number of selectors in this SelectorsList
     */
    public int size() {
        return selectors.size();
    }

    /**
     * Adds a selector to this SelectorsList
     *
     * @param selector the selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addSelector(Selector selector) throws InvalidParamException {
        /* FIXME TODO
           the grammar is checking the basic structure but specific rules
           should appear here
         */
        selectors.add(selector);
        stringrep = null;
    }

    /**
     * Adds an attribute selector
     *
     * @param attribute the attribute selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addAttribute(AttributeSelector attribute)
            throws InvalidParamException {
        addSelector(attribute);
    }

    /**
     * Adds an universal selector
     *
     * @param universal the universal selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addUniversal(UniversalSelector universal)
            throws InvalidParamException {
        addSelector(universal);
    }

    /**
     * Adds a type selector
     *
     * @param type the type selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addType(TypeSelector type) throws InvalidParamException {
        addSelector(type);
    }

    /**
     * Adds a descendant selector
     *
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addDescendantCombinator()
            throws InvalidParamException {
        addSelector(new DescendantCombinator());
    }

    /**
     * Adds a child selector
     *
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addChildCombinator() throws InvalidParamException {
        addSelector(new ChildCombinator());
    }

    /**
     * Adds a pseudo-class selector
     *
     * @param pc the pseudo-class to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addPseudoClass(PseudoClassSelector pc)
            throws InvalidParamException {
        addSelector(pc);
    }

    /**
     * Adds a pseudo-element selector
     * No other selector can be added after a pseudo-element
     *
     * @param pe the pseudo-element to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addPseudoElement(PseudoElementSelector pe)
            throws InvalidParamException {
        addSelector(pe);
    }

    /**
     * Adds a pseudo-function selector
     *
     * @param pf the pseudo-function to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addPseudoFunction(PseudoFunctionSelector pf)
            throws InvalidParamException {
        addSelector(pf);
    }

    /**
     * Adds a next sibling combinator
     *
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addNextSiblingCombinator()
            throws InvalidParamException {
        addSelector(new NextSiblingCombinator());
    }

    /**
     * Adds an subsequent sibling combinator
     *
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addSubsequentSiblingCombinator()
            throws InvalidParamException {
        addSelector(new SubsequentSiblingCombinator());
    }

    /**
     * Adds an column combinator
     *
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addColumnCombinator()
            throws InvalidParamException {
        addSelector(new ColumnCombinator());
    }

    /**
     * Adds a class selector
     *
     * @param cs the class selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addClass(ClassSelector cs) throws InvalidParamException {
        addSelector(cs);
    }

    /**
     * Adds an id selector
     *
     * @param id the id selector to add
     * @throws InvalidParamException when trying to add a selector after a pseudo-element
     */
    public void addId(IdSelector id) throws InvalidParamException {
        addSelector(id);
    }

    /**
     * Returns a String representation of this SelectorsList
     *
     * @return the String representation of this SelectorsList
     */
    public String toString() {
        if (stringrep != null) {
            return stringrep;
        }
        StringBuilder res = new StringBuilder();
        for (Selector selector : selectors) {
            res.append(selector);
        }
        stringrep = res.toString();
        return stringrep;
    }

    public boolean isToStringCached() {
        return (stringrep != null);
    }

    public String toStringEscaped() {
        return Messages.escapeString(toString());
    }

    /**
     * Sets the specificity of this SelectorsList
     *
     * @param specificity the specificity yo set
     */
    public void setSpecificity(int specificity) {
        this.specificity = specificity;
    }

    /**
     * Gets (and computes) the specificity of this selector.
     */
    public int getSpecificity() {
        int a = 0;
        int b = 0;
        int c = 0;

        for (int i = 0; i < size(); i++) {
            Selector s = getSelector(i);
            if (s instanceof IdSelector) {
                a++;
            } else if (s instanceof ClassSelector ||
                    s instanceof PseudoClassSelector) {
                b++;
            } else if (s instanceof TypeSelector ||
                    s instanceof AttributeSelector) {
                c++;
            }
            // some pseudo-functions might influence the specificity
            else if (s instanceof PseudoFunctionSelector) {
                specificity += ((PseudoFunctionSelector) s).getSpecificity();
            }
        }
        specificity += a * 100 + b * 10 + c;

        return specificity;
    }

    /**
     * Testing method
     *
     * @param args unused
     */
    public static void main(String[] args) {
        SelectorsList s = new SelectorsList();
        try {
            s.addType(new TypeSelector("E"));
            s.addAttribute(new AttributeExact("foo", "warning"));
            s.addChildCombinator();
            s.addType(new TypeSelector("F"));
            s.addAttribute(new AttributeBegin("lang", "en"));
            s.addAttribute(new AttributeAny("bar"));
            s.addNextSiblingCombinator();
            s.addType(new TypeSelector("G"));
            s.addId(new IdSelector("id"));
            s.addAttribute(new AttributeAny("blop"));
            s.addDescendantCombinator();
            s.addType(new TypeSelector("H"));

            System.out.println(s);
        } catch (InvalidParamException e) {
            System.err.println(e.getMessage());
        }
    }

}
