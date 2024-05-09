/*
 * Copyright (c) 2007-2015 Mozilla Foundation
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
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.io.BoundedInputStream;
import nu.validator.io.StreamBoundException;
import nu.validator.xml.ContentTypeParser;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.PrudentHttpEntityResolver;
import nu.validator.xml.TypedInputSource;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import io.mola.galimatias.URL;
import io.mola.galimatias.GalimatiasParseException;

public class ParseTreePrinter {

    private static final String FORM_HTML = "<!DOCTYPE html><title>Parse Tree Dump</title><form><p><input type='url' name='doc' id='doc' pattern='(?:https?://.+)?'> <input name='submit' value='Print Tree' type='submit' id='submit'></form><hr><form><p><select id=parser name=parser><option value=xml>XML; don\u2019t load external entities</option><option value=html5 selected>HTML5</option></select><p><textarea name=content rows=20 cols=72></textarea> <input name='submit' value='Print Tree' type='submit' id='submit'></form>";

    private static final long SIZE_LIMIT = Integer.parseInt(System.getProperty(
            "nu.validator.servlet.max-file-size", "2097152"));

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    /**
     * @param request
     * @param response
     */
    public ParseTreePrinter(final HttpServletRequest request,
            final HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    private String scrubUrl(String urlStr) {
        if (urlStr == null) {
            return null;
        }
        try {
            return URL.parse(urlStr).toString();
        } catch (GalimatiasParseException e) {
            return null;
        }
    }

    public void service() throws IOException {
        request.setCharacterEncoding("utf-8");
        String content = null;
        String document = scrubUrl(request.getParameter("doc"));
        document = ("".equals(document)) ? null : document;
        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8")) {
            if (document == null && methodIsGet() && (content = request.getParameter("content")) == null) {
                response.setContentType("text/html; charset=utf-8");
                writer.write(FORM_HTML);
                writer.flush();
                return;
            }

            response.setContentType("text/plain; charset=utf-8");
            try {
            PrudentHttpEntityResolver entityResolver = new PrudentHttpEntityResolver(
                    2048 * 1024, false, null);
            entityResolver.setAllowGenericXml(false);
            entityResolver.setAcceptAllKnownXmlTypes(false);
            entityResolver.setAllowHtml(true);
            entityResolver.setAllowXhtml(true);
            TypedInputSource documentInput;
            if (methodIsGet()) {
                if (content == null) {
                    documentInput = (TypedInputSource) entityResolver.resolveEntity(
                            null, document);
                } else {
                    documentInput = new TypedInputSource(new StringReader(content));
                    if ("xml".equals(request.getParameter("parser"))) {
                        documentInput.setType("application/xhtml+xml");
                    } else {
                        documentInput.setType("text/html");
                    }
                }
            } else { // POST
                String postContentType = request.getContentType();
                if (postContentType == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Content-Type missing");
                    return;
                } else if (postContentType.trim().toLowerCase().startsWith(
                        "application/x-www-form-urlencoded")) {
                    response.sendError(
                            HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                            "application/x-www-form-urlencoded not supported. Please use multipart/form-data.");
                    return;
                }
                long len = request.getContentLength();
                if (len > SIZE_LIMIT) {
                    throw new StreamBoundException("Resource size exceeds limit.");
                }
                ContentTypeParser contentTypeParser = new ContentTypeParser(null, false);
                contentTypeParser.setAllowGenericXml(false);
                contentTypeParser.setAcceptAllKnownXmlTypes(false);
                contentTypeParser.setAllowHtml(true);
                contentTypeParser.setAllowXhtml(true);
                documentInput = contentTypeParser.buildTypedInputSource(document,
                        null, postContentType);
                documentInput.setByteStream(len < 0 ? new BoundedInputStream(
                        request.getInputStream(), SIZE_LIMIT, document)
                        : request.getInputStream());
                documentInput.setSystemId(request.getHeader("Content-Location"));
            }
            String type = documentInput.getType();
            XMLReader parser;
            if ("text/html".equals(type) || "text/html-sandboxed".equals(type)) {
                writer.write("HTML parser\n\n#document\n");
                parser = new nu.validator.htmlparser.sax.HtmlParser();
                parser.setProperty("http://validator.nu/properties/heuristics", Heuristics.ALL);
                parser.setProperty("http://validator.nu/properties/xml-policy", XmlViolationPolicy.ALLOW);
            } else if ("application/xhtml+xml".equals(type)) {
                writer.write("XML parser\n\n#document\n");
                parser = new SAXDriver();
                parser.setFeature(
                        "http://xml.org/sax/features/external-general-entities",
                        false);
                parser.setFeature(
                        "http://xml.org/sax/features/external-parameter-entities",
                        false);
                parser.setEntityResolver(new NullEntityResolver());
            } else {
                writer.write("Unsupported content type.\n");
                writer.flush();
                return;
            }
            TreeDumpContentHandler treeDumpContentHandler = new TreeDumpContentHandler(writer, false);
            ListErrorHandler listErrorHandler = new ListErrorHandler();
            parser.setContentHandler(treeDumpContentHandler);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", treeDumpContentHandler);
            parser.setErrorHandler(listErrorHandler);
            parser.parse(documentInput);
            writer.write("#errors\n");
            for (String err : listErrorHandler.getErrors()) {
                writer.write(err);
                writer.write('\n');
            }
            } catch (SAXException e) {
                writer.write("SAXException:\n");
                writer.write(e.getMessage());
                writer.write("\n");
            } catch (IOException e) {
                writer.write("IOException:\n");
                writer.write(e.getMessage());
                writer.write("\n");
            } finally {
                writer.flush();
            }
        }
    }

    private boolean methodIsGet() {
        return "GET".equals(request.getMethod())
                || "HEAD".equals(request.getMethod());
    }

}
