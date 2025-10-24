package com.cybozu.labs.langdetect.util;

import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cut out N-gram from text. 
 * Users don't use this class directly.
 * @author Nakatani Shuyo
 */
public class NGram {
    private static final String LATIN1_EXCLUDED = Messages.getString("NGram.LATIN1_EXCLUDE");
    public final static int N_GRAM = 3;
    public static HashMap<Character, Character> cjk_map; 
    
    private StringBuffer grams_;
    private boolean capitalword_;

    /**
     * Constructor.
     */
    public NGram() {
        grams_ = new StringBuffer(" ");
        capitalword_ = false;
    }

    /**
     * Append a character into ngram buffer.
     * @param ch
     */
    public void addChar(char ch) {
        ch = normalize(ch);
        char lastchar = grams_.charAt(grams_.length() - 1);
        if (lastchar == ' ') {
            grams_ = new StringBuffer(" ");
            capitalword_ = false;
            if (ch==' ') return;
        } else if (grams_.length() >= N_GRAM) {
            grams_.deleteCharAt(0);
        }
        grams_.append(ch);

        if (Character.isUpperCase(ch)){
            if (Character.isUpperCase(lastchar)) capitalword_ = true;
        } else {
            capitalword_ = false;
        }
    }

    /**
     * Get n-Gram
     * @param n length of n-gram
     * @return n-Gram String (null if it is invalid)
     */
    public String get(int n) {
        if (capitalword_) return null;
        int len = grams_.length(); 
        if (n < 1 || n > 3 || len < n) return null;
        if (n == 1) {
            char ch = grams_.charAt(len - 1);
            if (ch == ' ') return null;
            return Character.toString(ch);
        } else {
            return grams_.substring(len - n, len);
        }
    }
    
    /**
     * Character Normalization
     * @param ch
     * @return Normalized character
     */
    static public char normalize(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (block == UnicodeBlock.BASIC_LATIN) {
            if (ch<'A' || (ch<'a' && ch >'Z') || ch>'z') ch = ' ';
        } else if (block == UnicodeBlock.LATIN_1_SUPPLEMENT) {
            if (LATIN1_EXCLUDED.indexOf(ch)>=0) ch = ' ';
        } else if (block == UnicodeBlock.LATIN_EXTENDED_B) {
            // normalization for Romanian
            if (ch == '\u0219') ch = '\u015f';  // Small S with comma below => with cedilla
            if (ch == '\u021b') ch = '\u0163';  // Small T with comma below => with cedilla
        } else if (block == UnicodeBlock.GENERAL_PUNCTUATION) {
            ch = ' ';
        } else if (block == UnicodeBlock.ARABIC) {
            if (ch == '\u06cc') ch = '\u064a';  // Farsi yeh => Arabic yeh
        } else if (block == UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
            if (ch >= '\u1ea0') ch = '\u1ec3';
        } else if (block == UnicodeBlock.HIRAGANA) {
            ch = '\u3042';
        } else if (block == UnicodeBlock.KATAKANA) {
            ch = '\u30a2';
        } else if (block == UnicodeBlock.BOPOMOFO || block == UnicodeBlock.BOPOMOFO_EXTENDED) {
            ch = '\u3105';
        } else if (block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            if (cjk_map.containsKey(ch)) ch = cjk_map.get(ch);
        } else if (block == UnicodeBlock.HANGUL_SYLLABLES) {
            ch = '\uac00';
        }
        return ch;
    }

    /**
     * Normalizer for Vietnamese.
     * Normalize Alphabet + Diacritical Mark(U+03xx) into U+1Exx .
     * @param text
     * @return normalized text
     */
    public static String normalize_vi(String text) {
        Matcher m = ALPHABET_WITH_DMARK.matcher(text);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            int alphabet = TO_NORMALIZE_VI_CHARS.indexOf(m.group(1));
            int dmark = DMARK_CLASS.indexOf(m.group(2)); // Diacritical Mark
            m.appendReplacement(buf, NORMALIZED_VI_CHARS[dmark].substring(alphabet, alphabet + 1));
        }
        if (buf.length() == 0)
            return text;
        m.appendTail(buf);
        return buf.toString();
    }

    private static final String[] NORMALIZED_VI_CHARS = {
            Messages.getString("NORMALIZED_VI_CHARS_0300"),
            Messages.getString("NORMALIZED_VI_CHARS_0301"),
            Messages.getString("NORMALIZED_VI_CHARS_0303"),
            Messages.getString("NORMALIZED_VI_CHARS_0309"),
            Messages.getString("NORMALIZED_VI_CHARS_0323") };
    private static final String TO_NORMALIZE_VI_CHARS = Messages.getString("TO_NORMALIZE_VI_CHARS");
    private static final String DMARK_CLASS = Messages.getString("DMARK_CLASS");
    private static final Pattern ALPHABET_WITH_DMARK = Pattern.compile("([" + TO_NORMALIZE_VI_CHARS + "])(["
            + DMARK_CLASS + "])");
    
    /**
     * CJK Kanji Normalization Mapping
     */
    static final String[] CJK_CLASS = {
        Messages.getString("NGram.KANJI_1_0"),
        Messages.getString("NGram.KANJI_1_2"),
        Messages.getString("NGram.KANJI_1_4"),
        Messages.getString("NGram.KANJI_1_8"),
        Messages.getString("NGram.KANJI_1_11"),
        Messages.getString("NGram.KANJI_1_12"),
        Messages.getString("NGram.KANJI_1_13"),
        Messages.getString("NGram.KANJI_1_14"),
        Messages.getString("NGram.KANJI_1_16"),
        Messages.getString("NGram.KANJI_1_18"),
        Messages.getString("NGram.KANJI_1_22"),
        Messages.getString("NGram.KANJI_1_27"),
        Messages.getString("NGram.KANJI_1_29"),
        Messages.getString("NGram.KANJI_1_31"),
        Messages.getString("NGram.KANJI_1_35"),
        Messages.getString("NGram.KANJI_2_0"),
        Messages.getString("NGram.KANJI_2_1"),
        Messages.getString("NGram.KANJI_2_4"),
        Messages.getString("NGram.KANJI_2_9"),
        Messages.getString("NGram.KANJI_2_10"),
        Messages.getString("NGram.KANJI_2_11"),
        Messages.getString("NGram.KANJI_2_12"),
        Messages.getString("NGram.KANJI_2_13"),
        Messages.getString("NGram.KANJI_2_15"),
        Messages.getString("NGram.KANJI_2_16"),
        Messages.getString("NGram.KANJI_2_18"),
        Messages.getString("NGram.KANJI_2_21"),
        Messages.getString("NGram.KANJI_2_22"),
        Messages.getString("NGram.KANJI_2_23"),
        Messages.getString("NGram.KANJI_2_28"),
        Messages.getString("NGram.KANJI_2_29"),
        Messages.getString("NGram.KANJI_2_30"),
        Messages.getString("NGram.KANJI_2_31"),
        Messages.getString("NGram.KANJI_2_32"),
        Messages.getString("NGram.KANJI_2_35"),
        Messages.getString("NGram.KANJI_2_36"),
        Messages.getString("NGram.KANJI_2_37"),
        Messages.getString("NGram.KANJI_2_38"),
        Messages.getString("NGram.KANJI_3_1"),
        Messages.getString("NGram.KANJI_3_2"),
        Messages.getString("NGram.KANJI_3_3"),
        Messages.getString("NGram.KANJI_3_4"),
        Messages.getString("NGram.KANJI_3_5"),
        Messages.getString("NGram.KANJI_3_8"),
        Messages.getString("NGram.KANJI_3_9"),
        Messages.getString("NGram.KANJI_3_11"),
        Messages.getString("NGram.KANJI_3_12"),
        Messages.getString("NGram.KANJI_3_13"),
        Messages.getString("NGram.KANJI_3_15"),
        Messages.getString("NGram.KANJI_3_16"),
        Messages.getString("NGram.KANJI_3_18"),
        Messages.getString("NGram.KANJI_3_19"),
        Messages.getString("NGram.KANJI_3_22"),
        Messages.getString("NGram.KANJI_3_23"),
        Messages.getString("NGram.KANJI_3_27"),
        Messages.getString("NGram.KANJI_3_29"),
        Messages.getString("NGram.KANJI_3_30"),
        Messages.getString("NGram.KANJI_3_31"),
        Messages.getString("NGram.KANJI_3_32"),
        Messages.getString("NGram.KANJI_3_35"),
        Messages.getString("NGram.KANJI_3_36"),
        Messages.getString("NGram.KANJI_3_37"),
        Messages.getString("NGram.KANJI_3_38"),
        Messages.getString("NGram.KANJI_4_0"),
        Messages.getString("NGram.KANJI_4_9"),
        Messages.getString("NGram.KANJI_4_10"),
        Messages.getString("NGram.KANJI_4_16"),
        Messages.getString("NGram.KANJI_4_17"),
        Messages.getString("NGram.KANJI_4_18"),
        Messages.getString("NGram.KANJI_4_22"),
        Messages.getString("NGram.KANJI_4_24"),
        Messages.getString("NGram.KANJI_4_28"),
        Messages.getString("NGram.KANJI_4_34"),
        Messages.getString("NGram.KANJI_4_39"),
        Messages.getString("NGram.KANJI_5_10"),
        Messages.getString("NGram.KANJI_5_11"),
        Messages.getString("NGram.KANJI_5_12"),
        Messages.getString("NGram.KANJI_5_13"),
        Messages.getString("NGram.KANJI_5_14"),
        Messages.getString("NGram.KANJI_5_18"),
        Messages.getString("NGram.KANJI_5_26"),
        Messages.getString("NGram.KANJI_5_29"),
        Messages.getString("NGram.KANJI_5_34"),
        Messages.getString("NGram.KANJI_5_39"),
        Messages.getString("NGram.KANJI_6_0"),
        Messages.getString("NGram.KANJI_6_3"),
        Messages.getString("NGram.KANJI_6_9"),
        Messages.getString("NGram.KANJI_6_10"),
        Messages.getString("NGram.KANJI_6_11"),
        Messages.getString("NGram.KANJI_6_12"),
        Messages.getString("NGram.KANJI_6_16"),
        Messages.getString("NGram.KANJI_6_18"),
        Messages.getString("NGram.KANJI_6_20"),
        Messages.getString("NGram.KANJI_6_21"),
        Messages.getString("NGram.KANJI_6_22"),
        Messages.getString("NGram.KANJI_6_23"),
        Messages.getString("NGram.KANJI_6_25"),
        Messages.getString("NGram.KANJI_6_28"),
        Messages.getString("NGram.KANJI_6_29"),
        Messages.getString("NGram.KANJI_6_30"),
        Messages.getString("NGram.KANJI_6_32"),
        Messages.getString("NGram.KANJI_6_34"),
        Messages.getString("NGram.KANJI_6_35"),
        Messages.getString("NGram.KANJI_6_37"),
        Messages.getString("NGram.KANJI_6_39"),
        Messages.getString("NGram.KANJI_7_0"),
        Messages.getString("NGram.KANJI_7_3"),
        Messages.getString("NGram.KANJI_7_6"),
        Messages.getString("NGram.KANJI_7_7"),
        Messages.getString("NGram.KANJI_7_9"),
        Messages.getString("NGram.KANJI_7_11"),
        Messages.getString("NGram.KANJI_7_12"),
        Messages.getString("NGram.KANJI_7_13"),
        Messages.getString("NGram.KANJI_7_16"),
        Messages.getString("NGram.KANJI_7_18"),
        Messages.getString("NGram.KANJI_7_19"),
        Messages.getString("NGram.KANJI_7_20"),
        Messages.getString("NGram.KANJI_7_21"),
        Messages.getString("NGram.KANJI_7_23"),
        Messages.getString("NGram.KANJI_7_25"),
        Messages.getString("NGram.KANJI_7_28"),
        Messages.getString("NGram.KANJI_7_29"),
        Messages.getString("NGram.KANJI_7_32"),
        Messages.getString("NGram.KANJI_7_33"),
        Messages.getString("NGram.KANJI_7_35"),
        Messages.getString("NGram.KANJI_7_37"),
    };
    static {
        cjk_map = new HashMap<Character, Character>();
        for (String cjk_list : CJK_CLASS) {
            char representative = cjk_list.charAt(0);
            for (int i=0;i<cjk_list.length();++i) {
                cjk_map.put(cjk_list.charAt(i), representative);
            }
        }
    }

}
