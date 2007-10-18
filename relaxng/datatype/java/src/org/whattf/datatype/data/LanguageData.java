package org.whattf.datatype.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    
    private BufferedReader in;
    
    private SortedSet<String> languageSet = new TreeSet<String>();
    
    private SortedSet<String> scriptSet = new TreeSet<String>();
    
    private SortedSet<String> regionSet = new TreeSet<String>();
    
    private SortedSet<String> variantSet = new TreeSet<String>();
    
    private SortedSet<String> grandfatheredSet = new TreeSet<String>();

    private SortedSet<String> deprecatedSet = new TreeSet<String>();

    private Map<String, String> suppressedScriptByLanguageMap = new HashMap<String, String>();
    
    private Map<String, Set<String[]>> prefixesByVariantMap = new HashMap<String, Set<String[]>>();
    
    private String[] languages = null;
    
    private String[] scripts = null;
    
    private String[] regions = null;
    
    private String[] variants = null;
    
    private String[] grandfathered = null;

    private String[] deprecated = null;
    
    private int[] suppressedScriptByLanguage = null;
    
    private String[][][] prefixesByVariant = null;
    
    public LanguageData() throws IOException {
        super();
        consumeRegistry();
        prepareArrays();
    }
    
    private void consumeRegistry() throws IOException {
        while(consumeRecord()) {
          // spin
        }
    }
    
    private void prepareArrays() throws IOException {
        scripts = scriptSet.toArray(EMPTY_STRING_ARRAY);
        regions = regionSet.toArray(EMPTY_STRING_ARRAY);
        grandfathered = grandfatheredSet.toArray(EMPTY_STRING_ARRAY);
        
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
                    throw new IOException("Malformed registry: reference to non-existent script.");
                }
                suppressedScriptByLanguage[i] = index;
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
        Set<String[]> prefixes = new HashSet<String[]>();
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
                String[] prefixSubtags = HYPHEN.split(line.substring(PREFIX.length()).trim());
                for (int i = 0; i < prefixSubtags.length; i++) {
                    prefixSubtags[i] = prefixSubtags[i].intern();
                }
                prefixes.add(prefixSubtags);
            } else if (line.startsWith(DEPRECATED)) {
                depr = true;
            }
        }
        if (subtag == null) {
            return hasMore;
        }
        if (depr) {
            deprecatedSet.add(subtag);
        }
        if ("language" == type) {
            languageSet.add(subtag);
            suppressedScriptByLanguageMap.put(subtag, suppressScript);
        } else if ("region" == type) {
            regionSet.add(subtag);
        } else if ("script" == type) {
            scriptSet.add(subtag);
        } else if ("variant" == type) {
            variantSet.add(subtag);
            prefixesByVariantMap.put(subtag, prefixes);
        } else if ("grandfathered" == type) {
            grandfatheredSet.add(subtag);
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

    /**
     * Returns the prefixesByVariant.
     * 
     * @return the prefixesByVariant
     */
    public String[][][] getPrefixesByVariant() {
        return prefixesByVariant;
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
     * Returns the grandfathered.
     * 
     * @return the grandfathered
     */
    public String[] getGrandfathered() {
        return grandfathered;
    }
}
