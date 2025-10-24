/*
 * Copyright (c) 2007 Henri Sivonen
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

package nu.validator.htmlparser.test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.impl.ErrorReportingTokenizer;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.io.Driver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONString;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

public class TokenizerTester {

    static int exitStatus = 0;

    private static JSONString PLAINTEXT = new JSONString("PLAINTEXT state");

    private static JSONString PCDATA = new JSONString("Data state");

    private static JSONString RCDATA = new JSONString("RCDATA state");

    private static JSONString CDATA = new JSONString("CDATA section state");

    private static JSONString RAWTEXT = new JSONString("RAWTEXT state");

    private static JSONString SCRIPT_DATA = new JSONString("Script data state");

    private static boolean jsonDeepEquals(JSONValue one, JSONValue other) {
        if (one.isSimple()) {
            return one.equals(other);
        } else if (one.isArray()) {
            if (other.isArray()) {
                JSONArray oneArr = (JSONArray) one;
                JSONArray otherArr = (JSONArray) other;
                return oneArr.getValue().equals(otherArr.getValue());
            } else {
                return false;
            }
        } else if (one.isObject()) {
            if (other.isObject()) {
                JSONObject oneObject = (JSONObject) one;
                JSONObject otherObject = (JSONObject) other;
                return oneObject.getValue().equals(otherObject.getValue());
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("Should never happen.");
        }
    }

    private JSONArray tests;

    private final JSONArrayTokenHandler tokenHandler;

    private final Driver driver;

    private final Writer writer;

    public TokenizerTester(InputStream stream) throws TokenStreamException,
            RecognitionException, UnsupportedEncodingException {
        tokenHandler = new JSONArrayTokenHandler();
        driver = new Driver(new ErrorReportingTokenizer(tokenHandler));
        driver.setCommentPolicy(XmlViolationPolicy.ALLOW);
        driver.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
        driver.setContentSpacePolicy(XmlViolationPolicy.ALLOW);
        driver.setNamePolicy(XmlViolationPolicy.ALLOW);
        driver.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
        driver.setErrorHandler(tokenHandler);
        driver.dontSwallowBom();
        writer = new OutputStreamWriter(System.out, "UTF-8");
        JSONParser jsonParser = new JSONParser(new InputStreamReader(stream,
                "UTF-8"));
        JSONObject obj = (JSONObject) jsonParser.nextValue();
        tests = (JSONArray) obj.get("tests");
        if (tests == null) {
            tests = (JSONArray) obj.get("xmlViolationTests");
            driver.setCommentPolicy(XmlViolationPolicy.ALTER_INFOSET);
            driver.setContentNonXmlCharPolicy(XmlViolationPolicy.ALTER_INFOSET);
            driver.setNamePolicy(XmlViolationPolicy.ALTER_INFOSET);
            driver.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
        }
    }

    void runTests() throws SAXException, IOException {
        for (JSONValue val : tests.getValue()) {
            runTest((JSONObject) val);
        }
        writer.flush();
    }

    private void runTest(JSONObject test) throws SAXException, IOException {
        String inputString = ((JSONString) test.get("input")).getValue();
        JSONArray expectedTokens = (JSONArray) test.get("output");
        String description = ((JSONString) test.get("description")).getValue();
        JSONString lastStartTagJSON = ((JSONString) test.get("lastStartTag"));
        String lastStartTag = lastStartTagJSON == null ? null
                : lastStartTagJSON.getValue();
        JSONArray contentModelFlags = (JSONArray) test.get("initialStates");
        if (contentModelFlags == null) {
            runTestInner(inputString, expectedTokens, description,
                    Tokenizer.DATA, null);
        } else {
            for (JSONValue value : contentModelFlags.getValue()) {
                if (PCDATA.equals(value)) {
                    lastStartTag = lastStartTag == null ? "xmp" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.DATA, lastStartTag);
                } else if (RAWTEXT.equals(value)) {
                    lastStartTag = lastStartTag == null ? "xmp" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.RAWTEXT, lastStartTag);
                } else if (RCDATA.equals(value)) {
                    lastStartTag = lastStartTag == null ? "xmp" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.RCDATA, lastStartTag);
                } else if (CDATA.equals(value)) {
                    lastStartTag = lastStartTag == null ? "xmp" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.CDATA_SECTION, lastStartTag);
                } else if (PLAINTEXT.equals(value)) {
                    lastStartTag = lastStartTag == null ? "plaintext" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.PLAINTEXT, lastStartTag);
                } else if (SCRIPT_DATA.equals(value)) {
                    lastStartTag = lastStartTag == null ? "script" : lastStartTag;
                    runTestInner(inputString, expectedTokens, description,
                            Tokenizer.SCRIPT_DATA, lastStartTag);
                } else {
                    throw new RuntimeException("Broken test data.");
                }
            }
        }
    }

    /**
     * @param contentModelElement
     * @param contentModelFlag
     * @param test
     * @throws SAXException
     * @throws IOException
     */
    private void runTestInner(String inputString, JSONArray expectedTokens,
            String description, int contentModelFlag,
            String contentModelElement) throws SAXException, IOException {
        tokenHandler.setContentModelFlag(contentModelFlag, contentModelElement);
        InputSource is = new InputSource(new StringReader(inputString));
        try {
            driver.tokenize(is);
            JSONArray actualTokens = tokenHandler.getArray();
            if (!jsonDeepEquals(actualTokens, expectedTokens)) {
                exitStatus = 1;
                writer.write("Failure\n");
                writer.write(description);
                writer.write("\nInput:\n");
                writer.write(inputString);
                writer.write("\nExpected tokens:\n");
                writer.write(expectedTokens.render(false));
                writer.write("\nActual tokens:\n");
                writer.write(actualTokens.render(false));
                writer.write("\n");
            }
        } catch (Throwable t) {
            exitStatus = 1;
            writer.write("Failure\n");
            writer.write(description);
            writer.write("\nInput:\n");
            writer.write(inputString);
            writer.write("\n");
            t.printStackTrace(new PrintWriter(writer, false));
        }
    }

    /**
     * @param args
     * @throws RecognitionException
     * @throws TokenStreamException
     * @throws IOException
     * @throws SAXException
     */
    public static void main(String[] args) throws TokenStreamException,
            RecognitionException, SAXException, IOException {
        for (int i = 0; i < args.length; i++) {
            byte[] fileBytes = Files.readAllBytes(Paths.get(args[i]));
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            String unescapedContent = fileContent.replace("\\\\u", "\\u");
            byte[] newBytes = unescapedContent.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream bais = new ByteArrayInputStream(newBytes);
            TokenizerTester tester = new TokenizerTester(bais);
            tester.runTests();
        }
        System.exit(exitStatus);
    }

}
