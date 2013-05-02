/*
 * Copyright (c) 2005 Petr Nalevka
 * Copyright (c) 2013 Mozilla Foundation
 *
 * Ported to Java from a set of Schematron assertiongs mechanically
 * extracted from RelaxNG files which had the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.whattf.checker.schematronequiv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.whattf.checker.Checker;
import org.whattf.checker.LocatorImpl;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class Html4Assertions extends Checker {

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

    private static final String[] SPECIAL_ANCESTORS = { "a", "button", "form",
            "label", "pre" };

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
        registerProhibitedAncestor("a", "a");
        registerProhibitedAncestor("button", "a");
        registerProhibitedAncestor("button", "button");
        registerProhibitedAncestor("button", "fieldset");
        registerProhibitedAncestor("button", "form");
        registerProhibitedAncestor("button", "iframe");
        registerProhibitedAncestor("button", "input");
        registerProhibitedAncestor("button", "isindex");
        registerProhibitedAncestor("button", "select");
        registerProhibitedAncestor("button", "textarea");
        registerProhibitedAncestor("form", "form");
        registerProhibitedAncestor("label", "label");
        registerProhibitedAncestor("pre", "pre");
        registerProhibitedAncestor("pre", "img");
        registerProhibitedAncestor("pre", "object");
        registerProhibitedAncestor("pre", "applet");
        registerProhibitedAncestor("pre", "big");
        registerProhibitedAncestor("pre", "small");
        registerProhibitedAncestor("pre", "sub");
        registerProhibitedAncestor("pre", "sup");
        registerProhibitedAncestor("pre", "font");
    }

    private static final int BUTTON_MASK = (1 << specialAncestorNumber("button"));

    private static final int LABEL_FOR_MASK = (1 << 28);

    private class IdrefLocator {
        private final Locator locator;

        private final String idref;

        /**
         * @param locator
         * @param idref
         */
        public IdrefLocator(Locator locator, String idref) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
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
    }

    private class StackNode {
        private final int ancestorMask;

        private boolean selectedOptions = false;

        private boolean optionFound = false;

        /**
         * @param ancestorMask
         */
        public StackNode(int ancestorMask, String name, String role,
                String activeDescendant, String forAttr) {
            this.ancestorMask = ancestorMask;
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
         * Returns the optionFound.
         *
         * @return the optionFound
         */
        public boolean hasOption() {
            return optionFound;
        }

        /**
         * Sets the optionFound.
         */
        public void setOptionFound() {
            this.optionFound = true;
        }

    }

    private StackNode[] stack;

    private int currentPtr;

    public Html4Assertions() {
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

    private LinkedHashSet<IdrefLocator> formControlReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> formControlIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> listIds = new HashSet<String>();

    private Set<String> allIds = new HashSet<String>();

    /**
     * @see org.whattf.checker.Checker#endDocument()
     */
    @Override public void endDocument() throws SAXException {
        // label for
        for (IdrefLocator idrefLocator : formControlReferences) {
            if (!formControlIds.contains(idrefLocator.getIdref())) {
                err("The \u201Cfor\u201D attribute of the "
                        + "\u201Clabel\u201D element must refer to "
                        + "a form control.", idrefLocator.getLocator());
            }
        }

        reset();
        stack = null;
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override public void endElement(String uri, String localName, String name)
            throws SAXException {
        StackNode node = pop();
        openSingleSelects.remove(node);
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("option" == localName && !stack[currentPtr].hasOption()) {
                stack[currentPtr].setOptionFound();
            }
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
        formControlReferences.clear();
        formControlIds.clear();
        listReferences.clear();
        listIds.clear();
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
        boolean hreflang = false;

        StackNode parent = peek();
        int ancestorMask = 0;
        if (parent != null) {
            ancestorMask = parent.getAncestorMask();
        }
        if ("http://www.w3.org/1999/xhtml" == uri) {
            boolean hidden = false;
            boolean usemap = false;
            boolean selected = false;
            String xmlLang = null;
            String lang = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attUri = atts.getURI(i);
                if (attUri.length() == 0) {
                    String attLocal = atts.getLocalName(i);
                    if ("href" == attLocal) {
                        href = true;
                    } else if ("hreflang" == attLocal) {
                        hreflang = true;
                    } else if ("lang" == attLocal) {
                        lang = atts.getValue(i);
                    } else if ("for" == attLocal && "label" == localName) {
                        forAttr = atts.getValue(i);
                        ancestorMask |= LABEL_FOR_MASK;
                    } else if ("selected" == attLocal) {
                        selected = true;
                    } else if ("usemap" == attLocal && "input" != localName) {
                        usemap = true;
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

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "";
            if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(localName)) != null) {
                mask = maskAsObject.intValue();
                descendantUiString = localName;
            } else if ("img" == localName && usemap) {
                mask = BUTTON_MASK;
                descendantUiString = "img\u201D with the attribute \u201Cusemap";
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (int j = 0; j < SPECIAL_ANCESTORS.length; j++) {
                        if ((maskHit & 1) != 0) {
                            err("The element \u201C"
                                    + descendantUiString
                                    + "\u201D must not appear as a descendant "
                                    + "of the element \u201C"
                                    + SPECIAL_ANCESTORS[j] + "\u201D.");
                        }
                        maskHit >>= 1;
                    }
                }
            }

            // lang and xml:lang
            if (lang != null
                    && (xmlLang == null || !equalsIgnoreAsciiCase(lang, xmlLang))) {
                err("When attribute \u201Clang\u201D in no namespace "
                        + "is specified, attribute \u201Clang\u201D in the XML "
                        + "namespace must also be specified, and both "
                        + "attributes must have the same value.");
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
                    || "select" == localName || "button" == localName) {
                formControlIds.addAll(ids);
            }

            // input@type=radio or input@type=checkbox
            if ("input" == localName
                    && (lowerCaseLiteralEqualsIgnoreAsciiCaseString("radio",
                            atts.getValue("", "type")) || lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                            "checkbox", atts.getValue("", "type")))) {
                if (atts.getValue("", "value") == null
                        || "".equals(atts.getValue("", "value"))) {
                    err("Element \u201Cinput\u201D with attribute "
                            + "\u201Ctype\u201D whose value is \u201Cradio\u201D "
                            + "or \u201Ccheckbox\u201D "
                            + "must have non-empty attribute \u201Cvalue\u201D.");
                }
            }

            // multiple selected options
            if ("option" == localName && selected) {
                for (Map.Entry<StackNode, Locator> entry : openSingleSelects.entrySet()) {
                    StackNode node = entry.getKey();
                    if (node.isSelectedOptions()) {
                        err("The \u201Cselect\u201D element must not have more "
                            + "than one selected \u201Coption\u201D descendant "
                            + "unless the \u201Cmultiple\u201D attribute is specified.");
                    } else {
                        node.setSelectedOptions();
                    }
                }
            }
        }

        if ("http://www.w3.org/1999/xhtml" == uri) {
            int number = specialAncestorNumber(localName);
            if (number > -1) {
                ancestorMask |= (1 << number);
            }
            if ("a" == localName && hreflang && !href) {
                err("Element \u201Ca\u201D with attribute "
                    + "\u201Chreflang\u201D must have "
                    + "\u201Chref\u201D attribute.");
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    activeDescendant, forAttr);
            if ("select" == localName && atts.getIndex("", "multiple") == -1) {
                openSingleSelects.put(child, getDocumentLocator());
            }
            push(child);
        }
    }

}
