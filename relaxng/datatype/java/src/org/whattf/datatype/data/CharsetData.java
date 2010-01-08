/*
 * Copyright (c) 2008-2010 Mozilla Foundation
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

package org.whattf.datatype.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CharsetData {

    private static final String NAME = "name: ";

    private static final String ALIAS = "alias: ";

    private static final String PREFERRED_MIME_NAME = "preferred mime name";

    private static final String SPACE = " ";

    private static final String[] EMPTY_STRING_ARRAY = {};

    private BufferedReader in;

    private SortedSet<String> preferredSet = new TreeSet<String>();

    private Map<String, String> nameByAliasMap = new HashMap<String, String>();

    private String[] preferred = null;

    public CharsetData() throws IOException {
        super();
        URL url = new URL(System.getProperty(
                "org.whattf.datatype.charset-registry",
                "http://www.iana.org/assignments/character-sets"));
        in = new BufferedReader(
                new InputStreamReader(url.openStream(), "UTF-8"));
        consumeRegistry();
        preferred = preferredSet.toArray(EMPTY_STRING_ARRAY);
    }

    private void consumeRegistry() throws IOException {
        while (consumeRecord()) {
            // spin
        }
        in.close();
    }

    private boolean consumeRecord() throws IOException {
        boolean hasMore = true;
        boolean noPreferred = true;
        String name = null;
        String alias = null;
        String line = null;
        for (;;) {
            line = in.readLine();
            if (line == null) {
                hasMore = false;
                break;
            }
            line = line.toLowerCase();
            if ("".equals(line)) {
                break;
            } else if (line.startsWith(NAME)) {
                if (line.indexOf(PREFERRED_MIME_NAME) != -1) {
                    name = line.substring(NAME.length()).split(SPACE)[0].trim().intern();
                    preferredSet.add(name);
                    noPreferred = false;
                } else if (line.substring(NAME.length()).trim().indexOf(SPACE) != -1) {
                    name = line.substring(NAME.length()).split(SPACE)[0].trim().intern();
                } else {
                    name = line.substring(NAME.length()).trim().intern();
                }
            } else if (line.startsWith(ALIAS)) {
                if (line.indexOf(PREFERRED_MIME_NAME) != -1) {
                    name = line.substring(ALIAS.length()).split(" ")[0].trim().intern();
                    preferredSet.add(name);
                    noPreferred = false;
                } else {
                    alias = line.substring(ALIAS.length()).trim().intern();
                    if (!"none".equals(alias)) {
                        nameByAliasMap.put(alias, name);
                    }
                }
            }
        }
        if (name != null && noPreferred) {
            preferredSet.add(name);
        }
        return hasMore;
    }

    /**
     * Returns the preferred.
     * 
     * @return the preferred
     */
    public String[] getPreferred() {
        return preferred;
    }

    /**
     * Returns the nameByAliasMap.
     * 
     * @return the nameByAliasMap
     */
    public Map<String, String> getNameByAliasMap() {
        return nameByAliasMap;
    }
}
