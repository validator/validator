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

public interface JsonHandler {

    public void startDocument(String callback) throws SAXException;

    public void endDocument() throws SAXException;

    public void startArray() throws SAXException;

    public void endArray() throws SAXException;

    public void startObject() throws SAXException;

    public void key(String key) throws SAXException;

    public void endObject() throws SAXException;

    public void startString() throws SAXException;

    public void characters(char[] ch, int start, int length) throws SAXException;

    public void endString() throws SAXException;    
    
    public void string(String string) throws SAXException;    
    
    public void number(int number) throws SAXException;    
    
    public void number(long number) throws SAXException;    

    public void number(float number) throws SAXException;    

    public void number(double number) throws SAXException;    

    public void bool(boolean bool) throws SAXException;    

}
