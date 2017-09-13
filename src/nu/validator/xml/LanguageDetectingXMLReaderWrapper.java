/*
 * Copyright (c) 2016-2017 Mozilla Foundation
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

package nu.validator.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.ibm.icu.util.ULocale;
import io.mola.galimatias.URL;
import io.mola.galimatias.Host;
import io.mola.galimatias.GalimatiasParseException;

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

import org.apache.log4j.Logger;

public final class LanguageDetectingXMLReaderWrapper
        implements XMLReader, ContentHandler {

    private static final Logger log4j = Logger.getLogger(
            LanguageDetectingXMLReaderWrapper.class);

    private static final String languageList = "nu/validator/localentities/files/"
            + "language-profiles-list.txt";

    private static final String profilesDir = "nu/validator/localentities/files/"
            + "language-profiles/";

    private static List<String> profiles = new ArrayList<>();

    private static List<String> languageTags = new ArrayList<>();

    private static final Map<String, String[]> LANG_TAGS_BY_TLD = new HashMap<>();

    static {
      LANG_TAGS_BY_TLD.put("ae", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("af", new String[] { "ps" });
      LANG_TAGS_BY_TLD.put("am", new String[] { "hy" });
      LANG_TAGS_BY_TLD.put("ar", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("at", new String[] { "de" });
      LANG_TAGS_BY_TLD.put("az", new String[] { "az" });
      LANG_TAGS_BY_TLD.put("ba", new String[] { "bs", "hr", "sr" });
      LANG_TAGS_BY_TLD.put("bd", new String[] { "bn" });
      LANG_TAGS_BY_TLD.put("be", new String[] { "de", "fr", "nl" });
      LANG_TAGS_BY_TLD.put("bg", new String[] { "bg" });
      LANG_TAGS_BY_TLD.put("bh", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("bo", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("br", new String[] { "pt" });
      LANG_TAGS_BY_TLD.put("by", new String[] { "be" });
      LANG_TAGS_BY_TLD.put("bz", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("ch", new String[] { "de", "fr", "it", "rm" });
      LANG_TAGS_BY_TLD.put("cl", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("co", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("cu", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("cr", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("cz", new String[] { "cs" });
      LANG_TAGS_BY_TLD.put("de", new String[] { "de" });
      LANG_TAGS_BY_TLD.put("dk", new String[] { "da" });
      LANG_TAGS_BY_TLD.put("do", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("ec", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("ee", new String[] { "et" });
      LANG_TAGS_BY_TLD.put("eg", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("es", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("fi", new String[] { "fi" });
      LANG_TAGS_BY_TLD.put("fr", new String[] { "fr" });
      LANG_TAGS_BY_TLD.put("ge", new String[] { "ka" });
      LANG_TAGS_BY_TLD.put("gr", new String[] { "el" });
      LANG_TAGS_BY_TLD.put("gt", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("hn", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("hr", new String[] { "hr" });
      LANG_TAGS_BY_TLD.put("hu", new String[] { "hu" });
      LANG_TAGS_BY_TLD.put("id", new String[] { "id" });
      LANG_TAGS_BY_TLD.put("is", new String[] { "is" });
      LANG_TAGS_BY_TLD.put("it", new String[] { "it" });
      LANG_TAGS_BY_TLD.put("il", new String[] { "iw" });
      LANG_TAGS_BY_TLD.put("in", new String[] { "bn", "gu", "hi", "kn", "ml", "mr", "pa", "ta", "te" });
      LANG_TAGS_BY_TLD.put("ja", new String[] { "jp" });
      LANG_TAGS_BY_TLD.put("jo", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("ke", new String[] { "sw" });
      LANG_TAGS_BY_TLD.put("kg", new String[] { "ky" });
      LANG_TAGS_BY_TLD.put("kh", new String[] { "km" });
      LANG_TAGS_BY_TLD.put("kr", new String[] { "ko" });
      LANG_TAGS_BY_TLD.put("kw", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("kz", new String[] { "kk" });
      LANG_TAGS_BY_TLD.put("la", new String[] { "lo" });
      LANG_TAGS_BY_TLD.put("li", new String[] { "de" });
      LANG_TAGS_BY_TLD.put("lb", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("lk", new String[] { "si", "ta" });
      LANG_TAGS_BY_TLD.put("lt", new String[] { "lt" });
      LANG_TAGS_BY_TLD.put("lu", new String[] { "de" });
      LANG_TAGS_BY_TLD.put("lv", new String[] { "lv" });
      LANG_TAGS_BY_TLD.put("md", new String[] { "mo" });
      LANG_TAGS_BY_TLD.put("mk", new String[] { "mk" });
      LANG_TAGS_BY_TLD.put("mn", new String[] { "mn" });
      LANG_TAGS_BY_TLD.put("mx", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("my", new String[] { "ms" });
      LANG_TAGS_BY_TLD.put("ni", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("nl", new String[] { "nl" });
      LANG_TAGS_BY_TLD.put("no", new String[] { "nn", "no" });
      LANG_TAGS_BY_TLD.put("np", new String[] { "ne" });
      LANG_TAGS_BY_TLD.put("pa", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("pe", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("ph", new String[] { "tl" });
      LANG_TAGS_BY_TLD.put("pl", new String[] { "pl" });
      LANG_TAGS_BY_TLD.put("pk", new String[] { "ur" });
      LANG_TAGS_BY_TLD.put("pr", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("pt", new String[] { "pt" });
      LANG_TAGS_BY_TLD.put("py", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("qa", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("ro", new String[] { "ro" });
      LANG_TAGS_BY_TLD.put("rs", new String[] { "sr" });
      LANG_TAGS_BY_TLD.put("ru", new String[] { "ru" });
      LANG_TAGS_BY_TLD.put("sa", new String[] { "ar" });
      LANG_TAGS_BY_TLD.put("se", new String[] { "sv" });
      LANG_TAGS_BY_TLD.put("si", new String[] { "sl" });
      LANG_TAGS_BY_TLD.put("sk", new String[] { "sk" });
      LANG_TAGS_BY_TLD.put("sv", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("th", new String[] { "th" });
      LANG_TAGS_BY_TLD.put("tj", new String[] { "tg" });
      LANG_TAGS_BY_TLD.put("tm", new String[] { "tk" });
      LANG_TAGS_BY_TLD.put("ua", new String[] { "uk" });
      LANG_TAGS_BY_TLD.put("uy", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("uz", new String[] { "uz" });
      LANG_TAGS_BY_TLD.put("ve", new String[] { "es" });
      LANG_TAGS_BY_TLD.put("vn", new String[] { "vi" });
      LANG_TAGS_BY_TLD.put("za", new String[] { "af" });
    }

    public static void initialize() throws LangDetectException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    LanguageDetectingXMLReaderWrapper.class.getClassLoader().getResourceAsStream(
                            languageList)));
            String languageTagAndName = br.readLine();
            while (languageTagAndName != null) {
                languageTags.add(languageTagAndName.split("\t")[0]);
                languageTagAndName = br.readLine();
            }
            for (String languageTag : languageTags) {
                profiles.add((new BufferedReader(new InputStreamReader(
                        LanguageDetectingXMLReaderWrapper.class.getClassLoader().getResourceAsStream(
                                profilesDir + languageTag)))).readLine());
            }
            DetectorFactory.clear();
            DetectorFactory.loadProfile(profiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final XMLReader wrappedReader;

    private ContentHandler contentHandler;

    private ErrorHandler errorHandler;

    private HttpServletRequest request;

    private String systemId;

    private String tld;

    private Locator locator = null;

    private Locator htmlStartTagLocator;

    private StringBuilder elementContent;

    private StringBuilder documentContent;

    private String httpContentLangHeader;

    private String htmlElementLangAttrValue;

    private String declaredLangCode;

    private boolean htmlElementHasLang;

    private String dirAttrValue;

    private boolean hasDir;

    private boolean inBody;

    private int currentOpenElementsInDifferentLang;

    private boolean collectingCharacters;

    private int nonWhitespaceCharacterCount;

    private static final int MAX_CHARS = 30720;

    private static final int MIN_CHARS = 1024;

    private static final double MIN_PROBABILITY = .90;

    private static final String[] RTL_LANGS = { "ar", "azb", "ckb", "dv", "fa",
            "he", "pnb", "ps", "sd", "ug", "ur" };

    private static final String[] COMMON_LANGS = { "ar", "ca", "cs", "da",
            "de", "el", "en", "es", "et", "fa", "fi", "fr", "he", "hi", "hu",
            "id", "it", "ja", "ka", "ko", "lt", "lv", "ms", "nl", "no", "pl",
            "pt", "ro", "ru", "sk", "sq", "sv", "th", "tr", "uk", "vi",
            "zh-hans", "zh-hant" };

    public LanguageDetectingXMLReaderWrapper(XMLReader wrappedReader,
            HttpServletRequest request, ErrorHandler errorHandler,
            String httpContentLangHeader, String systemId) {
        this.wrappedReader = wrappedReader;
        this.contentHandler = wrappedReader.getContentHandler();
        this.errorHandler = errorHandler;
        this.request = request;
        this.systemId = systemId;
        this.tld = "";
        this.htmlStartTagLocator = null;
        this.inBody = false;
        this.currentOpenElementsInDifferentLang = 0;
        this.collectingCharacters = false;
        this.nonWhitespaceCharacterCount = 0;
        this.elementContent = new StringBuilder();
        this.documentContent = new StringBuilder();
        this.httpContentLangHeader = httpContentLangHeader;
        this.htmlElementHasLang = false;
        this.htmlElementLangAttrValue = "";
        this.declaredLangCode = "";
        this.hasDir = false;
        this.dirAttrValue = "";
        try {
            if (systemId != null && systemId.startsWith("http")) {
                Host hostname = URL.parse(systemId).host();
                if (hostname != null) {
                    String host = hostname.toString();
                    this.tld = host.substring(host.lastIndexOf(".") + 1);
                }
            }
        } catch (GalimatiasParseException e) {
            throw new RuntimeException(e);
        }
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
        if (collectingCharacters && nonWhitespaceCharacterCount < MAX_CHARS) {
            for (int i = start; i < start + length; i++) {
                switch (ch[i]) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        continue;
                    default:
                        nonWhitespaceCharacterCount++;
                }
            }
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
        if (nonWhitespaceCharacterCount < MAX_CHARS) {
            documentContent.append(elementContent);
            elementContent.setLength(0);
        }
        if ("body".equals(localName)) {
            inBody = false;
            collectingCharacters = false;
        }
        if (currentOpenElementsInDifferentLang > 0) {
            currentOpenElementsInDifferentLang--;
            if (currentOpenElementsInDifferentLang == 0) {
                collectingCharacters = true;
            }
        } else {
            if (inBody && ("script".equals(localName) //
                    || "style".equals(localName) //
                    || "pre".equals(localName) //
                    || "a".equals(localName) //
                    || "td".equals(localName) //
                    || "select".equals(localName) //
                    || "ul".equals(localName) //
                    || "nav".equals(localName) //
                    || "form".equals(localName))) {
                collectingCharacters = true;
            }
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
        documentContent.setLength(0);
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
                    if (request != null) {
                        request.setAttribute(
                                "http://validator.nu/properties/lang-found",
                                true);
                    }
                    htmlElementHasLang = true;
                    htmlElementLangAttrValue = atts.getValue(i);
                    declaredLangCode = new ULocale(
                            htmlElementLangAttrValue).getLanguage();
                } else if ("dir".equals(atts.getLocalName(i))) {
                    hasDir = true;
                    dirAttrValue = atts.getValue(i);
                }
            }
        } else if ("body".equals(localName)) {
            inBody = true;
            collectingCharacters = true;
        } else if (inBody) {
            if (currentOpenElementsInDifferentLang > 0) {
                currentOpenElementsInDifferentLang++;
            } else {
                for (int i = 0; i < atts.getLength(); i++) {
                    if ("lang".equals(atts.getLocalName(i))) {
                        if (!"".equals(htmlElementLangAttrValue)
                                && !htmlElementLangAttrValue.equals(
                                        atts.getValue(i))) {
                            currentOpenElementsInDifferentLang++;
                            collectingCharacters = false;
                        }
                    }
                }
            }
        }
        if ("script".equals(localName) //
                || "style".equals(localName) //
                || "pre".equals(localName) //
                || "a".equals(localName) //
                || "td".equals(localName) //
                || "select".equals(localName) //
                || "ul".equals(localName) //
                || "nav".equals(localName) //
                || "textarea".equals(localName) //
                || "figcaption".equals(localName) //
                || "form".equals(localName)) {
            collectingCharacters = false;
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
        detectLanguageAndCheckAgainstDeclaredLanguage();
        contentHandler.endDocument();
    }

    private void detectLanguageAndCheckAgainstDeclaredLanguage()
            throws SAXException {
        if (nonWhitespaceCharacterCount < MIN_CHARS) {
            return;
        }
        if ("zxx".equals(declaredLangCode) // "No Linguistic Content"
                || "eo".equals(declaredLangCode) // Esperanto
                || "la".equals(declaredLangCode) // Latin
        ) {
            return;
        }
        if (LANG_TAGS_BY_TLD.containsKey(tld)
                && Arrays.binarySearch(LANG_TAGS_BY_TLD.get(tld),
                        declaredLangCode) >= 0) {
            return;
        }
        try {
            String textContent = documentContent.toString() //
                    .replaceAll("\\s+", " ");
            String detectedLanguage = "";
            Detector detector = DetectorFactory.create();
            detector.append(textContent);
            detector.getProbabilities();
            ArrayList<String> possibileLanguages = new ArrayList<>();
            ArrayList<Language> possibilities = detector.getProbabilities();
            for (Language possibility : possibilities) {
                possibileLanguages.add(possibility.lang);
                ULocale plocale = new ULocale(possibility.lang);
                if (Arrays.binarySearch(COMMON_LANGS, possibility.lang) < 0
                        && systemId != null) {
                    log4j.info(
                            String.format("%s %s %s", plocale.getDisplayName(),
                                    possibility.prob, systemId));
                }
                if (possibility.prob > MIN_PROBABILITY) {
                    detectedLanguage = possibility.lang;
                    setDocumentLanguage(detectedLanguage);
                } else if ((possibileLanguages.contains("hr")
                        && (possibileLanguages.contains("sr-latn")
                                || possibileLanguages.contains("bs")))
                        || (possibileLanguages.contains("sr-latn")
                                && (possibileLanguages.contains("hr")
                                        || possibileLanguages.contains("bs")))
                        || (possibileLanguages.contains("bs")
                                && (possibileLanguages.contains("hr")
                                        || possibileLanguages.contains(
                                                "sr-latn")))) {
                    if (htmlElementHasLang || systemId != null) {
                        detectedLanguage = getDetectedLanguageSerboCroatian();
                        setDocumentLanguage(detectedLanguage);
                    }
                    if ("sh".equals(detectedLanguage)) {
                        checkLangAttributeSerboCroatian();
                        return;
                    }
                }
            }
            if ("".equals(detectedLanguage)) {
                if (!htmlElementHasLang && errorHandler != null) {
                    String message = "Consider adding a \u201Clang\u201D"
                            + " attribute to the \u201Chtml\u201D"
                            + " start tag to declare the language"
                            + " of this document.";
                    SAXParseException spe = new SAXParseException(message,
                            htmlStartTagLocator);
                    errorHandler.warning(spe);
                }
                contentHandler.endDocument();
                return;
            }
            String detectedLanguageName = "";
            String preferredLanguageCode = "";
            ULocale locale = new ULocale(detectedLanguage);
            String detectedLanguageCode = locale.getLanguage();
            if ("no".equals(detectedLanguage)) {
                checkLangAttributeNorwegian();
                checkContentLanguageHeaderNorwegian(detectedLanguage,
                        detectedLanguageName, detectedLanguageCode);
                return;
            }
            if ("zh-hans".equals(detectedLanguage)) {
                detectedLanguageName = "Simplified Chinese";
                preferredLanguageCode = "zh-hans";
            } else if ("zh-hant".equals(detectedLanguage)) {
                detectedLanguageName = "Traditional Chinese";
                preferredLanguageCode = "zh-hant";
            } else if ("mhr".equals(detectedLanguage)) {
                detectedLanguageName = "Meadow Mari";
                preferredLanguageCode = "mhr";
            } else if ("mrj".equals(detectedLanguage)) {
                detectedLanguageName = "Hill Mari";
                preferredLanguageCode = "mrj";
            } else if ("nah".equals(detectedLanguage)) {
                detectedLanguageName = "Nahuatl";
                preferredLanguageCode = "nah";
            } else if ("pnb".equals(detectedLanguage)) {
                detectedLanguageName = "Western Panjabi";
                preferredLanguageCode = "pnb";
            } else if ("sr-cyrl".equals(detectedLanguage)) {
                detectedLanguageName = "Serbian";
                preferredLanguageCode = "sr";
            } else if ("sr-latn".equals(detectedLanguage)) {
                detectedLanguageName = "Serbian";
                preferredLanguageCode = "sr";
            } else if ("uz-cyrl".equals(detectedLanguage)) {
                detectedLanguageName = "Uzbek";
                preferredLanguageCode = "uz";
            } else if ("uz-latn".equals(detectedLanguage)) {
                detectedLanguageName = "Uzbek";
                preferredLanguageCode = "uz";
            } else if ("zxx".equals(detectedLanguage)) {
                detectedLanguageName = "Lorem ipsum text";
                preferredLanguageCode = "zxx";
            } else {
                detectedLanguageName = locale.getDisplayName();
                preferredLanguageCode = detectedLanguageCode;
            }
            checkLangAttribute(detectedLanguage, detectedLanguageName,
                    detectedLanguageCode, preferredLanguageCode);
            checkDirAttribute(detectedLanguage, detectedLanguageName,
                    detectedLanguageCode, preferredLanguageCode);
            checkContentLanguageHeader(detectedLanguage, detectedLanguageName,
                    detectedLanguageCode, preferredLanguageCode);
        } catch (LangDetectException e) {
        }
    }

    private void setDocumentLanguage(String languageTag) {
        if (request != null) {
            request.setAttribute(
                    "http://validator.nu/properties/document-language",
                    languageTag);
        }
    }

    private String getDetectedLanguageSerboCroatian() throws SAXException {
        if ("hr".equals(declaredLangCode) || "hr".equals(tld)) {
            return "hr";
        }
        if ("sr".equals(declaredLangCode) || ".rs".equals(tld)) {
            return "sr-latn";
        }
        if ("bs".equals(declaredLangCode) || ".ba".equals(tld)) {
            return "bs";
        }
        return "sh";
    }

    private void checkLangAttributeSerboCroatian() throws SAXException {
        String lowerCaseLang = htmlElementLangAttrValue.toLowerCase();
        String langWarning = "";
        if (!htmlElementHasLang) {
            langWarning = "This document appears to be written in either"
                    + " Croatian, Serbian, or Bosnian. Consider adding either"
                    + " \u201Clang=\"hr\"\u201D, \u201Clang=\"sr\"\u201D, or"
                    + " \u201Clang=\"bs\"\u201D to the"
                    + " \u201Chtml\u201D start tag.";
        } else if (!("hr".equals(declaredLangCode)
                || "sr".equals(declaredLangCode)
                || "bs".equals(declaredLangCode))) {
            langWarning = String.format(
                    "This document appears to be written in either Croatian,"
                            + " Serbian, or Bosnian, but the \u201Chtml\u201D"
                            + " start tag has %s. Consider using either"
                            + " \u201Clang=\"hr\"\u201D,"
                            + " \u201Clang=\"sr\"\u201D, or"
                            + " \u201Clang=\"bs\"\u201D instead.",
                    getAttValueExpr("lang", lowerCaseLang));
        }
        if (!"".equals(langWarning)) {
            warn(langWarning);
        }
    }

    private void checkLangAttributeNorwegian() throws SAXException {
        String lowerCaseLang = htmlElementLangAttrValue.toLowerCase();
        String langWarning = "";
        if (!htmlElementHasLang) {
            langWarning = "This document appears to be written in Norwegian"
                    + " Consider adding either"
                    + " \u201Clang=\"nn\"\u201D or \u201Clang=\"nb\"\u201D"
                    + " (or variant) to the \u201Chtml\u201D start tag.";
        } else if (!("no".equals(declaredLangCode)
                || "nn".equals(declaredLangCode)
                || "nb".equals(declaredLangCode))) {
            langWarning = String.format(
                    "This document appears to be written in Norwegian, but the"
                            + " \u201Chtml\u201D start tag has %s. Consider"
                            + " using either \u201Clang=\"nn\"\u201D or"
                            + " \u201Clang=\"nb\"\u201D (or variant) instead.",
                    getAttValueExpr("lang", lowerCaseLang));
        }
        if (!"".equals(langWarning)) {
            warn(langWarning);
        }
    }

    private void checkContentLanguageHeaderNorwegian(String detectedLanguage,
            String detectedLanguageName, String detectedLanguageCode)
                    throws SAXException {
        if ("".equals(httpContentLangHeader)
                || httpContentLangHeader.contains(",")) {
            return;
        }
        String lowerCaseContentLang = httpContentLangHeader.toLowerCase();
        String contentLangCode = new ULocale(
                lowerCaseContentLang).getLanguage();
        if (!("no".equals(contentLangCode) || "nn".equals(contentLangCode)
                || "nb".equals(contentLangCode))) {
            warn("This document appears to be written in Norwegian but the"
                    + " value of the HTTP \u201CContent-Language\u201D header"
                    + " is \u201C" + lowerCaseContentLang + "\u201D. Consider"
                    + " changing it to \u201Cnn\u201D or \u201Cnn\u201D"
                    + " (or variant) instead.");
        }
    }

    private void checkLangAttribute(String detectedLanguage,
            String detectedLanguageName, String detectedLanguageCode,
            String preferredLanguageCode) throws SAXException {
        String langWarning = "";
        String lowerCaseLang = htmlElementLangAttrValue.toLowerCase();
        if (!htmlElementHasLang) {
            langWarning = String.format(
                    "This document appears to be written in %s."
                            + " Consider adding \u201Clang=\"%s\"\u201D"
                            + " (or variant) to the \u201Chtml\u201D"
                            + " start tag.",
                    detectedLanguageName, preferredLanguageCode);
        } else {
            if (request != null) {
                if ("".equals(lowerCaseLang)) {
                    request.setAttribute(
                            "http://validator.nu/properties/lang-empty", true);
                } else {
                    request.setAttribute(
                            "http://validator.nu/properties/lang-value",
                            lowerCaseLang);
                }
            }
            if ("tl".equals(detectedLanguageCode)
                    && ("ceb".equals(declaredLangCode)
                            || "ilo".equals(declaredLangCode)
                            || "pag".equals(declaredLangCode)
                            || "war".equals(declaredLangCode))) {
                return;
            }
            if ("id".equals(detectedLanguageCode)
                    && "min".equals(declaredLangCode)) {
                return;
            }
            if ("ms".equals(detectedLanguageCode)
                    && "min".equals(declaredLangCode)) {
                return;
            }
            if ("hr".equals(detectedLanguageCode)
                    && ("sr".equals(declaredLangCode)
                            || "bs".equals(declaredLangCode)
                            || "sh".equals(declaredLangCode))) {
                return;
            }
            if ("sr".equals(detectedLanguageCode)
                    && ("hr".equals(declaredLangCode)
                            || "bs".equals(declaredLangCode)
                            || "sh".equals(declaredLangCode))) {
                return;
            }
            if ("bs".equals(detectedLanguageCode)
                    && ("hr".equals(declaredLangCode)
                            || "sr".equals(declaredLangCode)
                            || "sh".equals(declaredLangCode))) {
                return;
            }
            if ("de".equals(detectedLanguageCode)
                    && ("bar".equals(declaredLangCode)
                            || "gsw".equals(declaredLangCode)
                            || "lb".equals(declaredLangCode))) {
                return;
            }
            if ("zh".equals(detectedLanguageCode)
                    && "yue".equals(lowerCaseLang)) {
                return;
            }
            if ("es".equals(detectedLanguageCode)
                    && ("an".equals(declaredLangCode)
                            || "ast".equals(declaredLangCode))) {
                return;
            }
            if ("it".equals(detectedLanguageCode)
                    && ("co".equals(declaredLangCode)
                            || "pms".equals(declaredLangCode)
                            || "vec".equals(declaredLangCode)
                            || "lmo".equals(declaredLangCode)
                            || "scn".equals(declaredLangCode)
                            || "nap".equals(declaredLangCode))) {
                return;
            }
            if ("rw".equals(detectedLanguageCode)
                    && "rn".equals(declaredLangCode)) {
                return;
            }
            if ("mhr".equals(detectedLanguageCode)
                    && ("chm".equals(declaredLangCode)
                            || "mrj".equals(declaredLangCode))) {
                return;
            }
            if ("mrj".equals(detectedLanguageCode)
                    && ("chm".equals(declaredLangCode)
                            || "mhr".equals(declaredLangCode))) {
                return;
            }
            if ("ru".equals(detectedLanguageCode)
                    && "bg".equals(declaredLangCode)) {
                return;
            }
            String message = "This document appears to be written in %s"
                    + " but the \u201Chtml\u201D start tag has %s. Consider"
                    + " using \u201Clang=\"%s\"\u201D (or variant) instead.";
            if (zhSubtagMismatch(detectedLanguage, lowerCaseLang)
                    || !declaredLangCode.equals(detectedLanguageCode)) {
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/lang-wrong", true);
                }
                langWarning = String.format(message, detectedLanguageName,
                        getAttValueExpr("lang", htmlElementLangAttrValue),
                        preferredLanguageCode);
            }
        }
        if (!"".equals(langWarning)) {
            warn(langWarning);
        }
    }

    private void checkContentLanguageHeader(String detectedLanguage,
            String detectedLanguageName, String detectedLanguageCode,
            String preferredLanguageCode) throws SAXException {
        if ("".equals(httpContentLangHeader)
                || httpContentLangHeader.contains(",")) {
            return;
        }
        String message = "";
        String lowerCaseContentLang = httpContentLangHeader.toLowerCase();
        String contentLangCode = new ULocale(
                lowerCaseContentLang).getLanguage();
        if ("tl".equals(detectedLanguageCode) && ("ceb".equals(contentLangCode)
                || "ilo".equals(contentLangCode)
                || "pag".equals(contentLangCode)
                || "war".equals(contentLangCode))) {
            return;
        }
        if ("id".equals(detectedLanguageCode)
                && "min".equals(contentLangCode)) {
            return;
        }
        if ("ms".equals(detectedLanguageCode)
                && "min".equals(contentLangCode)) {
            return;
        }
        if ("hr".equals(detectedLanguageCode) && ("sr".equals(contentLangCode)
                || "bs".equals(contentLangCode)
                || "sh".equals(contentLangCode))) {
            return;
        }
        if ("sr".equals(detectedLanguageCode) && ("hr".equals(contentLangCode)
                || "bs".equals(contentLangCode)
                || "sh".equals(contentLangCode))) {
            return;
        }
        if ("bs".equals(detectedLanguageCode) && ("hr".equals(contentLangCode)
                || "sr".equals(contentLangCode)
                || "sh".equals(contentLangCode))) {
            return;
        }
        if ("de".equals(detectedLanguageCode) && ("bar".equals(contentLangCode)
                || "gsw".equals(contentLangCode)
                || "lb".equals(contentLangCode))) {
            return;
        }
        if ("zh".equals(detectedLanguageCode)
                && "yue".equals(lowerCaseContentLang)) {
            return;
        }
        if ("es".equals(detectedLanguageCode) && ("an".equals(contentLangCode)
                || "ast".equals(contentLangCode))) {
            return;
        }
        if ("it".equals(detectedLanguageCode) && ("co".equals(contentLangCode)
                || "pms".equals(contentLangCode)
                || "vec".equals(contentLangCode)
                || "lmo".equals(contentLangCode)
                || "scn".equals(contentLangCode)
                || "nap".equals(contentLangCode))) {
            return;
        }
        if ("rw".equals(detectedLanguageCode)
                && "rn".equals(contentLangCode)) {
            return;
        }
        if ("mhr".equals(detectedLanguageCode) && ("chm".equals(contentLangCode)
                || "mrj".equals(contentLangCode))) {
            return;
        }
        if ("mrj".equals(detectedLanguageCode) && ("chm".equals(contentLangCode)
                || "mhr".equals(contentLangCode))) {
            return;
        }
        if ("ru".equals(detectedLanguageCode)
                && "bg".equals(contentLangCode)) {
            return;
        }
        if (zhSubtagMismatch(detectedLanguage, lowerCaseContentLang)
                || !contentLangCode.equals(detectedLanguageCode)) {
            message = "This document appears to be written in %s but the value"
                    + " of the HTTP \u201CContent-Language\u201D header is"
                    + " \u201C%s\u201D. Consider changing it to"
                    + " \u201C%s\u201D (or variant).";
            String warning = String.format(message, detectedLanguageName,
                    lowerCaseContentLang, preferredLanguageCode,
                    preferredLanguageCode);
            if (errorHandler != null) {
                SAXParseException spe = new SAXParseException(warning, null);
                errorHandler.warning(spe);
            }
        }
        if (htmlElementHasLang) {
            message = "The value of the HTTP \u201CContent-Language\u201D"
                    + " header is \u201C%s\u201D but it will be ignored because"
                    + " the \u201Chtml\u201D start tag has %s.";
            String lowerCaseLang = htmlElementLangAttrValue.toLowerCase();
            if (htmlElementHasLang) {
                if (zhSubtagMismatch(lowerCaseContentLang, lowerCaseLang)
                        || !contentLangCode.equals(declaredLangCode)) {
                    warn(String.format(message, httpContentLangHeader,
                            getAttValueExpr("lang", //
                                    htmlElementLangAttrValue)));
                }
            }
        }
    }

    private void checkDirAttribute(String detectedLanguage,
            String detectedLanguageName, String detectedLanguageCode,
            String preferredLanguageCode) throws SAXException {
        if (Arrays.binarySearch(RTL_LANGS, detectedLanguageCode) < 0) {
            return;
        }
        String dirWarning = "";
        if (!hasDir) {
            dirWarning = String.format(
                    "This document appears to be written in %s."
                            + " Consider adding \u201Cdir=\"rtl\"\u201D"
                            + " to the \u201Chtml\u201D start tag.",
                    detectedLanguageName, preferredLanguageCode);
        } else if (!"rtl".equals(dirAttrValue)) {
            String message = "This document appears to be written in %s"
                    + " but the \u201Chtml\u201D start tag has %s."
                    + " Consider using \u201Cdir=\"rtl\"\u201D instead.";
            dirWarning = String.format(message, detectedLanguageName,
                    getAttValueExpr("dir", dirAttrValue));
        }
        if (!"".equals(dirWarning)) {
            warn(dirWarning);
        }
    }

    private boolean zhSubtagMismatch(String expectedLanguage,
            String declaredLanguage) {
        return (("zh-hans".equals(expectedLanguage)
                && (declaredLanguage.contains("zh-tw")
                        || declaredLanguage.contains("zh-hant")))
                || ("zh-hant".equals(expectedLanguage)
                        && (declaredLanguage.contains("zh-cn")
                                || declaredLanguage.contains("zh-hans"))));
    }

    private String getAttValueExpr(String attName, String attValue) {
        if ("".equals(attValue)) {
            return String.format("an empty \u201c%s\u201d attribute", attName);
        } else {
            return String.format("\u201C%s=\"%s\"\u201D", attName, attValue);
        }
    }

    private void warn(String message) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message,
                    htmlStartTagLocator);
            errorHandler.warning(spe);
        }
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
}
