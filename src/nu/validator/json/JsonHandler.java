/*
 * Copyright (c) 2007 Mozilla Foundation
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

package nu.validator.json;

import org.xml.sax.SAXException;

/**
 * A SAX-inspired streaming interface for JSON. This interface is biased 
 * towards streaming writing whereas SAX is biased towards streaming 
 * parsing.
 * 
 * @version $Id$
 * @author hsivonen
 */
public interface JsonHandler {

    /**
     * Reports the start of the JSON file. When <code>callback</code> is
     * <code>null</code>, the file is a pure JSON file. With a non-<code>null</code> 
     * <code>callback</code>, a JSON value is wrapped in a function call named 
     * <var>callback</var>.
     * 
     * <p>Note that the JSON <i>null</i> value is represented as 
     * <code>string(null)</code>.
     * 
     * @param callback JavaScript callback function name or <code>null</code> for 
     * pure JSON.
     * @throws SAXException if bad things happen
     */
    public void startDocument(String callback) throws SAXException;

    /**
     * Reports the end of the JSON file. Must be called <code>finally</code>.
     * 
     * @throws SAXException if bad things happen
     */
    public void endDocument() throws SAXException;

    /**
     * Reports the start of an <i>array</i>.
     * 
     * @throws SAXException if bad things happen
     */
    public void startArray() throws SAXException;

    /**
     * Reports the end of an <i>array</i>.
     * 
     * @throws SAXException if bad things happen
     */
    public void endArray() throws SAXException;

    /**
     * Reports the start of an <i>object</i>.
     * 
     * @throws SAXException if bad things happen
     */    
    public void startObject() throws SAXException;

    /**
     * Starts a key-value pair inside an <i>object</i>. 
     * The parameter <code>key</code> gives the key and the next 
     * reported value is taken to be the value associated with 
     * the key. (Hence, there is no need for a corresponding 
     * <code>end</code> callback.)
     * 
     * @param key the key for the key-value pair (must not be <code>null</code>)
     * @throws SAXException if bad things happen
     */
    public void key(String key) throws SAXException;

    /**
     * Reports the end of an <i>object</i>.
     * 
     * @throws SAXException if bad things happen
     */    
    public void endObject() throws SAXException;

    /**
     * Reports the start of a <i>string</i>.
     * 
     * @throws SAXException if bad things happen
     */    
    public void startString() throws SAXException;

    /**
     * Adds characters to the current <i>string</i> started with 
     * <code>startString()</code>.
     * 
     * @param ch a buffer of UTF-16 code units
     * @param start the first code unit to read
     * @param length the number of code units to read
     * @throws SAXException if bad things happen
     */
    public void characters(char[] ch, int start, int length) throws SAXException;

    /**
     * Reports the end of a <i>string</i>.
     * 
     * @throws SAXException if bad things happen
     */    
    public void endString() throws SAXException;    
    
    /**
     * Reports a JSON <i>null</i> on <code>null</code> and 
     * a <i>string</i> otherwise.
     * 
     * <p>When the argument is not <code>null</code>, this method is 
     * shorthand for
     * <pre>startString();
     * characters(string.toCharArray(), 0, string.length());
     * endString();</pre>
     * 
     * @param string a string or <code>null</code>
     * @throws SAXException if bad things happen
     */
    public void string(String string) throws SAXException;    
    
    /**
     * Reports a <i>number</i>.
     * 
     * @param number the number
     * @throws SAXException if bad things happen
     */
    public void number(int number) throws SAXException;    
    
    /**
     * Reports a <i>number</i>.
     * 
     * @param number the number
     * @throws SAXException if bad things happen
     */
    public void number(long number) throws SAXException;    

    /**
     * Reports a <i>number</i>.
     * 
     * @param number the number
     * @throws SAXException if bad things happen
     */
    public void number(float number) throws SAXException;    

    /**
     * Reports a <i>number</i>.
     * 
     * @param number the number
     * @throws SAXException if bad things happen
     */
    public void number(double number) throws SAXException;    

    /**
     * Reports a <i>boolean</i>.
     * 
     * @param bool the boolean
     * @throws SAXException if bad things happen
     */
    public void bool(boolean bool) throws SAXException;    

}
