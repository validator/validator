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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.shapesecurity.salvation2.Policy;
import com.shapesecurity.salvation2.URLs.URI;
import com.shapesecurity.salvation2.URLs.URLWithScheme;

/**
 * Checks that page resources comply with Content Security Policy (CSP).
 *
 * CSP can be specified via:
 * - HTTP Content-Security-Policy header (retrieved from request attribute)
 * - &lt;meta http-equiv="Content-Security-Policy"&gt; element
 *
 * When both are present, both policies are enforced (per CSP spec).
 */
public class CspEnforcementChecker extends Checker {

    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    private static final String CSP_POLICY_ATTRIBUTE =
            "http://validator.nu/properties/csp-policy";

    // Event handler attributes that contain JavaScript
    private static final String[] EVENT_HANDLERS = {
        "onabort", "onauxclick", "onbeforeinput", "onbeforematch",
        "onbeforetoggle", "onblur", "oncancel", "oncanplay",
        "oncanplaythrough", "onchange", "onclick", "onclose",
        "oncontextlost", "oncontextmenu", "oncontextrestored", "oncopy",
        "oncuechange", "oncut", "ondblclick", "ondrag", "ondragend",
        "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop",
        "ondurationchange", "onemptied", "onended", "onerror", "onfocus",
        "onformdata", "ongotpointercapture", "oninput", "oninvalid",
        "onkeydown", "onkeypress", "onkeyup", "onload", "onloadeddata",
        "onloadedmetadata", "onloadstart", "onlostpointercapture",
        "onmousedown", "onmouseenter", "onmouseleave", "onmousemove",
        "onmouseout", "onmouseover", "onmouseup", "onpaste", "onpause",
        "onplay", "onplaying", "onpointercancel", "onpointerdown",
        "onpointerenter", "onpointerleave", "onpointermove", "onpointerout",
        "onpointerover", "onpointerrawupdate", "onpointerup", "onprogress",
        "onratechange", "onreset", "onresize", "onscroll", "onscrollend",
        "onsecuritypolicyviolation", "onseeked", "onseeking", "onselect",
        "onslotchange", "onstalled", "onsubmit", "onsuspend", "ontimeupdate",
        "ontoggle", "onvolumechange", "onwaiting", "onwheel"
    };

    private Locator locator;

    private Policy httpPolicy;

    private Policy metaPolicy;

    private List<ResourceInfo> resources;

    private boolean collectingScriptContent;

    private StringBuilder scriptContent;

    private Locator scriptLocator;

    private String scriptNonce;

    private boolean collectingStyleContent;

    private StringBuilder styleContent;

    private Locator styleLocator;

    private String styleNonce;

    /**
     * Information about a resource to be validated.
     */
    private static class ResourceInfo {
        enum Type {
            EXTERNAL_SCRIPT,
            INLINE_SCRIPT,
            SCRIPT_ATTRIBUTE,
            EXTERNAL_STYLE,
            INLINE_STYLE,
            STYLE_ATTRIBUTE,
            IMAGE,
            FRAME,
            OBJECT,
            MEDIA
        }

        final Type type;
        final String src;
        final String nonce;
        final String integrity;
        final String content;
        final String attrName;
        final Locator locator;

        ResourceInfo(Type type, String src, String nonce, String integrity,
                String content, String attrName, Locator locator) {
            this.type = type;
            this.src = src;
            this.nonce = nonce;
            this.integrity = integrity;
            this.content = content;
            this.attrName = attrName;
            this.locator = locator;
        }
    }

    public CspEnforcementChecker() {
        super();
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        resources = new ArrayList<>();
        httpPolicy = null;
        metaPolicy = null;
        collectingScriptContent = false;
        scriptContent = null;
        scriptLocator = null;
        scriptNonce = null;
        collectingStyleContent = false;
        styleContent = null;
        styleLocator = null;
        styleNonce = null;

        // Retrieve HTTP CSP policy from request attribute
        if (getRequest() != null) {
            Object policyObj = getRequest().getAttribute(CSP_POLICY_ATTRIBUTE);
            if (policyObj instanceof Policy) {
                httpPolicy = (Policy) policyObj;
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (!XHTML_NS.equals(uri)) {
            return;
        }

        // Check for CSP meta tag
        if ("meta".equals(localName)) {
            String httpEquiv = atts.getValue("", "http-equiv");
            if (httpEquiv != null
                    && "content-security-policy".equalsIgnoreCase(httpEquiv)) {
                String content = atts.getValue("", "content");
                if (content != null && !content.isEmpty()) {
                    try {
                        metaPolicy = Policy.parseSerializedCSP(content,
                                (severity, message, directiveIndex,
                                        valueIndex) -> {
                                    // Ignore - syntax errors reported elsewhere
                                });
                    } catch (IllegalArgumentException e) {
                        // Ignore - policy parsing failed
                    }
                }
            }
            return;
        }

        // Check for script elements
        if ("script".equals(localName)) {
            String src = atts.getValue("", "src");
            String nonce = atts.getValue("", "nonce");
            String integrity = atts.getValue("", "integrity");

            if (src != null && !src.isEmpty()) {
                // External script
                resources.add(new ResourceInfo(ResourceInfo.Type.EXTERNAL_SCRIPT,
                        src, nonce, integrity, null, null,
                        new LocatorImpl(locator)));
            } else {
                // Inline script - collect content
                collectingScriptContent = true;
                scriptContent = new StringBuilder();
                scriptLocator = new LocatorImpl(locator);
                scriptNonce = nonce;
            }
        }

        // Check for style elements
        if ("style".equals(localName)) {
            String nonce = atts.getValue("", "nonce");
            collectingStyleContent = true;
            styleContent = new StringBuilder();
            styleLocator = new LocatorImpl(locator);
            styleNonce = nonce;
        }

        // Check for link stylesheet
        if ("link".equals(localName)) {
            String rel = atts.getValue("", "rel");
            if (rel != null && rel.toLowerCase().contains("stylesheet")) {
                String href = atts.getValue("", "href");
                String nonce = atts.getValue("", "nonce");
                if (href != null && !href.isEmpty()) {
                    resources.add(new ResourceInfo(
                            ResourceInfo.Type.EXTERNAL_STYLE, href, nonce,
                            null, null, null, new LocatorImpl(locator)));
                }
            }
        }

        // Check for images
        if ("img".equals(localName)) {
            String src = atts.getValue("", "src");
            if (src != null && !src.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.IMAGE, src,
                        null, null, null, null, new LocatorImpl(locator)));
            }
        }

        // Check for iframes
        if ("iframe".equals(localName)) {
            String src = atts.getValue("", "src");
            if (src != null && !src.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.FRAME, src,
                        null, null, null, null, new LocatorImpl(locator)));
            }
        }

        // Check for object/embed
        if ("object".equals(localName)) {
            String data = atts.getValue("", "data");
            if (data != null && !data.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.OBJECT, data,
                        null, null, null, null, new LocatorImpl(locator)));
            }
        }

        if ("embed".equals(localName)) {
            String src = atts.getValue("", "src");
            if (src != null && !src.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.OBJECT, src,
                        null, null, null, null, new LocatorImpl(locator)));
            }
        }

        // Check for video/audio
        if ("video".equals(localName) || "audio".equals(localName)) {
            String src = atts.getValue("", "src");
            if (src != null && !src.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.MEDIA, src,
                        null, null, null, null, new LocatorImpl(locator)));
            }
        }

        // Check for style attribute
        String styleAttr = atts.getValue("", "style");
        if (styleAttr != null && !styleAttr.isEmpty()) {
            resources.add(new ResourceInfo(ResourceInfo.Type.STYLE_ATTRIBUTE,
                    null, null, null, styleAttr, "style",
                    new LocatorImpl(locator)));
        }

        // Check for event handler attributes
        for (String handler : EVENT_HANDLERS) {
            String value = atts.getValue("", handler);
            if (value != null && !value.isEmpty()) {
                resources.add(new ResourceInfo(
                        ResourceInfo.Type.SCRIPT_ATTRIBUTE, null, null, null,
                        value, handler, new LocatorImpl(locator)));
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (collectingScriptContent && scriptContent != null) {
            scriptContent.append(ch, start, length);
        }
        if (collectingStyleContent && styleContent != null) {
            styleContent.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!XHTML_NS.equals(uri)) {
            return;
        }

        if ("script".equals(localName) && collectingScriptContent) {
            String content = scriptContent.toString().trim();
            if (!content.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.INLINE_SCRIPT,
                        null, scriptNonce, null, content, null, scriptLocator));
            }
            collectingScriptContent = false;
            scriptContent = null;
            scriptLocator = null;
            scriptNonce = null;
        }

        if ("style".equals(localName) && collectingStyleContent) {
            String content = styleContent.toString().trim();
            if (!content.isEmpty()) {
                resources.add(new ResourceInfo(ResourceInfo.Type.INLINE_STYLE,
                        null, styleNonce, null, content, null, styleLocator));
            }
            collectingStyleContent = false;
            styleContent = null;
            styleLocator = null;
            styleNonce = null;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // If no CSP policy, nothing to check
        if (httpPolicy == null && metaPolicy == null) {
            return;
        }

        // Validate each resource against CSP policies
        for (ResourceInfo resource : resources) {
            checkResource(resource);
        }
    }

    private void checkResource(ResourceInfo resource) throws SAXException {
        boolean allowedByHttp = httpPolicy == null
                || isAllowedByPolicy(httpPolicy, resource);
        boolean allowedByMeta = metaPolicy == null
                || isAllowedByPolicy(metaPolicy, resource);

        if (!allowedByHttp || !allowedByMeta) {
            String message = buildViolationMessage(resource, !allowedByHttp);
            warn(message, resource.locator);
        }
    }

    private boolean isAllowedByPolicy(Policy policy, ResourceInfo resource) {
        try {
            switch (resource.type) {
                case EXTERNAL_SCRIPT:
                    return policy.allowsExternalScript(
                            Optional.ofNullable(resource.nonce),
                            Optional.ofNullable(resource.integrity),
                            parseUrl(resource.src), Optional.of(true),
                            Optional.empty());

                case INLINE_SCRIPT:
                    return policy.allowsInlineScript(
                            Optional.ofNullable(resource.nonce),
                            Optional.ofNullable(resource.content),
                            Optional.of(true));

                case SCRIPT_ATTRIBUTE:
                    return policy.allowsScriptAsAttribute(
                            Optional.ofNullable(resource.content));

                case EXTERNAL_STYLE:
                    return policy.allowsExternalStyle(
                            Optional.ofNullable(resource.nonce),
                            parseUrl(resource.src), Optional.empty());

                case INLINE_STYLE:
                    return policy.allowsInlineStyle(
                            Optional.ofNullable(resource.nonce),
                            Optional.ofNullable(resource.content));

                case STYLE_ATTRIBUTE:
                    return policy.allowsStyleAsAttribute(
                            Optional.ofNullable(resource.content));

                case IMAGE:
                    return policy.allowsImage(parseUrl(resource.src),
                            Optional.empty());

                case FRAME:
                    return policy.allowsFrame(parseUrl(resource.src),
                            Optional.empty());

                case OBJECT:
                    return policy.allowsObject(parseUrl(resource.src),
                            Optional.empty());

                case MEDIA:
                    return policy.allowsMedia(parseUrl(resource.src),
                            Optional.empty());

                default:
                    return true;
            }
        } catch (Exception e) {
            // If we can't parse/check, assume allowed
            return true;
        }
    }

    private Optional<URLWithScheme> parseUrl(String url) {
        if (url == null || url.isEmpty()) {
            return Optional.empty();
        }
        try {
            Optional<URI> uri = URI.parseURI(url);
            if (uri.isPresent()) {
                return Optional.of(uri.get());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildViolationMessage(ResourceInfo resource,
            boolean violatesHttpPolicy) {
        String source = violatesHttpPolicy ? "HTTP header" : "meta tag";
        switch (resource.type) {
            case EXTERNAL_SCRIPT:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "external script \u201c%s\u201d blocked by "
                                + "\u201cscript-src\u201d directive.",
                        source, truncate(resource.src, 50));

            case INLINE_SCRIPT:
                return String.format(
                        "Inline script violates Content Security Policy (%s): "
                                + "blocked by \u201cscript-src\u201d directive "
                                + "(missing \u201c\u2018unsafe-inline\u2019\u201d or nonce/hash).",
                        source);

            case SCRIPT_ATTRIBUTE:
                return String.format(
                        "Event handler attribute \u201c%s\u201d violates Content "
                                + "Security Policy (%s): blocked by "
                                + "\u201cscript-src\u201d directive.",
                        resource.attrName, source);

            case EXTERNAL_STYLE:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "external stylesheet \u201c%s\u201d blocked by "
                                + "\u201cstyle-src\u201d directive.",
                        source, truncate(resource.src, 50));

            case INLINE_STYLE:
                return String.format(
                        "Inline style violates Content Security Policy (%s): "
                                + "blocked by \u201cstyle-src\u201d directive "
                                + "(missing \u201c\u2018unsafe-inline\u2019\u201d or nonce/hash).",
                        source);

            case STYLE_ATTRIBUTE:
                return String.format(
                        "The \u201cstyle\u201d attribute violates Content Security "
                                + "Policy (%s): blocked by \u201cstyle-src\u201d "
                                + "directive.",
                        source);

            case IMAGE:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "image \u201c%s\u201d blocked by \u201cimg-src\u201d directive.",
                        source, truncate(resource.src, 50));

            case FRAME:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "frame \u201c%s\u201d blocked by \u201cframe-src\u201d directive.",
                        source, truncate(resource.src, 50));

            case OBJECT:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "object/embed \u201c%s\u201d blocked by \u201cobject-src\u201d "
                                + "directive.",
                        source, truncate(resource.src, 50));

            case MEDIA:
                return String.format(
                        "Resource violates Content Security Policy (%s): "
                                + "media \u201c%s\u201d blocked by \u201cmedia-src\u201d directive.",
                        source, truncate(resource.src, 50));

            default:
                return "Resource violates Content Security Policy.";
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen - 3) + "...";
    }

    @Override
    public void reset() {
        resources = null;
        httpPolicy = null;
        metaPolicy = null;
        collectingScriptContent = false;
        scriptContent = null;
        scriptLocator = null;
        scriptNonce = null;
        collectingStyleContent = false;
        styleContent = null;
        styleLocator = null;
        styleNonce = null;
    }
}
