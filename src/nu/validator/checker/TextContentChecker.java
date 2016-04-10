/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2010-2013 Mozilla Foundation
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

package nu.validator.checker;

import java.util.LinkedList;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import nu.validator.datatype.Html5DatatypeException;
import nu.validator.datatype.CdoCdcPair;
import nu.validator.datatype.TimeDatetime;
import nu.validator.datatype.ScriptDocumentation;
import nu.validator.datatype.Script;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Checks the <code>textContent</code> of elements whose
 * <code>textContent</code> need special non-schema treatment. To smooth code
 * reuse between a conformance checker and editors that only allow RELAX NG plus
 * custom datatypes, this class uses objects that implement
 * <code>DatatypeStreamingValidator</code>.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class TextContentChecker extends Checker {

    private final static String XHTML_URL = "http://www.w3.org/1999/xhtml";

    /**
     * The stack of <code>DatatypeStreamingValidator</code>s corresponding to
     * open elements. Stack entry is <code>null</code> if the corresponding
     * element does not need <code>textContent</code> checking. Grows from the
     * tail.
     */
    private final LinkedList<DatatypeStreamingValidator> stack = new LinkedList<>();
    private boolean inEmptyTitleOrOption = false;

    /**
     * Constructor.
     */
    public TextContentChecker() {
        super();
    }

    /**
     * Returns a <code>DatatypeStreamingValidator</code> for the element if it
     * needs <code>textContent</code> checking or <code>null</code> if it does
     * not.
     * 
     * @param uri
     *            the namespace URI of the element
     * @param localName
     *            the local name of the element
     * @param atts
     *            the attributes
     * @return a <code>DatatypeStreamingValidator</code> or <code>null</code> if
     *         checks not necessary
     */
    private DatatypeStreamingValidator streamingValidatorFor(String uri,
            String localName, Attributes atts) {
        if (XHTML_URL.equals(uri)) {
            if ("time".equals(localName) && (atts.getIndex("", "datetime") < 0)) {
                return TimeDatetime.THE_INSTANCE.createStreamingValidator(null);
            }
            if ("script".equals(localName)) {
                if (atts.getIndex("", "src") < 0) {
                    return Script.THE_INSTANCE.createStreamingValidator(null);
                } else {
                    return ScriptDocumentation.THE_INSTANCE.createStreamingValidator(null);
                }
            } else if ("style".equals(localName)
                    || "textarea".equals(localName)
                    || "title".equals(localName)) {
                return CdoCdcPair.THE_INSTANCE.createStreamingValidator(null);
            }
        }
        return null;
    }

    /**
     * @see nu.validator.checker.Checker#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                default:
                    inEmptyTitleOrOption = false;
            }
        }
        for (DatatypeStreamingValidator dsv : stack) {
            if (dsv != null) {
                dsv.addCharacters(ch, start, length);
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (inEmptyTitleOrOption && XHTML_URL.equals(uri)
                && "title".equals(localName)) {
            err("Element \u201Ctitle\u201d must not be empty.");
            inEmptyTitleOrOption = false;
        } else if (inEmptyTitleOrOption && XHTML_URL.equals(uri)
                && "option".equals(localName)) {
            err("Element \u201Coption\u201d without "
                    + "attribute \u201clabel\u201d must not be empty.");
            inEmptyTitleOrOption = false;
        }
        DatatypeStreamingValidator dsv = stack.removeLast();
        if (dsv != null) {
            try {
                dsv.checkValid();
            } catch (DatatypeException e) {
                String msg = e.getMessage();
                if (msg == null) {
                    err("The text content of element \u201C" + localName
                            + "\u201D from namespace \u201C" + uri
                            + "\u201D was not in the required format.");
                } else {
                    if ("time".equals(localName)) {
                        try {
                            errBadTextContent(e,
                                    TimeDatetime.class,
                                    localName, uri);
                        } catch (ClassNotFoundException ce) {
                        }
                    } else if ("script".equals(localName)) {
                        // need cast to Html5DatatypeException in order to check
                        // what HTML5 datatype class this exception of for
                        assert e instanceof Html5DatatypeException : "Not an Html5DatatypeException";
                        Html5DatatypeException ex5 = (Html5DatatypeException) e;
                        if (Script.class.equals(ex5.getDatatypeClass())) {
                            try {
                                errBadTextContent(e, Script.class, localName,
                                        uri);
                            } catch (ClassNotFoundException ce) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            try {
                                errBadTextContent(e, ScriptDocumentation.class,
                                        localName, uri);
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                    } else if ("style".equals(localName)) {
                        try {
                            errBadTextContent(e,
                                    CdoCdcPair.class,
                                    localName, uri);
                        } catch (ClassNotFoundException ce) {
                        }
                    } else if ("textarea".equals(localName)
                            || "title".equals(localName)) {
                        try {
                            warnBadTextContent(e,
                                    CdoCdcPair.class,
                                    localName, uri);
                        } catch (ClassNotFoundException ce) {
                        }
                    } else {
                        err("The text content of element \u201C" + localName
                                // + "\u201D from namespace \u201C" + uri
                                + "\u201D was not in the required format: "
                                + msg.split(": ")[1]);
                    }
                }
            }
        }
    }

    private void errBadTextContent(DatatypeException e, Class<?> datatypeClass,
            String localName, String uri) throws SAXException,
            ClassNotFoundException {
        if (getErrorHandler() != null) {
            Html5DatatypeException ex5 = (Html5DatatypeException) e;
            DatatypeMismatchException dme = new DatatypeMismatchException(
                    "The text content of element \u201c" + localName
                            // + "\u201D from namespace \u201C" + uri
                            + "\u201d was not in the required format: "
                            + e.getMessage().split(": ")[1],
                    getDocumentLocator(), datatypeClass, ex5.isWarning());
            getErrorHandler().error(dme);
        }
    }

    private void warnBadTextContent(DatatypeException e, Class<?> datatypeClass,
            String localName, String uri) throws SAXException,
            ClassNotFoundException {
        if (getErrorHandler() != null) {
            DatatypeMismatchException dme = new DatatypeMismatchException(
                    "Possible problem in text content of element \u201c"
                            + localName
                            // + "\u201D from namespace \u201C" + uri
                            + "\u201d: " + e.getMessage().split(": ")[1],
                    getDocumentLocator(), datatypeClass, true);
            getErrorHandler().error(dme);
        }
    }

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        stack.addLast(streamingValidatorFor(uri, localName, atts));
        if (XHTML_URL.equals(uri)
                && ("title".equals(localName))
                || ("option".equals(localName) && atts.getIndex("", "label") < 0)) {
            inEmptyTitleOrOption = true;
        }
    }

    /**
     * @see nu.validator.checker.Checker#reset()
     */
    @Override
    public void reset() {
        stack.clear();
    }

}
