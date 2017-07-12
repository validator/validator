/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
 * Copyright (c) 2007-2016 Mozilla Foundation
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

package nu.validator.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.Set;

import nu.validator.checker.NormalizationChecker;
import nu.validator.checker.DatatypeMismatchException;
import nu.validator.checker.VnuBadAttrValueException;
import nu.validator.checker.VnuBadElementNameException;
import nu.validator.datatype.Html5DatatypeException;
import nu.validator.io.DataUri;
import nu.validator.io.SystemIdIOException;
import nu.validator.messages.types.MessageType;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeParser;
import nu.validator.servlet.imagereview.Image;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.Location;
import nu.validator.source.SourceCode;
import nu.validator.source.SourceHandler;
import nu.validator.spec.EmptySpec;
import nu.validator.spec.Spec;
import nu.validator.spec.html5.Html5AttributeDatatypeBuilder;
import nu.validator.spec.html5.ImageReportAdviceBuilder;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.CharacterUtil;
import nu.validator.xml.XhtmlSaxEmitter;

import org.relaxng.datatype.DatatypeException;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.relaxng.exceptions.AbstractValidationException;
import com.thaiopensource.relaxng.exceptions.BadAttributeValueException;
import com.thaiopensource.relaxng.exceptions.ImpossibleAttributeIgnoredException;
import com.thaiopensource.relaxng.exceptions.OnlyTextNotAllowedException;
import com.thaiopensource.relaxng.exceptions.OutOfContextElementException;
import com.thaiopensource.relaxng.exceptions.RequiredAttributesMissingException;
import com.thaiopensource.relaxng.exceptions.RequiredAttributesMissingOneOfException;
import com.thaiopensource.relaxng.exceptions.RequiredElementsMissingException;
import com.thaiopensource.relaxng.exceptions.RequiredElementsMissingOneOfException;
import com.thaiopensource.relaxng.exceptions.StringNotAllowedException;
import com.thaiopensource.relaxng.exceptions.TextNotAllowedException;
import com.thaiopensource.relaxng.exceptions.UnfinishedElementException;
import com.thaiopensource.relaxng.exceptions.UnfinishedElementOneOfException;
import com.thaiopensource.relaxng.exceptions.UnknownElementException;
import com.thaiopensource.xml.util.Name;

import org.apache.log4j.Logger;
import com.ibm.icu.text.Normalizer;

@SuppressWarnings("unchecked")
public final class MessageEmitterAdapter implements ErrorHandler {

    private static final Logger log4j = Logger.getLogger(MessageEmitterAdapter.class);

    private final static Map<String, char[]> WELL_KNOWN_NAMESPACES = new HashMap<>();

    static {
        WELL_KNOWN_NAMESPACES.put("", "unnamespaced".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/xhtml",
                "XHTML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2000/svg",
                "SVG".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1998/Math/MathML",
                "MathML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2005/Atom",
                "Atom".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/xlink",
                "XLink".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://docbook.org/ns/docbook",
                "DocBook".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://relaxng.org/ns/structure/1.0",
                "RELAX NG".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/XML/1998/namespace",
                "XML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/XSL/Transform",
                "XSLT".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/ns/xbl",
                "XBL2".toCharArray());
        WELL_KNOWN_NAMESPACES.put(
                "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",
                "XUL".toCharArray());
        WELL_KNOWN_NAMESPACES.put(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                "RDF".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://purl.org/dc/elements/1.1/",
                "Dublin Core".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2001/XMLSchema-instance",
                "XML Schema Instance".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2002/06/xhtml2/",
                "XHTML2".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.ascc.net/xml/schematron",
                "Schematron".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://purl.oclc.org/dsdl/schematron",
                "ISO Schematron".toCharArray());
        WELL_KNOWN_NAMESPACES.put(
                "http://www.inkscape.org/namespaces/inkscape",
                "Inkscape".toCharArray());
        WELL_KNOWN_NAMESPACES.put(
                "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd",
                "Sodipodi".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.openmath.org/OpenMath",
                "OpenMath".toCharArray());
        WELL_KNOWN_NAMESPACES.put(
                "http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/",
                "Adobe SVG Viewer 3.0 extension".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://ns.adobe.com/AdobeIllustrator/10.0/",
                "Adobe Illustrator 10.0".toCharArray());
        WELL_KNOWN_NAMESPACES.put("adobe:ns:meta/",
                "XMP Container".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://ns.adobe.com/xap/1.0/",
                "XMP".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://ns.adobe.com/pdf/1.3/",
                "Adobe PDF 1.3".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://ns.adobe.com/tiff/1.0/",
                "Adobe TIFF".toCharArray());
    }

    @SuppressWarnings("rawtypes")
    private final static Map<Class, DocumentFragment> HTML5_DATATYPE_ADVICE = new HashMap<>();

    private final static DocumentFragment IMAGE_REPORT_GENERAL;

    private final static DocumentFragment IMAGE_REPORT_EMPTY;

    private final static DocumentFragment NO_ALT_NO_LINK_ADVICE;

    private final static DocumentFragment NO_ALT_LINK_ADVICE;

    private final static DocumentFragment EMPTY_ALT_ADVICE;

    private final static DocumentFragment HAS_ALT_ADVICE;

    private final static DocumentFragment IMAGE_REPORT_FATAL;

    private static final String SPEC_LINK_URI = System.getProperty(
            "nu.validator.spec.html5-link",
            "https://html.spec.whatwg.org/multipage/");

    private static final long MAX_MESSAGES = Integer.parseInt(System.getProperty(
            "nu.validator.messages.limit", "1000"));

    private static final Map<String, String[]> validInputTypesByAttributeName = new TreeMap<>();

    static {
        validInputTypesByAttributeName.put("accept", new String[] {
                "#attr-input-accept", "file" });
        validInputTypesByAttributeName.put("alt", new String[] {
                "#attr-input-alt", "image" });
        validInputTypesByAttributeName.put("autocomplete", new String[] {
                "#attr-input-autocomplete", "text", "search", "url", "tel",
                "email", "password", "date", "month", "week",
                "time", "datetime-local", "number", "range", "color" });
        validInputTypesByAttributeName.put("autofocus",
                new String[] { "#attr-fe-autofocus" });
        validInputTypesByAttributeName.put("checked", new String[] {
                "#attr-input-checked", "checkbox", "radio" });
        validInputTypesByAttributeName.put("dirname", new String[] {
                "#attr-input-dirname", "text", "search" });
        validInputTypesByAttributeName.put("disabled",
                new String[] { "#attr-fe-disabled" });
        validInputTypesByAttributeName.put("form",
                new String[] { "#attr-fae-form" });
        validInputTypesByAttributeName.put("formaction", new String[] {
                "#attr-fs-formaction", "submit", "image" });
        validInputTypesByAttributeName.put("formenctype", new String[] {
                "#attr-fs-formenctype", "submit", "image" });
        validInputTypesByAttributeName.put("formmethod", new String[] {
                "#attr-fs-formmethod", "submit", "image" });
        validInputTypesByAttributeName.put("formnovalidate", new String[] {
                "#attr-fs-formnovalidate", "submit", "image" });
        validInputTypesByAttributeName.put("formtarget", new String[] {
                "#attr-fs-formtarget", "submit", "image" });
        validInputTypesByAttributeName.put("height", new String[] {
                "#attr-dim-height", "image" });
        validInputTypesByAttributeName.put("list", new String[] {
                "#attr-input-list", "text", "search", "url", "tel", "email",
                "date", "month", "week", "time", "datetime-local",
                "number", "range", "color" });
        validInputTypesByAttributeName.put("max", new String[] {
                "#attr-input-max", "date", "month", "week", "time",
                "datetime-local", "number", "range", });
        validInputTypesByAttributeName.put("maxlength", new String[] {
                "#attr-input-maxlength", "text", "search", "url", "tel",
                "email", "password" });
        validInputTypesByAttributeName.put("min", new String[] {
                "#attr-input-min", "date", "month", "week", "time",
                "datetime-local", "number", "range", });
        validInputTypesByAttributeName.put("multiple", new String[] {
                "#attr-input-multiple", "email", "file" });
        validInputTypesByAttributeName.put("name",
                new String[] { "#attr-fe-name" });
        validInputTypesByAttributeName.put("pattern", new String[] {
                "#attr-input-pattern", "text", "search", "url", "tel",
                "email", "password" });
        validInputTypesByAttributeName.put("placeholder", new String[] {
                "#attr-input-placeholder", "text", "search", "url", "tel",
                "email", "password", "number" });
        validInputTypesByAttributeName.put("readonly", new String[] {
                "#attr-input-readonly", "text", "search", "url", "tel",
                "email", "password", "date", "month", "week",
                "time", "datetime-local", "number" });
        validInputTypesByAttributeName.put("required",
                new String[] { "#attr-input-required", "text", "search", "url",
                        "tel", "email", "password", "date",
                        "month", "week", "time", "datetime-local", "number",
                        "checkbox", "radio", "file" });
        validInputTypesByAttributeName.put("size", new String[] {
                "#attr-input-size", "text", "search", "url", "tel", "email",
                "password" });
        validInputTypesByAttributeName.put("src", new String[] {
                "#attr-input-src", "image" });
        validInputTypesByAttributeName.put("step", new String[] {
                "#attr-input-step", "date", "month", "week",
                "time", "datetime-local", "number", "range", });
        validInputTypesByAttributeName.put("type",
                new String[] { "#attr-input-type" });
        validInputTypesByAttributeName.put("value",
                new String[] { "#attr-input-value" });
        validInputTypesByAttributeName.put("width", new String[] {
                "#attr-dim-width", "image" });
    }

    private static final Map<String, String> fragmentIdByInputType = new TreeMap<>();

    static {
        fragmentIdByInputType.put("hidden", "#hidden-state-(type=hidden)");
        fragmentIdByInputType.put("text",
                "#text-(type=text)-state-and-search-state-(type=search)");
        fragmentIdByInputType.put("search",
                "#text-(type=text)-state-and-search-state-(type=search)");
        fragmentIdByInputType.put("tel", "#telephone-state-(type=tel)");
        fragmentIdByInputType.put("url", "#url-state-(type=url)");
        fragmentIdByInputType.put("email", "#e-mail-state-(type=email)");
        fragmentIdByInputType.put("password", "#password-state-(type=password)");
        fragmentIdByInputType.put("date", "#date-state-(type=date)");
        fragmentIdByInputType.put("month", "#month-state-(type=month)");
        fragmentIdByInputType.put("week", "#week-state-(type=week)");
        fragmentIdByInputType.put("time", "#time-state-(type=time)");
        fragmentIdByInputType.put("datetime-local",
                "#local-date-and-time-state-(type=datetime-local)");
        fragmentIdByInputType.put("number", "#number-state-(type=number)");
        fragmentIdByInputType.put("range", "#range-state-(type=range)");
        fragmentIdByInputType.put("color", "#color-state-(type=color)");
        fragmentIdByInputType.put("checkbox", "#checkbox-state-(type=checkbox)");
        fragmentIdByInputType.put("radio", "#radio-button-state-(type=radio)");
        fragmentIdByInputType.put("file", "#file-upload-state-(type=file)");
        fragmentIdByInputType.put("submit", "#submit-button-state-(type=submit)");
        fragmentIdByInputType.put("image", "#image-button-state-(type=image)");
        fragmentIdByInputType.put("reset", "#reset-button-state-(type=reset)");
        fragmentIdByInputType.put("button", "#button-state-(type=button)");
    }

    static {
        try {
            HTML5_DATATYPE_ADVICE.putAll(Html5AttributeDatatypeBuilder.parseSyntaxDescriptions());
            List<DocumentFragment> list = ImageReportAdviceBuilder.parseAltAdvice();
            IMAGE_REPORT_GENERAL = list.get(0);
            NO_ALT_NO_LINK_ADVICE = list.get(1);
            NO_ALT_LINK_ADVICE = list.get(2);
            EMPTY_ALT_ADVICE = list.get(3);
            HAS_ALT_ADVICE = list.get(4);
            IMAGE_REPORT_EMPTY = list.get(5);
            IMAGE_REPORT_FATAL = list.get(6);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private final static char[] INDETERMINATE_MESSAGE = "The result cannot be determined due to a non-document-error.".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_BEFORE = "Attributes for element ".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_AFTER = ":".toCharArray();

    private final static char[] CONTENT_MODEL_BEFORE = "Content model for element ".toCharArray();

    private final static char[] CONTENT_MODEL_AFTER = ":".toCharArray();

    private final static char[] CONTEXT_BEFORE = "Contexts in which element ".toCharArray();

    private final static char[] CONTEXT_AFTER = " may be used:".toCharArray();

    private final static char[] BAD_VALUE = "Bad value ".toCharArray();

    private final static char[] POTENTIALLY_BAD_VALUE = "Potentially bad value ".toCharArray();

    private final static char[] BAD_ELEMENT_NAME = "Bad element name".toCharArray();

    private final static char[] POTENTIALLY_BAD_ELEMENT_NAME = "Potentially bad element name".toCharArray();

    private final static char[] FOR = " for ".toCharArray();

    private final static char[] ATTRIBUTE = "attribute ".toCharArray();

    private final static char[] FROM_NAMESPACE = " from namespace ".toCharArray();

    private final static char[] SPACE = " ".toCharArray();

    private final static char[] ON = " on ".toCharArray();

    private final static char[] ELEMENT = "element ".toCharArray();

    private final static char[] PERIOD = ".".toCharArray();

    private final static char[] COMMA = ", ".toCharArray();

    private final static char[] COLON = ":".toCharArray();

    private final static char[] NOT_ALLOWED_ON = " not allowed on ".toCharArray();

    private final static char[] AT_THIS_POINT = " at this point.".toCharArray();

    private final static char[] ONLY_TEXT = " is not allowed to have content that consists solely of text.".toCharArray();

    private final static char[] NOT_ALLOWED = " not allowed".toCharArray();

    private final static char[] AS_CHILD_OF = " as child of ".toCharArray();

    private final static char[] IN_THIS_CONTEXT_SUPPRESSING = " in this context. (Suppressing further errors from this subtree.)".toCharArray();

    private final static char[] REQUIRED_ATTRIBUTES_MISSING = " is missing required attribute ".toCharArray();

    private final static char[] REQUIRED_ATTRIBUTES_MISSING_ONE_OF = " is missing one or more of the following attributes: ".toCharArray();

    private final static char[] REQUIRED_ELEMENTS_MISSING = "Required elements missing.".toCharArray();

    private final static char[] IS_MISSING_A_REQUIRED_CHILD = " is missing a required child element".toCharArray();

    private final static char[] REQUIRED_CHILDREN_MISSING_FROM = " is missing a required instance of child element ".toCharArray();

    private final static char[] REQUIRED_CHILDREN_MISSING_ONE_OF_FROM = " is missing a required instance of one or more of the following child elements: ".toCharArray();

    private final static char[] BAD_CHARACTER_CONTENT = "Bad character content ".toCharArray();

    private final static char[] IN_THIS_CONTEXT = " in this context.".toCharArray();

    private final static char[] TEXT_NOT_ALLOWED_IN = "Text not allowed in ".toCharArray();

    private final static char[] UNKNOWN = "Unknown ".toCharArray();

    private static final char[] NO_ALT_NO_LINK_HEADING = "No textual alternative available, not linked".toCharArray();

    private static final char[] NO_ALT_LINK_HEADING = "No textual alternative available, image linked".toCharArray();

    private static final char[] EMPTY_ALT = "Empty textual alternative\u2014Omitted from non-graphical presentation".toCharArray();

    private static final char[] HAS_ALT = "Images with textual alternative".toCharArray();

    private final AttributesImpl attributesImpl = new AttributesImpl();

    private final char[] oneChar = { '\u0000' };

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    private final boolean batchMode;

    private int nonDocumentErrors = 0;

    private final Pattern filterPattern;

    private final SourceCode sourceCode;

    private final MessageEmitter emitter;

    private final ExactErrorHandler exactErrorHandler;

    private final boolean showSource;

    private final ImageCollector imageCollector;

    private final int lineOffset;

    private Spec spec = EmptySpec.THE_INSTANCE;

    private boolean html = false;

    private boolean loggingOk = false;

    private boolean errorsOnly = false;

    @SuppressWarnings("deprecation")
    protected static String scrub(String s) throws SAXException {
        if (s == null) {
            return null;
        }
        s = CharacterUtil.prudentlyScrubCharacterData(s);
        if (NormalizationChecker.startsWithComposingChar(s)) {
            s = " " + s;
        }
        return Normalizer.normalize(s, Normalizer.NFC, 0);
    }

    private StringBuilder zapLf(StringBuilder builder) {
        int len = builder.length();
        for (int i = 0; i < len; i++) {
            char c = builder.charAt(i);
            if (c == '\n' || c == '\r') {
                builder.setCharAt(i, ' ');
            }
        }
        return builder;
    }

    private void throwIfTooManyMessages() throws SAXException {
        if (!batchMode && (warnings + errors > MAX_MESSAGES)) {
            throw new TooManyErrorsException("Too many messages.");
        }
    }

    public MessageEmitterAdapter(Pattern filterPattern, SourceCode sourceCode,
            boolean showSource, ImageCollector imageCollector, int lineOffset,
            boolean batchMode, MessageEmitter messageEmitter) {
        super();
        this.filterPattern = filterPattern;
        this.sourceCode = sourceCode;
        this.emitter = messageEmitter;
        this.exactErrorHandler = new ExactErrorHandler(this);
        this.showSource = showSource;
        this.lineOffset = lineOffset;
        this.batchMode = batchMode;
        this.imageCollector = imageCollector;
    }

    /**
     * @return Returns the errors.
     */
    public int getErrors() {
        return errors;
    }

    /**
     * @return Returns the fatalErrors.
     */
    public int getFatalErrors() {
        return fatalErrors;
    }

    /**
     * @return Returns the warnings.
     */
    public int getWarnings() {
        return warnings;
    }

    private boolean isErrors() {
        return !(errors == 0 && fatalErrors == 0);
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
        warning(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void warning(SAXParseException e, boolean exact)
            throws SAXException {
        if ((!batchMode && fatalErrors > 0) || nonDocumentErrors > 0) {
            return;
        }
        this.warnings++;
        throwIfTooManyMessages();
        messageFromSAXParseException(MessageType.WARNING, e, exact);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        error(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void error(SAXParseException e, boolean exact) throws SAXException {
        if ((!batchMode && fatalErrors > 0) || nonDocumentErrors > 0) {
            return;
        }
        Map<String, DatatypeException> datatypeErrors = null;
        if (e instanceof BadAttributeValueException) {
            datatypeErrors = ((BadAttributeValueException) e).getExceptions();
        }
        if (e instanceof VnuBadAttrValueException) {
            datatypeErrors = ((VnuBadAttrValueException) e).getExceptions();
        }
        if (e instanceof VnuBadElementNameException) {
            datatypeErrors = ((VnuBadElementNameException) e).getExceptions();
        }
        if (e instanceof DatatypeMismatchException) {
            datatypeErrors = ((DatatypeMismatchException) e).getExceptions();
        }
        if (datatypeErrors != null) {
            for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                DatatypeException dex = entry.getValue();
                if (dex instanceof Html5DatatypeException) {
                    Html5DatatypeException ex5 = (Html5DatatypeException) dex;
                    if (ex5.isWarning()) {
                        this.warnings++;
                        throwIfTooManyMessages();
                        messageFromSAXParseException(MessageType.WARNING, e,
                                exact);
                        return;
                    }
                }
            }
        }
        this.errors++;
        throwIfTooManyMessages();
        messageFromSAXParseException(MessageType.ERROR, e, exact);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        fatalError(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void fatalError(SAXParseException e, boolean exact)
            throws SAXException {
        if ((!batchMode && fatalErrors > 0) || nonDocumentErrors > 0) {
            return;
        }
        this.fatalErrors++;
        Exception wrapped = e.getException();
        String systemId = null;
        if (wrapped instanceof SystemIdIOException) {
            SystemIdIOException siie = (SystemIdIOException) wrapped;
            systemId = siie.getSystemId();
        }
        if (wrapped instanceof IOException) {
            message(MessageType.IO, wrapped, systemId, -1, -1, false);
        } else {
            messageFromSAXParseException(MessageType.FATAL, e, exact);
        }
    }

    public void info(String str) throws SAXException {
        if (emitter instanceof GnuMessageEmitter)
            return;
        message(MessageType.INFO, new Exception(str), null, -1, -1, false);
    }

    public void ioError(IOException e) throws SAXException {
        this.nonDocumentErrors++;
        String systemId = null;
        if (e instanceof SystemIdIOException) {
            SystemIdIOException siie = (SystemIdIOException) e;
            systemId = siie.getSystemId();
        }
        message(MessageType.IO, e, systemId, -1, -1, false);
    }

    public void internalError(Throwable e, String message) throws SAXException {
        this.nonDocumentErrors++;
        message(MessageType.INTERNAL, new Exception(message), null, -1, -1,
                false);
    }

    public void schemaError(Exception e) throws SAXException {
        this.nonDocumentErrors++;
        message(MessageType.SCHEMA, e, null, -1, -1, false);
    }

    public void start(String documentUri) throws SAXException {
        emitter.startMessages(scrub(shortenDataUri(documentUri)), showSource);
    }

    private String shortenDataUri(String uri) {
        if (DataUri.startsWithData(uri)) {
            return "data:\u2026";
        } else {
            return uri;
        }
    }

    public void end(String successMessage, String failureMessage,
            String language) throws SAXException {
        ResultHandler resultHandler = emitter.startResult();
        if (resultHandler != null) {
            if (isIndeterminate()) {
                resultHandler.startResult(Result.INDETERMINATE);
                resultHandler.characters(INDETERMINATE_MESSAGE, 0,
                        INDETERMINATE_MESSAGE.length);
                resultHandler.endResult();
            } else if (isErrors()) {
                resultHandler.startResult(Result.FAILURE);
                resultHandler.characters(failureMessage.toCharArray(), 0,
                        failureMessage.length());
                resultHandler.endResult();
            } else {
                resultHandler.startResult(Result.SUCCESS);
                resultHandler.characters(successMessage.toCharArray(), 0,
                        successMessage.length());
                resultHandler.endResult();
            }
        }
        emitter.endResult();

        if (imageCollector != null) {
            DocumentFragment instruction = IMAGE_REPORT_GENERAL;
            boolean fatal = false;
            if (getFatalErrors() > 0) {
                fatal = true;
                instruction = IMAGE_REPORT_FATAL;
            } else if (imageCollector.isEmpty()) {
                instruction = IMAGE_REPORT_EMPTY;
            }

            ImageReviewHandler imageReviewHandler = emitter.startImageReview(
                    instruction, fatal);
            if (imageReviewHandler != null && !fatal) {
                emitImageReview(imageReviewHandler);
            }
            emitter.endImageReview();
        }

        if (showSource) {
            SourceHandler sourceHandler = emitter.startFullSource(lineOffset);
            if (sourceHandler != null) {
                sourceCode.emitSource(sourceHandler);
            }
            emitter.endFullSource();
        }
        emitter.endMessages(language);
    }

    private void emitImageReview(ImageReviewHandler imageReviewHandler)
            throws SAXException {
        List<Image> noAltNoLink = new LinkedList<>();
        List<Image> noAltLink = new LinkedList<>();
        List<Image> emptyAlt = new LinkedList<>();
        List<Image> hasAlt = new LinkedList<>();

        for (Image image : imageCollector) {
            String alt = image.getAlt();
            if (alt == null) {
                if (image.isLinked()) {
                    noAltLink.add(image);
                } else {
                    noAltNoLink.add(image);
                }
            } else if ("".equals(alt)) {
                emptyAlt.add(image);
            } else {
                hasAlt.add(image);
            }
        }

        emitImageList(imageReviewHandler, noAltLink, NO_ALT_LINK_HEADING,
                NO_ALT_LINK_ADVICE, false);
        emitImageList(imageReviewHandler, noAltNoLink, NO_ALT_NO_LINK_HEADING,
                NO_ALT_NO_LINK_ADVICE, false);
        emitImageList(imageReviewHandler, emptyAlt, EMPTY_ALT,
                EMPTY_ALT_ADVICE, false);
        emitImageList(imageReviewHandler, hasAlt, HAS_ALT, HAS_ALT_ADVICE, true);
    }

    private void emitImageList(ImageReviewHandler imageReviewHandler,
            List<Image> list, char[] heading, DocumentFragment instruction,
            boolean hasAlt) throws SAXException {
        if (!list.isEmpty()) {
            imageReviewHandler.startImageGroup(heading, instruction, hasAlt);
            for (Image image : list) {
                String systemId = image.getSystemId();
                int oneBasedLine = image.getLineNumber();
                int oneBasedColumn = image.getColumnNumber();
                Location rangeLast = sourceCode.newLocatorLocation(
                        oneBasedLine, oneBasedColumn);
                if (sourceCode.isWithinKnownSource(rangeLast)) {
                    Location rangeStart = sourceCode.rangeStartForRangeLast(rangeLast);
                    imageReviewHandler.image(image, hasAlt, systemId,
                            rangeStart.getLine() + 1,
                            rangeStart.getColumn() + 1, oneBasedLine,
                            oneBasedColumn);
                } else {
                    imageReviewHandler.image(image, hasAlt, systemId, -1, -1,
                            -1, -1);
                }
            }
            imageReviewHandler.endImageGroup();
        }
    }

    private boolean isIndeterminate() {
        return nonDocumentErrors > 0;
    }

    private void messageFromSAXParseException(MessageType type,
            SAXParseException spe, boolean exact) throws SAXException {
        message(type, spe, spe.getSystemId(), spe.getLineNumber(),
                spe.getColumnNumber(), exact);
    }

    private void message(MessageType type, Exception message, String systemId,
            int oneBasedLine, int oneBasedColumn, boolean exact)
            throws SAXException {
        String msg = message.getMessage();
        if (filterPattern != null && msg != null
                && filterPattern.matcher(msg).matches()) {
            return;
        }
        if (loggingOk
                && (type.getSuperType() == "error")
                && spec != EmptySpec.THE_INSTANCE
                && systemId != null
                && msg != null
                && (systemId.startsWith("http:") || systemId.startsWith("https:"))) {
            log4j.info(zapLf(new StringBuilder() //
                    .append(systemId).append('\t').append(msg)));
        }
        if (errorsOnly && type.getSuperType() == "info") {
            return;
        }
        String uri = sourceCode.getUri();
        if (oneBasedLine > -1
                && (uri == systemId || (uri != null && uri.equals(systemId)))) {
            if (oneBasedColumn > -1) {
                if (exact) {
                    messageWithExact(type, message, systemId, oneBasedLine,
                            oneBasedColumn);
                } else {
                    messageWithRange(type, message, systemId, oneBasedLine,
                            oneBasedColumn);
                }
            } else {
                messageWithLine(type, message, systemId, oneBasedLine);
            }
        } else {
            messageWithoutExtract(type, message, systemId, oneBasedLine,
                    oneBasedColumn);
        }
    }

    private void messageWithRange(MessageType type, Exception message,
            String systemId, int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        systemId = batchMode ? systemId : null;
        Location rangeLast = sourceCode.newLocatorLocation(oneBasedLine,
                oneBasedColumn);
        if (!sourceCode.isWithinKnownSource(rangeLast)) {
            messageWithoutExtract(type, message, null, oneBasedLine,
                    oneBasedColumn);
            return;
        }
        Location rangeStart = sourceCode.rangeStartForRangeLast(rangeLast);
        startMessage(type, scrub(shortenDataUri(systemId)),
                rangeStart.getLine() + 1, rangeStart.getColumn() + 1,
                oneBasedLine, oneBasedColumn, false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.rangeEndError(rangeStart, rangeLast, sourceHandler);
        }
        emitter.endSource();
        elaboration(message);
        endMessage();
    }

    private void messageWithExact(MessageType type, Exception message,
            String systemId, int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        systemId = batchMode ? systemId : null;
        startMessage(type, scrub(shortenDataUri(systemId)), oneBasedLine,
                oneBasedColumn, oneBasedLine, oneBasedColumn, true);
        messageText(message);
        Location location = sourceCode.newLocatorLocation(oneBasedLine,
                oneBasedColumn);
        if (sourceCode.isWithinKnownSource(location)) {
            SourceHandler sourceHandler = emitter.startSource();
            if (sourceHandler != null) {
                sourceCode.exactError(location, sourceHandler);
            }
            emitter.endSource();
        } else {
            sourceCode.rememberExactError(location);
        }
        elaboration(message);
        endMessage();
    }

    private void messageWithLine(MessageType type, Exception message,
            String systemId, int oneBasedLine) throws SAXException {
        systemId = batchMode ? systemId : null;
        if (!sourceCode.isWithinKnownSource(oneBasedLine)) {
            throw new RuntimeException("Bug. Line out of range!");
        }
        startMessage(type, scrub(shortenDataUri(systemId)), oneBasedLine, -1,
                oneBasedLine, -1, false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.lineError(oneBasedLine, sourceHandler);
        }
        emitter.endSource();
        elaboration(message);
        endMessage();
    }

    private void messageWithoutExtract(MessageType type, Exception message,
            String systemId, int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        if (systemId == null) {
            systemId = sourceCode.getUri();
        }
        startMessage(type, scrub(shortenDataUri(systemId)), oneBasedLine,
                oneBasedColumn, oneBasedLine, oneBasedColumn, false);
        messageText(message);
        elaboration(message);
        endMessage();
    }

    /**
     * @param message
     * @throws SAXException
     */
    private void messageText(Exception message) throws SAXException {
        if (message instanceof AbstractValidationException) {
            AbstractValidationException ave = (AbstractValidationException) message;
            rngMessageText(ave);
        } else if (message instanceof VnuBadAttrValueException) {
            VnuBadAttrValueException e = (VnuBadAttrValueException) message;
            vnuBadAttrValueMessageText(e);
        } else if (message instanceof VnuBadElementNameException) {
            VnuBadElementNameException e = (VnuBadElementNameException) message;
            vnuElementNameMessageText(e);
        } else {
            String msg = message.getMessage();
            if (msg != null) {
                MessageTextHandler messageTextHandler = emitter.startText();
                if (messageTextHandler != null) {
                    emitStringWithQurlyQuotes(messageTextHandler, msg);
                }
                emitter.endText();
            }
        }
    }

    private void vnuBadAttrValueMessageText(VnuBadAttrValueException e)
            throws SAXException {
        MessageTextHandler messageTextHandler = emitter.startText();
        if (messageTextHandler != null) {
            boolean isWarning = false;
            Map<String, DatatypeException> datatypeErrors = e.getExceptions();
            for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                DatatypeException dex = entry.getValue();
                if (dex instanceof Html5DatatypeException) {
                    Html5DatatypeException ex5 = (Html5DatatypeException) dex;
                    if (ex5.isWarning()) {
                        isWarning = true;
                    }
                }
            }
            if (isWarning) {
                messageTextString(messageTextHandler, POTENTIALLY_BAD_VALUE,
                        false);
            } else {
                messageTextString(messageTextHandler, BAD_VALUE, false);
            }
            if (e.getAttributeValue().length() < 200) {
                codeString(messageTextHandler, e.getAttributeValue());
            }
            messageTextString(messageTextHandler, FOR, false);
            attribute(messageTextHandler, e.getAttributeName(),
                    e.getCurrentElement(), false);
            messageTextString(messageTextHandler, ON, false);
            element(messageTextHandler, e.getCurrentElement(), false);
            emitDatatypeErrors(messageTextHandler, e.getExceptions());
        }
        emitter.endText();
    }

    private void vnuElementNameMessageText(VnuBadElementNameException e)
            throws SAXException {
        MessageTextHandler messageTextHandler = emitter.startText();
        if (messageTextHandler != null) {
            boolean isWarning = false;
            Map<String, DatatypeException> datatypeErrors = e.getExceptions();
            for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                DatatypeException dex = entry.getValue();
                if (dex instanceof Html5DatatypeException) {
                    Html5DatatypeException ex5 = (Html5DatatypeException) dex;
                    if (ex5.isWarning()) {
                        isWarning = true;
                    }
                }
            }
            if (isWarning) {
                messageTextString(messageTextHandler,
                        POTENTIALLY_BAD_ELEMENT_NAME, false);
            } else {
                messageTextString(messageTextHandler, BAD_ELEMENT_NAME, false);
            }
            messageTextString(messageTextHandler, SPACE, false);
            codeString(messageTextHandler, e.getElementName());
            emitDatatypeErrors(messageTextHandler, e.getExceptions());
        }
        emitter.endText();
    }

    private void rngMessageText(
            AbstractValidationException e) throws SAXException {
        MessageTextHandler messageTextHandler = emitter.startText();
        if (messageTextHandler != null) {
            if (e instanceof BadAttributeValueException) {
                BadAttributeValueException ex = (BadAttributeValueException) e;
                boolean isWarning = false;
                Map<String, DatatypeException> datatypeErrors = ex.getExceptions();
                for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                    DatatypeException dex = entry.getValue();
                    if (dex instanceof Html5DatatypeException) {
                        Html5DatatypeException ex5 = (Html5DatatypeException) dex;
                        if (ex5.isWarning()) {
                            isWarning = true;
                        }
                    }
                }
                if (isWarning) {
                    messageTextString(messageTextHandler,
                            POTENTIALLY_BAD_VALUE, false);
                } else {
                    messageTextString(messageTextHandler, BAD_VALUE, false);
                }
                if (ex.getAttributeValue().length() < 200) {
                    codeString(messageTextHandler, ex.getAttributeValue());
                }
                messageTextString(messageTextHandler, FOR, false);
                attribute(messageTextHandler, ex.getAttributeName(),
                        ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, ON, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                emitDatatypeErrors(messageTextHandler, ex.getExceptions());
            } else if (e instanceof ImpossibleAttributeIgnoredException) {
                ImpossibleAttributeIgnoredException ex = (ImpossibleAttributeIgnoredException) e;
                attribute(messageTextHandler, ex.getAttributeName(),
                        ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, NOT_ALLOWED_ON, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, AT_THIS_POINT, false);
            } else if (e instanceof OnlyTextNotAllowedException) {
                OnlyTextNotAllowedException ex = (OnlyTextNotAllowedException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, ONLY_TEXT, false);
            } else if (e instanceof OutOfContextElementException) {
                OutOfContextElementException ex = (OutOfContextElementException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, NOT_ALLOWED, false);
                if (ex.getParent() != null) {
                    messageTextString(messageTextHandler, AS_CHILD_OF, false);
                    element(messageTextHandler, ex.getParent(), false);
                }
                messageTextString(messageTextHandler,
                        IN_THIS_CONTEXT_SUPPRESSING, false);
            } else if (e instanceof RequiredAttributesMissingOneOfException) {
                RequiredAttributesMissingOneOfException ex = (RequiredAttributesMissingOneOfException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler,
                        REQUIRED_ATTRIBUTES_MISSING_ONE_OF, false);
                for (Iterator<String> iter = ex.getAttributeLocalNames().iterator(); iter.hasNext();) {
                    codeString(messageTextHandler, iter.next());
                    if (iter.hasNext()) {
                        messageTextString(messageTextHandler, COMMA, false);
                    }
                }
                messageTextString(messageTextHandler, PERIOD, false);
            } else if (e instanceof RequiredAttributesMissingException) {
                RequiredAttributesMissingException ex = (RequiredAttributesMissingException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler,
                        REQUIRED_ATTRIBUTES_MISSING, false);
                codeString(messageTextHandler, ex.getAttributeLocalName());
                messageTextString(messageTextHandler, PERIOD, false);
            } else if (e instanceof RequiredElementsMissingException) {
                RequiredElementsMissingException ex = (RequiredElementsMissingException) e;
                if (ex.getParent() == null) {
                    messageTextString(messageTextHandler,
                            REQUIRED_ELEMENTS_MISSING, false);
                } else {
                    element(messageTextHandler, ex.getParent(), true);
                    if (ex.getMissingElementName() == null) {
                        messageTextString(messageTextHandler,
                                IS_MISSING_A_REQUIRED_CHILD, false);
                    } else {
                        messageTextString(messageTextHandler,
                                REQUIRED_CHILDREN_MISSING_FROM, false);
                        codeString(messageTextHandler,
                                ex.getMissingElementName());
                    }
                    messageTextString(messageTextHandler, PERIOD, false);
                }
            } else if (e instanceof StringNotAllowedException) {
                StringNotAllowedException ex = (StringNotAllowedException) e;
                messageTextString(messageTextHandler, BAD_CHARACTER_CONTENT,
                        false);
                codeString(messageTextHandler, ex.getValue());
                messageTextString(messageTextHandler, FOR, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                emitDatatypeErrors(messageTextHandler, ex.getExceptions());
            } else if (e instanceof TextNotAllowedException) {
                TextNotAllowedException ex = (TextNotAllowedException) e;
                messageTextString(messageTextHandler, TEXT_NOT_ALLOWED_IN,
                        false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, IN_THIS_CONTEXT, false);
            } else if (e instanceof UnfinishedElementException) {
                UnfinishedElementException ex = (UnfinishedElementException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                if (ex.getMissingElementName() == null) {
                    messageTextString(messageTextHandler,
                            IS_MISSING_A_REQUIRED_CHILD, false);
                } else {
                    messageTextString(messageTextHandler,
                            REQUIRED_CHILDREN_MISSING_FROM, false);
                    codeString(messageTextHandler, ex.getMissingElementName());
                }
                messageTextString(messageTextHandler, PERIOD, false);
            } else if (e instanceof UnfinishedElementOneOfException) {
                UnfinishedElementOneOfException ex = (UnfinishedElementOneOfException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler,
                        REQUIRED_CHILDREN_MISSING_ONE_OF_FROM, false);
                for (Iterator<String> iter = ex.getMissingElementNames().iterator(); iter.hasNext();) {
                    String missingElementName = iter.next();
                    if (!("http://www.w3.org/1999/xhtml".equals(ex.getCurrentElement().getNamespaceUri()) && "frameset".equals(missingElementName))) {
                        codeString(messageTextHandler, missingElementName);
                        if (iter.hasNext()) {
                            messageTextString(messageTextHandler, COMMA, false);
                        }
                    }
                }
                messageTextString(messageTextHandler, PERIOD, false);
            } else if (e instanceof RequiredElementsMissingOneOfException) {
                RequiredElementsMissingOneOfException ex = (RequiredElementsMissingOneOfException) e;
                element(messageTextHandler, ex.getParent(), true);
                messageTextString(messageTextHandler,
                        REQUIRED_CHILDREN_MISSING_ONE_OF_FROM, false);
                for (Iterator<String> iter = ex.getMissingElementNames().iterator(); iter.hasNext();) {
                    String missingElementName = iter.next();
                    if (!("http://www.w3.org/1999/xhtml".equals(ex.getCurrentElement().getNamespaceUri()) && "frameset".equals(missingElementName))) {
                        codeString(messageTextHandler, missingElementName);
                        if (iter.hasNext()) {
                            messageTextString(messageTextHandler, COMMA, false);
                        }
                    }
                }
                messageTextString(messageTextHandler, PERIOD, false);
            } else if (e instanceof UnknownElementException) {
                UnknownElementException ex = (UnknownElementException) e;
                messageTextString(messageTextHandler, UNKNOWN, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, NOT_ALLOWED, false);
                if (ex.getParent() != null) {
                    messageTextString(messageTextHandler, AS_CHILD_OF, false);
                    element(messageTextHandler, ex.getParent(), false);
                }
                messageTextString(messageTextHandler, PERIOD, false);
            }
        }
        emitter.endText();
    }

    /**
     * @param messageTextHandler
     * @param datatypeErrors
     * @throws SAXException
     */
    private void emitDatatypeErrors(MessageTextHandler messageTextHandler,
            Map<String, DatatypeException> datatypeErrors) throws SAXException {
        if (datatypeErrors.isEmpty()) {
            messageTextString(messageTextHandler, PERIOD, false);
        } else {
            messageTextString(messageTextHandler, COLON, false);
            for (Map.Entry<String, DatatypeException> entry : datatypeErrors.entrySet()) {
                messageTextString(messageTextHandler, SPACE, false);
                DatatypeException ex = entry.getValue();
                if (ex instanceof Html5DatatypeException) {
                    Html5DatatypeException ex5 = (Html5DatatypeException) ex;
                    String[] segments = ex5.getSegments();
                    for (int i = 0; i < segments.length; i++) {
                        String segment = segments[i];
                        if (i % 2 == 0) {
                            emitStringWithQurlyQuotes(messageTextHandler,
                                    segment);
                        } else {
                            String scrubbed = scrub(segment);
                            messageTextHandler.startCode();
                            messageTextHandler.characters(
                                    scrubbed.toCharArray(), 0,
                                    scrubbed.length());
                            messageTextHandler.endCode();
                        }
                    }
                } else {
                    emitStringWithQurlyQuotes(messageTextHandler,
                            ex.getMessage());
                }
            }
        }
    }

    private void element(MessageTextHandler messageTextHandler, Name element,
            boolean atSentenceStart) throws SAXException {
        if (html) {
            messageTextString(messageTextHandler, ELEMENT, atSentenceStart);
            linkedCodeString(messageTextHandler, element.getLocalName(),
                    spec.elementLink(element));
        } else {
            String ns = element.getNamespaceUri();
            char[] humanReadable = WELL_KNOWN_NAMESPACES.get(ns);
            if (humanReadable == null) {
                if (loggingOk) {
                    log4j.info(new StringBuilder().append("UNKNOWN_NS:\t").append(
                            ns));
                }
                messageTextString(messageTextHandler, ELEMENT, atSentenceStart);
                linkedCodeString(messageTextHandler, element.getLocalName(),
                        spec.elementLink(element));
                messageTextString(messageTextHandler, FROM_NAMESPACE, false);
                codeString(messageTextHandler, ns);
            } else {
                messageTextString(messageTextHandler, humanReadable,
                        atSentenceStart);
                messageTextString(messageTextHandler, SPACE, false);
                messageTextString(messageTextHandler, ELEMENT, false);
                linkedCodeString(messageTextHandler, element.getLocalName(),
                        spec.elementLink(element));
            }
        }
    }

    private void linkedCodeString(MessageTextHandler messageTextHandler,
            String str, String url) throws SAXException {
        if (url != null) {
            messageTextHandler.startLink(url, null);
        }
        codeString(messageTextHandler, str);
        if (url != null) {
            messageTextHandler.endLink();
        }

    }

    private void attribute(MessageTextHandler messageTextHandler,
            Name attributeName, Name elementName, boolean atSentenceStart)
            throws SAXException {
        String ns = attributeName.getNamespaceUri();
        if (html || "".equals(ns)) {
            messageTextString(messageTextHandler, ATTRIBUTE, atSentenceStart);
            codeString(messageTextHandler, attributeName.getLocalName());
        } else if ("http://www.w3.org/XML/1998/namespace".equals(ns)) {
            messageTextString(messageTextHandler, ATTRIBUTE, atSentenceStart);
            codeString(messageTextHandler,
                    "xml:" + attributeName.getLocalName());
        } else {
            char[] humanReadable = WELL_KNOWN_NAMESPACES.get(ns);
            if (humanReadable == null) {
                if (loggingOk) {
                    log4j.info(new StringBuilder().append("UNKNOWN_NS:\t").append(
                            ns));
                }
                messageTextString(messageTextHandler, ATTRIBUTE,
                        atSentenceStart);
                codeString(messageTextHandler, attributeName.getLocalName());
                messageTextString(messageTextHandler, FROM_NAMESPACE, false);
                codeString(messageTextHandler, ns);
            } else {
                messageTextString(messageTextHandler, humanReadable,
                        atSentenceStart);
                messageTextString(messageTextHandler, SPACE, false);
                messageTextString(messageTextHandler, ATTRIBUTE, false);
                codeString(messageTextHandler, attributeName.getLocalName());
            }
        }
    }

    private void codeString(MessageTextHandler messageTextHandler, String str)
            throws SAXException {
        messageTextHandler.startCode();
        messageTextHandler.characters(str.toCharArray(), 0, str.length());
        messageTextHandler.endCode();
    }

    private void messageTextString(MessageTextHandler messageTextHandler,
            char[] ch, boolean capitalize) throws SAXException {
        if (capitalize && ch[0] >= 'a' && ch[0] <= 'z') {
            oneChar[0] = (char) (ch[0] - 0x20);
            messageTextHandler.characters(oneChar, 0, 1);
            if (ch.length > 1) {
                messageTextHandler.characters(ch, 1, ch.length - 1);
            }
        } else {
            messageTextHandler.characters(ch, 0, ch.length);
        }
    }

    private void emitStringWithQurlyQuotes(
            MessageTextHandler messageTextHandler, String message)
            throws SAXException {
        if (message == null) {
            message = "";
        }
        message = scrub(message);
        int len = message.length();
        int start = 0;
        int startQuotes = 0;
        for (int i = 0; i < len; i++) {
            char c = message.charAt(i);
            if (c == '\u201C') {
                startQuotes++;
                if (startQuotes == 1) {
                    char[] scrubbed = scrub(message.substring(start, i)).toCharArray();
                    messageTextHandler.characters(scrubbed, 0, scrubbed.length);
                    start = i + 1;
                    messageTextHandler.startCode();
                }
            } else if (c == '\u201D' && startQuotes > 0) {
                startQuotes--;
                if (startQuotes == 0) {
                    char[] scrubbed = scrub(message.substring(start, i)).toCharArray();
                    messageTextHandler.characters(scrubbed, 0, scrubbed.length);
                    start = i + 1;
                    messageTextHandler.endCode();
                }
            }
        }
        if (start < len) {
            char[] scrubbed = scrub(message.substring(start, len)).toCharArray();
            messageTextHandler.characters(scrubbed, 0, scrubbed.length);
        }
        if (startQuotes > 0) {
            messageTextHandler.endCode();
        }
    }

    private void elaboration(Exception e)
            throws SAXException {
        if (!(e instanceof AbstractValidationException
                || e instanceof VnuBadAttrValueException
                || e instanceof VnuBadElementNameException
                || e instanceof DatatypeMismatchException)) {
            return;
        }

        if (e instanceof ImpossibleAttributeIgnoredException) {
            ImpossibleAttributeIgnoredException ex = (ImpossibleAttributeIgnoredException) e;
            Name elt = ex.getCurrentElement();
            elaborateElementSpecificAttributes(elt, ex.getAttributeName());
        } else if (e instanceof OnlyTextNotAllowedException) {
            OnlyTextNotAllowedException ex = (OnlyTextNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof OutOfContextElementException) {
            OutOfContextElementException ex = (OutOfContextElementException) e;
            Name parent = ex.getParent();
            Name child = ex.getCurrentElement();
            elaborateContentModelandContext(parent, child);
        } else if (e instanceof RequiredAttributesMissingException) {
            RequiredAttributesMissingException ex = (RequiredAttributesMissingException) e;
            Name elt = ex.getCurrentElement();
            elaborateElementSpecificAttributes(elt);
        } else if (e instanceof RequiredAttributesMissingOneOfException) {
            RequiredAttributesMissingOneOfException ex = (RequiredAttributesMissingOneOfException) e;
            Name elt = ex.getCurrentElement();
            elaborateElementSpecificAttributes(elt);
        } else if (e instanceof RequiredElementsMissingException) {
            RequiredElementsMissingException ex = (RequiredElementsMissingException) e;
            Name elt = ex.getParent();
            elaborateContentModel(elt);
        } else if (e instanceof RequiredElementsMissingOneOfException) {
            RequiredElementsMissingOneOfException ex = (RequiredElementsMissingOneOfException) e;
            Name elt = ex.getParent();
            elaborateContentModel(elt);
        } else if (e instanceof StringNotAllowedException) {
            StringNotAllowedException ex = (StringNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof TextNotAllowedException) {
            TextNotAllowedException ex = (TextNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof UnfinishedElementException) {
            UnfinishedElementException ex = (UnfinishedElementException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof UnfinishedElementOneOfException) {
            UnfinishedElementOneOfException ex = (UnfinishedElementOneOfException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof UnknownElementException) {
            UnknownElementException ex = (UnknownElementException) e;
            Name elt = ex.getParent();
            elaborateContentModel(elt);
        } else if (e instanceof BadAttributeValueException) {
            BadAttributeValueException ex = (BadAttributeValueException) e;
            Map<String, DatatypeException> map = ex.getExceptions();
            elaborateDatatypes(map);
        } else if (e instanceof VnuBadAttrValueException) {
            VnuBadAttrValueException ex = (VnuBadAttrValueException) e;
            Map<String, DatatypeException> map = ex.getExceptions();
            elaborateDatatypes(map);
        } else if (e instanceof VnuBadElementNameException) {
            VnuBadElementNameException ex = (VnuBadElementNameException) e;
            Map<String, DatatypeException> map = ex.getExceptions();
            elaborateDatatypes(map);
        } else if (e instanceof DatatypeMismatchException) {
            DatatypeMismatchException ex = (DatatypeMismatchException) e;
            Map<String, DatatypeException> map = ex.getExceptions();
            elaborateDatatypes(map);
        } else if (e instanceof StringNotAllowedException) {
            StringNotAllowedException ex = (StringNotAllowedException) e;
            Map<String, DatatypeException> map = ex.getExceptions();
            elaborateDatatypes(map);
        }
    }

    private void elaborateDatatypes(Map<String, DatatypeException> map)
            throws SAXException {
        Set<DocumentFragment> fragments = new HashSet<>();
        for (Map.Entry<String, DatatypeException> entry : map.entrySet()) {
            DatatypeException ex = entry.getValue();
            if (ex instanceof Html5DatatypeException) {
                Html5DatatypeException ex5 = (Html5DatatypeException) ex;
                DocumentFragment fragment = HTML5_DATATYPE_ADVICE.get(ex5.getDatatypeClass());
                if (fragment != null) {
                    fragments.add(fragment);
                }
            }
        }
        if (!fragments.isEmpty()) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                for (DocumentFragment fragment : fragments) {
                    treeParser.parse(fragment);
                }
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();

        }
    }

    /**
     * @param elt
     * @throws SAXException
     */
    private void elaborateContentModel(Name elt) throws SAXException {
        DocumentFragment dds = spec.contentModelDescription(elt);
        if (dds != null) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                emitContentModelDt(xhtmlSaxEmitter, elt);
                treeParser.parse(dds);
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();
        }
    }

    private void elaborateContentModelandContext(Name parent, Name child)
            throws SAXException {
        DocumentFragment contentModelDds = spec.contentModelDescription(parent);
        DocumentFragment contextDds = spec.contextDescription(child);
        if (contentModelDds != null || contextDds != null) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                if (contextDds != null) {
                    emitContextDt(xhtmlSaxEmitter, child);
                    treeParser.parse(contextDds);
                }
                if (contentModelDds != null) {
                    emitContentModelDt(xhtmlSaxEmitter, parent);
                    treeParser.parse(contentModelDds);
                }
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();
        }
    }

    /**
     * @param elt
     * @throws SAXException
     */
    private void elaborateElementSpecificAttributes(Name elt)
            throws SAXException {
        this.elaborateElementSpecificAttributes(elt, null);
    }

    private void elaborateElementSpecificAttributes(Name elt, Name attribute)
            throws SAXException {
        if ("input".equals(elt.getLocalName())) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                elaborateInputAttributes(xhtmlSaxEmitter, elt, attribute);
            }
            emitter.endElaboration();
        } else {
            DocumentFragment dds = spec.elementSpecificAttributesDescription(elt);
            if (dds != null) {
                ContentHandler ch = emitter.startElaboration();
                if (ch != null) {
                    TreeParser treeParser = new TreeParser(ch, null);
                    XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                    xhtmlSaxEmitter.startElement("dl");
                    emitElementSpecificAttributesDt(xhtmlSaxEmitter, elt);
                    treeParser.parse(dds);
                    xhtmlSaxEmitter.endElement("dl");
                }
                emitter.endElaboration();
            }
        }
    }

    private void emitElementSpecificAttributesDt(
            XhtmlSaxEmitter xhtmlSaxEmitter, Name elt) throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(ELEMENT_SPECIFIC_ATTRIBUTES_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(ELEMENT_SPECIFIC_ATTRIBUTES_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }

    private void emitContextDt(XhtmlSaxEmitter xhtmlSaxEmitter, Name elt)
            throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(CONTEXT_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(CONTEXT_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }

    private void emitContentModelDt(XhtmlSaxEmitter xhtmlSaxEmitter, Name elt)
            throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(CONTENT_MODEL_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(CONTENT_MODEL_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }

    private void emitLinkifiedLocalName(XhtmlSaxEmitter xhtmlSaxEmitter,
            Name elt) throws SAXException {
        String url = spec.elementLink(elt);
        if (url != null) {
            attributesImpl.clear();
            attributesImpl.addAttribute("href", url);
            xhtmlSaxEmitter.startElement("a", attributesImpl);
        }
        xhtmlSaxEmitter.startElement("code");
        xhtmlSaxEmitter.characters(elt.getLocalName());
        xhtmlSaxEmitter.endElement("code");
        if (url != null) {
            xhtmlSaxEmitter.endElement("a");
        }
    }

    private void elaborateInputAttributes(XhtmlSaxEmitter xhtmlSaxEmitter,
            Name elt, Name badAttribute) throws SAXException {
        attributesImpl.clear();
        attributesImpl.addAttribute("class", "inputattrs");
        xhtmlSaxEmitter.startElement("dl", attributesImpl);
        emitElementSpecificAttributesDt(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.startElement("dd");
        attributesImpl.clear();
        addHyperlink(xhtmlSaxEmitter, "Global attributes", SPEC_LINK_URI
                + "#global-attributes");
        attributesImpl.addAttribute("class", "inputattrtypes");
        xhtmlSaxEmitter.startElement("span", attributesImpl);
        xhtmlSaxEmitter.endElement("span");
        xhtmlSaxEmitter.endElement("dd");
        for (Map.Entry<String, String[]> entry : validInputTypesByAttributeName.entrySet()) {
            String attributeName = entry.getKey();
            xhtmlSaxEmitter.startElement("dd");
            attributesImpl.clear();
            attributesImpl.addAttribute("class", "inputattrname");
            xhtmlSaxEmitter.startElement("code", attributesImpl);
            attributesImpl.clear();
            attributesImpl.addAttribute("href",
                    SPEC_LINK_URI + entry.getValue()[0]);
            xhtmlSaxEmitter.startElement("a", attributesImpl);
            addText(xhtmlSaxEmitter, attributeName);
            xhtmlSaxEmitter.endElement("a");
            xhtmlSaxEmitter.endElement("code");
            attributesImpl.addAttribute("class", "inputattrtypes");
            if (badAttribute != null
                    && attributeName.equals(badAttribute.getLocalName())) {
                listInputTypesForAttribute(xhtmlSaxEmitter, attributeName, true);
            } else {
                listInputTypesForAttribute(xhtmlSaxEmitter, attributeName,
                        false);
            }
            xhtmlSaxEmitter.endElement("dd");
        }
        xhtmlSaxEmitter.endElement("dl");
    }

    private void listInputTypesForAttribute(XhtmlSaxEmitter xhtmlSaxEmitter,
            String attributeName, boolean bad) throws SAXException {
        String[] typeNames = validInputTypesByAttributeName.get(attributeName);
        int typeCount = typeNames.length;
        String wrapper = (bad ? "b" : "span");
        String highlight = (bad ? " highlight" : "");
        if (typeCount > 1 || "value".equals(attributeName)) {
            addText(xhtmlSaxEmitter, " ");
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute("class", "inputattrtypes" + highlight);
            xhtmlSaxEmitter.startElement(wrapper, attributesImpl);
            addText(xhtmlSaxEmitter, "when ");
            xhtmlSaxEmitter.startElement("code");
            addText(xhtmlSaxEmitter, "type");
            xhtmlSaxEmitter.endElement("code", "code");
            addText(xhtmlSaxEmitter, " is ");
            if ("value".equals(attributeName)) {
                addText(xhtmlSaxEmitter, "not ");
                addHyperlink(xhtmlSaxEmitter, "file", SPEC_LINK_URI
                        + fragmentIdByInputType.get("file"));
                addText(xhtmlSaxEmitter, " or ");
                addHyperlink(xhtmlSaxEmitter, "image", SPEC_LINK_URI
                        + fragmentIdByInputType.get("image"));
            } else {
                for (int i = 1; i < typeCount; i++) {
                    String typeName = typeNames[i];
                    if (i > 1) {
                        addText(xhtmlSaxEmitter, " ");
                    }
                    if (typeCount > 2 && i == typeCount - 1) {
                        addText(xhtmlSaxEmitter, "or ");
                    }
                    addHyperlink(xhtmlSaxEmitter, typeName, SPEC_LINK_URI
                            + fragmentIdByInputType.get(typeName));
                    if (i < typeCount - 1 && typeCount > 3) {
                        addText(xhtmlSaxEmitter, ",");
                    }
                }
            }
            xhtmlSaxEmitter.endElement(wrapper);
        } else {
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute("class", "inputattrtypes");
            xhtmlSaxEmitter.startElement("span", attributesImpl);
            xhtmlSaxEmitter.endElement("span");
        }
    }

    private void addText(XhtmlSaxEmitter xhtmlSaxEmitter, String text)
            throws SAXException {
        char[] ch = text.toCharArray();
        xhtmlSaxEmitter.characters(ch, 0, ch.length);
    }

    private void addHyperlink(XhtmlSaxEmitter xhtmlSaxEmitter, String text,
            String href) throws SAXException {
        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute("href", href);
        xhtmlSaxEmitter.startElement("a", attributesImpl);
        addText(xhtmlSaxEmitter, text);
        xhtmlSaxEmitter.endElement("a");
    }

    private final class ExactErrorHandler implements ErrorHandler {

        private final MessageEmitterAdapter owner;

        /**
         * @param owner
         */
        ExactErrorHandler(final MessageEmitterAdapter owner) {
            this.owner = owner;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            owner.error(exception, true);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            owner.fatalError(exception, true);
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            owner.warning(exception, true);
        }

    }

    /**
     * Returns the exactErrorHandler.
     *
     * @return the exactErrorHandler
     */
    public ErrorHandler getExactErrorHandler() {
        return exactErrorHandler;
    }

    /**
     * Sets the spec.
     *
     * @param spec
     *            the spec to set
     */
    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    /**
     * Sets the html.
     *
     * @param html
     *            the html to set
     */
    public void setHtml(boolean html) {
        this.html = html;
    }

    public void setLoggingOk(boolean ok) {
        this.loggingOk = ok;
    }

    /**
     * Sets the errorsOnly.
     *
     * @param errorsOnly
     *            the errorsOnly to set
     */
    public void setErrorsOnly(boolean errorsOnly) {
        this.errorsOnly = errorsOnly;
    }

    /**
     * @throws SAXException
     * @see nu.validator.messages.MessageEmitter#endMessage()
     */
    public void endMessage() throws SAXException {
        emitter.endMessage();
    }

    /**
     * @param type
     * @param systemId
     * @param oneBasedFirstLine
     * @param oneBasedFirstColumn
     * @param oneBasedLastLine
     * @param oneBasedLastColumn
     * @param exact
     * @throws SAXException
     * @see nu.validator.messages.MessageEmitter#startMessage(nu.validator.messages.types.MessageType,
     *      java.lang.String, int, int, int, int, boolean)
     */
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException {
        emitter.startMessage(type, systemId, (oneBasedFirstLine == -1) ? -1
                : oneBasedFirstLine + lineOffset, oneBasedFirstColumn,
                (oneBasedLastLine == -1) ? -1 : oneBasedLastLine + lineOffset,
                oneBasedLastColumn, exact);
    }
}
