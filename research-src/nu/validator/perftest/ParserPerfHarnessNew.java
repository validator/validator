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

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.io.Driver;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.htmlparser.sax.XmlSerializer;
import nu.validator.xml.NullEntityResolver;

import org.apache.xerces.impl.Version;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ParserPerfHarnessNew {

    private final long endTime;

    private final XMLReader reader;

    private final char[] testData;

    /**
     * @param endTime
     * @param reader
     * @param testFile
     * @throws IOException
     */
    public ParserPerfHarnessNew(long endTime, XMLReader reader, char[] testData)
            throws IOException {
        this.endTime = endTime;
        this.reader = reader;
        this.testData = testData;
    }

    private static char[] loadFileIntoArray(File testFile) throws IOException {
        Reader in = new InputStreamReader(new FileInputStream(testFile),
                "utf-8");
        StringBuilder sb = new StringBuilder();
        int c = 0;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
        }
        char[] rv = new char[sb.length()];
        sb.getChars(0, sb.length(), rv, 0);
        return rv;
    }

    public long runLoop() throws SAXException, IOException {
        long times = 0;
        while (System.currentTimeMillis() < endTime) {
            InputSource inputSource = new InputSource(new CharArrayReader(
                    testData));
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
    public static void main(String[] args) throws SAXException, IOException,
            ParserConfigurationException {
        long duration = Long.parseLong(args[1]) * 60000L;
        String path = args[2];
        
        char[] testData = loadFileIntoArray(new File(path));
        
        XmlSerializer ch = new XmlSerializer(new NullWriter());
        
        XMLReader reader = null;
        if ("h".equals(args[0])) {
            HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
            parser.setContentHandler(ch);
            parser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
            reader = parser;
        } else if ("t".equals(args[0])) {
            Driver driver = new Driver(new TokensToSax(ch));
            driver.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
            driver.setContentSpacePolicy(XmlViolationPolicy.ALLOW);
            driver.setNamePolicy(XmlViolationPolicy.ALLOW);
            driver.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
            reader = new DriverWrapper(driver);
        } else if ("a".equals(args[0])) {
            reader = new SAXDriver();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://xml.org/sax/features/string-interning", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setContentHandler(ch);
            reader.setEntityResolver(new NullEntityResolver());
       } else if ("n".equals(args[0])) {
            System.out.println(Version.getVersion());
            reader = new SAXParser();
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://xml.org/sax/features/string-interning", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setContentHandler(ch);
            reader.setEntityResolver(new NullEntityResolver());
        } else {
            System.out.println(Version.getVersion());
            reader = new SAXParser();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://xml.org/sax/features/string-interning", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setContentHandler(ch);
            reader.setEntityResolver(new NullEntityResolver());            
        }
        System.out.println("Warmup:");
        System.out.println((new ParserPerfHarnessNew(System.currentTimeMillis()
                + duration, reader, testData)).runLoop());
        System.gc();
        System.out.println("Real:");
        System.out.println((new ParserPerfHarnessNew(System.currentTimeMillis()
                + duration, reader, testData)).runLoop());
    }
}
