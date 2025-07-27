/*
 * Copyright (c) 2016-2019 Mozilla Foundation
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

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.ibm.icu.util.ULocale;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.Host;
import io.mola.galimatias.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class LanguageDetectingChecker extends Checker {

    private static final String languageList = //
            "nu/validator/localentities/files/language-profiles-list.txt";

    private static final String profilesDir = //
            "nu/validator/localentities/files/language-profiles/";

    private static final Map<String, String[]> LANG_TAGS_BY_TLD = //
            new HashMap<>();

    private String systemId;

    private String tld;

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

    private int currentOpenElementsWithSkipName = 0;

    private int nonWhitespaceCharacterCount;

    private static final int MAX_CHARS = 30720;

    private static final int MIN_CHARS = 1024;

    private static final double MIN_PROBABILITY = .90;

    private static final String[] RTL_LANGS = { "ar", "azb", "ckb", "dv", "fa",
            "he", "pnb", "ps", "sd", "ug", "ur" };

    private static final String[] SKIP_NAMES = { "a", "details", "figcaption",
            "form", "li", "nav", "pre", "script", "select", "span", "style",
            "summary", "td", "textarea", "th", "tr" };

    static {
        if (!"0".equals(System.getProperty("nu.validator.checker.enableLangDetection"))) {
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
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        LanguageDetectingChecker.class.getClassLoader() //
                                .getResourceAsStream(languageList),
                        "UTF-8"));
                List<String> languageTags = new ArrayList<>();
                String languageTagAndName = br.readLine();
                while (languageTagAndName != null) {
                    languageTags.add(languageTagAndName.split("\t")[0]);
                    languageTagAndName = br.readLine();
                }
                List<String> profiles = new ArrayList<>();
                for (String languageTag : languageTags) {
                    profiles.add((new BufferedReader(new InputStreamReader(
                            LanguageDetectingChecker.class.getClassLoader() //
                                    .getResourceAsStream(profilesDir + languageTag),
                            "UTF-8"))).readLine());
                }
                DetectorFactory.clear();
                DetectorFactory.loadProfile(profiles);
                try {
                    long seed = Long.parseLong(System.getProperty("nu.validator.checker.langDetectionSeed"));
                    DetectorFactory.setSeed(seed);
                } catch (NumberFormatException e) {
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (LangDetectException e) {
            }
        }
    }

    private boolean shouldAppendToLangdetectContent() {
        return (inBody && currentOpenElementsWithSkipName < 1
                && currentOpenElementsInDifferentLang < 1
                && nonWhitespaceCharacterCount < MAX_CHARS);
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
            warn(langWarning, htmlStartTagLocator);
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
            warn(langWarning, htmlStartTagLocator);
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
            warn("This document appears to be written in"
                    + " Norwegian but the value of the HTTP"
                    + " \u201CContent-Language\u201D header is" + " \u201C"
                    + lowerCaseContentLang + "\u201D. Consider"
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
            if ("el".equals(detectedLanguageCode)
                    && "grc".equals(declaredLangCode)) {
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
            warn(langWarning, htmlStartTagLocator);
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
        if ("hr".equals(detectedLanguageCode)
                && ("sr".equals(contentLangCode) || "bs".equals(contentLangCode)
                        || "sh".equals(contentLangCode))) {
            return;
        }
        if ("sr".equals(detectedLanguageCode)
                && ("hr".equals(contentLangCode) || "bs".equals(contentLangCode)
                        || "sh".equals(contentLangCode))) {
            return;
        }
        if ("bs".equals(detectedLanguageCode)
                && ("hr".equals(contentLangCode) || "sr".equals(contentLangCode)
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
        if ("el".equals(detectedLanguageCode)
                && "grc".equals(contentLangCode)) {
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
        if ("rw".equals(detectedLanguageCode) && "rn".equals(contentLangCode)) {
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
        if ("ru".equals(detectedLanguageCode) && "bg".equals(contentLangCode)) {
            return;
        }
        if (zhSubtagMismatch(detectedLanguage, lowerCaseContentLang)
                || !contentLangCode.equals(detectedLanguageCode)) {
            message = "This document appears to be written in %s but the value"
                    + " of the HTTP \u201CContent-Language\u201D header is"
                    + " \u201C%s\u201D. Consider changing it to"
                    + " \u201C%s\u201D (or variant).";
            warn(String.format(message, detectedLanguageName,
                    lowerCaseContentLang, preferredLanguageCode,
                    preferredLanguageCode));
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
                            getAttValueExpr("lang", htmlElementLangAttrValue)),
                            htmlStartTagLocator);
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
            warn(dirWarning, htmlStartTagLocator);
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

    public LanguageDetectingChecker() {
        super();
    }

    private HttpServletRequest request;

    public void setHttpContentLanguageHeader(String httpContentLangHeader) {
        if (httpContentLangHeader != null) {
            this.httpContentLangHeader = httpContentLangHeader;
        }
    }

    /**
     * @see nu.validator.checker.Checker#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        if (!"0".equals(System.getProperty(
                "nu.validator.checker.enableLangDetection"))
                && htmlStartTagLocator != null) {
                detectLanguageAndCheckAgainstDeclaredLanguage();
            }
        }

    private void warnIfMissingLang() throws SAXException {
        if (!htmlElementHasLang) {
            String message = "Consider adding a \u201Clang\u201D"
                    + " attribute to the \u201Chtml\u201D"
                    + " start tag to declare the language"
                    + " of this document.";
            warn(message, htmlStartTagLocator);
        }
    }

    private void detectLanguageAndCheckAgainstDeclaredLanguage()
            throws SAXException {
        if (nonWhitespaceCharacterCount < MIN_CHARS) {
            warnIfMissingLang();
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
                warnIfMissingLang();
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

    /**
     * @see nu.validator.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }
        if (nonWhitespaceCharacterCount < MAX_CHARS) {
            documentContent.append(elementContent);
            elementContent.setLength(0);
        }
        if ("body".equals(localName)) {
            inBody = false;
            currentOpenElementsWithSkipName = 0;
        }
        if (currentOpenElementsInDifferentLang > 0) {
            currentOpenElementsInDifferentLang--;
            if (currentOpenElementsInDifferentLang < 0) {
                currentOpenElementsInDifferentLang = 0;
            }
        } else {
            if (Arrays.binarySearch(SKIP_NAMES, localName) >= 0) {
                currentOpenElementsWithSkipName--;
                if (currentOpenElementsWithSkipName < 0) {
                    currentOpenElementsWithSkipName = 0;
                }
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        request = getRequest();
        httpContentLangHeader = "";
        tld = "";
        htmlStartTagLocator = null;
        inBody = false;
        currentOpenElementsInDifferentLang = 0;
        currentOpenElementsWithSkipName = 0;
        nonWhitespaceCharacterCount = 0;
        elementContent = new StringBuilder();
        documentContent = new StringBuilder();
        htmlElementHasLang = false;
        htmlElementLangAttrValue = "";
        declaredLangCode = "";
        hasDir = false;
        dirAttrValue = "";
        documentContent.setLength(0);
        currentOpenElementsWithSkipName = 0;
        try {
            systemId = getDocumentLocator().getSystemId();
            if (systemId != null && systemId.startsWith("http")) {
                Host hostname = URL.parse(systemId).host();
                if (hostname != null) {
                    String host = hostname.toString();
                    tld = host.substring(host.lastIndexOf(".") + 1);
                }
            }
        } catch (GalimatiasParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }
        if ("html".equals(localName)) {
            htmlStartTagLocator = new LocatorImpl(getDocumentLocator());
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
                        }
                    }
                }
            }
        }
        if (Arrays.binarySearch(SKIP_NAMES, localName) >= 0) {
            currentOpenElementsWithSkipName++;
        }
    }

    /**
     * @see nu.validator.checker.Checker#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (shouldAppendToLangdetectContent()) {
            elementContent.append(ch, start, length);
        }
        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case '#':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    continue;
                default:
                    if (shouldAppendToLangdetectContent()) {
                        nonWhitespaceCharacterCount++;
                    }
            }
        }
    }
}
