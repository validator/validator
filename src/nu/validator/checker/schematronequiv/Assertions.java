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

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;

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
import nu.validator.xml.AttributesImpl;

import nu.validator.vendor.relaxng.datatype.DatatypeException;

import org.w3c.css.css.StyleSheetParser;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.CssParseException;
import org.w3c.css.parser.Errors;
import org.w3c.css.util.ApplContext;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Assertions extends Checker {

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

    /**
     * Checks if a sizes attribute value starts with "auto" (case-insensitive).
     * Per HTML spec, sizes can be exactly "auto" or start with "auto," for
     * fallback support in browsers that don't understand auto.
     */
    private static final boolean sizesStartsWithAuto(String sizes) {
        if (sizes == null) {
            return false;
        }
        String trimmed = trimLeadingSpaces(sizes);
        if (trimmed.length() < 4) {
            return false;
        }
        char c0 = trimmed.charAt(0);
        char c1 = trimmed.charAt(1);
        char c2 = trimmed.charAt(2);
        char c3 = trimmed.charAt(3);
        if ((c0 == 'a' || c0 == 'A')
                && (c1 == 'u' || c1 == 'U')
                && (c2 == 't' || c2 == 'T')
                && (c3 == 'o' || c3 == 'O')) {
            // Check if it's exactly "auto" or "auto," (with optional whitespace)
            if (trimmed.length() == 4) {
                return true; // exactly "auto"
            }
            // Skip whitespace after "auto"
            int i = 4;
            while (i < trimmed.length()) {
                char c = trimmed.charAt(i);
                if (' ' == c || '\t' == c || '\n' == c || '\f' == c
                        || '\r' == c) {
                    i++;
                } else {
                    break;
                }
            }
            if (i == trimmed.length()) {
                return true; // "auto" followed by only whitespace
            }
            if (trimmed.charAt(i) == ',') {
                return true; // "auto," with fallback values
            }
        }
        return false;
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
        OBSOLETE_ELEMENTS.put("applet", "Use “embed” or “object” element instead.");
        OBSOLETE_ELEMENTS.put("acronym", "Use the “abbr” element instead.");
        OBSOLETE_ELEMENTS.put("bgsound", "Use the “audio” element instead.");
        OBSOLETE_ELEMENTS.put("dir", "Use the “ul” element instead.");
        OBSOLETE_ELEMENTS.put("frame", "Use the “iframe” element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put("frameset", "Use the “iframe” element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put("noframes", "Use the “iframe” element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put("isindex", "Use the “form” element containing “input” element of type “text” instead.");
        OBSOLETE_ELEMENTS.put("keygen", "");
        OBSOLETE_ELEMENTS.put("listing", "Use “pre” or “code” element instead.");
        OBSOLETE_ELEMENTS.put("menuitem", "Use script to handle “contextmenu” event instead.");
        OBSOLETE_ELEMENTS.put("nextid", "Use GUIDs instead.");
        OBSOLETE_ELEMENTS.put("noembed", "Use the “object” element instead.");
        OBSOLETE_ELEMENTS.put("param", "Use the “data” attribute of the “object” element to set the URL of the external resource.");
        OBSOLETE_ELEMENTS.put("plaintext", "Use the “text/plain” MIME type instead.");
        OBSOLETE_ELEMENTS.put("rb", "");
        OBSOLETE_ELEMENTS.put("rtc", "");
        OBSOLETE_ELEMENTS.put("strike", "Use “del” or “s” element instead.");
        OBSOLETE_ELEMENTS.put("xmp", "Use “pre” or “code” element instead.");
        OBSOLETE_ELEMENTS.put("basefont", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("big", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("blink", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("center", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("font", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("marquee", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("multicol", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("nobr", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("spacer", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("tt", "Use CSS instead.");
    }

    /**
     * Collection that contains attribute name as key, while value is {@link Map} that contains element name as key and error message as a value.
     */
    private static final Map<String, Map<String, String>> OBSOLETE_ATTRIBUTES = new HashMap<>();

    private static void registerObsoleteAttribute(String name, String[] elements, String suggestion) {
        Map<String, String> map = OBSOLETE_ATTRIBUTES.computeIfAbsent(name, k -> new HashMap<>());
        for(String element : elements) {
            map.put(element, suggestion);
        }
    }

    static {
        registerObsoleteAttribute("abbr",
                new String[] { "td" },
                "Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.");
        registerObsoleteAttribute("accept",
                new String[] { "form" },
                "Use the “accept” attribute directly on the “input” elements instead.");
        registerObsoleteAttribute("archive",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("a",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("axis",
                new String[] { "td", "th" },
                "Use the “scope” attribute.");
        registerObsoleteAttribute("border",
                new String[] { "input", "img", "object", "table" },
                "Consider specifying “img { border: 0; }” in CSS instead.");
        registerObsoleteAttribute("charset",
                new String[] { "a", "link" },
                "Use an HTTP Content-Type header on the linked resource instead.");
        registerObsoleteAttribute("classid",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("code",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("codebase",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("codetype",
                new String[] { "object" },
                "Use the “data” and “type” attributes to invoke plugins.");
        registerObsoleteAttribute("coords",
                new String[] { "a" },
                "Use “area” instead of “a” for image maps.");
        registerObsoleteAttribute("datafld",
                new String[] { "a", "button", "div", "fieldset", "iframe", "img", "input", "label", "legend", "object", "select", "span", "textarea" },
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        registerObsoleteAttribute("dataformatas",
                new String[] { "button", "div", "input", "label", "legend", "object", "option", "select", "span", "table" },
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        registerObsoleteAttribute("datapagesize",
                new String[] { "table" },
                "You can safely omit it.");
        registerObsoleteAttribute("datasrc",
                new String[] { "a", "button", "div", "iframe", "img", "input", "label", "legend", "object", "option", "select", "span", "table", "textarea" },
                "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        registerObsoleteAttribute("declare",
                new String[] { "object" },
                "Repeat the “object” element completely each time the resource is to be reused.");
        registerObsoleteAttribute("event",
                new String[] { "script" },
                "Use DOM Events mechanisms to register event listeners.");
        registerObsoleteAttribute("for",
                new String[] { "script" },
                "Use DOM Events mechanisms to register event listeners.");
        registerObsoleteAttribute("hreflang",
                new String[] { "area" },
                "You can safely omit it.");
        registerObsoleteAttribute("ismap",
                new String[] { "input" },
                "You can safely omit it.");
        registerObsoleteAttribute("label",
                new String[] { "menu" },
                "Use script to handle “contextmenu” event instead.");
        registerObsoleteAttribute("language",
                new String[] { "script" },
                "Use the “type” attribute instead.");
        registerObsoleteAttribute("longdesc",
                new String[] { "iframe", "img" },
                "Use a regular “a” element to link to the description.");
        registerObsoleteAttribute("lowsrc",
                new String[] { "img" },
                "Use a progressive JPEG image instead.");
        registerObsoleteAttribute("manifest",
                new String[] { "html" },
                "Use service workers instead.");
        registerObsoleteAttribute("methods",
                new String[] { "a", "link" },
                "Use the HTTP OPTIONS feature instead.");
        registerObsoleteAttribute("name",
                new String[] { "a", "embed", "img", "option" },
                "Use the “id” attribute instead.");
        registerObsoleteAttribute("name",
                new String[] { "a" },
                "Consider putting an “id” attribute on the nearest container instead.");
        registerObsoleteAttribute("nohref",
                new String[] { "area" },
                "Omitting the “href” attribute is sufficient.");
        registerObsoleteAttribute("profile",
                new String[] { "head" },
                "To declare which “meta” terms are used in the document, instead register the names as meta extensions. To trigger specific UA behaviors, use a “link” element instead.");
        registerObsoleteAttribute("scheme",
                new String[] { "meta" },
                "Use only one scheme per field, or make the scheme declaration part of the value.");
        registerObsoleteAttribute("scope",
                new String[] { "td" },
                "Use the “scope” attribute on a “th” element instead.");
        registerObsoleteAttribute("shape",
                new String[] { "a" },
                "Use “area” instead of “a” for image maps.");
        registerObsoleteAttribute("standby",
                new String[] { "object" },
                "Optimise the linked resource so that it loads quickly or, at least, incrementally.");
        registerObsoleteAttribute("summary",
                new String[] { "table" },
                "Consider describing the structure of the “table” in a “caption” element or in a “figure” element containing the “table”; or, simplify the structure of the “table” so that no description is needed.");
        registerObsoleteAttribute("target",
                new String[] { "link" },
                "You can safely omit it.");
        registerObsoleteAttribute("type",
                new String[] { "param" },
                "Use the “name” and “value” attributes without declaring value types.");
        registerObsoleteAttribute("type",
                new String[] { "area" },
                "You can safely omit it.");
        registerObsoleteAttribute("type",
                new String[] { "menu" },
                "Use script to handle “contextmenu” event instead.");
        registerObsoleteAttribute("typemustmatch",
                new String[] { "object" },
                "Avoid using “object” elements with untrusted resources.");
        registerObsoleteAttribute("urn",
                new String[] { "a", "link" },
                "Specify the preferred persistent identifier using the “href” attribute instead.");
        registerObsoleteAttribute("usemap",
                new String[] { "input", "object" },
                "Use the “img” element instead.");
        registerObsoleteAttribute("valuetype",
                new String[] { "param" },
                "Use the “name” and “value” attributes without declaring value types.");
        registerObsoleteAttribute("version",
                new String[] { "html" },
                "You can safely omit it.");
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
        OBSOLETE_STYLE_ATTRS.put("background", new String[] { "body", "table", "thead", "tbody", "tfoot", "tr", "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("bgcolor",
                new String[] { "table", "tr", "td", "th", "body" });
        OBSOLETE_STYLE_ATTRS.put("bordercolor", new String[] { "table" });
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
        OBSOLETE_STYLE_ATTRS.put("framespacing", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("frame", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("height", new String[] { "table", "thead", "tbody", "tfoot", "tr", "td", "th" });
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

    private static final Map<String, String> OBSOLETE_GLOBAL_ATTRIBUTES = new HashMap<>();

    static {
        OBSOLETE_GLOBAL_ATTRIBUTES.put("contextmenu", "Use script to handle “contextmenu” event instead.");
        OBSOLETE_GLOBAL_ATTRIBUTES.put("dropzone", "Use script to handle the “dragenter” and “dragover” events instead.");
        OBSOLETE_GLOBAL_ATTRIBUTES.put("onshow", "Use script to handle “contextmenu” event instead.");
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

    private static final String[] ARIA_GLOBAL_ATTRIBUTES = { "aria-atomic",
        "aria-braillelabel", "aria-brailleroledescription", "aria-busy",
        "aria-controls", "aria-current", "aria-describedby", "aria-details",
        "aria-disabled", "aria-errormessage", "aria-flowto", "aria-haspopup",
        "aria-hidden", "aria-invalid", "aria-keyshortcuts", "aria-label",
        "aria-labelledby", "aria-live", "aria-owns", "aria-relevant",
        "aria-roledescription", "aria-description" };

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

    private static final Set<String> ROLES_WHICH_CANNOT_BE_NAMED =
        new LinkedHashSet<>(
                Arrays.asList("caption", "code", "deletion", "emphasis",
                    "generic", "insertion", "paragraph", "presentation",
                    "strong", "subscript", "superscript")
                );

    private static final Set<String> ELEMENTS_THAT_CAN_HAVE_A_NAME_ATTRIBUTE =
            Set.of("button", "fieldset", "input", "output", "select",
                    "textarea", "details", "form", "iframe", "object", "map",
                    "meta", "slot");

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
        ELEMENTS_WITH_IMPLICIT_ROLE.put("s", "deletion");
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
     * Map of global aria attributes as keys and array of roles for which they have been deprecated for, as values.
     * Array of roles is sorted according to {@linkplain Comparable natural ordering}
     */
    private static final Map<String, String[]> ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE = new HashMap<>();
    
    static {
        ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.put("aria-disabled", new String[] {
                "alert", "alertdialog", "article", "associationlist",
                "associationlistitemkey", "associationlistitemvalue", "banner",
                "blockquote", "caption", "cell", "code", "command", "comment",
                "complementary", "contentinfo", "definition", "deletion",
                "dialog", "directory", "document", "emphasis", "feed", "figure",
                "form", "generic", "heading", "img", "insertion", "landmark",
                "list", "listitem", "log", "main", "mark", "marquee", "math",
                "meter", "navigation", "note", "paragraph", "presentation",
                "progressbar", "range", "region", "rowgroup", "search",
                "section", "sectionhead", "status", "strong", "structure",
                "subscript", "suggestion", "superscript", "table", "tabpanel",
                "term", "time", "timer", "tooltip", "widget", "window" });
        ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.put("aria-errormessage",
                new String[] { "alert", "alertdialog", "article",
                        "associationlist", "associationlistitemkey",
                        "associationlistitemvalue", "banner", "blockquote",
                        "button", "caption", "cell", "code", "command",
                        "comment", "complementary", "composite", "contentinfo",
                        "definition", "deletion", "dialog", "directory",
                        "document", "emphasis", "feed", "figure", "form",
                        "generic", "grid", "group", "heading", "img", "input",
                        "insertion", "landmark", "link", "list", "listitem",
                        "log", "main", "mark", "marquee", "math", "menu",
                        "menubar", "menuitem", "menuitemcheckbox",
                        "menuitemradio", "meter", "navigation", "note",
                        "option", "paragraph", "presentation", "progressbar",
                        "radio", "range", "region", "row", "rowgroup",
                        "scrollbar", "search", "section", "sectionhead",
                        "select", "separator", "status", "strong", "structure",
                        "subscript", "suggestion", "superscript", "tab",
                        "table", "tablist", "tabpanel", "term", "time", "timer",
                        "toolbar", "tooltip", "treeitem", "widget", "window" });
        ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.put("aria-haspopup", new String[] {
                "alert", "alertdialog", "article", "associationlist",
                "associationlistitemkey", "associationlistitemvalue", "banner",
                "blockquote", "caption", "cell", "checkbox", "code", "command",
                "comment", "complementary", "composite", "contentinfo",
                "definition", "deletion", "dialog", "directory", "document",
                "emphasis", "feed", "figure", "form", "generic", "grid",
                "group", "heading", "img", "input", "insertion", "landmark",
                "list", "listbox", "listitem", "log", "main", "mark", "marquee",
                "math", "menu", "menubar", "meter", "navigation", "note",
                "option", "paragraph", "presentation", "progressbar", "radio",
                "radiogroup", "range", "region", "row", "rowgroup", "scrollbar",
                "search", "section", "sectionhead", "select", "separator",
                "spinbutton", "status", "strong", "structure", "subscript",
                "suggestion", "superscript", "switch", "table", "tablist",
                "tabpanel", "term", "time", "timer", "toolbar", "tooltip",
                "tree", "treegrid", "widget", "window" });
        ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.put("aria-invalid", new String[] {
                "alert", "alertdialog", "article", "associationlist",
                "associationlistitemkey", "associationlistitemvalue", "banner",
                "blockquote", "button", "caption", "cell", "code", "command",
                "comment", "complementary", "composite", "contentinfo",
                "definition", "deletion", "dialog", "directory", "document",
                "emphasis", "feed", "figure", "form", "generic", "grid",
                "group", "heading", "img", "input", "insertion", "landmark",
                "link", "list", "listitem", "log", "main", "mark", "marquee",
                "math", "menu", "menubar", "menuitem", "menuitemcheckbox",
                "menuitemradio", "meter", "navigation", "note", "option",
                "paragraph", "presentation", "progressbar", "radio", "range",
                "region", "row", "rowgroup", "scrollbar", "search", "section",
                "sectionhead", "select", "separator", "status", "strong",
                "structure", "subscript", "suggestion", "superscript", "tab",
                "table", "tablist", "tabpanel", "term", "time", "timer",
                "toolbar", "tooltip", "treeitem", "widget", "window" });
        ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.put("aria-level",
                new String[] { "listitem" });
    }

    /**
     * Names of link types that are considered as "External Resource" according to https://html.spec.whatwg.org/multipage/links.html#linkTypes
     */
    private static final String[] EXTERNAL_RESOURCE_LINK_REL = new String[] {
            "dns-prefetch", "icon", "manifest", "modulepreload", "pingback", "preconnect", "prefetch", "preload", "prerender", "stylesheet"
    };

    private static final Set<String> HTML_ELEMENTS = new HashSet<>(Arrays.asList(
            "a", "abbr", "acronym", "address", "annotation-xml", "applet", "area",
            "article", "aside", "attachment", "audio", "b", "base", "basefont", "bdi",
            "bdo", "bgsound", "big", "blockquote", "body", "br", "button", "canvas",
            "caption", "center", "cite", "code", "col", "colgroup", "color-profile",
            "data", "datalist", "dd", "del", "details", "dfn", "dialog", "dir", "div",
            "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure", "font",
            "font-face", "font-face-format", "font-face-name", "font-face-src",
            "font-face-uri", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4",
            "h5", "h6", "head", "header", "hgroup", "hr", "html", "i", "iframe", "image",
            "img", "input", "ins", "kbd", "keygen", "label", "legend", "li", "link",
            "listing", "main", "map", "mark", "marquee", "menu", "meta", "meter",
            "missing-glyph", "model", "nav", "nobr", "noembed", "noframes", "noscript",
            "object", "ol", "optgroup", "option", "output", "p", "param", "picture",
            "plaintext", "pre", "progress", "q", "rb", "rp", "rt", "rtc", "ruby", "s",
            "samp", "script", "search", "section", "select", "selectedcontent", "slot",
            "small", "source", "span", "strike", "strong", "style", "sub", "summary",
            "sup", "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead",
            "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "xmp"
    ));

    private static final Set<String> MATHML_ELEMENTS = new HashSet<>(Arrays.asList(
            "annotation", "annotation-xml", "maction", "maligngroup", "malignmark", "math",
            "menclose", "merror", "mfenced", "mfrac", "mglyph", "mi", "mlabeledtr",
            "mlongdiv", "mmultiscripts", "mn", "mo", "mover", "mpadded", "mphantom",
            "mprescripts", "mroot", "mrow", "ms", "mscarries", "mscarry", "msgroup",
            "msline", "mspace", "msqrt", "msrow", "mstack", "mstyle", "msub", "msubsup",
            "msup", "mtable", "mtd", "mtext", "mtr", "munder", "munderover", "none",
            "semantics"
    ));

    private static final Set<String> SVG_ELEMENTS = new HashSet<>(Arrays.asList(
            "a", "altGlyph", "altGlyphDef", "altGlyphItem", "animate", "animateColor",
            "animateMotion", "animateTransform", "circle", "clipPath", "color-profile",
            "cursor", "defs", "desc", "ellipse", "feBlend", "feColorMatrix",
            "feComponentTransfer", "feComposite", "feConvolveMatrix", "feDiffuseLighting",
            "feDisplacementMap", "feDistantLight", "feDropShadow", "feFlood", "feFuncA",
            "feFuncB", "feFuncG", "feFuncR", "feGaussianBlur", "feImage", "feMerge",
            "feMergeNode", "feMorphology", "feOffset", "fePointLight",
            "feSpecularLighting", "feSpotLight", "feTile", "feTurbulence", "filter",
            "font", "font-face", "font-face-format", "font-face-name", "font-face-src",
            "font-face-uri", "foreignObject", "g", "glyph", "glyphRef", "hkern", "image",
            "line", "linearGradient", "marker", "mask", "metadata", "missing-glyph",
            "mpath", "path", "pattern", "polygon", "polyline", "radialGradient", "rect",
            "script", "set", "stop", "style", "svg", "switch", "symbol", "text",
            "textPath", "title", "tref", "tspan", "use", "view", "vkern"
    ));

    private static final String h1WarningMessage = "Consider using the"
            + " “h1” element as a top-level heading only — or else"
            + " use the “headingoffset” attribute (otherwise, all"
            + " “h1” elements are treated as top-level headings"
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

    public class IdReference {
        private final Locator locator;
        private final String idref;
        private final String elementName;
        private final Attributes attributes;
        public IdReference(Locator locator, String idref, String elementName,
                Attributes attributes) {
            this.locator = locator;
            this.idref = idref;
            this.elementName = elementName;
            this.attributes = attributes;
        }
        public Locator getLocator() { return locator; }
        public String getIdref() { return idref; }
        public String getElementName() { return elementName; }
        public Attributes getAttributes() { return attributes; }
    }

    private class Element {
        private final Locator locator;
        private final String name;
        private final Attributes attributes;
        public Element(Locator locator, String name, Attributes attributes) {
            this.locator = locator;
            this.name = name;
            this.attributes = attributes;
        }
        public Locator getLocator() { return locator; }
        public String getName() { return name; }
        public Attributes getAttributes() { return attributes; }
    }

    private class StackNode {
        private final int ancestorMask;

        private final String name; // null if not HTML

        private final StringBuilder textContent;

        private final String role;

        private List<String> roles;

        private final String activeDescendant;

        private final String forAttr;

        private final Attributes atts;

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
               List<String> roles, String activeDescendant, String forAttr,
               Attributes atts) {
            this.ancestorMask = ancestorMask;
            this.name = name;
            this.role = role;
            this.roles = roles;
            this.activeDescendant = activeDescendant;
            this.forAttr = forAttr;
            this.atts = atts;
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

    private boolean isProhibitedFromBeingNamed(String localName,
            List<String> roles, Attributes atts) {
        if (// https://github.com/validator/validator/issues/1334
                (localName.contains("-") // custom element
                || "a" == localName && atts.getIndex("", "href") == -1)
                || "abbr" == localName
                || ("area" == localName && atts.getIndex("", "href") == -1)
                || "b" == localName
                || "bdi" == localName
                || "bdo" == localName
                || "body" == localName
                || "caption" == localName
                || "cite" == localName
                || "code" == localName
                || "data" == localName
                || "del" == localName
                || "div" == localName
                || "em" == localName
                || "figcaption" == localName
                || "i" == localName
                || "ins" == localName
                || "kbd" == localName
                || "legend" == localName
                || "mark" == localName
                || "p" == localName
                || "pre" == localName
                || "q" == localName
                || "rp" == localName
                || "rt" == localName
                || "s" == localName
                || "samp" == localName
                || "small" == localName
                || "span" == localName
                || "strong" == localName
                || "sub" == localName
                || "sup" == localName
                || "time" == localName
                || "u" == localName
                || "var" == localName) {
                    if (roles != null) {
                        for (String roleValue : roles) {
                            if (ROLES_WHICH_CANNOT_BE_NAMED.contains(roleValue)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }
        return false;
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

    private LinkedHashSet<IdReference> formControlReferences = new LinkedHashSet<>();

    private LinkedHashSet<IdrefLocator> commandForReferences = new LinkedHashSet<>();

    private LinkedHashSet<IdrefLocator> formElementReferences = new LinkedHashSet<>();

    private LinkedHashSet<IdrefLocator> needsAriaOwner = new LinkedHashSet<>();

    private Map<String, Element> formControlIds = new HashMap<>();

    private Map<String, Element> formElementIds = new HashMap<>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<>();

    private Map<String, Element> listIds = new HashMap<>();

    private LinkedHashSet<IdrefLocator> ariaReferences = new LinkedHashSet<>();

    private Map<String, Element> allIds = new HashMap<>();

    private Map<String, Element> tabpanelElements = new HashMap<>();

    private Map<String, Element> tabElementsActive = new HashMap<>();

    private int currentFigurePtr;

    private int currentHeadingPtr;

    private boolean hasHeadingoffset;

    private final LinkedList<Integer> sectioningElementPtrs = new LinkedList<>();

    private boolean hasVisibleMain;

    private boolean hasVisibleMainRole;

    private boolean hasMetaCharset;

    private boolean hasMetaDescription;

    private boolean hasContentTypePragma;

    private boolean hasLinkOrScript;

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
        err("An element with “role=" + role + "”"
                + " must be contained in, or owned by, an element with the "
                + "“role” value "
                + renderRoleSet(REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(role))
                + ".", locator);
    }

    private final void errObsoleteAttribute(String attribute, String element,
            String suggestion) throws SAXException {
        err("The “" + attribute + "” attribute on the “"
                + element + "” element is obsolete." + suggestion);
    }

    private final void warnObsoleteAttribute(String attribute, String element,
            String suggestion) throws SAXException {
        warn("The “" + attribute + "” attribute on the “"
                + element + "” element is obsolete." + suggestion);
    }

    private final void warnExplicitRoleUnnecessaryForType(String element,
            String role, String type) throws SAXException {
        warn("The “" + role + "” role is unnecessary for element"
                + " “" + element + "” whose" + " type is" + " “"
                + type + "”.");
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
                        + " “role=" + ancestorRole + "”.");
            }
        }
    }

    /**
     * @see nu.validator.checker.Checker#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        // label for
        for (IdReference label : formControlReferences) {
            String idref = label.getIdref();
            Element referencedElement = formControlIds.get(idref);
            if (referencedElement == null) {
                err("The value of the “for” attribute of the"
                        + " “label” element must be the ID of a"
                        + " non-hidden form control.",
                        label.getLocator());
            } else if (isLabelableElement(referencedElement.getName(),
                        referencedElement.getAttributes())) {
                Attributes labelAttributes = label.getAttributes();
                if (labelAttributes.getIndex("", "role") > -1) {
                    err("The “role” attribute must not be used on any"
                            + " “label” element that is associated"
                            + " with a labelable element.",
                            label.getLocator());
                }
                for (int i = 0; i < labelAttributes.getLength(); i++) {
                    String attLocal = labelAttributes.getLocalName(i);
                    if (attLocal.startsWith("aria-")) {
                        err("The “" + attLocal + "” attribute must not"
                                + " be used on any “label” element"
                                + " that is associated with a labelable element.",
                                label.getLocator());
                    }
                }
            }
        }

        // button commandfor
        for (IdrefLocator idrefLocator : commandForReferences) {
            if (!allIds.containsKey(idrefLocator.getIdref())) {
                err("The value of the “commandfor” attribute of the"
                        + " “button” element must be the ID of an"
                        + " element in the same tree as the"
                        + " “button” with the “commandfor”"
                        + " attribute.",
                        idrefLocator.getLocator());
            }
        }
        // references to IDs from form attributes
        for (IdrefLocator idrefLocator : formElementReferences) {
            if (!formElementIds.containsKey(idrefLocator.getIdref())) {
                err("The “form” attribute must refer to a form element.",
                        idrefLocator.getLocator());
            }
        }

        // input list
        for (IdrefLocator idrefLocator : listReferences) {
            if (!listIds.containsKey(idrefLocator.getIdref())) {
                err("The “list” attribute of the “input” element must refer to a “datalist” element.",
                        idrefLocator.getLocator());
            }
        }

        tabElements:
        for (Map.Entry<String, Element> tabEntry :
                tabElementsActive.entrySet()) {
            String tabId = tabEntry.getKey();
            Element tab = tabEntry.getValue();
            String controlsValue = tab.getAttributes()
                .getValue("", "aria-controls");
            if (controlsValue != null) {
                if (tabpanelElements.containsKey(controlsValue)) {
                    continue;
                }
            }
            for (Map.Entry<String, Element> tabpanelEntry :
                    tabpanelElements.entrySet()) {
                Element tabpanel = tabpanelEntry.getValue();
                String arialabelledbyValue = tabpanel.getAttributes()
                    .getValue("", "aria-labelledby");
                if (arialabelledbyValue != null) {
                    if (arialabelledbyValue.equals(tabId)) {
                        continue tabElements;
                    }
                }
            }
            err("Every active “role=tab” element must have a"
                    + " corresponding “role=tabpanel” element.",
                    tab.getLocator());
        }

        // ARIA idrefs
        for (IdrefLocator idrefLocator : ariaReferences) {
            if (!allIds.containsKey(idrefLocator.getIdref())) {
                err("The “" + idrefLocator.getAdditional()
                        + "” attribute must point to an element in the same document.",
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

        if (hasTopLevelH1 && !hasHeadingoffset) {
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
        hasHeadingoffset = false;
        StackNode node = pop();
        String systemId = node.locator().getSystemId();
        String publicId = node.locator().getPublicId();
        Locator locator = null;
        openSingleSelects.remove(node);
        openLabels.remove(node);
        openMediaElements.remove(node);
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("figure" == localName) {
                if (node.hasFigcaptionContent() && node.role != null
                        && !"figure".equals(node.role)
                        && !"doc-example".equals(node.role)) {
                    err("A “figure” element with a"
                            + " “figcaption” descendant must not"
                            + " have a “role” attribute.");
                }
                if ((node.needsFigcaption() && !node.hasFigcaptionContent())
                        || node.hasTextNode() || node.hasEmbeddedContent()) {
                    for (Locator imgLocator : node.getImagesLackingAlt()) {
                        err("An “img” element must have an"
                                + " “alt” attribute, except under"
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
                    warn("When a “table” element is the only content"
                            + " in a “figure” element other than the"
                            + " “figcaption”, the “caption”"
                            + " element should be omitted in favor of the"
                            + " “figcaption”.",
                            node.getCaptionNestedInFigure());
                }
            } else if ("picture" == localName) {
                siblingSources.clear();
            } else if ("dialog" == localName
                    || node.atts.getIndex("", "popover") > -1) {
                hasAutofocus = false;
            } else if ("select" == localName && node.isOptionNeeded()) {
                if (!node.hasOption()) {
                    err("A “select” element with a"
                            + " “required” attribute, and without a"
                            + " “multiple” attribute, and without a"
                            + " “size” attribute whose value is"
                            + " greater than"
                            + " “1”, must have a child"
                            + " “option” element.");
                }
                if (node.nonEmptyOptionLocator() != null) {
                    err("The first child “option” element of a"
                            + " “select” element with a"
                            + " “required” attribute, and without a"
                            + " “multiple” attribute, and without a"
                            + " “size” attribute whose value is"
                            + " greater than"
                            + " “1”, must have either an empty"
                            + " “value” attribute, or must have no"
                            + " text content."
                            + " Consider either adding a placeholder option"
                            + " label, or adding a"
                            + " “size” attribute with a value equal"
                            + " to the number of"
                            + " “option” elements.",
                            node.nonEmptyOptionLocator());
                }
            } else if ("section" == localName && !node.hasHeading()) {
                warn("Section lacks heading. Consider using"
                        + " “h2”-“h6” elements to add"
                        + " identifying headings to all sections, or else"
                        + " use a “div” element instead for any"
                        + " cases where no heading is needed.",
                        node.locator());
            } else if ("article" == localName && !node.hasHeading()) {
                warn("Article lacks heading. Consider using"
                        + " “h2”-“h6” elements to add"
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
                if (styleContents.startsWith("\uFEFF")) {
                    styleContents = styleContents.substring(1);
                }
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
                            cssProperty = String.format("“%s”: ",
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
            warn("Attribute “aria-activedescendant” value should "
                    + "either refer to a descendant element, or should "
                    + "be accompanied by attribute “aria-owns”.",
                    locator);
        }
    }

    private boolean isImportMapValid(String scriptContent) throws SAXException {
        JsonStructure importMap;
        try {
            JsonReader reader = Json.createReader(new StringReader(scriptContent));
            importMap = reader.readObject();
        } catch (JsonException e) {
            err("A script “script” with a “type” attribute"
                    + " whose value is “importmap” must have valid"
                    + " JSON content.");
            return false;
        }
        if (!(importMap instanceof Map)) {
            err("A “script” element with a “type” attribute"
                    + " whose value is “importmap” must contain a"
                    + " JSON object.");
            return false;
        }

        final Map<String, Object> importMapObject = (Map<String, Object>) importMap;

        for (Map.Entry<String, Object> importMapEntry : importMapObject.entrySet()) {
            String specifierType = importMapEntry.getKey();
            if (!"imports".equals(specifierType)
                    && !"scopes".equals(specifierType)
                    && !"integrity".equals(specifierType)) {
                err("A “script” element with a “type”"
                        + " attribute whose value is “importmap” must"
                        + " contain a JSON object with no properties other than"
                        + " “imports”, “scopes”,"
                        + " and “integrity”.");
                return false;
            }
            if (!(importMapEntry.getValue() instanceof Map)) {
                err("The value of the “" + specifierType + "”"
                        + " property within the content of a “script”"
                        + " element with a “type” attribute whose"
                        + " value is “importmap” must be a JSON"
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
                        err("The value of the “scopes” property"
                                + " within the content of a “script”"
                                + " element with a “type” attribute"
                                + " whose value is “importmap” must"
                                + " be a JSON object whose keys are valid URL"
                                + " strings.");
                        return false;
                    }
                    if (!(entry.getValue() instanceof Map)) {
                        err("The value of the “scopes” property"
                                + " within the content of a “script”"
                                + " element with a “type” attribute"
                                + " whose value is “importmap” must"
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
            err("A specifier map defined in a “" + type + "”"
                    + " property within the content of a “script”"
                    + " element with a “type” attribute whose value"
                    + " is “importmap” must only contain non-empty"
                    + " keys.");
            return false;
        }
        if (!(value instanceof JsonString)) {
            err("A specifier map defined in a “" + type + "”"
                    + " property within the content of a “script”"
                    + " element with a “type” attribute whose value"
                    + " is “importmap” must only contain string"
                    + " values.");
            return false;
        }
        String sValue = ((JsonString) value).getString();
        if (!isValidURL(sValue)) {
            err("A specifier map defined in a “" + type + "”"
                    + " property within the content of a “script”"
                    + " element with a “type” attribute whose value"
                    + " is “importmap” must only contain valid URL"
                    + " values.");
            return false;
        }
        if (key.endsWith("/") && !sValue.endsWith("/")) {
            err("A specifier map defined in a “" + type + "”"
                    + " property within the content of a “script”"
                    + " element with a “type” attribute whose value"
                    + " is “importmap” must have values that end with"
                    + " “/” when its corresponding key ends with"
                    + " “/”.");
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
        hasVisibleMainRole = false;
        hasMetaCharset = false;
        hasMetaDescription = false;
        hasContentTypePragma = false;
        hasAutofocus = false;
        hasLinkOrScript = false;
        hasTopLevelH1 = false;
        hasAncestorTableIsRoleTableGridOrTreeGrid = false;
        numberOfTemplatesDeep = 0;
        numberOfSvgAelementsDeep = 0;
        hasHeadingoffset = false;
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
        commandForReferences.clear();
        formElementReferences.clear();
        formControlIds.clear();
        formElementIds.clear();
        listReferences.clear();
        listIds.clear();
        ariaReferences.clear();
        allIds.clear();
        tabpanelElements.clear();
        tabElementsActive.clear();
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
                err("The SVG element “a” must not appear as a"
                        + " descendant of another SVG element “a”.");
            }
        }
        Map<String, Element> ids = new HashMap<>();
        String role = null;
        List<String> roles = null;
        String inputTypeVal = null;
        String activeDescendant = null;
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
            warn("Element “math” does not need a"
                    + " “role” attribute.");
        }
        if ("http://www.w3.org/1999/xhtml" == uri
                && !localName.contains("-")
                && !HTML_ELEMENTS.contains(localName)) {
            err("The “" + localName
                    + "” element is a completely-unknown element that"
                    + " is not allowed anywhere in any HTML content.");
        } else if ("http://www.w3.org/2000/svg" == uri
                && !SVG_ELEMENTS.contains(localName)) {
            err("The “" + localName
                    + "” element is a completely-unknown element that"
                    + " is not allowed anywhere in any SVG content.");
        } else if ("http://www.w3.org/1998/Math/MathML" == uri
                && !MATHML_ELEMENTS.contains(localName)) {
            err("The “" + localName
                    + "” element is a completely-unknown element that"
                    + " is not allowed anywhere in any MathML content.");
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
                String attLocal = atts.getLocalName(i);
                boolean isEmptyAriaAttribute = "".equals(atts.getValue(i))
                    && attLocal.startsWith("aria-");
                if (attUri.length() == 0) {
                    if ("name".equals(attLocal)
                            && !ELEMENTS_THAT_CAN_HAVE_A_NAME_ATTRIBUTE
                                .contains(localName)) {
                        info("The “name” attribute is never allowed"
                                + " on the “" + localName + "”"
                                + " element.");
                    }
                    if ("aria-hidden".equals(attLocal) && !isEmptyAriaAttribute) {
                        if (Arrays.binarySearch(
                                ARIA_HIDDEN_NOT_ALLOWED_ELEMENTS,
                                localName) >= 0) {
                            err("The “aria-hidden” attribute must not"
                                    + " be specified on the “" + localName
                                    + "” element.");
                        } else if ("input" == localName
                                && "hidden".equals(atts.getValue("", "type"))) {
                            err("The “aria-hidden” attribute must not"
                                    + " be specified on an “input”"
                                    + " element whose “type”"
                                    + " attribute has the value"
                                    + " “hidden”.");
                        }
                        if ("body".equals(localName) &&
                                "true".equals(atts.getValue(i))) {
                            err("“aria-hidden=true” must not be used"
                                    + " on the “body” element.");
                        }
                    } else if (attLocal.startsWith("aria-") && !isEmptyAriaAttribute) {
                        hasAriaAttributesOtherThanAriaHidden = true;
                    }
                    if (ATTRIBUTES_WITH_IMPLICIT_STATE_OR_PROPERTY.contains(
                            attLocal) && (!isEmptyAriaAttribute || "hidden".equals(attLocal))) {
                        String stateOrProperty = "aria-" + attLocal;
                        if (atts.getIndex("", stateOrProperty) > -1) {
                            String attLocalValue = atts.getValue("", attLocal);
                            String stateOrPropertyValue = atts.getValue("",
                                    stateOrProperty);
                            if ("hidden".equals(attLocal)
                                    && "until-found".equals(attLocalValue)
                                    && "true".equals(stateOrPropertyValue)) {
                                err("Attribute “aria-hidden” with value"
                                        + " “true” must not be specified"
                                        + " on elements with “hidden”"
                                        + " attribute value “until-found”."
                                        + " This combination prevents content from"
                                        + " being accessible to assistive technology"
                                        + " when revealed through search.");
                            } else if ("true".equals(stateOrPropertyValue)
                                    || attLocalValue.equals(
                                            stateOrPropertyValue)) {
                                warn("Attribute “" + stateOrProperty
                                        + "” is unnecessary for elements"
                                        + " that have attribute “"
                                        + attLocal + "”.");
                            } else if ("false".equals(stateOrPropertyValue)) {
                                err("Attribute “" + stateOrProperty
                                        + "” must not be specified on"
                                        + " elements that have attribute"
                                        + " “" + attLocal + "”.");
                            } else if (!attLocalValue.equals(
                                            stateOrPropertyValue)) {
                                err("Attribute “" + stateOrProperty
                                        + "” must not be specified with"
                                        + " a different value than  attribute"
                                        + " “" + attLocal + "”.");
                            }
                        }
                    }
                    if ("embed".equals(localName)) {
                        for (int j = 0; j < attLocal.length(); j++) {
                            char c = attLocal.charAt(j);
                            if (c >= 'A' && c <= 'Z') {
                                err("Bad attribute name “" + attLocal
                                        + "”. Attribute names for the"
                                        + " “embed” element must not"
                                        + " contain uppercase ASCII letters.");
                            }
                        }
                        if (!NCName.isNCName(attLocal)) {
                            err("Bad attribute name “" + attLocal
                                    + "”. Attribute names for the"
                                    + " “embed” element must be"
                                    + " XML-compatible.");
                        }
                    }
                    if ("headingoffset" == attLocal) {
                        hasHeadingoffset = true;
                        if (!atts.getValue(i).matches("[0-8]")) {
                            err("The value of the “headingoffset”"
                                    + " attribute must be a number between"
                                    + " “0” and “8”.");
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
                                            "“%s”: ",
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
                    } else if ("aria-dropeffect" == attLocal || "aria-grabbed" == attLocal) {
                        warn("The “" + attLocal + "” attribute is"
                                + " deprecated and should not be used. Support for"
                                + " it is poor and is unlikely to improve.");
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
                        roles = Arrays.asList(role.trim()
                                .toLowerCase().split("\\s+"));
                    } else if ("aria-activedescendant" == attLocal
                            && !isEmptyAriaAttribute) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal && !isEmptyAriaAttribute) {
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
                                " Use the “rel” attribute instead,"
                                        + " with a term having the opposite meaning.");
                    } else if ("autofocus" == attLocal) {
                        if (hasAutofocus) {
                            err("There must not be two elements with the same"
                                    + " \"nearest ancestor autofocus scoping"
                                    + " root element\" that both have the"
                                    + " “autofocus” attribute"
                                    + " specified.");
                        }
                        hasAutofocus = true;
                    } else if ("autocomplete".equals(attLocal)) {
                        if (atts.getValue(i).contains("webauthn")
                                && !"input".equals(localName)
                                && !"textarea".equals(localName)) {
                            err("The value of the “autocomplete”"
                                    + " attribute for the “" + localName
                                    + "” element must not contain"
                                    + " “webauthn”.");
                                }
                    }
                    if (OBSOLETE_ATTRIBUTES.containsKey(attLocal)
                            && OBSOLETE_ATTRIBUTES.get(attLocal).containsKey(localName)) {
                        String suggestion = OBSOLETE_ATTRIBUTES.get(attLocal).get(localName);
                        warnObsoleteAttribute(attLocal, localName, suggestion.isEmpty() ? "" : " " + suggestion);
                    } else if (OBSOLETE_STYLE_ATTRS.containsKey(attLocal)) {
                        String[] elementNames = OBSOLETE_STYLE_ATTRS.get(
                                attLocal);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            warnObsoleteAttribute(attLocal, localName,
                                    " Use CSS instead.");
                        }
                    } else if (OBSOLETE_GLOBAL_ATTRIBUTES.containsKey(attLocal)) {
                        String suggestion = OBSOLETE_GLOBAL_ATTRIBUTES.get(attLocal);
                        err("The “" + attLocal + "” attribute is obsolete." + (suggestion.isEmpty() ? "" : " " + suggestion));
                    } else if (INPUT_ATTRIBUTES.containsKey(attLocal)
                            && "input" == localName) {
                        String[] allowedTypes = INPUT_ATTRIBUTES.get(attLocal);
                        inputTypeVal = inputTypeVal == null ? "text"
                                : inputTypeVal;
                        if (Arrays.binarySearch(allowedTypes,
                                inputTypeVal) < 0) {
                            err("Attribute “" + attLocal
                                    + "” is only allowed when the input"
                                    + " type is " + renderTypeList(allowedTypes)
                                    + ".");
                        }
                    }
                } else if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    if ("lang" == atts.getLocalName(i)) {
                        xmlLang = atts.getValue(i);
                    }
                }

                if (atts.getType(i) == "ID" || "id" == atts.getLocalName(i)) {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.put(attVal, new Element(
                                    new LocatorImpl(getDocumentLocator()),
                                    localName,
                                    new AttributesImpl(atts)));
                    }
                }
            }

            if (localName.contains("-")) {
                isCustomElement = true;
                if (atts.getIndex("", "is") > -1) {
                    err("Autonomous custom elements must not specify the"
                            + " “is” attribute.");
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
            if ("base".equals(localName) && atts.getIndex("", "href") > -1
                    && hasLinkOrScript) {
                err("The “base” element must come before any"
                        + " “link” or “script” elements"
                        + " in the document.");
            }
            if ("div".equals(localName) && "dl".equals(parentName)
                    && role != null
                    && !role.equals("none")
                    && !role.equals("presentation")) {
                err("A “div” child of a “dl” element must"
                        + " not have any “role” value other than"
                        + " “presentation” or “none”.");
            }
            if ("input".equals(localName)) {
                if (atts.getIndex("", "name") > -1
                        && "isindex".equals(atts.getValue("", "name"))) {
                    err("The value “isindex” for the “name”"
                            + " attribute of the “input” element is"
                            + " not allowed.");
                }
                if (atts.getIndex("", "type") > -1
                        && "hidden".equals(atts.getValue("", "type"))
                        && hasAriaAttributesOtherThanAriaHidden) {
                    err("An “input” element with a “type”"
                            + " attribute whose value is “hidden”"
                            + " must not have any “aria-*”"
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
                                err("An “input” element with a"
                                        + " “type” attribute whose"
                                        + " value is “hidden” must"
                                        + " not have an"
                                        + " “autocomplete” attribute"
                                        + " whose value is “on” or"
                                        + " “off”.");
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
                String sizesVal = atts.getValue("", sizesName);
                String loadingVal = atts.getValue("", "loading");
                boolean isLazyLoaded = "lazy".equals(loadingVal);
                boolean sizesStartsWithAuto = sizesStartsWithAuto(sizesVal);
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
                            // Per HTML spec: sizes is required when srcset has
                            // width descriptors, UNLESS loading=lazy (which
                            // allows auto-sizes). For source elements in
                            // picture, check is deferred until img is seen.
                            if (atts.getIndex("", sizesName) < 0
                                    && "img".equals(localName)
                                    && !isLazyLoaded) {
                                err("When the “" + srcSetName
                                        + "” attribute has any image"
                                        + " candidate string with a width"
                                        + " descriptor, the “" + sizesName
                                        + "” attribute must"
                                        + " also be specified.");
                            }
                            // For link elements, keep the original behavior
                            if (atts.getIndex("", sizesName) < 0
                                    && "link".equals(localName)) {
                                err("When the “" + srcSetName
                                        + "” attribute has any image"
                                        + " candidate string with a width"
                                        + " descriptor, the “" + sizesName
                                        + "” attribute must"
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
                                err("A “source” element that has a"
                                        + " following sibling"
                                        + " “source” element or"
                                        + " “img” element with a"
                                        + " “" + srcSetName + "” attribute"
                                        + " must have a"
                                        + " “media” attribute and/or"
                                        + " “type” attribute.",
                                        locator);
                                siblingSources.remove(locator);
                            } else if (media != null
                                    && "".equals(trimSpaces(media))) {
                                err("Value of “media” attribute here"
                                        + " must not be empty.",
                                        locator);
                            } else if (media != null
                                    && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                            "all", trimSpaces(media))) {
                                err("Value of “media” attribute here"
                                        + " must not be “all”.",
                                        locator);
                            }
                            // Check source elements for sizes=auto without
                            // loading=lazy on the img
                            String sourceSizes = sourceAtts.get("sizes");
                            if (sizesStartsWithAuto(sourceSizes)
                                    && !isLazyLoaded) {
                                err("The “sizes” attribute value"
                                        + " starting with “auto” is"
                                        + " only valid for lazy-loaded images."
                                        + " The “img” element must"
                                        + " have a “loading” attribute"
                                        + " set to “lazy”.",
                                        locator);
                            }
                            // Check source elements for missing sizes when
                            // srcset has width descriptors and img is not lazy
                            String sourceSrcset = sourceAtts.get("srcset");
                            if (sourceSrcset != null && sourceSizes == null
                                    && !isLazyLoaded) {
                                // Check if source srcset has width descriptors
                                try {
                                    ImageCandidateStrings.THE_INSTANCE.checkValid(
                                            sourceSrcset);
                                    if ("1".equals(System.getProperty(
                                            "nu.validator.checker.imageCandidateString.hasWidth"))) {
                                        err("When the “srcset”"
                                                + " attribute has any image"
                                                + " candidate string with a"
                                                + " width descriptor, the"
                                                + " “sizes” attribute"
                                                + " must also be specified.",
                                                locator);
                                    }
                                } catch (DatatypeException e) {
                                    // srcset validation errors handled elsewhere
                                }
                            }
                        }
                    }
                } else if (atts.getIndex("", sizesName) > -1) {
                    err("The “" + sizesName + "” attribute must only"
                            + " be specified if the “" + srcSetName
                            + "” attribute is also specified.");
                }
                // Validate sizes=auto requires loading=lazy for img element
                if ("img".equals(localName) && sizesStartsWithAuto
                        && !isLazyLoaded) {
                    err("The “sizes” attribute value starting with"
                            + " “auto” is only valid for lazy-loaded"
                            + " images. Add “loading=”“lazy”"
                            + " to this element.");
                }
            }

            // Check source elements in picture when img is encountered
            // This handles cases where img doesn't have srcset but sources do
            if ("img".equals(localName) && "picture".equals(parentName)
                    && !siblingSources.isEmpty()) {
                String loadingVal = atts.getValue("", "loading");
                boolean isLazy = "lazy".equals(loadingVal);
                for (Map.Entry<Locator, Map<String, String>> entry : siblingSources.entrySet()) {
                    Locator locator = entry.getKey();
                    Map<String, String> sourceAtts = entry.getValue();
                    String sourceSizes = sourceAtts.get("sizes");
                    String sourceSrcset = sourceAtts.get("srcset");
                    // Check source for sizes=auto without loading=lazy on img
                    if (sizesStartsWithAuto(sourceSizes) && !isLazy) {
                        err("The “sizes” attribute value"
                                + " starting with “auto” is"
                                + " only valid for lazy-loaded images."
                                + " The “img” element must"
                                + " have a “loading” attribute"
                                + " set to “lazy”.",
                                locator);
                    }
                    // Check source for missing sizes when srcset has width
                    // descriptors and img is not lazy-loaded
                    if (sourceSrcset != null && sourceSizes == null && !isLazy) {
                        try {
                            ImageCandidateStrings.THE_INSTANCE.checkValid(
                                    sourceSrcset);
                            if ("1".equals(System.getProperty(
                                    "nu.validator.checker.imageCandidateString.hasWidth"))) {
                                err("When the “srcset” attribute"
                                        + " has any image candidate string"
                                        + " with a width descriptor, the"
                                        + " “sizes” attribute must"
                                        + " also be specified.",
                                        locator);
                            }
                        } catch (DatatypeException e) {
                            // srcset validation errors handled elsewhere
                        }
                    }
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
                    warn("The “aria-selected” attribute should not be"
                            + " used on the “option” element.");
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
                err("The “" + localName + "” element is obsolete."
                        + suggestion);
            }

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "The element “" + localName
                    + "”";
            if ("a" == localName && href) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “a” with the"
                        + " attribute “href”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(
                    localName)) != null) {
                mask = maskAsObject.intValue();
            } else if ("video" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “video” with the"
                        + " attribute “controls”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("audio" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “audio” with the"
                        + " attribute “controls”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("menu" == localName && toolbar) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “menu” with the"
                        + " attribute “type=toolbar”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("img" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “img” with the"
                        + " attribute “usemap”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("object" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "The element “object” with the"
                        + " attribute “usemap”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if ("input" == localName && !hidden) {
                mask = A_BUTTON_MASK;
                checkForInteractiveAncestorRole(descendantUiString);
            } else if (tabindex) {
                mask = A_BUTTON_MASK;
                descendantUiString = "An element with the attribute"
                        + " “tabindex”";
                checkForInteractiveAncestorRole(descendantUiString);
            } else if (role != null && role != ""
                    && Arrays.binarySearch(INTERACTIVE_ROLES, role) >= 0) {
                mask = A_BUTTON_MASK;
                descendantUiString = "An element with the attribute “"
                        + "role=" + role + "”";
                checkForInteractiveAncestorRole(descendantUiString);
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (String ancestor : SPECIAL_ANCESTORS) {
                        if ((maskHit & 1) != 0) {
                            err(descendantUiString + " must not appear as a"
                                    + " descendant of the “" + ancestor
                                    + "” element.");
                        }
                        maskHit >>= 1;
                    }
                }
            }
            if (Arrays.binarySearch(INTERACTIVE_ELEMENTS, localName) >= 0) {
                checkForInteractiveAncestorRole(
                        "The element “" + localName + "”");
            }

            if (roles !=null && roles.contains("tabpanel")) {
                tabpanelElements.put(id, new Element(
                            new LocatorImpl(getDocumentLocator()),
                            localName,
                            new AttributesImpl(atts)));
            }

            if (roles != null && roles.contains("tab")
                    && "true".equals(atts.getValue("", "aria-selected"))) {
                tabElementsActive.put(id, new Element(
                            new LocatorImpl(getDocumentLocator()),
                            localName,
                            new AttributesImpl(atts)));
            }

            if (role != null && !role.isEmpty()) {
                for (Map.Entry<String, String[]> attributeAndRoles : ARIA_DEPRECATED_ATTRIBUTES_BY_ROLE.entrySet()) {
                    if (atts.getIndex("", attributeAndRoles.getKey()) >= 0
                            && Arrays.binarySearch(attributeAndRoles.getValue(),
                                    role) >= 0) {
                        warn("The “" + attributeAndRoles.getKey()
                                + "” attribute should not be used on any"
                                + " element which has “role=" + role + "”.");
                    }
                }
                // Check for multiple main roles in document
                if (roles.contains("main") && atts.getIndex("", "hidden") < 0) {
                    if (hasVisibleMainRole) {
                        warn("A document should not include more than one visible"
                                + " element with “role=main”.");
                    }
                    hasVisibleMainRole = true;
                }
            }

            if ("li" == localName && roles != null && roles.size() > 0) {
                List<String> ancestorRoles = new ArrayList<>();
                for (int i = 0; i < currentPtr; i++) {
                    if (stack[currentPtr - i] != null
                            && stack[currentPtr - i].roles != null) {
                        ancestorRoles.addAll(stack[currentPtr - i].roles);
                    }
                }
                if (ancestorRoles.size() == 0
                        || ancestorRoles.contains("list")) {
                    err("An “li” element that is a descendant of"
                            + " a “ul”, “ol”,"
                            + " or “menu” element with no explicit"
                            + " “role” value, or a descendant of a"
                            + " “role=list” element, must not have any"
                            + " “role” value other than"
                            + " “listitem”.");
                }
                if (!roles.contains("none")
                        && !roles.contains("presentation")
                        && !roles.contains("generic")) {
                    if (ancestorRoles.contains("tablist")
                            && !roles.contains("tab")) {
                        err("An “li” element that is a descendant of"
                                + " a “role=tablist” element must not"
                                + " have any “role” value other than"
                                + " “tab”.");
                            }
                    if (ancestorRoles.contains("tree")
                            && !roles.contains("treeitem")) {
                        err("An “li” element that is a descendant of"
                                + " a “role=tree” element must not"
                                + " have any “role” value other than"
                                + " “treeitem”.");
                            }
                    if ((ancestorRoles.contains("listbox")
                                || ancestorRoles.contains("list"))
                            && !roles.contains("group")
                            && !roles.contains("option")) {
                        err("An “li” element that is a descendant of"
                                + " a “role=listbox” element"
                                + " or “role=list” element must not"
                                + " have any “role” value other than"
                                + " “group” or “option”.");
                            }
                    if ((ancestorRoles.contains("menu") ||
                                ancestorRoles.contains("menubar"))
                            && !roles.contains("group")
                            && !roles.contains("menuitem")
                            && !roles.contains("menuitemcheckbox")
                            && !roles.contains("menuitemradio")
                            && !roles.contains("separator")) {
                        err("An “li” element that is a descendant of"
                                + " a “role=menu” element"
                                + " or “role=menubar” element must not"
                                + " have any “role” value other than"
                                + " “group”,"
                                + " “menuitem”,"
                                + " “menuitemcheckbox”,"
                                + " “menuitemradio”,"
                                + " or “separator”.");
                    }
                }
            }

            // Ancestor requirements/restrictions
            if ("area" == localName && ((ancestorMask & MAP_MASK) == 0)) {
                err("The “area” element must have a “map” ancestor.");
            } else if ("img" == localName) {
                String titleVal = atts.getValue("", "title");
                if (ismap && ((ancestorMask & HREF_MASK) == 0)) {
                    err("The “img” element with the "
                            + "“ismap” attribute set must have an "
                            + "“a” ancestor with the "
                            + "“href” attribute.");
                }
                if (atts.getIndex("", "alt") < 0
                        && atts.getIndex("", "aria-label") < 0
                        && atts.getIndex("", "aria-labelledby") < 0) {
                    if (role != null) {
                        err("An “img” element with a “role”"
                                + " attribute must also have an accessible"
                                + " name (e.g., an “alt” attribute).");
                    } else if (hasAriaAttributesOtherThanAriaHidden) {
                        err("An “img” element with any"
                                + " “aria-*” attributes"
                                + " other than “aria-hidden”"
                                + " must also have an accessible name."
                                + " (e.g., an “alt” attribute).");
                    } else if ((titleVal == null || "".equals(titleVal))) {
                        if ((ancestorMask & FIGURE_MASK) == 0) {
                            err("An “img” element must have an"
                                    + " “alt” attribute, except under"
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
                        err("An “img” element with a “role”"
                                + " attribute must not have an “alt”"
                                + " attribute whose value is the empty string.");
                    } else {
                        // img with alt="some text" and role
                        for (String roleValue : roles) {
                            if ("none".equals(roleValue)
                                    || "presentation".equals(roleValue)) {
                                err("Bad value “" + roleValue + "”"
                                        + " for attribute “role” on"
                                        + " element “img”");
                            }
                        }
                    }
                }
            } else if ("table" == localName) {
                if (role == null // implicit role=table
                        || "table".equals(role) || "grid".equals(role)
                        || "treegrid".equals(role)) {
                    hasAncestorTableIsRoleTableGridOrTreeGrid = true;
                } else if ("presentation".equals(role)) {
                    hasAncestorTableIsRoleTableGridOrTreeGrid = false;
                }
            } else if (hasAncestorTableIsRoleTableGridOrTreeGrid
                    && atts.getIndex("", "role") >= 0 && ("td" == localName
                            || "tr" == localName || "th" == localName)) {
                err("The “role” attribute must not be used"
                        + " on a “" + localName + "” element"
                        + " which has a “table” ancestor with"
                        + " no “role” attribute, or with a"
                        + " “role” attribute whose value is"
                        + " “table”, “grid”,"
                        + " or “treegrid”.");
            } else if ("track" == localName
                    && atts.getIndex("", "default") >= 0) {
                for (Map.Entry<StackNode, TaintableLocatorImpl> entry : openMediaElements.entrySet()) {
                    StackNode node = entry.getKey();
                    TaintableLocatorImpl locator = entry.getValue();
                    if (node.isTrackDescendant()) {
                        err("The “default” attribute must not occur"
                                + " on more than one “track” element"
                                + " within the same “audio” or"
                                + " “video” element.");
                        if (!locator.isTainted()) {
                            warn("“audio” or “video” element"
                                    + " has more than one “track” child"
                                    + " element with a “default” attribute.",
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
                        err("The “main” element must not appear as a"
                                + " descendant of the “" + ancestorName
                                + "” element.");
                    }
                }
                if (atts.getIndex("", "hidden") < 0) {
                    if (hasVisibleMain) {
                        err("A document must not include more than one visible"
                                + " “main” element.");
                    }
                    hasVisibleMain = true;
                    hasVisibleMainRole = true; // <main> has implicit role="main"
                }
            } else if ("h1" == localName && !hasHeadingoffset) {
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
                            err("The value of the  “value” attribute must be less than or equal to one when the “max” attribute is absent.");
                        }
                    } else {
                        if (!(value <= max)) {
                            err("The value of the  “value” attribute must be less than or equal to the value of the “max” attribute.");
                        }
                    }
                }
                if (atts.getIndex("", "aria-valuemax") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemax"))) {
                    if (atts.getIndex("", "max") >= 0) {
                        err("The “aria-valuemax” attribute must not"
                                + " be used on an element which has a"
                                + " “max” attribute.");
                    } else {
                        warn("The “aria-valuemax” attribute should"
                                + " not be used on a “progress”"
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
                    err("The value of the “min” attribute must be less than or equal to the value of the “value” attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(value)
                        && !(0 <= value)) {
                    err("The value of the “value” attribute must be greater than or equal to zero when the “min” attribute is absent.");
                }
                if (!Double.isNaN(value) && !Double.isNaN(max)
                        && !(value <= max)) {
                    err("The value of the “value” attribute must be less than or equal to the value of the “max” attribute.");
                }
                if (!Double.isNaN(value) && Double.isNaN(max)
                        && !(value <= 1)) {
                    err("The value of the “value” attribute must be less than or equal to one when the “max” attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && !(min <= max)) {
                    err("The value of the “min” attribute must be less than or equal to the value of the “max” attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(max) && !(0 <= max)) {
                    err("The value of the “max” attribute must be greater than or equal to zero when the “min” attribute is absent.");
                }
                if (!Double.isNaN(min) && Double.isNaN(max) && !(min <= 1)) {
                    err("The value of the “min” attribute must be less than or equal to one when the “max” attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(low) && !(min <= low)) {
                    err("The value of the “min” attribute must be less than or equal to the value of the “low” attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(low) && !(0 <= low)) {
                    err("The value of the “low” attribute must be greater than or equal to zero when the “min” attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(high)
                        && !(min <= high)) {
                    err("The value of the “min” attribute must be less than or equal to the value of the “high” attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(high) && !(0 <= high)) {
                    err("The value of the “high” attribute must be greater than or equal to zero when the “min” attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(high)
                        && !(low <= high)) {
                    err("The value of the “low” attribute must be less than or equal to the value of the “high” attribute.");
                }
                if (!Double.isNaN(high) && !Double.isNaN(max)
                        && !(high <= max)) {
                    err("The value of the “high” attribute must be less than or equal to the value of the “max” attribute.");
                }
                if (!Double.isNaN(high) && Double.isNaN(max) && !(high <= 1)) {
                    err("The value of the “high” attribute must be less than or equal to one when the “max” attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(max) && !(low <= max)) {
                    err("The value of the “low” attribute must be less than or equal to the value of the “max” attribute.");
                }
                if (!Double.isNaN(low) && Double.isNaN(max) && !(low <= 1)) {
                    err("The value of the “low” attribute must be less than or equal to one when the “max” attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(min <= optimum)) {
                    err("The value of the “min” attribute must be less than or equal to the value of the “optimum” attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(0 <= optimum)) {
                    err("The value of the “optimum” attribute must be greater than or equal to zero when the “min” attribute is absent.");
                }
                if (!Double.isNaN(optimum) && !Double.isNaN(max)
                        && !(optimum <= max)) {
                    err("The value of the “optimum” attribute must be less than or equal to the value of the “max” attribute.");
                }
                if (!Double.isNaN(optimum) && Double.isNaN(max)
                        && !(optimum <= 1)) {
                    err("The value of the “optimum” attribute must be less than or equal to one when the “max” attribute is absent.");
                }
                if (atts.getIndex("", "aria-valuemin") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemin"))) {
                    if (atts.getIndex("", "min") >= 0) {
                        err("The “aria-valuemin” attribute must not"
                                + " be used on an element which has a"
                                + " “min” attribute.");
                    } else {
                        warn("The “aria-valuemin” attribute should"
                                + " not be used on a “meter”"
                                + " element.");
                    }
                }
                if (atts.getIndex("", "aria-valuemax") >= 0
                        && !"".equals(atts.getValue("", "aria-valuemax"))) {
                    if (atts.getIndex("", "max") >= 0) {
                        err("The “aria-valuemax” attribute must not"
                                + " be used on an element which has a"
                                + " “max” attribute.");
                    } else {
                        warn("The “aria-valuemax” attribute should"
                                + " not be used on a “meter”"
                                + " element.");
                    }
                }
            }

            // map required attrs
            else if ("map" == localName && id != null) {
                String nameVal = atts.getValue("", "name");
                if (nameVal != null && !nameVal.equals(id)) {
                    err("The “id” attribute on a “map” element must have an the same value as the “name” attribute.");
                }
            }
            else if ("form" == localName) {
                if (atts.getIndex("", "accept-charset") >= 0) {
                    if (!"utf-8".equals(
                            atts.getValue("", "accept-charset").toLowerCase())) {
                        err("The only allowed value for the"
                                + " “accept-charset” attribute for"
                                + " the “form” element is"
                                + " “utf-8”.");
                    }
                }
            }
            // script
            else if ("script" == localName) {
                hasLinkOrScript = true;
                // script language
                if (languageJavaScript && typeNotTextJavaScript) {
                    err("A “script” element with the “language=\"JavaScript\"” attribute set must not have a “type” attribute whose value is not “text/javascript”.");
                }
                if (atts.getIndex("", "charset") >= 0) {
                    warnObsoleteAttribute("charset", "script", "");
                    if (!"utf-8".equals(
                            atts.getValue("", "charset").toLowerCase())) {
                        err("The only allowed value for the “charset”"
                                + " attribute for the “script”"
                                + " element is “utf-8”. (But the"
                                + " attribute is not needed and should be"
                                + " omitted altogether.)");
                    }
                }

                // Determine script type
                String scriptType = "";
                if (atts.getIndex("", "type") > -1) {
                    scriptType = atts.getValue("", "type").toLowerCase();
                }
                boolean hasSrc = atts.getIndex("", "src") >= 0;
                boolean isClassicScript = scriptType.isEmpty() || 
                        JAVASCRIPT_MIME_TYPES.contains(scriptType);
                boolean isModuleScript = "module".equals(scriptType);
                boolean isImportMap = "importmap".equals(scriptType);
                boolean isSpeculationRules = "speculationrules".equals(scriptType);
                boolean isDataBlock = !scriptType.isEmpty() && 
                        !isClassicScript && !isModuleScript && !isImportMap &&
                        !isSpeculationRules;

                // Validate attributes based on script type
                if (isImportMap) {
                    // Import maps: only inline, no other script-specific attributes
                    if (hasSrc) {
                        err("A “script” element with a"
                                + " “type” attribute whose value"
                                + " is “importmap” must not have"
                                + " a “src” attribute.");
                    }
                    if (atts.getIndex("", "async") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " an “async” attribute.");
                    }
                    if (atts.getIndex("", "nomodule") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “nomodule” attribute.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “defer” attribute.");
                    }
                    if (atts.getIndex("", "crossorigin") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “crossorigin” attribute.");
                    }
                    if (atts.getIndex("", "integrity") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " an “integrity” attribute.");
                    }
                    if (atts.getIndex("", "referrerpolicy") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “referrerpolicy” attribute.");
                    }
                    if (atts.getIndex("", "fetchpriority") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “fetchpriority” attribute.");
                    }
                    if (atts.getIndex("", "blocking") >= 0) {
                        err("A “script” element with"
                                + " “type=importmap” must not have"
                                + " a “blocking” attribute.");
                    }
                    parsingScriptImportMap = true;
                } else if (isSpeculationRules) {
                    // Speculation rules: only inline, no other script-specific attributes
                    if (atts.getIndex("", "nomodule") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “nomodule” attribute.");
                    }
                    if (atts.getIndex("", "async") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " an “async” attribute.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “defer” attribute.");
                    }
                    if (atts.getIndex("", "blocking") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “blocking” attribute.");
                    }
                    if (atts.getIndex("", "crossorigin") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “crossorigin” attribute.");
                    }
                    if (atts.getIndex("", "referrerpolicy") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “referrerpolicy” attribute.");
                    }
                    if (atts.getIndex("", "integrity") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " an “integrity” attribute.");
                    }
                    if (atts.getIndex("", "fetchpriority") >= 0) {
                        err("A “script” element with"
                                + " “type=speculationrules” must not have"
                                + " a “fetchpriority” attribute.");
                    }
                } else if (isDataBlock) {
                    // Data blocks: no script-specific attributes
                    if (hasSrc) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “src” attribute.");
                    }
                    if (atts.getIndex("", "async") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " an “async” attribute.");
                    }
                    if (atts.getIndex("", "nomodule") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “nomodule” attribute.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “defer” attribute.");
                    }
                    if (atts.getIndex("", "crossorigin") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “crossorigin” attribute.");
                    }
                    if (atts.getIndex("", "integrity") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " an “integrity” attribute.");
                    }
                    if (atts.getIndex("", "referrerpolicy") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “referrerpolicy” attribute.");
                    }
                    if (atts.getIndex("", "fetchpriority") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “fetchpriority” attribute.");
                    }
                    if (atts.getIndex("", "blocking") >= 0) {
                        err("A “script” element with a"
                                + " “type” attribute whose value is"
                                + " neither a JavaScript MIME type, “module”,"
                                + " “importmap”, nor “speculationrules”"
                                + " (i.e., a data block) must not have"
                                + " a “blocking” attribute.");
                    }
                } else if (isModuleScript) {
                    // Module scripts
                    if (atts.getIndex("", "nomodule") >= 0) {
                        err("A “script” element with a"
                                + " “nomodule” attribute must not have a"
                                + " “type” attribute with the value"
                                + " “module”.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("A “script” element with"
                                + " “type=module” must not have"
                                + " a “defer” attribute.");
                    }
                    if (!hasSrc) {
                        // Inline module script
                        if (atts.getIndex("", "integrity") >= 0) {
                            err("An inline “script” element with"
                                    + " “type=module” must not have"
                                    + " an “integrity” attribute.");
                        }
                        if (atts.getIndex("", "fetchpriority") >= 0) {
                            err("An inline “script” element with"
                                    + " “type=module” must not have"
                                    + " a “fetchpriority” attribute.");
                        }
                        if (atts.getIndex("", "blocking") >= 0) {
                            err("An inline “script” element with"
                                    + " “type=module” must not have"
                                    + " a “blocking” attribute.");
                        }
                    }
                } else if (isClassicScript) {
                    // Classic scripts
                    if (scriptType.isEmpty() || JAVASCRIPT_MIME_TYPES.contains(scriptType)) {
                        if (!scriptType.isEmpty()) {
                            warn("The “type” attribute is unnecessary for"
                                    + " JavaScript resources.");
                        }
                    }
                    if (!hasSrc) {
                        // Inline classic script
                        if (atts.getIndex("", "defer") >= 0) {
                            err("An inline “script” element"
                                    + " (i.e., a “script” element without"
                                    + " a “src” attribute and with a"
                                    + " “type” attribute that is either"
                                    + " unspecified, empty, or a JavaScript MIME type)"
                                    + " must not have a “defer” attribute.");
                        }
                        if (atts.getIndex("", "async") >= 0) {
                            err("An inline classic “script” element"
                                    + " (i.e., a “script” element without"
                                    + " a “src” attribute and with a"
                                    + " “type” attribute that is either"
                                    + " unspecified, empty, or a JavaScript MIME type)"
                                    + " must not have an “async” attribute.");
                        }
                        if (atts.getIndex("", "integrity") >= 0) {
                            err("An inline classic “script” element"
                                    + " (i.e., a “script” element without"
                                    + " a “src” attribute and with a"
                                    + " “type” attribute that is either"
                                    + " unspecified, empty, or a JavaScript MIME type)"
                                    + " must not have an “integrity” attribute.");
                        }
                        if (atts.getIndex("", "fetchpriority") >= 0) {
                            err("An inline classic “script” element"
                                    + " (i.e., a “script” element without"
                                    + " a “src” attribute and with a"
                                    + " “type” attribute that is either"
                                    + " unspecified, empty, or a JavaScript MIME type)"
                                    + " must not have a “fetchpriority” attribute.");
                        }
                        if (atts.getIndex("", "blocking") >= 0) {
                            err("An inline classic “script” element"
                                    + " (i.e., a “script” element without"
                                    + " a “src” attribute and with a"
                                    + " “type” attribute that is either"
                                    + " unspecified, empty, or a JavaScript MIME type)"
                                    + " must not have a “blocking” attribute.");
                        }
                    }
                }

                // charset validation (inline scripts must not have charset)
                if (!hasSrc && atts.getIndex("", "charset") >= 0) {
                    err("Element “script” must not have attribute"
                            + " “charset” unless attribute “src”"
                            + " is also specified.");
                }
            }
            else if ("style" == localName) {
                if (atts.getIndex("", "type") > -1) {
                    String styleType = atts.getValue("", "type").toLowerCase();
                    if ("text/css".equals(styleType)) {
                        warn("The “type” attribute for the"
                                + " “style” element is not needed and"
                                + " should be omitted.");
                    } else {
                        err("The only allowed value for the “type”"
                                + " attribute for the “style” element"
                                + " is “text/css” (with no"
                                + " parameters). (But the attribute is not"
                                + " needed and should be omitted altogether.)");
                    }
                }
            }

            else if ("bdo" == localName) {
                if (atts.getIndex("", "dir") < 0) {
                    err("Element “bdo” must have attribute"
                            + " “dir”.");
                } else if ("auto".equals(
                            atts.getValue("", "dir").toLowerCase())) {
                    err("The value of “dir” attribute for the"
                            + " “bdo” element must not be"
                            + " “auto”.");
                }
            }

            // labelable elements
            if (isLabelableElement(localName, atts)) {
                for (Map.Entry<StackNode, Locator> entry : openLabels.entrySet()) {
                    StackNode label = entry.getKey();
                    Locator locator = entry.getValue();
                    Attributes labelAttributes = label.atts;
                    if (labelAttributes.getIndex("", "role") > -1) {
                        err("The “role” attribute must not be used on any"
                                + " “label” element that is an"
                                + " ancestor of a labelable element.",
                                locator);
                    }
                    for (int i = 0; i < labelAttributes.getLength(); i++) {
                        String attLocal = labelAttributes.getLocalName(i);
                        if (attLocal.startsWith("aria-")) {
                            err("The “" + attLocal + "” attribute must not"
                                    + " be used on any “label” element"
                                    + " that is an ancestor of a labelable element.",
                                    locator);
                        }
                    }
                    if (label.isLabeledDescendants()) {
                        err("The “label” element may contain at most"
                                + " one “button”, “input”,"
                                + " “meter”, “output”,"
                                + " “progress”, “select”,"
                                + " or “textarea” descendant.");
                        warn("“label” element with multiple labelable"
                                + " descendants.", locator);
                    } else {
                        label.setLabeledDescendants();
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
                        err("Any “" + localName
                                + "” descendant of a “label”"
                                + " element with a “for” attribute"
                                + " must have an ID value that matches that"
                                + " “for” attribute.");
                    }
                }
            }

            // lang and xml:lang for XHTML5
            if (lang != null && xmlLang != null
                    && !equalsIgnoreAsciiCase(lang, xmlLang)) {
                err("When the attribute “lang” in no namespace and the attribute “lang” in the XML namespace are both present, they must have the same value.");
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
                listIds.putAll(ids);
            }

            // label for
            if ("label" == localName) {
                String forVal = atts.getValue("", "for");
                if (forVal != null) {
                    formControlReferences.add(new IdReference(
                                new LocatorImpl(getDocumentLocator()),
                                forVal, localName, new AttributesImpl(atts)));
                }
            }

            // button commandfor
            if ("button" == localName) {
                String commandforVal = atts.getValue("", "commandfor");
                if (commandforVal != null) {
                    commandForReferences.add(new IdrefLocator(
                            new LocatorImpl(getDocumentLocator()), commandforVal));
                }
            }

            if ("form" == localName) {
                formElementIds.putAll(ids);
                if (atts.getIndex("action") > -1
                        && "".equals(atts.getValue("", "action"))) {
                    info("To set the document\u2019s location as the action for"
                            + " a form, omit the “action” attribute.");
                }
            }

            if ("area" == localName) {
                if (atts.getIndex("href") < 0 && atts.getIndex("alt") > -1) {
                    info("Either remove the “alt” attribute from this"
                            + " “area” element, or else, add an"
                            + " “href” attribute.");
                }
            }

            if (("button" == localName //
                    || "input" == localName && !hidden) //
                    || "meter" == localName //
                    || "output" == localName //
                    || "progress" == localName //
                    || "select" == localName //
                    || "textarea" == localName //
                    || isCustomElement) {
                formControlIds.putAll(ids);
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
                if ("button".equals(role )
                        && atts.getIndex("", "aria-pressed") < 0) {
                    err("An “input” element with a “type”"
                            + " attribute whose value is “checkbox”"
                            + " and with a “role” attribute whose"
                            + " value is “button” must have an"
                            + " “aria-pressed” attribute.");
                }
            }

            // input@type=button
            if ("input" == localName
                    && AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                            "button", atts.getValue("", "type"))) {
                if (atts.getValue("", "value") == null
                        || "".equals(atts.getValue("", "value"))) {
                    err("Element “input” with attribute “type” whose value is “button” must have non-empty attribute “value”.");
                }
            }

            // track
            if ("track" == localName) {
                if ("".equals(atts.getValue("", "label"))) {
                    err("Attribute “label” for element “track” must have non-empty value.");
                }
            }

            // multiple selected options
            if ("option" == localName && selected) {
                for (Map.Entry<StackNode, Locator> entry : openSingleSelects.entrySet()) {
                    StackNode node = entry.getKey();
                    if (node.isSelectedOptions()) {
                        err("The “select” element cannot have more than one selected “option” descendant unless the “multiple” attribute is specified.");
                    } else {
                        node.setSelectedOptions();
                    }
                }
            }
            if ("meta" == localName) {
                if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "content-language", atts.getValue("", "http-equiv"))) {
                    err("Using the “meta” element to specify the"
                            + " document-wide default language is obsolete."
                            + " Consider specifying the language on the root"
                            + " element instead.");
                } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "x-ua-compatible", atts.getValue("", "http-equiv"))
                        && !AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "ie=edge", atts.getValue("", "content"))) {
                    err("A “meta” element with an"
                            + " “http-equiv” attribute whose value is"
                            + " “X-UA-Compatible”" + " must have a"
                            + " “content” attribute with the value"
                            + " “IE=edge”.");
                }
                if (atts.getIndex("", "charset") > -1) {
                    if (!"utf-8".equals(
                            atts.getValue("", "charset").toLowerCase())) {
                        err("The only allowed value for the “charset”"
                                + " attribute for the “meta”"
                                + " element is “utf-8”.");
                    }
                    if (hasMetaCharset) {
                        err("A document must not include more than one"
                                + " “meta” element with a"
                                + " “charset” attribute.");
                    }
                    if (hasContentTypePragma) {
                        err("A document must not include both a"
                                + " “meta” element with an"
                                + " “http-equiv” attribute"
                                + " whose value is “content-type”,"
                                + " and a “meta” element with a"
                                + " “charset” attribute.");
                    }
                    hasMetaCharset = true;
                }
                if (atts.getIndex("", "name") > -1) {
                    if (atts.getIndex("", "itemprop") > -1) {
                        info("Either remove the “itemprop” attribute"
                                + " from this “meta” element, or else,"
                                + " remove the “name” attribute."
                                + " Exactly one of the “name”,"
                                + " “http-equiv”, “charset”,"
                                + " and “itemprop” attributes must"
                                + " be specified.");
                    }
                    if ("description".equals(atts.getValue("", "name"))) {
                        if (hasMetaDescription) {
                            err("A document must not include more than one"
                                    + " “meta” element with its"
                                    + " “name” attribute set to the"
                                    + " value “description”.");
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
                                + " “meta” element with an"
                                + " “http-equiv” attribute"
                                + " whose value is “content-type”,"
                                + " and a “meta” element with a"
                                + " “charset” attribute.");
                    }
                    if (hasContentTypePragma) {
                        err("A document must not include more than one"
                                + " “meta” element with a"
                                + " “http-equiv” attribute"
                                + " whose value is “content-type”.");
                    }
                    hasContentTypePragma = true;
                }
                if (atts.getIndex("", "media") > -1
                        && (atts.getIndex("", "name") <= -1
                                || !atts.getValue("", "name").equalsIgnoreCase(
                                        "theme-color"))) {
                    err("A “meta” element with a “media”"
                            + " attribute must have a “name”"
                            + " attribute whose value is"
                            + " “theme-color”.");
                }
            }
            if ("link" == localName) {
                hasLinkOrScript = true;
                boolean hasRel = false;
                List<String> relList = new ArrayList<>();
                if (atts.getIndex("", "rel") > -1) {
                    hasRel = true;
                    Collections.addAll(relList, //
                            atts.getValue("", "rel") //
                            .toLowerCase().split("\\s+"));
                    if (atts.getIndex("", "itemprop") > -1) {
                        info("Either remove the “itemprop” attribute"
                                + " from this “link” element, or else,"
                                + " remove the “rel” attribute."
                                + " A “link” element must have either"
                                + " a “rel” attribute, or an"
                                + " “itemprop” attribute, but not both.");
                    }
                }
                if (atts.getIndex("", "href") == -1
                        && atts.getIndex("", "imagesrcset") == -1
                        && atts.getIndex("", "resource") == -1) { //rdfa
                    err("A “link” element must have an"
                                + " “href” or “imagesrcset”"
                                + " attribute, or both.");
                }
                if (relList.contains("preload")
                        && atts.getIndex("", "as") < 0) {
                    err("A “link” element with a"
                            + " “rel” attribute that contains the"
                            + " value “preload” must have an"
                            + " “as” attribute.");
                }
                if (atts.getIndex("", "as") > -1 //
                        && (!(relList.contains("preload")
                                || relList.contains("modulepreload")) //
                                || !hasRel)) {
                    err("A “link” element with an"
                            + " “as” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “preload” or the value"
                            + " “modulepreload”.");
                }
                if (atts.getIndex("", "integrity") > -1
                        && (!(relList.contains("stylesheet")
                                || relList.contains("preload")
                                || relList.contains("modulepreload")) //
                                || !hasRel)) {
                    err("A “link” element with an"
                            + " “integrity” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “stylesheet” or the value"
                            + " “preload” or the value"
                            + " “modulepreload”.");
                }
                if (atts.getIndex("", "disabled") > -1
                        && (!relList.contains("stylesheet") //
                                || !hasRel)) {
                    err("A “link” element with a"
                            + " “disabled” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “stylesheet”.");
                }
                if (atts.getIndex("", "sizes") > -1
                        && (!(relList.contains("icon")
                                || relList.contains("apple-touch-icon")
                                || relList.contains(
                                        "apple-touch-icon-precomposed"))
                                || !hasRel)) {
                    err("A “link” element with a"
                            + " “sizes” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “icon” or the value"
                            + " “apple-touch-icon” or the value"
                            + " “apple-touch-icon-precomposed”.");
                }
                if (atts.getIndex("", "color") > -1
                        && (!relList.contains("mask-icon") //
                                || !hasRel)) {
                    err("A “link” element with a"
                            + " “color” attribute must have a"
                            + " “rel” attribute that contains"
                            + " the value “mask-icon”.");
                }
                if (atts.getIndex("", "scope") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A “link” element with a"
                            + " “scope” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “serviceworker”.");
                }
                if (atts.getIndex("", "updateviacache") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A “link” element with an"
                            + " “updateviacache” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “serviceworker”.");
                }
                if (atts.getIndex("", "workertype") > -1
                        && (!relList.contains("serviceworker") //
                                || !hasRel)) {
                    err("A “link” element with a"
                            + " “workertype” attribute must have a"
                            + " “rel” attribute that contains the"
                            + " value “serviceworker”.");
                }
                if (atts.getIndex("", "imagesrcset") > -1
                        && !(relList.contains("preload")
                        || !hasRel)) {
                    err("A “link” element with an"
                                + " “imagesrcset” attribute must have a"
                                + " “rel” attribute that contains the"
                                + " value “preload”.");
                }
                if (atts.getIndex("", "imagesizes") > -1
                        && !(relList.contains("preload")
                        || !hasRel)) {
                    err("A “link” element with an"
                                + " “imagesizes” attribute must have a"
                                + " “rel” attribute that contains the"
                                + " value “preload”.");
                }
                if (atts.getIndex("", "imagesrcset") > -1
                        && (atts.getIndex("", "as") == -1
                        || !atts.getValue("", "as").equalsIgnoreCase("image"))) {
                    err("A “link” element with an"
                                + " “imagesrcset” attribute must have an"
                                + " “as” attribute with value “image”.");
                }
                if (atts.getIndex("", "imagesizes") > -1
                        && (atts.getIndex("", "as") == -1
                        || !atts.getValue("", "as").equalsIgnoreCase("image"))) {
                    err("A “link” element with an"
                                + " “imagesizes” attribute must have an"
                                + " “as” attribute with value “image”.");
                }
                if (relList.contains("alternate")
                        && relList.contains("stylesheet")
                        && (atts.getIndex("", "title") == -1
                            || "".equals(atts.getValue("", "title")))) {
                    err("A “link” element with a"
                                + " “rel” attribute that"
                                + " contains both the values"
                                + " “alternate” and"
                                + " “stylesheet” must have a"
                                + " “title” attribute with a"
                                + " non-empty value.");
                }
                if (atts.getIndex("", "fetchpriority") > -1
                        && Arrays.stream(EXTERNAL_RESOURCE_LINK_REL).noneMatch(relList::contains)) {
                    warn("A “link” element with “fetchpriority”"
                                + " attribute should have a “rel”"
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
                    err("A “link” element must not appear"
                            + " as a descendant of a “body” element"
                            + " unless the “link” element has an"
                            + " “itemprop” attribute or has a"
                            + " “rel” attribute whose value contains"
                            + " “dns-prefetch”,"
                            + " “modulepreload”,"
                            + " “pingback”,"
                            + " “preconnect”,"
                            + " “prefetch”,"
                            + " “preload”,"
                            + " “prerender”, or"
                            + " “stylesheet”.");
                }
                if (atts.getIndex("", "blocking") > -1
                        && (atts.getIndex("", "rel") == -1
                        || !relList.contains("stylesheet"))) {
                    err("A “link” element with a"
                                + " “blocking” attribute must have a"
                                + " “rel” attribute whose value is"
                                + " “stylesheet”.");
                }
            }

            // microdata
            if (itemid && !(itemscope && itemtype)) {
                err("The “itemid” attribute must not be specified on elements that do not have both an “itemscope” attribute and an “itemtype” attribute specified.");
            }
            if (itemref && !itemscope) {
                err("The “itemref” attribute must not be specified on elements that do not have an “itemscope” attribute specified.");
            }
            if (itemtype && !itemscope) {
                err("The “itemtype” attribute must not be specified on elements that do not have an “itemscope” attribute specified.");
            }

            // Errors for use of ARIA attributes that conflict with native
            // element semantics.
            if (atts.getIndex("", "contenteditable") > -1
                    && "true".equals(atts.getValue("", "aria-readonly"))) {
                err("The “aria-readonly” attribute must only be"
                        + " specified with a value of “false”"
                        + " on elements that have a “contenteditable”"
                        + " attribute.");
            }
            if (atts.getIndex("", "aria-placeholder") > -1
                    && atts.getIndex("", "placeholder") > -1) {
                err("The “aria-placeholder” attribute must not be"
                        + " specified on elements that have a"
                        + " “placeholder” attribute.");
            }
            // Warnings for use of ARIA attributes with markup already
            // having implicit ARIA semantics.
            if (ELEMENTS_WITH_IMPLICIT_ROLE.containsKey(localName)
                    && ELEMENTS_WITH_IMPLICIT_ROLE.get(localName).equals(
                            role)) {
                if (!("img".equals(localName)
                        && ("".equals(atts.getValue("", "alt"))))) {
                    warn("The “" + role + "” role is unnecessary for"
                            + " element" + " “" + localName + "”.");
                }
            } else if (ELEMENTS_WITH_IMPLICIT_ROLES.containsKey(localName)
                    && role != null
                    && Arrays.binarySearch(
                            ELEMENTS_WITH_IMPLICIT_ROLES.get(localName),
                            role) >= 0) {
                warn("The “" + role + "” role is unnecessary for"
                        + " element" + " “" + localName + "”.");
            } else if (ELEMENTS_THAT_NEVER_NEED_ROLE.containsKey(localName)
                    && ELEMENTS_THAT_NEVER_NEED_ROLE.get(localName).equals(
                            role)) {
                warn("Element “" + localName + "” does not need a"
                        + " “role” attribute.");
            } else if ("input" == localName) {
                inputTypeVal = inputTypeVal == null ? "text" : inputTypeVal;
                if ("radio".equals(inputTypeVal)
                        || "checkbox".equals(inputTypeVal)) {
                    if (atts.getIndex("", "aria-checked") >= 0
                            && !"".equals(atts.getValue("", "aria-checked"))) {
                        err("The “aria-checked” attribute must not"
                                + " be used on an “input” element"
                                + " which has a “type” attribute"
                                + " whose value is “" + inputTypeVal
                                + "”.");
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
                            err("The “aria-valuemin” attribute must"
                                    + " not be used on an element which has a"
                                    + " “min” attribute.");
                        } else {
                            warn("The “aria-valuemin” attribute"
                                    + " should not be used on an"
                                    + " “input” element which has a"
                                    + " “type” attribute whose value"
                                    + " is “" + inputTypeVal + "”.");
                        }
                    }
                    if (atts.getIndex("", "aria-valuemax") >= 0
                            && !"".equals(atts.getValue("", "aria-valuemax"))) {
                        if (atts.getIndex("", "max") >= 0) {
                            err("The “aria-valuemax” attribute must"
                                    + " not be used on an element which has a"
                                    + " “max” attribute.");
                        } else {
                            warn("The “aria-valuemax” attribute"
                                    + " should not be used on an"
                                    + " “input” element which has a"
                                    + " “type” attribute whose value"
                                    + " is “" + inputTypeVal + "”.");
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
                            warn("The “textbox” role is unnecessary"
                                    + " for an “input” element that"
                                    + " has no “list” attribute and"
                                    + " whose type is" + " “"
                                    + inputTypeVal + "”.");
                        }
                        if ("searchbox".equals(role)
                                && "search".equals(inputTypeVal)) {
                            warn("The “searchbox” role is unnecessary"
                                    + " for an “input” element that"
                                    + " has no “list” attribute and"
                                    + " whose type is" + " “"
                                    + inputTypeVal + "”.");
                        }
                    } else {
                        if ("combobox".equals(role)) {
                            warn("The “combobox” role is unnecessary"
                                    + " for an “input” element that"
                                    + " has a “list” attribute and"
                                    + " whose type is" + " “"
                                    + inputTypeVal + "”.");
                        }
                        if (atts.getIndex("", "aria-haspopup") >= 0
                                && !"".equals(
                                        atts.getValue("", "aria-haspopup"))) {
                            warn("The “aria-haspopup” attribute"
                                    + " should not be used on an"
                                    + " “input” element that has a"
                                    + " “list” attribute and whose"
                                    + " type is “" + inputTypeVal
                                    + "”.");
                        }
                    }
                }
            } else if (atts.getIndex("", "href") > -1 && "link".equals(role)
                    && ("a".equals(localName) || "area".equals(localName)
                            || "link".equals(localName))) {
                warn("The “link” role is unnecessary for element"
                        + " “" + localName + "” with attribute"
                        + " “href”.");
            } else if (atts.getIndex("", "href") > -1 && "link".equals(role)
                    && ("a".equals(localName) || "area".equals(localName)
                            || "link".equals(localName))) {
                warn("The “link” role is unnecessary for element"
                        + " “" + localName + "” with attribute"
                        + " “href”.");
            } else if (("tbody".equals(localName) || "tfoot".equals(localName)
                    || "thead".equals(localName)) && "rowgroup".equals(role)) {
                warn("The “rowgroup” role is unnecessary for element"
                        + " “" + localName + "”.");
            } else if ("th" == localName && ("columnheader".equals(role)
                    || "rowheader".equals(role))) {
                warn("The “" + role + "” role is unnecessary for"
                        + " element “th”.");
            } else if ("li" == localName && "listitem".equals(role)
                    && !"menu".equals(parentName)) {
                warn("The “listitem” role is unnecessary for an"
                        + " “li” element whose parent is"
                        + " an “ol” element or a"
                        + " “ul” element.");
            } else if ("button" == localName && "button".equals(role)
                    && "menu".equals(atts.getValue("", "type"))) {
                warnExplicitRoleUnnecessaryForType("button", "button", "menu");
            } else if ("menu" == localName && "toolbar".equals(role)
                    && "toolbar".equals(atts.getValue("", "type"))) {
                warnExplicitRoleUnnecessaryForType("menu", "toolbar",
                        "toolbar");
            } else if ("li" == localName && "listitem".equals(role)
                    && !"menu".equals(parentName)) {
                warn("The “listitem” role is unnecessary for an"
                        + " “li” element whose parent is"
                        + " an “ol” element or a"
                        + " “ul” element.");
            }
            if (atts.getIndex("", "aria-expanded") > -1) {
                if (atts.getIndex("", "popovertarget") > -1) {
                    err("The “aria-expanded” attribute must not be"
                            + " used on any element which has a"
                            + " “popovertarget” attribute.");
                }
                if (atts.getIndex("", "command") > -1) {
                    err("The “aria-expanded” attribute must not be"
                            + " used on any element which has a"
                            + " “command” attribute.");
                }
            }
            for (String aLabelAtt: new String[] { "aria-label",
                    "aria-labelledby", "aria-braillelabel"}) {
                if (atts.getIndex("", aLabelAtt) > -1) {
                    if (isProhibitedFromBeingNamed(localName, roles, atts)) {
                        String message =
                            "The “" + aLabelAtt + "” attribute"
                            + " must not be specified on any"
                            + " “" + localName + "” element"
                            + " unless the element has a"
                            + " “role” value other than "
                            + renderRoleSet(ROLES_WHICH_CANNOT_BE_NAMED);
                        err(message + ".");
                    }
                }
            }
        } else {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String value = atts.getValue(i);
                boolean isEmptyAtt = (value != null && value.length() == 0);
                if (atts.getType(i) == "ID") {
                    String attVal = value;
                    if (attVal != null && attVal.length() != 0) {
                        ids.put(attVal, new Element(
                                    new LocatorImpl(getDocumentLocator()),
                                    localName,
                                    new AttributesImpl(atts)));
                    }
                }
                String attLocal = atts.getLocalName(i);
                if (atts.getURI(i).length() == 0) {
                    if ("role" == attLocal) {
                        role = value;
                    } else if ("aria-activedescendant" == attLocal
                            && !isEmptyAtt) {
                        activeDescendant = value;
                    } else if ("aria-owns" == attLocal && !isEmptyAtt) {
                        owns = value;
                    }
                }
            }

            allIds.putAll(ids);
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
        allIds.putAll(ids);

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
            if (ids.containsKey(entry.getKey().getActiveDescendant())) {
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
                        warn("An “aria-disabled” attribute whose"
                                + " value is “true” should not be"
                                + " specified on an “a” element"
                                + " that has an “href” attribute.");
                    }
                }
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    roles, activeDescendant, forAttr, new AttributesImpl(atts));
            if ("style" == localName) {
                child.setIsCollectingCharacters(true);
            }
            if ("script" == localName) {
                child.setIsCollectingCharacters(true);
            }
            if ("figure" == localName) {
                child.setIsCollectingChildren(true);
            }
            if ("details" == localName) {
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
                    warn("The “aria-multiselectable” attribute"
                            + " should not be used with the “select"
                            + " ” element.");
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
                        warn("The “listbox” role is unnecessary for"
                                + " element “select” with a"
                                + " “multiple” attribute or with a"
                                + " “size” attribute whose value"
                                + " is greater than 1.");
                    } else if (role != null) {
                        err("A “select” element with a"
                                + " “multiple” attribute or with a"
                                + " “size” attribute whose value"
                                + " is greater than 1 must not have any"
                                + " “role” attribute.");
                    }
                }
                if (!hasMultiple) {
                    if (!sizeIsGreaterThanOne && role != null) {
                        if ("combobox".equals(role)) {
                            warn("The “combobox” role is unnecessary"
                                    + " for element “select”"
                                    + " without a “multiple”"
                                    + " attribute and without a"
                                    + " “size” attribute whose value"
                                    + " is greater than 1.");
                        } else if (!"menu".equals(role)) {
                            err("The “" + role + "” role is not"
                                    + " allowed for element “select”"
                                    + " without a “multiple”"
                                    + " attribute and without a"
                                    + " “size” attribute whose value"
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
            if ("summary" == localName && "details".equals(parent.name)
                    && !parent.getCollectedChildren().stream().anyMatch(
                        s -> "summary" == s.getName())) {
                for (int i = 0; i < atts.getLength(); i++) {
                    String attLocal = atts.getLocalName(i);
                    if ("role".equals(attLocal)
                            || (attLocal.startsWith("aria-")
                                && Arrays.binarySearch(ARIA_GLOBAL_ATTRIBUTES,
                                    attLocal) < 0
                                && !"aria-haspopup".equals(attLocal)
                                && !"aria-disabled".equals(attLocal))) {
                        err("The “" + attLocal + "” attribute must"
                                + " not be used on any “summary”"
                                + " element that is a summary for its parent"
                                + " “details” element.");
                    }
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
            err("Element “" + localName + "” from namespace"
                    + " “http://n.validator.nu/custom-elements/”"
                    + " not allowed.");
        } else {
            StackNode child = new StackNode(ancestorMask, null, role, roles,
                    activeDescendant, forAttr, new AttributesImpl(atts));
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
            sb.append("“");
            sb.append(types[i]);
            sb.append('”');
        }
        return sb;
    }

    private CharSequence renderRoleSet(Set<String> roles) {
        int size = roles.size();
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String role : roles) {
            sb.append('“').append(role).append('”');
            index++;
            if (index < size - 1) {
                sb.append(", ");
            } else if (index == size - 1) {
                sb.append(", or ");
            }
        }
        return sb;
    }

}
