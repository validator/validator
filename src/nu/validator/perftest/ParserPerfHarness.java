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

package nu.validator.perftest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.saxtree.TreeBuilder;
import nu.validator.xml.NullEntityResolver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ParserPerfHarness {

    private final long endTime;
    
    private final XMLReader reader;
    
    private final File testFile;

    /**
     * @param endTime
     * @param reader
     * @param testFile
     */
    public ParserPerfHarness(long endTime, XMLReader reader,
            File testFile) {
        this.endTime = endTime;
        this.reader = reader;
        this.testFile = testFile;
    }
        
    public long runLoop() throws SAXException, IOException {
        long times = 0;
         while (System.currentTimeMillis() < endTime) {
//        for (int i = 0; i < 10000; i++) {
            InputSource inputSource = new InputSource(new InputStreamReader(new FileInputStream(
                    testFile), "utf-8"));
            inputSource.setEncoding("utf-8");
            reader.parse(inputSource);
            times++;
        }
        return times;
    }
    
    /**
     * @param args
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
        long duration = Long.parseLong(args[0]) * 60000L;
        boolean html = "h".equals(args[1]);
        String path = args[2];
        XMLReader reader = null;
        if (html) {
            HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
            parser.setContentHandler(new DefaultHandler());
            parser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
            reader = parser;
        } else {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            reader = factory.newSAXParser().getXMLReader();
            reader.setContentHandler(new DefaultHandler());
            reader.setEntityResolver(new NullEntityResolver());
        }
        System.out.println("Warmup:");
        System.out.println((new ParserPerfHarness(System.currentTimeMillis() + duration, reader, new File(path))).runLoop());
        System.gc();
        System.out.println("Real");
        System.out.println((new ParserPerfHarness(System.currentTimeMillis() + duration, reader, new File(path))).runLoop());
    }
}
