/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2011 Mozilla Foundation
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

package org.whattf.checker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.UnicodeSet;

/**
 * Checks that the following constructs do not start with a composing character:
 * <ul>
 * <li>Local names of elements
 * <li>Local names of attributes
 * <li>Attribute values
 * <li>Declared namespace prefixes
 * <li>Declared namespace URIs
 * <li>PI targets
 * <li>PI data
 * <li>Concatenations of consecutive character data between element
 *  boundaries and PIs ignoring comments and CDATA section boundaries.
 * </ul>
 * <p>Checks that the following constructs are in the Unicode Normalization 
 * Form C. (It is assumed the normalization of the rest of the constructs 
 * is enforced by other means, such as checking the document source for 
 * normalization.)
 * <ul>
 * <li>Attribute values
 * <li>PI data
 * <li>Concatenations of consecutive character data between element
 *  boundaries and PIs ignoring comments and CDATA section boundaries.
 * </ul>
 * <p>All <code>String</code>s must be valid UTF-16!
 * <p>This class can also be used as a source code mode where the source 
 * code of the document is fed to <code>characters()</code>. The mode 
 * modifies the error messages appropriately.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class NormalizationChecker extends Checker {

    /**
     * A thread-safe set of composing characters as per Charmod Norm.
     */
    @SuppressWarnings("deprecation")
    private static final UnicodeSet COMPOSING_CHARACTERS = (UnicodeSet) new UnicodeSet(
            "[[:nfc_qc=maybe:][:^ccc=0:]]").freeze();
    // see http://sourceforge.net/mailarchive/message.php?msg_id=37279908

    /**
     * A buffer for holding sequences overlap the SAX buffer boundary.
     */
    private char[] buf = new char[128];

    /**
     * A holder for the original buffer (for the memory leak prevention 
     * mechanism).
     */
    private char[] bufHolder = null;    
    
    /**
     * The current used length of the buffer, i.e. the index of the first slot 
     * that does not hold current data.
     */
    private int pos;

    /**
     * Indicates whether the checker the next call to <code>characters()</code> 
     * is the first call in a run.
     */
    private boolean atStartOfRun;

    /**
     * Indicates whether the current run has already caused an error.
     */
    private boolean alreadyComplainedAboutThisRun;

    /**
     * Indicates whether error messages related to source code checking should 
     * be used.
     */
    private final boolean sourceTextMode;

    /**
     * Returns <code>true</code> if the argument is a composing BMP character 
     * or a surrogate and <code>false</code> otherwise.
     * 
     * @param c a UTF-16 code unit
     * @return <code>true</code> if the argument is a composing BMP character 
     * or a surrogate and <code>false</code> otherwise
     */
    private static boolean isComposingCharOrSurrogate(char c) {
        if (UCharacter.isHighSurrogate(c) || UCharacter.isLowSurrogate(c)) {
            return true;
        }
        return isComposingChar(c);
    }

    /**
     * Returns <code>true</code> if the argument is a composing character 
     * and <code>false</code> otherwise.
     * 
     * @param c a Unicode code point
     * @return <code>true</code> if the argument is a composing character 
     * <code>false</code> otherwise
     */
    private static boolean isComposingChar(int c) {
        return COMPOSING_CHARACTERS.contains(c);
    }

    /**
     * Returns <code>true</code> if the argument starts with a composing 
     * character and <code>false</code> otherwise.
     * 
     * @param str a string
     * @return <code>true</code> if the argument starts with a composing 
     * character and <code>false</code> otherwise.
     * @throws SAXException on malformed UTF-16
     */
    public static boolean startsWithComposingChar(String str)
            throws SAXException {
        if (str.length() == 0) {
            return false;
        }
        int first32;
        char first = str.charAt(0);
        if (UCharacter.isHighSurrogate(first)) {
            try {
                char second = str.charAt(1);
                first32 = UCharacter.getCodePoint(first, second);
            } catch (StringIndexOutOfBoundsException e) {
                throw new SAXException("Malformed UTF-16!");
            }
        } else {
            first32 = first;
        }
        return isComposingChar(first32);
    }

    /**
     * Constructor for non-source mode.
     */
    public NormalizationChecker() {
        this(false);
    }

    /**
     * Constructor with mode selection.
     * 
     * @param sourceTextMode whether the source text-related messages 
     * should be enabled.
     */
    public NormalizationChecker(boolean sourceTextMode) {
        super();
        this.sourceTextMode = sourceTextMode;
        reset();
    }

    /**
     * @see org.whattf.checker.Checker#reset()
     */
    public void reset() {
        atStartOfRun = true;
        alreadyComplainedAboutThisRun = false;
        pos = 0;
        if (bufHolder != null) {
            // restore the original small buffer to avoid leaking
            // memory if this checker is recycled
            buf = bufHolder;
            bufHolder = null;
        }
    }

    /**
     * In the normal mode, this method has the usual SAX semantics. In the 
     * source text mode, this method is used for reporting the source text.
     * 
     * @see org.whattf.checker.Checker#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (alreadyComplainedAboutThisRun) {
            return;
        }
        if (atStartOfRun) {
            char c = ch[start];
            if (pos == 1) {
                // there's a single high surrogate in buf
                if (isComposingChar(UCharacter.getCodePoint(buf[0], c))) {
                    warn("Text run starts with a composing character.");
                }
                atStartOfRun = false;
            } else {
                if (length == 1 && UCharacter.isHighSurrogate(c)) {
                    buf[0] = c;
                    pos = 1;
                    return;
                } else {
                    if (UCharacter.isHighSurrogate(c)) {
                        if (isComposingChar(UCharacter.getCodePoint(c,
                                ch[start + 1]))) {
                            warn("Text run starts with a composing character.");
                        }
                    } else {
                        if (isComposingCharOrSurrogate(c)) {
                            warn("Text run starts with a composing character.");
                        }
                    }
                    atStartOfRun = false;
                }
            }
        }
        int i = start;
        int stop = start + length;
        if (pos > 0) {
            // there's stuff in buf
            while (i < stop && isComposingCharOrSurrogate(ch[i])) {
                i++;
            }
            appendToBuf(ch, start, i);
            if (i == stop) {
                return;
            } else {
                if (!Normalizer.isNormalized(buf, 0, pos, Normalizer.NFC, 0)) {
                    errAboutTextRun();
                }
                pos = 0;
            }
        }
        if (i < stop) {
            start = i;
            i = stop - 1;
            while (i > start && isComposingCharOrSurrogate(ch[i])) {
                i--;
            }
            if (i > start) {
                if (!Normalizer.isNormalized(ch, start, i, Normalizer.NFC, 0)) {
                    errAboutTextRun();
                }
            }
            appendToBuf(ch, i, stop);
        }
    }

    /**
     * Emits an error stating that the current text run or the source 
     * text is not in NFC.
     * 
     * @throws SAXException if the <code>ErrorHandler</code> throws
     */
    private void errAboutTextRun() throws SAXException {
        if (sourceTextMode) {
            warn("Source text is not in Unicode Normalization Form C.");
        } else {
            warn("Text run is not in Unicode Normalization Form C.");
        }
        alreadyComplainedAboutThisRun = true;
    }

    /**
     * Appends a slice of an UTF-16 code unit array to the internal 
     * buffer.
     * 
     * @param ch the array from which to copy
     * @param start the index of the first element that is copied
     * @param end the index of the first element that is not copied
     */
    private void appendToBuf(char[] ch, int start, int end) {
        if (start == end) {
            return;
        }
        int neededBufLen = pos + (end - start);
        if (neededBufLen > buf.length) {
            char[] newBuf = new char[neededBufLen];
            System.arraycopy(buf, 0, newBuf, 0, pos);
            if (bufHolder == null) {
                bufHolder = buf; // keep the original around
            }
            buf = newBuf;
        }
        System.arraycopy(ch, start, buf, pos, end - start);
        pos += (end - start);
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        flush();
    }

    /**
     * @see org.whattf.checker.Checker#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        flush();
        if (!"".equals(target)) {
            if (startsWithComposingChar(target)) {
                warn("Processing instruction target starts with a composing character.");
            }
        }
        if (!"".equals(data)) {
            if (startsWithComposingChar(data)) {
                warn("Processing instruction data starts with a composing character.");
            } else if (!Normalizer.isNormalized(data, Normalizer.NFC, 0)) {
                warn("Processing instruction data in not in Unicode Normalization Form C.");
            }
        }
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        flush();
        if (startsWithComposingChar(localName)) {
            warn("Element name \u201C " + localName
                    + "\u201D starts with a composing character.");
        }

        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String name = atts.getLocalName(i);
            if (startsWithComposingChar(name)) {
                warn("Attribute name \u201C " + localName
                        + "\u201D starts with a composing character.");
            }

            String value = atts.getValue(i);
            if (!"".equals(value)) {
                if (startsWithComposingChar(value)) {
                    warn("The value of attribute \u201C"
                            + atts.getLocalName(i)
                            + "\u201D"
                            + ("".equals(atts.getURI(i)) ? ""
                                    : " in namespace \u201C" + atts.getURI(i)
                                            + "\u201D") + " on element \u201C"
                            + localName + "\u201D from namespace \u201C" + uri
                            + "\u201D starts with a composing character.");
                } else if (!Normalizer.isNormalized(value, Normalizer.NFC, 0)) {
                    warn("The value of attribute \u201C"
                            + atts.getLocalName(i)
                            + "\u201D"
                            + ("".equals(atts.getURI(i)) ? ""
                                    : " in namespace \u201C" + atts.getURI(i)
                                            + "\u201D") + " on element \u201C"
                            + localName + "\u201D from namespace \u201C" + uri
                            + "\u201D is not in Unicode Normalization Form C.");
                }
            }
        }
    }

    /**
     * @see org.whattf.checker.Checker#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (startsWithComposingChar(prefix)) {
            warn("Namespace prefix \u201C " + prefix
                    + "\u201D starts with a composing character.");
        }
        if (startsWithComposingChar(uri)) {
            warn("Namespace URI \u201C " + uri
                    + "\u201D starts with a composing character.");
        }
    }

    /**
     * Called to indicate the end of a run of characters. When this class is 
     * used for checking source text, this method should be called after all 
     * the calls to <code>characters()</code>.
     * 
     * @throws SAXException if the <code>ErrorHandler</code> throws.
     */
    public void flush() throws SAXException {
        if (!alreadyComplainedAboutThisRun
                && !Normalizer.isNormalized(buf, 0, pos, Normalizer.NFC, 0)) {
            errAboutTextRun();
        }
        reset();
    }

}
