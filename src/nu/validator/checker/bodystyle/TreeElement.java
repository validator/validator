/*
 * Copyright (c) 2026 Mozilla Foundation
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


package nu.validator.checker.bodystyle;

import org.xml.sax.Attributes;

/**
 * A lightweight record of one element in the document tree, kept so that
 * selectors can be matched against everything parsed before a "style"
 * element in "body".
 */
final class TreeElement {

    private static final String[] NO_ATTRIBUTES = new String[0];

    final String namespace;

    final String localName;

    final boolean isHtml;

    final TreeElement parent;

    /**
     * The element's position in tree order among all recorded elements.
     */
    final int index;

    TreeElement firstChild;

    TreeElement lastChild;

    TreeElement previousSibling;

    TreeElement nextSibling;

    /**
     * True once the element's end tag has been seen — so that no more
     * children can be added to it.
     */
    boolean closed;

    boolean hasText;

    boolean hasNonWhitespaceText;

    /**
     * Attributes as (namespace, local name, value) triples.
     */
    private final String[] attributes;

    private String[] classes;

    TreeElement(String namespace, String localName, Attributes atts,
            TreeElement parent, int index) {
        this.namespace = namespace;
        this.localName = localName;
        this.isHtml = "http://www.w3.org/1999/xhtml".equals(namespace);
        this.parent = parent;
        this.index = index;
        int len = atts == null ? 0 : atts.getLength();
        if (len == 0) {
            attributes = NO_ATTRIBUTES;
        } else {
            attributes = new String[len * 3];
            for (int i = 0; i < len; i++) {
                attributes[i * 3] = atts.getURI(i);
                attributes[i * 3 + 1] = atts.getLocalName(i);
                attributes[i * 3 + 2] = atts.getValue(i);
            }
        }
        if (parent != null) {
            if (parent.lastChild == null) {
                parent.firstChild = this;
            } else {
                parent.lastChild.nextSibling = this;
                previousSibling = parent.lastChild;
            }
            parent.lastChild = this;
        }
    }

    int attributeCount() {
        return attributes.length / 3;
    }

    String attributeNamespace(int i) {
        return attributes[i * 3];
    }

    String attributeLocalName(int i) {
        return attributes[i * 3 + 1];
    }

    String attributeValue(int i) {
        return attributes[i * 3 + 2];
    }

    /**
     * Returns the value of the attribute with the given local name and no
     * namespace, or null if there's no such attribute.
     */
    String getAttribute(String name) {
        for (int i = 0; i + 2 < attributes.length; i += 3) {
            if (attributes[i].isEmpty() && attributes[i + 1].equals(name)) {
                return attributes[i + 2];
            }
        }
        return null;
    }

    /**
     * Returns the value of the attribute with the given namespace and local
     * name, or null if there's no such attribute.
     */
    String getAttribute(String namespace, String name) {
        for (int i = 0; i + 2 < attributes.length; i += 3) {
            if (attributes[i].equals(namespace)
                    && attributes[i + 1].equals(name)) {
                return attributes[i + 2];
            }
        }
        return null;
    }

    boolean hasClass(String name) {
        if (classes == null) {
            String value = getAttribute("class");
            classes = value == null ? NO_ATTRIBUTES
                    : SelectorMatcher.splitOnWhitespace(value);
        }
        for (String c : classes) {
            if (c.equals(name)) {
                return true;
            }
        }
        return false;
    }

    boolean hasSameTypeAs(TreeElement other) {
        return localName.equals(other.localName)
                && namespace.equals(other.namespace);
    }

    /**
     * True if no more siblings can be added after this element — either
     * because its parent is closed, or because it's the root element.
     */
    boolean isParentClosed() {
        return parent == null || parent.closed;
    }
}
