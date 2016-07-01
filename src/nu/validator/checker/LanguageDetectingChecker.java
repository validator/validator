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

package nu.validator.checker;

import com.cybozu.labs.langdetect.LangDetectException;
import com.ibm.icu.util.ULocale;
import org.apache.stanbol.enhancer.engines.langdetect.LanguageIdentifier;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.SAXException;

public final class LanguageDetectingChecker extends Checker {

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

    private LocatorImpl htmlLocator;

    private int MAX_CHARS = 35840;

    private int MIN_CHARS = 512;

    @Override
    public void reset() {
        inBody = false;
        collectingCharacters = false;
        characterCount = 0;
        elementContent = new StringBuilder();
        documentContent = new StringBuilder();
        hasLang = false;
        hasXmlLang = false;
        langAttrValue = "";
        xmlLangAttrValue = "";
        try {
            languageIdentifier = new LanguageIdentifier();
        } catch (LangDetectException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (characterCount > MAX_CHARS) {
            return;
        }
        if (collectingCharacters) {
            characterCount += length;
            elementContent.append(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (characterCount > MAX_CHARS) {
            return;
        }
        documentContent.append(elementContent);
        elementContent.setLength(0);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if ("html".equals(localName)) {
            for (int i = 0; i < atts.getLength(); i++) {
                if ("lang".equals(atts.getLocalName(i))) {
                    hasLang = true;
                    langAttrValue = atts.getValue(i);
                } else if ("xml:lang".equals(atts.getLocalName(i))) {
                    hasXmlLang = true;
                    xmlLangAttrValue = atts.getValue(i);
                }
            }
            Locator documentLocator = getDocumentLocator();
            htmlLocator = new LocatorImpl(documentLocator);
            htmlLocator.setLineNumber(documentLocator.getLineNumber());
            htmlLocator.setColumnNumber(documentLocator.getColumnNumber());
        } else if (characterCount > MAX_CHARS) {
            return;
        } else if ("body".equals(localName)) {
            inBody = true;
        }
        collectingCharacters = false;
        if (inBody && !"script".equals(localName)
                && !"style".equals(localName)) {
            collectingCharacters = true;
        }
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        System.out.println(documentContent.toString());
        if (characterCount < MIN_CHARS) {
            return;
        }
        try {
            String textContent = documentContent.toString();
            String language = languageIdentifier.getLanguage(textContent);
            String detectedLanguageName = "";
            String preferredLanguageCode = "";
            ULocale locale = new ULocale(language);
            String detectedLanguageCode = locale.getLanguage();
            if ("zh-cn".equals(language)) {
                detectedLanguageName = "Simplified Chinese";
                preferredLanguageCode = "zh-Hans";
            } else if ("zh-tw".equals(language)) {
                detectedLanguageName = "Traditional Chinese";
                preferredLanguageCode = "zh-Hant";
            } else {
                detectedLanguageName = locale.getDisplayName();
                preferredLanguageCode = detectedLanguageCode;
            }
            if ("".equals(langAttrValue) && "".equals(xmlLangAttrValue)) {
                warn(String.format(
                        "This document appears to be written in %s."
                                + " Consider adding \u201Clang=\"%s\"\u201D"
                                + " (or variant) to the \u201Chtml\u201D element"
                                + " start tag.",
                        detectedLanguageName, preferredLanguageCode),
                        htmlLocator);
            }
            String message = "This document appears to be written in %s but the"
                    + " \u201Chtml\u201D element start tag has %s"
                    + " \u201C%s\u201D attribute with the value \u201C%s\u201D."
                    + " Consider changing the \u201C%s\u201D value to"
                    + " \u201C%s\u201D (or variant) instead.";
            if (hasLang && !(new ULocale(langAttrValue).getLanguage()).equals(
                    detectedLanguageCode)) {
                warn(String.format(message, detectedLanguageName, "a", "lang",
                        langAttrValue, "lang", preferredLanguageCode),
                        htmlLocator);
            }
            if (hasXmlLang
                    && !(new ULocale(xmlLangAttrValue).getLanguage()).equals(
                            detectedLanguageCode)) {
                warn(String.format(message, detectedLanguageName, "an",
                        "xml:lang", xmlLangAttrValue, "xml:lang",
                        preferredLanguageCode), htmlLocator);
            }
            reset();
        } catch (LangDetectException e) {
            e.printStackTrace();
        }
    }
}
