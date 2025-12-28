/*
 * Copyright (c) 2025 Mozilla Foundation
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

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Checks that the text content of a type=speculationrules script element
 * conforms to the “speculation rule set” requirements in the HTML spec:
 * https://whatwg.org/html/#speculation-rule-set
 */
public final class SpeculationRulesChecker extends Checker {

    private static final String HTML_NS = "http://www.w3.org/1999/xhtml";
    private boolean parsingSpeculationRules = false;
    private StringBuilder scriptContent = null;

    public SpeculationRulesChecker() {
        super();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (HTML_NS.equals(uri) && "script".equals(localName)) {
            if (atts.getIndex("", "type") > -1) {
                String scriptType = atts.getValue("", "type").toLowerCase();
                if ("speculationrules".equals(scriptType)) {
                    if (atts.getIndex("", "src") > -1) {
                        err("A \u201cscript\u201d element with a"
                                + " \u201ctype\u201d attribute whose value"
                                + " is \u201cspeculationrules\u201d must not"
                                + " have a \u201Csrc\u201D attribute.");
                    }
                    parsingSpeculationRules = true;
                    scriptContent = new StringBuilder();
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (parsingSpeculationRules && scriptContent != null) {
            scriptContent.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (HTML_NS.equals(uri) && "script".equals(localName)
                && parsingSpeculationRules) {
            isSpeculationRulesValid(scriptContent.toString());
            parsingSpeculationRules = false;
            scriptContent = null;
        }
    }

    private boolean isSpeculationRulesValid(String content)
            throws SAXException {
        JsonStructure speculationRules;
        try {
            JsonReader reader = Json.createReader(new StringReader(content));
            speculationRules = reader.read();
        } catch (JsonException e) {
            err("A \u201cscript\u201d element with a \u201ctype\u201d attribute"
                    + " whose value is \u201cspeculationrules\u201d must have"
                    + " valid JSON content.");
            return false;
        }
        if (!(speculationRules instanceof JsonObject)) {
            err("A \u201cscript\u201d element with a \u201ctype\u201d attribute"
                    + " whose value is \u201cspeculationrules\u201d must contain"
                    + " a JSON object.");
            return false;
        }
        JsonObject speculationRulesObject = (JsonObject) speculationRules;
        if (!speculationRulesObject.containsKey("prefetch")
                && !speculationRulesObject.containsKey("prerender")) {
            err("A \u201cscript\u201d element with a \u201ctype\u201d attribute"
                    + " whose value is \u201cspeculationrules\u201d must contain"
                    + " a JSON object with at least one of the properties"
                    + " \u201cprefetch\u201d or \u201cprerender\u201d.");
            return false;
        }
        for (String key : speculationRulesObject.keySet()) {
            if (!"prefetch".equals(key) && !"prerender".equals(key)) {
                err("A \u201cscript\u201d element with a \u201ctype\u201d attribute"
                        + " whose value is \u201cspeculationrules\u201d must contain"
                        + " a JSON object with only \u201cprefetch\u201d and/or"
                        + " \u201cprerender\u201d as properties.");
                return false;
            }
        }
        if (speculationRulesObject.containsKey("prefetch")) {
            if (!isSpeculationRuleArrayValid("prefetch",
                    speculationRulesObject.get("prefetch"))) {
                return false;
            }
        }
        if (speculationRulesObject.containsKey("prerender")) {
            if (!isSpeculationRuleArrayValid("prerender",
                    speculationRulesObject.get("prerender"))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpeculationRuleArrayValid(String ruleType,
            JsonValue ruleValue) throws SAXException {
        if (!(ruleValue instanceof JsonArray)) {
            err("The \u201c" + ruleType + "\u201d property within the content"
                    + " of a \u201cscript\u201d element with a \u201ctype\u201d"
                    + " attribute whose value is \u201cspeculationrules\u201d"
                    + " must be a JSON array.");
            return false;
        }

        JsonArray rulesArray = (JsonArray) ruleValue;
        for (JsonValue rule : rulesArray) {
            if (!isSpeculationRuleObjectValid(ruleType, rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpeculationRuleObjectValid(String ruleType,
            JsonValue ruleValue) throws SAXException {
        if (!(ruleValue instanceof JsonObject)) {
            err("Each item in the \u201c" + ruleType + "\u201d array within"
                    + " the content of a \u201cscript\u201d element with a"
                    + " \u201ctype\u201d attribute whose value is"
                    + " \u201cspeculationrules\u201d must be a JSON object.");
            return false;
        }
        JsonObject ruleObject = (JsonObject) ruleValue;
        if (!ruleObject.containsKey("source")) {
            err("Each rule in the \u201c" + ruleType + "\u201d array within"
                    + " the content of a \u201cscript\u201d element with a"
                    + " \u201ctype\u201d attribute whose value is"
                    + " \u201cspeculationrules\u201d must have a"
                    + " \u201csource\u201d property.");
            return false;
        }
        JsonValue sourceValue = ruleObject.get("source");
        if (!(sourceValue instanceof JsonString)) {
            err("The \u201csource\u201d property in a speculation rule must"
                    + " be a string.");
            return false;
        }
        String source = ((JsonString) sourceValue).getString();
        if (!"list".equals(source) && !"document".equals(source)) {
            err("The \u201csource\u201d property in a speculation rule must"
                    + " be either \u201clist\u201d or \u201cdocument\u201d.");
            return false;
        }
        for (String key : ruleObject.keySet()) {
            if (!"source".equals(key) && !"urls".equals(key)
                    && !"where".equals(key) && !"eagerness".equals(key)) {
                err("Each rule in the \u201c" + ruleType + "\u201d array must"
                        + " only contain the properties \u201csource\u201d,"
                        + " \u201curls\u201d, \u201cwhere\u201d, and"
                        + " \u201ceagerness\u201d.");
                return false;
            }
        }
        if ("list".equals(source)) {
            if (!ruleObject.containsKey("urls")) {
                err("A speculation rule with \u201csource\u201d set to"
                        + " \u201clist\u201d must have a \u201curls\u201d"
                        + " property.");
                return false;
            }
            if (ruleObject.containsKey("where")) {
                err("A speculation rule with \u201csource\u201d set to"
                        + " \u201clist\u201d must not have a \u201cwhere\u201d"
                        + " property.");
                return false;
            }
            if (!isUrlsArrayValid(ruleObject.get("urls"))) {
                return false;
            }
        } else if ("document".equals(source)) {
            if (!ruleObject.containsKey("where")) {
                err("A speculation rule with \u201csource\u201d set to"
                        + " \u201cdocument\u201d must have a \u201cwhere\u201d"
                        + " property.");
                return false;
            }
            if (ruleObject.containsKey("urls")) {
                err("A speculation rule with \u201csource\u201d set to"
                        + " \u201cdocument\u201d must not have a \u201curls\u201d"
                        + " property.");
                return false;
            }
            if (!isDocumentRuleValid(ruleObject.get("where"))) {
                return false;
            }
        }
        if (ruleObject.containsKey("eagerness")) {
            JsonValue eagernessValue = ruleObject.get("eagerness");
            if (!(eagernessValue instanceof JsonString)) {
                err("The \u201ceagerness\u201d property in a speculation rule"
                        + " must be a string.");
                return false;
            }
            String eagerness = ((JsonString) eagernessValue).getString();
            if (!"eager".equals(eagerness) && !"moderate".equals(eagerness)
                    && !"conservative".equals(eagerness)) {
                err("The \u201ceagerness\u201d property in a speculation rule"
                        + " must be one of \u201ceager\u201d,"
                        + " \u201cmoderate\u201d, or \u201cconservative\u201d.");
                return false;
            }
        }
        return true;
    }

    private boolean isUrlsArrayValid(JsonValue urlsValue) throws SAXException {
        if (!(urlsValue instanceof JsonArray)) {
            err("The \u201curls\u201d property in a speculation rule must be a"
                    + " JSON array.");
            return false;
        }
        JsonArray urlsArray = (JsonArray) urlsValue;
        if (urlsArray.isEmpty()) {
            err("The \u201curls\u201d property in a speculation rule must"
                    + " contain at least one URL.");
            return false;
        }
        for (JsonValue urlValue : urlsArray) {
            if (!(urlValue instanceof JsonString)) {
                err("Each item in the \u201curls\u201d array must be a string.");
                return false;
            }
            String url = ((JsonString) urlValue).getString();
            if (url.isEmpty()) {
                err("Each URL in the \u201curls\u201d array must be a non-empty"
                        + " string.");
                return false;
            }
        }
        return true;
    }

    private boolean isDocumentRuleValid(JsonValue whereValue)
            throws SAXException {
        if (!(whereValue instanceof JsonObject)) {
            err("The \u201cwhere\u201d property in a speculation rule must be a"
                    + " JSON object.");
            return false;
        }
        JsonObject whereObject = (JsonObject) whereValue;
        int predicateCount = 0;
        if (whereObject.containsKey("and"))
            predicateCount++;
        if (whereObject.containsKey("or"))
            predicateCount++;
        if (whereObject.containsKey("not"))
            predicateCount++;
        if (whereObject.containsKey("href_matches"))
            predicateCount++;
        if (whereObject.containsKey("selector_matches"))
            predicateCount++;
        if (predicateCount == 0) {
            err("A document rule predicate must have one of the properties"
                    + " \u201cand\u201d, \u201cor\u201d, \u201cnot\u201d,"
                    + " \u201chref_matches\u201d, or"
                    + " \u201cselector_matches\u201d.");
            return false;
        }
        if (predicateCount > 1) {
            err("A document rule predicate must have only one of the properties"
                    + " \u201cand\u201d, \u201cor\u201d, \u201cnot\u201d,"
                    + " \u201chref_matches\u201d, or"
                    + " \u201cselector_matches\u201d.");
            return false;
        }
        for (String key : whereObject.keySet()) {
            if (!"and".equals(key) && !"or".equals(key) && !"not".equals(key)
                    && !"href_matches".equals(key)
                    && !"selector_matches".equals(key)) {
                err("A document rule predicate must only contain one of the"
                        + " properties \u201cand\u201d, \u201cor\u201d,"
                        + " \u201cnot\u201d, \u201chref_matches\u201d, or"
                        + " \u201cselector_matches\u201d.");
                return false;
            }
        }
        if (whereObject.containsKey("and")) {
            return isAndOrRuleValid("and", whereObject.get("and"));
        } else if (whereObject.containsKey("or")) {
            return isAndOrRuleValid("or", whereObject.get("or"));
        } else if (whereObject.containsKey("not")) {
            return isNotRuleValid(whereObject.get("not"));
        } else if (whereObject.containsKey("href_matches")) {
            return isHrefMatchesRuleValid(whereObject.get("href_matches"));
        } else if (whereObject.containsKey("selector_matches")) {
            return isSelectorMatchesRuleValid(
                    whereObject.get("selector_matches"));
        }
        return true;
    }

    private boolean isAndOrRuleValid(String ruleType, JsonValue ruleValue)
            throws SAXException {
        if (!(ruleValue instanceof JsonArray)) {
            err("The \u201c" + ruleType + "\u201d property in a document rule"
                    + " must be a JSON array.");
            return false;
        }
        JsonArray rulesArray = (JsonArray) ruleValue;
        if (rulesArray.isEmpty()) {
            err("The \u201c" + ruleType + "\u201d property in a document rule"
                    + " must contain at least one item.");
            return false;
        }
        for (JsonValue rule : rulesArray) {
            if (!isDocumentRuleValid(rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNotRuleValid(JsonValue ruleValue) throws SAXException {
        return isDocumentRuleValid(ruleValue);
    }

    private boolean isHrefMatchesRuleValid(JsonValue ruleValue)
            throws SAXException {
        if (!(ruleValue instanceof JsonString)) {
            err("The \u201chref_matches\u201d property in a document rule must"
                    + " be a string.");
            return false;
        }
        String pattern = ((JsonString) ruleValue).getString();
        if (pattern.isEmpty()) {
            err("The \u201chref_matches\u201d property in a document rule must"
                    + " be a non-empty string.");
            return false;
        }
        return true;
    }

    private boolean isSelectorMatchesRuleValid(JsonValue ruleValue)
            throws SAXException {
        if (!(ruleValue instanceof JsonString)) {
            err("The \u201cselector_matches\u201d property in a document rule"
                    + " must be a string.");
            return false;
        }
        String selector = ((JsonString) ruleValue).getString();
        if (selector.isEmpty()) {
            err("The \u201cselector_matches\u201d property in a document rule"
                    + " must be a non-empty string.");
            return false;
        }
        return true;
    }
}
