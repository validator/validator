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

package nu.validator.batchresearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.DocumentMode;
import nu.validator.htmlparser.common.DocumentModeHandler;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.xml.AttributesPermutingXMLReaderWrapper;
import nu.validator.xml.dataattributes.DataAttributeDroppingSchemaWrapper;
import nu.validator.xml.ariaattributes.AriaAttributeDroppingSchemaWrapper;
import nu.validator.xml.langattributes.XmlLangAttributeDroppingSchemaWrapper;

import org.whattf.checker.jing.CheckerSchema;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.thaiopensource.relaxng.impl.CombineValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.CompactSchemaReader;

public class ValidationWorker implements Runnable, ErrorHandler,
        DocumentModeHandler {

    private static final Pattern[] PATTERNS = {
      Pattern.compile("Duplicate ID \u201C[^\u201D]*\u201D."),
      Pattern.compile("Bad value \u201C[^\u201D]*\u201D for attribute "),
      Pattern.compile("declares a duplicate ID value \u201C[^\u201D]*\u201D"),
      Pattern.compile("The hashed ID reference in attribute \u201Cusemap\u201D referred to \u201C[^\u201D]*\u201D,"),
    };
    
    private static final String[] REPLACEMENTS = {
        "Duplicate ID (consolidated).",
        "Bad value (consolidated) for attribute ",
        "declares a duplicate ID value (consolidated)",
        "The hashed ID reference in attribute \u201Cusemap\u201D referred to (consolidated),"
    };
    
    private static String replaceSpecificValues(String str) {
        if (str.startsWith("Malformed byte sequence: ")) {
            return "Malformed byte sequence.";
        } else if (str.startsWith("Unmappable byte sequence: ")) {
            return "Unmappable byte sequence.";
        }
        for (int i = 0; i < PATTERNS.length; i++) {
            Pattern p = PATTERNS[i];
            Matcher m = p.matcher(str);
            if (m.find()) {
                return m.replaceFirst(REPLACEMENTS[i]);
            }
        }
        return str;
    }
    
    private final CountingReadLine in;

    private final PrintWriter out;

    private final File rootDir;

    private final XMLReader parser;

    private final HashSet<String> parseErrors = new HashSet<String>();

    private final HashSet<String> validationErrors = new HashSet<String>();
    
    private String documentMode = null;

    private Set<Schema> schemas;

    /**
     * @param in
     * @param out
     * @param rootDir
     * @param resolver
     */
    public ValidationWorker(CountingReadLine in, PrintWriter out, File rootDir,
            Set<Schema> schemas) {
        this.in = in;
        this.out = out;
        this.rootDir = rootDir;
        this.schemas = schemas;
        this.parser = setupParser();
    }

    private Validator setupValidator(Set<Schema> schemas) {
        PropertyMapBuilder builder = new PropertyMapBuilder();
        builder.put(ValidateProperty.ERROR_HANDLER, new ErrorHandler() {

            public void error(SAXParseException exception) throws SAXException {
                validationErrors.add(replaceSpecificValues(exception.getMessage()));
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                // should not happen
                validationErrors.add(replaceSpecificValues(exception.getMessage()));
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
            }});
        PropertyMap map = builder.toPropertyMap();
        Validator rv = null;
        for (Schema schema : schemas) {
            Validator v = schema.createValidator(map);
            if (rv == null) {
                rv = v;
            } else {
                rv = new CombineValidator(rv, v);
            }
        }
        return rv;
    }

    private XMLReader setupParser() {
        HtmlParser htmlParser = new HtmlParser();
        htmlParser.setCommentPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentSpacePolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setMappingLangToXmlLang(true);
        htmlParser.setHeuristics(Heuristics.ALL);
        htmlParser.setDoctypeExpectation(DoctypeExpectation.NO_DOCTYPE_ERRORS);
        htmlParser.setCheckingNormalization(true);
        htmlParser.setDocumentModeHandler(this);
        XMLReader rv = new AttributesPermutingXMLReaderWrapper(htmlParser);
        rv.setErrorHandler(this);
        return rv;
    }

    public void run() {
        String inLine = null;
        for (;;) {
            String url = null;
            try {
                while ((inLine = in.readLine()) != null) {
                    parseErrors.clear();
                    validationErrors.clear();
                    documentMode = null;

                    Validator validator = setupValidator(schemas);
                    parser.setContentHandler(validator.getContentHandler());
                    
                    String md5;
                    String charset;
                    int firstTab = inLine.indexOf('\t');
                    int secondTab = inLine.indexOf('\t', firstTab + 1);
                    md5 = inLine.substring(0, firstTab);
                    url = inLine.substring(firstTab + 1, secondTab);
                    charset = inLine.substring(secondTab + 1, inLine.length());

                    InputSource is = new InputSource();

                    File top = new File(rootDir, md5.substring(0, 2));
                    File second = new File(top, md5.substring(2, 4));
                    File inFile = new File(second, md5 + ".gz");

                    is.setByteStream(new GZIPInputStream(new FileInputStream(
                            inFile)));
                    is.setSystemId(url);
                    if (!"null".equals(charset)) {
                        is.setEncoding(charset);
                    }
                    
                    parser.parse(is);

                    validator = null;
                    
                    StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    
                    if (parseErrors.isEmpty() && validationErrors.isEmpty()) {
                        if (!first) {
                            sb.append('\n');
                        } else {
                            first = false;                                
                        }
                        sb.append(url + '\t' + documentMode + "\tP\t"
                                + "NEITHER ERRORS");                        
                    }
                    if (parseErrors.isEmpty()) {
                        if (!first) {
                            sb.append('\n');
                        } else {
                            first = false;                                
                        }
                        sb.append(url + '\t' + documentMode + "\tP\t"
                                + "NO PARSE ERRORS");
                    } else {
                        for (String error : parseErrors) {
                            if (!first) {
                                sb.append('\n');
                            } else {
                                first = false;                                
                            }
                            sb.append(url + '\t' + documentMode + "\tP\t"
                                    + sanitize(error));
                        }
                    }
                    if (validationErrors.isEmpty()) {
                        if (!first) {
                            sb.append('\n');
                        } else {
                            first = false;                                
                        }
                        sb.append(url + '\t' + documentMode + "\tV\t"
                                + "NO VALIDATION ERRORS");
                    } else {
                        for (String error : validationErrors) {
                            if (!first) {
                                sb.append('\n');
                            } else {
                                first = false;                                
                            }
                            sb.append(url + '\t' + documentMode + "\tV\t"
                                    + sanitize(error));
                        }
                    }
                    out.println(sb.toString());
                }
                return;
            } catch (Throwable t) {
                System.err.println(url);
                t.printStackTrace();
            }
        }
    }

    private String sanitize(String error) {
        return error.replaceAll("[\t\r\n]", " ");
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0]), "utf-8"));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(
                new FileOutputStream(args[1])), "utf-8"), true);
        File rootDir = new File(args[2]);
        
        Set<Schema> schemas = new HashSet<Schema>();
        schemas.add(CheckerSchema.ASSERTION_SCH);
        schemas.add(CheckerSchema.NORMALIZATION_CHECKER);
        schemas.add(CheckerSchema.TABLE_CHECKER);
        schemas.add(CheckerSchema.TEXT_CONTENT_CHECKER);
        schemas.add(CheckerSchema.USEMAP_CHECKER);

        InputSource is = new InputSource((new File(args[3])).toURL().toExternalForm());
        SchemaReader sr = CompactSchemaReader.getInstance();
        schemas.add(new XmlLangAttributeDroppingSchemaWrapper(new DataAttributeDroppingSchemaWrapper(sr.createSchema(is, PropertyMap.EMPTY))));
        schemas.add(new XmlLangAttributeDroppingSchemaWrapper(new AriaAttributeDroppingSchemaWrapper(sr.createSchema(is, PropertyMap.EMPTY))));

        CountingReadLine countingReadLine = new CountingReadLine(in);
        
        for (int i = 0; i < 4; i++) {
            (new Thread(new ValidationWorker(countingReadLine, out, rootDir, schemas))).start();
        }
    }

    public void error(SAXParseException exception) throws SAXException {
        this.parseErrors.add(replaceSpecificValues(exception.getMessage()));
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        // This should never happen 
        this.parseErrors.add(replaceSpecificValues(exception.getMessage()));
    }

    public void warning(SAXParseException exception) throws SAXException {

    }

    public void documentMode(DocumentMode mode, String publicIdentifier,
            String systemIdentifier, boolean html4SpecificAdditionalErrorChecks)
            throws SAXException {
        switch (mode) {
            case ALMOST_STANDARDS_MODE:
                documentMode = "A";
                break;
            case QUIRKS_MODE:
                documentMode = "Q";
                break;
            case STANDARDS_MODE:
                if (publicIdentifier == null && systemIdentifier == null) {
                    documentMode = "H";
                } else {
                    documentMode = "S";
                }
                break;
        }
    }
    
}
