/*
 * Copyright (c) 2005, 2006 Henri Sivonen
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

package fi.iki.hsivonen.verifierservlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.java.dev.xmlidfilter.XMLIdFilter;

import org.apache.log4j.Logger;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.whattf.checker.DebugChecker;
import org.whattf.checker.NormalizationChecker;
import org.whattf.checker.SignificantInlineChecker;
import org.whattf.checker.TextContentChecker;
import org.whattf.checker.jing.CheckerValidator;
import org.whattf.checker.table.TableChecker;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.ibm.icu.text.Normalizer;
import com.thaiopensource.relaxng.impl.CombineValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;

import fi.iki.hsivonen.gnu.xml.aelfred2.SAXDriver;
import fi.iki.hsivonen.htmlparser.DoctypeHandler;
import fi.iki.hsivonen.htmlparser.HtmlParser;
import fi.iki.hsivonen.xml.AttributesImpl;
import fi.iki.hsivonen.xml.HtmlSerializer;
import fi.iki.hsivonen.xml.LocalCacheEntityResolver;
import fi.iki.hsivonen.xml.NullEntityResolver;
import fi.iki.hsivonen.xml.PrudentHttpEntityResolver;
import fi.iki.hsivonen.xml.SystemErrErrorHandler;
import fi.iki.hsivonen.xml.TypedInputSource;
import fi.iki.hsivonen.xml.XhtmlIdFilter;
import fi.iki.hsivonen.xml.XhtmlSaxEmitter;
import fi.karppinen.xml.CharacterUtil;

/**
 * @version $Id: VerifierServletTransaction.java,v 1.10 2005/07/24 07:32:48
 *          hsivonen Exp $
 * @author hsivonen
 */
class VerifierServletTransaction implements DoctypeHandler {
    
    private enum OutputFormat {
        HTML,
        XHTML,
        TEXT,
        XML,
        JSON,
        RELAXED,
        SOAP,
        UNICORN
    }
    
    private static final Logger log4j = Logger.getLogger(VerifierServletTransaction.class);

    private static final Pattern SPACE = Pattern.compile("\\s+");

    private static final int NO_EXTERNAL_ENTITIES = 4;

    private static final int EXTERNAL_ENTITIES_NO_VALIDATION = 5;

    private static final int HTML_PARSER = DoctypeHandler.ANY_DOCTYPE;

    private static final int HTML_PARSER_5 = DoctypeHandler.DOCTYPE_HTML5;

    private static final int HTML_PARSER_4_STRICT = DoctypeHandler.DOCTYPE_HTML401_STRICT;

    private static final int HTML_PARSER_4_TRANSITIONAL = DoctypeHandler.DOCTYPE_HTML401_TRANSITIONAL;

    private static final int AUTOMATIC_PARSER = 6;

    protected static final int XHTML5_SCHEMA = 7;

    private static final char[] SERVICE_TITLE = "Validation Service for RELAX NG ".toCharArray();

    private static final char[] TWO_POINT_OH_BETA = "2.0 Beta".toCharArray();

    private static final char[] RESULTS_TITLE = "Validation results for ".toCharArray();

    private static final Map pathMap = new HashMap();

    private static int[] presetDoctypes;

    private static String[] presetLabels;

    private static String[] presetUrls;

    private static String[] presetNamespaces;

    private static final String[] KNOWN_CONTENT_TYPES = {
            "application/atom+xml", "application/docbook+xml",
            "application/xhtml+xml", "application/xv+xml" };

    private static final String[] NAMESPACES_FOR_KNOWN_CONTENT_TYPES = {
            "http://www.w3.org/2005/Atom", "http://docbook.org/ns/docbook",
            "http://www.w3.org/1999/xhtml", "http://www.w3.org/1999/xhtml" };

    private static final String[] ALL_CHECKERS = {
        "http://hsivonen.iki.fi/checkers/table/",
        "http://hsivonen.iki.fi/checkers/nfc/",
        "http://hsivonen.iki.fi/checkers/significant-inline/",
        "http://hsivonen.iki.fi/checkers/text-content/"};

    private static final String[] ALL_CHECKERS_HTML4 = {
        "http://hsivonen.iki.fi/checkers/table/",
        "http://hsivonen.iki.fi/checkers/nfc/" };

    private long start = System.currentTimeMillis();

    private HttpServletRequest request;

    private HttpServletResponse response;

    private IRIFactory iriFactory;

    protected String document;

    private int parser = AUTOMATIC_PARSER;

    private boolean laxType = false;

    protected ContentHandler contentHandler;

    protected XhtmlSaxEmitter emitter;

    protected InfoErrorHandler errorHandler;

    private AttributesImpl attrs = new AttributesImpl();

    private OutputStream out;

    private PropertyMap jingPropertyMap;

    protected LocalCacheEntityResolver entityResolver;

    private static long lastModified;

    private static String[] preloadedSchemaUrls;

    private static Schema[] preloadedSchemas;

    private String schemaUrls = null;

    protected Validator validator = null;

    private BufferingRootNamespaceSniffer bufferingRootNamespaceSniffer = null;

    private String contentType = null;

    protected HtmlParser htmlParser = null;

    protected XMLReader reader;

    protected TypedInputSource documentInput;

    protected PrudentHttpEntityResolver httpRes;

    private Set loadedValidatorUrls = new HashSet();
    
    private boolean checkNormalization = false;

    private boolean rootNamespaceSeen = false;

    private OutputFormat outputFormat;

    static {
        try {
            log4j.debug("Starting static initializer.");

            String presetPath = System.getProperty("fi.iki.hsivonen.verifierservlet.presetconfpath");
            File presetFile = new File(presetPath);
            lastModified = presetFile.lastModified();
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new FileInputStream(presetFile), "UTF-8"));
            String line;
            List doctypes = new LinkedList();
            List namespaces = new LinkedList();
            List labels = new LinkedList();
            List urls = new LinkedList();

            log4j.debug("Starting to loop over config file lines.");

            while ((line = r.readLine()) != null) {
                if ("".equals(line.trim())) {
                    break;
                }
                String s[] = line.split("\t");
                doctypes.add(s[0]);
                namespaces.add(s[1]);
                labels.add(s[2]);
                urls.add(s[3]);
            }

            log4j.debug("Finished reading config.");

            String[] presetDoctypesAsStrings = (String[]) doctypes.toArray(new String[0]);
            presetNamespaces = (String[]) namespaces.toArray(new String[0]);
            presetLabels = (String[]) labels.toArray(new String[0]);
            presetUrls = (String[]) urls.toArray(new String[0]);

            log4j.debug("Converted config to arrays.");

            for (int i = 0; i < presetNamespaces.length; i++) {
                String str = presetNamespaces[i];
                if ("-".equals(str)) {
                    presetNamespaces[i] = null;
                } else {
                    presetNamespaces[i] = presetNamespaces[i].intern();
                }
            }

            log4j.debug("Prepared namespace array.");

            presetDoctypes = new int[presetDoctypesAsStrings.length];
            for (int i = 0; i < presetDoctypesAsStrings.length; i++) {
                presetDoctypes[i] = Integer.parseInt(presetDoctypesAsStrings[i]);
            }

            log4j.debug("Parsed doctype numbers into ints.");

            String prefix = System.getProperty("fi.iki.hsivonen.verifierservlet.cachepathprefix");

            log4j.debug("The cache path prefix is: " + prefix);

            String cacheConfPath = System.getProperty("fi.iki.hsivonen.verifierservlet.cacheconfpath");

            log4j.debug("The cache config path is: " + cacheConfPath);

            r = new BufferedReader(new InputStreamReader(new FileInputStream(
                    cacheConfPath), "UTF-8"));
            while ((line = r.readLine()) != null) {
                if ("".equals(line.trim())) {
                    break;
                }
                String s[] = line.split("\t");
                pathMap.put(s[0], prefix + s[1]);
            }

            log4j.debug("Cache config read.");

            ErrorHandler eh = new SystemErrErrorHandler();
            LocalCacheEntityResolver er = new LocalCacheEntityResolver(pathMap,
                    new NullEntityResolver());
            er.setAllowRnc(true);
            PropertyMapBuilder pmb = new PropertyMapBuilder();
            pmb.put(ValidateProperty.ERROR_HANDLER, eh);
            pmb.put(ValidateProperty.ENTITY_RESOLVER, er);
            pmb.put(ValidateProperty.XML_READER_CREATOR,
                    new VerifierServletXMLReaderCreator(eh, er));
            RngProperty.CHECK_ID_IDREF.add(pmb);
            PropertyMap pMap = pmb.toPropertyMap();

            log4j.debug("Parsing set up. Starting to read schemas.");

            SortedMap schemaMap = new TreeMap();
            for (int i = 0; i < presetUrls.length; i++) {
                String[] urls1 = SPACE.split(presetUrls[i]);
                for (int j = 0; j < urls1.length; j++) {
                    String url = urls1[j];
                    if (schemaMap.get(url) == null && !isCheckerUrl(url)) {
                        Schema sch = schemaByUrl(url, er, pMap);
                        schemaMap.put(url, sch);
                    }
                }
            }

            log4j.debug("Schemas read.");

            preloadedSchemaUrls = new String[schemaMap.size()];
            preloadedSchemas = new Schema[schemaMap.size()];
            int i = 0;
            for (Iterator iter = schemaMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                preloadedSchemaUrls[i] = entry.getKey().toString().intern();
                preloadedSchemas[i] = (Schema) entry.getValue();
                i++;
            }

            log4j.debug("Initialization complete.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static String scrub(String s) {
        return Normalizer.normalize(
                CharacterUtil.prudentlyScrubCharacterData(s), Normalizer.NFC);
    }

    private static boolean isCheckerUrl(String url) {
        if ("http://hsivonen.iki.fi/checkers/all/".equals(url)) {
            return true;
        } else if ("http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
            return true;
        }
        for (int i = 0; i < ALL_CHECKERS.length; i++) {
            if (ALL_CHECKERS[i].equals(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param request
     * @param response
     */
    VerifierServletTransaction(HttpServletRequest request,
            HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.iriFactory = IRIFactory.iriImplementation();
    }

    protected boolean willValidate() {
        return document != null;
    }

    void doGet() throws ServletException, IOException {

        this.out = response.getOutputStream();

        request.setCharacterEncoding("utf-8");
        
        String outFormat = request.getParameter("out");
        if (outFormat == null) {
            outputFormat = OutputFormat.HTML;
        } else {
            if ("html".equals(outFormat)) {
                outputFormat = OutputFormat.HTML;                
            } else if ("xhtml".equals(outFormat)) {
                outputFormat = OutputFormat.XHTML;                                
            } else if ("text".equals(outFormat)) {
                outputFormat = OutputFormat.TEXT;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported output format");
                return;
            }
        }

        document = scrubUrl(request.getParameter("doc"));

        document = ("".equals(document)) ? null : document;
        
        if (willValidate()) {
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache");
        } else if (outputFormat == OutputFormat.HTML || outputFormat == OutputFormat.XHTML) {
            response.setDateHeader("Last-Modified", lastModified);            
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No input document");
            return;
        }

        setup();

        try {
            if (outputFormat == OutputFormat.HTML || outputFormat == OutputFormat.XHTML) {
                if (outputFormat == OutputFormat.HTML) {
                    response.setContentType("text/html; charset=utf-8");
                    contentHandler = new HtmlSerializer(out, HtmlSerializer.DOCTYPE_HTML5,
                            false, "UTF-8");                    
                } else {
                    response.setContentType("application/xhtml+xml");
                    Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
                    Serializer ser = SerializerFactory.getSerializer(props);
                    ser.setOutputStream(out);
                    contentHandler = ser.asContentHandler();
                }
                emitter = new XhtmlSaxEmitter(contentHandler);
                errorHandler = new XhtmlEmittingErrorHandler(contentHandler);                
                PageEmitter.emit(contentHandler, this);
            } else {
                if (outputFormat == OutputFormat.TEXT) {
                response.setContentType("text/plain; charset=utf-8");
                    CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
                    enc.onMalformedInput(CodingErrorAction.REPLACE);
                    enc.onUnmappableCharacter(CodingErrorAction.REPLACE);
                    errorHandler = new TextEmittingErrorHandler(
                            new OutputStreamWriter(out, enc));
                } else {
                    throw new RuntimeException("Unreachable.");
                }
                validate();
            }
        } catch (SAXException e) {
            throw new ServletException(e);
        }
    }

    /**
     * @throws ServletException
     */
    protected void setup() throws ServletException {
        String preset = request.getParameter("preset");

        if (preset != null && !"".equals(preset)) {
            schemaUrls = preset;
        } else {
            schemaUrls = request.getParameter("schema");
        }
        if (schemaUrls == null) {
            schemaUrls = "";
        }

        String parserStr = request.getParameter("parser");

        if ("html".equals(parserStr)) {
            parser = HTML_PARSER;
        } else if ("xmldtd".equals(parserStr)) {
            parser = EXTERNAL_ENTITIES_NO_VALIDATION;
        } else if ("xml".equals(parserStr)) {
            parser = NO_EXTERNAL_ENTITIES;
        } else if ("html5".equals(parserStr)) {
            parser = HTML_PARSER_5;
        } else if ("html4".equals(parserStr)) {
            parser = HTML_PARSER_4_STRICT;
        } else if ("html4tr".equals(parserStr)) {
            parser = HTML_PARSER_4_TRANSITIONAL;
        } // else auto

        laxType = (request.getParameter("laxtype") != null);
    }

    private boolean isHtmlUnsafePreset() {
        if ("".equals(schemaUrls)) {
            return false;
        }
        boolean preset = false;
        for (int i = 0; i < presetUrls.length; i++) {
            if (presetUrls[i].equals(schemaUrls)) {
                preset = true;
                break;
            }
        }
        if (!preset) {
            return false;
        }
        return !(schemaUrls.startsWith("http://hsivonen.iki.fi/xhtml-schema/xhtml-basic.rng")
                || schemaUrls.startsWith("http://hsivonen.iki.fi/xhtml-schema/xhtml-strict.rng")
                || schemaUrls.startsWith("http://hsivonen.iki.fi/xhtml-schema/xhtml-strict-wcag.rng")
                || schemaUrls.startsWith("http://hsivonen.iki.fi/xhtml-schema/xhtml-transitional.rng")
                || schemaUrls.startsWith("http://hsivonen.iki.fi/xhtml-schema/xhtml-transitional-wcag.rng") || schemaUrls.startsWith("http://syntax.whattf.org/relaxng/xhtml5full-html.rnc"));

    }

    /**
     * @throws SAXException
     */
    void validate() throws SAXException {
        if (!willValidate()) {
            return;
        }
        try {
            out.flush();
        } catch (IOException e1) {
            throw new SAXException(e1);
        }
        httpRes = new PrudentHttpEntityResolver(2048 * 1024, laxType,
                errorHandler);
        entityResolver = new LocalCacheEntityResolver(pathMap, httpRes);
        httpRes.setAllowRnc(true);
        entityResolver.setAllowRnc(true);
        boolean stats = (outputFormat == OutputFormat.HTML || outputFormat == OutputFormat.XHTML);
        try {
            this.errorHandler.start(document);
            PropertyMapBuilder pmb = new PropertyMapBuilder();
            pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
            pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
            pmb.put(ValidateProperty.XML_READER_CREATOR,
                    new VerifierServletXMLReaderCreator(errorHandler,
                            entityResolver));
            RngProperty.CHECK_ID_IDREF.add(pmb);
            jingPropertyMap = pmb.toPropertyMap();

            tryToSetupValidator();

            httpRes.setAllowRnc(false);
            entityResolver.setAllowRnc(false);

            loadDocAndSetupParser();

            reader.setErrorHandler(errorHandler);
            contentType = documentInput.getType();
            if (validator == null) {
                checkNormalization = true;
            }
            if (checkNormalization) {
                reader.setFeature("http://hsivonen.iki.fi/checkers/nfc/", true);
            }
            reader.parse(documentInput);
        } catch (SAXException e) {
            log4j.debug("SAXException", e);
        } catch (IOException e) {
            stats = false;
            log4j.info("IOException", e);
            errorHandler.ioError(e);
        } catch (IncorrectSchemaException e) {
            log4j.debug("IncorrectSchemaException", e);
            errorHandler.schemaError(e);
        } catch (RuntimeException e) {
            stats = false;
            log4j.error("RuntimeException, doc: " + document + " schema: "
                    + schemaUrls + " lax: " + laxType, e);
            errorHandler.internalError(e, "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
        } catch (Error e) {
            stats = false;
            log4j.error("Error, doc: " + document + " schema: " + schemaUrls
                    + " lax: " + laxType, e);
            errorHandler.internalError(e, "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
        } finally {
            errorHandler.end(successMessage(), failureMessage());
        }
        if (stats) {
            StatsEmitter.emit(contentHandler, this);
        }
    }

    /**
     * @return 
     * @throws SAXException
     */
    protected String successMessage() throws SAXException {
        return "The document validates according to the specified schema(s).";
    }

    protected String failureMessage() throws SAXException {
       return "There were errors.";
    }

    /**
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    protected void tryToSetupValidator() throws SAXException, IOException,
            IncorrectSchemaException {
        validator = validatorByUrls(schemaUrls);
    }

    /**
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    protected void loadDocAndSetupParser() throws SAXException, IOException,
            IncorrectSchemaException, SAXNotRecognizedException,
            SAXNotSupportedException {
        switch (parser) {
            case HTML_PARSER:
            case HTML_PARSER_5:
            case HTML_PARSER_4_STRICT:
            case HTML_PARSER_4_TRANSITIONAL:
                if (isHtmlUnsafePreset()) {
                    String message = "The chosen preset schema is not appropriate for HTML.";
                    SAXException se = new SAXException(message);
                    errorHandler.schemaError(se);
                    throw se;
                }
                httpRes.setAllowGenericXml(false);
                httpRes.setAllowHtml(true);
                httpRes.setAcceptAllKnownXmlTypes(false);
                httpRes.setAllowXhtml(false);
                documentInput = (TypedInputSource) entityResolver.resolveEntity(
                        null, document);
                htmlParser = new HtmlParser();
                htmlParser.setDoctypeMode(parser); // magic numbers!
                htmlParser.setDoctypeHandler(this);
                reader = htmlParser;
                if (validator == null) {
                    validator = validatorByDoctype(parser); // magic
                    // numbers!
                    // can still be null
                }
                if (validator != null) {
                    reader.setContentHandler(validator.getContentHandler());
                }
                break;
            case NO_EXTERNAL_ENTITIES:
            case EXTERNAL_ENTITIES_NO_VALIDATION:
                httpRes.setAllowGenericXml(true);
                httpRes.setAllowHtml(false);
                httpRes.setAcceptAllKnownXmlTypes(true);
                httpRes.setAllowXhtml(true);
                documentInput = (TypedInputSource) entityResolver.resolveEntity(
                        null, document);
                reader = setupXmlParser();
                break;
            default:
                httpRes.setAllowGenericXml(true);
                httpRes.setAllowHtml(true);
                httpRes.setAcceptAllKnownXmlTypes(true);
                httpRes.setAllowXhtml(true);
                documentInput = (TypedInputSource) entityResolver.resolveEntity(
                        null, document);
                if ("text/html".equals(documentInput.getType())) {
                    if (isHtmlUnsafePreset()) {
                        String message = "The Content-Type was \u201Ctext/html\u201D, but the chosen preset schema is not appropriate for HTML.";
                        SAXException se = new SAXException(message);
                        errorHandler.schemaError(se);
                        throw se;
                    }
                    errorHandler.info("The Content-Type was \u201Ctext/html\u201D. Using the HTML parser.");
                    htmlParser = new HtmlParser();
                    htmlParser.setDoctypeMode(DoctypeHandler.ANY_DOCTYPE);
                    htmlParser.setDoctypeHandler(this);
                    reader = htmlParser;
                    if (validator != null) {
                        reader.setContentHandler(validator.getContentHandler());
                    }
                } else {
                    errorHandler.info("The Content-Type was \u201C"
                            + documentInput.getType()
                            + "\u201D. Using the XML parser (not resolving external entities).");
                    reader = setupXmlParser();
                }
                break;
        }
    }

    protected Validator validatorByDoctype(int doctype) throws SAXException,
            IOException, IncorrectSchemaException {
        if (doctype == ANY_DOCTYPE) {
            return null;
        }
        for (int i = 0; i < presetDoctypes.length; i++) {
            if (presetDoctypes[i] == doctype) {
                return validatorByUrls(presetUrls[i]);
            }
        }
        throw new RuntimeException("Doctype mappings not initialized properly.");
    }

    /**
     * @param entityResolver2
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    protected XMLReader setupXmlParser() throws SAXNotRecognizedException,
            SAXNotSupportedException {
        XMLReader reader;
        reader = new SAXDriver();
        reader = new XhtmlIdFilter(new XMLIdFilter(reader));
        reader.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                parser == EXTERNAL_ENTITIES_NO_VALIDATION);
        reader.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                parser == EXTERNAL_ENTITIES_NO_VALIDATION);
        if (parser == EXTERNAL_ENTITIES_NO_VALIDATION) {
            reader.setEntityResolver(entityResolver);
        } else {
            reader.setEntityResolver(new NullEntityResolver());
        }
        if (validator == null) {
            bufferingRootNamespaceSniffer = new BufferingRootNamespaceSniffer(
                    this);
            reader.setContentHandler(bufferingRootNamespaceSniffer);
        } else {
            reader.setContentHandler(new RootNamespaceSniffer(this,
                    validator.getContentHandler()));
            reader.setDTDHandler(validator.getDTDHandler());
        }
        return reader;
    }

    /**
     * @param validator
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator validatorByUrls(String schemaList) throws SAXException,
            IOException, IncorrectSchemaException {
        Validator validator = null;
        String[] schemas = SPACE.split(schemaList);
        for (int i = schemas.length - 1; i > -1; i--) {
            String url = schemas[i];
            if ("http://hsivonen.iki.fi/checkers/all/".equals(url)) {
                for (int j = 0; j < ALL_CHECKERS.length; j++) {
                    validator = combineValidatorByUrl(validator,
                            ALL_CHECKERS[j]);
                }
            } else if ("http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
                    for (int j = 0; j < ALL_CHECKERS_HTML4.length; j++) {
                        validator = combineValidatorByUrl(validator,
                                ALL_CHECKERS_HTML4[j]);
                    }
            } else {
                validator = combineValidatorByUrl(validator, url);
            }
        }
        return validator;
    }

    /**
     * @param validator
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator combineValidatorByUrl(Validator validator, String url)
            throws SAXException, IOException, IncorrectSchemaException {
        if (!"".equals(url)) {
            Validator v = validatorByUrl(url);
            if (validator == null) {
                validator = v;
            } else {
                validator = new CombineValidator(v, validator);
            }
        }
        return validator;
    }

    /**
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator validatorByUrl(String url) throws SAXException,
            IOException, IncorrectSchemaException {
        if (loadedValidatorUrls.contains(url)) {
            return null;
        }
        loadedValidatorUrls.add(url);
        if ("http://hsivonen.iki.fi/checkers/table/".equals(url)) {
            return new CheckerValidator(new TableChecker(), jingPropertyMap);
        } else if ("http://hsivonen.iki.fi/checkers/nfc/".equals(url)) {
            this.checkNormalization = true;
            return new CheckerValidator(new NormalizationChecker(),
                    jingPropertyMap);
        } else if ("http://hsivonen.iki.fi/checkers/significant-inline/".equals(url)) {
            return new CheckerValidator(new SignificantInlineChecker(),
                    jingPropertyMap);
        } else if ("http://hsivonen.iki.fi/checkers/debug/".equals(url)) {
            return new CheckerValidator(new DebugChecker(),
                    jingPropertyMap);
        } else if ("http://hsivonen.iki.fi/checkers/text-content/".equals(url)) {
            return new CheckerValidator(new TextContentChecker(),
                    jingPropertyMap);
        }
        Schema sch = schemaByUrl(url);
        Validator validator = sch.createValidator(jingPropertyMap);
        return validator;
    }

    /**
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Schema schemaByUrl(String url) throws SAXException, IOException,
            IncorrectSchemaException {
        int i = Arrays.binarySearch(preloadedSchemaUrls, url);
        if (i > -1) {
            return preloadedSchemas[i];
        }

        TypedInputSource schemaInput = (TypedInputSource) entityResolver.resolveEntity(
                null, url);
        SchemaReader sr = null;
        if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
            sr = CompactSchemaReader.getInstance();
        } else {
            sr = new AutoSchemaReader();
        }
        Schema sch = sr.createSchema(schemaInput, jingPropertyMap);
        return sch;
    }

    /**
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private static Schema schemaByUrl(String url, EntityResolver resolver,
            PropertyMap pMap) throws SAXException, IOException,
            IncorrectSchemaException {
        log4j.debug("Will load schema: " + url);
        TypedInputSource schemaInput = (TypedInputSource) resolver.resolveEntity(
                null, url);
        SchemaReader sr = null;
        if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
            sr = CompactSchemaReader.getInstance();
        } else {
            sr = new AutoSchemaReader();
        }
        Schema sch = sr.createSchema(schemaInput, pMap);
        return sch;
    }

    /**
     * @throws SAXException
     */
    void emitTitle(boolean markupAllowed) throws SAXException {
        if (willValidate()) {
            emitter.characters(RESULTS_TITLE);
            emitter.characters(scrub(document));
        } else {
            emitter.characters(SERVICE_TITLE);
            if (markupAllowed) {
                emitter.startElement("span");
                emitter.characters(TWO_POINT_OH_BETA);
                emitter.endElement("span");
            }
        }
    }

    void emitForm() throws SAXException {
        attrs.clear();
        attrs.addAttribute("method", "get");
        attrs.addAttribute("action", request.getRequestURL().toString());
        attrs.addAttribute("onsubmit", "formSubmission()");
        emitter.startElement("form", attrs);
        emitFormContent();
        emitter.endElement("form");
    }

    /**
     * @throws SAXException
     */
    protected void emitFormContent() throws SAXException {
        FormEmitter.emit(contentHandler, this);
    }

    void emitSchemaField() throws SAXException {
        attrs.clear();
        attrs.addAttribute("name", "schema");
        attrs.addAttribute("id", "schema");
        attrs.addAttribute("onchange", "schemaChanged();");
        attrs.addAttribute("pattern", "(?:https?://.+(?:\\s+https?://.+)*)?");
        attrs.addAttribute(
                "title",
                "The schema field takes zero or more space-separated absolute IRIs (http or https only) of the schemas that the document is to be validated against. (When left blank, the service will attempt to pick schemas automatically.)");
        if (schemaUrls != null) {
            attrs.addAttribute("value", scrub(schemaUrls));
        }
        emitter.startElement("input", attrs);
        emitter.endElement("input");
    }

    void emitDocField() throws SAXException {
        attrs.clear();
        attrs.addAttribute("type", "url");
        attrs.addAttribute("name", "doc");
        attrs.addAttribute("id", "doc");
        attrs.addAttribute("pattern", "(?:https?://.+)?");
        attrs.addAttribute(
                "title",
                "The document field takes the absolute IRI (http or https only) of the document to be checked. (The document field can also be left blank in order to bookmark settings.)");
        if (document != null) {
            attrs.addAttribute("value", scrub(document));
        }
        emitter.startElement("input", attrs);
        emitter.endElement("input");
    }

    private String scrubUrl(String urlStr) {
        if (urlStr == null) {
            return null;
        }

        try {
            IRI iri = iriFactory.construct(urlStr);
            return iri.toASCIIString();
        } catch (IRIException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * @throws SAXException
     * 
     */
    void emitSchemaDuration() throws SAXException {
    }

    /**
     * @throws SAXException
     * 
     */
    void emitDocDuration() throws SAXException {
    }

    /**
     * @throws SAXException
     * 
     */
    void emitTotalDuration() throws SAXException {
        emitter.characters("" + (System.currentTimeMillis() - start));
    }

    /**
     * @throws SAXException
     * 
     */
    void emitPresetOptions() throws SAXException {
        for (int i = 0; i < presetUrls.length; i++) {
            emitter.option(presetLabels[i], presetUrls[i], false);
        }
    }

    /**
     * @throws SAXException
     * 
     */
    void emitParserOptions() throws SAXException {
        emitter.option("Automatically from Content-Type", "",
                (parser == AUTOMATIC_PARSER));
        emitter.option("XML; don\u2019t load external entities", "xml",
                (parser == NO_EXTERNAL_ENTITIES));
        emitter.option("XML; load external entities", "xmldtd",
                (parser == EXTERNAL_ENTITIES_NO_VALIDATION));
        emitter.option("HTML; flavor from doctype", "html",
                (parser == HTML_PARSER));
        emitter.option("HTML5", "html5", (parser == HTML_PARSER_5));
        emitter.option("HTML 4.01 Strict", "html4",
                (parser == HTML_PARSER_4_STRICT));
        emitter.option("HTML 4.01 Transitional", "html4tr",
                (parser == HTML_PARSER_4_TRANSITIONAL));
    }

    /**
     * @throws SAXException
     * 
     */
    void emitLaxTypeField() throws SAXException {
        emitter.checkbox("laxtype", "yes", laxType);
    }

    void rootNamespace(String namespace, Locator locator) throws SAXException {
        if (validator == null) {
            int index = -1;
            for (int i = 0; i < presetNamespaces.length; i++) {
                if (namespace.equals(presetNamespaces[i])) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                String message = "Cannot find preset schema for namespace: \u201C"
                        + namespace + "\u201D.";
                SAXException se = new SAXException(message);
                errorHandler.schemaError(se);
                throw se;
            }
            String label = presetLabels[index];
            String urls = presetUrls[index];
            errorHandler.info("Using the preset for " + label
                    + " based on the root namespace.");
            try {
                validator = validatorByUrls(urls);
            } catch (IOException ioe) {
                // At this point the schema comes from memory.
                throw new RuntimeException(ioe);
            } catch (IncorrectSchemaException e) {
                // At this point the schema comes from memory.
                throw new RuntimeException(e);
            }
            if (bufferingRootNamespaceSniffer == null) {
                throw new RuntimeException(
                        "Bug! bufferingRootNamespaceSniffer was null.");
            }
            bufferingRootNamespaceSniffer.setContentHandler(validator.getContentHandler());
        }

        if (!rootNamespaceSeen) {
            rootNamespaceSeen = true;
            if (contentType != null) {
                int i;
                if ((i = Arrays.binarySearch(KNOWN_CONTENT_TYPES, contentType)) > -1) {
                    if (!NAMESPACES_FOR_KNOWN_CONTENT_TYPES[i].equals(namespace)) {
                        String message = "\u201C"
                                + contentType
                                + "\u201D is not an appropriate Content-Type for a document whose root namespace is \u201C"
                                + namespace + "\u201D.";
                        SAXParseException spe = new SAXParseException(message,
                                locator);
                        errorHandler.warning(spe);
                    }
                }
            }
        }
    }

    public void doctype(int doctype) throws SAXException {
        if (validator == null) {
            try {
                validator = validatorByDoctype(doctype);
            } catch (IOException ioe) {
                // At this point the schema comes from memory.
                throw new RuntimeException(ioe);
            } catch (IncorrectSchemaException e) {
                // At this point the schema comes from memory.
                throw new RuntimeException(e);
            }
            switch (doctype) {
                case DoctypeHandler.DOCTYPE_HTML5:
                    errorHandler.info("HTML5 doctype seen. Running the HTML parser in the HTML5 mode and using the preset for "
                            + schemaLabelFromDoctype(doctype) + ".");
                    break;
                case DoctypeHandler.DOCTYPE_HTML401_STRICT:
                    errorHandler.info("HTML 4.01 Strict doctype seen. Running the HTML parser in the HTML 4.01 mode and using the preset for "
                            + schemaLabelFromDoctype(doctype) + ".");
                    break;
                case DoctypeHandler.DOCTYPE_HTML401_TRANSITIONAL:
                    errorHandler.info("HTML 4.01 Transitional doctype seen. Running the HTML parser in the HTML 4.01 mode and using the preset for "
                            + schemaLabelFromDoctype(doctype) + ".");
                    break;
            }
            htmlParser.setContentHandler(validator.getContentHandler());
            htmlParser.refireStart();
        } else {
            switch (doctype) {
                case DoctypeHandler.DOCTYPE_HTML5:
                    errorHandler.info("HTML5 doctype seen. Running the HTML parser in the HTML5 mode.");
                    break;
                case DoctypeHandler.DOCTYPE_HTML401_STRICT:
                    errorHandler.info("HTML 4.01 Strict doctype seen. Running the HTML parser in the HTML 4.01 mode.");
                    break;
                case DoctypeHandler.DOCTYPE_HTML401_TRANSITIONAL:
                    errorHandler.info("HTML 4.01 Transitional doctype seen. Running the HTML parser in the HTML 4.01 mode.");
                    break;
            }
        }
    }

    private String schemaLabelFromDoctype(int doctype) {
        for (int i = 0; i < presetDoctypes.length; i++) {
            if (doctype == presetDoctypes[i]) {
                return presetLabels[i];
            }
        }
        throw new RuntimeException("Bug: Bad magic number.");
    }
}