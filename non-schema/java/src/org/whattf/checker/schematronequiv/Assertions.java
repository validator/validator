/*
 * Copyright (c) 2008-2009 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.whattf.checker.schematronequiv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.whattf.checker.AttributeUtil;
import org.whattf.checker.Checker;
import org.whattf.checker.LocatorImpl;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class Assertions extends Checker {

    private static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(
            String lowerCaseLiteral, String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsIgnoreAsciiCase(String one, String other) {
        if (other == null) {
            if (one == null) {
                return true;
            } else {
                return false;
            }
        }
        if (one.length() != other.length()) {
            return false;
        }
        for (int i = 0; i < one.length(); i++) {
            char c0 = one.charAt(i);
            char c1 = other.charAt(i);
            if (c0 >= 'A' && c0 <= 'Z') {
                c0 += 0x20;
            }
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    private static final Map<String, String> OBSOLETE_ELEMENTS = new HashMap<String, String>();

    static {
        OBSOLETE_ELEMENTS.put("center", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("font", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("big", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("s", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("strike", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("tt", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("u", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("acronym",
                "Use the \u201Cabbr\u201D element instead.");
        OBSOLETE_ELEMENTS.put("dir", "Use the \u201Cul\u201D element instead.");
        OBSOLETE_ELEMENTS.put("applet",
                "Use the \u201Cobject\u201D element instead.");
        OBSOLETE_ELEMENTS.put("basefont", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put(
                "frameset",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put(
                "noframes",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
    }

    private static final Map<String, String[]> OBSOLETE_ATTRIBUTES = new HashMap<String, String[]>();

    static {
        OBSOLETE_ATTRIBUTES.put("abbr", new String[] { "td", "th" });
        OBSOLETE_ATTRIBUTES.put("archive", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("axis", new String[] { "td", "th" });
        OBSOLETE_ATTRIBUTES.put("charset", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("classid", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("code", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codebase", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codetype", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("coords", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("datafld", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("dataformatas", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datasrc", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datapagesize", new String[] { "table" });
        OBSOLETE_ATTRIBUTES.put("declare", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("event", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("for", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("language", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("longdesc", new String[] { "img", "iframe" });
        OBSOLETE_ATTRIBUTES.put("methods", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("name", new String[] { "img", "embed", "option" });
        OBSOLETE_ATTRIBUTES.put("nohref", new String[] { "area" });
        OBSOLETE_ATTRIBUTES.put("profile", new String[] { "head" });
        OBSOLETE_ATTRIBUTES.put("rev", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("scheme", new String[] { "meta" });
        OBSOLETE_ATTRIBUTES.put("scope", new String[] { "td" });
        OBSOLETE_ATTRIBUTES.put("shape", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("standby", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("target", new String[] { "link" });
        OBSOLETE_ATTRIBUTES.put("type", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("urn", new String[] { "a", "link" });
        OBSOLETE_ATTRIBUTES.put("usemap", new String[] { "input" });
        OBSOLETE_ATTRIBUTES.put("valuetype", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("version", new String[] { "html" });
    }

    private static final Map<String, String> OBSOLETE_ATTRIBUTES_MSG = new HashMap<String, String>();

    static {
        OBSOLETE_ATTRIBUTES_MSG.put(
                "abbr",
                "Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "archive",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Carchive\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("axis",
                "Use the \u201Cscope\u201D attribute.");
        OBSOLETE_ATTRIBUTES_MSG.put("charset",
                "Use an HTTP Content-Type header on the linked resource instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "classid",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Cclassid\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "code",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccode\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codebase",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodebase\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codetype",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodetype\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("coords",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("datapagesize", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("datafld", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("dataformatas", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("datasrc", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("for",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put("event",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "declare",
                "Repeat the \u201Cobject\u201D element completely each time the resource is to be reused.");
        OBSOLETE_ATTRIBUTES_MSG.put("language",
                "Use the \u201Ctype\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("longdesc",
                "Use a regular \u201Ca\u201D element to link to the description.");
        OBSOLETE_ATTRIBUTES_MSG.put("methods",
                "Use the HTTP OPTIONS feature instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("name",
                "Use the \u201Cid\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("nohref",
                "Omitting the \u201Chref\u201D attribute is sufficient.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "profile",
                "To declare which \u201Cmeta\u201D terms are used in the document, instead register the names as meta extensions. To trigger specific UA behaviors, use a \u201Clink\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "rev",
                "Use the \u201Crel\u201D attribute instead, with a term having the opposite meaning.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "scheme",
                "Use only one scheme per field, or make the scheme declaration part of the value.");
        OBSOLETE_ATTRIBUTES_MSG.put("scope",
                "Use the \u201Cscope\u201D attribute on a \u201Cth\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("shape",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "standby",
                "Optimise the linked resource so that it loads quickly or, at least, incrementally.");
        OBSOLETE_ATTRIBUTES_MSG.put("target", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "type",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "urn",
                "Specify the preferred persistent identifier using the \u201Chref\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "usemap",
                "Use the \u201Cimg\u201D element instead of the \u201Cinput\u201D element for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "valuetype",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put("version", "You can safely omit it.");
    }

    private static final Map<String, String[]> OBSOLETE_STYLE_ATTRS = new HashMap<String, String[]>();

    static {
        OBSOLETE_STYLE_ATTRS.put("align", new String[] { "caption", "iframe",
                "img", "input", "object", "embed", "legend", "table", "hr",
                "div", "h1", "h2", "h3", "h4", "h5", "h6", "p", "col",
                "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("alink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("allowtransparency", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("background", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("bgcolor", new String[] { "table", "tr", "td",
                "th", "body" });
        OBSOLETE_STYLE_ATTRS.put("border", new String[] { "table", "object" });
        OBSOLETE_STYLE_ATTRS.put("cellpadding", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("cellspacing", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("char", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("charoff", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("clear", new String[] { "br" });
        OBSOLETE_STYLE_ATTRS.put("color", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("compact", new String[] { "dl", "menu", "ol",
                "ul" });
        OBSOLETE_STYLE_ATTRS.put("frameborder", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("frame", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("height", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("hspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("link", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginbottom", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginheight", new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("marginleft", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginright", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("margintop", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginwidth", new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("noshade", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("nowrap", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("rules", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("scrolling", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("size", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("text", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("type", new String[] { "li", "ol", "ul" });
        OBSOLETE_STYLE_ATTRS.put("valign", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("vlink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("vspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("width", new String[] { "hr", "table", "td",
                "th", "col", "colgroup", "pre" });
    }

    private static final String[] SPECIAL_ANCESTORS = { "a", "address",
            "button", "caption", "dfn", "footer", "form", "header", "label",
            "map", "noscript", "time", "progress", "meter" };

    private static int specialAncestorNumber(String name) {
        for (int i = 0; i < SPECIAL_ANCESTORS.length; i++) {
            if (name == SPECIAL_ANCESTORS[i]) {
                return i;
            }
        }
        return -1;
    }

    private static Map<String, Integer> ANCESTOR_MASK_BY_DESCENDANT = new HashMap<String, Integer>();

    private static void registerProhibitedAncestor(String ancestor,
            String descendant) {
        int number = specialAncestorNumber(ancestor);
        if (number == -1) {
            throw new IllegalStateException("Ancestor not found in array: "
                    + ancestor);
        }
        Integer maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(descendant);
        int mask = 0;
        if (maskAsObject != null) {
            mask = maskAsObject.intValue();
        }
        mask |= (1 << number);
        ANCESTOR_MASK_BY_DESCENDANT.put(descendant, new Integer(mask));
    }

    static {
        registerProhibitedAncestor("form", "form");
        registerProhibitedAncestor("time", "time");
        registerProhibitedAncestor("progress", "progress");
        registerProhibitedAncestor("meter", "meter");
        registerProhibitedAncestor("dfn", "dfn");
        registerProhibitedAncestor("noscript", "noscript");
        registerProhibitedAncestor("label", "label");
        registerProhibitedAncestor("address", "address");
        registerProhibitedAncestor("address", "section");
        registerProhibitedAncestor("address", "nav");
        registerProhibitedAncestor("address", "article");
        registerProhibitedAncestor("address", "aside");
        registerProhibitedAncestor("header", "header");
        registerProhibitedAncestor("footer", "header");
        registerProhibitedAncestor("address", "header");
        registerProhibitedAncestor("header", "footer");
        registerProhibitedAncestor("footer", "footer");
        registerProhibitedAncestor("address", "footer");
        registerProhibitedAncestor("address", "h1");
        registerProhibitedAncestor("address", "h2");
        registerProhibitedAncestor("address", "h3");
        registerProhibitedAncestor("address", "h4");
        registerProhibitedAncestor("address", "h5");
        registerProhibitedAncestor("address", "h6");
        registerProhibitedAncestor("a", "a");
        registerProhibitedAncestor("button", "a");
        registerProhibitedAncestor("a", "details");
        registerProhibitedAncestor("button", "details");
        registerProhibitedAncestor("a", "button");
        registerProhibitedAncestor("button", "button");
        registerProhibitedAncestor("a", "textarea");
        registerProhibitedAncestor("button", "textarea");
        registerProhibitedAncestor("a", "select");
        registerProhibitedAncestor("button", "select");
        registerProhibitedAncestor("a", "keygen");
        registerProhibitedAncestor("button", "keygen");
        registerProhibitedAncestor("a", "embed");
        registerProhibitedAncestor("button", "embed");
        registerProhibitedAncestor("a", "iframe");
        registerProhibitedAncestor("button", "iframe");
        registerProhibitedAncestor("a", "label");
        registerProhibitedAncestor("button", "label");
        registerProhibitedAncestor("caption", "table");
    }

    private static final int A_BUTTON_MASK = (1 << specialAncestorNumber("a"))
            | (1 << specialAncestorNumber("button"));

    private static final int MAP_MASK = (1 << specialAncestorNumber("map"));

    private static final int HREF_MASK = (1 << 30);

    private static final int LABEL_FOR_MASK = (1 << 28);

    private static final Map<String, Set<String>> REQUIRED_ROLE_PARENT_BY_CHILD = new HashMap<String, Set<String>>();

    private static void registerRequiredParentRole(String parent, String child) {
        Set<String> parents = REQUIRED_ROLE_PARENT_BY_CHILD.get(child);
        if (parents == null) {
            parents = new HashSet<String>();
        }
        parents.add(parent);
        REQUIRED_ROLE_PARENT_BY_CHILD.put(child, parents);
    }

    static {
        registerRequiredParentRole("listbox", "option");
        registerRequiredParentRole("menu", "menuitem");
        registerRequiredParentRole("menu", "menuitemcheckbox");
        registerRequiredParentRole("menu", "menuitemradio");
        registerRequiredParentRole("tablist", "tab");
        registerRequiredParentRole("tree", "treeitem");
        registerRequiredParentRole("list", "listitem");
        registerRequiredParentRole("row", "gridcell");
    }

    private static final Set<String> MUST_NOT_DANGLE_IDREFS = new HashSet<String>();

    static {
        MUST_NOT_DANGLE_IDREFS.add("aria-controls");
        MUST_NOT_DANGLE_IDREFS.add("aria-describedby");
        MUST_NOT_DANGLE_IDREFS.add("aria-flowto");
        MUST_NOT_DANGLE_IDREFS.add("aria-labelledby");
        MUST_NOT_DANGLE_IDREFS.add("aria-owns");
    }

    private static final Map<String, Set<String>> ALLOWED_CHILD_ROLE_BY_PARENT = new HashMap<String, Set<String>>();

    private static void registerAllowedChildRole(String parent, String child) {
        Set<String> children = ALLOWED_CHILD_ROLE_BY_PARENT.get(parent);
        if (children == null) {
            children = new HashSet<String>();
        }
        children.add(child);
        ALLOWED_CHILD_ROLE_BY_PARENT.put(parent, children);
    }

    static {
        registerAllowedChildRole("listbox", "option");
        registerAllowedChildRole("menu", "menuitem");
        registerAllowedChildRole("menu", "menuitemcheckbox");
        registerAllowedChildRole("menu", "menuitemradio");
        registerAllowedChildRole("tree", "treeitem");
        registerAllowedChildRole("list", "listitem");
        registerAllowedChildRole("radiogroup", "radio");
        registerAllowedChildRole("tablist", "tab");
        registerAllowedChildRole("row", "gridcell");
    }

    private class IdrefLocator {
        private final Locator locator;

        private final String idref;

        private final String additional;

        /**
         * @param locator
         * @param idref
         */
        public IdrefLocator(Locator locator, String idref) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = null;
        }

        public IdrefLocator(Locator locator, String idref, String additional) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = additional;
        }

        /**
         * Returns the locator.
         * 
         * @return the locator
         */
        public Locator getLocator() {
            return locator;
        }

        /**
         * Returns the idref.
         * 
         * @return the idref
         */
        public String getIdref() {
            return idref;
        }

        /**
         * Returns the additional.
         * 
         * @return the additional
         */
        public String getAdditional() {
            return additional;
        }
    }

    private class StackNode {
        private final int ancestorMask;

        private final String name; // null if not HTML

        private final String role;

        private final String activeDescendant;

        private final String forAttr;

        private boolean children = false;

        private boolean selectedOptions = false;

        private boolean labeledDescendants = false;

        /**
         * @param ancestorMask
         */
        public StackNode(int ancestorMask, String name, String role,
                String activeDescendant, String forAttr) {
            this.ancestorMask = ancestorMask;
            this.name = name;
            this.role = role;
            this.activeDescendant = activeDescendant;
            this.forAttr = forAttr;
        }

        /**
         * Returns the ancestorMask.
         * 
         * @return the ancestorMask
         */
        public int getAncestorMask() {
            return ancestorMask;
        }

        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the children.
         * 
         * @return the children
         */
        public boolean isChildren() {
            return children;
        }

        /**
         * Sets the children.
         * 
         * @param children
         *            the children to set
         */
        public void setChildren() {
            this.children = true;
        }

        /**
         * Returns the selectedOptions.
         * 
         * @return the selectedOptions
         */
        public boolean isSelectedOptions() {
            return selectedOptions;
        }

        /**
         * Sets the selectedOptions.
         * 
         * @param selectedOptions
         *            the selectedOptions to set
         */
        public void setSelectedOptions() {
            this.selectedOptions = true;
        }

        /**
         * Returns the labeledDescendants.
         * 
         * @return the labeledDescendants
         */
        public boolean isLabeledDescendants() {
            return labeledDescendants;
        }

        /**
         * Sets the labeledDescendants.
         * 
         * @param labeledDescendants
         *            the labeledDescendants to set
         */
        public void setLabeledDescendants() {
            this.labeledDescendants = true;
        }

        /**
         * Returns the role.
         * 
         * @return the role
         */
        public String getRole() {
            return role;
        }

        /**
         * Returns the activeDescendant.
         * 
         * @return the activeDescendant
         */
        public String getActiveDescendant() {
            return activeDescendant;
        }

        /**
         * Returns the forAttr.
         * 
         * @return the forAttr
         */
        public String getForAttr() {
            return forAttr;
        }
    }

    private StackNode[] stack;

    private int currentPtr;

    public Assertions() {
        super();
    }

    private void push(StackNode node) {
        currentPtr++;
        if (currentPtr == stack.length) {
            StackNode[] newStack = new StackNode[stack.length + 64];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[currentPtr] = node;
    }

    private StackNode pop() {
        return stack[currentPtr--];
    }

    private StackNode peek() {
        return stack[currentPtr];
    }

    private Map<StackNode, Locator> openSingleSelects = new HashMap<StackNode, Locator>();

    private Map<StackNode, Locator> openLabels = new HashMap<StackNode, Locator>();

    private Map<StackNode, Locator> openActiveDescendants = new HashMap<StackNode, Locator>();

    private LinkedHashSet<IdrefLocator> contextmenuReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> menuIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> formControlReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> formControlIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> listIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> ariaReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> allIds = new HashSet<String>();

    /**
     * @see org.whattf.checker.Checker#endDocument()
     */
    @Override public void endDocument() throws SAXException {
        // contextmenu
        for (IdrefLocator idrefLocator : contextmenuReferences) {
            if (!menuIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Ccontextmenu\u201D attribute must refer to a \u201Cmenu\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // label for
        for (IdrefLocator idrefLocator : formControlReferences) {
            if (!formControlIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Cfor\u201D attribute of the \u201Clabel\u201D element must refer to a form control.",
                        idrefLocator.getLocator());
            }
        }

        // input list
        for (IdrefLocator idrefLocator : listReferences) {
            if (!listIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Clist\u201D attribute of the \u201Cinput\u201D element must refer to a \u201Cdatalist\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA idrefs
        for (IdrefLocator idrefLocator : ariaReferences) {
            if (!allIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201C"
                                + idrefLocator.getAdditional()
                                + "\u201D attribute must point to an element in the same document.",
                        idrefLocator.getLocator());
            }
        }

        reset();
        stack = null;
    }

    private static double getDoubleAttribute(Attributes atts, String name) {
        String str = atts.getValue("", name);
        if (str == null) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override public void endElement(String uri, String localName, String name)
            throws SAXException {
        StackNode node = pop();
        Locator locator = null;
        openSingleSelects.remove(node);
        openLabels.remove(node);
        if ((locator = openActiveDescendants.remove(node)) != null) {
            err(
                    "The \u201Caria-activedescendant\u201D attribute must refer to a descendant element.",
                    locator);
        }
    }

    /**
     * @see org.whattf.checker.Checker#startDocument()
     */
    @Override public void startDocument() throws SAXException {
        reset();
        stack = new StackNode[32];
        currentPtr = 0;
        stack[0] = null;
    }

    public void reset() {
        openSingleSelects.clear();
        openLabels.clear();
        openActiveDescendants.clear();
        contextmenuReferences.clear();
        menuIds.clear();
        formControlReferences.clear();
        formControlIds.clear();
        listReferences.clear();
        listIds.clear();
        ariaReferences.clear();
        allIds.clear();
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String name, Attributes atts) throws SAXException {
        Set<String> ids = new HashSet<String>();
        String role = null;
        String activeDescendant = null;
        String forAttr = null;
        boolean href = false;

        StackNode parent = peek();
        int ancestorMask = 0;
        String parentRole = null;
        String parentName = null;
        if (parent != null) {
            ancestorMask = parent.getAncestorMask();
            parentName = parent.getName();
            parentRole = parent.getRole();
        }
        if ("http://www.w3.org/1999/xhtml" == uri) {
            boolean controls = false;
            boolean hidden = false;
            boolean add = false;
            boolean toolbar = false;
            boolean usemap = false;
            boolean ismap = false;
            boolean selected = false;
            boolean languageJavaScript = false;
            boolean typeNotTextJavaScript = false;
            String xmlLang = null;
            String lang = null;
            String id = null;
            String contextmenu = null;
            String list = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attUri = atts.getURI(i);
                if (attUri.length() == 0) {
                    String attLocal = atts.getLocalName(i);
                    if ("href" == attLocal) {
                        href = true;
                    } else if ("controls" == attLocal) {
                        controls = true;
                    } else if ("type" == attLocal && "param" != localName
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String attValue = atts.getValue(i);
                        if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "hidden", attValue)) {
                            hidden = true;
                        } else if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "toolbar", attValue)) {
                            toolbar = true;
                        }

                        if (!lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "text/javascript", attValue)) {
                            typeNotTextJavaScript = true;
                        }
                    } else if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    } else if ("list" == attLocal) {
                        list = atts.getValue(i);
                    } else if ("lang" == attLocal) {
                        lang = atts.getValue(i);
                    } else if ("id" == attLocal) {
                        id = atts.getValue(i);
                    } else if ("for" == attLocal && "label" == localName) {
                        forAttr = atts.getValue(i);
                        ancestorMask |= LABEL_FOR_MASK;
                    } else if ("contextmenu" == attLocal) {
                        contextmenu = atts.getValue(i);
                    } else if ("ismap" == attLocal) {
                        ismap = true;
                    } else if ("selected" == attLocal) {
                        selected = true;
                    } else if ("usemap" == attLocal && "input" != localName) {
                        usemap = true;
                    } else if ("language" == attLocal
                            && lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                    "javascript", atts.getValue(i))) {
                        languageJavaScript = true;
                    } else if (OBSOLETE_ATTRIBUTES.containsKey(attLocal)
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String[] elementNames = OBSOLETE_ATTRIBUTES.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            String suggestion = OBSOLETE_ATTRIBUTES_MSG.containsKey(attLocal) ? " "
                                    + OBSOLETE_ATTRIBUTES_MSG.get(attLocal)
                                    : "";
                            err("The \u201C" + attLocal
                                    + "\u201D attribute on the \u201C"
                                    + localName + "\u201D element is obsolete."
                                    + suggestion);
                        }
                    } else if (OBSOLETE_STYLE_ATTRS.containsKey(attLocal)) {
                        String[] elementNames = OBSOLETE_STYLE_ATTRS.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            err("The \u201C"
                                    + attLocal
                                    + "\u201D attribute on the \u201C"
                                    + localName
                                    + "\u201D element is obsolete. Use CSS instead.");
                        }
                    }
                } else if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    if ("lang" == atts.getLocalName(i)) {
                        xmlLang = atts.getValue(i);
                    }
                }

                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
            }

            // Obsolete elements
            if (OBSOLETE_ELEMENTS.get(localName) != null) {
                err("The \u201C" + localName + "\u201D element is obsolete. "
                        + OBSOLETE_ELEMENTS.get(localName));
            }

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "";
            if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(localName)) != null) {
                mask = maskAsObject.intValue();
                descendantUiString = localName;
            } else if ("video" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "video\u201D with the attribute \u201Ccontrols";
            } else if ("audio" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "audio\u201D with the attribute \u201Ccontrols";
            } else if ("menu" == localName && toolbar) {
                mask = A_BUTTON_MASK;
                descendantUiString = "menu\u201D with the attribute \u201Ctype=toolbar";
            } else if ("img" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "img\u201D with the attribute \u201Cusemap";
            } else if ("object" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "object\u201D with the attribute \u201Cusemap";
            } else if ("input" == localName && !hidden) {
                mask = A_BUTTON_MASK;
                descendantUiString = "input";
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (int j = 0; j < SPECIAL_ANCESTORS.length; j++) {
                        if ((maskHit & 1) != 0) {
                            err("The element \u201C"
                                    + descendantUiString
                                    + "\u201D must not appear as a descendant of the \u201C"
                                    + SPECIAL_ANCESTORS[j] + "\u201D element.");
                        }
                        maskHit >>= 1;
                    }
                }
            }

            // Required ancestors
            if ("area" == localName && ((ancestorMask & MAP_MASK) == 0)) {
                err("The \u201Carea\u201D element must have a \u201Cmap\u201D ancestor.");
            } else if ("img" == localName && ismap
                    && ((ancestorMask & HREF_MASK) == 0)) {
                err("The \u201Cimg\u201D element with the \u201Cismap\u201D attribute set must have an \u201Ca\u201D ancestor with the \u201Chref\u201D attribute.");
            } else if ("input" == localName || "button" == localName
                    || "select" == localName || "textarea" == localName
                    || "keygen" == localName) {
                for (Map.Entry<StackNode, Locator> entry : openLabels.entrySet()) {
                    StackNode node = entry.getKey();
                    Locator locator = entry.getValue();
                    if (node.isLabeledDescendants()) {
                        err("The \u201Clabel\u201D element may contain at most one \u201Cinput\u201D, \u201Cbutton\u201D, \u201Cselect\u201D, \u201Ctextarea\u201D, or \u201Ckeygen\u201D descendant.");
                        warn(
                                "\u201Clabel\u201D element with multiple labelable descendants.",
                                locator);
                    } else {
                        node.setLabeledDescendants();
                    }
                }
                if ((ancestorMask & LABEL_FOR_MASK) != 0) {
                    boolean hasMatchingFor = false;
                    for (int i = 0; (stack[currentPtr - i].getAncestorMask() & LABEL_FOR_MASK) != 0; i++) {
                        String forVal = stack[currentPtr - i].getForAttr();
                        if (forVal != null && forVal.equals(id)) {
                            hasMatchingFor = true;
                            break;
                        }
                    }
                    if (id == null || !hasMatchingFor) {
                        err("Any \u201C"
                                + localName
                                + "\u201D descendant of a \u201Clabel\u201D element with a \u201Cfor\u201D attribute must have an ID value that matches that \u201Cfor\u201D attribute.");
                    }
                }
            }

            // progress
            else if ("progress" == localName) {
                double value = getDoubleAttribute(atts, "value");
                if (!Double.isNaN(value)) {
                    double max = getDoubleAttribute(atts, "max");
                    if (Double.isNaN(max)) {
                        if (!(value <= 1.0)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                        }
                    } else {
                        if (!(value <= max)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                        }
                    }
                }
            }

            // meter
            else if ("meter" == localName) {
                double value = getDoubleAttribute(atts, "value");
                double min = getDoubleAttribute(atts, "min");
                double max = getDoubleAttribute(atts, "max");
                double optimum = getDoubleAttribute(atts, "optimum");
                double low = getDoubleAttribute(atts, "low");
                double high = getDoubleAttribute(atts, "high");

                if (!Double.isNaN(min) && !Double.isNaN(value)
                        && !(min <= value)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cvalue\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(value) && !(0 <= value)) {
                    err("The value of the \u201Cvalue\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(value) && !Double.isNaN(max)
                        && !(value <= max)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(value) && Double.isNaN(max) && !(value <= 1)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && !(min <= max)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(max) && !(0 <= max)) {
                    err("The value of the \u201Cmax\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && Double.isNaN(max) && !(min <= 1)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(low) && !(min <= low)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Clow\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(low) && !(0 <= low)) {
                    err("The value of the \u201Clow\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(high) && !(min <= high)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(high) && !(0 <= high)) {
                    err("The value of the \u201Chigh\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(high) && !(low <= high)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (!Double.isNaN(high) && !Double.isNaN(max) && !(high <= max)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(high) && Double.isNaN(max) && !(high <= 1)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(max) && !(low <= max)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(low) && Double.isNaN(max) && !(low <= 1)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(min <= optimum)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Coptimum\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(0 <= optimum)) {
                    err("The value of the \u201Coptimum\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(optimum) && !Double.isNaN(max)
                        && !(optimum <= max)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(optimum) && Double.isNaN(max)
                        && !(optimum <= 1)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
            }

            // map required attrs
            else if ("map" == localName && id != null) {
                String nameVal = atts.getValue("", "name");
                if (nameVal != null && !nameVal.equals(id)) {
                    err("The \u201Cid\u201D attribute on a \u201Cmap\u201D element must have an the same value as the \u201Cname\u201D attribute.");
                }
            }

            // script language
            else if ("script" == localName && languageJavaScript
                    && typeNotTextJavaScript) {
                err("A \u201Cscript\u201D element with the \u201Clanguage=\"JavaScript\"\u201D attribute set must not have a \u201Ctype\u201D attribute whose value is not \u201Ctext/javascript\u201D.");
            }

            // bdo required attrs
            else if ("bdo" == localName && atts.getIndex("", "dir") < 0) {
                err("A \u201Cbdo\u201D element must have an \u201Cdir\u201D attribute.");
            }

            // lang and xml:lang for XHTML5
            if (lang != null && xmlLang != null
                    && !equalsIgnoreAsciiCase(lang, xmlLang)) {
                err("When the attribute \u201Clang\u201D in no namespace and the attribute \u201Clang\u201D in the XML namespace are both present, they must have the same value.");
            }

            // contextmenu
            if (contextmenu != null) {
                contextmenuReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), contextmenu));
            }
            if ("menu" == localName) {
                menuIds.addAll(ids);
            }
            if ("datalist" == localName) {
                listIds.addAll(ids);
            }

            // label for
            if ("label" == localName) {
                String forVal = atts.getValue("", "for");
                if (forVal != null) {
                    formControlReferences.add(new IdrefLocator(new LocatorImpl(
                            getDocumentLocator()), forVal));
                }
            }
            if (("input" == localName && !hidden) || "textarea" == localName
                    || "select" == localName || "button" == localName
                    || "keygen" == localName || "output" == localName) {
                formControlIds.addAll(ids);
            }

            // input list
            if ("input" == localName && list != null) {
                listReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), list));
            }

            // multiple selected options
            if ("option" == localName && selected) {
                for (Map.Entry<StackNode, Locator> entry : openSingleSelects.entrySet()) {
                    StackNode node = entry.getKey();
                    if (node.isSelectedOptions()) {
                        err("The \u201Cselect\u201D element cannot have more than one selected \u201Coption\u201D descendant unless the \u201Cmultiple\u201D attribute is specified.");
                    } else {
                        node.setSelectedOptions();
                    }
                }
            }
        } else {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
                String attLocal = atts.getLocalName(i);
                if (atts.getURI(i).length() == 0) {
                    if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    }
                }
            }

            allIds.addAll(ids);
        }

        // ARIA required parents
        Set<String> requiredParents = REQUIRED_ROLE_PARENT_BY_CHILD.get(role);
        if (requiredParents != null) {
            if (!requiredParents.contains(parentRole)) {
                err("An element with \u201Crole=" + role + "\u201D requires "
                        + renderRoleSet(requiredParents) + " on the parent.");
            }
        }

        // ARIA only allowed children
        Set<String> allowedChildren = ALLOWED_CHILD_ROLE_BY_PARENT.get(parentRole);
        if (allowedChildren != null) {
            if (!allowedChildren.contains(role)) {
                err("Only elements with "
                        + renderRoleSet(allowedChildren)
                        + " are allowed as children of an element with \u201Crole="
                        + parentRole + "\u201D.");
            }
        }

        // ARIA row
        if ("row".equals(role)) {
            if (!("grid".equals(parentRole) || "treegrid".equals(parentRole) || (currentPtr > 1
                    && "grid".equals(stack[currentPtr - 1].getRole()) || "treegrid".equals(stack[currentPtr - 1].getRole())))) {
                err("An element with \u201Crole=row\u201D requires \u201Crole=treegrid\u201D or \u201Crole=grid\u201D on the parent or grandparent.");
            }
        }

        // ARIA IDREFS
        for (String att : MUST_NOT_DANGLE_IDREFS) {
            String attVal = atts.getValue("", att);
            if (attVal != null) {
                String[] tokens = AttributeUtil.split(attVal);
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    ariaReferences.add(new IdrefLocator(getDocumentLocator(),
                            token, att));
                }
            }
        }
        allIds.addAll(ids);

        // activedescendant
        for (Iterator<Map.Entry<StackNode, Locator>> iterator = openActiveDescendants.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<StackNode, Locator> entry = iterator.next();
            if (ids.contains(entry.getKey().getActiveDescendant())) {
                iterator.remove();
            }
        }

        if ("http://www.w3.org/1999/xhtml" == uri) {
            int number = specialAncestorNumber(localName);
            if (number > -1) {
                ancestorMask |= (1 << number);
            }
            if ("a" == localName && href) {
                ancestorMask |= HREF_MASK;
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            if ("select" == localName && atts.getIndex("", "multiple") == -1) {
                openSingleSelects.put(child, getDocumentLocator());
            } else if ("label" == localName) {
                openLabels.put(child, new LocatorImpl(getDocumentLocator()));
            }
            push(child);
        } else {
            StackNode child = new StackNode(ancestorMask, null, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            push(child);
        }

    }

    private void processChildContent(StackNode parent) throws SAXException {
        if (parent == null) {
            return;
        }
        parent.setChildren();
    }

    /**
     * @see org.whattf.checker.Checker#characters(char[], int, int)
     */
    @Override public void characters(char[] ch, int start, int length)
            throws SAXException {
        for (int i = start; i < length; i++) {
            char c = ch[i];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                default:
                    processChildContent(peek());
                    return;
            }
        }
    }

    private CharSequence renderRoleSet(Set<String> requiredParents) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String role : requiredParents) {
            if (first) {
                first = false;
            } else {
                sb.append(" or ");
            }
            sb.append("\u201Crole=");
            sb.append(role);
            sb.append('\u201D');
        }
        return sb;
    }

}
