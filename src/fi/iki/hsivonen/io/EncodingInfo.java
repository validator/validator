package fi.iki.hsivonen.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class EncodingInfo {

    private static String[] NOT_OBSCURE = {"Big5",
        "Big5-HKSCS",
        "EUC-JP",
        "EUC-KR",
        "GB18030",
        "GBK",
        "ISO-2022-JP",
        "ISO-2022-KR",
        "ISO-8859-1",
        "ISO-8859-13",
        "ISO-8859-15",
        "ISO-8859-2",
        "ISO-8859-3",
        "ISO-8859-4",
        "ISO-8859-5",
        "ISO-8859-6",
        "ISO-8859-7",
        "ISO-8859-8",
        "ISO-8859-9",
        "KOI8-R",
        "Shift_JIS",
        "TIS-620",
        "US-ASCII",
        "UTF-16",
        "UTF-16BE",
        "UTF-16LE",
        "UTF-8",
        "windows-1250",
        "windows-1251",
        "windows-1252",
        "windows-1253",
        "windows-1254",
        "windows-1255",
        "windows-1256",
        "windows-1257",
        "windows-1258"};
    
    private static String[] asciiSuperset;

    private static String[] notAsciiSuperset;   

    static {
        byte[] testBuf = new byte[0x63];
        for (int i = 0; i < 0x60; i++) {
            testBuf[i] = (byte) (i + 0x20);
        }
        testBuf[0x60] = (byte) '\n';
        testBuf[0x61] = (byte) '\r';
        testBuf[0x62] = (byte) '\t';

        SortedSet<String> asciiSupersetSet = new TreeSet<String>();
        SortedSet<String> notAsciiSupersetSet = new TreeSet<String>();
        
        SortedMap charsets = Charset.availableCharsets();
        for (Iterator iter = charsets.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Charset cs = (Charset) entry.getValue();
            if (asciiMapsToBasicLatin(testBuf, cs)) {
                asciiSupersetSet.add(cs.name().intern());
            } else {
                notAsciiSupersetSet.add(cs.name().intern());
            }
        }
        
        asciiSuperset = (String[]) asciiSupersetSet.toArray(new String[0]);
        notAsciiSuperset = (String[]) notAsciiSupersetSet.toArray(new String[0]);
    }

    public static boolean isAsciiSuperset(String preferredIanaName) {
        return (Arrays.binarySearch(asciiSuperset, preferredIanaName) > -1);
    }

    public static boolean isNotAsciiSuperset(String preferredIanaName) {
        return (Arrays.binarySearch(notAsciiSuperset, preferredIanaName) > -1);
    }

    public static boolean isObscure(String preferredIanaName) {
        return !(Arrays.binarySearch(NOT_OBSCURE, preferredIanaName) > -1);
    }
    
    /**
     * @param testBuf
     * @param cs
     */
    private static boolean asciiMapsToBasicLatin(byte[] testBuf, Charset cs) {
        CharsetDecoder dec = cs.newDecoder();
        dec.onMalformedInput(CodingErrorAction.REPORT);
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        Reader r = new InputStreamReader(new ByteArrayInputStream(testBuf), dec);
        try {
            for (int i = 0; i < 0x60; i++) {
                if ((i + 0x20) != r.read()) {
                    return false;
                }
            }
            if ('\n' != r.read()) {
                return false;
            }
            if ('\r' != r.read()) {
                return false;
            }
            if ('\t' != r.read()) {
                return false;
            }        
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        System.out.println("ASCII maps to Basic Latin:");
        for (int i = 0; i < asciiSuperset.length; i++) {
            System.out.println(asciiSuperset[i]);            
        }
        System.out.println();
        System.out.println("ASCII does not map to Basic Latin:");
        for (int i = 0; i < notAsciiSuperset.length; i++) {
            System.out.println(notAsciiSuperset[i]);            
        }
    }
}
