package org.whattf.checker;


import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utilities for working with (X)HTML5 attribute values.
 * 
 * @version $Id$
 * @author hsivonen
 */
public class AttributeUtil {

    /**
     * An empty string array instance.
     */
    private final static String[] EMPTY_STRING_ARRAY = {};

    /**
     * The pattern for extracting an integer by skipping white space and ignoring 
     * trailing garbage.
     */
    private static Pattern INTEGER_PATTERN = Pattern.compile("^[ \t\n\r]*(-?[0-9]+)");

    /**
     * Private constructor to prevent instantiation.
     */
    private AttributeUtil() {
        super();
    }

    /**
     * Returns the integer represented by <code>attrVal</code> or 
     * <code>Integer.MIN_VALUE</code> on error.
     * 
     * @param attrVal a string representing an integer attribute 
     * value (can be <code>null</code>)
     * @return the integer represented by <code>attrVal</code> or 
     * <code>Integer.MIN_VALUE</code> on error
     */
    public static int parseInteger(String attrVal) {
        if (attrVal == null) {
            return Integer.MIN_VALUE;
        }
        Matcher m = INTEGER_PATTERN.matcher(attrVal);
        if (!m.matches()) {
            return Integer.MIN_VALUE;
        }
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Returns the non-negative integer represented by 
     * <code>attrVal</code> or -1 on error.
     * 
     * @param attrVal a string representing a non-negative 
     * integer attribute value (can be <code>null</code>)
     * @return the integer represented by <code>attrVal</code> or 
     * -1 on error
     */
    public static int parseNonNegativeInteger(String attrVal) {
        int rv = parseInteger(attrVal);
        if (rv < 0) {
            return -1;
        } else {
            return rv;
        }
    }

    /**
     * Returns the positive integer represented by 
     * <code>attrVal</code> or -1 on error.
     * 
     * @param attrVal a string representing a positive 
     * integer attribute value (can be <code>null</code>)
     * @return the integer represented by <code>attrVal</code> or 
     * -1 on error
     */
    public static int parsePositiveInteger(String attrVal) {
        int rv = parseInteger(attrVal);
        if (rv < 1) {
            return -1;
        } else {
            return rv;
        }
    }

    /**
     * Splits the argument on white space.
     * 
     * @param value the attribute value
     * @return a string array with zero or more strings none of which 
     * is the empty string and none of which contains white space characters.
     */
    public static String[] split(String value) {
        if (value == null || "".equals(value)) {
            return EMPTY_STRING_ARRAY;
        }
        int len = value.length();
        List<String> list = new LinkedList<String>();
        boolean collectingSpace = true;
        int start = 0;
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!collectingSpace) {
                    list.add(value.substring(start, i));
                    collectingSpace = true;
                }
            } else {
                if (collectingSpace) {
                    start = i;
                    collectingSpace = false;
                }
            }
        }
        if (start < len) {
            list.add(value.substring(start, len));
        }
        return list.toArray(EMPTY_STRING_ARRAY);
    }
    
    /**
     * Checks if <code>string</code> matches <code>lowerCaseLiteral</code> when
     * ASCII-lowercased.
     * @param lowerCaseLiteral a lower-case literal
     * @param string potentially mixed-case string or <code>null</code>
     * @return <code>true</code> if <code>string</code> matches 
     * <code>lowerCaseLiteral</code> when ASCII-lowercased
     */
    public static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(String lowerCaseLiteral,
            String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }
}
