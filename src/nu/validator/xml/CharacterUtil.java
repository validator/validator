/*
 * Copyright (c) 2005 Marko Karppinen & Co. LLC
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

package nu.validator.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id$
 * @author hsivonen
 */
public class CharacterUtil {

    private final static Pattern MINIMAL = Pattern.compile("[^\\x09\\x0A\\x0D\\u0020-\\uFFFD\\uD800-\\uDBFF\\uDC00–\\uDFFF]");

    // FIXME include UTF-16 representations of U+?FFFE and U+?FFFF.
    private final static Pattern PRUDENT = Pattern.compile("[^\\x09\\x0A\\x0D\\u0020-\\uFFFD\\uD800-\\uDBFF\\uDC00–\\uDFFF]|\\uFEFF|[\\x7F-\\x9F]|[\\uFDD0-\\uFDDF]");
    
    public static String scrubCharacterData(CharSequence data) {
        Matcher m = MINIMAL.matcher(data);
        return m.replaceAll("");
    }
    public static String prudentlyScrubCharacterData(CharSequence data) {
        Matcher m = PRUDENT.matcher(data);
        return m.replaceAll("");
    }    
}
