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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class DmozDriver {

    /**
     */
    public static void main(String[] args) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        XMLReader parser = factory.newSAXParser().getXMLReader();
        
        DmozHandler domzHandler = new DmozHandler();
        
        parser.setContentHandler(domzHandler);
        parser.setFeature("http://xml.org/sax/features/string-interning", true);
        parser.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                InputSource is = new InputSource(new ByteArrayInputStream(new byte[0]));
                is.setPublicId(publicId);
                is.setSystemId(systemId);
                return is;
            }
            
        });
        parser.setErrorHandler(new ErrorHandler() {

            public void error(SAXParseException exception) throws SAXException {
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
            }});
        
        InputSource is = new InputSource(new GZIPInputStream(new FileInputStream(args[0])));
        
        parser.parse(is);
        
        Writer out = new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8");
        for (Map.Entry<String, String> entry : domzHandler.getTheMap().entrySet()) {
            out.write(entry.getKey());
            out.write('\t');
            out.write(entry.getValue());
            out.write('\n');
        }
    }

}
