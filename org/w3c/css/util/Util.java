//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * Be Careful this version is not the original version.
 * I modified some sources.          Philippe Le Hegaret
 *
 * @(#)Util.java                    0.2-2 23/03/1997
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996,1997  Ronald Tschalaer
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *  Ronald.Tschalaer@psi.ch
 *
 */

package org.w3c.css.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds various utility methods.
 *
 * @version $Revision$
 */

public final class Util {
    // Constructors

    /**
     * This class isn't meant to be instantiated.
     */
    private Util() {
    }

    private static Pattern uri_param = Pattern.compile("[&?][^&?=]+=([^&?]+)");

    //unravel uris contained in querystrings by url decoding and then looking for all sub URIs
    // like http://www.example.com/foo?bar=http%3A%2F%2fwww.example.org%2Ftoto
    // ->  http://www.example.com/foo?bar=http%3A%2F%2fwww.example.org%2Ftoto
    // and http://www.example.org/toto
    private static ArrayList<String> parseURIs(String uri) {
        ArrayList<String> uris = new ArrayList<String>();
        int pos = 0;
        uris.add(uri);
        // we avoid recursion by putting stuff on our plate at the right place for processing
        while (pos < uris.size()) {
            String u = uris.get(pos++);
            Matcher m = uri_param.matcher(u);
            while (m.find()) {
                try {
                    String compound = URLDecoder.decode(m.group(1), "UTF-8");
                    if (compound.contains("://")) {
                        uris.add(compound);
                    }
                } catch (UnsupportedEncodingException e) {
                    // it is supported
                }
            }
        }
        return uris;
    }

    public static boolean checkURI(String uri) {
        ArrayList<String> uris = parseURIs(uri);
        // now let's check if there is a recursive call
        for (String u : uris) {
            int qm = u.indexOf('?');
            if (qm != -1) {
                u = u.substring(0, qm);
            }
            u.toLowerCase();
            // TODO: use a list of forbidden URIs
            if (u.startsWith("http://jigsaw.w3.org/css-validator/validator")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an array of distances to speed up the search in findStr().
     * The returned array should be passed as the second argument to
     * findStr().
     *
     * @param search the search string (same as the first argument to
     *               findStr()).
     * @return an array of distances (to be passed as the second argument to
     * findStr()).
     */
    public final static int[] compile_search(byte[] search) {
        int[] cmp = {0, 1, 0, 1, 0, 1};
        int end;

        for (int idx = 0; idx < search.length; idx++) {
            for (end = idx + 1; end < search.length; end++) {
                if (search[idx] == search[end]) break;
            }
            if (end < search.length) {
                if ((end - idx) > cmp[1]) {
                    cmp[4] = cmp[2];
                    cmp[5] = cmp[3];
                    cmp[2] = cmp[0];
                    cmp[3] = cmp[1];
                    cmp[0] = idx;
                    cmp[1] = end - idx;
                } else if ((end - idx) > cmp[3]) {
                    cmp[4] = cmp[2];
                    cmp[5] = cmp[3];
                    cmp[2] = idx;
                    cmp[3] = end - idx;
                } else if ((end - idx) > cmp[5]) {
                    cmp[4] = idx;
                    cmp[5] = end - idx;
                }
            }
        }

        cmp[1] += cmp[0];
        cmp[3] += cmp[2];
        cmp[5] += cmp[4];
        return cmp;
    }

    /**
     * Search for a string. Use compile_search() to first generate the second
     * argument.
     *
     * @param search the string to search for.
     * @param cmp    the the array returned by compile_search.
     * @param str    the string in which to look for <var>search</var>.
     * @param begin  the position at which to start the search in
     *               <var>str</var>.
     * @param end    the position at which to end the search in <var>str</var>.
     * @return the position in <var>str</var> where <var>search</var> was
     * found, or -1 if not found.
     */
    public final static int findStr(byte[] search, int[] cmp, byte[] str,
                                    int begin, int end) {
        int beg = begin;
        int c1f = cmp[0];
        int c1l = cmp[1];
        int d1 = c1l - c1f;
        int c2f = cmp[2];
        int c2l = cmp[3];
        int d2 = c2l - c2f;
        int c3f = cmp[4];
        int c3l = cmp[5];
        int d3 = c3l - c3f;

        Find:
        while (beg + search.length <= end) {
            if (search[c1l] == str[beg + c1l]) {
                Comp:
                if (search[c1f] == str[beg + c1f]) {
                    for (int idx = 0; idx < search.length; idx++)
                        if (search[idx] != str[beg + idx]) break Comp;

                    break Find;     // we found it
                }
                beg += d1;
            } else if (search[c2l] == str[beg + c2l])
                beg += d2;
            else if (search[c3l] == str[beg + c3l])
                beg += d3;
            else
                beg++;
        }

        if (beg + search.length > end)
            return -1;
        else
            return beg;
    }

    /**
     * Print a message on System.err only if the user wants debug trace.
     */
    public static final void verbose(String s) {
        if (onDebug) {
            System.err.println(s);
        }
    }

    /**
     * Suppressed all white spaces in the beginning of the string
     */
    public static final String suppressWhiteSpace(String s) {
        if (s != null) {
            int len = s.length();
            int i = 0;
            while (i < len && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            if (i == 0) {
                return s;
            } else {
                return (i == len) ? null : s.substring(i);
            }
        }
        return null;
    }

    /**
     * Suppress all white spaces
     *
     * @param s the string.
     */
    public final static String strip(String s) {
        int index = 0;
        char[] news = new char[s.length()];

        for (char c : s.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                news[index++] = c;
            }
        }
        return new String(news, 0, index);
    }

    /**
     * Display a float without .0 if necessary
     */
    public final static String displayFloat(Float value) {
        int intValue = value.intValue();
        float floatValue = value.floatValue();
        if (((float) intValue) == floatValue) {
            return Integer.toString(intValue, 10);
        } else {
            return value.toString();
        }
    }

    /**
     * Display a float without .0 if necessary
     */
    public final static String displayFloat(float value) {
        int intValue = (int) value;
        if (((float) intValue) == value) {
            return Integer.toString(intValue, 10);
        } else {
            return Float.toString(value);
        }
    }

    /**
     * Replaces characters that may be confused by a HTML
     * parser with their equivalent character entity references
     * to prevent inserted code to be executed while displaying
     * the validation results in HTML format.
     * <p/>
     * This method will replace HTML characters such as &gt; with their
     * HTML entity reference (&amp;gt;) so that the html parser will
     * be sure to interpret them as plain text rather than HTML or script.
     * <p/>
     *
     * @param s String to be escaped
     * @return escaped String
     */
    public static String escapeHTML(String s) {
        if (s == null) {
            return null;
        }
        int length = s.length();
        int newLength = length;
        boolean someCharacterEscaped = false;
        // first check for characters that might
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            int cint = 0xffff & c;
            if (cint < 32) {
                switch (c) {
                    case '\r':
                    case '\n':
                    case '\t':
                    case '\f': {
                    }
                    break;
                    default: {
                        newLength -= 1;
                        someCharacterEscaped = true;
                    }
                }
            } else {
                switch (c) {
                    case '\"': {
                        newLength += 5;
                        someCharacterEscaped = true;
                    }
                    break;
                    case '&':
                    case '\'': {
                        newLength += 4;
                        someCharacterEscaped = true;
                    }
                    break;
                    case '<':
                    case '>': {
                        newLength += 3;
                        someCharacterEscaped = true;
                    }
                    break;
                }
            }
        }
        if (!someCharacterEscaped) {
            // nothing to escape in the string
            return s;
        }
        StringBuilder sb = new StringBuilder(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            int cint = 0xffff & c;
            if (cint < 32) {
                switch (c) {
                    case '\r':
                    case '\n':
                    case '\t':
                    case '\f': {
                        sb.append(c);
                    }
                    break;
                    default: {
                        // Remove this character
                    }
                }
            } else {
                switch (c) {
                    case '\"': {
                        sb.append("&quot;");
                    }
                    break;
                    case '\'': {
                        sb.append("&#39;");
                    }
                    break;
                    case '&': {
                        sb.append("&amp;");
                    }
                    break;
                    case '<': {
                        sb.append("&lt;");
                    }
                    break;
                    case '>': {
                        sb.append("&gt;");
                    }
                    break;
                    default: {
                        sb.append(c);
                    }
                }
            }

        }
        return sb.toString();
    }


    /**
     * <code>true</code> if the validator runs in a servlet (security pbs)
     */
    public static boolean servlet;

    /**
     * <code>true</code> if the validator can import URL.
     */
    public static boolean importSecurity;

    /**
     * <code>true</code> if the validator doesn't need errors
     */
    public static boolean noErrorTrace;

    /**
     * <code>true</code> if the input is a HTML file
     *
     * @@BUG in thread 'coz this a static variable ... :-]
     */
    public static boolean fromHTMLFile;

    /**
     * <code>true</code> if the user wants debug traces.
     *
     * @see #verbose
     */
    public static boolean onDebug = Boolean.getBoolean("CSS.debug");
    //    public static boolean onDebug = false;

    // default maximum size for checked entities
    public static long maxEntitySize = 1048576L;
}

