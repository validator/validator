/*
 * Copyright (c) 2005, 2006 Henri Sivonen
 * Copyright (c) 2007-2017 Mozilla Foundation
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.validate.IncorrectSchemaException;


public class Html5ConformanceCheckerTransaction extends
        VerifierServletTransaction {

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#isSimple()
     */
    @Override
    protected boolean isSimple() {
        return true;
    }
    
    private final static String GENERIC_FACET = (VerifierServlet.GENERIC_HOST.isEmpty() ? "" : ("//" + VerifierServlet.GENERIC_HOST)) + VerifierServlet.GENERIC_PATH;

    private static final char[] GENERIC_UI = "More options".toCharArray();    
    
    private static final char[] SERVICE_TITLE = (System.getProperty(
            "nu.validator.servlet.service-name", "Validator.nu") + " (X)HTML5 Validator ").toCharArray();

    private static final char[] TECHNOLOGY_PREVIEW = "(Living Validator)".toCharArray();

    private static final char[] RESULTS_TITLE = "(X)HTML5 validation results".toCharArray();

    private static final char[] FOR = " for ".toCharArray();
    
    private static final String SUCCESS_HTML = "The document is valid HTML5 + ARIA + SVG 1.1 + MathML 2.0 (subject to the utter previewness of this service).";

    private static final String SUCCESS_XHTML = "The document is valid XHTML5 + ARIA + SVG 1.1 + MathML 2.0 (subject to the utter previewness of this service).";

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
    @Override
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
    @Override
    protected void loadDocAndSetupParser() throws SAXException, IOException, IncorrectSchemaException, SAXNotRecognizedException, SAXNotSupportedException {
        setAllowGenericXml(false);
        setAcceptAllKnownXmlTypes(false);
        setAllowHtml(true);
        setAllowXhtml(true);
        loadDocumentInput();
        String type = documentInput.getType();
        if ("text/html".equals(type) || "text/html-sandboxed".equals(type)) {
            validator = validatorByDoctype(HTML5_SCHEMA);
            usingHtml = true;
            newHtmlParser();
            htmlParser.setDocumentModeHandler(this);
            htmlParser.setContentHandler(validator.getContentHandler());
            reader = htmlParser;
        } else {
            validator = validatorByDoctype(XHTML5_SCHEMA);
            setupXmlParser();
            if (!("application/xhtml+xml".equals(type) || "application/xml".equals(type))) {
                String message = "The preferred Content-Type for XHTML5 is application/xhtml+xml. The Content-Type was " + type + ".";
                SAXParseException spe = new SAXParseException(message, null, documentInput.getSystemId(), -1, -1);
                errorHandler.warning(spe);
            }
        }

    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#setup()
     */
    @Override
    protected void setup() throws ServletException {
        schemaUrls = "";
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#emitTitle()
     */
    @Override
    void emitTitle(boolean markupAllowed) throws SAXException {
        if (willValidate()) {
            emitter.characters(RESULTS_TITLE);
            if (document != null && document.length() > 0) {
                emitter.characters(FOR);                
                emitter.characters(scrub(shortenDataUri(document)));                
            }
        } else {
            emitter.characters(SERVICE_TITLE);
            if (markupAllowed && System.getProperty("nu.validator.servlet.service-name", "Validator.nu").equals("Validator.nu")) {
                emitter.startElement("span");
                emitter.characters(TECHNOLOGY_PREVIEW);
                emitter.endElement("span");
            }
        }
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#tryToSetupValidator()
     */
    @Override
    protected void tryToSetupValidator() throws SAXException, IOException, IncorrectSchemaException {
        // No-op
    }

    /**
     * @see nu.validator.servlet.VerifierServletTransaction#failureMessage()
     */
    @Override
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
    @Override
    protected void emitFormContent() throws SAXException {
        Html5FormEmitter.emit(contentHandler, this);
    }

    @Override
    void maybeEmitNsfilterField() throws SAXException {
        if (request.getParameter("nsfilter") != null) {
            NsFilterEmitter.emit(contentHandler, this);
        }
    }

    @Override
    void maybeEmitCharsetField() throws SAXException {
        if (request.getParameter("charset") != null) {
            CharsetEmitter.emit(contentHandler, this);
        }
    }
    
    @Override
    void emitOtherFacetLink() throws SAXException {
        attrs.clear();
        attrs.addAttribute("href", GENERIC_FACET);
        emitter.startElement("a", attrs);
        emitter.characters(GENERIC_UI);
        emitter.endElement("a");   
    }

}
