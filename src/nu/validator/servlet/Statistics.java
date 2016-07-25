/*
 * Copyright (c) 2012-2016 Mozilla Foundation
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
            + " var langRows = new Array();"
            + " var rows = document.querySelectorAll('tr');"
            + " for (var i=0; i < rows.length; i++) { var row = rows[i];"
            + "   if (row.textContent.indexOf('Detected language') > -1) {"
            + "       var sortnr = parseInt(row.cells[1].textContent"
            + "         || row.cells[0].innerText);"
            + "       if (!isNaN(sortnr)) langRows.push([sortnr, row]);"
            + "   }"
            + " } langRows.sort(function(x,y) { return x[0] - y[0]; });"
            + " langRows.reverse();"
            + " for (var i=0; i<langRows.length; i++) {"
            + "   document.querySelector('tbody').appendChild(langRows[i][1]);"
            + " }").toCharArray();

    public enum Field {
        // Sigh. Eclipse's formatting of the following code is sad.
        CUSTOM_ENC("Manually set character encoding"), AUTO_SCHEMA(
                "Automatically chosen schema"), PRESET_SCHEMA("Preset schema"), BUILT_IN_NON_PRESET(
                "Custom schema combined from built-ins"), HTML5_SCHEMA(
                "(X)HTML5 schema"), HTML5_RDFA_LITE_SCHEMA(
                "(X)HTML5+RDFa Lite schema"), HTML4_STRICT_SCHEMA(
                "Legacy Strict schema"), HTML4_TRANSITIONAL_SCHEMA(
                "Legacy Transitional schema"), HTML4_FRAMESET_SCHEMA(
                "Legacy Frameset schema"), XHTML1_COMPOUND_SCHEMA(
                "Legacy XHTML+SVG+MathML schema"), SVG_SCHEMA("SVG schema"), EXTERNAL_SCHEMA_NON_SCHEMATRON(
                "non-Schematron custom schema"), EXTERNAL_SCHEMA_SCHEMATRON(
                "Schematron custom schema"), LOGIC_ERROR(
                "Logic errors in schema stats"), PARSER_XML_EXTERNAL(
                "Parser set to XML with external entities"), PARSER_HTML4(
                "Parser set to explicit HTML4 mode"), XMLNS_FILTER(
                "XMLNS filter set"), LAX_TYPE(
                "Being lax about HTTP content type"), IMAGE_REPORT(
                "Image report"), SHOW_SOURCE("Show source"), SHOW_OUTLINE(
                "Show outline"), INPUT_GET("GET-based input"), INPUT_POST(
                "POST-based input"), INPUT_TEXT_FIELD("\u2514 Text-field input"), INPUT_FILE_UPLOAD(
                "\u2514 File-upload input"), INPUT_ENTITY_BODY(
                "\u2514 Entity-body input"), OUTPUT_HTML("HTML output"), OUTPUT_XHTML(
                "XHTML output"), OUTPUT_XML("XML output"), OUTPUT_JSON(
                "JSON output"), OUTPUT_GNU("GNU output"), OUTPUT_TEXT(
                "Text output"), INPUT_HTML("HTML input"), INPUT_XML("XML input"),
                LANG_AF("Detected language: Afrikaans"),
                LANG_AM("Detected language: Amharic"),
                LANG_AR("Detected language: Arabic"),
                LANG_ARZ("Detected language: Egyptian Arabic"),
                LANG_AZ("Detected language: Azerbaijani"),
                LANG_BA("Detected language: Bashkir"),
                LANG_BE("Detected language: Belarusian"),
                LANG_BG("Detected language: Bulgarian"),
                LANG_BN("Detected language: Bengali"),
                LANG_BR("Detected language: Breton"),
                LANG_CA("Detected language: Catalan"),
                LANG_CE("Detected language: Chechen"),
                LANG_CEB("Detected language: Cebuano"),
                LANG_CHR("Detected language: Cherokee"),
                LANG_CKB("Detected language: Sorani Kurdish"),
                LANG_CO("Detected language: Corsican"),
                LANG_CR("Detected language: Cree"),
                LANG_CS("Detected language: Czech"),
                LANG_CV("Detected language: Chuvash"),
                LANG_CY("Detected language: Welsh"),
                LANG_DA("Detected language: Danish"),
                LANG_DE("Detected language: German"),
                LANG_EL("Detected language: Greek"),
                LANG_EN("Detected language: English"),
                LANG_EO("Detected language: Esperanto"),
                LANG_ES("Detected language: Spanish"),
                LANG_ET("Detected language: Estonian"),
                LANG_EU("Detected language: Basque"),
                LANG_FA("Detected language: Persian"),
                LANG_FI("Detected language: Finnish"),
                LANG_FO("Detected language: Faroese"),
                LANG_FR("Detected language: French"),
                LANG_FY("Detected language: Western Frisian"),
                LANG_GA("Detected language: Irish"),
                LANG_GD("Detected language: Scottish Gaelic"),
                LANG_GU("Detected language: Gujarati"),
                LANG_HE("Detected language: Hebrew"),
                LANG_HI("Detected language: Hindi"),
                LANG_HT("Detected language: Haitian"),
                LANG_HU("Detected language: Hungarian"),
                LANG_HY("Detected language: Armenian"),
                LANG_ID("Detected language: Indonesian"),
                LANG_IG("Detected language: Igbo"),
                LANG_ILO("Detected language: Iloko"),
                LANG_IS("Detected language: Icelandic"),
                LANG_IT("Detected language: Italian"),
                LANG_IU("Detected language: Inuktitut"),
                LANG_JA("Detected language: Japanese"),
                LANG_JV("Detected language: Javanese"),
                LANG_KA("Detected language: Georgian"),
                LANG_KM("Detected language: Khmer"),
                LANG_KK("Detected language: Kazakh"),
                LANG_KN("Detected language: Kannada"),
                LANG_KO("Detected language: Korean"),
                LANG_KU("Detected language: Kurdish"),
                LANG_KY("Detected language: Kyrgyz"),
                LANG_LMO("Detected language: Lombard"),
                LANG_LO("Detected language: Lao"),
                LANG_LT("Detected language: Lithuanian"),
                LANG_LV("Detected language: Latvian"),
                LANG_MG("Detected language: Malagasy"),
                LANG_MHR("Detected language: Meadow Mari"),
                LANG_MI("Detected language: Maori"),
                LANG_MIN("Detected language: Minangkabau"),
                LANG_MK("Detected language: Macedonian"),
                LANG_ML("Detected language: Malayalam"),
                LANG_MN("Detected language: Mongolian"),
                LANG_MR("Detected language: Marathi"),
                LANG_MRJ("Detected language: Hill Mari"),
                LANG_MS("Detected language: Malay"),
                LANG_MT("Detected language: Maltese"),
                LANG_MY("Detected language: Burmese"),
                LANG_NAH("Detected language: Nahuatl"),
                LANG_NE("Detected language: Nepali"),
                LANG_NL("Detected language: Dutch"),
                LANG_NN("Detected language: Norwegian Nynorsk"),
                LANG_NO("Detected language: Norwegian"),
                LANG_NY("Detected language: Nyanja"),
                LANG_OC("Detected language: Occitan"),
                LANG_OR("Detected language: Oriya"),
                LANG_OS("Detected language: Ossetian"),
                LANG_PA("Detected language: Punjabi"),
                LANG_PAG("Detected language: Pangasinan"),
                LANG_PL("Detected language: Polish"),
                LANG_PMS("Detected language: Piedmontese"),
                LANG_PNB("Detected language: Western Panjabi"),
                LANG_PS("Detected language: Pashto"),
                LANG_PT("Detected language: Portuguese"),
                LANG_QU("Detected language: Quechua"),
                LANG_RO("Detected language: Romanian"),
                LANG_RU("Detected language: Russian"),
                LANG_SAH("Detected language: Sakha"),
                LANG_SCN("Detected language: Sicilian"),
                LANG_SD("Detected language: Sindhi"),
                LANG_SH("Detected language: Serbo-Croatian"),
                LANG_SI("Detected language: Sinhala"),
                LANG_SK("Detected language: Slovak"),
                LANG_SL("Detected language: Slovenian"),
                LANG_SM("Detected language: Samoan"),
                LANG_SN("Detected language: Shona"),
                LANG_SQ("Detected language: Albanian"),
                LANG_SR("Detected language: Serbian"),
                LANG_ST("Detected language: Southern Sotho"),
                LANG_SU("Detected language: Sundanese"),
                LANG_SV("Detected language: Swedish"),
                LANG_SW("Detected language: Swahili"),
                LANG_TA("Detected language: Tamil"),
                LANG_TE("Detected language: Telugu"),
                LANG_TG("Detected language: Tajik"),
                LANG_TH("Detected language: Thai"),
                LANG_TL("Detected language: Tagalog"),
                LANG_TR("Detected language: Turkish"),
                LANG_TT("Detected language: Tatar"),
                LANG_UG("Detected language: Uyghur"),
                LANG_UK("Detected language: Ukrainian"),
                LANG_UR("Detected language: Urdu"),
                LANG_UZ("Detected language: Uzbek"),
                LANG_VEC("Detected language: Venetian"),
                LANG_VI("Detected language: Vietnamese"),
                LANG_WAR("Detected language: Waray"),
                LANG_XH("Detected language: Xhosa"),
                LANG_YI("Detected language: Yiddish"),
                LANG_ZH_HANS("Detected language: Simplied Chinese"),
                LANG_ZH_HANT("Detected language: Traditional Chinese"),
                LANG_ZU("Detected language: Zulu");

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
        throw new RuntimeException("No statistics field with name " + name);
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
