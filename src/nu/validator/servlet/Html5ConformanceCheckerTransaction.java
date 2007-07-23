package nu.validator.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.validator.htmlparser.DoctypeExpectation;
import nu.validator.htmlparser.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.validate.IncorrectSchemaException;

import fi.iki.hsivonen.xml.TypedInputSource;

public class Html5ConformanceCheckerTransaction extends
        VerifierServletTransaction {

    private static final char[] SERVICE_TITLE = "(X)HTML5 Conformance Checking Service ".toCharArray();

    private static final char[] TECHNOLOGY_PREVIEW = "Technology Preview".toCharArray();

    private static final char[] RESULTS_TITLE = "(X)HTML5 conformance checking results for ".toCharArray();

    private static final String SUCCESS_HTML = "The document conforms to the machine-checkable conformance requirements for HTML5 (subject to the utter previewness of this service).";

    private static final String SUCCESS_XHTML = "The document conforms to the machine-checkable conformance requirements for XHTML5 (subject to the utter previewness of this service).";

    private static final String FAILURE_HTML = "There were errors. (Tried in the text/html mode.)";

    private static final String FAILURE_XHTML = "There were errors. (Tried in the XHTML mode.)";

    private boolean usingHtml = false;
    
    public Html5ConformanceCheckerTransaction(HttpServletRequest request,
            HttpServletResponse response) {
        super(request, response);
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#successMessage()
     */
    protected String successMessage() throws SAXException {
        if (usingHtml) {
            return SUCCESS_HTML;
        } else {
            return SUCCESS_XHTML;
        }
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#loadDocAndSetupParser()
     */
    protected void loadDocAndSetupParser() throws SAXException, IOException, IncorrectSchemaException, SAXNotRecognizedException, SAXNotSupportedException {
        httpRes.setAllowGenericXml(false);
        httpRes.setAcceptAllKnownXmlTypes(false);
        httpRes.setAllowHtml(true);
        httpRes.setAllowXhtml(true);
        documentInput = (TypedInputSource) entityResolver.resolveEntity(
                null, document);
        String type = documentInput.getType();
        if ("text/html".equals(type)) {
            validator = validatorByDoctype(HTML5_SCHEMA);
            usingHtml = true;
            htmlParser = new HtmlParser();
            htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
            htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
            htmlParser.setMappingLangToXmlLang(true);
            htmlParser.setDoctypeExpectation(DoctypeExpectation.HTML);
            htmlParser.setDocumentModeHandler(this);
            htmlParser.setContentHandler(validator.getContentHandler());
            reader = htmlParser;
        } else {
            validator = validatorByDoctype(XHTML5_SCHEMA);
            reader = setupXmlParser();
            if (!("application/xhtml+xml".equals(type) || "application/xml".equals(type))) {
                String message = "The preferred Content-Type for XHTML5 is application/xhtml+xml. The Content-Type was " + type + ".";
                SAXParseException spe = new SAXParseException(message, null, documentInput.getSystemId(), -1, -1);
                errorHandler.warning(spe);
            }
        }

    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#setupAndStartEmission()
     */
    protected void setup() throws ServletException {
        // No-op
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#emitTitle()
     */
    void emitTitle(boolean markupAllowed) throws SAXException {
        if (willValidate()) {
            emitter.characters(RESULTS_TITLE);
            emitter.characters(scrub(document));
        } else {
            emitter.characters(SERVICE_TITLE);
            if (markupAllowed) {
                emitter.startElement("span");
                emitter.characters(TECHNOLOGY_PREVIEW);
                emitter.endElement("span");
            }
        }
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#tryToSetupValidator()
     */
    protected void tryToSetupValidator() throws SAXException, IOException, IncorrectSchemaException {
        // No-op
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#failureMessage()
     */
    protected String failureMessage() throws SAXException {
        if (usingHtml) {
            return FAILURE_HTML;
        } else {
            return FAILURE_XHTML;
        }
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#emitFormContent()
     */
    protected void emitFormContent() throws SAXException {
        Html5FormEmitter.emit(contentHandler, this);
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#doctype(int)
     */
    public void doctype(int doctype) throws SAXException {
        // No-op
    }

}
