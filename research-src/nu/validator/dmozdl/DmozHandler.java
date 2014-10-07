/*
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.dmozdl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class DmozHandler implements ContentHandler {

    private SortedMap<String, String> theMap = new TreeMap<String, String>();

    private MessageDigest md;

    private IRIFactory fac = new IRIFactory();

    private boolean inCollectableTopic;

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {

    }

    public void endDocument() throws SAXException {

    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if ("http://dmoz.org/rdf" == uri && "Topic" == localName) {
            inCollectableTopic = false;
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {

    }

    public void processingInstruction(String target, String data)
            throws SAXException {

    }

    public void setDocumentLocator(Locator locator) {

    }

    public void skippedEntity(String name) throws SAXException {

    }

    public void startDocument() throws SAXException {
        inCollectableTopic = false;
    }

    private static String toHexString(byte[] md5) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < md5.length; i++) {
            byte b = md5[i];
            int asInt = ((int) b) & 0xFF;
            String s = Integer.toHexString(asInt);
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if ("http://dmoz.org/rdf" == uri) {
            if ("Topic" == localName) {
                String id = atts.getValue("http://www.w3.org/TR/RDF/", "id");
                if (id != null && !id.startsWith("Top/Adult")) {
                    inCollectableTopic = true;
                }
            } else if (inCollectableTopic && "link" == localName) {
                String resource = atts.getValue("http://www.w3.org/TR/RDF/",
                        "resource");
                if (resource != null) {
                    try {
                        IRI iri = fac.create(resource);
                        String u = iri.toASCIIString();
                        byte[] md5 = md.digest(u.getBytes("utf-8"));
                        String md5str = toHexString(md5);
                        theMap.put(md5str, u);
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {

    }

    /**
     * @throws NoSuchAlgorithmException 
     * 
     */
    public DmozHandler() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("MD5");
    }

    /**
     * Returns the theMap.
     * 
     * @return the theMap
     */
    public SortedMap<String, String> getTheMap() {
        return theMap;
    }

}
