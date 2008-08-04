/*
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.svgresearch;

public class NameTriple {
    
    public static final NameTriple ANY_MARKER = new NameTriple();
    
    private final String local;
    
    private final String prefix;
    
    private final String uri;

    /**
     * @param local
     * @param prefix
     * @param uri
     */
    public NameTriple(String local, String qname, String uri) {
        this.local = local;
        this.prefix = prefixFromQname(qname);
        this.uri = uri;
    }

    public NameTriple() {
        this.local = "";
        this.prefix = "";
        this.uri = "";
    }

    private static String prefixFromQname(String qname) {
        int index = qname.indexOf(':');
        if (index == -1) {
            return "";
        } else {
            return qname.substring(0, index).intern();
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override public boolean equals(Object other) {
        if (other instanceof NameTriple) {
            NameTriple otherNT = (NameTriple) other;
            return this.local == otherNT.local && this.prefix == otherNT.prefix && this.uri == otherNT.uri;
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() {
        return local.hashCode() ^ prefix.hashCode() ^ uri.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        if (this == ANY_MARKER) {
            return "ANY";
        }
        return uri + ' ' + prefix + ' ' + local;
    }

    /**
     * Returns the local.
     * 
     * @return the local
     */
    public String getLocal() {
        return local;
    }

    /**
     * Returns the prefix.
     * 
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the uri.
     * 
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
    
    
}
