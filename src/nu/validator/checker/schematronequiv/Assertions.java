/*
 * Copyright (c) 2008-2015 Mozilla Foundation
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import nu.validator.checker.AttributeUtil;
import nu.validator.checker.Checker;
import nu.validator.checker.LocatorImpl;
import nu.validator.checker.TaintableLocatorImpl;
import nu.validator.checker.VnuBadAttrValueException;

import nu.validator.datatype.Html5DatatypeException;
import nu.validator.datatype.ImageCandidateStringsWidthRequired;
import nu.validator.datatype.ImageCandidateStrings;
import nu.validator.datatype.ImageCandidateURL;

import org.relaxng.datatype.DatatypeException;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class Assertions extends Checker {

    private static boolean followW3Cspec = "1".equals(System.getProperty("nu.validator.servlet.follow-w3c-spec")) ? true
            : false;

    private static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(
            String lowerCaseLiteral, String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

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
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c)) {
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
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c)) {
                return str.substring(0, i + 1);
            }
        }
        return "";
    }

    private static final Map<String, String> OBSOLETE_ELEMENTS = new HashMap<String, String>();

    static {
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
        OBSOLETE_ELEMENTS.put(
                "frameset",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put(
                "noframes",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        if (followW3Cspec) {
            OBSOLETE_ELEMENTS.put(
                    "hgroup",
                    "To mark up subheadings, consider either just putting the "
                            + "subheading into a \u201Cp\u201D element after the "
                            + "\u201Ch1\u201D-\u201Ch6\u201D element containing the "
                            + "main heading, or else putting the subheading directly "
                            + "within the \u201Ch1\u201D-\u201Ch6\u201D element "
                            + "containing the main heading, but separated from the main "
                            + "heading by punctuation and/or within, for example, a "
                            + "\u201Cspan class=\"subheading\"\u201D element with "
                            + "differentiated styling. "
                            + "To group headings and subheadings, alternative titles, "
                            + "or taglines, consider using the \u201Cheader\u201D or "
                            + "\u201Cdiv\u201D elements.");
        }
    }

    private static final Map<String, String[]> OBSOLETE_ATTRIBUTES = new HashMap<String, String[]>();

    static {
        OBSOLETE_ATTRIBUTES.put("abbr", new String[] { "td", "th" });
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
        if (!followW3Cspec) {
            OBSOLETE_ATTRIBUTES.put("longdesc",
                    new String[] { "img", "iframe" });
        }
        OBSOLETE_ATTRIBUTES.put("methods", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("name", new String[] { "img", "embed", "option" });
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
    }

    private static final Map<String, String> OBSOLETE_ATTRIBUTES_MSG = new HashMap<String, String>();

    static {
        OBSOLETE_ATTRIBUTES_MSG.put(
                "abbr",
                "Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "archive",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Carchive\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("axis",
                "Use the \u201Cscope\u201D attribute.");
        OBSOLETE_ATTRIBUTES_MSG.put("charset",
                "Use an HTTP Content-Type header on the linked resource instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "classid",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Cclassid\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "code",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccode\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codebase",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodebase\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codetype",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodetype\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("coords",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("datapagesize", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("datafld", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("dataformatas", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("datasrc", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("for",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put("event",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "declare",
                "Repeat the \u201Cobject\u201D element completely each time the resource is to be reused.");
        OBSOLETE_ATTRIBUTES_MSG.put("language",
                "Use the \u201Ctype\u201D attribute instead.");
        if (!followW3Cspec) {
            OBSOLETE_ATTRIBUTES_MSG.put("longdesc",
                    "Use a regular \u201Ca\u201D element to link to the description.");
        }
        OBSOLETE_ATTRIBUTES_MSG.put("methods",
                "Use the HTTP OPTIONS feature instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("name",
                "Use the \u201Cid\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("nohref",
                "Omitting the \u201Chref\u201D attribute is sufficient.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "profile",
                "To declare which \u201Cmeta\u201D terms are used in the document, instead register the names as meta extensions. To trigger specific UA behaviors, use a \u201Clink\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "scheme",
                "Use only one scheme per field, or make the scheme declaration part of the value.");
        OBSOLETE_ATTRIBUTES_MSG.put("scope",
                "Use the \u201Cscope\u201D attribute on a \u201Cth\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("shape",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "standby",
                "Optimise the linked resource so that it loads quickly or, at least, incrementally.");
        OBSOLETE_ATTRIBUTES_MSG.put("target", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "type",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "urn",
                "Specify the preferred persistent identifier using the \u201Chref\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "usemap",
                "Use the \u201Cimg\u201D element instead of the \u201Cinput\u201D element for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "valuetype",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put("version", "You can safely omit it.");
    }

    private static final Map<String, String[]> OBSOLETE_STYLE_ATTRS = new HashMap<String, String[]>();

    static {
        OBSOLETE_STYLE_ATTRS.put("align", new String[] { "caption", "iframe",
                "img", "input", "object", "embed", "legend", "table", "hr",
                "div", "h1", "h2", "h3", "h4", "h5", "h6", "p", "col",
                "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("alink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("allowtransparency", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("background", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("bgcolor", new String[] { "table", "tr", "td",
                "th", "body" });
        OBSOLETE_STYLE_ATTRS.put("cellpadding", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("cellspacing", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("char", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("charoff", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("clear", new String[] { "br" });
        OBSOLETE_STYLE_ATTRS.put("color", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("compact", new String[] { "dl", "menu", "ol",
                "ul" });
        OBSOLETE_STYLE_ATTRS.put("frameborder", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("frame", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("height", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("hspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("link", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginbottom", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginheight", new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("marginleft", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginright", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("margintop", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginwidth", new String[] { "iframe", "body" });
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
        OBSOLETE_STYLE_ATTRS.put("vspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("width", new String[] { "hr", "table", "td",
                "th", "col", "colgroup", "pre" });
    }

    private static final String[] SPECIAL_ANCESTORS = { "a", "address",
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

    private static Map<String, Integer> ANCESTOR_MASK_BY_DESCENDANT = new HashMap<String, Integer>();

    private static void registerProhibitedAncestor(String ancestor,
            String descendant) {
        int number = specialAncestorNumber(ancestor);
        if (number == -1) {
            throw new IllegalStateException("Ancestor not found in array: "
                    + ancestor);
        }
        Integer maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(descendant);
        int mask = 0;
        if (maskAsObject != null) {
            mask = maskAsObject.intValue();
        }
        mask |= (1 << number);
        ANCESTOR_MASK_BY_DESCENDANT.put(descendant, new Integer(mask));
    }

    static {
        registerProhibitedAncestor("form", "form");
        registerProhibitedAncestor("time", "time");
        registerProhibitedAncestor("progress", "progress");
        registerProhibitedAncestor("meter", "meter");
        registerProhibitedAncestor("dfn", "dfn");
        registerProhibitedAncestor("noscript", "noscript");
        registerProhibitedAncestor("label", "label");
        registerProhibitedAncestor("address", "address");
        registerProhibitedAncestor("address", "section");
        registerProhibitedAncestor("address", "nav");
        registerProhibitedAncestor("address", "article");
        registerProhibitedAncestor("address", "aside");
        registerProhibitedAncestor("header", "header");
        registerProhibitedAncestor("footer", "header");
        registerProhibitedAncestor("address", "header");
        registerProhibitedAncestor("header", "footer");
        registerProhibitedAncestor("footer", "footer");
        registerProhibitedAncestor("dt", "header");
        registerProhibitedAncestor("dt", "footer");
        registerProhibitedAncestor("dt", "article");
        registerProhibitedAncestor("dt", "aside");
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
        registerProhibitedAncestor("th", "aside");
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
        registerProhibitedAncestor("a", "a");
        registerProhibitedAncestor("button", "a");
        registerProhibitedAncestor("a", "details");
        registerProhibitedAncestor("button", "details");
        registerProhibitedAncestor("a", "button");
        registerProhibitedAncestor("button", "button");
        registerProhibitedAncestor("a", "textarea");
        registerProhibitedAncestor("button", "textarea");
        registerProhibitedAncestor("a", "select");
        registerProhibitedAncestor("button", "select");
        registerProhibitedAncestor("a", "keygen");
        registerProhibitedAncestor("button", "keygen");
        registerProhibitedAncestor("a", "embed");
        registerProhibitedAncestor("button", "embed");
        registerProhibitedAncestor("a", "iframe");
        registerProhibitedAncestor("button", "iframe");
        registerProhibitedAncestor("a", "label");
        registerProhibitedAncestor("button", "label");
        registerProhibitedAncestor("caption", "table");
        registerProhibitedAncestor("article", "main");
        registerProhibitedAncestor("aside", "main");
        registerProhibitedAncestor("header", "main");
        registerProhibitedAncestor("footer", "main");
        registerProhibitedAncestor("nav", "main");
    }

    private static final int A_BUTTON_MASK = (1 << specialAncestorNumber("a"))
            | (1 << specialAncestorNumber("button"));

    private static final int FIGCAPTION_MASK = (1 << specialAncestorNumber("figcaption"));

    private static final int FIGURE_MASK = (1 << specialAncestorNumber("figure"));

    private static final int H1_MASK = (1 << specialAncestorNumber("h1"));

    private static final int H2_MASK = (1 << specialAncestorNumber("h2"));

    private static final int H3_MASK = (1 << specialAncestorNumber("h3"));

    private static final int H4_MASK = (1 << specialAncestorNumber("h4"));

    private static final int H5_MASK = (1 << specialAncestorNumber("h5"));

    private static final int H6_MASK = (1 << specialAncestorNumber("h6"));

    private static final int MAP_MASK = (1 << specialAncestorNumber("map"));

    private static final int HREF_MASK = (1 << 30);

    private static final int LABEL_FOR_MASK = (1 << 28);

    private static final Map<String, Set<String>> REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT = new HashMap<String, Set<String>>();

    private static final Map<String, Set<String>> ariaOwnsIdsByRole = new HashMap<String, Set<String>>();

    private static void registerRequiredAncestorRole(String parent, String child) {
        Set<String> parents = REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(child);
        if (parents == null) {
            parents = new HashSet<String>();
        }
        parents.add(parent);
        REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.put(child, parents);
    }

    static {
        registerRequiredAncestorRole("combobox", "option");
        registerRequiredAncestorRole("listbox", "option");
        registerRequiredAncestorRole("radiogroup", "option");
        registerRequiredAncestorRole("menu", "option");
        registerRequiredAncestorRole("menu", "menuitem");
        registerRequiredAncestorRole("menu", "menuitemcheckbox");
        registerRequiredAncestorRole("menu", "menuitemradio");
        registerRequiredAncestorRole("menubar", "menuitem");
        registerRequiredAncestorRole("menubar", "menuitemcheckbox");
        registerRequiredAncestorRole("menubar", "menuitemradio");
        registerRequiredAncestorRole("tablist", "tab");
        registerRequiredAncestorRole("tree", "treeitem");
        registerRequiredAncestorRole("tree", "option");
        registerRequiredAncestorRole("group", "treeitem");
        registerRequiredAncestorRole("group", "listitem");
        registerRequiredAncestorRole("group", "menuitemradio");
        registerRequiredAncestorRole("list", "listitem");
        registerRequiredAncestorRole("row", "gridcell");
        registerRequiredAncestorRole("row", "columnheader");
        registerRequiredAncestorRole("row", "rowheader");
        registerRequiredAncestorRole("grid", "row");
        registerRequiredAncestorRole("grid", "rowgroup");
        registerRequiredAncestorRole("rowgroup", "row");
        registerRequiredAncestorRole("treegrid", "row");
    }

    private static final Set<String> MUST_NOT_DANGLE_IDREFS = new HashSet<String>();

    static {
        MUST_NOT_DANGLE_IDREFS.add("aria-controls");
        MUST_NOT_DANGLE_IDREFS.add("aria-describedby");
        MUST_NOT_DANGLE_IDREFS.add("aria-flowto");
        MUST_NOT_DANGLE_IDREFS.add("aria-labelledby");
        MUST_NOT_DANGLE_IDREFS.add("aria-owns");
    }

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

        private final String role;

        private final String activeDescendant;

        private final String forAttr;

        private Set<Locator> imagesLackingAlt = new HashSet<Locator>();

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

    }

    private StackNode[] stack;

    private int currentPtr;

    private int currentSectioningDepth;

    public Assertions() {
        super();
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

    private Map<StackNode, Locator> openSingleSelects = new HashMap<StackNode, Locator>();

    private Map<StackNode, Locator> openLabels = new HashMap<StackNode, Locator>();

    private Map<StackNode, TaintableLocatorImpl> openMediaElements = new HashMap<StackNode, TaintableLocatorImpl>();

    private Map<StackNode, Locator> openActiveDescendants = new HashMap<StackNode, Locator>();

    private LinkedHashSet<IdrefLocator> contextmenuReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> menuIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> formControlReferences = new LinkedHashSet<IdrefLocator>();

    private LinkedHashSet<IdrefLocator> formElementReferences = new LinkedHashSet<IdrefLocator>();

    private LinkedHashSet<IdrefLocator> needsAriaOwner = new LinkedHashSet<IdrefLocator>();

    private Set<String> formControlIds = new HashSet<String>();

    private Set<String> formElementIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> listIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> ariaReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> allIds = new HashSet<String>();

    private int currentFigurePtr;

    private int currentHeadingPtr;

    private int currentSectioningElementPtr;

    private boolean hasMain;

    private boolean hasTopLevelH1;

    private Set<Locator> secondLevelH1s = new HashSet<Locator>();

    private Map<Locator, Map<String, String>> siblingSources = new ConcurrentHashMap<Locator, Map<String, String>>();

    private final void errContainedInOrOwnedBy(String role, Locator locator)
            throws SAXException {
        err("An element with \u201Crole=" + role + "\u201D"
                + " must be contained in, or owned by, an element with "
                + renderRoleSet(REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(role)) + ".",
                locator);
    }

    private final void errObsoleteAttribute(String attribute, String element,
            String suggestion) throws SAXException {
        err("The \u201C" + attribute + "\u201D attribute on the \u201C"
                + element + "\u201D element is obsolete." + suggestion);
    }

    private final void warnPresentationalAttribute(String attribute,
            String element, String suggestion) throws SAXException {
        warn("The \u201C" + attribute + "\u201D attribute on the \u201C"
                + element + "\u201D element is presentational markup."
                + " Consider using CSS instead." + suggestion);
    }

    private boolean currentElementHasRequiredAncestorRole(
            Set<String> requiredAncestorRoles) {
        for (String role : requiredAncestorRoles) {
            for (int i = 0; i < currentPtr; i++) {
                if (role.equals(stack[currentPtr - i].getRole())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @see nu.validator.checker.Checker#endDocument()
     */
    @Override public void endDocument() throws SAXException {
        // contextmenu
        for (IdrefLocator idrefLocator : contextmenuReferences) {
            if (!menuIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Ccontextmenu\u201D attribute must refer to a \u201Cmenu\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // label for
        for (IdrefLocator idrefLocator : formControlReferences) {
            if (!formControlIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Cfor\u201D attribute of the \u201Clabel\u201D element must refer to a form control.",
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
                err(
                        "The \u201Clist\u201D attribute of the \u201Cinput\u201D element must refer to a \u201Cdatalist\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA idrefs
        for (IdrefLocator idrefLocator : ariaReferences) {
            if (!allIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201C"
                                + idrefLocator.getAdditional()
                                + "\u201D attribute must point to an element in the same document.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA required owners
        for (IdrefLocator idrefLocator : needsAriaOwner) {
            boolean foundOwner = false;
            String role = idrefLocator.getAdditional();
            for (String ownerRole : REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(role)) {
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
    @Override public void endElement(String uri, String localName, String name)
            throws SAXException {
        StackNode node = pop();
        Locator locator = null;
        openSingleSelects.remove(node);
        openLabels.remove(node);
        openMediaElements.remove(node);
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("figure" == localName) {
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
                        + " identifying headings to all sections.",
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
            } else if ("option" == localName && !stack[currentPtr].hasOption()) {
                stack[currentPtr].setOptionFound();
            }
            if ("article" == localName || "aside" == localName
                    || "nav" == localName || "section" == localName) {
                currentSectioningElementPtr = -1;
                currentSectioningDepth--;
            }
        }
        if ((locator = openActiveDescendants.remove(node)) != null) {
            warn(
                    "Attribute \u201Caria-activedescendant\u201D value should "
                    + "either refer to a descendant element, or should "
                    + "be accompanied by attribute \u201Caria-owns\u201D.",
                    locator);
        }
    }

    /**
     * @see nu.validator.checker.Checker#startDocument()
     */
    @Override public void startDocument() throws SAXException {
        reset();
        stack = new StackNode[32];
        currentPtr = 0;
        currentFigurePtr = -1;
        currentHeadingPtr = -1;
        currentSectioningElementPtr = -1;
        currentSectioningDepth = 0;
        stack[0] = null;
        hasMain = false;
        hasTopLevelH1 = false;
    }

    public void reset() {
        openSingleSelects.clear();
        openLabels.clear();
        openMediaElements.clear();
        openActiveDescendants.clear();
        contextmenuReferences.clear();
        menuIds.clear();
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
    }

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String name, Attributes atts) throws SAXException {
        Set<String> ids = new HashSet<String>();
        String role = null;
        String activeDescendant = null;
        String owns = null;
        String forAttr = null;
        boolean href = false;
        boolean activeDescendantWithAriaOwns = false;
        // see nu.validator.datatype.ImageCandidateStrings
        System.setProperty("nu.validator.checker.imageCandidateString.hasWidth", "0");

        StackNode parent = peek();
        int ancestorMask = 0;
        String parentRole = null;
        String parentName = null;
        if (parent != null) {
            ancestorMask = parent.getAncestorMask();
            parentName = parent.getName();
            parentRole = parent.getRole();
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
            boolean languageJavaScript = false;
            boolean typeNotTextJavaScript = false;
            String xmlLang = null;
            String lang = null;
            String id = null;
            String contextmenu = null;
            String list = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attUri = atts.getURI(i);
                if (attUri.length() == 0) {
                    String attLocal = atts.getLocalName(i);
                    if ("href" == attLocal) {
                        href = true;
                    } else if ("controls" == attLocal) {
                        controls = true;
                    } else if ("type" == attLocal && "param" != localName
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String attValue = atts.getValue(i);
                        if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "hidden", attValue)) {
                            hidden = true;
                        } else if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "toolbar", attValue)) {
                            toolbar = true;
                        }

                        if (!lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "text/javascript", attValue)) {
                            typeNotTextJavaScript = true;
                        }
                    } else if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal) {
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
                    } else if ("contextmenu" == attLocal) {
                        contextmenu = atts.getValue(i);
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
                            && lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                    "javascript", atts.getValue(i))) {
                        languageJavaScript = true;
                    } else if ("rev" == attLocal
                            && !("1".equals(System.getProperty("nu.validator.schema.rdfa-full")))) {
                        errObsoleteAttribute(
                                "rev",
                                localName,
                                " Use the \u201Crel\u201D attribute instead,"
                                        + " with a term having the opposite meaning.");
                    } else if (OBSOLETE_ATTRIBUTES.containsKey(attLocal)
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String[] elementNames = OBSOLETE_ATTRIBUTES.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            String suggestion = OBSOLETE_ATTRIBUTES_MSG.containsKey(attLocal) ? " "
                                    + OBSOLETE_ATTRIBUTES_MSG.get(attLocal)
                                    : "";
                            errObsoleteAttribute(attLocal, localName, suggestion);
                        }
                    } else if (OBSOLETE_STYLE_ATTRS.containsKey(attLocal)) {
                        String[] elementNames = OBSOLETE_STYLE_ATTRS.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            errObsoleteAttribute(attLocal, localName,
                                    " Use CSS instead.");
                        }
                    } else if ("dropzone" == attLocal) {
                        String[] tokens = atts.getValue(i).toString().split(
                                "[ \\t\\n\\f\\r]+");
                        Arrays.sort(tokens);
                        for (int j = 0; j < tokens.length; j++) {
                            String keyword = tokens[j];
                            if (j > 0 && keyword.equals(tokens[j - 1])) {
                                err("Duplicate keyword " + keyword
                                        + ". Each keyword must be unique.");
                            }
                        }
                    }
                } else if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    if ("lang" == atts.getLocalName(i)) {
                        xmlLang = atts.getValue(i);
                    }
                }

                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
            }

            if ("img".equals(localName) || "source".equals(localName)) {
                if (atts.getIndex("", "srcset") > -1) {
                    String srcsetVal = atts.getValue("", "srcset");
                    try {
                        if (atts.getIndex("", "sizes") > -1) {
                            ImageCandidateStringsWidthRequired.THE_INSTANCE.checkValid(srcsetVal);
                        } else {
                            ImageCandidateStrings.THE_INSTANCE.checkValid(srcsetVal);
                        }
                        // see nu.validator.datatype.ImageCandidateStrings
                        if ("1".equals(System.getProperty("nu.validator.checker.imageCandidateString.hasWidth"))) {
                            if (atts.getIndex("", "sizes") < 0) {
                                err("When the \u201csrcset\u201d attribute has"
                                        + " any image candidate string with a"
                                        + " width descriptor, the"
                                        + " \u201csizes\u201d attribute"
                                        + " must also be present.");
                            }
                        }
                    } catch (DatatypeException e) {
                        Class<?> datatypeClass = ImageCandidateStrings.class;
                        if (atts.getIndex("", "sizes") > -1) {
                            datatypeClass = ImageCandidateStringsWidthRequired.class;
                        }
                        try {
                            if (getErrorHandler() != null) {
                                String msg = e.getMessage();
                                if (e instanceof Html5DatatypeException) {
                                    Html5DatatypeException ex5 = (Html5DatatypeException) e;
                                    if (!ex5.getDatatypeClass().equals(
                                            ImageCandidateURL.class)) {
                                        msg = msg.substring(msg.indexOf(": ") + 2);
                                    }
                                }
                                VnuBadAttrValueException ex = new VnuBadAttrValueException(
                                        localName, uri, "srcset", srcsetVal,
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
                            if (media == null && sourceAtts.get("type") == null) {
                                err("A \u201csource\u201d element that has a"
                                        + " following sibling"
                                        + " \u201csource\u201d element or"
                                        + " \u201cimg\u201d element with a"
                                        + " \u201csrcset\u201d attribute"
                                        + " must have a"
                                        + " \u201cmedia\u201d attribute and/or"
                                        + " \u201ctype\u201d attribute.",
                                        locator);
                                siblingSources.remove(locator);
                            } else if (media != null
                                    && lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                            "all", trimSpaces(media))) {
                                err("Value of \u201cmedia\u201d attribute here"
                                        + " must not be \u201call\u201d.",
                                        locator);
                            }
                        }
                    }
                } else if (atts.getIndex("", "sizes") > -1) {
                    err("The \u201csizes\u201d attribute may be specified"
                            + " only if the \u201csrcset\u201d attribute is"
                            + " also present.");
                }
            }

            if ("picture".equals(parentName) && "source".equals(localName)) {
                Map<String, String> sourceAtts = new HashMap<String, String>();
                for (int i = 0; i < atts.getLength(); i++) {
                    sourceAtts.put(atts.getLocalName(i), atts.getValue(i));
                }
                siblingSources.put((new LocatorImpl(getDocumentLocator())),
                        sourceAtts);
            }

            if ("figure" == localName) {
                currentFigurePtr = currentPtr + 1;
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
                currentSectioningElementPtr = currentPtr + 1;
                currentSectioningDepth++;
            }
            if ("h1" == localName || "h2" == localName || "h3" == localName
                    || "h4" == localName || "h5" == localName
                    || "h6" == localName) {
                currentHeadingPtr = currentPtr + 1;
                if (currentSectioningElementPtr > -1) {
                    stack[currentSectioningElementPtr].setHeadingFound();
                }
            }
            if (((ancestorMask & H1_MASK) != 0 || (ancestorMask & H2_MASK) != 0
                    || (ancestorMask & H3_MASK) != 0
                    || (ancestorMask & H4_MASK) != 0
                    || (ancestorMask & H5_MASK) != 0 || (ancestorMask & H6_MASK) != 0)
                    && "img" == localName
                    && atts.getIndex("", "alt") > -1
                    && !"".equals(atts.getValue("", "alt"))) {
                stack[currentHeadingPtr].setImgFound();
            }

            if ("option" == localName && !parent.hasOption()) {
                if (atts.getIndex("", "value") < 0) {
                    parent.setNoValueOptionFound();
                } else if (atts.getIndex("", "value") > -1
                        && "".equals(atts.getValue("", "value"))) {
                    parent.setEmptyValueOptionFound();
                } else {
                    parent.setNonEmptyOption((new LocatorImpl(
                            getDocumentLocator())));
                }
            }

            // Obsolete elements
            if (OBSOLETE_ELEMENTS.get(localName) != null) {
                err("The \u201C" + localName + "\u201D element is obsolete. "
                        + OBSOLETE_ELEMENTS.get(localName));
            }

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "";
            if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(localName)) != null) {
                mask = maskAsObject.intValue();
                descendantUiString = localName;
            } else if ("video" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "video\u201D with the attribute \u201Ccontrols";
            } else if ("audio" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "audio\u201D with the attribute \u201Ccontrols";
            } else if ("menu" == localName && toolbar) {
                mask = A_BUTTON_MASK;
                descendantUiString = "menu\u201D with the attribute \u201Ctype=toolbar";
            } else if ("img" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "img\u201D with the attribute \u201Cusemap";
            } else if ("object" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "object\u201D with the attribute \u201Cusemap";
            } else if ("input" == localName && !hidden) {
                mask = A_BUTTON_MASK;
                descendantUiString = "input";
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (int j = 0; j < SPECIAL_ANCESTORS.length; j++) {
                        if ((maskHit & 1) != 0) {
                            err("The element \u201C"
                                    + descendantUiString
                                    + "\u201D must not appear as a descendant of the \u201C"
                                    + SPECIAL_ANCESTORS[j] + "\u201D element.");
                        }
                        maskHit >>= 1;
                    }
                }
            }

            // Ancestor requirements/restrictions
            if ("area" == localName && ((ancestorMask & MAP_MASK) == 0)) {
                err("The \u201Carea\u201D element must have a \u201Cmap\u201D ancestor.");
            } else if ("img" == localName) {
                String titleVal = atts.getValue("", "title");
                if (ismap && ((ancestorMask & HREF_MASK) == 0)) {
                    err("The \u201Cimg\u201D element with the "
                            + "\u201Cismap\u201D attribute set must have an "
                            + "\u201Ca\u201D ancestor with the "
                            + "\u201Chref\u201D attribute.");
                }
                if (atts.getIndex("", "alt") < 0) {
                    if (followW3Cspec || (titleVal == null || "".equals(titleVal))) {
                        if ((ancestorMask & FIGURE_MASK) == 0) {
                            err("An \u201Cimg\u201D element must have an"
                                    + " \u201Calt\u201D attribute, except under"
                                    + " certain conditions. For details, consult"
                                    + " guidance on providing text alternatives"
                                    + " for images.");
                        } else {
                            stack[currentFigurePtr].setFigcaptionNeeded();
                            stack[currentFigurePtr].addImageLackingAlt(new LocatorImpl(
                                    getDocumentLocator()));
                        }
                    }
                }
            } else if ("input" == localName || "button" == localName
                    || "select" == localName || "textarea" == localName
                    || "keygen" == localName) {
                for (Map.Entry<StackNode, Locator> entry : openLabels.entrySet()) {
                    StackNode node = entry.getKey();
                    Locator locator = entry.getValue();
                    if (node.isLabeledDescendants()) {
                        err("The \u201Clabel\u201D element may contain at most one \u201Cinput\u201D, \u201Cbutton\u201D, \u201Cselect\u201D, \u201Ctextarea\u201D, or \u201Ckeygen\u201D descendant.");
                        warn(
                                "\u201Clabel\u201D element with multiple labelable descendants.",
                                locator);
                    } else {
                        node.setLabeledDescendants();
                    }
                }
                if ((ancestorMask & LABEL_FOR_MASK) != 0) {
                    boolean hasMatchingFor = false;
                    for (int i = 0; (stack[currentPtr - i].getAncestorMask() & LABEL_FOR_MASK) != 0; i++) {
                        String forVal = stack[currentPtr - i].getForAttr();
                        if (forVal != null && forVal.equals(id)) {
                            hasMatchingFor = true;
                            break;
                        }
                    }
                    if (id == null || !hasMatchingFor) {
                        err("Any \u201C"
                                + localName
                                + "\u201D descendant of a \u201Clabel\u201D element with a \u201Cfor\u201D attribute must have an ID value that matches that \u201Cfor\u201D attribute.");
                    }
                }
            } else if ("table" == localName) {
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
                    if (followW3Cspec) {
                        if (atts.getIndex("", "border") > -1
                                && (!("".equals(atts.getValue("", "border")) || "1".equals(atts.getValue(
                                        "", "border"))))) {
                            errObsoleteAttribute("border", "table",
                                    " Use CSS instead.");
                        } else {
                            warnPresentationalAttribute("border", "table",
                                    " For example: \u201Ctable, td, th { border: 1px solid gray }\u201D");
                        }
                    } else {
                        errObsoleteAttribute("border", "table",
                                " Use CSS instead.");
                    }
                }
            } else if ("track" == localName && atts.getIndex("", "default") >= 0) {
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
            } else if ("main" == localName) {
                if (hasMain) {
                    err("A document must not include more than one"
                            + " \u201Cmain\u201D element.");
                }
                hasMain = true;
            } else if ("h1" == localName) {
                if (currentSectioningDepth > 1) {
                    warn(h1WarningMessage);
                } else if (currentSectioningDepth == 1) {
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
                if (Double.isNaN(min) && !Double.isNaN(value) && !(0 <= value)) {
                    err("The value of the \u201Cvalue\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(value) && !Double.isNaN(max)
                        && !(value <= max)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(value) && Double.isNaN(max) && !(value <= 1)) {
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
                if (!Double.isNaN(min) && !Double.isNaN(high) && !(min <= high)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(high) && !(0 <= high)) {
                    err("The value of the \u201Chigh\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(high) && !(low <= high)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (!Double.isNaN(high) && !Double.isNaN(max) && !(high <= max)) {
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
                                    + " and attribute \u201Csrc\u201D are also specified.");
                        }
                    }
                }
            }

            // script
            else if ("script" == localName) {
                // script language
                if (languageJavaScript && typeNotTextJavaScript) {
                    err("A \u201Cscript\u201D element with the \u201Clanguage=\"JavaScript\"\u201D attribute set must not have a \u201Ctype\u201D attribute whose value is not \u201Ctext/javascript\u201D.");
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
                        err("Element \u201Cscript\u201D must not have attribute \u201Casync\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                }
            }

            // bdo required attrs
            else if ("bdo" == localName && atts.getIndex("", "dir") < 0) {
                err("Element \u201Cbdo\u201D must have attribute \u201Cdir\u201D.");
            }

            // lang and xml:lang for XHTML5
            if (lang != null && xmlLang != null
                    && !equalsIgnoreAsciiCase(lang, xmlLang)) {
                err("When the attribute \u201Clang\u201D in no namespace and the attribute \u201Clang\u201D in the XML namespace are both present, they must have the same value.");
            }

            // contextmenu
            if (contextmenu != null) {
                contextmenuReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), contextmenu));
            }
            if ("menu" == localName) {
                menuIds.addAll(ids);
            }
            if (role != null && owns != null) {
                for (Set<String> value : REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.values()) {
                    if (value.contains(role)) {
                        String[] ownedIds = AttributeUtil.split(owns);
                        for (int i = 0; i < ownedIds.length; i++) {
                            Set<String> ownedIdsForThisRole = ariaOwnsIdsByRole.get(role);
                            if (ownedIdsForThisRole == null) {
                                ownedIdsForThisRole = new HashSet<String>();
                            }
                            ownedIdsForThisRole.add(ownedIds[i]);
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
                    formControlReferences.add(new IdrefLocator(new LocatorImpl(
                            getDocumentLocator()), forVal));
                }
            }

            if ("form" == localName) {
                formElementIds.addAll(ids);
            }

            if (("input" == localName && !hidden) || "textarea" == localName
                    || "select" == localName || "button" == localName
                    || "keygen" == localName || "output" == localName) {
                formControlIds.addAll(ids);
            }

            if ("button" == localName || "fieldset" == localName
                    || ("input" == localName && !hidden)
                    || "keygen" == localName || "label" == localName
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
                listReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), list));
            }

            // input@type=button
            if ("input" == localName
                    && lowerCaseLiteralEqualsIgnoreAsciiCaseString("button",
                            atts.getValue("", "type"))) {
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
                if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "content-language", atts.getValue("", "http-equiv"))) {
                    err("Using the \u201Cmeta\u201D element to specify the"
                        + " document-wide default language is obsolete."
                        + " Consider specifying the language on the root"
                        + " element instead.");
                } else if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "x-ua-compatible", atts.getValue("", "http-equiv"))
                        && !lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "ie=edge", atts.getValue("", "content"))) {
                    err("A \u201Cmeta\u201D element with an"
                            + " \u201Chttp-equiv\u201D attribute whose value is"
                            + " \u201CX-UA-Compatible\u201D" + " must have a"
                            + " \u201Ccontent\u201D attribute with the value"
                            + " \u201CIE=edge\u201D.");
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
        } else {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
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
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal) {
                        owns = atts.getValue(i);
                    }
                }
            }

            allIds.addAll(ids);
        }

        // ARIA required owner/ancestors
        Set<String> requiredAncestorRoles = REQUIRED_ROLE_ANCESTOR_BY_DESCENDANT.get(role);
        if (requiredAncestorRoles != null && !"presentation".equals(parentRole)
               && !"tbody".equals(localName) && !"tfoot".equals(localName)
               && !"thead".equals(localName)) {
            if (!currentElementHasRequiredAncestorRole(requiredAncestorRoles)) {
                if (atts.getIndex("", "id") > -1
                        && !"".equals(atts.getValue("", "id"))) {
                    needsAriaOwner.add(new IdrefLocator(new LocatorImpl(
                            getDocumentLocator()), atts.getValue("", "id"),
                            role));
                } else {
                    errContainedInOrOwnedBy(role, getDocumentLocator());
                }
            }
        }

        // ARIA IDREFS
        for (String att : MUST_NOT_DANGLE_IDREFS) {
            String attVal = atts.getValue("", att);
            if (attVal != null) {
                String[] tokens = AttributeUtil.split(attVal);
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    ariaReferences.add(new IdrefLocator(getDocumentLocator(),
                            token, att));
                }
            }
        }
        allIds.addAll(ids);

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
            if ("a" == localName && href) {
                ancestorMask |= HREF_MASK;
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null && !activeDescendantWithAriaOwns) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            if ("select" == localName && atts.getIndex("", "multiple") == -1) {
                openSingleSelects.put(child, getDocumentLocator());
            } else if ("label" == localName) {
                openLabels.put(child, new LocatorImpl(getDocumentLocator()));
            } else if ("video" == localName || "audio" == localName ) {
                openMediaElements.put(child, new TaintableLocatorImpl(getDocumentLocator()));
            }
            push(child);
            if ("select" == localName && atts.getIndex("", "required") > -1
                    && atts.getIndex("", "multiple") < 0) {
                if (atts.getIndex("", "size") > -1) {
                    String size = trimSpaces(atts.getValue("", "size"));
                    if (!"".equals(size)) {
                        try {
                            if ((size.length() > 1 && size.charAt(0) == '+' && Integer.parseInt(size.substring(1)) == 1)
                                    || Integer.parseInt(size) == 1) {
                                child.setOptionNeeded();
                            } else {
                                // do nothing
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                } else {
                    // default size is 1
                    child.setOptionNeeded();
                }
            }
        } else {
            StackNode child = new StackNode(ancestorMask, null, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            push(child);
        }
        stack[currentPtr].setLocator(new LocatorImpl(
                getDocumentLocator()));
    }

    /**
     * @see nu.validator.checker.Checker#characters(char[], int, int)
     */
    @Override public void characters(char[] ch, int start, int length)
            throws SAXException {
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
                            if ("figure".equals(stack[currentFigurePtr - j].getName())) {
                                stack[currentFigurePtr - j].setTextNodeFound();
                            }
                        }
                    } else if ("figure".equals(node.name)
                            || (node.ancestorMask & FIGURE_MASK) != 0) {
                        stack[currentFigurePtr].setTextNodeFound();
                        // for any ancestor figures of this figure, this
                        // also counts as a text node descendant
                        for (int k = 1; k < currentFigurePtr; k++) {
                            if ("figure".equals(stack[currentFigurePtr - k].getName())) {
                                stack[currentFigurePtr - k].setTextNodeFound();
                            }
                        }
                    } else if ("option".equals(node.name)
                            && !stack[currentPtr - 1].hasOption()
                            && (!stack[currentPtr - 1].hasEmptyValueOption() || stack[currentPtr - 1].hasNoValueOption())
                            && stack[currentPtr - 1].nonEmptyOptionLocator() == null) {
                        stack[currentPtr - 1].setNonEmptyOption((new LocatorImpl(
                                getDocumentLocator())));
                    }
                    return;
            }
        }
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
