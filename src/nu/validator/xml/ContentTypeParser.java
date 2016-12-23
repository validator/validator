/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007-2016 Mozilla Foundation
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

package nu.validator.xml;

import nu.validator.io.SystemIdIOException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ContentTypeParser {

    private static final char[] CHARSET = { 'c', 'h', 'a', 'r', 's', 'e', 't' };
    
    private final boolean hasCharset(String param, int offset) {
        if (param.length() - offset < 7) {
            return false;
        }
        for (int i = 0; i < 7; i++) {
            char c = param.charAt(offset + i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            if (c != CHARSET[i]) {
                return false;
            }
        }
        return true;
    }

    private final void malformedContentTypeError(String contentType, String reason)
        throws SAXException {
        if (errorHandler != null) {
            errorHandler.error(new SAXParseException(
                    "Document served with malformed Content-Type header: "
                    + "  \u201c" + contentType + "\u201d. "
                    + reason,
                    null, null, -1, -1));
        }
    }

    private final ErrorHandler errorHandler;

    private boolean laxContentType;

    private boolean allowRnc = false;

    private boolean allowHtml = false;

    private boolean allowXhtml = false;

    private boolean acceptAllKnownXmlTypes = false;

    private boolean allowGenericXml = true;

    /**
     * @param errorHandler
     * @param laxContentType
     * @param allowRnc
     * @param allowHtml
     * @param allowXhtml
     * @param acceptAllKnownXmlTypes
     * @param allowGenericXml
     */
    public ContentTypeParser(final ErrorHandler errorHandler, boolean laxContentType, boolean allowRnc, boolean allowHtml, boolean allowXhtml, boolean acceptAllKnownXmlTypes, boolean allowGenericXml) {
        this.errorHandler = errorHandler;
        this.laxContentType = laxContentType;
        this.allowRnc = allowRnc;
        this.allowHtml = allowHtml;
        this.allowXhtml = allowXhtml;
        this.acceptAllKnownXmlTypes = acceptAllKnownXmlTypes;
        this.allowGenericXml = allowGenericXml;
    }

    public ContentTypeParser(final ErrorHandler errorHandler, boolean laxContentType) {
        this.errorHandler = errorHandler;
        this.laxContentType = laxContentType;        
    }
    protected boolean xmlContentType(String type, InputSource is)
            throws SAXException {
        if ("application/xhtml-voice+xml".equals(type)) {
            if (errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "application/xhtml-voice+xml is an obsolete type.",
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
        }
        boolean typeOk = "application/xml".equals(type)
                || "text/xml".equals(type) || type.endsWith("+xml")
                || "application/xml-external-parsed-entity".equals(type)
                || "text/xml-external-parsed-entity".equals(type)
                || "application/xml-dtd".equals(type)
                || "application/octet-stream".equals(type);
        if (!typeOk && laxContentType) {
            boolean laxOk = "text/plain".equals(type)
                    || "text/html".equals(type) || "text/xsl".equals(type);
            if (laxOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "Being lax about non-XML Content-Type: " + type,
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
            return laxOk;
        } else {
            return typeOk;
        }
    }

    
    protected boolean rncContentType(String type, InputSource is)
            throws SAXException {
        boolean typeOk = "application/relax-ng-compact-syntax".equals(type);
        if (!typeOk) {
            typeOk = "application/vnd.relax-ng.rnc".equals(type);
            if (typeOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "application/vnd.relax-ng.rnc is an unregistered type. application/relax-ng-compact-syntax is the registered type.",
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
        }
        if (!typeOk) {
            typeOk = "application/octet-stream".equals(type)
                    && is.getSystemId().endsWith(".rnc");
        }
        if (!typeOk && laxContentType) {
            boolean laxOk = "text/plain".equals(type)
                    && is.getSystemId().endsWith(".rnc");
            if (laxOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "Being lax about non-RNC Content-Type: " + type,
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
            return laxOk;
        } else if (!typeOk && !laxContentType && errorHandler != null) {
            String msg = "Non-RNC Content-Type: \u201C" + type + "\u201D."
                    + " (application/relax-ng-compact-syntax"
                    + " is the registered type.)";
            SAXParseException spe = new SAXParseException(msg,
                    is.getPublicId(), is.getSystemId(), -1, -1,
                    new SystemIdIOException(is.getSystemId(), msg));
            if (errorHandler != null) {
                errorHandler.fatalError(spe);
            }
            throw spe;
        } else {
            return typeOk;
        }
    }

    /**
     * @param baseUri
     * @param publicId
     * @param contentType
     * @return
     * @throws SAXException
     * @throws SAXParseException
     */
    public TypedInputSource buildTypedInputSource(String baseUri,
            String publicId, String contentType)
            throws SAXException, SAXParseException {
        TypedInputSource is;
        is = new TypedInputSource();
        is.setPublicId(publicId);
        is.setSystemId(baseUri);
        if (contentType != null) {
            String[] params = contentType.split(";");
            String type = params[0].trim().toLowerCase();
            boolean wasRnc = false;
            boolean wasHtml = false;
            if (isAllowRnc()) {
                if (rncContentType(type, is)) {
                    wasRnc = true;
                    is.setType("application/relax-ng-compact-syntax");
                }
            }
            if (!wasRnc) {
                if (isAllowHtml()) {
                    if ("text/html".equals(type) || "text/html-sandboxed".equals(type)) {
                        is.setType(type);
                        wasHtml = true;
                    } else if (isOnlyHtmlAllowed()) {
                        if (laxContentType && "text/plain".equals(type)) {
                            is.setType(type);
                            wasHtml = true;
                            if (errorHandler != null) {
                                errorHandler.warning(new SAXParseException(
                                        "Being lax about non-HTML Content-Type: "
                                                + type, is.getPublicId(),
                                        is.getSystemId(), -1, -1));
                            }
                        } else if ("application/octet-stream".equals(type)) {
                            is.setType(type);
                            wasHtml = true;                            
                        } else {
                            String msg = "Non-HTML Content-Type: \u201C" + type
                                    + "\u201D.";
                            SAXParseException spe = new SAXParseException(msg,
                                    publicId, baseUri, -1, -1,
                                    new SystemIdIOException(baseUri, msg));
                            if (errorHandler != null) {
                                errorHandler.fatalError(spe);
                            }
                            throw spe;
                        }
                    }
                }
                if (!wasHtml
                        && (isAllowGenericXml() || isAllowXhtml() || isAcceptAllKnownXmlTypes())) {
                    if (!xmlContentType(type, is)) {
                        String msg = "Non-XML Content-Type: \u201C" + type
                                + "\u201D.";
                        SAXParseException spe = new SAXParseException(msg,
                                publicId, baseUri, -1, -1,
                                new SystemIdIOException(baseUri, msg));
                        if (errorHandler != null) {
                            errorHandler.fatalError(spe);
                        }
                        throw new NonXmlContentTypeException(
                                String.format("%s: %s", baseUri, msg));
                    } else {
                        is.setType(type);
                    }
                }
            }
            String charset = null;
            char c;
            boolean quoted = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < params.length; i++) {
                String param = params[i];
                int offset;
                beforeCharset: for (offset = 0; offset < param.length(); offset++) {
                    c = param.charAt(offset);
                    switch (c) {
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\u000C':
                        case '\r':
                            continue;
                        case 'c':
                        case 'C':
                            break beforeCharset;
                        default:
                    }
                }
                inCharset: if (hasCharset(param, offset)) {
                    offset += 7;
                    c = param.charAt(offset);
                    switch (c) {
                        case '=':
                            offset++;
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\u000C':
                        case '\r':
                            malformedContentTypeError(contentType,
                                "Whitespace is not allowed before the \u201c=\u201d sign in the \u201ccharset\u201d parameter.");
                            break inCharset;
                        default:
                            malformedContentTypeError(contentType,
                                "Expected an \u201c=\u201d sign but saw \u201c" + c + "\u201d instead.");
                            break inCharset;
                    }
                    if (offset == param.length()) {
                        malformedContentTypeError(contentType,
                                "The empty string is not a valid encoding name.");
                        break inCharset;
                    }
                    c = param.charAt(offset);
                    switch (c) {
                        case '"':
                            offset++;
                            quoted = true;
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\u000C':
                        case '\r':
                            malformedContentTypeError(contentType,
                                "Whitespace is not allowed after the \u201c=\u201d sign in the parameter value.");
                            break inCharset;
                        default:
                            break;
                    }
                    inEncodingName: for (int j = offset; j < param.length(); j++) {
                        c = param.charAt(j);
                        switch (c) {
                            case '"':
                                if (!quoted) {
                                    malformedContentTypeError(contentType,
                                        "Unmatched \u201c\"\u201d character in \u201ccharset\u201d parameter.");
                                    break inCharset;
                                }
                                break inEncodingName;
                            case ' ':
                            case '\t':
                            case '\n':
                            case '\u000C':
                            case '\r':
                                break inEncodingName;
                            default:
                        }
                        if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                                || (c >= 'a' && c <= 'z') || c == '-' || c == '!'
                                || c == '#' || c == '$' || c == '%' || c == '&'
                                || c == '\'' || c == '+' || c == '_' || c == '`'
                                || c == '{' || c == '}' || c == '~' || c == '^')) {
                            malformedContentTypeError(contentType,
                                "The character \u201c" + c + "\u201d is not a valid character in an encoding name.");
                            break inCharset;
                        }
                        offset++;
                        sb.append(c);
                    }
                    if (quoted) {
                        if (param.length() > offset && '"' == param.charAt(offset)) {
                            offset++;
                        } else {
                            malformedContentTypeError(contentType,
                                "Unmatched \u201c\"\u201d character in \u201ccharset\u201d parameter.");
                            break inCharset;
                        }
                    }
                    if (param.length() > offset) {
                        for (int k = offset + 1; k < param.length(); k++) {
                            c = param.charAt(k);
                            switch (c) {
                                case ' ':
                                case '\t':
                                case '\n':
                                case '\u000C':
                                case '\r':
                                    offset++;
                                    continue;
                                default:
                                    malformedContentTypeError(contentType,
                                        "Only whitespace is allowed after the encoding name in the \u201ccharset\u201d parameter. "
                                        + "Found \u201c" + c + "\u201d instead.");
                                    break inCharset;
                            }
                        }
                    }
                    if (sb.length() == 0) {
                        malformedContentTypeError(contentType,
                            "The empty string is not a valid encoding name.");
                    }
                }
                if (sb.length() > 0) {
                    if ('\'' == sb.charAt(0) && '\'' == sb.charAt(sb.length() - 1)) {
                        malformedContentTypeError(contentType,
                            "Single-quoted encoding names are not allowed in the \u201ccharset\u201d parameter.");
                    } else {
                        charset = sb.toString();
                    }
                }
            }
            if (charset != null) {
                is.setEncoding(charset);
            } else if (type.startsWith("text/") && !wasHtml) {
                if (laxContentType) {
                    if (errorHandler != null) {
                        errorHandler.warning(new SAXParseException(
                                "text/* type without a charset parameter seen. Would have defaulted to US-ASCII had the lax option not been chosen.",
                                is.getPublicId(), is.getSystemId(), -1, -1));
                    }
                } else {
                    is.setEncoding("US-ASCII");
                    if (errorHandler != null) {
                        errorHandler.warning(new SAXParseException(
                                "text/* type without a charset parameter seen. Defaulting to US-ASCII per section 3.1 of RFC 3023.",
                                is.getPublicId(), is.getSystemId(), -1, -1));
                    }
                }
            }
        }
        return is;
    }


    /**
     * Returns the acceptAllKnownXmlTypes.
     * 
     * @return the acceptAllKnownXmlTypes
     */
    public boolean isAcceptAllKnownXmlTypes() {
        return acceptAllKnownXmlTypes;
    }


    /**
     * Sets the acceptAllKnownXmlTypes.
     * 
     * @param acceptAllKnownXmlTypes the acceptAllKnownXmlTypes to set
     */
    public void setAcceptAllKnownXmlTypes(boolean acceptAllKnownXmlTypes) {
        this.acceptAllKnownXmlTypes = acceptAllKnownXmlTypes;
    }


    /**
     * Returns the allowGenericXml.
     * 
     * @return the allowGenericXml
     */
    public boolean isAllowGenericXml() {
        return allowGenericXml;
    }


    /**
     * Sets the allowGenericXml.
     * 
     * @param allowGenericXml the allowGenericXml to set
     */
    public void setAllowGenericXml(boolean allowGenericXml) {
        this.allowGenericXml = allowGenericXml;
    }


    /**
     * Returns the allowHtml.
     * 
     * @return the allowHtml
     */
    public boolean isAllowHtml() {
        return allowHtml;
    }


    /**
     * Sets the allowHtml.
     * 
     * @param allowHtml the allowHtml to set
     */
    public void setAllowHtml(boolean allowHtml) {
        this.allowHtml = allowHtml;
    }


    /**
     * Returns the allowRnc.
     * 
     * @return the allowRnc
     */
    public boolean isAllowRnc() {
        return allowRnc;
    }


    /**
     * Sets the allowRnc.
     * 
     * @param allowRnc the allowRnc to set
     */
    public void setAllowRnc(boolean allowRnc) {
        this.allowRnc = allowRnc;
    }


    /**
     * Returns the allowXhtml.
     * 
     * @return the allowXhtml
     */
    public boolean isAllowXhtml() {
        return allowXhtml;
    }


    /**
     * Sets the allowXhtml.
     * 
     * @param allowXhtml the allowXhtml to set
     */
    public void setAllowXhtml(boolean allowXhtml) {
        this.allowXhtml = allowXhtml;
    }


    /**
     * Returns the laxContentType.
     * 
     * @return the laxContentType
     */
    public boolean isLaxContentType() {
        return laxContentType;
    }


    /**
     * Sets the laxContentType.
     * 
     * @param laxContentType the laxContentType to set
     */
    public void setLaxContentType(boolean laxContentType) {
        this.laxContentType = laxContentType;
    }


    public boolean isOnlyHtmlAllowed() {
        return !isAllowGenericXml() && !isAllowRnc() && !isAllowXhtml();
    }

    public class NonXmlContentTypeException extends SAXException {
        public NonXmlContentTypeException (String message) {
            super(message);
        }
    }
}
