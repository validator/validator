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

package org.whattf.datatype.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class LanguageData {

    private static final Pattern HYPHEN = Pattern.compile("-");

    private static final String[][] EMPTY_DOUBLE_STRING_ARRAY = {};

    private static final String[] EMPTY_STRING_ARRAY = {};

    private static final String PREFIX = "prefix: ";

    private static final String SUPPRESS_SCRIPT = "suppress-script: ";

    private static final String SUBTAG = "subtag: ";

    private static final String TAG = "tag: ";

    private static final String TYPE = "type: ";

    private static final String DEPRECATED = "deprecated: ";

    private static final String PREFERRED_VALUE = "preferred-value: ";

    private BufferedReader in;

    private SortedSet<String> languageSet = new TreeSet<String>();

    private SortedSet<String> extlangSet = new TreeSet<String>();

    private SortedSet<String> scriptSet = new TreeSet<String>();

    private SortedSet<String> regionSet = new TreeSet<String>();

    private SortedSet<String> variantSet = new TreeSet<String>();

    private SortedSet<String> grandfatheredSet = new TreeSet<String>();

    private SortedSet<String> redundantSet = new TreeSet<String>();

    private SortedSet<String> deprecatedLangSet = new TreeSet<String>();

    private SortedSet<String> deprecatedSet = new TreeSet<String>();

    private Map<String, String> suppressedScriptByLanguageMap = new HashMap<String, String>();

    private Map<String, String> prefixByExtlangMap = new HashMap<String, String>();

    private Map<String, String> preferredValueByLanguageMap = new HashMap<String, String>();

    private Map<String, Set<String[]>> prefixesByVariantMap = new HashMap<String, Set<String[]>>();

    private String[] languages = null;

    private String[] extlangs = null;

    private String[] scripts = null;

    private String[] regions = null;

    private String[] variants = null;

    private String[] grandfathered = null;

    private String[] redundant = null;

    private String[] deprecatedLang = null;

    private String[] deprecated = null;

    private int[] suppressedScriptByLanguage = null;

    private int[] prefixByExtlang = null;

    private String[][][] prefixesByVariant = null;

    public LanguageData() throws IOException {
        super();
        URL url = new URL(System.getProperty(
                "org.whattf.datatype.lang-registry",
                "http://www.iana.org/assignments/language-subtag-registry"));
        in = new BufferedReader(
                new InputStreamReader(url.openStream(), "UTF-8"));
        consumeRegistry();
        prepareArrays();
    }

    private void consumeRegistry() throws IOException {
        while (consumeRecord()) {
            // spin
        }
        in.close();
    }

    private void prepareArrays() throws IOException {
        scripts = scriptSet.toArray(EMPTY_STRING_ARRAY);
        regions = regionSet.toArray(EMPTY_STRING_ARRAY);
        grandfathered = grandfatheredSet.toArray(EMPTY_STRING_ARRAY);
        redundant = redundantSet.toArray(EMPTY_STRING_ARRAY);
        deprecated = deprecatedSet.toArray(EMPTY_STRING_ARRAY);
        deprecatedLang = deprecatedLangSet.toArray(EMPTY_STRING_ARRAY);

        int i = 0;
        languages = new String[languageSet.size()];
        suppressedScriptByLanguage = new int[languageSet.size()];
        for (String language : languageSet) {
            languages[i] = language;
            String suppressed = suppressedScriptByLanguageMap.get(language);
            if (suppressed == null) {
                suppressedScriptByLanguage[i] = -1;
            } else {
                int index = Arrays.binarySearch(scripts, suppressed);
                if (index < 0) {
                    throw new IOException(
                            "Malformed registry: reference to non-existent script.");
                }
                suppressedScriptByLanguage[i] = index;
            }
            i++;
        }

        i = 0;
        extlangs = new String[extlangSet.size()];
        prefixByExtlang = new int[extlangSet.size()];
        for (String extlang : extlangSet) {
            extlangs[i] = extlang;
            String prefix = prefixByExtlangMap.get(extlang);
            if (prefix == null) {
                prefixByExtlang[i] = -1;
            } else {
                int index = Arrays.binarySearch(languages, prefix);
                if (index < 0) {
                    throw new IOException(
                            "Malformed registry: reference to non-existent prefix for extlang.");
                }
                prefixByExtlang[i] = index;
            }
            i++;
        }

        i = 0;
        variants = new String[variantSet.size()];
        prefixesByVariant = new String[variantSet.size()][][];
        for (String variant : variantSet) {
            variants[i] = variant;
            Set<String[]> prefixes = prefixesByVariantMap.get(variant);
            if (prefixes != null) {
                prefixesByVariant[i] = prefixes.toArray(EMPTY_DOUBLE_STRING_ARRAY);
            } else {
                prefixesByVariant[i] = EMPTY_DOUBLE_STRING_ARRAY;
            }
            i++;
        }
    }

    private boolean consumeRecord() throws IOException {
        boolean hasMore = true;
        String type = null;
        String subtag = null;
        String suppressScript = null;
        String preferredValue = null;
        Set<String[]> prefixes = new HashSet<String[]>();
        String singlePrefix = null;
        boolean depr = false;
        String line = null;
        for (;;) {
            line = in.readLine();
            if (line == null) {
                hasMore = false;
                break;
            }
            line = line.toLowerCase();
            if ("%%".equals(line)) {
                break;
            } else if (line.startsWith(TYPE)) {
                type = line.substring(TYPE.length()).trim().intern();
            } else if (line.startsWith(SUBTAG)) {
                subtag = line.substring(SUBTAG.length()).trim().intern();
            } else if (line.startsWith(TAG)) {
                subtag = line.substring(TAG.length()).trim().intern();
            } else if (line.startsWith(SUPPRESS_SCRIPT)) {
                suppressScript = line.substring(SUPPRESS_SCRIPT.length()).trim().intern();
            } else if (line.startsWith(PREFIX)) {
                String[] prefixSubtags = HYPHEN.split(line.substring(
                        PREFIX.length()).trim());
                for (int i = 0; i < prefixSubtags.length; i++) {
                    prefixSubtags[i] = prefixSubtags[i].intern();
                }
                prefixes.add(prefixSubtags);
                singlePrefix = prefixSubtags[0];
            } else if (line.startsWith(DEPRECATED)) {
                depr = true;
            } else if (line.startsWith(PREFERRED_VALUE)) {
                preferredValue = line.substring(PREFERRED_VALUE.length()).trim().intern();
                preferredValueByLanguageMap.put(subtag, preferredValue);
            }
        }
        if (subtag == null) {
            return hasMore;
        }
        if (depr) {
            if ("language" == type) {
                deprecatedLangSet.add(subtag);
            } else {
                deprecatedSet.add(subtag);
            }
        }
        if ("language" == type) {
            languageSet.add(subtag);
            suppressedScriptByLanguageMap.put(subtag, suppressScript);
        }
        if ("extlang" == type) {
            extlangSet.add(subtag);
            prefixByExtlangMap.put(subtag, singlePrefix);
        } else if ("region" == type) {
            regionSet.add(subtag);
        } else if ("script" == type) {
            scriptSet.add(subtag);
        } else if ("variant" == type) {
            variantSet.add(subtag);
            prefixesByVariantMap.put(subtag, prefixes);
        } else if ("grandfathered" == type) {
            grandfatheredSet.add(subtag);
        } else if ("redundant" == type) {
            redundantSet.add(subtag);
        }
        return hasMore;
    }

    /**
     * Returns the languages.
     * 
     * @return the languages
     */
    public String[] getLanguages() {
        return languages;
    }

    public String[] getExtlangs() {
        return extlangs;
    }

    /**
     * Returns the prefixesByVariant.
     * 
     * @return the prefixesByVariant
     */
    public String[][][] getPrefixesByVariant() {
        return prefixesByVariant;
    }

    public int[] getPrefixByExtlang() {
        return prefixByExtlang;
    }

    /**
     * Returns the regions.
     * 
     * @return the regions
     */
    public String[] getRegions() {
        return regions;
    }

    /**
     * Returns the scripts.
     * 
     * @return the scripts
     */
    public String[] getScripts() {
        return scripts;
    }

    /**
     * Returns the suppressedScriptByLanguage.
     * 
     * @return the suppressedScriptByLanguage
     */
    public int[] getSuppressedScriptByLanguage() {
        return suppressedScriptByLanguage;
    }

    /**
     * Returns the variants.
     * 
     * @return the variants
     */
    public String[] getVariants() {
        return variants;
    }

    /**
     * Returns the deprecated.
     * 
     * @return the deprecated
     */
    public String[] getDeprecated() {
        return deprecated;
    }

    /**
     * Returns the preferredValueByLanguageMap.
     * 
     * @return the preferredValueByLanguageMap
     */
    public Map<String, String> getPreferredValueByLanguageMap() {
        return preferredValueByLanguageMap;
    }

    /**
     * Returns the grandfathered.
     * 
     * @return the grandfathered
     */
    public String[] getGrandfathered() {
        return grandfathered;
    }

    /**
     * Returns the redundant.
     * 
     * @return the redundant
     */
    public String[] getRedundant() {
        return redundant;
    }

    /**
     * Returns the deprecatedLang.
     * 
     * @return the deprecatedLang
     */
    public String[] getDeprecatedLang() {
        return deprecatedLang;
    }
}
