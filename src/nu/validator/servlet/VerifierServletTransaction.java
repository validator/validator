/*
 * Copyright (c) 2005, 2006 Henri Sivonen
 * Copyright (c) 2007-2008 Mozilla Foundation
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

package nu.validator.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.DocumentMode;
import nu.validator.htmlparser.common.DocumentModeHandler;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.messages.JsonMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.messages.XhtmlMessageEmitter;
import nu.validator.messages.XmlMessageEmitter;
import nu.validator.source.SourceCode;
import nu.validator.spec.Spec;
import nu.validator.spec.html5.Html5SpecBuilder;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.CharacterUtil;
import nu.validator.xml.ContentTypeParser;
import nu.validator.xml.ForbiddenCharacterFilter;
import nu.validator.xml.HtmlSerializer;
import nu.validator.xml.IdFilter;
import nu.validator.xml.LocalCacheEntityResolver;
import nu.validator.xml.NamespaceDroppingXMLReaderWrapper;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.PrudentHttpEntityResolver;
import nu.validator.xml.SystemErrErrorHandler;
import nu.validator.xml.TypedInputSource;
import nu.validator.xml.WiretapXMLReaderWrapper;
import nu.validator.xml.XhtmlSaxEmitter;

import org.apache.log4j.Logger;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.whattf.checker.DebugChecker;
import org.whattf.checker.NormalizationChecker;
import org.whattf.checker.TextContentChecker;
import org.whattf.checker.UncheckedSubtreeWarner;
import org.whattf.checker.UsemapChecker;
import org.whattf.checker.jing.CheckerValidator;
import org.whattf.checker.table.TableChecker;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

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


/**
 * @version $Id: VerifierServletTransaction.java,v 1.10 2005/07/24 07:32:48
 *          hsivonen Exp $
 * @author hsivonen
 */
class VerifierServletTransaction implements DocumentModeHandler {

    private enum OutputFormat {
        HTML, XHTML, TEXT, XML, JSON, RELAXED, SOAP, UNICORN, EMACS
    }

    private static final Logger log4j = Logger.getLogger(VerifierServletTransaction.class);

    private static final Pattern SPACE = Pattern.compile("\\s+");
    
    private static final Pattern JS_IDENTIFIER = Pattern.compile("[\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Nl}_\\$][\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Nl}_\\$\\p{Mn}\\p{Mc}\\p{Nd}\\p{Pc}]*");

    private static final String[] JS_RESERVED_WORDS = { "abstract", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "debugger", "default", "delete", "do", "double",
            "else", "enum", "export", "extends", "final", "finally", "float",
            "for", "function", "goto", "if", "implements", "import", "in",
            "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short",
            "static", "super", "switch", "synchronized", "this", "throw",
            "throws", "transient", "try", "typeof", "var", "void", "volatile",
            "while", "with" };
    
    private static final String[] CHARSETS = {
        "UTF-8",
        "UTF-16",
        "Windows-1250",
        "Windows-1251",
        "Windows-1252",
        "Windows-1253",
        "Windows-1254",
        "Windows-1255",
        "Windows-1256",
        "Windows-1257",
        "Windows-1258",
        "ISO-8859-1",
        "ISO-8859-2",
        "ISO-8859-3",
        "ISO-8859-4",
        "ISO-8859-5",
        "ISO-8859-6",
        "ISO-8859-7",
        "ISO-8859-8",
        "ISO-8859-9",
        "ISO-8859-13",
        "ISO-8859-15",
        "KOI8-R",
        "TIS-620",
        "GBK",
        "GB18030",
        "Big5",
        "Big5-HKSCS",
        "Shift_JIS",
        "ISO-2022-JP",
        "EUC-JP",
        "ISO-2022-KR",
        "EUC-KR"
    };

    private static final char[][] CHARSET_DESCRIPTIONS = {
        "UTF-8 (Global)".toCharArray(),
        "UTF-16 (Global)".toCharArray(),
        "Windows-1250 (Central European)".toCharArray(),
        "Windows-1251 (Cyrillic)".toCharArray(),
        "Windows-1252 (Western)".toCharArray(),
        "Windows-1253 (Greek)".toCharArray(),
        "Windows-1254 (Turkish)".toCharArray(),
        "Windows-1255 (Hebrew)".toCharArray(),
        "Windows-1256 (Arabic)".toCharArray(),
        "Windows-1257 (Baltic)".toCharArray(),
        "Windows-1258 (Vietnamese)".toCharArray(),
        "ISO-8859-1 (Western)".toCharArray(),
        "ISO-8859-2 (Central European)".toCharArray(),
        "ISO-8859-3 (South European)".toCharArray(),
        "ISO-8859-4 (Baltic)".toCharArray(),
        "ISO-8859-5 (Cyrillic)".toCharArray(),
        "ISO-8859-6 (Arabic)".toCharArray(),
        "ISO-8859-7 (Greek)".toCharArray(),
        "ISO-8859-8 (Hebrew)".toCharArray(),
        "ISO-8859-9 (Turkish)".toCharArray(),
        "ISO-8859-13 (Baltic)".toCharArray(),
        "ISO-8859-15 (Western)".toCharArray(),
        "KOI8-R (Russian)".toCharArray(),
        "TIS-620 (Thai)".toCharArray(),
        "GBK (Chinese, simplified)".toCharArray(),
        "GB18030 (Chinese, simplified)".toCharArray(),
        "Big5 (Chinese, traditional)".toCharArray(),
        "Big5-HKSCS (Chinese, traditional)".toCharArray(),
        "Shift_JIS (Japanese)".toCharArray(),
        "ISO-2022-JP (Japanese)".toCharArray(),
        "EUC-JP (Japanese)".toCharArray(),
        "ISO-2022-KR (Korean)".toCharArray(),
        "EUC-KR (Korean)".toCharArray()
    };
        
    protected static final int HTML5_SCHEMA = 3;

    protected static final int XHTML1STRICT_SCHEMA = 2;

    protected static final int XHTML1TRANSITIONAL_SCHEMA = 1;

    protected static final int XHTML5_SCHEMA = 7;

    private static final char[] SERVICE_TITLE = (System.getProperty(
            "nu.validator.servlet.service-name", "Validator.nu") + " ").toCharArray();

    private static final char[] VERSION = "3".toCharArray();

    private static final char[] RESULTS_TITLE = "Validation results".toCharArray();

    private static final char[] FOR = " for ".toCharArray();

    private static final char[] ABOUT_THIS_SERVICE = "About this service".toCharArray();

    private static final Map pathMap = new HashMap();

    private static Spec html5spec;
    
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
            "http://c.validator.nu/table/",
            "http://c.validator.nu/nfc/",
            "http://c.validator.nu/text-content/",
            "http://c.validator.nu/unchecked/",
            "http://c.validator.nu/usemap/"};

    private static final String[] ALL_CHECKERS_HTML4 = {
            "http://c.validator.nu/table/",
            "http://c.validator.nu/nfc/",
            "http://c.validator.nu/unchecked/",
            "http://c.validator.nu/usemap/"};

    private long start = System.currentTimeMillis();

    protected final HttpServletRequest request;

    private final HttpServletResponse response;

    private IRIFactory iriFactory;

    protected String document = null;

    private ParserMode parser = ParserMode.AUTO;

    private boolean laxType = false;

    protected ContentHandler contentHandler;

    protected XhtmlSaxEmitter emitter;

    protected MessageEmitterAdapter errorHandler;

    private AttributesImpl attrs = new AttributesImpl();

    private OutputStream out;

    private PropertyMap jingPropertyMap;

    protected LocalCacheEntityResolver entityResolver;

    private static long lastModified;

    private static String[] preloadedSchemaUrls;

    private static Schema[] preloadedSchemas;
    
    private final static String ABOUT_PAGE = System.getProperty("nu.validator.servlet.about-page", "http://about.validator.nu/");

    private final static String STYLE_SHEET = System.getProperty("nu.validator.servlet.style-sheet", "http://about.validator.nu/style.css");

    private final static String SCRIPT = System.getProperty("nu.validator.servlet.script", "http://about.validator.nu/script.js");

    private String schemaUrls = null;

    protected Validator validator = null;

    private BufferingRootNamespaceSniffer bufferingRootNamespaceSniffer = null;

    private String contentType = null;

    protected HtmlParser htmlParser = null;

    protected SAXDriver xmlParser = null;
    
    protected XMLReader reader;

    protected TypedInputSource documentInput;

    protected PrudentHttpEntityResolver httpRes;

    protected ContentTypeParser contentTypeParser;

    private Set<String> loadedValidatorUrls = new HashSet<String>();

    private boolean checkNormalization = false;

    private boolean rootNamespaceSeen = false;

    private OutputFormat outputFormat;

    private String postContentType;

    private boolean methodIsGet;

    private SourceCode sourceCode = new SourceCode();

    private boolean showSource;
    
    private String charsetOverride = null;
    
    private Set<String> filteredNamespaces = new LinkedHashSet<String>(); // linked for UI stability

    static {
        try {
            log4j.debug("Starting static initializer.");

            String presetPath = System.getProperty("nu.validator.servlet.presetconfpath");
            File presetFile = new File(presetPath);
            lastModified = presetFile.lastModified();
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new FileInputStream(presetFile), "UTF-8"));
            String line;
            List<String> doctypes = new LinkedList<String>();
            List<String> namespaces = new LinkedList<String>();
            List<String> labels = new LinkedList<String>();
            List<String> urls = new LinkedList<String>();

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

            String[] presetDoctypesAsStrings = doctypes.toArray(new String[0]);
            presetNamespaces = namespaces.toArray(new String[0]);
            presetLabels = labels.toArray(new String[0]);
            presetUrls = urls.toArray(new String[0]);

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

            String prefix = System.getProperty("nu.validator.servlet.cachepathprefix");

            log4j.debug("The cache path prefix is: " + prefix);

            String cacheConfPath = System.getProperty("nu.validator.servlet.cacheconfpath");

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

            SortedMap<String, Schema> schemaMap = new TreeMap<String, Schema>();
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

            log4j.debug("Reading spec.");

            html5spec = Html5SpecBuilder.parseSpec();

            log4j.debug("Spec read.");
            
            log4j.debug("Initialization complete.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static String scrub(CharSequence s) {
        return Normalizer.normalize(
                CharacterUtil.prudentlyScrubCharacterData(s), Normalizer.NFC);
    }

    private static boolean isCheckerUrl(String url) {
        if ("http://c.validator.nu/all/".equals(url) || "http://hsivonen.iki.fi/checkers/all/".equals(url)) {
            return true;
        } else if ("http://c.validator.nu/all-html4/".equals(url) || "http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
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
        if (methodIsGet) {
            return document != null;
        } else { // POST
            return true;
        }
    }

    void service() throws ServletException, IOException {
        this.methodIsGet = "GET".equals(request.getMethod())
                || "HEAD".equals(request.getMethod());

        this.out = response.getOutputStream();

        request.setCharacterEncoding("utf-8");

        if (!methodIsGet) {
            postContentType = request.getContentType();
            if (postContentType == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Content-Type missing");
                return;
            } else if (postContentType.trim().toLowerCase().startsWith("application/x-www-form-urlencoded")) {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                "application/x-www-form-urlencoded not supported. Please use multipart/form-data.");
                return;                
            }
        }

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
            } else if ("xml".equals(outFormat)) {
                outputFormat = OutputFormat.XML;
            } else if ("json".equals(outFormat)) {
                outputFormat = OutputFormat.JSON;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Unsupported output format");
                return;
            }
        }

        if (!methodIsGet) {
            document = scrubUrl(request.getHeader("Content-Location"));
        }
        if (document == null) {
            document = scrubUrl(request.getParameter("doc"));
        }

        document = ("".equals(document)) ? null : document;

        String callback = null;
        if (outputFormat == OutputFormat.JSON) {
            callback = request.getParameter("callback");
            if (callback != null) {
                Matcher m = JS_IDENTIFIER.matcher(callback);
                if (m.matches()) {
                    if (Arrays.binarySearch(JS_RESERVED_WORDS, callback) >= 0) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                "Callback is a reserved word.");
                        return;
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Callback is not a valid ECMA 262 IdentifierName.");
                    return;
                }
            }
        }

        String methodCheck = request.getHeader("Method-Check");
        
        if (willValidate()) {
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache");
        } else if (methodCheck != null) {
            // XXX revisit if anne changes the access-control stuff to use OPTIONS
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.setHeader("Allow", "POST");
            return;
        } else if (outputFormat == OutputFormat.HTML
                || outputFormat == OutputFormat.XHTML) {
            response.setDateHeader("Last-Modified", lastModified);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No input document");
            return;
        }

        setup();

        showSource = (request.getParameter("showsource") != null);
        
        String charset = request.getParameter("charset");
        if (charset != null) {
            charset = scrub(charset.trim());
            if (!"".equals(charset)) {
                charsetOverride = charset;
            }
        }
        
        String nsfilter = request.getParameter("nsfilter");
        if (nsfilter != null) {
            String[] nsfilterArr = SPACE.split(nsfilter);
            for (int i = 0; i < nsfilterArr.length; i++) {
                String ns = nsfilterArr[i];
                if (ns.length() > 0) {
                    filteredNamespaces.add(ns);
                }
            }
        }
        
        try {
            if (outputFormat == OutputFormat.HTML
                    || outputFormat == OutputFormat.XHTML) {
                if (outputFormat == OutputFormat.HTML) {
                    response.setContentType("text/html; charset=utf-8");
                    contentHandler = new HtmlSerializer(out,
                            HtmlSerializer.DOCTYPE_HTML5, false, "UTF-8");
                } else {
                    response.setContentType("application/xhtml+xml");
                    Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
                    Serializer ser = SerializerFactory.getSerializer(props);
                    ser.setOutputStream(out);
                    contentHandler = new ForbiddenCharacterFilter(ser.asContentHandler());
                }
                emitter = new XhtmlSaxEmitter(contentHandler);
                errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                        new XhtmlMessageEmitter(contentHandler));
                PageEmitter.emit(contentHandler, this);
            } else {
                if (outputFormat == OutputFormat.TEXT) {
                    response.setContentType("text/plain; charset=utf-8");
                    errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                            new TextMessageEmitter(out));
                } else if (outputFormat == OutputFormat.XML) {
                    response.setContentType("application/xml");
                    Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
                    Serializer ser = SerializerFactory.getSerializer(props);
                    ser.setOutputStream(out);
                    errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                            new XmlMessageEmitter(new ForbiddenCharacterFilter(ser.asContentHandler())));
                } else if (outputFormat == OutputFormat.JSON) {
                    if (callback == null) {
                        response.setContentType("application/json");
                    } else {
                        response.setContentType("application/javascript");
                     }
                    errorHandler = new MessageEmitterAdapter(sourceCode, showSource,
                            new JsonMessageEmitter(
                                    new nu.validator.json.Serializer(out),
                                    callback));
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
            parser = ParserMode.HTML_AUTO;
        } else if ("xmldtd".equals(parserStr)) {
            parser = ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION;
        } else if ("xml".equals(parserStr)) {
            parser = ParserMode.XML_NO_EXTERNAL_ENTITIES;
        } else if ("html5".equals(parserStr)) {
            parser = ParserMode.HTML;
        } else if ("html4".equals(parserStr)) {
            parser = ParserMode.HTML401_STRICT;
        } else if ("html4tr".equals(parserStr)) {
            parser = ParserMode.HTML401_TRANSITIONAL;
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
        return !(schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-basic.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-strict.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-transitional.rnc") 
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-frameset.rnc") 
                || schemaUrls.startsWith("http://s.validator.nu/html5/html5full.rnc"));

    }

    /**
     * @throws SAXException
     */
    @SuppressWarnings("deprecation")
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
        contentTypeParser = new ContentTypeParser(errorHandler, laxType);
        entityResolver = new LocalCacheEntityResolver(pathMap, httpRes);
        setAllowRnc(true);
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

            setAllowRnc(false);

            loadDocAndSetupParser();

            reader.setErrorHandler(errorHandler); 
            // XXX set xml:id filter separately
            contentType = documentInput.getType();
            sourceCode.initialize(documentInput);
            if (validator == null) {
                checkNormalization = true;
            }
            if (checkNormalization) {
                reader.setFeature(
                        "http://xml.org/sax/features/unicode-normalization-checking",
                        true);
            }
            WiretapXMLReaderWrapper wiretap = new WiretapXMLReaderWrapper(
                    reader);
            ContentHandler recorder = sourceCode.getLocationRecorder();
            wiretap.setWiretapContentHander(recorder);
            wiretap.setWiretapLexicalHandler((LexicalHandler) recorder);
            reader = wiretap;
            if (htmlParser != null) {
                htmlParser.addCharacterHandler(sourceCode);
                htmlParser.setMappingLangToXmlLang(true);
                htmlParser.setErrorHandler(errorHandler.getExactErrorHandler());
                htmlParser.setTreeBuilderErrorHandlerOverride(errorHandler);
                errorHandler.setHtml(true);
            } else if (xmlParser != null) {
                // this must be after wiretap!
                if (!filteredNamespaces.isEmpty()) {
                    reader = new NamespaceDroppingXMLReaderWrapper(reader, filteredNamespaces);
                }
                xmlParser.setErrorHandler(errorHandler.getExactErrorHandler());
                xmlParser.lockErrorHandler();
            } else {
                throw new RuntimeException("Bug. Unreachable.");
            }
            if (charsetOverride != null) {
                String charset = documentInput.getEncoding();
                if (charset == null) {
                    errorHandler.warning(new SAXParseException("Overriding document character encoding from none to \u201C" + charsetOverride + "\u201D.", null));
                } else {
                    errorHandler.warning(new SAXParseException("Overriding document character encoding from \u201C" + charset + "\u201D to \u201C" + charsetOverride + "\u201D.", null));                    
                }
                documentInput.setEncoding(charsetOverride);
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
            errorHandler.internalError(
                    e,
                    "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
        } catch (Error e) {
            stats = false;
            log4j.error("Error, doc: " + document + " schema: " + schemaUrls
                    + " lax: " + laxType, e);
            errorHandler.internalError(
                    e,
                    "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
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
            case HTML_AUTO:
            case HTML:
            case HTML401_STRICT:
            case HTML401_TRANSITIONAL:
                if (isHtmlUnsafePreset()) {
                    String message = "The chosen preset schema is not appropriate for HTML.";
                    SAXException se = new SAXException(message);
                    errorHandler.schemaError(se);
                    throw se;
                }
                setAllowGenericXml(false);
                setAllowHtml(true);
                setAcceptAllKnownXmlTypes(false);
                setAllowXhtml(false);
                loadDocumentInput();
                newHtmlParser();
                DoctypeExpectation doctypeExpectation;
                int schemaId;
                switch (parser) {
                    case HTML:
                        doctypeExpectation = DoctypeExpectation.HTML;
                        schemaId = HTML5_SCHEMA;
                        break;
                    case HTML401_STRICT:
                        doctypeExpectation = DoctypeExpectation.HTML401_STRICT;
                        schemaId = XHTML1STRICT_SCHEMA;
                        break;
                    case HTML401_TRANSITIONAL:
                        doctypeExpectation = DoctypeExpectation.HTML401_TRANSITIONAL;
                        schemaId = XHTML1TRANSITIONAL_SCHEMA;
                        break;
                    default:
                        doctypeExpectation = DoctypeExpectation.AUTO;
                        schemaId = 0;
                        break;
                }
                htmlParser.setDoctypeExpectation(doctypeExpectation);
                htmlParser.setDocumentModeHandler(this);
                reader = htmlParser;
                if (validator == null) {
                    validator = validatorByDoctype(schemaId);
                }
                if (validator != null) {
                    reader.setContentHandler(validator.getContentHandler());
                }
                break;
            case XML_NO_EXTERNAL_ENTITIES:
            case XML_EXTERNAL_ENTITIES_NO_VALIDATION:
                setAllowGenericXml(true);
                setAllowHtml(false);
                setAcceptAllKnownXmlTypes(true);
                setAllowXhtml(true);
                loadDocumentInput();
                setupXmlParser();
                break;
            default:
                setAllowGenericXml(true);
                setAllowHtml(true);
                setAcceptAllKnownXmlTypes(true);
                setAllowXhtml(true);
                loadDocumentInput();
                if ("text/html".equals(documentInput.getType())) {
                    if (isHtmlUnsafePreset()) {
                        String message = "The Content-Type was \u201Ctext/html\u201D, but the chosen preset schema is not appropriate for HTML.";
                        SAXException se = new SAXException(message);
                        errorHandler.schemaError(se);
                        throw se;
                    }
                    errorHandler.info("The Content-Type was \u201Ctext/html\u201D. Using the HTML parser.");
                    newHtmlParser();
                    htmlParser.setDoctypeExpectation(DoctypeExpectation.AUTO);
                    htmlParser.setDocumentModeHandler(this);
                    reader = htmlParser;
                    if (validator != null) {
                        reader.setContentHandler(validator.getContentHandler());
                    }
                } else {
                    errorHandler.info("The Content-Type was \u201C"
                            + documentInput.getType()
                            + "\u201D. Using the XML parser (not resolving external entities).");
                    setupXmlParser();
                }
                break;
        }
    }

    /**
     * 
     */
    protected void newHtmlParser() {
        htmlParser = new HtmlParser();
        htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
        htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setMappingLangToXmlLang(true);
        htmlParser.setHtml4ModeCompatibleWithXhtml1Schemata(true);
    }

    protected Validator validatorByDoctype(int schemaId) throws SAXException,
            IOException, IncorrectSchemaException {
        if (schemaId == 0) {
            return null;
        }
        for (int i = 0; i < presetDoctypes.length; i++) {
            if (presetDoctypes[i] == schemaId) {
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
    protected void setupXmlParser() throws SAXNotRecognizedException,
            SAXNotSupportedException {
        xmlParser = new SAXDriver();
        xmlParser.setCharacterHandler(sourceCode);
        reader = new IdFilter(xmlParser);
        reader.setFeature(
                "http://xml.org/sax/features/string-interning",
                true);
        reader.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION);
        reader.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION);
        if (parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION) {
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
            if ("http://c.validator.nu/all/".equals(url) || "http://hsivonen.iki.fi/checkers/all/".equals(url)) {
                for (int j = 0; j < ALL_CHECKERS.length; j++) {
                    validator = combineValidatorByUrl(validator,
                            ALL_CHECKERS[j]);
                }
            } else if ("http://c.validator.nu/all-html4/".equals(url) || "http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
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
        if ("http://c.validator.nu/table/".equals(url) || "http://hsivonen.iki.fi/checkers/table/".equals(url)) {
            return new CheckerValidator(new TableChecker(), jingPropertyMap);
        } else if ("http://c.validator.nu/nfc/".equals(url) || "http://hsivonen.iki.fi/checkers/nfc/".equals(url)) {
            this.checkNormalization = true;
            return new CheckerValidator(new NormalizationChecker(),
                    jingPropertyMap);
        } else if ("http://c.validator.nu/debug/".equals(url) || "http://hsivonen.iki.fi/checkers/debug/".equals(url)) {
            return new CheckerValidator(new DebugChecker(), jingPropertyMap);
        } else if ("http://c.validator.nu/text-content/".equals(url) || "http://hsivonen.iki.fi/checkers/text-content/".equals(url)) {
            return new CheckerValidator(new TextContentChecker(),
                    jingPropertyMap);
        } else if ("http://c.validator.nu/usemap/".equals(url) || "http://n.validator.nu/checkers/usemap/".equals(url)) {
            return new CheckerValidator(new UsemapChecker(), jingPropertyMap);
        } else if ("http://c.validator.nu/unchecked/".equals(url)) {
            return new CheckerValidator(new UncheckedSubtreeWarner(), jingPropertyMap);
        }
        if ("http://s.validator.nu/xhtml5-rdf-svg-mathml.rnc".equals(url) || "http://s.validator.nu/html5/html5full.rnc".equals(url) || "http://s.validator.nu/html5/xhtml5full-xhtml.rnc".equals(url) || "http://syntax.whattf.org/relaxng/xhtml5full-xhtml.rnc".equals(url) || "http://syntax.whattf.org/relaxng/html5full.rnc".equals(url)) {
            errorHandler.setSpec(html5spec);
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
            if (document != null && document.length() > 0) {
                emitter.characters(FOR);
                emitter.characters(scrub(document));
            }
        } else {
            emitter.characters(SERVICE_TITLE);
            if (markupAllowed) {
                emitter.startElement("span");
                emitter.characters(VERSION);
                emitter.endElement("span");
            }
        }
    }

    void emitForm() throws SAXException {
        attrs.clear();
        attrs.addAttribute("method", "get");
        attrs.addAttribute("action", request.getRequestURL().toString());
        if (isSimple()) {
            attrs.addAttribute("class", "simple");            
        }
        attrs.addAttribute("onsubmit", "formSubmission()");
        emitter.startElement("form", attrs);
        emitFormContent();
        emitter.endElement("form");
    }

    protected boolean isSimple() {
        return false;
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
                "Space-separated list of schema IRIs. (Leave blank to let the service guess.)");
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
                "Absolute IRI (http or https only) of the document to be checked.");
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
                (parser == ParserMode.AUTO));
        emitter.option("XML; don\u2019t load external entities", "xml",
                (parser == ParserMode.XML_NO_EXTERNAL_ENTITIES));
        emitter.option("XML; load external entities", "xmldtd",
                (parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION));
        emitter.option("HTML; flavor from doctype", "html",
                (parser == ParserMode.HTML_AUTO));
        emitter.option("HTML5", "html5", (parser == ParserMode.HTML));
        emitter.option("HTML 4.01 Strict", "html4",
                (parser == ParserMode.HTML401_STRICT));
        emitter.option("HTML 4.01 Transitional", "html4tr",
                (parser == ParserMode.HTML401_TRANSITIONAL));
    }

    /**
     * @throws SAXException
     * 
     */
    void emitLaxTypeField() throws SAXException {
        emitter.checkbox("laxtype", "yes", laxType);
    }

    /**
     * @throws SAXException
     * 
     */
    void emitShowSourceField() throws SAXException {
        emitter.checkbox("showsource", "yes", showSource);
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

    public void documentMode(DocumentMode mode, String publicIdentifier,
            String systemIdentifier, boolean html4SpecificAdditionalErrorChecks)
            throws SAXException {
        if (validator == null) {
            try {
                if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("XHTML 1.0 Transitional doctype seen. Appendix C is not supported. Proceeding anyway for your convenience. The parser is still an HTML parser, so namespace processing is not performed and \u201Cxml:*\u201D attributes are not supported. Using the schema for XHTML 1.0 Transitional."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                                    : ""));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD XHTML 1.0 Strict//EN".equals(publicIdentifier)) {
                    errorHandler.info("XHTML 1.0 Strict doctype seen. Appendix C is not supported. Proceeding anyway for your convenience. The parser is still an HTML parser, so namespace processing is not performed and \u201Cxml:*\u201D attributes are not supported. Using the schema for XHTML 1.0 Strict."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                                    : ""));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("HTML 4.01 Transitional doctype seen. Using the schema for XHTML 1.0 Transitional."
                            + (html4SpecificAdditionalErrorChecks ? ""
                                    : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.01//EN".equals(publicIdentifier)) {
                    errorHandler.info("HTML 4.01 Strict doctype seen. Using the schema for XHTML 1.0 Strict."
                            + (html4SpecificAdditionalErrorChecks ? ""
                                    : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.0 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("Legacy HTML 4.0 Transitional doctype seen.  Please consider using HTML 4.01 Transitional instead. Proceeding anyway for your convenience with the schema for XHTML 1.0 Transitional."
                            + (html4SpecificAdditionalErrorChecks ? ""
                                    : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.0//EN".equals(publicIdentifier)) {
                    errorHandler.info("Legacy HTML 4.0 Strict doctype seen. Please consider using HTML 4.01 instead. Proceeding anyway for your convenience with the schema for XHTML 1.0 Strict."
                            + (html4SpecificAdditionalErrorChecks ? ""
                                    : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else {
                    errorHandler.info("Using the schema for HTML5."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                                    : ""));
                    validator = validatorByDoctype(HTML5_SCHEMA);
                }
            } catch (IOException ioe) {
                // At this point the schema comes from memory.
                throw new RuntimeException(ioe);
            } catch (IncorrectSchemaException e) {
                // At this point the schema comes from memory.
                throw new RuntimeException(e);
            }
            ContentHandler ch = validator.getContentHandler();
            ch.setDocumentLocator(htmlParser.getDocumentLocator());
            ch.startDocument();
            reader.setContentHandler(ch);
        } else {
            if (html4SpecificAdditionalErrorChecks) {
                errorHandler.info("HTML4-specific tokenization errors are enabled.");
            }
        }
    }

    /**
     * @param acceptAllKnownXmlTypes
     * @see nu.validator.xml.ContentTypeParser#setAcceptAllKnownXmlTypes(boolean)
     */
    protected void setAcceptAllKnownXmlTypes(boolean acceptAllKnownXmlTypes) {
        contentTypeParser.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
        httpRes.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
    }

    /**
     * @param allowGenericXml
     * @see nu.validator.xml.ContentTypeParser#setAllowGenericXml(boolean)
     */
    protected void setAllowGenericXml(boolean allowGenericXml) {
        contentTypeParser.setAllowGenericXml(allowGenericXml);
        httpRes.setAllowGenericXml(allowGenericXml);
    }

    /**
     * @param allowHtml
     * @see nu.validator.xml.ContentTypeParser#setAllowHtml(boolean)
     */
    protected void setAllowHtml(boolean allowHtml) {
        contentTypeParser.setAllowHtml(allowHtml);
        httpRes.setAllowHtml(allowHtml);
    }

    /**
     * @param allowRnc
     * @see nu.validator.xml.ContentTypeParser#setAllowRnc(boolean)
     */
    protected void setAllowRnc(boolean allowRnc) {
        contentTypeParser.setAllowRnc(allowRnc);
        httpRes.setAllowRnc(allowRnc);
        entityResolver.setAllowRnc(allowRnc);
    }

    /**
     * @param allowXhtml
     * @see nu.validator.xml.ContentTypeParser#setAllowXhtml(boolean)
     */
    protected void setAllowXhtml(boolean allowXhtml) {
        contentTypeParser.setAllowXhtml(allowXhtml);
        httpRes.setAllowXhtml(allowXhtml);
    }

    /**
     * @throws SAXException
     * @throws IOException
     */
    protected void loadDocumentInput() throws SAXException, IOException {
        if (methodIsGet) {
            documentInput = (TypedInputSource) entityResolver.resolveEntity(
                    null, document);
            errorHandler.setLoggingOk(true);
        } else { // POST
            documentInput = contentTypeParser.buildTypedInputSource(document,
                    null, postContentType);
            documentInput.setByteStream(request.getInputStream());
        }
    }

    void emitIncludes() throws SAXException {
        attrs.clear();
        attrs.addAttribute("src", SCRIPT);
        emitter.startElement("script", attrs);
        emitter.endElement("script");        
        attrs.clear();
        attrs.addAttribute("href", STYLE_SHEET);
        attrs.addAttribute("rel", "stylesheet");
        emitter.startElement("link", attrs);
        emitter.endElement("link");        
    }

    void emitAbout() throws SAXException {
        attrs.clear();
        attrs.addAttribute("href", ABOUT_PAGE);
        emitter.startElement("a", attrs);
        emitter.characters(ABOUT_THIS_SERVICE);
        emitter.endElement("a");
    }

    void emitNsfilterField() throws SAXException {
        attrs.clear();
        attrs.addAttribute("name", "nsfilter");
        attrs.addAttribute("id", "nsfilter");
        attrs.addAttribute("pattern", "(?:.+:.+(?:\\s+.+:.+)*)?");
        attrs.addAttribute(
                "title",
                "Space-separated namespace URIs for vocabularies to be filtered out.");
        if (!filteredNamespaces.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String ns : filteredNamespaces) {
                if (!first) {
                    sb.append(' ');
                }
                sb.append(ns);
                first = false;
            }
            attrs.addAttribute("value", scrub(sb));
        }
        emitter.startElement("input", attrs);
        emitter.endElement("input");
    }

    void maybeEmitNsfilterField() throws SAXException {
        NsFilterEmitter.emit(contentHandler, this);
    }

    void emitCharsetOptions() throws SAXException {
        boolean found = false;
        for (int i = 0; i < CHARSETS.length; i++) {
            String charset = CHARSETS[i];
            boolean selected = charset.equalsIgnoreCase(charsetOverride); // XXX use ASCII-caseinsensitivity
            emitter.option(CHARSET_DESCRIPTIONS[i], charset, selected);
            if (selected) {
                found = true;
            }
        }
        if (!found && charsetOverride != null) {
            emitter.option(charsetOverride, charsetOverride, true);
        }
    }

    void maybeEmitCharsetField() throws SAXException {
        CharsetEmitter.emit(contentHandler, this);
    }
}