/*
 * Copyright (c) 2008-2020 Mozilla Foundation
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

package nu.validator.checker.schematronequiv;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import nu.validator.checker.AttributeUtil;
import nu.validator.checker.Checker;
import nu.validator.checker.LocatorImpl;
import nu.validator.checker.TaintableLocatorImpl;
import nu.validator.checker.VnuBadAttrValueException;
import nu.validator.checker.VnuBadElementNameException;
import nu.validator.client.TestRunner;
import nu.validator.datatype.AutocompleteDetailsAny;
import nu.validator.datatype.AutocompleteDetailsDate;
import nu.validator.datatype.AutocompleteDetailsEmail;
import nu.validator.datatype.AutocompleteDetailsMonth;
import nu.validator.datatype.AutocompleteDetailsNumeric;
import nu.validator.datatype.AutocompleteDetailsPassword;
import nu.validator.datatype.AutocompleteDetailsTel;
import nu.validator.datatype.AutocompleteDetailsText;
import nu.validator.datatype.AutocompleteDetailsUrl;
import nu.validator.datatype.Color;
import nu.validator.datatype.CustomElementName;
import nu.validator.datatype.Html5DatatypeException;
import nu.validator.datatype.ImageCandidateStringsWidthRequired;
import nu.validator.datatype.ImageCandidateStrings;
import nu.validator.datatype.ImageCandidateURL;
import nu.validator.htmlparser.impl.NCName;
import nu.validator.messages.MessageEmitterAdapter;

import org.eclipse.jetty.util.ajax.JSON;
import org.relaxng.datatype.DatatypeException;

import org.w3c.css.css.StyleSheetParser;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.CssParseException;
import org.w3c.css.parser.Errors;
import org.w3c.css.util.ApplContext;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.log4j.Logger;

public class Assertions extends Checker {

    private static final Logger log4j = //
            Logger.getLogger(Assertions.class);

    private static boolean equalsIgnoreAsciiCase(String one, String other) {
        if (other == null) {
            return one == null;
        }
        if (one.length() != other.length()) {
            return false;
        }
        for (int i = 0; i < one.length(); i++) {
            char c0 = one.charAt(i);
            char c1 = other.charAt(i);
            if (c0 >= 'A' && c0 <= 'Z') {
                c0 += 0x20;
            }
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    private static final String trimSpaces(String str) {
        return trimLeadingSpaces(trimTrailingSpaces(str));
    }

    private static final String trimLeadingSpaces(String str) {
        if (str == null) {
            return null;
        }
        for (int i = str.length(); i > 0; --i) {
            char c = str.charAt(str.length() - i);
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c
                    || '\r' == c)) {
                return str.substring(str.length() - i, str.length());
            }
        }
        return "";
    }

    private static final String trimTrailingSpaces(String str) {
        if (str == null) {
            return null;
        }
        for (int i = str.length() - 1; i >= 0; --i) {
            char c = str.charAt(i);
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c
                    || '\r' == c)) {
                return str.substring(0, i + 1);
            }
        }
        return "";
    }

    private static final Map<String, String[]> INPUT_ATTRIBUTES = new HashMap<>();

    static {
        INPUT_ATTRIBUTES.put("autocomplete",
                new String[] { "hidden", "text", "search", "url", "tel", "email",
                        "password", "date", "month", "week", "time",
                        "datetime-local", "number", "range", "color" });
        INPUT_ATTRIBUTES.put("list",
                new String[] { "text", "search", "url", "tel", "email",
                        "date", "month", "week", "time",
                        "datetime-local", "number", "range", "color" });
        INPUT_ATTRIBUTES.put("maxlength", new String[] { "text", "search",
                "url", "tel", "email", "password" });
        INPUT_ATTRIBUTES.put("minlength", new String[] { "text", "search",
                "url", "tel", "email", "password" });
        INPUT_ATTRIBUTES.put("pattern", new String[] { "text", "search", "url",
                "tel", "email", "password" });
        INPUT_ATTRIBUTES.put("placeholder", new String[] { "text", "search",
                "url", "tel", "email", "password", "number" });
        INPUT_ATTRIBUTES.put("readonly",
                new String[] { "text", "search", "url", "tel", "email",
                        "password", "date", "month", "week", "time",
                        "datetime-local", "number" });
        INPUT_ATTRIBUTES.put("required",
                new String[] { "text", "search", "url", "tel", "email",
                        "password", "date", "month", "week", "time",
                        "datetime-local", "number", "checkbox", "radio",
                        "file" });
        INPUT_ATTRIBUTES.put("size", new String[] { "text", "search", "url",
                "tel", "email", "password" });

        for (String[] allowedTypes: INPUT_ATTRIBUTES.values()) {
            Arrays.sort(allowedTypes);
        }
    }

    private static final Map<String, String> OBSOLETE_ELEMENTS = new HashMap<>();

    static {
        OBSOLETE_ELEMENTS.put("keygen", "");
        OBSOLETE_ELEMENTS.put("center", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("font", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("big", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("strike", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("tt", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("acronym",
                "Use the \u201Cabbr\u201D element instead.");
        OBSOLETE_ELEMENTS.put("dir", "Use the \u201Cul\u201D element instead.");
        OBSOLETE_ELEMENTS.put("applet",
                "Use the \u201Cobject\u201D element instead.");
        OBSOLETE_ELEMENTS.put("basefont", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("frameset",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put("noframes",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
    }

    private static final Map<String, String[]> OBSOLETE_ATTRIBUTES = new HashMap<>();

    static {
        OBSOLETE_ATTRIBUTES.put("abbr", new String[] { "td" });
        OBSOLETE_ATTRIBUTES.put("archive", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("axis", new String[] { "td", "th" });
        OBSOLETE_ATTRIBUTES.put("charset", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("classid", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("code", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codebase", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codetype", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("coords", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("datafld", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("dataformatas", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datasrc", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datapagesize", new String[] { "table" });
        OBSOLETE_ATTRIBUTES.put("declare", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("event", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("for", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("language", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("longdesc", new String[] { "img", "iframe" });
        OBSOLETE_ATTRIBUTES.put("methods", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("name",
                new String[] { "img", "embed", "option" });
        OBSOLETE_ATTRIBUTES.put("nohref", new String[] { "area" });
        OBSOLETE_ATTRIBUTES.put("profile", new String[] { "head" });
        OBSOLETE_ATTRIBUTES.put("scheme", new String[] { "meta" });
        OBSOLETE_ATTRIBUTES.put("scope", new String[] { "td" });
        OBSOLETE_ATTRIBUTES.put("shape", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("standby", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("target", new String[] { "link" });
        OBSOLETE_ATTRIBUTES.put("type", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("urn", new String[] { "a", "link" });
        OBSOLETE_ATTRIBUTES.put("usemap", new String[] { "input" });
        OBSOLETE_ATTRIBUTES.put("valuetype", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("version", new String[] { "html" });
        OBSOLETE_ATTRIBUTES.put("manifest", new String[] { "html" });

        for (String[] elementNames: OBSOLETE_ATTRIBUTES.values()) {
            Arrays.sort(elementNames);
        }
    }

    private static final Map<String, String> OBSOLETE_ATTRIBUTES_MSG = new HashMap<>();

    static {
        OBSOLETE_ATTRIBUTES_MSG.put("abbr",
                "Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.");
        OBSOLETE_ATTRIBUTES_MSG.put("archive",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Carchive\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("axis",
                "Use the \u201Cscope\u201D attribute.");
        OBSOLETE_ATTRIBUTES_MSG.put("charset",
                "Use an HTTP Content-Type header on the linked resource instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("classid",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Cclassid\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("code",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccode\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("codebase",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodebase\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("codetype",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodetype\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("coords",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("datapagesize", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("datafld",
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("dataformatas",
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("datasrc",
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("for",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put("event",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put("declare",
                "Repeat the \u201Cobject\u201D element completely each time the resource is to be reused.");
        OBSOLETE_ATTRIBUTES_MSG.put("language",
                "Use the \u201Ctype\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("longdesc",
                "Use a regular \u201Ca\u201D element to link to the description.");
        OBSOLETE_ATTRIBUTES_MSG.put("methods",
                "Use the HTTP OPTIONS feature instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("name",
                "Use the \u201Cid\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("nohref",
                "Omitting the \u201Chref\u201D attribute is sufficient.");
        OBSOLETE_ATTRIBUTES_MSG.put("profile",
                "To declare which \u201Cmeta\u201D terms are used in the document, instead register the names as meta extensions. To trigger specific UA behaviors, use a \u201Clink\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("scheme",
                "Use only one scheme per field, or make the scheme declaration part of the value.");
        OBSOLETE_ATTRIBUTES_MSG.put("scope",
                "Use the \u201Cscope\u201D attribute on a \u201Cth\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("shape",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("standby",
                "Optimise the linked resource so that it loads quickly or, at least, incrementally.");
        OBSOLETE_ATTRIBUTES_MSG.put("target", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("type",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put("urn",
                "Specify the preferred persistent identifier using the \u201Chref\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("usemap",
                "Use the \u201Cimg\u201D element instead of the \u201Cinput\u201D element for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("valuetype",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put("version", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("manifest", "Use service workers instead.");
    }

    private static final Map<String, String[]> OBSOLETE_STYLE_ATTRS = new HashMap<>();

    static {
        OBSOLETE_STYLE_ATTRS.put("align",
                new String[] { "caption", "iframe", "img", "input", "object",
                        "embed", "legend", "table", "hr", "div", "h1", "h2",
                        "h3", "h4", "h5", "h6", "p", "col", "colgroup", "tbody",
                        "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("alink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("allowtransparency",
                new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("background", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("bgcolor",
                new String[] { "table", "tr", "td", "th", "body" });
        OBSOLETE_STYLE_ATTRS.put("cellpadding", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("cellspacing", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("char", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("charoff", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("clear", new String[] { "br" });
        OBSOLETE_STYLE_ATTRS.put("color", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("compact",
                new String[] { "dl", "menu", "ol", "ul" });
        OBSOLETE_STYLE_ATTRS.put("frameborder", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("frame", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("height", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("hspace",
                new String[] { "embed", "iframe", "input", "img", "object" });
        OBSOLETE_STYLE_ATTRS.put("link", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("bottommargin", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginheight",
                new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("leftmargin", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("rightmargin", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("topmargin", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginwidth",
                new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("noshade", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("nowrap", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("rules", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("scrolling", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("size", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("text", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("type", new String[] { "li", "ul" });
        OBSOLETE_STYLE_ATTRS.put("valign", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("vlink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("vspace",
                new String[] { "embed", "iframe", "input", "img", "object" });
        OBSOLETE_STYLE_ATTRS.put("width", new String[] { "hr", "table", "td",
                "th", "col", "colgroup", "pre" });

        for (String[] elementNames: OBSOLETE_STYLE_ATTRS.values()) {
            Arrays.sort(elementNames);
        }
    }

    private static final HashSet<String> JAVASCRIPT_MIME_TYPES = new HashSet<>();

    static {
        JAVASCRIPT_MIME_TYPES.add("application/ecmascript");
        JAVASCRIPT_MIME_TYPES.add("application/javascript");
        JAVASCRIPT_MIME_TYPES.add("application/x-ecmascript");
        JAVASCRIPT_MIME_TYPES.add("application/x-javascript");
        JAVASCRIPT_MIME_TYPES.add("text/ecmascript");
        JAVASCRIPT_MIME_TYPES.add("text/javascript");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.0");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.1");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.2");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.3");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.4");
        JAVASCRIPT_MIME_TYPES.add("text/javascript1.5");
        JAVASCRIPT_MIME_TYPES.add("text/jscript");
        JAVASCRIPT_MIME_TYPES.add("text/livescript");
        JAVASCRIPT_MIME_TYPES.add("text/x-ecmascript");
        JAVASCRIPT_MIME_TYPES.add("text/x-javascript");
    }

    private static final String[] ARIA_HIDDEN_NOT_ALLOWED_ELEMENTS = { "base",
            "col", "colgroup", "head", "html", "link", "map", "meta",
            "noscript", "param", "script", "slot", "source", "style",
            "template", "title", "track" };

    private static final String[] INTERACTIVE_ELEMENTS = { "a", "button",
            "details", "dialog", "embed", "iframe", "label", "select",
            "textarea" };

    private static final String[] INTERACTIVE_ROLES = { "button", "checkbox",
            "combobox", "grid", "gridcell", "listbox", "menu", "menubar",
            "menuitem", "menuitemcheckbox", "menuitemradio", "option", "radio",
            "scrollbar", "searchbox", "slider", "spinbutton", "switch", "tab",
            "textbox", "treeitem" };

    private static final String[] PROHIBITED_INTERACTIVE_ANCESTOR_ROLES = {
            "button", "link" };

    private static final String[] PROHIBITED_MAIN_ANCESTORS = { "a", "address",
            "article", "aside", "audio", "blockquote", "canvas", "caption",
            "dd", "del", "details", "dialog", "dt", "fieldset", "figure",
            "footer", "header", "ins", "li", "main", "map", "nav", "noscript",
            "object", "section", "slot", "td", "th", "video" };

    private static final String[] SPECIAL_ANCESTORS = { "a", "address", "body",
            "button", "caption", "dfn", "dt", "figcaption", "figure", "footer",
            "form", "header", "label", "map", "noscript", "th", "time",
            "progress", "meter", "article", "section", "aside", "nav", "h1",
            "h2", "h3", "h4", "h5", "h6" };

    private static int specialAncestorNumber(String name) {
        for (int i = 0; i < SPECIAL_ANCESTORS.length; i++) {
            if (name == SPECIAL_ANCESTORS[i]) {
                return i;
            }
        }
        return -1;
    }

    private static Map<String, Integer> ANCESTOR_MASK_BY_DESCENDANT = new HashMap<>();

    private static void registerProhibitedAncestor(String ancestor,
            String descendant) {
        int number = specialAncestorNumber(ancestor);
        if (number == -1) {
            throw new IllegalStateException(
                    "Ancestor not found in array: " + ancestor);
        }
        Integer maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(descendant);
        int mask = 0;
        if (maskAsObject != null) {
            mask = maskAsObject.intValue();
        }
        mask |= (1 << number);
        ANCESTOR_MASK_BY_DESCENDANT.put(descendant, Integer.valueOf(mask));
    }

    static {
        registerProhibitedAncestor("form", "form");
        registerProhibitedAncestor("progress", "progress");
        registerProhibitedAncestor("meter", "meter");
        registerProhibitedAncestor("dfn", "dfn");
        registerProhibitedAncestor("noscript", "noscript");
        registerProhibitedAncestor("label", "label");
        registerProhibitedAncestor("address", "address");
        registerProhibitedAncestor("address", "section");
        registerProhibitedAncestor("address", "nav");
        registerProhibitedAncestor("address", "article");
        registerProhibitedAncestor("header", "header");
        registerProhibitedAncestor("footer", "header");
        registerProhibitedAncestor("address", "header");
        registerProhibitedAncestor("header", "footer");
        registerProhibitedAncestor("footer", "footer");
        registerProhibitedAncestor("dt", "header");
        registerProhibitedAncestor("dt", "footer");
        registerProhibitedAncestor("dt", "article");
        registerProhibitedAncestor("dt", "nav");
        registerProhibitedAncestor("dt", "section");
        registerProhibitedAncestor("dt", "h1");
        registerProhibitedAncestor("dt", "h2");
        registerProhibitedAncestor("dt", "h2");
        registerProhibitedAncestor("dt", "h3");
        registerProhibitedAncestor("dt", "h4");
        registerProhibitedAncestor("dt", "h5");
        registerProhibitedAncestor("dt", "h6");
        registerProhibitedAncestor("dt", "hgroup");
        registerProhibitedAncestor("th", "header");
        registerProhibitedAncestor("th", "footer");
        registerProhibitedAncestor("th", "article");
        registerProhibitedAncestor("th", "nav");
        registerProhibitedAncestor("th", "section");
        registerProhibitedAncestor("th", "h1");
        registerProhibitedAncestor("th", "h2");
        registerProhibitedAncestor("th", "h2");
        registerProhibitedAncestor("th", "h3");
        registerProhibitedAncestor("th", "h4");
        registerProhibitedAncestor("th", "h5");
        registerProhibitedAncestor("th", "h6");
        registerProhibitedAncestor("th", "hgroup");
        registerProhibitedAncestor("address", "footer");
        registerProhibitedAncestor("address", "h1");
        registerProhibitedAncestor("address", "h2");
        registerProhibitedAncestor("address", "h3");
        registerProhibitedAncestor("address", "h4");
        registerProhibitedAncestor("address", "h5");
        registerProhibitedAncestor("address", "h6");
        registerProhibitedAncestor("caption", "table");

        for (String elementName : INTERACTIVE_ELEMENTS) {
            registerProhibitedAncestor("a", elementName);
            registerProhibitedAncestor("button", elementName);
        }
    }

    private static final int BODY_MASK = (1 << specialAncestorNumber("body"));

    private static final int A_BUTTON_MASK = (1 << specialAncestorNumber("a"))
            | (1 << specialAncestorNumber("button"));

    private static final int FIGCAPTION_MASK = (1 << specialAncestorNumber(
            "figcaption"));

    private static final int FIGURE_MASK = (1 << specialAncestorNumber(
            "figure"));

    private static final int H1_MASK = (1 << specialAncestorNumber("h1"));

    private static final int H2_MASK = (1 << specialAncestorNumber("h2"));

    private static final int H3_MASK = (1 << specialAncestorNumber("h3"));

    private static final int H4_MASK = (1 << specialAncestorNumber("h4"));

    private static final int H5_MASK = (1 << specialAncestorNumber("h5"));

    private static final int H6_MASK = (1 << specialAncestorNumber("h6"));

    private static final int MAP_MASK = (1 << specialAncestorNumber("map"));

    private static final int HREF_MASK = (1 << 30);

    private static final int LABEL_FOR_MASK = (1 << 29);

    private static final Map<String, Set<String>> REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT = new HashMap<>();

    private static final Map<String, Set<String>> ariaOwnsIdsByRole = new HashMap<>();

    private static void registerRequiredAncestorRole(String parent,
            String child) {
        Set<String> parents = REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(child);
        if (parents == null) {
            parents = new HashSet<>();
        }
        parents.add(parent);
        REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.put(child, parents);
    }

    static {
        registerRequiredAncestorRole("listbox", "option");
        registerRequiredAncestorRole("menu", "menuitem");
        registerRequiredAncestorRole("menu", "menuitemcheckbox");
        registerRequiredAncestorRole("menu", "menuitemradio");
        registerRequiredAncestorRole("menubar", "menuitem");
        registerRequiredAncestorRole("menubar", "menuitemcheckbox");
        registerRequiredAncestorRole("menubar", "menuitemradio");
        registerRequiredAncestorRole("tablist", "tab");
        registerRequiredAncestorRole("tree", "treeitem");
        registerRequiredAncestorRole("group", "treeitem");
        registerRequiredAncestorRole("group", "listitem");
        registerRequiredAncestorRole("group", "menuitemradio");
        registerRequiredAncestorRole("list", "listitem");
        registerRequiredAncestorRole("row", "cell");
        registerRequiredAncestorRole("row", "gridcell");
        registerRequiredAncestorRole("row", "columnheader");
        registerRequiredAncestorRole("row", "rowheader");
        registerRequiredAncestorRole("grid", "row");
        registerRequiredAncestorRole("grid", "rowgroup");
        registerRequiredAncestorRole("rowgroup", "row");
        registerRequiredAncestorRole("treegrid", "row");
        registerRequiredAncestorRole("treegrid", "rowgroup");
        registerRequiredAncestorRole("table", "rowgroup");
        registerRequiredAncestorRole("table", "row");
    }

    private static final Set<String> MUST_NOT_DANGLE_IDREFS = new HashSet<>();

    static {
        MUST_NOT_DANGLE_IDREFS.add("aria-controls");
        MUST_NOT_DANGLE_IDREFS.add("aria-describedby");
        MUST_NOT_DANGLE_IDREFS.add("aria-flowto");
        MUST_NOT_DANGLE_IDREFS.add("aria-labelledby");
        MUST_NOT_DANGLE_IDREFS.add("aria-owns");
    }

    private static final Map<String, String> ELEMENTS_WITH_IMPLICIT_ROLE = new HashMap<>();

    static {
        ELEMENTS_WITH_IMPLICIT_ROLE.put("article", "article");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("aside", "complementary");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("body", "document");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("button", "button");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("datalist", "listbox");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("dd", "definition");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("details", "group");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("dialog", "dialog");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("dfn", "term");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("dt", "term");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("fieldset", "group");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("figure", "figure");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("form", "form");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("footer", "contentinfo");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h1", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h2", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h3", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h4", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h5", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("h6", "heading");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("hr", "separator");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("header", "banner");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("img", "img");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("li", "listitem");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("link", "link");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("main", "main");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("nav", "navigation");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("ol", "list");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("output", "status");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("progress", "progressbar");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("section", "region");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("summary", "button");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("table", "table");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("tbody", "rowgroup");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("textarea", "textbox");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("tfoot", "rowgroup");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("thead", "rowgroup");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("td", "cell");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("tr", "row");
        ELEMENTS_WITH_IMPLICIT_ROLE.put("ul", "list");
    }

    private static final Map<String, String[]> //
        ELEMENTS_WITH_IMPLICIT_ROLES = new HashMap<>();

    static {
        ELEMENTS_WITH_IMPLICIT_ROLES.put("th",
                new String[] { "columnheader", "rowheader" });
    }

    private static final Map<String, String> ELEMENTS_THAT_NEVER_NEED_ROLE = new HashMap<>();

    static {
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("body", "document");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("datalist", "listbox");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("details", "group");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("form", "form");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("main", "main");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("meter", "progressbar");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("nav", "navigation");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("option", "option");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("optgroup", "group");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("progress", "progressbar");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("summary", "button");
        ELEMENTS_THAT_NEVER_NEED_ROLE.put("textarea", "textbox");
    }

    private static final Map<String, String> INPUT_TYPES_WITH_IMPLICIT_ROLE = new HashMap<>();

    static {
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("button", "button");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("checkbox", "checkbox");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("image", "button");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("number", "spinbutton");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("radio", "radio");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("range", "slider");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("reset", "button");
        INPUT_TYPES_WITH_IMPLICIT_ROLE.put("submit", "button");
    }

    private static final Set<String> ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY = new HashSet<>();

    static {
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("colspan");
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("disabled");
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("hidden");
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("readonly");
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("required");
        ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.add("rowspan");
    }

    /**
     * Names of link types that are considered as "External Resource" according to https://html.spec.whatwg.org/multipage/links.html#linkTypes
     */
    private static final String[] EXTERNAL_RESOURCE_LINK_REL = new String[] {
            "dns-prefetch", "icon", "manifest", "modulepreload", "pingback", "preconnect", "prefetch", "preload", "prerender", "stylesheet"
    };

    private static final String h1WarningMessage = "Consider using the"
            + " \u201Ch1\u201D element as a top-level heading only (all"
            + " \u201Ch1\u201D elements are treated as top-level headings"
            + " by many screen readers and other tools).";

    private class IdrefLocator {
        private final Locator locator;

        private final String idref;

        private final String additional;

        /**
         * @param locator
         * @param idref
         */
        public IdrefLocator(Locator locator, String idref) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = null;
        }

        public IdrefLocator(Locator locator, String idref, String additional) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = additional;
        }

        /**
         * Returns the locator.
         *
         * @return the locator
         */
        public Locator getLocator() {
            return locator;
        }

        /**
         * Returns the idref.
         *
         * @return the idref
         */
        public String getIdref() {
            return idref;
        }

        /**
         * Returns the additional.
         *
         * @return the additional
         */
        public String getAdditional() {
            return additional;
        }
    }

    private class StackNode {
        private final int ancestorMask;

        private final String name; // null if not HTML

        private final StringBuilder textContent;

        private final String role;

        private final String activeDescendant;

        private final String forAttr;

        private Set<Locator> imagesLackingAlt = new HashSet<>();

        private Locator nonEmptyOption = null;

        private Locator locator = null;

        private boolean selectedOptions = false;

        private boolean labeledDescendants = false;

        private boolean trackDescendants = false;

        private boolean textNodeFound = false;

        private boolean imgFound = false;

        private boolean embeddedContentFound = false;

        private boolean figcaptionNeeded = false;

        private boolean figcaptionContentFound = false;

        private boolean headingFound = false;

        private boolean optionNeeded = false;

        private boolean optionFound = false;

        private boolean noValueOptionFound = false;

        private boolean emptyValueOptionFound = false;

        private boolean isCollectingCharacters = false;

        private Locator captionNestedInFigure;

        private boolean isCollectingChildren = false;

        private List<StackNode> collectedChildren;

        /**
         * @param ancestorMask
         */
        public StackNode(int ancestorMask, String name, String role,
                String activeDescendant, String forAttr) {
            this.ancestorMask = ancestorMask;
            this.name = name;
            this.role = role;
            this.activeDescendant = activeDescendant;
            this.forAttr = forAttr;
            this.textContent = new StringBuilder();
        }

        /**
         * Returns the ancestorMask.
         *
         * @return the ancestorMask
         */
        public int getAncestorMask() {
            return ancestorMask;
        }

        /**
         * Returns the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the selectedOptions.
         *
         * @return the selectedOptions
         */
        public boolean isSelectedOptions() {
            return selectedOptions;
        }

        /**
         * Sets the selectedOptions.
         *
         * @param selectedOptions
         *            the selectedOptions to set
         */
        public void setSelectedOptions() {
            this.selectedOptions = true;
        }

        /**
         * Returns the labeledDescendants.
         *
         * @return the labeledDescendants
         */
        public boolean isLabeledDescendants() {
            return labeledDescendants;
        }

        /**
         * Sets the labeledDescendants.
         *
         * @param labeledDescendants
         *            the labeledDescendants to set
         */
        public void setLabeledDescendants() {
            this.labeledDescendants = true;
        }

        /**
         * Returns the trackDescendants.
         *
         * @return the trackDescendants
         */
        public boolean isTrackDescendant() {
            return trackDescendants;
        }

        /**
         * Sets the trackDescendants.
         *
         * @param trackDescendants
         *            the trackDescendants to set
         */
        public void setTrackDescendants() {
            this.trackDescendants = true;
        }

        /**
         * Returns the role.
         *
         * @return the role
         */
        public String getRole() {
            return role;
        }

        /**
         * Returns the activeDescendant.
         *
         * @return the activeDescendant
         */
        public String getActiveDescendant() {
            return activeDescendant;
        }

        /**
         * Returns the forAttr.
         *
         * @return the forAttr
         */
        public String getForAttr() {
            return forAttr;
        }

        /**
         * Returns the textNodeFound.
         *
         * @return the textNodeFound
         */
        public boolean hasTextNode() {
            return textNodeFound;
        }

        /**
         * Sets the textNodeFound.
         */
        public void setTextNodeFound() {
            this.textNodeFound = true;
        }

        /**
         * Returns the imgFound.
         *
         * @return the imgFound
         */
        public boolean hasImg() {
            return imgFound;
        }

        /**
         * Sets the imgFound.
         */
        public void setImgFound() {
            this.imgFound = true;
        }

        /**
         * Returns the embeddedContentFound.
         *
         * @return the embeddedContentFound
         */
        public boolean hasEmbeddedContent() {
            return embeddedContentFound;
        }

        /**
         * Sets the embeddedContentFound.
         */
        public void setEmbeddedContentFound() {
            this.embeddedContentFound = true;
        }

        /**
         * Returns the figcaptionNeeded.
         *
         * @return the figcaptionNeeded
         */
        public boolean needsFigcaption() {
            return figcaptionNeeded;
        }

        /**
         * Sets the figcaptionNeeded.
         */
        public void setFigcaptionNeeded() {
            this.figcaptionNeeded = true;
        }

        /**
         * Returns the figcaptionContentFound.
         *
         * @return the figcaptionContentFound
         */
        public boolean hasFigcaptionContent() {
            return figcaptionContentFound;
        }

        /**
         * Sets the figcaptionContentFound.
         */
        public void setFigcaptionContentFound() {
            this.figcaptionContentFound = true;
        }

        /**
         * Returns the headingFound.
         *
         * @return the headingFound
         */
        public boolean hasHeading() {
            return headingFound;
        }

        /**
         * Sets the headingFound.
         */
        public void setHeadingFound() {
            this.headingFound = true;
        }

        /**
         * Returns the imagesLackingAlt
         *
         * @return the imagesLackingAlt
         */
        public Set<Locator> getImagesLackingAlt() {
            return imagesLackingAlt;
        }

        /**
         * Adds to the imagesLackingAlt
         */
        public void addImageLackingAlt(Locator locator) {
            this.imagesLackingAlt.add(locator);
        }

        /**
         * Returns the optionNeeded.
         *
         * @return the optionNeeded
         */
        public boolean isOptionNeeded() {
            return optionNeeded;
        }

        /**
         * Sets the optionNeeded.
         */
        public void setOptionNeeded() {
            this.optionNeeded = true;
        }

        /**
         * Returns the optionFound.
         *
         * @return the optionFound
         */
        public boolean hasOption() {
            return optionFound;
        }

        /**
         * Sets the optionFound.
         */
        public void setOptionFound() {
            this.optionFound = true;
        }

        /**
         * Returns the noValueOptionFound.
         *
         * @return the noValueOptionFound
         */
        public boolean hasNoValueOption() {
            return noValueOptionFound;
        }

        /**
         * Sets the noValueOptionFound.
         */
        public void setNoValueOptionFound() {
            this.noValueOptionFound = true;
        }

        /**
         * Returns the emptyValueOptionFound.
         *
         * @return the emptyValueOptionFound
         */
        public boolean hasEmptyValueOption() {
            return emptyValueOptionFound;
        }

        /**
         * Sets the emptyValueOptionFound.
         */
        public void setEmptyValueOptionFound() {
            this.emptyValueOptionFound = true;
        }

        /**
         * Returns the nonEmptyOption.
         *
         * @return the nonEmptyOption
         */
        public Locator nonEmptyOptionLocator() {
            return nonEmptyOption;
        }

        /**
         * Sets the nonEmptyOption.
         */
        public void setNonEmptyOption(Locator locator) {
            this.nonEmptyOption = locator;
        }

        /**
         * Sets the collectingCharacters.
         */
        public void setIsCollectingCharacters(boolean isCollectingCharacters) {
            this.isCollectingCharacters = isCollectingCharacters;
        }

        /**
         * Gets the collectingCharacters.
         */
        public boolean getIsCollectingCharacters() {
            return this.isCollectingCharacters;
        }

        /**
         * Appends to the textContent.
         */
        public void appendToTextContent(char ch[], int start, int length) {
            this.textContent.append(ch, start, length);
        }

        /**
         * Gets the textContent.
         */
        public StringBuilder getTextContent() {
            return this.textContent;
        }

        /**
         * Returns the locator.
         *
         * @return the locator
         */
        public Locator locator() {
            return locator;
        }

        /**
         * Sets the locator.
         */
        public void setLocator(Locator locator) {
            this.locator = locator;
        }

        public void setCaptionNestedInFigure(Locator nestedFigureCaption) {
            this.captionNestedInFigure = nestedFigureCaption;
        }

        public Locator getCaptionNestedInFigure() {
            return captionNestedInFigure;
        }

        public void setIsCollectingChildren(boolean isCollectingChildren) {
            this.isCollectingChildren = isCollectingChildren;
        }

        public boolean isCollectingChildren() {
            return isCollectingChildren;
        }

        public void addChild(StackNode node) {
            if (collectedChildren == null) {
                collectedChildren = new ArrayList<>();
            }
            collectedChildren.add(node);
        }

        public List<StackNode> getCollectedChildren() {
            return collectedChildren == null ? Collections.emptyList()
                    : collectedChildren;
        }
    }

    private StackNode[] stack;

    private int currentPtr;

    public Assertions() {
        super();
    }

    private HttpServletRequest request;

    private boolean sourceIsCss;

    public void setSourceIsCss(boolean sourceIsCss) {
        this.sourceIsCss = sourceIsCss;
    }

    private boolean hasPageEmitterInCallStack() {
        for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
            if (el.getClassName().equals("nu.validator.servlet.PageEmitter")) {
                return true;
            }
        }
        return false;
    }

    private boolean isAriaLabelMisuse(String ariaLabel, String localName,
            String role, Attributes atts) {
        if (ariaLabel == "") {
            return false;
        }
        if (ariaLabel == null) {
            return false;
        }
        if (Arrays.binarySearch(INTERACTIVE_ELEMENTS, localName) >= 0) {
            return false;
        }
        if (isLabelableElement(localName, atts)) {
            return false;
        }
        // https://html5accessibility.com/stuff/2020/11/07/not-so-short-note-on-aria-label-usage-big-table-edition/
        if (//
                // "a" == localName // INTERACTIVE_ELEMENTS
                "area" == localName // https://github.com/validator/validator/issues/775#issuecomment-494455608
                || "article" == localName //
                || "aside" == localName //
                || "audio" == localName //
                // "button" == localName // INTERACTIVE_ELEMENTS
                // "details" == localName // INTERACTIVE_ELEMENTS
                // "dialog" == localName // INTERACTIVE_ELEMENTS
                || "dl" == localName //
                || "fieldset" == localName //
                || "figure" == localName //
                || "footer" == localName //
                || "form" == localName //
                || "h1" == localName //
                || "h2" == localName //
                || "h3" == localName //
                || "h4" == localName //
                || "h5" == localName //
                || "h6" == localName //
                || "header" == localName //
                || "hr" == localName //
                // "iframe" == localName // INTERACTIVE_ELEMENTS
                || "img" == localName // https://github.com/validator/validator/issues/775
                // "input" == localName // isLabelableElement
                || "main" == localName //
                // "meter" == localName // isLabelableElement
                || "nav" == localName //
                || "ol" == localName //
                // "progress" == localName // isLabelableElement
                || "section" == localName //
                // "select" == localName // isLabelableElement
                || "summary" == localName // https://github.com/validator/validator/issues/775#issuecomment-494459220
                || "svg" == localName //
                || "table" == localName //
                // "textarea" == localName // INTERACTIVE_ELEMENTS
                || "video" == localName //
                || "ul" == localName //
        ) {
            return false;
        }
        if (role != null) {
            return false;
        }
        return true;
    }

    private boolean isLabelableElement(String localName, Attributes atts) {
        if ("button" == localName //
                || ("input" == localName && !AttributeUtil //
                        .lowerCaseLiteralEqualsIgnoreAsciiCaseString("hidden",
                                atts.getValue("", "type"))) //
                || "meter" == localName //
                || "output" == localName //
                || "progress" == localName //
                || "select" == localName //
                || "textarea" == localName) {
            return true;
        }
        return false;
    }

    private void incrementUseCounter(String useCounterName) {
        if (request != null) {
            request.setAttribute(
                    "http://validator.nu/properties/" + useCounterName, true);
        }
    }

    private void push(StackNode node) {
        currentPtr++;
        if (currentPtr == stack.length) {
            StackNode[] newStack = new StackNode[stack.length + 64];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[currentPtr] = node;
    }

    private StackNode pop() {
        return stack[currentPtr--];
    }

    private StackNode peek() {
        return stack[currentPtr];
    }

    private Map<StackNode, Locator> openSingleSelects = new HashMap<>();

    private Map<StackNode, Locator> openLabels = new HashMap<>();

    private Map<StackNode, TaintableLocatorImpl> openMediaElements = new HashMap<>();

    private Map<StackNode, Locator> openActiveDescendants = new HashMap<>();

    private LinkedHashSet<IdrefLocator> formControlReferences = new LinkedHashSet<>();

    private LinkedHashSet<IdrefLocator> formElementReferences = new LinkedHashSet<>();

    private LinkedHashSet<IdrefLocator> needsAriaOwner = new LinkedHashSet<>();

    private Set<String> formControlIds = new HashSet<>();

    private Set<String> formElementIds = new HashSet<>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<>();

    private Set<String> listIds = new HashSet<>();

    private LinkedHashSet<IdrefLocator> ariaReferences = new LinkedHashSet<>();

    private Set<String> allIds = new HashSet<>();

    private int currentFigurePtr;

    private int currentHeadingPtr;

    private final LinkedList<Integer> sectioningElementPtrs = new LinkedList<>();

    private boolean hasVisibleMain;

    private boolean hasMetaCharset;

    private boolean hasMetaDescription;

    private boolean hasContentTypePragma;

    private boolean hasAutofocus;

    private boolean hasTopLevelH1;

    private boolean hasAncestorTableIsRoleTableGridOrTreeGrid = false;

    private boolean parsingScriptImportMap = false;

    private int numberOfTemplatesDeep = 0;

    private int numberOfSvgAelementsDeep = 0;

    private Set<Locator> secondLevelH1s = new HashSet<>();

    private Map<Locator, Map<String, String>> siblingSources = new ConcurrentHashMap<>();

    private final void errContainedInOrOwnedBy(String role, Locator locator)
            throws SAXException {
        err("An element with \u201Crole=" + role + "\u201D"
                + " must be contained in, or owned by, an element with "
                + renderRoleSet(REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(role))
                + ".", locator);
    }

    private final void errObsoleteAttribute(String attribute, String element,
            String suggestion) throws SAXException {
        err("The \u201C" + attribute + "\u201D attribute on the \u201C"
                + element + "\u201D element is obsolete." + suggestion);
    }

    private final void warnObsoleteAttribute(String attribute, String element,
            String suggestion) throws SAXException {
        warn("The \u201C" + attribute + "\u201D attribute on the \u201C"
                + element + "\u201D element is obsolete." + suggestion);
    }

    private final void warnExplicitRoleUnnecessaryForType(String element,
            String role, String type) throws SAXException {
        warn("The \u201C" + role + "\u201D role is unnecessary for element"
                + " \u201C" + element + "\u201D whose" + " type is" + " \u201C"
                + type + "\u201D.");
    }

    private boolean currentElementHasRequiredAncestorRole(
            Set<String> requiredAncestorRoles) {
        for (String role : requiredAncestorRoles) {
            for (int i = 0; i < currentPtr; i++) {
                if (role.equals(stack[currentPtr - i].getRole())) {
                    return true;
                }
                String openElementName = stack[currentPtr - i].getName();
                if (ELEMENTS_WITH_IMPLICIT_ROLE.containsKey(openElementName)
                        && ELEMENTS_WITH_IMPLICIT_ROLE.get(openElementName) //
                                .equals(role)) {
                    return true;
                }
                if (ELEMENTS_WITH_IMPLICIT_ROLES.containsKey(openElementName)
                        && Arrays.binarySearch(ELEMENTS_WITH_IMPLICIT_ROLES //
                                .get(openElementName), role) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkForInteractiveAncestorRole(String descendantUiString)
            throws SAXException {
        for (int i = 0; i < currentPtr; i++) {
            String ancestorRole = stack[currentPtr - i].getRole();
            if (ancestorRole != null && ancestorRole != ""
                    && Arrays.binarySearch(
                            PROHIBITED_INTERACTIVE_ANCESTOR_ROLES,
                            ancestorRole) >= 0) {
                err(descendantUiString + " must not appear as a"
                        + " descendant of an element with the attribute"
                        + " \u201Crole=" + ancestorRole + "\u201D.");
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        // label for
        for (IdrefLocator idrefLocator : formControlReferences) {
            if (!formControlIds.contains(idrefLocator.getIdref())) {
                err("The value of the \u201Cfor\u201D attribute of the"
                        + " \u201Clabel\u201D element must be the ID of a"
                        + " non-hidden form control.",
                        idrefLocator.getLocator());
            }
        }

        // references to IDs from form attributes
        for (IdrefLocator idrefLocator : formElementReferences) {
            if (!formElementIds.contains(idrefLocator.getIdref())) {
                err("The \u201Cform\u201D attribute must refer to a form element.",
                        idrefLocator.getLocator());
            }
        }

        // input list
        for (IdrefLocator idrefLocator : listReferences) {
            if (!listIds.contains(idrefLocator.getIdref())) {
                err("The \u201Clist\u201D attribute of the \u201Cinput\u201D element must refer to a \u201Cdatalist\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA idrefs
        for (IdrefLocator idrefLocator : ariaReferences) {
            if (!allIds.contains(idrefLocator.getIdref())) {
                err("The \u201C" + idrefLocator.getAdditional()
                        + "\u201D attribute must point to an element in the same document.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA required owners
        for (IdrefLocator idrefLocator : needsAriaOwner) {
            boolean foundOwner = false;
            String role = idrefLocator.getAdditional();
            for (String ownerRole : REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(
                    role)) {
                if (ariaOwnsIdsByRole.size() != 0
                        && ariaOwnsIdsByRole.get(ownerRole) != null
                        && ariaOwnsIdsByRole.get(ownerRole).contains(
                                idrefLocator.getIdref())) {
                    foundOwner = true;
                    break;
                }
            }
            if (!foundOwner) {
                errContainedInOrOwnedBy(role, idrefLocator.getLocator());
            }
        }

        if (hasTopLevelH1) {
            for (Locator locator : secondLevelH1s) {
                warn(h1WarningMessage, locator);
            }
        }

        reset();
        stack = null;
    }

    private static double getDoubleAttribute(Attributes atts, String name) {
        String str = atts.getValue("", name);
        if (str == null) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri
                && "template".equals(localName)) {
            numberOfTemplatesDeep--;
            if (numberOfTemplatesDeep != 0) {
                return;
            }
        } else if (numberOfTemplatesDeep > 0) {
            return;
        }
        if ("http://www.w3.org/2000/svg" == uri && "a".equals(localName)) {
            numberOfSvgAelementsDeep--;
        }
        StackNode node = pop();
        String systemId = node.locator().getSystemId();
        String publicId = node.locator().getPublicId();
        Locator locator = null;
        openSingleSelects.remove(node);
        openLabels.remove(node);
        openMediaElements.remove(node);
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("figure" == localName) {
                if (node.hasFigcaptionContent() && (node.role != null)) {
                    err("A \u201Cfigure\u201D element with a"
                            + " \u201Cfigcaption\u201D descendant must not"
                            + " have a \u201Crole\u201D attribute.");
                }
                if ((node.needsFigcaption() && !node.hasFigcaptionContent())
                        || node.hasTextNode() || node.hasEmbeddedContent()) {
                    for (Locator imgLocator : node.getImagesLackingAlt()) {
                        err("An \u201Cimg\u201D element must have an"
                                + " \u201Calt\u201D attribute, except under"
                                + " certain conditions. For details, consult"
                                + " guidance on providing text alternatives"
                                + " for images.", imgLocator);
                    }
                }
                if (node.getCaptionNestedInFigure() != null
                        && node.getCollectedChildren().size() == 2
                        && node.getCollectedChildren().stream().anyMatch(
                                s -> "figcaption" == s.getName())
                        && node.getCollectedChildren().stream().anyMatch(
                                s -> "table" == s.getName())) {
                    warn("When a \u201Ctable\u201D element is the only content"
                            + " in a \u201Cfigure\u201D element other than the"
                            + " \u201Cfigcaption\u201D, the \u201Ccaption\u201D"
                            + " element should be omitted in favor of the"
                            + " \u201Cfigcaption\u201D.",
                            node.getCaptionNestedInFigure());
                }
            } else if ("picture" == localName) {
                siblingSources.clear();
            } else if ("select" == localName && node.isOptionNeeded()) {
                if (!node.hasOption()) {
                    err("A \u201Cselect\u201D element with a"
                            + " \u201Crequired\u201D attribute, and without a"
                            + " \u201Cmultiple\u201D attribute, and without a"
                            + " \u201Csize\u201D attribute whose value is"
                            + " greater than"
                            + " \u201C1\u201D, must have a child"
                            + " \u201Coption\u201D element.");
                }
                if (node.nonEmptyOptionLocator() != null) {
                    err("The first child \u201Coption\u201D element of a"
                            + " \u201Cselect\u201D element with a"
                            + " \u201Crequired\u201D attribute, and without a"
                            + " \u201Cmultiple\u201D attribute, and without a"
                            + " \u201Csize\u201D attribute whose value is"
                            + " greater than"
                            + " \u201C1\u201D, must have either an empty"
                            + " \u201Cvalue\u201D attribute, or must have no"
                            + " text content."
                            + " Consider either adding a placeholder option"
                            + " label, or adding a"
                            + " \u201Csize\u201D attribute with a value equal"
                            + " to the number of"
                            + " \u201Coption\u201D elements.",
                            node.nonEmptyOptionLocator());
                }
            } else if ("section" == localName && !node.hasHeading()) {
                warn("Section lacks heading. Consider using"
                        + " \u201ch2\u201d-\u201ch6\u201d elements to add"
                        + " identifying headings to all sections, or else"
                        + " use a \u201cdiv\u201d element instead for any"
                        + " cases where no heading is needed.",
                        node.locator());
            } else if ("article" == localName && !node.hasHeading()) {
                warn("Article lacks heading. Consider using"
                        + " \u201ch2\u201d-\u201ch6\u201d elements to add"
                        + " identifying headings to all articles.",
                        node.locator());
            } else if (("h1" == localName || "h2" == localName
                    || "h3" == localName || "h4" == localName
                    || "h5" == localName || "h6" == localName)
                    && !node.hasTextNode() && !node.hasImg()) {
                warn("Empty heading.", node.locator());
            } else if ("option" == localName
                    && !stack[currentPtr].hasOption()) {
                stack[currentPtr].setOptionFound();
            } else if ("style" == localName) {
                String styleContents = node.getTextContent().toString();
                int lineOffset = 0;
                if (styleContents.startsWith("\n")) {
                    lineOffset = 1;
                }
                ApplContext ac = new ApplContext("en");
                ac.setCssVersionAndProfile("css3svg");
                ac.setMedium("all");
                ac.setSuggestPropertyName(false);
                ac.setTreatVendorExtensionsAsWarnings(true);
                ac.setTreatCssHacksAsWarnings(true);
                ac.setWarningLevel(-1);
                ac.setFakeURL("file://localhost/StyleElement");
                StyleSheetParser styleSheetParser = new StyleSheetParser(ac, true);
                styleSheetParser.parseStyleSheet(ac,
                        new StringReader(styleContents.substring(lineOffset)),
                        null);
                styleSheetParser.getStyleSheet().findConflicts(ac);
                Errors errors = styleSheetParser.getStyleSheet().getErrors();
                if (errors.getErrorCount() > 0) {
                    incrementUseCounter("style-element-errors-found");
                }
                for (int i = 0; i < errors.getErrorCount(); i++) {
                    String message = "";
                    String cssProperty = "";
                    String cssMessage = "";
                    CssError error = errors.getErrorAt(i);
                    int beginLine = error.getBeginLine() + lineOffset;
                    int beginColumn = error.getBeginColumn();
                    int endLine = error.getEndLine() + lineOffset;
                    int endColumn = error.getEndColumn();
                    if (beginLine == 0) {
                        continue;
                    }
                    Throwable ex = error.getException();
                    if (ex instanceof CssParseException) {
                        CssParseException cpe = (CssParseException) ex;
                        if ("generator.unrecognize" //
                                .equals(cpe.getErrorType())) {
                            cssMessage = "Parse Error";
                        }
                        if (cpe.getProperty() != null) {
                            cssProperty = String.format("\u201c%s\u201D: ",
                                    cpe.getProperty());
                        }
                        if (cpe.getMessage() != null) {
                            cssMessage = cpe.getMessage();
                        }
                        if (!"".equals(cssMessage)) {
                            message = cssProperty + cssMessage.trim();
                            if (!".".equals(
                                    message.substring(message.length() - 1))) {
                                message = message + ".";
                            }
                        }
                    } else {
                        message = ex.getMessage();
                    }
                    if (!"".equals(message)) {
                        int lastLine = node.locator.getLineNumber() //
                                + endLine - 1;
                        int lastColumn = endColumn;
                        int columnOffset = node.locator.getColumnNumber();
                        if (error.getBeginLine() == 1) {
                            if (lineOffset != 0) {
                                columnOffset = 0;
                            }
                        } else {
                            columnOffset = 0;
                        }
                        String prefix = sourceIsCss ? "" : "CSS: ";
                        SAXParseException spe = new SAXParseException( //
                                prefix + message, publicId, systemId, //
                                lastLine, lastColumn);
                        int[] start = {
                                node.locator.getLineNumber() + beginLine - 1,
                                beginColumn, columnOffset };
                        if ((getErrorHandler() instanceof MessageEmitterAdapter)
                                && !(getErrorHandler() instanceof TestRunner)) {
                            ((MessageEmitterAdapter) getErrorHandler()) //
                                    .errorWithStart(spe, start);
                        } else {
                            getErrorHandler().error(spe);
                        }
                    }
                }
            }
            if ("article" == localName || "aside" == localName
                    || "nav" == localName || "section" == localName) {
                sectioningElementPtrs.pollLast();
            }
            if ("script" == localName && parsingScriptImportMap) {
                isImportMapValid(node.getTextContent().toString());
                parsingScriptImportMap = false;
            }
        }
        if ((locator = openActiveDescendants.remove(node)) != null) {
            warn("Attribute \u201Caria-activedescendant\u201D value should "
                    + "either refer to a descendant element, or should "
                    + "be accompanied by attribute \u201Caria-owns\u201D.",
                    locator);
        }
    }

    private boolean isImportMapValid(String scriptContent) throws SAXException {
        Object importMap;
        try {
            importMap = new JSON().parse(new JSON.StringSource(scriptContent), true);
        } catch (IllegalStateException e) {
            err("A script \u201cscript\u201d with a \u201ctype\u201d attribute"
                    + " whose value is \u201cimportmap\u201d must have valid"
                    + " JSON content.");
            return false;
        }
        if (!(importMap instanceof Map)) {
            err("A \u201cscript\u201d element with a \u201ctype\u201d attribute"
                    + " whose value is \u201cimportmap\u201d must contain a"
                    + " JSON object.");
            return false;
        }

        Map<String, Object> importMapObject = (Map<String, Object>) importMap;

        for (Map.Entry<String, Object> importMapEntry : importMapObject.entrySet()) {
            String specifierType = importMapEntry.getKey();
            if (!"imports".equals(specifierType)
                    && !"scopes".equals(specifierType)) {
                err("A \u201cscript\u201d element with a \u201ctype\u201d"
                        + " attribute whose value is \u201cimportmap\u201d must"
                        + " contain a JSON object with no properties other than"
                        + " \u201cimports\u201d and \u201cscopes\u201d.");
                return false;
            }
            if (!(importMapEntry.getValue() instanceof Map)) {
                err("The value of the \u201c" + specifierType + "\u201d"
                        + " property within the content of a \u201cscript\u201d"
                        + " element with a \u201ctype\u201d attribute whose"
                        + " value is \u201cimportmap\u201d must be a JSON"
                        + " object.");
                return false;
            }

            Map<String, Object> importMapValue = (Map<String, Object>) importMapEntry.getValue();
            for (Map.Entry<String, Object> entry : importMapValue.entrySet()) {

                if ("imports".equals(specifierType)) {
                    if (!isSpecifierMapValid(specifierType, entry.getKey(),
                            entry.getValue())) {
                        return false;
                    }
                } else if ("scopes".equals(specifierType)) {
                    if (!isValidURL(entry.getKey())) {
                        err("The value of the \u201cscopes\u201d property"
                                + " within the content of a \u201cscript\u201d"
                                + " element with a \u201ctype\u201d attribute"
                                + " whose value is \u201cimportmap\u201d must"
                                + " be a JSON object whose keys are valid URL"
                                + " strings.");
                        return false;
                    }
                    if (!(entry.getValue() instanceof Map)) {
                        err("The value of the \u201cscopes\u201d property"
                                + " within the content of a \u201cscript\u201d"
                                + " element with a \u201ctype\u201d attribute"
                                + " whose value is \u201cimportmap\u201d must"
                                + " be a JSON object whose values are also"
                                + " JSON objects.");
                        return false;
                    }
                    Map<String, Object> scopesMap = (Map<String, Object>) entry.getValue();
                    for (Map.Entry<String, Object> scopesEntry : scopesMap.entrySet()) {
                        if (!isSpecifierMapValid(specifierType,
                                scopesEntry.getKey(), scopesEntry.getValue())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isSpecifierMapValid(String type, String key, Object value)
            throws SAXException {
        if (key.isEmpty()) {
            err("A specifier map defined in a \u201c" + type + "\u201d"
                    + " property within the content of a \u201cscript\u201d"
                    + " element with a \u201ctype\u201d attribute whose value"
                    + " is \u201cimportmap\u201d must only contain non-empty"
                    + " keys.");
            return false;
        }
        if (!(value instanceof String)) {
            err("A specifier map defined in a \u201c" + type + "\u201d"
                    + " property within the content of a \u201cscript\u201d"
                    + " element with a \u201ctype\u201d attribute whose value"
                    + " is \u201cimportmap\u201d must only contain string"
                    + " values.");
            return false;
        }
        String sValue = (String) value;
        if (!isValidURL(sValue)) {
            err("A specifier map defined in a \u201c" + type + "\u201d"
                    + " property within the content of a \u201cscript\u201d"
                    + " element with a \u201ctype\u201d attribute whose value"
                    + " is \u201cimportmap\u201d must only contain valid URL"
                    + " values.");
            return false;
        }
        if (key.endsWith("/") && !sValue.endsWith("/")) {
            err("A specifier map defined in a \u201c" + type + "\u201d"
                    + " property within the content of a \u201cscript\u201d"
                    + " element with a \u201ctype\u201d attribute whose value"
                    + " is \u201cimportmap\u201d must have values that end with"
                    + " \u201c/\u201d when its corresponding key ends with"
                    + " \u201c/\u201d.");
            return false;
        }
        return true;
    }

    private boolean isValidURL(String value) {
        try {
            URL.parse(value);
            return true;
        } catch (GalimatiasParseException e) {
        }

        if (value.startsWith("/") || value.startsWith("./")
                || value.startsWith("../")) {
            try {
                URL.parse("https://example.com/" + value);
                return true;
            } catch (GalimatiasParseException e) {
            }
        }

        return false;
    }

    /**
     * @see nu.validator.checker.Checker#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        reset();
        request = getRequest();
        stack = new StackNode[32];
        currentPtr = 0;
        currentFigurePtr = -1;
        currentHeadingPtr = -1;
        stack[0] = null;
        hasVisibleMain = false;
        hasMetaCharset = false;
        hasMetaDescription = false;
        hasContentTypePragma = false;
        hasAutofocus = false;
        hasTopLevelH1 = false;
        hasAncestorTableIsRoleTableGridOrTreeGrid = false;
        numberOfTemplatesDeep = 0;
        numberOfSvgAelementsDeep = 0;
    }

    @Override
    public void reset() {
        openSingleSelects.clear();
        openLabels.clear();
        openMediaElements.clear();
        openActiveDescendants.clear();
        ariaOwnsIdsByRole.clear();
        needsAriaOwner.clear();
        formControlReferences.clear();
        formElementReferences.clear();
        formControlIds.clear();
        formElementIds.clear();
        listReferences.clear();
        listIds.clear();
        ariaReferences.clear();
        allIds.clear();
        siblingSources.clear();
        secondLevelH1s.clear();
        sectioningElementPtrs.clear();
    }

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri
                && "template".equals(localName)) {
            numberOfTemplatesDeep++;
            if (numberOfTemplatesDeep != 1) {
                return;
            }
        } else if (numberOfTemplatesDeep > 0) {
            return;
        }
        if ("http://www.w3.org/2000/svg" == uri && "a".equals(localName)) {
            numberOfSvgAelementsDeep++;
            if (numberOfSvgAelementsDeep != 1) {
                err("The SVG element \u201Ca\u201D must not appear as a"
                        + " descendant of another SVG element \u201Ca\u201D.");
            }
        }
        Set<String> ids = new HashSet<>();
        String role = null;
        String inputTypeVal = null;
        String activeDescendant = null;
        String ariaLabel = null;
        String owns = null;
        String forAttr = null;
        boolean href = false;
        boolean activeDescendantWithAriaOwns = false;
        // see nu.validator.datatype.ImageCandidateStrings
        System.setProperty("nu.validator.checker.imageCandidateString.hasWidth",
                "0");

        StackNode parent = peek();
        int ancestorMask = 0;
        String parentRole = null;
        String parentName = null;
        if (parent != null) {
            ancestorMask = parent.getAncestorMask();
            parentName = parent.getName();
            parentRole = parent.getRole();
        }
        if ("http://www.w3.org/1998/Math/MathML" == uri
                && "math".equals(localName) //
                && atts.getIndex("", "role") > -1
                && "math".equals(atts.getValue("", "role"))) {
            warn("Element \u201Cmath\u201D does not need a"
                    + " \u201Crole\u201D attribute.");
        }
        if ("http://www.w3.org/1999/xhtml" == uri) {
            boolean controls = false;
            boolean hidden = false;
            boolean toolbar = false;
            boolean usemap = false;
            boolean ismap = false;
            boolean selected = false;
            boolean itemid = false;
            boolean itemref = false;
            boolean itemscope = false;
            boolean itemtype = false;
            boolean tabindex = false;
            boolean languageJavaScript = false;
            boolean typeNotTextJavaScript = false;
            boolean hasAriaAttributesOtherThanAriaHidden = false;
            boolean isCustomElement = false;
            String xmlLang = null;
            String lang = null;
            String id = null;
            String list = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attUri = atts.getURI(i);
                boolean isEmptyAtt = "".equals(atts.getValue(i));
                if (attUri.length() == 0) {
                    String attLocal = atts.getLocalName(i);
                    if ("aria-hidden".equals(attLocal) && !isEmptyAtt) {
                        if (Arrays.binarySearch(
                                ARIA_HIDDEN_NOT_ALLOWED_ELEMENTS,
                                localName) >= 0) {
                            err("The \u201Caria-hidden\u201D attribute must not"
                                    + " be specified on the \u201C" + localName
                                    + "\u201D element.");
                        } else if ("input" == localName
                                && "hidden".equals(atts.getValue("", "type"))) {
                            err("The \u201Caria-hidden\u201D attribute must not"
                                    + " be specified on an \u201Cinput\u201D"
                                    + " element whose \u201Ctype\u201D"
                                    + " attribute has the value"
                                    + " \u201Chidden\u201D.");
                        }
                    } else if (attLocal.startsWith("aria-") && !isEmptyAtt) {
                        hasAriaAttributesOtherThanAriaHidden = true;
                    }
                    if (ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.contains(
                            attLocal) && (!isEmptyAtt || "hidden".equals(attLocal))) {
                        String stateOrProperty = "aria-" + attLocal;
                        if (atts.getIndex("", stateOrProperty) > -1) {
                            String attLocalValue = atts.getValue("", attLocal);
                            String stateOrPropertyValue = atts.getValue("",
                                    stateOrProperty);
                            if ("hidden".equals(attLocal)
                                    && "until-found".equals(attLocalValue)
                                    && "true".equals(stateOrPropertyValue)) {
                                err("Attribute \u201Caria-hidden\u201D with value"
                                        + " \u201Ctrue\u201D must not be specified"
                                        + " on elements with \u201Chidden\u201D"
                                        + " attribute value \u201Cuntil-found\u201D."
                                        + " This combination prevents content from"
                                        + " being accessible to assistive technology"
                                        + " when revealed through search.");
                            } else if ("true".equals(stateOrPropertyValue)
                                    || attLocalValue.equals(
                                            stateOrPropertyValue)) {
                                warn("Attribute \u201C" + stateOrProperty
                                        + "\u201D is unnecessary for elements"
                                        + " that have attribute \u201C"
                                        + attLocal + "\u201D.");
                            } else if ("false".equals(stateOrPropertyValue)) {
                                err("Attribute \u201C" + stateOrProperty
                                        + "\u201D must not be specified on"
                                        + " elements that have attribute"
                                        + " \u201C" + attLocal + "\u201D.");
                            } else if (!attLocalValue.equals(
                                            stateOrPropertyValue)) {
                                err("Attribute \u201C" + stateOrProperty
                                        + "\u201D must not be specified with"
                                        + " a different value than  attribute"
                                        + " \u201C" + attLocal + "\u201D.");
                            }
                        }
                    }
                    if ("embed".equals(localName)) {
                        for (int j = 0; j < attLocal.length(); j++) {
                            char c = attLocal.charAt(j);
                            if (c >= 'A' && c <= 'Z') {
                                err("Bad attribute name \u201c" + attLocal
                                        + "\u201d. Attribute names for the"
                                        + " \u201cembed\u201d element must not"
                                        + " contain uppercase ASCII letters.");
                            }
                        }
                        if (!NCName.isNCName(attLocal)) {
                            err("Bad attribute name \u201c" + attLocal
                                    + "\u201d. Attribute names for the"
                                    + " \u201cembed\u201d element must be"
                                    + " XML-compatible.");
                        }
                    }
                    if ("style" == attLocal) {
                        String styleContents = atts.getValue(i);
                        ApplContext ac = new ApplContext("en");
                        ac.setCssVersionAndProfile("css3svg");
                        ac.setMedium("all");
                        ac.setSuggestPropertyName(false);
                        ac.setTreatVendorExtensionsAsWarnings(true);
                        ac.setTreatCssHacksAsWarnings(true);
                        ac.setWarningLevel(-1);
                        ac.setFakeURL("file://localhost/StyleAttribute");
                        StyleSheetParser styleSheetParser = //
                                new StyleSheetParser(ac);
                        styleSheetParser.parseStyleAttribute(ac,
                                new ByteArrayInputStream(
                                        styleContents.getBytes()),
                                "", ac.getFakeURL(),
                                getDocumentLocator().getLineNumber());
                        styleSheetParser.getStyleSheet().findConflicts(ac);
                        Errors errors = //
                                styleSheetParser.getStyleSheet().getErrors();
                        if (errors.getErrorCount() > 0) {
                            incrementUseCounter("style-attribute-errors-found");
                        }
                        for (int j = 0; j < errors.getErrorCount(); j++) {
                            String message = "";
                            String cssProperty = "";
                            String cssMessage = "";
                            CssError error = errors.getErrorAt(j);
                            Throwable ex = error.getException();
                            if (ex instanceof CssParseException) {
                                CssParseException cpe = (CssParseException) ex;
                                if ("generator.unrecognize" //
                                        .equals(cpe.getErrorType())) {
                                    cssMessage = "Parse Error";
                                }
                                if (cpe.getProperty() != null) {
                                    cssProperty = String.format(
                                            "\u201c%s\u201D: ",
                                            cpe.getProperty());
                                }
                                if (cpe.getMessage() != null) {
                                    cssMessage = cpe.getMessage();
                                }
                                if (!"".equals(cssMessage)) {
                                    message = cssProperty + cssMessage.trim();
                                    if (!".".equals(message.substring(
                                            message.length() - 1))) {
                                        message = message + ".";
                                    }
                                }
                            } else {
                                message = ex.getMessage();
                            }
                            if (!"".equals(message)) {
                                err("CSS: " + message);
                            }
                        }
                    } else if ("tabindex" == attLocal) {
                        tabindex = true;
                    } else if ("href" == attLocal) {
                        href = true;
                    } else if ("controls" == attLocal) {
                        controls = true;
                    } else if ("type" == attLocal && "param" != localName
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        if ("input" == localName) {
                            inputTypeVal = atts.getValue(i).toLowerCase();
                        }
                        String attValue = atts.getValue(i);
                        if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "hidden", attValue)) {
                            hidden = true;
                        } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "toolbar", attValue)) {
                            toolbar = true;
                        }

                        if (!AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "text/javascript", attValue)) {
                            typeNotTextJavaScript = true;
                        }
                    } else if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal
                            && !isEmptyAtt) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-label" == attLocal && !isEmptyAtt) {
                        ariaLabel = atts.getValue(i);
                    } else if ("aria-owns" == attLocal && !isEmptyAtt) {
                        owns = atts.getValue(i);
                    } else if ("list" == attLocal) {
                        list = atts.getValue(i);
                    } else if ("lang" == attLocal) {
                        lang = atts.getValue(i);
                    } else if ("id" == attLocal) {
                        id = atts.getValue(i);
                    } else if ("for" == attLocal && "label" == localName) {
                        forAttr = atts.getValue(i);
                        ancestorMask |= LABEL_FOR_MASK;
                    } else if ("ismap" == attLocal) {
                        ismap = true;
                    } else if ("selected" == attLocal) {
                        selected = true;
                    } else if ("usemap" == attLocal && "input" != localName) {
                        usemap = true;
                    } else if ("itemid" == attLocal) {
                        itemid = true;
                    } else if ("itemref" == attLocal) {
                        itemref = true;
                    } else if ("itemscope" == attLocal) {
                        itemscope = true;
                    } else if ("itemtype" == attLocal) {
                        itemtype = true;
                    } else if ("language" == attLocal
                            && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                    "javascript", atts.getValue(i))) {
                        languageJavaScript = true;
                    } else if ("rev" == attLocal
                            && !("1".equals(System.getProperty(
                                    "nu.validator.schema.rdfa-full")))) {
                        errObsoleteAttribute("rev", localName,
                                " Use the \u201Crel\u201D attribute instead,"
                                        + " with a term having the opposite meaning.");
                    } else if (OBSOLETE_ATTRIBUTES.containsKey(attLocal)
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String[] elementNames = OBSOLETE_ATTRIBUTES.get(
                                attLocal);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            String suggestion = OBSOLETE_ATTRIBUTES_MSG.containsKey(
                                    attLocal)
                                            ? " " + OBSOLETE_ATTRIBUTES_MSG.get(
                                                    attLocal)
                                            : "";
                            errObsoleteAttribute(attLocal, localName,
                                    suggestion);
                        }
                    } else if (OBSOLETE_STYLE_ATTRS.containsKey(attLocal)) {
                        String[] elementNames = OBSOLETE_STYLE_ATTRS.get(
                                attLocal);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            errObsoleteAttribute(attLocal, localName,
                                    " Use CSS instead.");
                        }
                    } else if (INPUT_ATTRIBUTES.containsKey(attLocal)
                            && "input" == localName) {
                        String[] allowedTypes = INPUT_ATTRIBUTES.get(attLocal);
                        inputTypeVal = inputTypeVal == null ? "text"
                                : inputTypeVal;
                        if (Arrays.binarySearch(allowedTypes,
                                inputTypeVal) < 0) {
                            err("Attribute \u201c" + attLocal
                                    + "\u201d is only allowed when the input"
                                    + " type is " + renderTypeList(allowedTypes)
                                    + ".");
                        }
                    } else if ("autofocus" == attLocal) {
                        if (hasAutofocus) {
                            err("A document must not include more than one"
                                    + " \u201Cautofocus\u201D attribute.");
                        }
                        hasAutofocus = true;
                    }
                } else if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    if ("lang" == atts.getLocalName(i)) {
                        xmlLang = atts.getValue(i);
                    }
                }

                if (atts.getType(i) == "ID" || "id" == atts.getLocalName(i)) {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
            }

            if (localName.contains("-")) {
                isCustomElement = true;
                if (atts.getIndex("", "is") > -1) {
                    err("Autonomous custom elements must not specify the"
                            + " \u201cis\u201d attribute.");
                }
                try {
                    CustomElementName.THE_INSTANCE.checkValid(localName);
                } catch (DatatypeException e) {
                    try {
                        if (getErrorHandler() != null) {
                            String msg = e.getMessage();
                            if (e instanceof Html5DatatypeException) {
                                msg = msg.substring(msg.indexOf(": ") + 2);
                            }
                            VnuBadElementNameException ex = new VnuBadElementNameException(
                                    localName, uri, msg, getDocumentLocator(),
                                    CustomElementName.class, false);
                            getErrorHandler().error(ex);
                        }
                    } catch (ClassNotFoundException ce) {
                    }
                }
            }
            if ("input".equals(localName)) {
                if (atts.getIndex("", "name") > -1
                        && "isindex".equals(atts.getValue("", "name"))) {
                    err("The value \u201cisindex\u201d for the \u201cname\u201d"
                            + " attribute of the \u201cinput\u201d element is"
                            + " not allowed.");
                }
                if (atts.getIndex("", "type") > -1
                        && "hidden".equals(atts.getValue("", "type"))
                        && hasAriaAttributesOtherThanAriaHidden) {
                    err("An \u201cinput\u201d element with a \u201ctype\u201d"
                            + " attribute whose value is \u201chidden\u201d"
                            + " must not have any \u201Caria-*\u201D"
                            + " attributes.");
                }
                inputTypeVal = inputTypeVal == null ? "text" : inputTypeVal;
                if (atts.getIndex("", "autocomplete") > -1) {
                    Class<?> datatypeClass = null;
                    String autocompleteVal = atts.getValue("", "autocomplete");
                    try {
                        if ("on".equals(autocompleteVal)
                                || "off".equals(autocompleteVal)) {
                            if ("hidden".equals(inputTypeVal)) {
                                err("An \u201cinput\u201d element with a"
                                        + " \u201ctype\u201d attribute whose"
                                        + " value is \u201chidden\u201d must"
                                        + " not have an"
                                        + " \u201Cautocomplete\u201D attribute"
                                        + " whose value is \u201Con\u201D or"
                                        + " \u201Coff\u201D.");
                            }
                        } else {
                            if ("hidden".equals(inputTypeVal)) {
                                AutocompleteDetailsAny.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsAny.class;
                            } else if ("text".equals(inputTypeVal)
                                    || "search".equals(autocompleteVal)) {
                                AutocompleteDetailsText.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsText.class;
                            } else if ("password".equals(inputTypeVal)) {
                                AutocompleteDetailsPassword.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsPassword.class;
                            } else if ("url".equals(inputTypeVal)) {
                                AutocompleteDetailsUrl.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsUrl.class;
                            } else if ("email".equals(inputTypeVal)) {
                                AutocompleteDetailsEmail.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsEmail.class;
                            } else if ("tel".equals(inputTypeVal)) {
                                AutocompleteDetailsTel.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsTel.class;
                            } else if ("number".equals(inputTypeVal)) {
                                AutocompleteDetailsNumeric.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsNumeric.class;
                            } else if ("month".equals(inputTypeVal)) {
                                AutocompleteDetailsMonth.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsMonth.class;
                            } else if ("date".equals(inputTypeVal)) {
                                AutocompleteDetailsDate.THE_INSTANCE.checkValid(
                                        autocompleteVal);
                                datatypeClass = AutocompleteDetailsDate.class;
                            }
                        }
                    } catch (DatatypeException e) {
                        try {
                            if (getErrorHandler() != null) {
                                String msg = e.getMessage();
                                msg = msg.substring(msg.indexOf(": ") + 2);
                                VnuBadAttrValueException ex = new VnuBadAttrValueException(
                                        localName, uri, "autocomplete",
                                        autocompleteVal, msg,
                                        getDocumentLocator(), datatypeClass,
                                        false);
                                getErrorHandler().error(ex);
                            }
                        } catch (ClassNotFoundException ce) {
                        }
                    }
                }
            }
            if ("img".equals(localName) || "source".equals(localName)
                    || "link".equals(localName)) {
                String srcSetName = "link".equals(localName) ? "imagesrcset"
                        : "srcset";
                String sizesName = "link".equals(localName) ? "imagesizes"
                        : "sizes";
                if (atts.getIndex("", srcSetName) > -1) {
                    String srcsetVal = atts.getValue("", srcSetName);
                    try {
                        if (atts.getIndex("", sizesName) > -1) {
                            ImageCandidateStringsWidthRequired.THE_INSTANCE.checkValid(
                                    srcsetVal);
                        } else {
                            ImageCandidateStrings.THE_INSTANCE.checkValid(
                                    srcsetVal);
                        }
                        // see nu.validator.datatype.ImageCandidateStrings
                        if ("1".equals(System.getProperty(
                                "nu.validator.checker.imageCandidateString.hasWidth"))) {
                            if (atts.getIndex("", sizesName) < 0) {
                                err("When the \u201c" + srcSetName
                                        + "\u201d attribute has any image"
                                        + " candidate string with a width"
                                        + " descriptor, the \u201c" + sizesName
                                        + "\u201d attribute must"
                                        + " also be specified.");
                            }
                        }
                    } catch (DatatypeException e) {
                        Class<?> datatypeClass = ImageCandidateStrings.class;
                        if (atts.getIndex("", sizesName) > -1) {
                            datatypeClass = ImageCandidateStringsWidthRequired.class;
                        }
                        try {
                            if (getErrorHandler() != null) {
                                String msg = e.getMessage();
                                if (e instanceof Html5DatatypeException) {
                                    Html5DatatypeException ex5 = (Html5DatatypeException) e;
                                    if (!ex5.getDatatypeClass().equals(
                                            ImageCandidateURL.class)) {
                                        msg = msg.substring(
                                                msg.indexOf(": ") + 2);
                                    }
                                }
                                VnuBadAttrValueException ex = new VnuBadAttrValueException(
                                        localName, uri, srcSetName, srcsetVal,
                                        msg, getDocumentLocator(),
                                        datatypeClass, false);
                                getErrorHandler().error(ex);
                            }
                        } catch (ClassNotFoundException ce) {
                        }
                    }
                    if ("picture".equals(parentName)
                            && !siblingSources.isEmpty()) {
                        for (Map.Entry<Locator, Map<String, String>> entry : siblingSources.entrySet()) {
                            Locator locator = entry.getKey();
                            Map<String, String> sourceAtts = entry.getValue();
                            String media = sourceAtts.get("media");
                            if (media == null
                                    && sourceAtts.get("type") == null) {
                                err("A \u201csource\u201d element that has a"
                                        + " following sibling"
                                        + " \u201csource\u201d element or"
                                        + " \u201cimg\u201d element with a"
                                        + " \u201c" + srcSetName + "\u201d attribute"
                                        + " must have a"
                                        + " \u201cmedia\u201d attribute and/or"
                                        + " \u201ctype\u201d attribute.",
                                        locator);
                                siblingSources.remove(locator);
                            } else if (media != null
                                    && "".equals(trimSpaces(media))) {
                                err("Value of \u201cmedia\u201d attribute here"
                                        + " must not be empty.",
                                        locator);
                            } else if (media != null
                                    && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                            "all", trimSpaces(media))) {
                                err("Value of \u201cmedia\u201d attribute here"
                                        + " must not be \u201call\u201d.",
                                        locator);
                            }
                        }
                    }
                } else if (atts.getIndex("", sizesName) > -1) {
                    err("The \u201c" + sizesName + "\u201d attribute must only"
                            + " be specified if the \u201c" + srcSetName
                            + "\u201d attribute is also specified.");
                }
            }

            if ("picture".equals(parentName) && "source".equals(localName)) {
                Map<String, String> sourceAtts = new HashMap<>();
                for (int i = 0; i < atts.getLength(); i++) {
                    sourceAtts.put(atts.getLocalName(i), atts.getValue(i));
                }
                siblingSources.put((new LocatorImpl(getDocumentLocator())),
                        sourceAtts);
            }

            if ("figure" == localName) {
                currentFigurePtr = currentPtr + 1;
            }
            if ("caption" == localName && "table" == parentName
                    && stack.length >= currentPtr - 1
                    && "figure" == stack[currentPtr - 1].getName()) {
                stack[currentPtr - 1].setCaptionNestedInFigure(
                        new LocatorImpl(getDocumentLocator()));
            }
            if ((ancestorMask & FIGURE_MASK) != 0) {
                if ("img" == localName) {
                    if (stack[currentFigurePtr].hasImg()) {
                        stack[currentFigurePtr].setEmbeddedContentFound();
                    } else {
                        stack[currentFigurePtr].setImgFound();
                    }
                } else if ("audio" == localName || "canvas" == localName
                        || "embed" == localName || "iframe" == localName
                        || "math" == localName || "object" == localName
                        || "svg" == localName || "video" == localName) {
                    stack[currentFigurePtr].setEmbeddedContentFound();
                }
            }
            if ("article" == localName || "aside" == localName
                    || "nav" == localName || "section" == localName) {
                sectioningElementPtrs.add(currentPtr + 1);
            }
            if ("h1" == localName || "h2" == localName || "h3" == localName
                    || "h4" == localName || "h5" == localName
                    || "h6" == localName) {
                currentHeadingPtr = currentPtr + 1;
                if (!sectioningElementPtrs.isEmpty()) {
                    stack[sectioningElementPtrs.peekLast()].setHeadingFound();
                }
            }
            if (((ancestorMask & H1_MASK) != 0 || (ancestorMask & H2_MASK) != 0
                    || (ancestorMask & H3_MASK) != 0
                    || (ancestorMask & H4_MASK) != 0
                    || (ancestorMask & H5_MASK) != 0
                    || (ancestorMask & H6_MASK) != 0) && "img" == localName
                    && atts.getIndex("", "alt") > -1
                    && !"".equals(atts.getValue("", "alt"))) {
                stack[currentHeadingPtr].setImgFound();
            }

            if ("option" == localName && !parent.hasOption()) {
                if (atts.getIndex("", "aria-selected") > -1
                        && !"".equals(atts.getValue("", "aria-selected"))) {
                    warn("The \u201Caria-selected\u201D attribute should not be"
                            + " used on the \u201Coption\u201D element.");
                }
                if (atts.getIndex("", "value") < 0) {
                    parent.setNoValueOptionFound();
                } else if (atts.getIndex("", "value") > -1
                        && "".equals(atts.getValue("", "value"))) {
                    parent.setEmptyValueOptionFound();
                } else {
                    parent.setNonEmptyOption(
                            (new LocatorImpl(getDocumentLocator())));
                }
            }

            // Obsolete elements
            if (OBSOLETE_ELEMENTS.get(localName) != null) {
                String suggestion = "";
                if (!"".equals(OBSOLETE_ELEMENTS.get(localName))) {
                    suggestion = " " + OBSOLETE_ELEMENTS.get(localName);
                }
                err("The \u201C" + localName + "\u201D element is obsolete."
                        + suggestion);
            }

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "The element \u201C" + localName
                    + "\u201D";
            if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(
                    localName)) != null) {
                mask = maskAsObject.intValue();
            } else if ("video" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element \u201Cvideo\u201D with the"
                        + " attribute \u201Ccontrols\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("audio" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element \u201Caudio\u201D with the"
                        + " attribute \u201Ccontrols\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("menu" == localName && toolbar) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element \u201Cmenu\u201D with the"
                        + " attribute \u201Ctype=toolbar\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("img" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element \u201Cimg\u201D with the"
                        + " attribute \u201Cusemap\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("object" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element \u201Cobject\u201D with the"
                        + " attribute \u201Cusemap\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("input" == localName && !hidden) {
                mask = A_BUTTON_MASK;
                checkForInteractiveAncestorRole(descendantUiString);
            } else if (tabindex) {
                mask = A_BUTTON_MASK;
                descendantUiString = "An element with the attribute"
                        + " \u201Ctabindex\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if (role != null && role != ""
                    && Arrays.binarySearch(INTERACTIVE_ROLES, role) >= 0) {
                mask = A_BUTTON_MASK;
                descendantUiString = "An element with the attribute \u201C"
                        + "role=" + role + "\u201D";
                checkForInteractiveAncestorRole(descendantUiString);
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (String ancestor : SPECIAL_ANCESTORS) {
                        if ((maskHit & 1) != 0) {
                            err(descendantUiString + " must not appear as a"
                                    + " descendant of the \u201C" + ancestor
                                    + "\u201D element.");
                        }
                        maskHit >>= 1;
                    }
                }
            }
            if (Arrays.binarySearch(INTERACTIVE_ELEMENTS, localName) >= 0) {
                checkForInteractiveAncestorRole(
                        "The element \u201C" + localName + "\u201D");
            }

            // Ancestor requirements/restrictions
            if ("area" == localName && ((ancestorMask & MAP_MASK) == 0)) {
                err("The \u201Carea\u201D element must have a \u201Cmap\u201D ancestor.");
            } else if ("img" == localName) {
                List<String> roles = null;
                if (role != null) {
                    roles = Arrays.asList(role.trim() //
                            .toLowerCase().split("\\s+"));
                }
                String titleVal = atts.getValue("", "title");
                if (ismap && ((ancestorMask & HREF_MASK) == 0)) {
                    err("The \u201Cimg\u201D element with the "
                            + "\u201Cismap\u201D attribute set must have an "
                            + "\u201Ca\u201D ancestor with the "
                            + "\u201Chref\u201D attribute.");
                }
                if (atts.getIndex("", "alt") < 0) {
                    if (role != null) {
                        err("An \u201Cimg\u201D element with no \u201Calt\u201D"
                                + " attribute must not have a"
                                + " \u201Crole\u201D attribute.");
                    }
                    if (hasAriaAttributesOtherThanAriaHidden) {
                        err("An \u201Cimg\u201D element with no \u201Calt\u201D"
                                + " attribute must not have any"
                                + " \u201Caria-*\u201D attributes other than"
                                + " \u201Caria-hidden\u201D.");
                    }
                    if ((titleVal == null || "".equals(titleVal))) {
                        if ((ancestorMask & FIGURE_MASK) == 0) {
                            err("An \u201Cimg\u201D element must have an"
                                    + " \u201Calt\u201D attribute, except under"
                                    + " certain conditions. For details, consult"
                                    + " guidance on providing text alternatives"
                                    + " for images.");
                        } else {
                            stack[currentFigurePtr].setFigcaptionNeeded();
                            stack[currentFigurePtr].addImageLackingAlt(
                                    new LocatorImpl(getDocumentLocator()));
                        }
                    }
                } else if (role != null) {
                    if ("".equals(atts.getValue("", "alt"))) {
                        // img with alt="" and role
                        err("An \u201Cimg\u201D element which has an"
                                + " \u201Calt\u201D attribute whose value is"
                                + " the empty string must not have a"
                                + " \u201Crole\u201D attribute.");
                    } else {
                        // img with alt="some text" and role
                        for (String roleValue : roles) {
                            if ("none".equals(roleValue)
                                    || "presentation".equals(roleValue)) {
                                err("Bad value \u201C" + roleValue + "\u201D"
                                        + " for attribute \u201Crole\u201D on"
                                        + " element \u201Cimg\u201D");
                            }
                        }
                    }
                }
            } else if ("table" == localName) {
                if (role == null // implicit role=table
                        || "table".equals(role) || "grid".equals(role)
                        || "treegrid".equals(role)) {
                    hasAncestorTableIsRoleTableGridOrTreeGrid = true;
                }
                if (atts.getIndex("", "summary") >= 0) {
                    errObsoleteAttribute("summary", "table",
                            " Consider describing the structure of the"
                                    + " \u201Ctable\u201D in a \u201Ccaption\u201D "
                                    + " element or in a \u201Cfigure\u201D element "
                                    + " containing the \u201Ctable\u201D; or,"
                                    + " simplify the structure of the"
                                    + " \u201Ctable\u201D so that no description"
                                    + " is needed.");
                }
                if (atts.getIndex("", "border") > -1) {
                    errObsoleteAttribute("border", "table",
                            " Use CSS instead.");
                }
            } else if (hasAncestorTableIsRoleTableGridOrTreeGrid
                    && atts.getIndex("", "role") >= 0 && ("td" == localName
                            || "tr" == localName || "th" == localName)) {
                err("The \u201Crole\u201D attribute must not be used"
                        + " on a \u201C" + localName + "\u201D element"
                        + " which has a \u201Ctable\u201D ancestor with"
                        + " no \u201Crole\u201D attribute, or with a"
                        + " \u201Crole\u201D attribute whose value is"
                        + " \u201Ctable\u201D, \u201Cgrid\u201D,"
                        + " or \u201Ctreegrid\u201D.");
            } else if ("track" == localName
                    && atts.getIndex("", "default") >= 0) {
                for (Map.Entry<StackNode, TaintableLocatorImpl> entry : openMediaElements.entrySet()) {
                    StackNode node = entry.getKey();
                    TaintableLocatorImpl locator = entry.getValue();
                    if (node.isTrackDescendant()) {
                        err("The \u201Cdefault\u201D attribute must not occur"
                                + " on more than one \u201Ctrack\u201D element"
                                + " within the same \u201Caudio\u201D or"
                                + " \u201Cvideo\u201D element.");
                        if (!locator.isTainted()) {
                            warn("\u201Caudio\u201D or \u201Cvideo\u201D element"
                                    + " has more than one \u201Ctrack\u201D child"
                                    + " element with a \u201Cdefault\u201D attribute.",
                                    locator);
                            locator.markTainted();
                        }
                    } else {
                        node.setTrackDescendants();
                    }
                }
            } else if ("hgroup" == localName) {
                incrementUseCounter("hgroup-found");
            } else if ("main" == localName) {
                for (int i = 0; i < currentPtr; i++) {
                    String ancestorName = stack[currentPtr - i].getName();
                    if (ancestorName != null
                            && Arrays.binarySearch(PROHIBITED_MAIN_ANCESTORS,
                                    ancestorName) >= 0) {
                        err("The \u201Cmain\u201D element must not appear as a"
                                + " descendant of the \u201C" + ancestorName
                                + "\u201D element.");
                    }
                }
                if (atts.getIndex("", "hidden") < 0) {
                    if (hasVisibleMain) {
                        err("A document must not include more than one visible"
                                + " \u201Cmain\u201D element.");
                    }
                    hasVisibleMain = true;
                }
            } else if ("h1" == localName) {
                if (sectioningElementPtrs.size() > 1) {
                    warn(h1WarningMessage);
                } else if (sectioningElementPtrs.size() == 1) {
                    secondLevelH1s.add(new LocatorImpl(getDocumentLocator()));
                } else {
                    hasTopLevelH1 = true;
                }
            }

            // progress
            else if ("progress" == localName) {
                double value = getDoubleAttribute(atts, "value");
                if (!Double.isNaN(value)) {
                    double max = getDoubleAttribute(atts, "max");
                    if (Double.isNaN(max)) {
                        if (!(value <= 1.0)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                        }
                    } else {
                        if (!(value <= max)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                        }
                    }
                }
                if (atts.getIndex("", "aria-valuemax") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemax"))) {
                    if (atts.getIndex("", "max") >= 0) {
                        err("The \u201Caria-valuemax\u201D attribute must not"
                                + " be used on an element which has a"
                                + " \u201Cmax\u201D attribute.");
                    } else {
                        warn("The \u201Caria-valuemax\u201D attribute should"
                                + " not be used on a \u201Cprogress\u201D"
                                + " element.");
                    }
                }
            }

            // meter
            else if ("meter" == localName) {
                double value = getDoubleAttribute(atts, "value");
                double min = getDoubleAttribute(atts, "min");
                double max = getDoubleAttribute(atts, "max");
                double optimum = getDoubleAttribute(atts, "optimum");
                double low = getDoubleAttribute(atts, "low");
                double high = getDoubleAttribute(atts, "high");
                if (!Double.isNaN(min) && !Double.isNaN(value)
                        && !(min <= value)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cvalue\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(value)
                        && !(0 <= value)) {
                    err("The value of the \u201Cvalue\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(value) && !Double.isNaN(max)
                        && !(value <= max)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(value) && Double.isNaN(max)
                        && !(value <= 1)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && !(min <= max)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(max) && !(0 <= max)) {
                    err("The value of the \u201Cmax\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && Double.isNaN(max) && !(min <= 1)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(low) && !(min <= low)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Clow\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(low) && !(0 <= low)) {
                    err("The value of the \u201Clow\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(high)
                        && !(min <= high)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(high) && !(0 <= high)) {
                    err("The value of the \u201Chigh\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(high)
                        && !(low <= high)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (!Double.isNaN(high) && !Double.isNaN(max)
                        && !(high <= max)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(high) && Double.isNaN(max) && !(high <= 1)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(max) && !(low <= max)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(low) && Double.isNaN(max) && !(low <= 1)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(min <= optimum)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Coptimum\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(0 <= optimum)) {
                    err("The value of the \u201Coptimum\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(optimum) && !Double.isNaN(max)
                        && !(optimum <= max)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(optimum) && Double.isNaN(max)
                        && !(optimum <= 1)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (atts.getIndex("", "aria-valuemin") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemin"))) {
                    if (atts.getIndex("", "min") >= 0) {
                        err("The \u201Caria-valuemin\u201D attribute must not"
                                + " be used on an element which has a"
                                + " \u201Cmin\u201D attribute.");
                    } else {
                        warn("The \u201Caria-valuemin\u201D attribute should"
                                + " not be used on a \u201Cmeter\u201D"
                                + " element.");
                    }
                }
                if (atts.getIndex("", "aria-valuemax") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemax"))) {
                    if (atts.getIndex("", "max") >= 0) {
                        err("The \u201Caria-valuemax\u201D attribute must not"
                                + " be used on an element which has a"
                                + " \u201Cmax\u201D attribute.");
                    } else {
                        warn("The \u201Caria-valuemax\u201D attribute should"
                                + " not be used on a \u201Cmeter\u201D"
                                + " element.");
                    }
                }
            }

            // map required attrs
            else if ("map" == localName && id != null) {
                String nameVal = atts.getValue("", "name");
                if (nameVal != null && !nameVal.equals(id)) {
                    err("The \u201Cid\u201D attribute on a \u201Cmap\u201D element must have an the same value as the \u201Cname\u201D attribute.");
                }
            }

            else if ("object" == localName) {
                if (atts.getIndex("", "typemustmatch") >= 0) {
                    if ((atts.getIndex("", "data") < 0)
                            || (atts.getIndex("", "type") < 0)) {
                        {
                            err("Element \u201Cobject\u201D must not have"
                                    + " attribute \u201Ctypemustmatch\u201D unless"
                                    + " both attribute \u201Cdata\u201D"
                                    + " and attribute \u201Ctype\u201D are also specified.");
                        }
                    }
                }
            }
            else if ("form" == localName) {
                if (atts.getIndex("", "accept-charset") >= 0) {
                    if (!"utf-8".equals(
                            atts.getValue("", "accept-charset").toLowerCase())) {
                        err("The only allowed value for the"
                                + " \u201Caccept-charset\u201D attribute for"
                                + " the \u201Cform\u201D element is"
                                + " \u201Cutf-8\u201D.");
                    }
                }
            }
            // script
            else if ("script" == localName) {
                // script language
                if (languageJavaScript && typeNotTextJavaScript) {
                    err("A \u201Cscript\u201D element with the \u201Clanguage=\"JavaScript\"\u201D attribute set must not have a \u201Ctype\u201D attribute whose value is not \u201Ctext/javascript\u201D.");
                }
                if (atts.getIndex("", "charset") >= 0) {
                    warnObsoleteAttribute("charset", "script", "");
                    if (!"utf-8".equals(
                            atts.getValue("", "charset").toLowerCase())) {
                        err("The only allowed value for the \u201Ccharset\u201D"
                                + " attribute for the \u201Cscript\u201D"
                                + " element is \u201Cutf-8\u201D. (But the"
                                + " attribute is not needed and should be"
                                + " omitted altogether.)");
                    }
                }
                // src-less script
                if (atts.getIndex("", "src") < 0) {
                    if (atts.getIndex("", "charset") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute \u201Ccharset\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute \u201Cdefer\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                    if (atts.getIndex("", "async") >= 0) {
                        if (!(atts.getIndex("", "type") > -1 && //
                                "module".equals(atts.getValue("", "type") //
                                        .toLowerCase()))) {
                            err("Element \u201Cscript\u201D must not have"
                                    + " attribute \u201Casync\u201D unless"
                                    + " attribute \u201Csrc\u201D is also"
                                    + " specified or unless attribute"
                                    + " \u201Ctype\u201D is specified with"
                                    + " value \u201Cmodule\u201D.");
                        }
                    }
                    if (atts.getIndex("", "integrity") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute"
                                + " \u201Cintegrity\u201D unless attribute"
                                + " \u201Csrc\u201D is also specified.");
                    }
                    if (atts.getIndex("", "fetchpriority") > -1) {
                        warn("Element \u201Cscript\u201D should not have attribute"
                                     + " \u201Cfetchpriority\u201D unless attribute"
                                     + " \u201Csrc\u201D is also specified.");
                    }
                }
                if (atts.getIndex("", "type") > -1) {
                    String scriptType = atts.getValue("", "type").toLowerCase();
                    if (JAVASCRIPT_MIME_TYPES.contains(scriptType)
                            || "".equals(scriptType)) {
                        warn("The \u201Ctype\u201D attribute is unnecessary for"
                                + " JavaScript resources.");
                    } else if ("module".equals(scriptType)) {
                        if (atts.getIndex("", "defer") > -1) {
                            err("A \u201Cscript\u201D element with a"
                                    + " \u201Cdefer\u201D attribute must not have a"
                                    + " \u201Ctype\u201D attribute with the value"
                                    + " \u201Cmodule\u201D.");
                        }
                        if (atts.getIndex("", "nomodule") > -1) {
                            err("A \u201Cscript\u201D element with a"
                                    + " \u201Cnomodule\u201D attribute must not have a"
                                    + " \u201Ctype\u201D attribute with the value"
                                    + " \u201Cmodule\u201D.");
                        }
                    } else if ("importmap".equals(scriptType)) {
                        if (atts.getIndex("", "src") > -1) {
                            err("A \u201cscript\u201d element with a"
                                    + " \u201ctype\u201d attribute whose value"
                                    + " is \u201cimportmap\u201d must not have"
                                    + " a \u201Csrc\u201D attribute.");
                        }
                        parsingScriptImportMap = true;
                    }
                }
            }
            else if ("style" == localName) {
                if (atts.getIndex("", "type") > -1) {
                    String styleType = atts.getValue("", "type").toLowerCase();
                    if ("text/css".equals(styleType)) {
                        warn("The \u201Ctype\u201D attribute for the"
                                + " \u201Cstyle\u201D element is not needed and"
                                + " should be omitted.");
                    } else {
                        err("The only allowed value for the \u201Ctype\u201D"
                                + " attribute for the \u201Cstyle\u201D element"
                                + " is \u201Ctext/css\u201D (with no"
                                + " parameters). (But the attribute is not"
                                + " needed and should be omitted altogether.)");
                    }
                }
            }

            // bdo required attrs
            else if ("bdo" == localName && atts.getIndex("", "dir") < 0) {
                err("Element \u201Cbdo\u201D must have attribute \u201Cdir\u201D.");
            }

            // labelable elements
            if (isLabelableElement(localName, atts)) {
                for (Map.Entry<StackNode, Locator> entry : openLabels.entrySet()) {
                    StackNode node = entry.getKey();
                    Locator locator = entry.getValue();
                    if (node.isLabeledDescendants()) {
                        err("The \u201Clabel\u201D element may contain at most"
                                + " one \u201Cbutton\u201D, \u201Cinput\u201D,"
                                + " \u201Cmeter\u201D, \u201Coutput\u201D,"
                                + " \u201Cprogress\u201D, \u201Cselect\u201D,"
                                + " or \u201Ctextarea\u201D descendant.");
                        warn("\u201Clabel\u201D element with multiple labelable"
                                + " descendants.", locator);
                    } else {
                        node.setLabeledDescendants();
                    }
                }
                if ((ancestorMask & LABEL_FOR_MASK) != 0) {
                    boolean hasMatchingFor = false;
                    for (int i = 0; (stack[currentPtr - i].getAncestorMask()
                            & LABEL_FOR_MASK) != 0; i++) {
                        String forVal = stack[currentPtr - i].getForAttr();
                        if (forVal != null && forVal.equals(id)) {
                            hasMatchingFor = true;
                            break;
                        }
                    }
                    if (id == null || !hasMatchingFor) {
                        err("Any \u201C" + localName
                                + "\u201D descendant of a \u201Clabel\u201D"
                                + " element with a \u201Cfor\u201D attribute"
                                + " must have an ID value that matches that"
                                + " \u201Cfor\u201D attribute.");
                    }
                }
            }

            // lang and xml:lang for XHTML5
            if (lang != null && xmlLang != null
                    && !equalsIgnoreAsciiCase(lang, xmlLang)) {
                err("When the attribute \u201Clang\u201D in no namespace and the attribute \u201Clang\u201D in the XML namespace are both present, they must have the same value.");
            }

            if (role != null && owns != null) {
                for (Set<String> value : REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.values()) {
                    if (value.contains(role)) {
                        String[] ownedIds = AttributeUtil.split(owns);
                        for (String ownedId : ownedIds) {
                            Set<String> ownedIdsForThisRole = ariaOwnsIdsByRole.get(
                                    role);
                            if (ownedIdsForThisRole == null) {
                                ownedIdsForThisRole = new HashSet<>();
                            }
                            ownedIdsForThisRole.add(ownedId);
                            ariaOwnsIdsByRole.put(role, ownedIdsForThisRole);
                        }
                        break;
                    }
                }
            }
            if ("datalist" == localName) {
                listIds.addAll(ids);
            }

            // label for
            if ("label" == localName) {
                String forVal = atts.getValue("", "for");
                if (forVal != null) {
                    formControlReferences.add(new IdrefLocator(
                            new LocatorImpl(getDocumentLocator()), forVal));
                }
            }

            if ("form" == localName) {
                formElementIds.addAll(ids);
            }

            if (("button" == localName //
                    || "input" == localName && !hidden) //
                    || "meter" == localName //
                    || "output" == localName //
                    || "progress" == localName //
                    || "select" == localName //
                    || "textarea" == localName //
                    || isCustomElement) {
                formControlIds.addAll(ids);
            }

            if ("button" == localName || "fieldset" == localName
                    || ("input" == localName && !hidden)
                    || "object" == localName || "output" == localName
                    || "select" == localName || "textarea" == localName) {
                String formVal = atts.getValue("", "form");
                if (formVal != null) {
                    formElementReferences.add(new IdrefLocator(
                            new LocatorImpl(getDocumentLocator()), formVal));
                }
            }

            // input list
            if ("input" == localName && list != null) {
                listReferences.add(new IdrefLocator(
                        new LocatorImpl(getDocumentLocator()), list));
            }

            // input@type=checkbox
            if ("input" == localName
                    && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                            "checkbox", atts.getValue("", "type"))) {
                if ("button".equals(role)
                        && !"true".equals(atts.getValue("", "aria-pressed"))) {
                    err("An \u201Cinput\u201D element with a \u201Ctype\u201D"
                            + " attribute whose value is \u201Ccheckbox\u201D"
                            + " and with a \u201Crole\u201D attribute whose"
                            + " value is \u201Cbutton\u201D must have an"
                            + " \u201Caria-pressed\u201D attribute whose value"
                            + " is \u201Ctrue\u201D.");
                }
            }

            // input@type=button
            if ("input" == localName
                    && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                            "button", atts.getValue("", "type"))) {
                if (atts.getValue("", "value") == null
                        || "".equals(atts.getValue("", "value"))) {
                    err("Element \u201Cinput\u201D with attribute \u201Ctype\u201D whose value is \u201Cbutton\u201D must have non-empty attribute \u201Cvalue\u201D.");
                }
            }

            // track
            if ("track" == localName) {
                if ("".equals(atts.getValue("", "label"))) {
                    err("Attribute \u201Clabel\u201D for element \u201Ctrack\u201D must have non-empty value.");
                }
            }

            // multiple selected options
            if ("option" == localName && selected) {
                for (Map.Entry<StackNode, Locator> entry : openSingleSelects.entrySet()) {
                    StackNode node = entry.getKey();
                    if (node.isSelectedOptions()) {
                        err("The \u201Cselect\u201D element cannot have more than one selected \u201Coption\u201D descendant unless the \u201Cmultiple\u201D attribute is specified.");
                    } else {
                        node.setSelectedOptions();
                    }
                }
            }
            if ("meta" == localName) {
                if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "content-language", atts.getValue("", "http-equiv"))) {
                    err("Using the \u201Cmeta\u201D element to specify the"
                            + " document-wide default language is obsolete."
                            + " Consider specifying the language on the root"
                            + " element instead.");
                } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "x-ua-compatible", atts.getValue("", "http-equiv"))
                        && !AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "ie=edge", atts.getValue("", "content"))) {
                    err("A \u201Cmeta\u201D element with an"
                            + " \u201Chttp-equiv\u201D attribute whose value is"
                            + " \u201CX-UA-Compatible\u201D" + " must have a"
                            + " \u201Ccontent\u201D attribute with the value"
                            + " \u201CIE=edge\u201D.");
                }
                if (atts.getIndex("", "charset") > -1) {
                    if (!"utf-8".equals(
                            atts.getValue("", "charset").toLowerCase())) {
                        err("The only allowed value for the \u201Ccharset\u201D"
                                + " attribute for the \u201Cmeta\u201D"
                                + " element is \u201Cutf-8\u201D.");
                    }
                    if (hasMetaCharset) {
                        err("A document must not include more than one"
                                + " \u201Cmeta\u201D element with a"
                                + " \u201Ccharset\u201D attribute.");
                    }
                    if (hasContentTypePragma) {
                        err("A document must not include both a"
                                + " \u201Cmeta\u201D element with an"
                                + " \u201Chttp-equiv\u201D attribute"
                                + " whose value is \u201Ccontent-type\u201D,"
                                + " and a \u201Cmeta\u201D element with a"
                                + " \u201Ccharset\u201D attribute.");
                    }
                    hasMetaCharset = true;
                }
                if (atts.getIndex("", "name") > -1) {
                    if ("description".equals(atts.getValue("", "name"))) {
                        if (hasMetaDescription) {
                            err("A document must not include more than one"
                                    + " \u201Cmeta\u201D element with its"
                                    + " \u201Cname\u201D attribute set to the"
                                    + " value \u201Cdescription\u201D.");
                        }
                        hasMetaDescription = true;
                    }
                    if ("viewport".equals(atts.getValue("", "name"))
                            && atts.getIndex("", "content") > -1) {
                        String contentVal = atts.getValue("",
                                "content").toLowerCase();
                        if (contentVal.contains("user-scalable=no")
                                || contentVal.contains("maximum-scale=1.0")) {
                            warn("Consider avoiding viewport values that"
                                    + " prevent users from resizing documents.");
                        }
                    }
                    if ("theme-color".equals(atts.getValue("", "name"))
                            && atts.getIndex("", "content") > -1) {
                        String contentVal = atts.getValue("",
                                "content").toLowerCase();
                        try {
                            Color.THE_INSTANCE.checkValid(contentVal);
                        } catch (DatatypeException e) {
                            try {
                                if (getErrorHandler() != null) {
                                    String msg = e.getMessage();
                                    if (e instanceof Html5DatatypeException) {
                                        msg = msg.substring(
                                                msg.indexOf(": ") + 2);
                                    }
                                    VnuBadAttrValueException ex = //
                                            new VnuBadAttrValueException(
                                                    localName, uri, "content",
                                                    contentVal, msg,
                                                    getDocumentLocator(),
                                                    Color.class, false);
                                    getErrorHandler().error(ex);
                                }
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                    }
                }
                if (atts.getIndex("", "http-equiv") > -1
                        && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "content-type",
                                atts.getValue("", "http-equiv"))) {
                    if (hasMetaCharset) {
                        err("A document must not include both a"
                                + " \u201Cmeta\u201D element with an"
                                + " \u201Chttp-equiv\u201D attribute"
                                + " whose value is \u201Ccontent-type\u201D,"
                                + " and a \u201Cmeta\u201D element with a"
                                + " \u201Ccharset\u201D attribute.");
                    }
                    if (hasContentTypePragma) {
                        err("A document must not include more than one"
                                + " \u201Cmeta\u201D element with a"
                                + " \u201Chttp-equiv\u201D attribute"
                                + " whose value is \u201Ccontent-type\u201D.");
                    }
                    hasContentTypePragma = true;
                }
                if (atts.getIndex("", "media") > -1
                        && (atts.getIndex("", "name") <= -1
                                || !atts.getValue("", "name").equalsIgnoreCase(
                                        "theme-color"))) {
                    err("A \u201Cmeta\u201D element with a \u201Cmedia\u201D"
                            + " attribute must have a \u201Cname\u201D"
                            + " attribute whose value is"
                            + " \u201Ctheme-color\u201D.");
                }
            }
            if ("link" == localName) {
                boolean hasRel = false;
                List<String> relList = new ArrayList<>();
                if (atts.getIndex("", "rel") > -1) {
                    hasRel = true;
                    Collections.addAll(relList, //
                            atts.getValue("", "rel") //
                            .toLowerCase().split("\\s+"));
                }
                if (atts.getIndex("", "href") == -1
                        && atts.getIndex("", "imagesrcset") == -1
                        && atts.getIndex("", "resource") == -1) { //rdfa
                    err("A \u201Clink\u201D element must have an"
                                + " \u201Chref\u201D or \u201Cimagesrcset\u201D"
                                + " attribute, or both.");
                }
                if (relList.contains("preload")
                        && atts.getIndex("", "as") < 0) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cpreload\u201D must have an"
                            + " \u201Cas\u201D attribute.");
                }
                if (atts.getIndex("", "as") > -1 //
                        && (!(relList.contains("preload")
                                || relList.contains("modulepreload")) //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with an"
                            + " \u201Cas\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cpreload\u201D or the value"
                            + " \u201Cmodulepreload\u201D.");
                }
                if (atts.getIndex("", "integrity") > -1
                        && (!(relList.contains("stylesheet")
                                || relList.contains("preload")
                                || relList.contains("modulepreload")) //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with an"
                            + " \u201Cintegrity\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cstylesheet\u201D or the value"
                            + " \u201Cpreload\u201D or the value"
                            + " \u201Cmodulepreload\u201D.");
                }
                if (atts.getIndex("", "disabled") > -1
                        && (!relList.contains("stylesheet") //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Cdisabled\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cstylesheet\u201D.");
                }
                if (atts.getIndex("", "sizes") > -1
                        && (!(relList.contains("icon")
                                || relList.contains("apple-touch-icon")
                                || relList.contains(
                                        "apple-touch-icon-precomposed"))
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Csizes\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cicon\u201D or the value"
                            + " \u201Capple-touch-icon\u201D or the value"
                            + " \u201Capple-touch-icon-precomposed\u201D.");
                }
                if (atts.getIndex("", "color") > -1
                        && (!relList.contains("mask-icon") //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Ccolor\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains"
                            + " the value \u201Cmask-icon\u201D.");
                }
                if (atts.getIndex("", "scope") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Cscope\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cserviceworker\u201D.");
                }
                if (atts.getIndex("", "updateviacache") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with an"
                            + " \u201Cupdateviacache\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cserviceworker\u201D.");
                }
                if (atts.getIndex("", "workertype") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A \u201Clink\u201D element with a"
                            + " \u201Cworkertype\u201D attribute must have a"
                            + " \u201Crel\u201D attribute that contains the"
                            + " value \u201Cserviceworker\u201D.");
                }
                if (atts.getIndex("", "imagesrcset") > -1
                        && !(relList.contains("preload")
                        || !hasRel)) {
                    err("A \u201Clink\u201D element with an"
                                + " \u201Cimagesrcset\u201D attribute must have a"
                                + " \u201Crel\u201D attribute that contains the"
                                + " value \u201Cpreload\u201D.");
                }
                if (atts.getIndex("", "imagesizes") > -1
                        && !(relList.contains("preload")
                        || !hasRel)) {
                    err("A \u201Clink\u201D element with an"
                                + " \u201Cimagesizes\u201D attribute must have a"
                                + " \u201Crel\u201D attribute that contains the"
                                + " value \u201Cpreload\u201D.");
                }
                if (atts.getIndex("", "imagesrcset") > -1
                        && (atts.getIndex("", "as") == -1
                        || !atts.getValue("", "as").equalsIgnoreCase("image"))) {
                    err("A \u201Clink\u201D element with an"
                                + " \u201Cimagesrcset\u201D attribute must have an"
                                + " \u201Cas\u201D attribute with value \u201Cimage\u201D.");
                }
                if (atts.getIndex("", "imagesizes") > -1
                        && (atts.getIndex("", "as") == -1
                        || !atts.getValue("", "as").equalsIgnoreCase("image"))) {
                    err("A \u201Clink\u201D element with an"
                                + " \u201Cimagesizes\u201D attribute must have an"
                                + " \u201Cas\u201D attribute with value \u201Cimage\u201D.");
                }
                if (relList.contains("alternate")
                        && relList.contains("stylesheet")
                        && (atts.getIndex("", "title") == -1
                            || "".equals(atts.getValue("", "title")))) {
                    err("A \u201Clink\u201D element with a"
                                + " \u201Crel\u201D attribute that"
                                + " contains both the values"
                                + " \u201Calternate\u201D and"
                                + " \u201Cstylesheet\u201D must have a"
                                + " \u201Ctitle\u201D attribute with a"
                                + " non-empty value.");
                }
                if (atts.getIndex("", "fetchpriority") > -1
                        && Arrays.stream(EXTERNAL_RESOURCE_LINK_REL).noneMatch(relList::contains)) {
                    warn("A \u201Clink\u201D element with \u201Cfetchpriority\u201D"
                                + " attribute should have a \u201Crel\u201D"
                                + " attribute containing external resource type.");
                }
                if ((ancestorMask & BODY_MASK) != 0
                        && (relList != null
                                && !(relList.contains("dns-prefetch")
                                        || relList.contains("modulepreload")
                                        || relList.contains("pingback")
                                        || relList.contains("preconnect")
                                        || relList.contains("prefetch")
                                        || relList.contains("preload")
                                        || relList.contains("prerender")
                                        || relList.contains("stylesheet")))
                        && atts.getIndex("", "itemprop") < 0
                        && atts.getIndex("", "property") < 0) {
                    err("A \u201Clink\u201D element must not appear"
                            + " as a descendant of a \u201Cbody\u201D element"
                            + " unless the \u201Clink\u201D element has an"
                            + " \u201Citemprop\u201D attribute or has a"
                            + " \u201Crel\u201D attribute whose value contains"
                            + " \u201Cdns-prefetch\u201D,"
                            + " \u201Cmodulepreload\u201D,"
                            + " \u201Cpingback\u201D,"
                            + " \u201Cpreconnect\u201D,"
                            + " \u201Cprefetch\u201D,"
                            + " \u201Cpreload\u201D,"
                            + " \u201Cprerender\u201D, or"
                            + " \u201Cstylesheet\u201D.");
                }
                if (atts.getIndex("", "blocking") > -1
                        && (atts.getIndex("", "rel") == -1
                        || !relList.contains("stylesheet"))) {
                    err("A \u201Clink\u201D element with a"
                                + " \u201Cblocking\u201D attribute must have a"
                                + " \u201Crel\u201D attribute whose value is"
                                + " \u201Cstylesheet\u201D.");
                }
            }

            // microdata
            if (itemid && !(itemscope && itemtype)) {
                err("The \u201Citemid\u201D attribute must not be specified on elements that do not have both an \u201Citemscope\u201D attribute and an \u201Citemtype\u201D attribute specified.");
            }
            if (itemref && !itemscope) {
                err("The \u201Citemref\u201D attribute must not be specified on elements that do not have an \u201Citemscope\u201D attribute specified.");
            }
            if (itemtype && !itemscope) {
                err("The \u201Citemtype\u201D attribute must not be specified on elements that do not have an \u201Citemscope\u201D attribute specified.");
            }

            // Errors for use of ARIA attributes that conflict with native
            // element semantics.
            if (atts.getIndex("", "contenteditable") > -1
                    && "true".equals(atts.getValue("", "aria-readonly"))) {
                err("The \u201Caria-readonly\u201D attribute must only be"
                        + " specified with a value of \u201Cfalse\u201D"
                        + " on elements that have a \u201Ccontenteditable\u201D"
                        + " attribute.");
            }
            if (atts.getIndex("", "aria-placeholder") > -1
                    && atts.getIndex("", "placeholder") > -1) {
                err("The \u201Caria-placeholder\u201D attribute must not be"
                        + " specified on elements that have a"
                        + " \u201Cplaceholder\u201D attribute.");
            }
            // Warnings for use of ARIA attributes with markup already
            // having implicit ARIA semantics.
            if (ELEMENTS_WITH_IMPLICIT_ROLE.containsKey(localName)
                    && ELEMENTS_WITH_IMPLICIT_ROLE.get(localName).equals(
                            role)) {
                if (!("img".equals(localName)
                        && ("".equals(atts.getValue("", "alt"))))) {
                    warn("The \u201C" + role + "\u201D role is unnecessary for"
                            + " element" + " \u201C" + localName + "\u201D.");
                }
            } else if (ELEMENTS_WITH_IMPLICIT_ROLES.containsKey(localName)
                    && role != null
                    && Arrays.binarySearch(
                            ELEMENTS_WITH_IMPLICIT_ROLES.get(localName),
                            role) >= 0) {
                warn("The \u201C" + role + "\u201D role is unnecessary for"
                        + " element" + " \u201C" + localName + "\u201D.");
            } else if (ELEMENTS_THAT_NEVER_NEED_ROLE.containsKey(localName)
                    && ELEMENTS_THAT_NEVER_NEED_ROLE.get(localName).equals(
                            role)) {
                warn("Element \u201C" + localName + "\u201D does not need a"
                        + " \u201Crole\u201D attribute.");
            } else if ("input" == localName) {
                inputTypeVal = inputTypeVal == null ? "text" : inputTypeVal;
                if ("radio".equals(inputTypeVal)
                        || "checkbox".equals(inputTypeVal)) {
                    if (atts.getIndex("", "aria-checked") >= 0
                            && !"".equals(atts.getValue("", "aria-checked"))) {
                        err("The \u201Caria-checked\u201D attribute must not"
                                + " be used on an \u201Cinput\u201D element"
                                + " which has a \u201Ctype\u201D attribute"
                                + " whose value is \u201C" + inputTypeVal
                                + "\u201D.");
                    }
                }
                if ("date".equals(inputTypeVal)
                        || "month".equals(inputTypeVal)
                        || "week".equals(inputTypeVal)
                        || "time".equals(inputTypeVal)
                        || "datetime-local".equals(inputTypeVal)
                        || "number".equals(inputTypeVal)
                        || "range".equals(inputTypeVal)) {
                    if (atts.getIndex("", "aria-valuemin") >= 0
                            && !"".equals(atts.getValue("", "aria-valuemin"))) {
                        if (atts.getIndex("", "min") >= 0) {
                            err("The \u201Caria-valuemin\u201D attribute must"
                                    + " not be used on an element which has a"
                                    + " \u201Cmin\u201D attribute.");
                        } else {
                            warn("The \u201Caria-valuemin\u201D attribute"
                                    + " should not be used on an"
                                    + " \u201Cinput\u201D element which has a"
                                    + " \u201Ctype\u201D attribute whose value"
                                    + " is \u201C" + inputTypeVal + "\u201D.");
                        }
                    }
                    if (atts.getIndex("", "aria-valuemax") >= 0
                            && !"".equals(atts.getValue("", "aria-valuemax"))) {
                        if (atts.getIndex("", "max") >= 0) {
                            err("The \u201Caria-valuemax\u201D attribute must"
                                    + " not be used on an element which has a"
                                    + " \u201Cmax\u201D attribute.");
                        } else {
                            warn("The \u201Caria-valuemax\u201D attribute"
                                    + " should not be used on an"
                                    + " \u201Cinput\u201D element which has a"
                                    + " \u201Ctype\u201D attribute whose value"
                                    + " is \u201C" + inputTypeVal + "\u201D.");
                        }
                    }
                }
                if (INPUT_TYPES_WITH_IMPLICIT_ROLE.containsKey(inputTypeVal)
                        && INPUT_TYPES_WITH_IMPLICIT_ROLE.get(
                                inputTypeVal).equals(role)) {
                    warnExplicitRoleUnnecessaryForType("input", role,
                            inputTypeVal);
                } else if ("email".equals(inputTypeVal)
                        || "search".equals(inputTypeVal)
                        || "tel".equals(inputTypeVal)
                        || "text".equals(inputTypeVal)
                        || "url".equals(inputTypeVal)) {
                    if (atts.getIndex("", "list") < 0) {
                        if ("textbox".equals(role)
                                && !"search".equals(inputTypeVal)) {
                            warn("The \u201Ctextbox\u201D role is unnecessary"
                                    + " for an \u201Cinput\u201D element that"
                                    + " has no \u201Clist\u201D attribute and"
                                    + " whose type is" + " \u201C"
                                    + inputTypeVal + "\u201D.");
                        }
                        if ("searchbox".equals(role)
                                && "search".equals(inputTypeVal)) {
                            warn("The \u201Csearchbox\u201D role is unnecessary"
                                    + " for an \u201Cinput\u201D element that"
                                    + " has no \u201Clist\u201D attribute and"
                                    + " whose type is" + " \u201C"
                                    + inputTypeVal + "\u201D.");
                        }
                    } else {
                        if ("combobox".equals(role)) {
                            warn("The \u201Ccombobox\u201D role is unnecessary"
                                    + " for an \u201Cinput\u201D element that"
                                    + " has a \u201Clist\u201D attribute and"
                                    + " whose type is" + " \u201C"
                                    + inputTypeVal + "\u201D.");
                        }
                        if (atts.getIndex("", "aria-haspopup") >= 0
                                && !"".equals(
                                        atts.getValue("", "aria-haspopup"))) {
                            warn("The \u201Caria-haspopup\u201D attribute"
                                    + " should not be used on an"
                                    + " \u201Cinput\u201D element that has a"
                                    + " \u201Clist\u201D attribute and whose"
                                    + " type is \u201C" + inputTypeVal
                                    + "\u201D.");
                        }
                    }
                }
            } else if (atts.getIndex("", "href") > -1 && "link".equals(role)
                    && ("a".equals(localName) || "area".equals(localName)
                            || "link".equals(localName))) {
                warn("The \u201Clink\u201D role is unnecessary for element"
                        + " \u201C" + localName + "\u201D with attribute"
                        + " \u201Chref\u201D.");
            } else if (atts.getIndex("", "href") > -1 && "link".equals(role)
                    && ("a".equals(localName) || "area".equals(localName)
                            || "link".equals(localName))) {
                warn("The \u201Clink\u201D role is unnecessary for element"
                        + " \u201C" + localName + "\u201D with attribute"
                        + " \u201Chref\u201D.");
            } else if (("tbody".equals(localName) || "tfoot".equals(localName)
                    || "thead".equals(localName)) && "rowgroup".equals(role)) {
                warn("The \u201Crowgroup\u201D role is unnecessary for element"
                        + " \u201C" + localName + "\u201D.");
            } else if ("th" == localName && ("columnheader".equals(role)
                    || "columnheader".equals(role))) {
                warn("The \u201C" + role + "\u201D role is unnecessary for"
                        + " element \u201Cth\u201D.");
            } else if ("li" == localName && "listitem".equals(role)
                    && !"menu".equals(parentName)) {
                warn("The \u201Clistitem\u201D role is unnecessary for an"
                        + " \u201Cli\u201D element whose parent is"
                        + " an \u201Col\u201D element or a"
                        + " \u201Cul\u201D element.");
            } else if ("button" == localName && "button".equals(role)
                    && "menu".equals(atts.getValue("", "type"))) {
                warnExplicitRoleUnnecessaryForType("button", "button", "menu");
            } else if ("menu" == localName && "toolbar".equals(role)
                    && "toolbar".equals(atts.getValue("", "type"))) {
                warnExplicitRoleUnnecessaryForType("menu", "toolbar",
                        "toolbar");
            } else if ("li" == localName && "listitem".equals(role)
                    && !"menu".equals(parentName)) {
                warn("The \u201Clistitem\u201D role is unnecessary for an"
                        + " \u201Cli\u201D element whose parent is"
                        + " an \u201Col\u201D element or a"
                        + " \u201Cul\u201D element.");
            }
        } else {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                boolean isEmptyAtt = !"".equals(atts.getValue(i));
                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
                String attLocal = atts.getLocalName(i);
                if (atts.getURI(i).length() == 0) {
                    if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal
                            && !isEmptyAtt) {
                        System.out.println("found aria-activedescendant 1");
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal && !isEmptyAtt) {
                        owns = atts.getValue(i);
                    }
                }
            }

            allIds.addAll(ids);
        }

        // ARIA required owner/ancestors
        Set<String> requiredAncestorRoles = REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(
                role);
        if (requiredAncestorRoles != null && !"presentation".equals(parentRole)
                && !"tbody".equals(localName) && !"tfoot".equals(localName)
                && !"thead".equals(localName)) {
            if (!currentElementHasRequiredAncestorRole(requiredAncestorRoles)) {
                if (atts.getIndex("", "id") > -1
                        && !"".equals(atts.getValue("", "id"))) {
                    needsAriaOwner.add(new IdrefLocator(
                            new LocatorImpl(getDocumentLocator()),
                            atts.getValue("", "id"), role));
                } else {
                    errContainedInOrOwnedBy(role, getDocumentLocator());
                }
            }
        }

        // ARIA IDREFS
        for (String att : MUST_NOT_DANGLE_IDREFS) {
            String attVal = atts.getValue("", att);
            if (attVal != null && attVal != "") {
                String[] tokens = AttributeUtil.split(attVal);
                for (String token : tokens) {
                    ariaReferences.add(
                            new IdrefLocator(getDocumentLocator(), token, att));
                }
            }
        }
        allIds.addAll(ids);

        if (isAriaLabelMisuse(ariaLabel, localName, role, atts)) {
            warn("Possible misuse of \u201Caria-label\u201D. (If you disagree"
                    + " with this warning, file an issue report or send e-mail"
                    + " to www-validator@w3.org.)");
            incrementUseCounter("aria-label-misuse-found");
            String systemId = getDocumentLocator().getSystemId();
            if (systemId != null && hasPageEmitterInCallStack()) {
                log4j.info("aria-label misuse " + systemId);
            }
        }

        // aria-activedescendant accompanied by aria-owns
        if (activeDescendant != null && !"".equals(activeDescendant)) {
            // String activeDescendantVal = atts.getValue("",
            // "aria-activedescendant");
            if (owns != null && !"".equals(owns)) {
                activeDescendantWithAriaOwns = true;
                // String[] tokens = AttributeUtil.split(owns);
                // for (int i = 0; i < tokens.length; i++) {
                // String token = tokens[i];
                // if (token.equals(activeDescendantVal)) {
                // activeDescendantWithAriaOwns = true;
                // break;
                // }
                // }
            }
        }
        // activedescendant
        for (Iterator<Map.Entry<StackNode, Locator>> iterator = openActiveDescendants.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<StackNode, Locator> entry = iterator.next();
            if (ids.contains(entry.getKey().getActiveDescendant())) {
                iterator.remove();
            }
        }

        if ("http://www.w3.org/1999/xhtml" == uri) {
            int number = specialAncestorNumber(localName);
            if (number > -1) {
                ancestorMask |= (1 << number);
            }
            if ("a" == localName) {
                if (href) {
                    ancestorMask |= HREF_MASK;
                    if ("true".equals(atts.getValue("", "aria-disabled"))) {
                        warn("An \u201Caria-disabled\u201D attribute whose"
                                + " value is \u201Ctrue\u201D should not be"
                                + " specified on an \u201Ca\u201D element"
                                + " that has an \u201Chref\u201D attribute.");
                    }
                }
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    activeDescendant, forAttr);
            if ("style" == localName) {
                child.setIsCollectingCharacters(true);
            }
            if ("script" == localName) {
                child.setIsCollectingCharacters(true);
            }
            if ("figure" == localName) {
                child.setIsCollectingChildren(true);
            }
            if (activeDescendant != null && !activeDescendantWithAriaOwns) {
                openActiveDescendants.put(child,
                        new LocatorImpl(getDocumentLocator()));
            }
            if ("select" == localName && atts.getIndex("", "multiple") == -1) {
                openSingleSelects.put(child, getDocumentLocator());
            } else if ("label" == localName) {
                openLabels.put(child, new LocatorImpl(getDocumentLocator()));
            } else if ("video" == localName || "audio" == localName) {
                openMediaElements.put(child,
                        new TaintableLocatorImpl(getDocumentLocator()));
            }
            push(child);
            if ("article" == localName || "aside" == localName
                    || "nav" == localName || "section" == localName) {
                if (atts.getIndex("", "aria-label") > -1
                        && !"".equals(atts.getValue("", "aria-label"))) {
                    child.setHeadingFound();
                }
            }
            if ("select" == localName) {
                boolean hasSize = false;
                boolean sizeIsOne = false;
                boolean sizeIsGreaterThanOne = false;
                boolean hasMultiple = atts.getIndex("", "multiple") > -1;
                if (atts.getIndex("", "aria-multiselectable") > -1) {
                    warn("The \u201Caria-multiselectable\u201D attribute"
                            + " should not be used with the \u201Cselect"
                            + " \u201D element.");
                }
                if (atts.getIndex("", "size") > -1) {
                    hasSize = true;
                    String size = trimSpaces(atts.getValue("", "size"));
                    if (!"".equals(size)) {
                        try {
                            if ((size.length() > 1 //
                                    && size.charAt(0) == '+'
                                    && Integer.parseInt(size.substring(1)) == 1)
                                    || Integer.parseInt(size) == 1) {
                                sizeIsOne = true;
                            } else if ((size.length() > 1
                                    && size.charAt(0) == '+'
                                    && Integer.parseInt(size.substring(1)) > 1)
                                    || Integer.parseInt(size) > 1) {
                                sizeIsGreaterThanOne = true;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                if (sizeIsGreaterThanOne || hasMultiple) {
                    if ("listbox".equals(role)) {
                        warn("The \u201Clistbox\u201D role is unnecessary for"
                                + " element \u201Cselect\u201D with a"
                                + " \u201Cmultiple\u201D attribute or with a"
                                + " \u201Csize\u201D attribute whose value"
                                + " is greater than 1.");
                    } else if (role != null) {
                        err("A \u201Cselect\u201D element with a"
                                + " \u201Cmultiple\u201D attribute or with a"
                                + " \u201Csize\u201D attribute whose value"
                                + " is greater than 1 must not have any"
                                + " \u201Crole\u201D attribute.");
                    }
                }
                if (!hasMultiple) {
                    if (!sizeIsGreaterThanOne && role != null) {
                        if ("combobox".equals(role)) {
                            warn("The \u201Ccombobox\u201D role is unnecessary"
                                    + " for element \u201Cselect\u201D"
                                    + " without a \u201Cmultiple\u201D"
                                    + " attribute and without a"
                                    + " \u201Csize\u201D attribute whose value"
                                    + " is greater than 1.");
                        } else if (!"menu".equals(role)) {
                            err("The \u201C" + role + "\u201D role is not"
                                    + " allowed for element \u201Cselect\u201D"
                                    + " without a \u201Cmultiple\u201D"
                                    + " attribute and without a"
                                    + " \u201Csize\u201D attribute whose value"
                                    + " is greater than 1.");
                        }
                    }
                    if (atts.getIndex("", "required") > -1) {
                        if (hasSize) {
                            if (sizeIsOne) {
                                child.setOptionNeeded();
                            } else {
                                // "size" attr specified but isn't 1; do nothing
                            }
                        } else {
                            // "size" unspecified; so browsers default size to 1
                            child.setOptionNeeded();
                        }
                    }
                } else {
                        // ARIA
                }
            }
            if (parent != null && parent.isCollectingChildren()) {
                parent.addChild(child);
            }
        } else if ("http://n.validator.nu/custom-elements/" == uri) {
            /*
             * For elements with names containing "-" (custom elements), the
             * customelements/NamespaceChanging* code exposes them to jing as
             * elements in the http://n.validator.nu/custom-elements/ namespace.
             * Therefore our RelaxNG schema allows those elements. However,
             * schematronequiv.Assertions still sees those elements as being in
             * the HTML namespace, so here we need to emit an error for the case
             * where, in source transmitted with an XML content type, somebody
             * (for whatever reason) has elements in their markup which they
             * have explicitly placed in that namespace (otherwise, due to
             * allowing those elements in our RelaxNG schema, Jing on its own
             * won't emit any error for them).
             */
            err("Element \u201c" + localName + "\u201d from namespace"
                    + " \u201chttp://n.validator.nu/custom-elements/\u201d"
                    + " not allowed.");
        } else {
            StackNode child = new StackNode(ancestorMask, null, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null) {
                openActiveDescendants.put(child,
                        new LocatorImpl(getDocumentLocator()));
            }
            push(child);
        }
        stack[currentPtr].setLocator(new LocatorImpl(getDocumentLocator()));
    }

    /**
     * @see nu.validator.checker.Checker#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (numberOfTemplatesDeep > 0) {
            return;
        }
        if (stack[currentPtr].getIsCollectingCharacters()) {
            stack[currentPtr].appendToTextContent(ch, start, length);
        }
        StackNode node = peek();
        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                default:
                    if ("h1".equals(node.name) || "h2".equals(node.name)
                            || "h3".equals(node.name) || "h4".equals(node.name)
                            || "h5".equals(node.name) || "h6".equals(node.name)
                            || (node.ancestorMask & H1_MASK) != 0
                            || (node.ancestorMask & H2_MASK) != 0
                            || (node.ancestorMask & H3_MASK) != 0
                            || (node.ancestorMask & H4_MASK) != 0
                            || (node.ancestorMask & H5_MASK) != 0
                            || (node.ancestorMask & H6_MASK) != 0) {
                        stack[currentHeadingPtr].setTextNodeFound();
                    } else if ("figcaption".equals(node.name)
                            || (node.ancestorMask & FIGCAPTION_MASK) != 0) {
                        if ((node.ancestorMask & FIGURE_MASK) != 0) {
                            stack[currentFigurePtr].setFigcaptionContentFound();
                        }
                        // for any ancestor figures of the parent figure
                        // of this figcaption, the content of this
                        // figcaption counts as a text node descendant
                        for (int j = 1; j < currentFigurePtr; j++) {
                            if ("figure".equals(
                                    stack[currentFigurePtr - j].getName())) {
                                stack[currentFigurePtr - j].setTextNodeFound();
                            }
                        }
                    } else if ("figure".equals(node.name)
                            || (node.ancestorMask & FIGURE_MASK) != 0) {
                        stack[currentFigurePtr].setTextNodeFound();
                        // for any ancestor figures of this figure, this
                        // also counts as a text node descendant
                        for (int k = 1; k < currentFigurePtr; k++) {
                            if ("figure".equals(
                                    stack[currentFigurePtr - k].getName())) {
                                stack[currentFigurePtr - k].setTextNodeFound();
                            }
                        }
                    } else if ("option".equals(node.name)
                            && !stack[currentPtr - 1].hasOption()
                            && (!stack[currentPtr - 1].hasEmptyValueOption()
                                    || stack[currentPtr - 1].hasNoValueOption())
                            && stack[currentPtr
                                    - 1].nonEmptyOptionLocator() == null) {
                        stack[currentPtr - 1].setNonEmptyOption(
                                (new LocatorImpl(getDocumentLocator())));
                    }
                    return; // This return can be removed if other code is added
                            // here. But it's here for now because we know we
                            // have at least one non-WS character, and for the
                            // purposes of the current code, that's all we need;
                            // it's a waste to keep checking for more.
            }
        }
    }

    private CharSequence renderTypeList(String[] types) {
        StringBuilder sb = new StringBuilder();
        int len = types.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (i == len - 1) {
                sb.append("or ");
            }
            sb.append("\u201C");
            sb.append(types[i]);
            sb.append('\u201D');
        }
        return sb;
    }

    private CharSequence renderRoleSet(Set<String> roles) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String role : roles) {
            if (first) {
                first = false;
            } else {
                sb.append(" or ");
            }
            sb.append("\u201Crole=");
            sb.append(role);
            sb.append('\u201D');
        }
        return sb;
    }

}
