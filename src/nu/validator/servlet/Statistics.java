/*
 * Copyright (c) 2012-2017 Mozilla Foundation
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
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletResponse;

import nu.validator.htmlparser.sax.HtmlSerializer;
import nu.validator.xml.EmptyAttributes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Statistics {

    public static final Statistics STATISTICS;

    private static final char[] VALIDATOR_STATISTICS = "Validator statistics".toCharArray();

    private static final char[] COUNTER_NAME = "Counter".toCharArray();

    private static final char[] COUNTER_VALUE = "Value".toCharArray();

    private static final char[] COUNTER_PROPORTION = "Proportion".toCharArray();

    private static final char[] TOTAL_VALIDATIONS = "Total number of validations".toCharArray();

    private static final char[] UPTIME_DAYS = "Uptime in days".toCharArray();

    private static final char[] VALIDATIONS_PER_SECOND = "Validations per second".toCharArray();

    private static final char[] SORT_LANGS_SCRIPT = (""
            + " var rows = document.querySelectorAll('tr');"
            + " var langRows = new Array();"
            + " for (var i=0; i < rows.length; i++) { var row = rows[i];"
            + "   if (row.textContent.indexOf('Detected language') > -1) {"
            + "     var sortnr = parseInt(row.cells[1].textContent"
            + "       || row.cells[0].innerText);"
            + "     if (sortnr == 0) {"
            + "       row.remove();"
            + "     } else if (!isNaN(sortnr)) {"
            + "       langRows.push([sortnr, row]);"
            + "     }"
            + "   }"
            + " } langRows.sort(function(x,y) { return x[0] - y[0]; });"
            + " langRows.reverse();"
            + " for (var i=0; i<langRows.length; i++) {"
            + "   document.querySelector('tbody').appendChild(langRows[i][1]);"
            + " }"
            + " var langValRows = new Array();"
            + " for (var i=0; i < rows.length; i++) { var row = rows[i];"
            + "   if (row.textContent.indexOf('<html lang>: ') > -1) {"
            + "     var sortnr = parseInt(row.cells[1].textContent"
            + "       || row.cells[0].innerText);"
            + "     if (sortnr == 0) {"
            + "       row.remove();"
            + "     } else if (!isNaN(sortnr)) {"
            + "       langValRows.push([sortnr, row]);"
            + "     }"
            + "   }"
            + " } langValRows.sort(function(x,y) { return x[0] - y[0]; });"
            + " langValRows.reverse();"
            + " for (var i=0; i<langValRows.length; i++) {"
            + "   document.querySelector('tbody').appendChild(langValRows[i][1]);"
            + " }").toCharArray();

    private static final char[] STYLESHEET = (""
            + " body { font-family: sans-serif; }"
            + " td { padding: 4px 8px 4px 8px; }"
            + " tr:nth-child(even) { background-color: #cde; }").toCharArray();

    public enum Field {
        // @formatter:off
        INPUT_GET("Input: GET-based"), //
        INPUT_POST("Input: POST-based"), //
        INPUT_ENTITY_BODY("\u2514 Entity-body input"), //
        INPUT_TEXT_FIELD("\u2514 Text-field input"), //
        INPUT_FILE_UPLOAD("\u2514 File-upload input"), //
        INPUT_HTML("Input: text/html"), //
        INPUT_UNSUPPORTED("Input: unsupported content type (error)"), //
        INPUT_XML("Input: application/xhtml+xml or other XML content type"), //
        OUTPUT_XML("Output: XML"), //
        OUTPUT_HTML("Output: HTML"), //
        OUTPUT_JSON("Output: JSON"), //
        OUTPUT_GNU("Output: GNU"), //
        OUTPUT_TEXT("Output: Text"), //
        OUTPUT_XHTML("Output: XHTML"), //
        SHOW_SOURCE("Show: source"), //
        SHOW_OUTLINE("Show: outline"), //
        IMAGE_REPORT("Show: image report"), //
        HTML5_SCHEMA("Schema: HTML5 schema"), //
        HTML5_RDFA_LITE_SCHEMA("Schema: HTML5+RDFa Lite schema"), //
        XHTML1_DOCTYPE("Doctype: XHTML1"), //
        HTML4_DOCTYPE("Doctype: HTML4"), //
        ABOUT_LEGACY_COMPAT("Doctype: about:legacy-compat"), //
        AUTO_SCHEMA("Schema: automatically chosen"), //
        PRESET_SCHEMA("Schema: preset"), //
        HTML4_STRICT_SCHEMA("Schema: legacy Strict"), //
        HTML4_TRANSITIONAL_SCHEMA("Schema: legacy Transitional"), //
        HTML4_FRAMESET_SCHEMA("Schema: legacy Frameset"), //
        XHTML1_COMPOUND_SCHEMA("Schema: legacy XHTML+SVG+MathML"), //
        SVG_SCHEMA("Schema: SVG"), //
        BUILT_IN_NON_PRESET("Schema: custom combined from built-ins"), //
        EXTERNAL_SCHEMA_NON_SCHEMATRON("Schema: non-schematron custom"), //
        EXTERNAL_SCHEMA_SCHEMATRON("Schema: schematron custom"), //
        LAX_TYPE("Content type: being lax"), //
        CUSTOM_ENC("Encoding: manually set"), //
        PARSER_XML_EXTERNAL("Parser: set to XML with external entities"), //
        PARSER_HTML4("Parser: set to explicit HTML4 mode"), //
        XMLNS_FILTER("Parser: XMLNS filter set"), //
        LOGIC_ERROR("Logic errors in schema stats"), //
        LINK_WITH_CHARSET_FOUND("<link charset> found"), //
        SCRIPT_WITH_CHARSET_FOUND("<script charset> found"), //
        STYLE_IN_BODY_FOUND("<style> in <body> found"), //
        REL_ALTERNATE_FOUND("rel=alternate found"), //
        REL_AUTHOR_FOUND("rel=author found"), //
        REL_BOOKMARK_FOUND("rel=bookmark found"), //
        REL_CANONICAL_FOUND("rel=canonical found"), //
        REL_DNS_PREFETCH_FOUND("rel=dns-prefetch found"), //
        REL_EXTERNAL_FOUND("rel=external found"), //
        REL_HELP_FOUND("rel=help found"), //
        REL_ICON_FOUND("rel=icon found"), //
        REL_LICENSE_FOUND("rel=license found"), //
        REL_NEXT_FOUND("rel=next found"), //
        REL_NOFOLLOW_FOUND("rel=nofollow found"), //
        REL_NOOPENER_FOUND("rel=noopener found"), //
        REL_NOREFERRER_FOUND("rel=noreferrer found"), //
        REL_PINGBACK_FOUND("rel=pingback found"), //
        REL_PRECONNECT_FOUND("rel=preconnect found"), //
        REL_PREFETCH_FOUND("rel=prefetch found"), //
        REL_PRELOAD_FOUND("rel=preload found"), //
        REL_PRERENDER_FOUND("rel=prerender found"), //
        REL_PREV_FOUND("rel=prev found"), //
        REL_SEARCH_FOUND("rel=search found"), //
        REL_SERVICEWORKER_FOUND("rel=serviceworker found"), //
        REL_STYLESHEET_FOUND("rel=stylesheet found"), //
        REL_TAG_FOUND("rel=tag found"), //
        APPLE_TOUCH_ICON_WITH_SIZES_FOUND("<link rel=\"apple-touch-icon\" sizes=\"\u2026\"> found"), //
        LANG_FOUND("<html lang> found"), //
        LANG_WRONG("<html lang> does not match detected language"), //
        LANG_EMPTY("<html lang>: empty (lang=\"\")"), //
        LANG_OTHER("<html lang>: other (unrecognized/invalid)"), //
        LANG_AF("<html lang>: af"), //
        LANG_AM("<html lang>: am"), //
        LANG_AR("<html lang>: ar"), //
        LANG_AR_AA("<html lang>: ar-aa"), //
        LANG_AR_AE("<html lang>: ar-ae"), //
        LANG_AR_AR("<html lang>: ar-ar"), //
        LANG_AR_DZ("<html lang>: ar-dz"), //
        LANG_AR_EG("<html lang>: ar-eg"), //
        LANG_AR_JO("<html lang>: ar-jo"), //
        LANG_AR_KW("<html lang>: ar-kw"), //
        LANG_AR_OM("<html lang>: ar-om"), //
        LANG_AR_QA("<html lang>: ar-qa"), //
        LANG_AR_SA("<html lang>: ar-sa"), //
        LANG_AR_SY("<html lang>: ar-sy"), //
        LANG_AT("<html lang>: at"), //
        LANG_AZ("<html lang>: az"), //
        LANG_AZ_AZ("<html lang>: az-az"), //
        LANG_BA("<html lang>: ba"), //
        LANG_BE("<html lang>: be"), //
        LANG_BG("<html lang>: bg"), //
        LANG_BG_BG("<html lang>: bg-bg"), //
        LANG_BN("<html lang>: bn"), //
        LANG_BN_BD("<html lang>: bn-bd"), //
        LANG_BR("<html lang>: br"), //
        LANG_BS("<html lang>: bs"), //
        LANG_BS_BA("<html lang>: bs-ba"), //
        LANG_BS_LATN("<html lang>: bs-latn"), //
        LANG_CA("<html lang>: ca"), //
        LANG_CA_ES("<html lang>: ca-es"), //
        LANG_CE("<html lang>: ce"), //
        LANG_CH("<html lang>: ch"), //
        LANG_CHR("<html lang>: chr"), //
        LANG_CKB("<html lang>: ckb"), //
        LANG_CN("<html lang>: cn"), //
        LANG_CR("<html lang>: cr"), //
        LANG_CS("<html lang>: cs"), //
        LANG_CS_CZ("<html lang>: cs-cz"), //
        LANG_CY("<html lang>: cy"), //
        LANG_CZ("<html lang>: cz"), //
        LANG_DA("<html lang>: da"), //
        LANG_DA_DK("<html lang>: da-dk"), //
        LANG_DE("<html lang>: de"), //
        LANG_DE_AT("<html lang>: de-at"), //
        LANG_DE_CH("<html lang>: de-ch"), //
        LANG_DE_DE("<html lang>: de-de"), //
        LANG_DE__DE("<html lang>: de_de"), //
        LANG_DK("<html lang>: dk"), //
        LANG_EL("<html lang>: el"), //
        LANG_EL_GR("<html lang>: el-gr"), //
        LANG_EN("<html lang>: en"), //
        LANG_ENG("<html lang>: eng"), //
        LANG_EN_AE("<html lang>: en-ae"), //
        LANG_EN_AU("<html lang>: en-au"), //
        LANG_EN__AU("<html lang>: en_au"), //
        LANG_EN_CA("<html lang>: en-ca"), //
        LANG_EN__CA("<html lang>: en_ca"), //
        LANG_EN_DE("<html lang>: en-de"), //
        LANG_EN_EG("<html lang>: en-eg"), //
        LANG_EN_EN("<html lang>: en-en"), //
        LANG_EN__EN("<html lang>: en_en"), //
        LANG_EN_EU("<html lang>: en-eu"), //
        LANG_EN_GB("<html lang>: en-gb"), //
        LANG_EN__GB("<html lang>: en_gb"), //
        LANG_EN_HK("<html lang>: en-hk"), //
        LANG_EN_ID("<html lang>: en-id"), //
        LANG_EN_IE("<html lang>: en-ie"), //
        LANG_EN_IN("<html lang>: en-in"), //
        LANG_EN_MY("<html lang>: en-my"), //
        LANG_EN_NG("<html lang>: en-ng"), //
        LANG_EN_NZ("<html lang>: en-nz"), //
        LANG_EN_PH("<html lang>: en-ph"), //
        LANG_EN_PK("<html lang>: en-pk"), //
        LANG_EN_SG("<html lang>: en-sg"), //
        LANG_EN_UK("<html lang>: en-uk"), //
        LANG_EN_US("<html lang>: en-us"), //
        LANG_EN__US("<html lang>: en_us"), //
        LANG_EN_VN("<html lang>: en-vn"), //
        LANG_EN_ZA("<html lang>: en-za"), //
        LANG_ES("<html lang>: es"), //
        LANG_ES_AR("<html lang>: es-ar"), //
        LANG_ES__AR("<html lang>: es_ar"), //
        LANG_ES_CL("<html lang>: es-cl"), //
        LANG_ES_CO("<html lang>: es-co"), //
        LANG_ES_DO("<html lang>: es-do"), //
        LANG_ES_EC("<html lang>: es-ec"), //
        LANG_ES_ES("<html lang>: es-es"), //
        LANG_ES__ES("<html lang>: es_es"), //
        LANG_ES_LA("<html lang>: es-la"), //
        LANG_ES_MX("<html lang>: es-mx"), //
        LANG_ES_PE("<html lang>: es-pe"), //
        LANG_ES_PR("<html lang>: es-pr"), //
        LANG_ES_US("<html lang>: es-us"), //
        LANG_ES_VE("<html lang>: es-ve"), //
        LANG_ET("<html lang>: et"), //
        LANG_ET_EE("<html lang>: et-ee"), //
        LANG_EU("<html lang>: eu"), //
        LANG_FA("<html lang>: fa"), //
        LANG_FA_IR("<html lang>: fa-ir"), //
        LANG_FA__IR("<html lang>: fa_ir"), //
        LANG_FI("<html lang>: fi"), //
        LANG_FI_FI("<html lang>: fi-fi"), //
        LANG_FO("<html lang>: fo"), //
        LANG_FR("<html lang>: fr"), //
        LANG_FR_BE("<html lang>: fr-be"), //
        LANG_FR_CA("<html lang>: fr-ca"), //
        LANG_FR_CH("<html lang>: fr-ch"), //
        LANG_FR_FR("<html lang>: fr-fr"), //
        LANG_FR__FR("<html lang>: fr_fr"), //
        LANG_FR_MA("<html lang>: fr-ma"), //
        LANG_FR_US("<html lang>: fr-us"), //
        LANG_GA("<html lang>: ga"), //
        LANG_GB("<html lang>: gb"), //
        LANG_GE("<html lang>: ge"), //
        LANG_GL("<html lang>: gl"), //
        LANG_GL_ES("<html lang>: gl-es"), //
        LANG_GR("<html lang>: gr"), //
        LANG_GU("<html lang>: gu"), //
        LANG_HE("<html lang>: he"), //
        LANG_HE_IL("<html lang>: he-il"), //
        LANG_HI("<html lang>: hi"), //
        LANG_HR_HR("<html lang>: hr-hr"), //
        LANG_HU("<html lang>: hu"), //
        LANG_HU_HU("<html lang>: hu-hu"), //
        LANG_HY("<html lang>: hy"), //
        LANG_HY_AM("<html lang>: hy-am"), //
        LANG_ID("<html lang>: id"), //
        LANG_ID_ID("<html lang>: id-id"), //
        LANG_IG("<html lang>: ig"), //
        LANG_IN("<html lang>: in"), //
        LANG_IN_ID("<html lang>: in-id"), //
        LANG_IR("<html lang>: ir"), //
        LANG_IS("<html lang>: is"), //
        LANG_IT("<html lang>: it"), //
        LANG_IT_IT("<html lang>: it-it"), //
        LANG_IT__IT("<html lang>: it_it"), //
        LANG_IU("<html lang>: iu"), //
        LANG_IW("<html lang>: iw"), //
        LANG_JA("<html lang>: ja"), //
        LANG_JA_JP("<html lang>: ja-jp"), //
        LANG_JA__JP("<html lang>: ja_jp"), //
        LANG_JP("<html lang>: jp"), //
        LANG_JV("<html lang>: jv"), //
        LANG_KA("<html lang>: ka"), //
        LANG_KA_GE("<html lang>: ka-ge"), //
        LANG_KK("<html lang>: kk"), //
        LANG_KK_KK("<html lang>: kk-kk"), //
        LANG_KK_KZ("<html lang>: kk-kz"), //
        LANG_KM("<html lang>: km"), //
        LANG_KN("<html lang>: kn"), //
        LANG_KO("<html lang>: ko"), //
        LANG_KO_KR("<html lang>: ko-kr"), //
        LANG_KR("<html lang>: kr"), //
        LANG_KU("<html lang>: ku"), //
        LANG_KY("<html lang>: ky"), //
        LANG_KZ("<html lang>: kz"), //
        LANG_KZ_KZ("<html lang>: kz-kz"), //
        LANG_LANG("<html lang>: lang"), //
        LANG_LO("<html lang>: lo"), //
        LANG_LT("<html lang>: lt"), //
        LANG_LT_LT("<html lang>: lt-lt"), //
        LANG_LV("<html lang>: lv"), //
        LANG_LV_LV("<html lang>: lv-lv"), //
        LANG_MG("<html lang>: mg"), //
        LANG_MHR("<html lang>: mhr"), //
        LANG_MI("<html lang>: mi"), //
        LANG_MK("<html lang>: mk"), //
        LANG_MK_MK("<html lang>: mk-mk"), //
        LANG_ML("<html lang>: ml"), //
        LANG_MN("<html lang>: mn"), //
        LANG_MN_MN("<html lang>: mn-mn"), //
        LANG_MR("<html lang>: mr"), //
        LANG_MRJ("<html lang>: mrj"), //
        LANG_MR_IN("<html lang>: mr-in"), //
        LANG_MS("<html lang>: ms"), //
        LANG_MS_MY("<html lang>: ms-my"), //
        LANG_MT("<html lang>: mt"), //
        LANG_MUL("<html lang>: mul"), //
        LANG_MX("<html lang>: mx"), //
        LANG_MY("<html lang>: my"), //
        LANG_NAH("<html lang>: nah"), //
        LANG_NB_NO("<html lang>: nb-no"), //
        LANG_NE("<html lang>: ne"), //
        LANG_NL("<html lang>: nl"), //
        LANG_NL_BE("<html lang>: nl-be"), //
        LANG_NL_NL("<html lang>: nl-nl"), //
        LANG_NL__NL("<html lang>: nl_nl"), //
        LANG_NN("<html lang>: nn"), //
        LANG_NO("<html lang>: no"), //
        LANG_NO_NB("<html lang>: no-nb"), //
        LANG_NO_NO("<html lang>: no-no"), //
        LANG_NY("<html lang>: ny"), //
        LANG_OC("<html lang>: oc"), //
        LANG_OR("<html lang>: or"), //
        LANG_OS("<html lang>: os"), //
        LANG_PA("<html lang>: pa"), //
        LANG_PL("<html lang>: pl"), //
        LANG_PL_PL("<html lang>: pl-pl"), //
        LANG_PL__PL("<html lang>: pl_pl"), //
        LANG_PNB("<html lang>: pnb"), //
        LANG_PS("<html lang>: ps"), //
        LANG_PT("<html lang>: pt"), //
        LANG_PT_BR("<html lang>: pt-br"), //
        LANG_PT__BR("<html lang>: pt_br"), //
        LANG_PT_PT("<html lang>: pt-pt"), //
        LANG_RO("<html lang>: ro"), //
        LANG_RO_RO("<html lang>: ro-ro"), //
        LANG_RO__RO("<html lang>: ro_ro"), //
        LANG_RS("<html lang>: rs"), //
        LANG_RU("<html lang>: ru"), //
        LANG_RU_RU("<html lang>: ru-ru"), //
        LANG_RU__RU("<html lang>: ru_ru"), //
        LANG_RU_UA("<html lang>: ru-ua"), //
        LANG_RW("<html lang>: rw"), //
        LANG_SAH("<html lang>: sah"), //
        LANG_SD("<html lang>: sd"), //
        LANG_SE("<html lang>: se"), //
        LANG_SH("<html lang>: sh"), //
        LANG_SI("<html lang>: si"), //
        LANG_SK("<html lang>: sk"), //
        LANG_SK_SK("<html lang>: sk-sk"), //
        LANG_SL("<html lang>: sl"), //
        LANG_SL_SI("<html lang>: sl-si"), //
        LANG_SN("<html lang>: sn"), //
        LANG_SP("<html lang>: sp"), //
        LANG_SQ("<html lang>: sq"), //
        LANG_SQ_AL("<html lang>: sq-al"), //
        LANG_SR("<html lang>: sr"), //
        LANG_SR_LATN("<html lang>: sr-latn"), //
        LANG_SR_RS("<html lang>: sr-rs"), //
        LANG_SR_SR("<html lang>: sr-sr"), //
        LANG_SR_YU("<html lang>: sr-yu"), //
        LANG_ST("<html lang>: st"), //
        LANG_SU("<html lang>: su"), //
        LANG_SV("<html lang>: sv"), //
        LANG_SV_SE("<html lang>: sv-se"), //
        LANG_SW("<html lang>: sw"), //
        LANG_TA("<html lang>: ta"), //
        LANG_TE("<html lang>: te"), //
        LANG_TG("<html lang>: tg"), //
        LANG_TH("<html lang>: th"), //
        LANG_TH_TH("<html lang>: th-th"), //
        LANG_TL("<html lang>: tl"), //
        LANG_TR("<html lang>: tr"), //
        LANG_TR_TR("<html lang>: tr-tr"), //
        LANG_TR__TR("<html lang>: tr_tr"), //
        LANG_TT("<html lang>: tt"), //
        LANG_TW("<html lang>: tw"), //
        LANG_UA("<html lang>: ua"), //
        LANG_UG("<html lang>: ug"), //
        LANG_UK("<html lang>: uk"), //
        LANG_UK_UA("<html lang>: uk-ua"), //
        LANG_UR("<html lang>: ur"), //
        LANG_UR_PK("<html lang>: ur-pk"), //
        LANG_US("<html lang>: us"), //
        LANG_US_EN("<html lang>: us-en"), //
        LANG_UZ("<html lang>: uz"), //
        LANG_VI("<html lang>: vi"), //
        LANG_VI_VN("<html lang>: vi-vn"), //
        LANG_VI__VN("<html lang>: vi_vn"), //
        LANG_VN("<html lang>: vn"), //
        LANG_XH("<html lang>: xh"), //
        LANG_ZH("<html lang>: zh"), //
        LANG_ZH_CH("<html lang>: zh-ch"), //
        LANG_ZH_CMN("<html lang>: zh-cmn"), //
        LANG_ZH_CMN_HANS("<html lang>: zh-cmn-hans"), //
        LANG_ZH_CMN_HANT("<html lang>: zh-cmn-hant"), //
        LANG_ZH_CN("<html lang>: zh-cn"), //
        LANG_ZH__CN("<html lang>: zh_cn"), //
        LANG_ZH_HANS("<html lang>: zh-hans"), //
        LANG_ZH_HANS_CN("<html lang>: zh-hans-cn"), //
        LANG_ZH_HANT("<html lang>: zh-hant"), //
        LANG_ZH_HANT_HK("<html lang>: zh-hant-hk"), //
        LANG_ZH_HANT_TW("<html lang>: zh-hant-tw"), //
        LANG_ZH_HK("<html lang>: zh-hk"), //
        LANG_ZH__HK("<html lang>: zh_hk"), //
        LANG_ZH_TW("<html lang>: zh-tw"), //
        LANG_ZH__TW("<html lang>: zh_tw"), //
        LANG_ZU("<html lang>: zu"), //
        LANG_ZXX("<html lang>: zxx"), //
        DETECTEDLANG_AF("Detected language: Afrikaans"), //
        DETECTEDLANG_AZB("Detected language: South Azerbaijani"), //
        DETECTEDLANG_AM("Detected language: Amharic"), //
        DETECTEDLANG_AR("Detected language: Arabic"), //
        DETECTEDLANG_AZ("Detected language: Azerbaijani"), //
        DETECTEDLANG_BA("Detected language: Bashkir"), //
        DETECTEDLANG_BE("Detected language: Belarusian"), //
        DETECTEDLANG_BN("Detected language: Bengali"), //
        DETECTEDLANG_BO("Detected language: Tibetan"), //
        DETECTEDLANG_BS("Detected language: Bosnian"), //
        DETECTEDLANG_CA("Detected language: Catalan"), //
        DETECTEDLANG_CE("Detected language: Chechen"), //
        DETECTEDLANG_CHR("Detected language: Cherokee"), //
        DETECTEDLANG_CKB("Detected language: Sorani Kurdish"), //
        DETECTEDLANG_CR("Detected language: Cree"), //
        DETECTEDLANG_CS("Detected language: Czech"), //
        DETECTEDLANG_CY("Detected language: Welsh"), //
        DETECTEDLANG_DA("Detected language: Danish"), //
        DETECTEDLANG_DE("Detected language: German"), //
        DETECTEDLANG_DV("Detected language: Divehi"), //
        DETECTEDLANG_EL("Detected language: Greek"), //
        DETECTEDLANG_EN("Detected language: English"), //
        DETECTEDLANG_ES("Detected language: Spanish"), //
        DETECTEDLANG_ET("Detected language: Estonian"), //
        DETECTEDLANG_EU("Detected language: Basque"), //
        DETECTEDLANG_FA("Detected language: Persian"), //
        DETECTEDLANG_FI("Detected language: Finnish"), //
        DETECTEDLANG_FO("Detected language: Faroese"), //
        DETECTEDLANG_FR("Detected language: French"), //
        DETECTEDLANG_GA("Detected language: Irish"), //
        DETECTEDLANG_GU("Detected language: Gujarati"), //
        DETECTEDLANG_HA("Detected language: Hausa"), //
        DETECTEDLANG_HE("Detected language: Hebrew"), //
        DETECTEDLANG_HI("Detected language: Hindi"), //
        DETECTEDLANG_HR("Detected language: Croatian"), //
        DETECTEDLANG_HU("Detected language: Hungarian"), //
        DETECTEDLANG_HY("Detected language: Armenian"), //
        DETECTEDLANG_ID("Detected language: Indonesian"), //
        DETECTEDLANG_IG("Detected language: Igbo"), //
        DETECTEDLANG_IS("Detected language: Icelandic"), //
        DETECTEDLANG_IT("Detected language: Italian"), //
        DETECTEDLANG_IU("Detected language: Inuktitut"), //
        DETECTEDLANG_JA("Detected language: Japanese"), //
        DETECTEDLANG_JV("Detected language: Javanese"), //
        DETECTEDLANG_KA("Detected language: Georgian"), //
        DETECTEDLANG_KM("Detected language: Khmer"), //
        DETECTEDLANG_KK("Detected language: Kazakh"), //
        DETECTEDLANG_KN("Detected language: Kannada"), //
        DETECTEDLANG_KO("Detected language: Korean"), //
        DETECTEDLANG_KU("Detected language: Kurdish"), //
        DETECTEDLANG_KY("Detected language: Kyrgyz"), //
        DETECTEDLANG_LO("Detected language: Lao"), //
        DETECTEDLANG_LT("Detected language: Lithuanian"), //
        DETECTEDLANG_LV("Detected language: Latvian"), //
        DETECTEDLANG_MG("Detected language: Malagasy"), //
        DETECTEDLANG_MHR("Detected language: Meadow Mari"), //
        DETECTEDLANG_MI("Detected language: Maori"), //
        DETECTEDLANG_MK("Detected language: Macedonian"), //
        DETECTEDLANG_ML("Detected language: Malayalam"), //
        DETECTEDLANG_MN("Detected language: Mongolian"), //
        DETECTEDLANG_MR("Detected language: Marathi"), //
        DETECTEDLANG_MRJ("Detected language: Hill Mari"), //
        DETECTEDLANG_MS("Detected language: Malay"), //
        DETECTEDLANG_MT("Detected language: Maltese"), //
        DETECTEDLANG_MY("Detected language: Burmese"), //
        DETECTEDLANG_NAH("Detected language: Nahuatl"), //
        DETECTEDLANG_NE("Detected language: Nepali"), //
        DETECTEDLANG_NL("Detected language: Dutch"), //
        DETECTEDLANG_NO("Detected language: Norwegian"), //
        DETECTEDLANG_NY("Detected language: Nyanja"), //
        DETECTEDLANG_OC("Detected language: Occitan"), //
        DETECTEDLANG_OM("Detected language: Oromo"), //
        DETECTEDLANG_OR("Detected language: Oriya"), //
        DETECTEDLANG_OS("Detected language: Ossetian"), //
        DETECTEDLANG_PA("Detected language: Punjabi"), //
        DETECTEDLANG_PL("Detected language: Polish"), //
        DETECTEDLANG_PNB("Detected language: Western Panjabi"), //
        DETECTEDLANG_PS("Detected language: Pashto"), //
        DETECTEDLANG_PT("Detected language: Portuguese"), //
        DETECTEDLANG_RO("Detected language: Romanian"), //
        DETECTEDLANG_RU("Detected language: Russian"), //
        DETECTEDLANG_RW("Detected language: Kinyarwanda"), //
        DETECTEDLANG_SAH("Detected language: Sakha"), //
        DETECTEDLANG_SD("Detected language: Sindhi"), //
        DETECTEDLANG_SH("Detected language: Croatian, Serbian, or Bosnian"), //
        DETECTEDLANG_SI("Detected language: Sinhala"), //
        DETECTEDLANG_SK("Detected language: Slovak"), //
        DETECTEDLANG_SL("Detected language: Slovenian"), //
        DETECTEDLANG_SN("Detected language: Shona"), //
        DETECTEDLANG_SO("Detected language: Somali"), //
        DETECTEDLANG_SQ("Detected language: Albanian"), //
        DETECTEDLANG_SR_CYRL("Detected language: Serbian (Cyrillic)"), //
        DETECTEDLANG_SR_LATN("Detected language: Serbian (Latin)"), //
        DETECTEDLANG_ST("Detected language: Southern Sotho"), //
        DETECTEDLANG_SU("Detected language: Sundanese"), //
        DETECTEDLANG_SV("Detected language: Swedish"), //
        DETECTEDLANG_SW("Detected language: Swahili"), //
        DETECTEDLANG_TA("Detected language: Tamil"), //
        DETECTEDLANG_TE("Detected language: Telugu"), //
        DETECTEDLANG_TG("Detected language: Tajik"), //
        DETECTEDLANG_TH("Detected language: Thai"), //
        DETECTEDLANG_TI("Detected language: Tigrinya"), //
        DETECTEDLANG_TL("Detected language: Tagalog"), //
        DETECTEDLANG_TR("Detected language: Turkish"), //
        DETECTEDLANG_TT("Detected language: Tatar"), //
        DETECTEDLANG_UG("Detected language: Uyghur"), //
        DETECTEDLANG_UK("Detected language: Ukrainian"), //
        DETECTEDLANG_UR("Detected language: Urdu"), //
        DETECTEDLANG_UZ_CYRL("Detected language: Uzbek (Cyrillic)"), //
        DETECTEDLANG_UZ_LATN("Detected language: Uzbek (Latin)"), //
        DETECTEDLANG_VI("Detected language: Vietnamese"), //
        DETECTEDLANG_XH("Detected language: Xhosa"), //
        DETECTEDLANG_ZH_HANS("Detected language: Simplified Chinese"), //
        DETECTEDLANG_ZH_HANT("Detected language: Traditional Chinese"), //
        DETECTEDLANG_ZU("Detected language: Zulu"), //
        DETECTEDLANG_ZXX("Detected language: Lorem ipsum text"); //
        // @formatter:on

        Field(String description) {
            this.description = description;
        }

        private final String description;

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return description;
        }
    }

    public Field getFieldFromName(String name) {
        for (Field field : Field.class.getEnumConstants()) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        throw new IllegalArgumentException(
                "No statistics field with name " + name);
    }

    static {
        if ("1".equals(System.getProperty("nu.validator.servlet.statistics"))) {
            STATISTICS = new Statistics();
        } else {
            STATISTICS = null;
        }
    }

    private final long startTime = System.currentTimeMillis();

    private long total = 0;

    private final long[] counters;

    private Statistics() {
        counters = new long[Field.values().length];
    }

    public void incrementTotal() {
        total++;
    }

    public void incrementField(Field field) {
        counters[field.ordinal()]++;
    }

    public void writeToResponse(HttpServletResponse response)
            throws IOException {
        try {
            long totalCopy;
            long[] countersCopy = new long[counters.length];
            synchronized (this) {
                totalCopy = total;
                System.arraycopy(counters, 0, countersCopy, 0, counters.length);
            }
            double totalDouble = totalCopy;
            double uptimeMillis = System.currentTimeMillis() - startTime;
            response.setContentType("text/html; charset=utf-8");
            ContentHandler ch = new HtmlSerializer(response.getOutputStream());
            try {
                ch.startDocument();
                startElement(ch, "html");
                startElement(ch, "head");
                startElement(ch, "title");
                characters(ch, VALIDATOR_STATISTICS);
                endElement(ch, "title");
                startElement(ch, "style");
                characters(ch, STYLESHEET);
                endElement(ch, "style");
                endElement(ch, "head");
                startElement(ch, "body");
                startElement(ch, "h1");
                characters(ch, VALIDATOR_STATISTICS);
                endElement(ch, "h1");

                startElement(ch, "dl");
                startElement(ch, "dt");
                characters(ch, TOTAL_VALIDATIONS);
                endElement(ch, "dt");
                startElement(ch, "dd");
                characters(ch, totalCopy);
                endElement(ch, "dd");

                startElement(ch, "dt");
                characters(ch, UPTIME_DAYS);
                endElement(ch, "dt");
                startElement(ch, "dd");
                characters(ch, uptimeMillis / (1000 * 60 * 60 * 24));
                endElement(ch, "dd");

                startElement(ch, "dt");
                characters(ch, VALIDATIONS_PER_SECOND);
                endElement(ch, "dt");
                startElement(ch, "dd");
                characters(ch, totalDouble / (uptimeMillis / 1000.0));
                endElement(ch, "dd");

                endElement(ch, "dl");

                startElement(ch, "table");
                startElement(ch, "thead");
                startElement(ch, "tr");
                startElement(ch, "th");
                characters(ch, COUNTER_NAME);
                endElement(ch, "th");
                startElement(ch, "th");
                characters(ch, COUNTER_VALUE);
                endElement(ch, "th");
                startElement(ch, "th");
                characters(ch, COUNTER_PROPORTION);
                endElement(ch, "th");
                endElement(ch, "tr");
                endElement(ch, "thead");
                startElement(ch, "tbody");
                for (int i = 0; i < countersCopy.length; i++) {
                    long count = countersCopy[i];
                    startElement(ch, "tr");
                    startElement(ch, "td");

                    characters(ch, Field.values()[i].toString());

                    endElement(ch, "td");
                    startElement(ch, "td");

                    characters(ch, count);

                    endElement(ch, "td");
                    startElement(ch, "td");

                    characters(ch, count / totalDouble);

                    endElement(ch, "td");
                    endElement(ch, "tr");
                }
                endElement(ch, "tbody");
                endElement(ch, "table");
                startElement(ch, "script");
                characters(ch, SORT_LANGS_SCRIPT);
                endElement(ch, "script");
                endElement(ch, "body");
                endElement(ch, "html");
            } finally {
                ch.endDocument();
            }
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void characters(ContentHandler ch, double d) throws SAXException {
        // Let's just create a new DecimalFormat each time to avoid the
        // complexity of recycling an instance correctly without threading
        // hazards.
        DecimalFormat df = new DecimalFormat("#,###,##0.000000");
        characters(ch, df.format(d));
    }

    private void characters(ContentHandler ch, long l) throws SAXException {
        characters(ch, Long.toString(l));
    }

    private void characters(ContentHandler ch, String str) throws SAXException {
        characters(ch, str.toCharArray());
    }

    private void characters(ContentHandler ch, char[] cs) throws SAXException {
        ch.characters(cs, 0, cs.length);
    }

    private void endElement(ContentHandler ch, String name)
            throws SAXException {
        ch.endElement("http://www.w3.org/1999/xhtml", name, name);
    }

    private void startElement(ContentHandler ch, String name)
            throws SAXException {
        ch.startElement("http://www.w3.org/1999/xhtml", name, name,
                EmptyAttributes.EMPTY_ATTRIBUTES);
    }

}
