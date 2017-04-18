/*
 * Copyright (c) 2013-2017 Mozilla Foundation
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

package nu.validator.datatype;

import nu.validator.datatype.tools.RegisteredRelValuesBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.relaxng.datatype.DatatypeException;
import org.xml.sax.SAXException;

public final class LinkRel extends AbstractRel {

    private static final HashSet<String> registeredValues;

    static {
        try {
            RegisteredRelValuesBuilder.parseRegistry();
            registeredValues = RegisteredRelValuesBuilder.getLinkRelValues();
            // Standard rel values for <link> from the spec
            registeredValues.add("alternate");
            registeredValues.add("author");
            registeredValues.add("dns-prefetch");
            registeredValues.add("help");
            registeredValues.add("icon");
            registeredValues.add("license");
            registeredValues.add("mask-icon");
            registeredValues.add("next");
            registeredValues.add("pingback");
            registeredValues.add("preconnect");
            registeredValues.add("prefetch");
            registeredValues.add("preload");
            registeredValues.add("prerender");
            registeredValues.add("prev");
            registeredValues.add("search");
            registeredValues.add("serviceworker");
            registeredValues.add("stylesheet");
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The singleton instance.
     */
    public static final LinkRel THE_INSTANCE = new LinkRel();

    /**
     * Package-private constructor
     */
    private LinkRel() {
        super();
    }

    private final static boolean WARN = System.getProperty(
            "nu.validator.datatype.warn", "").equals("true");

    @Override
    protected boolean isRegistered(CharSequence literal, String token)
            throws DatatypeException {
        if ("shortcut".equals(toAsciiLowerCase(token))) {
            if ("shortcut icon".equals(toAsciiLowerCase(literal))) {
                return true;
            } else {
                throw new DatatypeException("If the \u201cshortcut\u201d"
                        + " keyword is present, the \u201crel\u201d"
                        + " attribute's entire value must be"
                        + " \u201cshortcut icon\u201d.");
            }
        } else {
            if (WARN) {
                // Synonyms for current keywords
                Map<String, String> map = new HashMap<>();
                map.put("copyright", "license");
                map.put("previous", "prev");
                errSynonym(token, map);
            }
            return registeredValues.contains(token.toLowerCase());
        }
    }

    @Override
    public String getName() {
        return "list of link-type keywords";
    }

}
