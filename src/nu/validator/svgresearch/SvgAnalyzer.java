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

package nu.validator.svgresearch;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParserFactory;

import nu.validator.xml.NullEntityResolver;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class SvgAnalyzer {

    /**
     * @param args
     * @throws Exception
     * @throws SAXException
     */
    public static void main(String[] args) throws SAXException, Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        XMLReader parser = factory.newSAXParser().getXMLReader();
        
        SvgAnalysisHandler analysisHandler = new SvgAnalysisHandler();
        
        parser.setContentHandler(analysisHandler);
        parser.setDTDHandler(analysisHandler);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", analysisHandler);
        parser.setProperty("http://xml.org/sax/properties/declaration-handler", analysisHandler);
        parser.setFeature("http://xml.org/sax/features/string-interning", true);
        parser.setEntityResolver(new NullEntityResolver());
        parser.setErrorHandler(new ErrorHandler() {

            public void error(SAXParseException exception) throws SAXException {
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
            }});
        
        File dir = new File(args[0]);
        File[] list = dir.listFiles();
        int nsError = 0;
        int encodingErrors = 0;
        int otherIllFormed = 0;
        double total = (double) list.length;
        for (int i = 0; i < list.length; i++) {
            File file = list[i];
            try {
                InputSource is = new InputSource(new FileInputStream(file));
                is.setSystemId(file.toURL().toExternalForm());
                parser.parse(is);
            } catch (SAXParseException e) {
                String msg = e.getMessage();
                if (msg.startsWith("The prefix ")) {
                    nsError++;
                } else if (msg.contains("Prefixed namespace bindings may not be empty.")) {
                    nsError++;
                } else if (msg.startsWith("Invalid byte ")) {
                    encodingErrors++;
                } else {
                    otherIllFormed++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        
        System.out.print("NS errors: ");
        System.out.println(((double)nsError)/total);        
        System.out.print("Encoding errors: ");
        System.out.println(((double)encodingErrors)/total);        
        System.out.print("Other WF errors: ");
        System.out.println(((double)otherIllFormed)/total);        
        
        analysisHandler.print();
    }

}
