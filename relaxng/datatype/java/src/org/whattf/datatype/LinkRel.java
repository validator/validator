/*
 * Copyright (c) 2013 Mozilla Foundation
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

package org.whattf.datatype;

import java.util.Arrays;

public class LinkRel extends AbstractRel {

    private static final String[] REGISTERED_TOKENS = {
        "alternate",
        "appendix", // HTML4
        "apple-touch-icon", // extension
        "apple-touch-icon-precomposed", // extension
        "apple-touch-startup-image", // extension
        "author",
        "canonical", // extension
        "category", // extension
        "chapter", // HTML4
        "contents", // HTML4
        "copyright", // HTML4
        "discussion", // extension
        "dns-prefetch", // extension
        "edituri", // extension
        "glossary", // HTML4
        "help",
        "home", // extension
        "http://docs.oasis-open.org/ns/cmis/link/200908/acl", // extension
        "icon",
        "image_src", // extension
        "index", // extension
        "issues", // extension
        "its-rules", // extension
        "license",
        "me", // extension (Formats table)
        "next",
        "openid.delegate", // extension
        "openid.server", // extension
        "openid2.local_id", // extension
        "openid2.provider", // extension
        "p3pv1", // extension
        "pgpkey", // extension
        "pingback", // extension
        "prefetch",
        "prerender", // extension
        "prev",
        "previous", // HTML4
        "profile", // extension
        "schema.dc", // extension
        "schema.dcterms", // extension
        "search",
        "section", // HTML4
        "service", // extension
        "shortcut", // extension
        "shortlink", // extension
        "sidebar", // extension
        "start", // HTML4
        "stylesheet",
        "stylesheet/less", // extension
        "subsection", // HTML4
        "syndication", // extension
        "timesheet", // extension
        "toc", // HTML4
        "transformation", // extension (Formats table)
        "widget", // extension
        "wlwmanifest" // extension
    };
        
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

    @Override protected boolean isRegistered(String token) {
        return Arrays.binarySearch(REGISTERED_TOKENS, token) >= 0;
    }

    @Override public String getName() {
        return "link type valid for <link>";
    }

}
