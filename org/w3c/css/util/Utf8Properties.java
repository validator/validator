// $Id$
// From Jean-Guilhem Rouel
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

/**
 * This class extends the Properties class to provide the ability
 * to load properties file using UTF-8 encoding (original Java Properties class
 * uses ISO-8859-1, and unicode escaped characters)<br/>
 *
 * @see java.util.Properties
 */
public class Utf8Properties<K, V> extends Properties {

    /**
     * use serialVersionUID from JDK 1.1.X for interoperability
     */
    private static final long serialVersionUID = 5907218757225133892L;

    /**
     * Encoding used to read properties from a file
     */
    public static final String ENCODING = "UTF-8";

    /**
     * Characters used to write comment lines in a property file
     */
    private static final String COMMENT = "#!";

    /**
     * Possible Separator between key and value of a property in a property
     * file
     */
    private static final String keyValueSeparators = "=: \t\r\n\f";

    /**
     * Creates an empty property list with no default values.
     *
     * @see java.util.Properties#Properties()
     */
    public Utf8Properties() {
        this(null);
    }

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults the defaults.
     * @see java.util.Properties#Properties(java.util.Properties)
     */
    public Utf8Properties(Properties defaults) {
        this.defaults = defaults;
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * stream.  The stream is assumed to be using the UTF-8
     * character encoding or compatible.
     * Characters can be written with their unicode escape sequence.
     *
     * @param inStream the input stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if the input stream contains a
     *                                  malformed Unicode escape sequence.
     * @see java.util.Properties#load(java.io.InputStream)
     */
    public synchronized void load(InputStream inStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, ENCODING));
        String line = in.readLine();

        while (line != null) {
            line = removeWhiteSpaces(line);
            if (!line.equals("") && COMMENT.indexOf(line.charAt(0)) == -1) {
                // Removes the beginning separators
                String property = line;
                // Reads the whole property if it is on multiple lines
                while (continueLine(line)) {
                    property = property.substring(0, property.length() - 1);
                    line = in.readLine();
                    property += line;
                }
                //property = new String(property.getBytes(ENCODING), ENCODING);

                if (!property.equals("")) {
                    int endOfKey = 0;
                    // calculates the ending index of the key
                    int l = property.length();
                    while (endOfKey < l && (keyValueSeparators.indexOf(property.charAt(endOfKey)) == -1)) {
                        endOfKey++;
                    }
                    String key = property.substring(0, endOfKey);
                    String value = property.substring(endOfKey + 1, property.length());

                    key = loadConversion(key);
                    value = loadConversion(removeWhiteSpaces(value));

                    put(key, value);
                    //// For debugging only
                    //System.out.println("key: " + key);
                    //System.out.println("value: " + value);
                    //System.out.println("-----------");
                }
            }
            line = in.readLine();
        }
    }

    /**
     * A simple method to remove white spaces
     * at the beginning of a String
     *
     * @param line the String to treat
     * @return the same String without white spaces at the beginning
     */
    public static String removeWhiteSpaces(String line) {
        int index = 0;
        while (index < line.length() && keyValueSeparators.indexOf(line.charAt(index)) != -1) {
            index++;
        }
        return line.substring(index, line.length());
    }

    /**
     * Replaces all characters preceded by a '\' with the corresponding special
     * character and converts unicode escape sequences to their value
     *
     * @param line the String to treat
     * @return the converted line
     */
    private String loadConversion(String line) {
        StringBuilder val = new StringBuilder(line.length());

        int index = 0;

        // Replace all the "\." substrings with their corresponding
        // escaped characters
        for (; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '\\') {
                index++;
                currentChar = line.charAt(index);
                switch (currentChar) {
                    case 't':
                        currentChar = '\t';
                        break;
                    case 'r':
                        currentChar = '\r';
                        break;
                    case 'n':
                        currentChar = '\n';
                        break;
                    case 'f':
                        currentChar = '\f';
                        break;
                    case 'u':
                        index++;
                        // Read the xxxx
                        int value = 0;
                        for (int i = 0; i < 4; i++) {
                            currentChar = line.charAt(index++);
                            //System.out.println(currentChar);
                            switch (currentChar) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    value = (value << 4) + currentChar - '0';
                                    break;
                                case 'a':
                                case 'b':
                                case 'c':
                                case 'd':
                                case 'e':
                                case 'f':
                                    value = (value << 4) + 10 + currentChar - 'a';
                                    break;
                                case 'A':
                                case 'B':
                                case 'C':
                                case 'D':
                                case 'E':
                                case 'F':
                                    value = (value << 4) + 10 + currentChar - 'A';
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Malformed \\uxxxx encoding.");
                            }
                        }
                        // index must point on the last character of the escaped
                        // sequence to avoid missing the next character
                        index--;
                        currentChar = (char) value;
                    default:
                        break;
                }
            }
            val.append(currentChar);
        }

        return val.toString();
    }

    /**
     * Replaces special characters with their '2-chars' representation.<br/>
     * For example, '\n' becomes '\\' followed by 'n'
     *
     * @param line the String to treat
     * @return the resulting String
     */
    private String storeConversion(String line) {
        int length = line.length();
        StringBuilder outBuffer = new StringBuilder(length * 2);

        for (int i = 0; i < length; i++) {
            char currentChar = line.charAt(i);
            switch (currentChar) {
                case '\\':
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    outBuffer.append(currentChar);
                    break;
            }
        }
        return outBuffer.toString();
    }

    /**
     * Indicates wether the property continues on the next line or not
     *
     * @param line the beginning of the property that might be continued
     *             on the next line
     * @return true if the propertiy continues on the following line,
     *         false otherwise
     */
    private boolean continueLine(String line) {
        if (line != null && !line.equals("")) {
            return line.charAt(line.length() - 1) == '\\';
        }
        return false;
    }

    /**
     * The same method as java.util.Properties.store(...)
     *
     * @param out    an output stream
     * @param header a description of the property list
     * @see java.util.Properties#store(java.io.OutputStream, java.lang.String)
     */
    public void store(OutputStream out, String header) throws IOException {
        BufferedWriter output;
        output = new BufferedWriter(new OutputStreamWriter(out, ENCODING));
        if (header != null) {
            output.write('#' + header);
            output.newLine();
        }
        output.write('#' + (new Date()).toString());
        output.newLine();
        // we do not want that a Thread could modify this Utf8Properties
        // while storing it
        synchronized (this) {
            Enumeration e = keys();
            while (e.hasMoreElements()) {
                String key = storeConversion((String) e.nextElement());
                String val = storeConversion((String) get(key));

                output.write(key + '=' + val);
                output.newLine();
            }
        }
        output.flush();
    }

    public synchronized Set<Object> keySet() {
        return super.keySet();
    }
}
