/*
 * Copyright (c) 2016 Mozilla Foundation
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
 *
 */

package nu.validator.servlet;

import java.io.IOException;
import java.util.ArrayList;

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.ibm.icu.util.ULocale;

import org.apache.stanbol.enhancer.engines.langdetect.LanguageIdentifier;

import org.xml.sax.Attributes;
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
import org.xml.sax.helpers.LocatorImpl;

public final class LanguageDetectingXMLReaderWrapper
        implements XMLReader, ContentHandler {

    private final XMLReader wrappedReader;

    private ContentHandler contentHandler;

    private ErrorHandler errorHandler;

    private Locator locator = null;

    private Locator htmlStartTagLocator;

    private StringBuilder elementContent;

    private StringBuilder documentContent;

    private String langAttrValue;

    private String xmlLangAttrValue;

    private boolean hasLang;

    private boolean hasXmlLang;

    private LanguageIdentifier languageIdentifier;

    private boolean inBody;

    private boolean collectingCharacters;

    private int characterCount;

    private int MAX_CHARS = 35840;

    private int MIN_CHARS = 512;

    private double MIN_PROBABILITY = .90;

    public LanguageDetectingXMLReaderWrapper(XMLReader wrappedReader,
            ErrorHandler errorHandler, LanguageIdentifier languageIdentifier) {
        this.wrappedReader = wrappedReader;
        this.contentHandler = wrappedReader.getContentHandler();
        this.errorHandler = errorHandler;
        this.htmlStartTagLocator = null;
        this.languageIdentifier = languageIdentifier;
        this.inBody = false;
        this.collectingCharacters = false;
        this.characterCount = 0;
        this.elementContent = new StringBuilder();
        this.documentContent = new StringBuilder();
        this.hasLang = false;
        this.hasXmlLang = false;
        this.langAttrValue = "";
        this.xmlLangAttrValue = "";
        wrappedReader.setContentHandler(this);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if (collectingCharacters && characterCount < MAX_CHARS) {
            characterCount += length;
            elementContent.append(ch, start, length);
        }
        contentHandler.characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if (characterCount < MAX_CHARS) {
            documentContent.append(elementContent);
            elementContent.setLength(0);
        }
        contentHandler.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.startDocument();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if ("html".equals(localName)) {
            htmlStartTagLocator = new LocatorImpl(locator);
            for (int i = 0; i < atts.getLength(); i++) {
                if ("lang".equals(atts.getLocalName(i))) {
                    hasLang = true;
                    langAttrValue = atts.getValue(i);
                } else if ("xml:lang".equals(atts.getLocalName(i))) {
                    hasXmlLang = true;
                    xmlLangAttrValue = atts.getValue(i);
                }
            }
        } else if ("body".equals(localName)) {
            inBody = true;
        }
        collectingCharacters = false;
        if (inBody && !"script".equals(localName)
                && !"style".equals(localName)) {
            collectingCharacters = true;
        }
        contentHandler.startElement(uri, localName, qName, atts);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        if (contentHandler == null) {
            return;
        }
        this.locator = locator;
        contentHandler.setDocumentLocator(locator);
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        if (contentHandler == null) {
            return;
        }
        try {
            if (characterCount < MIN_CHARS) {
                contentHandler.endDocument();
                return;
            }
            String textContent = documentContent.toString();
            String detectedLanguage = "";
            ArrayList<Language> possibleLanguages = languageIdentifier.getLanguages(
                    textContent);
            for (Language possibility : possibleLanguages) {
                if (possibility.prob > MIN_PROBABILITY) {
                    detectedLanguage = possibility.lang;
                }
            }
            if ("".equals(detectedLanguage)) {
                contentHandler.endDocument();
                return;
            }
            String declaredLanguageCode = "";
            String detectedLanguageName = "";
            String preferredLanguageCode = "";
            ULocale locale = new ULocale(detectedLanguage);
            String detectedLanguageCode = locale.getLanguage();
            String langWarning = "";
            if ("zh-cn".equals(detectedLanguage)) {
                detectedLanguageName = "Simplified Chinese";
                preferredLanguageCode = "zh-Hans";
            } else if ("zh-tw".equals(detectedLanguage)) {
                detectedLanguageName = "Traditional Chinese";
                preferredLanguageCode = "zh-Hant";
            } else {
                detectedLanguageName = locale.getDisplayName();
                preferredLanguageCode = detectedLanguageCode;
            }
            if (!hasLang && !hasXmlLang) {
                langWarning = String.format(
                        "This document appears to be written in %s."
                                + " Consider adding \u201Clang=\"%s\"\u201D"
                                + " (or variant) to the \u201Chtml\u201D"
                                + " start tag.",
                        detectedLanguageName, preferredLanguageCode);
            } else {
                String attValueExpression = "";
                String message = "This document appears to be written in %s"
                        + " but the \u201Chtml\u201D start tag has %s."
                        + " Consider changing the \u201C%s\u201D value to"
                        + " \u201C%s\u201D (or variant) instead.";
                String lowerCaseLangValue = "";
                String attName = "";
                String attValue = "";
                String aOrAn = "";
                if (hasLang) {
                    declaredLanguageCode = new ULocale(
                            langAttrValue).getLanguage();
                    lowerCaseLangValue = langAttrValue.toLowerCase();
                    attName = "lang";
                    attValue = langAttrValue;
                    aOrAn = "a";
                }
                if (hasXmlLang) {
                    declaredLanguageCode = new ULocale(
                            xmlLangAttrValue).getLanguage();
                    lowerCaseLangValue = xmlLangAttrValue.toLowerCase();
                    attName = "xml:lang";
                    attValue = xmlLangAttrValue;
                    aOrAn = "an";
                }
                if (("zh-cn".equals(detectedLanguage)
                        && (lowerCaseLangValue.contains("zh-tw")
                                || lowerCaseLangValue.contains("zh-hant")))
                        || ("zh-tw".equals(detectedLanguage)
                                && (lowerCaseLangValue.contains("zh-cn")
                                        || lowerCaseLangValue.contains(
                                                "zh-hans")))
                        || !declaredLanguageCode.equals(detectedLanguageCode)) {
                    if ("".equals(attValue)) {
                        attValueExpression = "an empty \u201c" + attName
                                + "\u201d attribute";
                    } else {
                        attValueExpression = String.format(
                                "%s \u201C%s\u201D attribute with the value"
                                        + " \u201C%s\u201D.",
                                aOrAn, attName, attValue);
                    }
                    langWarning = String.format(message, detectedLanguageName,
                            attValueExpression, attName, preferredLanguageCode);
                }
            }
            if (!"".equals(langWarning)) {
                warn(langWarning, htmlStartTagLocator);
            }
        } catch (LangDetectException e) {
        }
        contentHandler.endDocument();
    }

    /**
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.endPrefixMapping(prefix);
    }

    /**
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    /**
     * @param target
     * @param data
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.processingInstruction(target, data);
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.skippedEntity(name);
    }

    /**
     * @param prefix
     * @param uri
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.startPrefixMapping(prefix, uri);
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    @Override
    public DTDHandler getDTDHandler() {
        return wrappedReader.getDTDHandler();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    @Override
    public EntityResolver getEntityResolver() {
        return wrappedReader.getEntityResolver();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        return wrappedReader.getFeature(name);
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        return wrappedReader.getProperty(name);
    }

    /**
     * @param input
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        wrappedReader.parse(input);
    }

    /**
     * @param systemId
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    @Override
    public void parse(String systemId) throws IOException, SAXException {
        wrappedReader.parse(systemId);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    @Override
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    @Override
    public void setDTDHandler(DTDHandler handler) {
        wrappedReader.setDTDHandler(handler);
    }

    /**
     * @param resolver
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver resolver) {
        wrappedReader.setEntityResolver(resolver);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler handler) {
        wrappedReader.setErrorHandler(handler);
    }

    /**
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setFeature(name, value);
    }

    /**
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setProperty(name, value);
    }

    private void warn(String message, Locator locator) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message, locator);
            errorHandler.warning(spe);
        }
    }
}
