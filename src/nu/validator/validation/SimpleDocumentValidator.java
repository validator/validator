/*
 * Copyright (c) 2013-2015 Mozilla Foundation
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

package nu.validator.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

import nu.validator.checker.jing.CheckerSchema;
import nu.validator.checker.jing.CheckerValidator;
import nu.validator.checker.table.TableChecker;
import nu.validator.checker.ConformingButObsoleteWarner;
import nu.validator.checker.MicrodataChecker;
import nu.validator.checker.NormalizationChecker;
import nu.validator.checker.TextContentChecker;
import nu.validator.checker.UncheckedSubtreeWarner;
import nu.validator.checker.UnsupportedFeatureChecker;
import nu.validator.checker.UsemapChecker;
import nu.validator.checker.XmlPiChecker;
import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.localentities.LocalCacheEntityResolver;
import nu.validator.source.SourceCode;
import nu.validator.xml.dataattributes.DataAttributeDroppingSchemaWrapper;
import nu.validator.xml.langattributes.XmlLangAttributeDroppingSchemaWrapper;
import nu.validator.xml.roleattributes.RoleAttributeFilteringSchemaWrapper;
import nu.validator.xml.IdFilter;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.TypedInputSource;
import nu.validator.xml.WiretapXMLReaderWrapper;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import com.thaiopensource.relaxng.impl.CombineValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;

import org.apache.log4j.PropertyConfigurator;

/**
 * Simple validation interface.
 */
public class SimpleDocumentValidator {

    private LocalCacheEntityResolver entityResolver;

    private Schema mainSchema;

    private boolean hasHtml5Schema;

    private Schema assertionSchema;

    private Validator validator;

    private SourceCode sourceCode = new SourceCode();

    private HtmlParser htmlParser = null;

    private XMLReader htmlReader;

    private SAXDriver xmlParser;

    private XMLReader xmlReader;

    private LexicalHandler lexicalHandler;

    private Schema schemaByUrl(String schemaUrl, ErrorHandler errorHandler)
            throws Exception, SchemaReadException {
        PropertyMapBuilder pmb = new PropertyMapBuilder();
        pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
        pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
        pmb.put(ValidateProperty.XML_READER_CREATOR,
                new Jaxp11XMLReaderCreator());
        RngProperty.CHECK_ID_IDREF.add(pmb);
        PropertyMap jingPropertyMap = pmb.toPropertyMap();

        try {
            TypedInputSource schemaInput = (TypedInputSource) entityResolver.resolveEntity(
                    null, schemaUrl);
            SchemaReader sr;
            if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
                sr = CompactSchemaReader.getInstance();
            } else {
                sr = new AutoSchemaReader();
            }
            return sr.createSchema(schemaInput, jingPropertyMap);
        } catch (ClassCastException e) {
            throw new SchemaReadException(String.format(
                    "Failed to resolve schema URL \"%s\".", schemaUrl));
        }
    }

    public SimpleDocumentValidator() {
        this(true);
    }

    /* *
     * Constructs a <code>SimpleDocumentValidator</code>.
     * 
     * @param initializeLog4j <code>true</code> to initialize log4j,
     * <code>false</code> to not initialize log4j. Use this parameter to prevent
     * <code>SimpleDocumentValidator</code> from overwriting an existing log4j
     * configuration when calling <code>SimpleDocumentValidator</code> from an
     * application that already has log4j configured.
     */
    public SimpleDocumentValidator(boolean initializeLog4j) {
        if (initializeLog4j) {
            PropertyConfigurator.configure(SimpleDocumentValidator.class.getClassLoader().getResource(
                    "nu/validator/localentities/files/log4j.properties"));
        }

        this.entityResolver = new LocalCacheEntityResolver(
                new NullEntityResolver());
        this.entityResolver.setAllowRnc(true);
    }

    public SourceCode getSourceCode() {
        return this.sourceCode;
    }

    /* *
     * Prepares the main RelaxNG schema to use for document validation, by
     * retrieving a serialized schema instance from the copies of known
     * http://s.validator.nu/* schemas in the local entity cache packaged with
     * the validator code and creating a Schema instance from it. Also checks
     * for resolution of secondary schemas.
     * 
     * @param schemaUrl an http://s.validator.nu/* URL
     * 
     * @param errorHandler error handler for schema-error reporting
     * 
     * @throws SchemaReadException if retrieval of any schema fails
     */
    public void setUpMainSchema(String schemaUrl, ErrorHandler errorHandler)
            throws SAXException, Exception, SchemaReadException {
        Schema schema = schemaByUrl(schemaUrl, errorHandler);
        if (schemaUrl.contains("html5")) {
            try {
                assertionSchema = CheckerSchema.ASSERTION_SCH;
            } catch (Exception e) {
                throw new SchemaReadException(
                        "Failed to retrieve secondary schema.");
            }
            schema = new DataAttributeDroppingSchemaWrapper(schema);
            schema = new XmlLangAttributeDroppingSchemaWrapper(schema);
            schema = new RoleAttributeFilteringSchemaWrapper(schema);
            this.hasHtml5Schema = true;
            if ("http://s.validator.nu/html5-all.rnc".equals(schemaUrl)) {
                System.setProperty("nu.validator.schema.rdfa-full", "1");
            } else {
                System.setProperty("nu.validator.schema.rdfa-full", "0");
            }
        }
        this.mainSchema = schema;
    }

    /* *
     * Prepares a Validator instance along with HTML and XML parsers, and then
     * attaches the Validator instance and supplied ErrorHandler instance to the
     * parsers so that the ErrorHandler is used for processing of all document-
     * validation problems reported.
     * 
     * @param docValidationErrHandler error handler for doc-validation reporting
     * 
     * @param loadExternalEnts whether XML parser should load remote DTDs, etc.
     * 
     * @param noStream whether HTML parser should buffer instead of streaming
     */
    public void setUpValidatorAndParsers(ErrorHandler docValidationErrHandler,
            boolean noStream, boolean loadExternalEnts) throws SAXException {
        PropertyMapBuilder pmb = new PropertyMapBuilder();
        pmb.put(ValidateProperty.ERROR_HANDLER, docValidationErrHandler);
        pmb.put(ValidateProperty.XML_READER_CREATOR,
                new Jaxp11XMLReaderCreator());
        RngProperty.CHECK_ID_IDREF.add(pmb);
        PropertyMap jingPropertyMap = pmb.toPropertyMap();

        validator = this.mainSchema.createValidator(jingPropertyMap);

        if (this.hasHtml5Schema) {
            Validator assertionValidator = assertionSchema.createValidator(jingPropertyMap);
            validator = new CombineValidator(validator, assertionValidator);
            validator = new CombineValidator(validator, new CheckerValidator(
                    new TableChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new ConformingButObsoleteWarner(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new MicrodataChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new NormalizationChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new TextContentChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new UncheckedSubtreeWarner(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new UnsupportedFeatureChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new UsemapChecker(), jingPropertyMap));
            validator = new CombineValidator(validator, new CheckerValidator(
                    new XmlPiChecker(), jingPropertyMap));
        }

        htmlParser = new HtmlParser();
        htmlParser.addCharacterHandler(sourceCode);
        htmlParser.setCommentPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentSpacePolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setMappingLangToXmlLang(true);
        htmlParser.setHtml4ModeCompatibleWithXhtml1Schemata(true);
        htmlParser.setDoctypeExpectation(DoctypeExpectation.HTML);
        htmlParser.setHeuristics(Heuristics.ALL);
        htmlParser.setContentHandler(validator.getContentHandler());
        htmlParser.setErrorHandler(docValidationErrHandler);
        htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setMappingLangToXmlLang(true);
        htmlParser.setFeature(
                "http://xml.org/sax/features/unicode-normalization-checking",
                true);
        if (!noStream) {
            htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
        }
        htmlReader = getWiretap(htmlParser);

        xmlParser = new SAXDriver();
        xmlParser.setContentHandler(validator.getContentHandler());
        if (lexicalHandler != null) {
            xmlParser.setProperty(
                    "http://xml.org/sax/properties/lexical-handler",
                    (LexicalHandler) lexicalHandler);
        }
        xmlReader = new IdFilter(xmlParser);
        xmlReader.setFeature("http://xml.org/sax/features/string-interning",
                true);
        xmlReader.setContentHandler(validator.getContentHandler());
        xmlReader.setFeature(
                "http://xml.org/sax/features/unicode-normalization-checking",
                true);
        if (loadExternalEnts) {
            xmlReader.setEntityResolver(entityResolver);
        } else {
            xmlReader.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);
            xmlReader.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);
            xmlReader.setEntityResolver(new NullEntityResolver());
        }
        xmlReader = getWiretap(xmlParser);
        xmlParser.setErrorHandler(docValidationErrHandler);
        xmlParser.lockErrorHandler();
    }

    private WiretapXMLReaderWrapper getWiretap(XMLReader reader) {
        WiretapXMLReaderWrapper wiretap = new WiretapXMLReaderWrapper(reader);
        ContentHandler recorder = sourceCode.getLocationRecorder();
        wiretap.setWiretapContentHander(recorder);
        wiretap.setWiretapLexicalHandler((LexicalHandler) recorder);
        return wiretap;
    }

    /* *
     * Checks an InputSource as a text/html HTML document.
     */
    public void checkHtmlInputSource(InputSource is) throws IOException,
            SAXException {
        validator.reset();
        is.setEncoding("UTF-8");
        checkAsHTML(is);
    }

    /* *
     * Checks an InputSource as an XHTML/XML document.
     */
    public void checkXmlInputSource(InputSource is) throws IOException,
            SAXException {
        validator.reset();
        checkAsXML(is);
    }

    /* *
     * Checks a text/html HTML document.
     */
    public void checkHtmlFile(File file, boolean asUTF8) throws IOException,
            SAXException {
        validator.reset();
        InputSource is = new InputSource(new FileInputStream(file));
        is.setSystemId(file.toURI().toURL().toString());
        if (asUTF8) {
            is.setEncoding("UTF-8");
        }
        checkAsHTML(is);
    }

    /* *
     * Checks an XHTML document or other XML document.
     */
    public void checkXmlFile(File file) throws IOException, SAXException {
        validator.reset();
        InputSource is = new InputSource(new FileInputStream(file));
        is.setSystemId(file.toURI().toURL().toString());
        checkAsXML(is);
    }

    /* *
     * Checks a Web document.
     * 
     * @throws IOException if loading of the URL fails for some reason
     */
    public void checkHttpURL(URL url) throws IOException, SAXException {
        String address = url.toString();
        validator.reset();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String contentType = connection.getContentType();
        InputSource is = new InputSource(url.openStream());
        is.setSystemId(address);
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                is.setEncoding(param.split("=", 2)[1]);
                break;
            }
        }
        if (connection.getContentType().startsWith("text/html")) {
            checkAsHTML(is);
        } else {
            checkAsXML(is);
        }
    }

    /* *
     * Parses a document with the text/html parser and validates it.
     */
    private void checkAsHTML(InputSource is) throws IOException, SAXException {
        sourceCode.initialize(is);
        try {
            htmlReader.parse(is);
        } catch (SAXParseException e) {
        }
    }

    /* *
     * Parses a document with the XML parser and validates it.
     */
    private void checkAsXML(InputSource is) throws IOException, SAXException {
        xmlParser.setCharacterHandler(sourceCode);
        sourceCode.initialize(is);
        try {
            xmlReader.parse(is);
        } catch (SAXParseException e) {
        }
    }

    @SuppressWarnings("serial") public class SchemaReadException extends
            Exception {

        public SchemaReadException() {
        }

        public SchemaReadException(String message) {
            super(message);
        }
    }

}
