/*
 * Copyright (c) 2006 Henri Sivonen
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

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;
import org.whattf.datatype.data.LanguageData;

/**
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Language extends AbstractDatatype {

    private static final Pattern HYPHEN = Pattern.compile("-");
    
    private static String[] languages = null;
    
    private static String[] scripts = null;
    
    private static String[] regions = null;
    
    private static String[] variants = null;
    
    private static int[] suppressedScriptByLanguage = null;
    
    private static String[][] prefixesByVariant = null;
    
    static {
        try {
            LanguageData data = new LanguageData();
            languages = data.getLanguages();
            scripts = data.getScripts();
            regions = data.getRegions();
            variants = data.getScripts();
            suppressedScriptByLanguage = data.getSuppressedScriptByLanguage();
            prefixesByVariant = data.getPrefixesByVariant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * List extracted from http://www.iana.org/assignments/language-tags on
     * 2006-04-13. List dated 2005-09-09.
     */
    private static final String[] GRANDFATHERED = { "art-lojban", // deprecated
            "az-arab", "az-cyrl", "az-latn", "be-latn", "bs-cyrl", "bs-latn",
            "cel-gaulish", "de-1901", "de-1996", "de-at-1901", "de-at-1996",
            "de-ch-1901", "de-ch-1996", "de-de-1901", "de-de-1996", "en-boont",
            "en-gb-oed", "en-scouse", "es-419", "i-ami", "i-bnn", "i-default", // inappropriate
            // for
            // HTML5?
            "i-enochian", "i-hak", // deprecated
            "i-klingon", // deprecated
            "i-lux", // deprecated
            "i-mingo", "i-navajo", // deprecated
            "i-pwn", "i-tao", "i-tay", "i-tsu", "iu-cans", "iu-latn",
            "mn-cyrl", "mn-mong", "no-bok", // deprecated
            "no-nyn", // deprecated
            "sgn-be-fr", "sgn-be-nl", "sgn-br", "sgn-ch-de", "sgn-co",
            "sgn-de", "sgn-dk", "sgn-es", "sgn-fr", "sgn-gb", "sgn-gr",
            "sgn-ie", "sgn-it", "sgn-jp", "sgn-mx", "sgn-ni", "sgn-nl",
            "sgn-no", "sgn-pt", "sgn-se", "sgn-us", "sgn-za", "sl-nedis",
            "sl-rozaj", "sr-cyrl", "sr-latn", "tg-arab", "tg-cyrl", "uz-cyrl",
            "uz-latn", "yi-latn", "zh-cmn", "zh-cmn-hans", "zh-cmn-hant",
            "zh-gan", "zh-guoyu", // deprecated
            "zh-hakka", "zh-hans", "zh-hans-cn", "zh-hans-hk", "zh-hans-mo",
            "zh-hans-sg", "zh-hans-tw", "zh-hant", "zh-hant-cn", "zh-hant-hk",
            "zh-hant-mo", "zh-hant-sg", "zh-hant-tw", "zh-min", "zh-min-nan",
            "zh-wuu", "zh-xiang", "zh-yue" };

    /**
     * Package-private constructor
     */
    Language() {
        super();
    }

    public void checkValid(String literal, ValidationContext context)
            throws DatatypeException {
        if (literal.length() == 0) {
            throw new DatatypeException(
                    "The empty string is not a valid language tag.");
        }
        literal = toAsciiLowerCase(literal);
        if (isGrandfathered(literal)) {
            return;
        }
        if (literal.startsWith("-")) {
            throw new DatatypeException(
                    "Language tag must not start with HYPHEN-MINUS.");
        }
        if (literal.endsWith("-")) {
            throw new DatatypeException(
                    "Language tag must not end with HYPHEN-MINUS.");
        }
        String[] subtags = HYPHEN.split(literal);
        int i = 0;
        String subtag = subtags[i];
        int len = subtag.length();
        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if ((len == 2 || len == 3) && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw new DatatypeException(
                        "Bad ISO language part in language tag");
            }
            i++;
            subtag = subtags[i];
            len = subtag.length();
            if (len == 3) {
                throw new DatatypeException(
                        "Found reserved language extension subtag.");
            }
        } else if (len == 4 && isLowerCaseAlpha(subtag)) {
            throw new DatatypeException("Found reserved language tag.");
        } else if (len == 5 && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw new DatatypeException(
                        "Bad IANA language part in language tag");
            }
            i++;
            subtag = subtags[i];
            len = subtag.length();
        }
        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if (subtag.length() == 4) {
            if (!isScript(subtag)) {
                throw new DatatypeException("Bad script subtag");
            }
            i++;
            subtag = subtags[i];
            len = subtag.length();
        }
        if ((len == 3 && isDigit(subtag))
                || (len == 2 && isLowerCaseAlpha(subtag))) {
            if (!isRegion(subtag)) {
                throw new DatatypeException("Bad region subtag");
            }
            i++;
            subtag = subtags[i];
            len = subtag.length();
        }
        while (i < subtags.length) {
            if ("x".equals(subtag)) {
                checkPrivateUse(i, subtags);
                return;
            }
            // cutting corners here a bit
            if (len == 1) {
                throw new DatatypeException("Unknown extension.");
            } else {
                if (!isVariant(subtag)) {
                    throw new DatatypeException("Bad variant subtag");
                }
            }
            i++;
            subtag = subtags[i];
            len = subtag.length();
        }
    }

    private boolean isVariant(String subtag) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isRegion(String subtag) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isScript(String subtag) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isLanguage(String language) {
        // TODO Auto-generated method stub
        return false;
    }

    private void checkPrivateUse(int i, String[] subtags)
            throws DatatypeException {
        int len = subtags.length;
        i++;
        if (i == len) {
            throw new DatatypeException("No subtags in private use sequence.");
        }
        while (i < len) {
            String subtag = subtags[i];
            if (subtag.length() < 1) {
                throw new DatatypeException("Zero-length private use subtag.");
            }
            if (subtag.length() > 8) {
                throw new DatatypeException("Private use subtag too long.");
            }
            if (!isLowerCaseAlphaNumeric(subtag)) {
                throw new DatatypeException(
                        "Bad character in private use subtag.");
            }
            i++;
        }
    }

    private final boolean isLowerCaseAlphaNumeric(char c) {
        return isLowerCaseAlpha(c) || isDigit(c);
    }

    private final boolean isLowerCaseAlphaNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isLowerCaseAlphaNumeric(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param c
     * @return
     */
    private final boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private final boolean isDigit(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param c
     * @return
     */
    private final boolean isLowerCaseAlpha(char c) {
        return (c >= 'a' && c <= 'z');
    }

    private final boolean isLowerCaseAlpha(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isLowerCaseAlpha(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isGrandfathered(String literal) {
        return Arrays.binarySearch(GRANDFATHERED, literal) > -1;
    }

}
