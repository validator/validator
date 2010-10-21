/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2007-2010 Mozilla Foundation
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;
import org.whattf.datatype.data.LanguageData;

/**
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Language extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Language THE_INSTANCE = new Language();

    private static final Pattern HYPHEN = Pattern.compile("-");

    private static final boolean WARN = System.getProperty(
            "org.whattf.datatype.warn", "").equals("true") ? true : false;

    private static String[] languages = null;

    private static String[] extlangs = null;

    private static String[] scripts = null;

    private static String[] regions = null;

    private static String[] variants = null;

    private static String[] grandfathered = null;

    private static String[] redundant = null;

    private static String[] deprecated = null;

    private static String[] deprecatedLang = null;

    private static int[] suppressedScriptByLanguage = null;

    private static Map<String, String> preferredValueByLanguageMap = new HashMap<String, String>();

    private static String[][][] prefixesByVariant = null;

    private static int[] prefixByExtlang = null;

    static {
        try {
            LanguageData data = new LanguageData();
            languages = data.getLanguages();
            extlangs = data.getExtlangs();
            scripts = data.getScripts();
            regions = data.getRegions();
            variants = data.getVariants();
            grandfathered = data.getGrandfathered();
            redundant = data.getRedundant();
            deprecated = data.getDeprecated();
            deprecatedLang = data.getDeprecatedLang();
            suppressedScriptByLanguage = data.getSuppressedScriptByLanguage();
            prefixByExtlang = data.getPrefixByExtlang();
            preferredValueByLanguageMap = data.getPreferredValueByLanguageMap();
            prefixesByVariant = data.getPrefixesByVariant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Package-private constructor
     */
    private Language() {
        super();
    }

    public void checkValid(CharSequence lit) throws DatatypeException {
        String literal = lit.toString();
        if (literal.length() == 0) {
            throw newDatatypeException("The empty string is not a valid language tag.");
        }
        literal = toAsciiLowerCase(literal);
        if (isGrandfathered(literal)) {
            if (isDeprecated(literal) && WARN) {
                throw newDatatypeException("The grandfathered language tag ",
                        literal, " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            return;
        }
        if (isRedundant(literal)) {
            if (isDeprecated(literal) && WARN) {
                throw newDatatypeException("The language tag ", lit.toString(),
                        " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            return;
        }
        if (literal.startsWith("-")) {
            throw newDatatypeException("Language tag must not start with HYPHEN-MINUS.");
        }
        if (literal.endsWith("-")) {
            throw newDatatypeException("Language tag must not end with HYPHEN-MINUS.");
        }

        String[] subtags = HYPHEN.split(literal);

        for (int j = 0; j < subtags.length; j++) {
            int len = subtags[j].length();
            if (len == 0) {
                throw newDatatypeException("Zero-length subtag.");
            } else if (len > 8) {
                throw newDatatypeException("Subtags must not exceed 8 characters in length.");
            }
        }

        // Language

        int i = 0;
        String subtag = subtags[i];
        int len = subtag.length();
        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if ((len == 2 || len == 3) && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw newDatatypeException("The language subtag ", subtag,
                        " is not a valid ISO language part of a language tag.");
            }
            if (isDeprecatedLang(subtag) && WARN) {
                throw newDatatypeException("The language subtag ", subtag,
                        " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        } else if (len == 4 && isLowerCaseAlpha(subtag)) {
            throw newDatatypeException("Found reserved language tag: ", subtag,
                    ".");
        } else if (len >= 5 && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw newDatatypeException("The language subtag ", subtag,
                        " is not a valid IANA language part of a language tag.");
            }
            if (isDeprecatedLang(subtag) && WARN) {
                throw newDatatypeException("The language subtag ", subtag,
                        " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        } else {
            throw newDatatypeException("The language subtag ", subtag,
                    " is not a valid language subtag.");
        }

        // extlang

        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if (subtag.length() == 3 && isLowerCaseAlpha(subtag)) {
            if (!isExtlang(subtag)) {
                throw newDatatypeException("Bad extlang subtag ", subtag, ".");
            }
            if (!usesPrefixByExtlang(subtags[0], subtag)) {
                // IANA language tags are never correct prefixes.
                throw newDatatypeException("Extlang subtag ", subtag,
                        " has an incorrect prefix.");
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }

        // Script?

        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if (subtag.length() == 4 & isLowerCaseAlpha(subtag)) {
            if (!isScript(subtag)) {
                throw newDatatypeException("Bad script subtag.");
            }
            if (isDeprecated(subtag) && WARN) {
                throw newDatatypeException("The script subtag ", subtag,
                        " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            if (shouldSuppressScript(subtags[0], subtag)) {
                throw newDatatypeException("Language tag should omit the default script for the language.");
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }

        // Region

        if ((len == 3 && isDigit(subtag))
                || (len == 2 && isLowerCaseAlpha(subtag))) {
            if (!isRegion(subtag)) {
                throw newDatatypeException("Bad region subtag.");
            }
            if (isDeprecated(subtag) && WARN) {
                throw newDatatypeException("The region subtag ", subtag,
                        " is deprecated." + " Use \u201C"
                                + preferredValueByLanguageMap.get(literal)
                                + "\u201D instead.", WARN);
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }

        // Variant

        for (;;) {
            if ("x".equals(subtag)) {
                checkPrivateUse(i, subtags);
                return;
            }
            // cutting corners here a bit since there are no extensions at this
            // time
            if (len == 1 && isLowerCaseAlphaNumeric(subtag)) {
                throw newDatatypeException("Unknown extension ", subtag, ".");
            } else if ((len == 4 && isDigit(subtag.charAt(0)) && isLowerCaseAlphaNumeric(subtag))
                    || (len >= 5 && isLowerCaseAlphaNumeric(subtag))) {
                if (!isVariant(subtag)) {
                    throw newDatatypeException("Bad variant subtag ", subtag, ".");
                }
                if (isDeprecated(subtag) && WARN) {
                    throw newDatatypeException("The variant subtag ", subtag,
                            " is deprecated." + " Use \u201C"
                                    + preferredValueByLanguageMap.get(literal)
                                    + "\u201D instead.", WARN);
                }
                if (!hasGoodPrefix(subtags, i)) {
                    throw newDatatypeException("Variant ", subtag, " lacks required prefix.");
                }
            } else {
                throw newDatatypeException("The subtag ", subtag,
                        " does not match the format for any permissible subtag type.");
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }
    }

    private boolean hasGoodPrefix(String[] subtags, int i) {
        String variant = subtags[i];
        int index = Arrays.binarySearch(variants, variant);
        assert index >= 0;
        String[][] prefixes = prefixesByVariant[index];
        if (prefixes.length == 0) {
            return true;
        }
        for (int j = 0; j < prefixes.length; j++) {
            String[] prefix = prefixes[j];
            if (prefixMatches(prefix, subtags, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean prefixMatches(String[] prefix, String[] subtags, int limit) {
        for (int i = 0; i < prefix.length; i++) {
            String prefixComponent = prefix[i];
            if (!subtagsContainPrefixComponent(prefixComponent, subtags, limit)) {
                return false;
            }
        }
        return true;
    }

    private boolean subtagsContainPrefixComponent(String prefixComponent,
            String[] subtags, int limit) {
        for (int i = 0; i < limit; i++) {
            String subtag = subtags[i];
            if (subtag.equals(prefixComponent)) {
                return true;
            }
        }
        return false;
    }

    private boolean usesPrefixByExtlang(String language, String extlang) {
        int langIndex = Arrays.binarySearch(languages, language);
        int extlangIndex = Arrays.binarySearch(extlangs, extlang);
        assert langIndex > -1;
        int prefixExpected = prefixByExtlang[extlangIndex];
        return prefixExpected == langIndex;
    }

    private boolean shouldSuppressScript(String language, String script) {
        int langIndex = Arrays.binarySearch(languages, language);
        assert langIndex > -1;
        int scriptIndex = suppressedScriptByLanguage[langIndex];
        if (scriptIndex < 0) {
            return false;
        } else {
            return scripts[scriptIndex].equals(script);
        }
    }

    private boolean isVariant(String subtag) {
        return (Arrays.binarySearch(variants, subtag) > -1);
    }

    private boolean isRegion(String subtag) {
        return (Arrays.binarySearch(regions, subtag) > -1)
                || "aa".equals(subtag)
                || ("qm".compareTo(subtag) <= 0 && "qz".compareTo(subtag) >= 0)
                || ("xa".compareTo(subtag) <= 0 && "xz".compareTo(subtag) >= 0)
                || "zz".equals(subtag);
    }

    private boolean isScript(String subtag) {
        return (Arrays.binarySearch(scripts, subtag) > -1)
                || ("qaaa".compareTo(subtag) <= 0 && "qabx".compareTo(subtag) >= 0);
    }

    private boolean isExtlang(String subtag) {
        return (Arrays.binarySearch(extlangs, subtag) > -1);
    }

    private boolean isLanguage(String subtag) {
        return (Arrays.binarySearch(languages, subtag) > -1)
                || ("qaa".compareTo(subtag) <= 0 && "qtz".compareTo(subtag) >= 0);
    }

    private void checkPrivateUse(int i, String[] subtags)
            throws DatatypeException {
        int len = subtags.length;
        i++;
        if (i == len) {
            throw newDatatypeException("No subtags in private use sequence.");
        }
        while (i < len) {
            String subtag = subtags[i];
            if (subtag.length() < 2) {
                throw newDatatypeException("Private use subtag ", subtag, " is too short.");                
            }
            if (!isLowerCaseAlphaNumeric(subtag)) {
                throw newDatatypeException("Bad character in private use subtag ", subtag, ".");
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
        return Arrays.binarySearch(grandfathered, literal) > -1;
    }

    private boolean isRedundant(String literal) {
        return Arrays.binarySearch(redundant, literal) > -1;
    }

    private boolean isDeprecated(String subtag) {
        return Arrays.binarySearch(deprecated, subtag) > -1;
    }

    private boolean isDeprecatedLang(String subtag) {
        return Arrays.binarySearch(deprecatedLang, subtag) > -1;
    }

    @Override public String getName() {
        return "language tag";
    }
}
