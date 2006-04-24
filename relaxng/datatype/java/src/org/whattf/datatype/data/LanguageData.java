package org.whattf.datatype.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class LanguageData {
    
    private static final String PREFIX = "prefix: ";

    private static final String SUPPRESS_SCRIPT = "suppress-script: ";

    private static final String SUBTAG = "subtag: ";

    private static final String TYPE = "type: ";
    
    private BufferedReader in;
    
    private SortedSet languageSet = new TreeSet();
    
    private SortedSet scriptSet = new TreeSet();
    
    private SortedSet regionSet = new TreeSet();
    
    private SortedSet variantSet = new TreeSet();
    
    private String[] languages = null;
    
    private String[] scripts = null;
    
    private String[] regions = null;
    
    private String[] variants = null;
    
    private int[] suppressedScriptByLanguage = null;
    
    private String[][] prefixesByVariant = null;
    
    public LanguageData() throws IOException {
        super();
        consumeRegistry();
        prepareArrays();
    }
    
    private void consumeRegistry() throws IOException {
        while(consumeRecord());
    }
    
    private void prepareArrays() throws IOException {
        int i = 0;
        scripts = new String[scriptSet.size()];
        for (Iterator iter = scriptSet.iterator(); iter.hasNext();) {
            String str = (String) iter.next();
            scripts[i] = str.intern();
            i++;
        }
        
        i = 0;
        languages = new String[languageSet.size()];
        suppressedScriptByLanguage = new int[languageSet.size()];
        for (Iterator iter = languageSet.iterator(); iter.hasNext();) {
            StringPair pair = (StringPair) iter.next();
            languages[i] = pair.getMain().intern();
            String suppressed = (String)pair.getOther();
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
        regions = new String[regionSet.size()];
        for (Iterator iter = regionSet.iterator(); iter.hasNext();) {
            String str = (String) iter.next();
            regions[i] = str.intern();
            i++;
        }
        
        i = 0;
        variants = new String[variantSet.size()];
        prefixesByVariant = new String[variantSet.size()][];
        for (Iterator iter = variantSet.iterator(); iter.hasNext();) {
            StringPair pair = (StringPair) iter.next();
            variants[i] = pair.getMain().intern();
            SortedSet other = (SortedSet) pair.getOther();
            String[] prefixArr = new String[other.size()];
            int j = 0;
            for (Iterator iterator = other.iterator(); iterator.hasNext();) {
                String str = (String) iterator.next();
                prefixArr[j] = str.intern();
                j++;
            }
            prefixesByVariant[i] = prefixArr;
            i++;
        }
    }
    
    private boolean consumeRecord() throws IOException {
        boolean hasMore = true;
        String type = null;
        String subtag = null;
        String suppressScript = null;
        SortedSet prefixes = new TreeSet();
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
                type = line.substring(TYPE.length()).trim();
            } else if (line.startsWith(SUBTAG)) {
                subtag = line.substring(SUBTAG.length()).trim();
            } else if (line.startsWith(SUPPRESS_SCRIPT)) {
                suppressScript = line.substring(SUPPRESS_SCRIPT.length()).trim();
            } else if (line.startsWith(PREFIX)) {
                prefixes.add(line.substring(PREFIX.length()).trim());
            }
        }
        if (subtag == null) {
            return hasMore;
        }
        if ("language".equals(type)) {
            languageSet.add(new StringPair(subtag, suppressScript));
        } else if ("region".equals(type)) {
            regionSet.add(subtag);
        } else if ("script".equals(type)) {
            scriptSet.add(subtag);
        } else if ("variant".equals(type)) {
            variantSet.add(new StringPair(subtag, prefixes));
        }
        return hasMore;
    }

    private class StringPair implements Comparable{

        private String main;
        
        private Object other;

        /**
         * Returns the main.
         * 
         * @return the main
         */
        public String getMain() {
            return main;
        }

        /**
         * Returns the other.
         * 
         * @return the other
         */
        public Object getOther() {
            return other;
        }

        /**
         * @param main
         * @param other
         */
        public StringPair(String main, Object other) {
            this.main = main;
            this.other = other;
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object arg0) {
            return main.equals(((StringPair)arg0).main);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return main.hashCode();
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object arg0) {
            return main.compareTo(((StringPair)arg0).main);
        }
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
    public String[][] getPrefixesByVariant() {
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
}
