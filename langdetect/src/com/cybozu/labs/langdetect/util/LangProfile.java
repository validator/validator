package com.cybozu.labs.langdetect.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link LangProfile} is a Language Profile Class.
 * Users don't use this class directly.
 * 
 * @author Nakatani Shuyo
 */
public class LangProfile {
    private static final int MINIMUM_FREQ = 2;
    private static final int LESS_FREQ_RATIO = 100000;
    public String name = null;
    public HashMap<String, Integer> freq = new HashMap<String, Integer>();
    public int[] n_words = new int[NGram.N_GRAM];

    /**
     * Constructor for JSONIC 
     */
    public LangProfile() {}

    /**
     * Normal Constructor
     * @param name language name
     */
    public LangProfile(String name, HashMap freq, int[] n_words) {
        this.name = name;
        this.freq = freq;
        this.n_words = n_words;
    }
    
    /**
     * Add n-gram to profile
     * @param gram
     */
    public void add(String gram) {
        if (name == null || gram == null) return;   // Illegal
        int len = gram.length();
        if (len < 1 || len > NGram.N_GRAM) return;  // Illegal
        ++n_words[len - 1];
        if (freq.containsKey(gram)) {
            freq.put(gram, freq.get(gram) + 1);
        } else {
            freq.put(gram, 1);
        }
    }

    /**
     * Eliminate below less frequency n-grams and noise Latin alphabets
     */
    public void omitLessFreq() {
        if (name == null) return;   // Illegal
        int threshold = n_words[0] / LESS_FREQ_RATIO;
        if (threshold < MINIMUM_FREQ) threshold = MINIMUM_FREQ;
        
        Set<String> keys = freq.keySet();
        int roman = 0;
        for(Iterator<String> i = keys.iterator(); i.hasNext(); ){
            String key = i.next();
            int count = freq.get(key);
            if (count <= threshold) {
                n_words[key.length()-1] -= count; 
                i.remove();
            } else {
                if (key.matches("^[A-Za-z]$")) {
                    roman += count;
                }
            }
        }

        // roman check
        if (roman < n_words[0] / 3) {
            Set<String> keys2 = freq.keySet();
            for(Iterator<String> i = keys2.iterator(); i.hasNext(); ){
                String key = i.next();
                if (key.matches(".*[A-Za-z].*")) {
                    n_words[key.length()-1] -= freq.get(key); 
                    i.remove();
                }
            }
            
        }
    }

    /**
     * Update the language profile with (fragmented) text.
     * Extract n-grams from text and add their frequency into the profile.
     * @param text (fragmented) text to extract n-grams
     */
    public void update(String text) {
        if (text == null) return;
        text = NGram.normalize_vi(text);
        NGram gram = new NGram();
        for(int i=0; i<text.length(); ++i) {
            gram.addChar(text.charAt(i));
            for(int n=1; n<=NGram.N_GRAM; ++n) {
                add(gram.get(n));
            }
        }
    }
}
