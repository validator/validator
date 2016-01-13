/*
 * Copyright (c) 2012 Mozilla Foundation
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
                "Text output"), INPUT_HTML("HTML input"), INPUT_XML("XML input");

        Field(String description) {
            this.description = description;
        }

        private final String description;

        /**
         * @see java.lang.Enum#toString()
         */
        @Override public String toString() {
            return description;
        }
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

    private void endElement(ContentHandler ch, String name) throws SAXException {
        ch.endElement("http://www.w3.org/1999/xhtml", name, name);
    }

    private void startElement(ContentHandler ch, String name)
            throws SAXException {
        ch.startElement("http://www.w3.org/1999/xhtml", name, name,
                EmptyAttributes.EMPTY_ATTRIBUTES);
    }

}
