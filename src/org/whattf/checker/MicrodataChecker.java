/*
 * Copyright (c) 2011 Mozilla Foundation
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

package org.whattf.checker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Checker for microdata constraints that require tree traversal.
 *
 * The SAX events are used to construct a minimal tree with only the
 * relevant elements and attributes, which is then validated.
 *
 * The followings constraints are supported:
 *
 * - The itemref attribute, if specified, must have a value that is an
 *   unordered set of unique space-separated tokens that are
 *   case-sensitive, consisting of IDs of elements in the same home
 *   subtree.
 *
 * - A document must not contain any items for which the algorithm to
 *   find the properties of an item finds any microdata errors.
 *
 * - All itemref attributes in a Document must be such that there are
 *   no cycles in the graph formed from representing each item in the
 *   Document as a node in the graph and each property of an item
 *   whose value is another item as an edge in the graph connecting
 *   those two items.
 *
 * - A document must not contain any elements that have an itemprop
 *   attribute that would not be found to be a property of any of the
 *   items in that document were their properties all to be determined
 *
 * Not all checks are unconditional. For example, the itemref
 * constraints are only checked if the itemscope attribute is
 * present. However, the checks are chained such that if a document
 * validates with no errors, then all the constraints are satisfied.
 */
public class MicrodataChecker extends Checker {
    /**
     * The relevant aspects of an HTML element.
     *
     * There is no Document class as there is no need for a root
     * element from which all Elements can be reached.
     */
    class Element {
        public final Locator locator;
        public final String[] itemProp;
        public final String[] itemRef;
        public final boolean itemScope;

        public final List<Element> children;

        // tree order of the element, for cheap sorting and hashing
        private final int order;

        public Element(Locator locator, String[] itemProp, String[] itemRef, boolean itemScope) {
            this.locator = locator;
            this.itemProp = itemProp;
            this.itemRef = itemRef;
            this.itemScope = itemScope;
            this.children = new LinkedList<Element>();
            this.order = counter++;
        }

        @Override public boolean equals(Object that) {
            return this == that;
        }

        @Override public int hashCode() {
            return order;
        }

        /**
         * Helper for building the Element tree(s).
         */
        class Builder {
            public final Builder parent;
            public final int depth; // nesting depth in the input

            public Builder(Builder parent, int depth) {
                this.parent = parent;
                this.depth = depth;
            }

            public void appendChild(Element elm) {
                Element.this.children.add(elm);
            }
        }
    }

    private int depth; // nesting depth in the input
    private Element.Builder builder;
    private static int counter;

    // top-level items (itemscope but not itemprop)
    private List<Element> items;
    // property elements (itemprop)
    private Set<Element> properties;
    // mapping from id to Element (like getElementById)
    private Map<String, Element> idmap;

    private Locator locator;

    /**
     * @see org.whattf.checker.Checker#reset()
     */
    @Override
    public void reset() {
        depth = 0;
        builder = null;
        counter = 0;
        items = new LinkedList<Element>();
        properties = new LinkedHashSet<Element>();
        idmap = new HashMap<String, Element>();
    }

     /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        depth++;

        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }

        String id = null;
        String[] itemProp = null;
        String[] itemRef = null;
        boolean itemScope = false;

        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            if (atts.getURI(i).isEmpty()) {
                String attLocal = atts.getLocalName(i);
                String attValue = atts.getValue(i);
                if ("id" == attLocal) {
                    id = attValue;
                } else if ("itemprop" == attLocal) {
                    itemProp = AttributeUtil.split(attValue);
                } else if ("itemref" == attLocal) {
                    itemRef = AttributeUtil.split(attValue);
                } else if ("itemscope" == attLocal) {
                    itemScope = true;
                }
            }
        }

        if (id != null || itemProp != null || itemScope == true) {
            Element elm = new Element(new LocatorImpl(locator), itemProp, itemRef, itemScope);

            if (itemProp != null) {
                properties.add(elm);
            } else if (itemScope) {
                items.add(elm);
            }
            if (!idmap.containsKey(id)) {
                idmap.put(id, elm);
            }

            if (builder != null) {
                builder.appendChild(elm);
            }
            builder = elm.new Builder(builder, depth);
        }
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (builder != null && builder.depth == depth) {
            builder = builder.parent;
        }
        depth--;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        // check all top-level items
        for (Element item : items) {
            checkItem(item, new ArrayDeque<Element>());
        }

        // emit errors for unreferenced properties
        for (Element prop : properties) {
            err("The \u201Citemprop\u201D attribute was specified, but the element is not a property of any item.", prop.locator);
        }
    }

    /**
     * Check itemref constraints.
     *
     * This mirrors the "the properties of an item" algorithm,
     * modified to recursively check sub-items.
     *
     * http://www.whatwg.org/specs/web-apps/current-work/multipage/microdata.html#the-properties-of-an-item
     */
    private void checkItem(Element root, Deque<Element> parents) throws SAXException {
        Deque<Element> pending = new ArrayDeque<Element>();
        Set<Element> memory = new HashSet<Element>();
        memory.add(root);
        for (Element child : root.children) {
            pending.push(child);
        }
        if (root.itemRef != null) {
            for (String id : root.itemRef) {
                Element refElm = idmap.get(id);
                if (refElm != null) {
                    pending.push(refElm);
                } else {
                    err("The \u201Citemref\u201D attribute referenced \u201C" + id + "\u201D, but there is no element with an \u201Cid\u201D attribute with that value.", root.locator);
                }
            }
        }
        boolean memoryError = false;
        while (pending.size() > 0) {
            Element current = pending.pop();
            if (memory.contains(current)) {
                memoryError = true;
                continue;
            }
            memory.add(current);
            if (!current.itemScope) {
                for (Element child : current.children) {
                    pending.push(child);
                }
            }
            if (current.itemProp != null) {
                properties.remove(current);
                if (current.itemScope) {
                    if (!parents.contains(current)) {
                        parents.push(root);
                        checkItem(current, parents);
                        parents.pop();
                    } else {
                        err("The \u201Citemref\u201D attribute created a circular reference with another item.", current.locator);
                    }
                }
            }
        }
        if (memoryError) {
            err("The \u201Citemref\u201D attribute contained redundant references.", root.locator);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
}
