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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class MultipartFormDataFilter implements Filter {

    private static Pattern EXTENSION = Pattern.compile("^.*\\.(.+)$");

    private final static Map<String, String> EXTENSION_TO_TYPE = new HashMap<>();

    static {
        EXTENSION_TO_TYPE.put("html", "text/html");
        EXTENSION_TO_TYPE.put("htm", "text/html");
        EXTENSION_TO_TYPE.put("xhtml", "application/xhtml+xml");
        EXTENSION_TO_TYPE.put("xht", "application/xhtml+xml");
        EXTENSION_TO_TYPE.put("atom", "application/atom+xml");
        EXTENSION_TO_TYPE.put("rng", "application/xml");
        EXTENSION_TO_TYPE.put("xsl", "application/xml");
        EXTENSION_TO_TYPE.put("xml", "application/xml");
        EXTENSION_TO_TYPE.put("dbk", "application/xml");
        EXTENSION_TO_TYPE.put("csl", "application/xml");
    }

    private static String utf8ByteStreamToString(InputStream stream)
            throws IOException {
        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
        dec.onMalformedInput(CodingErrorAction.REPORT);
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        Reader reader = new InputStreamReader(stream, dec);
        StringBuilder builder = new StringBuilder();
        int c;
        int i = 0;
        while ((c = reader.read()) != -1) {
            if (i > 2048) {
                throw new IOException("Form field value too large.");
            }
            builder.append((char) c);
            i++;
        }
        return builder.toString();
    }

    private static void putParam(Map<String, String[]> params, String key,
            String value) {
        String[] oldVal = params.get(key);
        if (oldVal == null) {
            String[] arr = new String[1];
            arr[0] = value;
            params.put(key, arr);
        } else {
            for (String string : oldVal) {
                if (string.equals(value)) {
                    return;
                }
            }
            String[] arr = new String[oldVal.length + 1];
            System.arraycopy(oldVal, 0, arr, 0, oldVal.length);
            arr[oldVal.length] = value;
            params.put(key, arr);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                boolean utf8 = false;
                String contentType = null;
                Map<String, String[]> params = new HashMap<>();
                InputStream fileStream = null;
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream fileItemStream = iter.next();
                    if (fileItemStream.isFormField()) {
                        String fieldName = fileItemStream.getFieldName();
                        if ("content".equals(fieldName)
                                || "fragment".equals(fieldName)) {
                            utf8 = true;
                            String[] parser = params.get("parser");
                            if (parser != null && parser[0].startsWith("xml")) {
                                contentType = "application/xml";
                            } else {
                                contentType = "text/html";
                            }
                            request.setAttribute("nu.validator.servlet.MultipartFormDataFilter.type", "textarea");
                            fileStream = fileItemStream.openStream();
                            break;
                        } else {
                            putParam(
                                    params,
                                    fieldName,
                                    utf8ByteStreamToString(fileItemStream.openStream()));
                        }
                    } else {
                        String fileName = fileItemStream.getName();
                        if (fileName != null) {
                            putParam(params, fileItemStream.getFieldName(),
                                    fileName);
                            request.setAttribute(
                                    "nu.validator.servlet.MultipartFormDataFilter.filename",
                                    fileName);
                            Matcher m = EXTENSION.matcher(fileName);
                            if (m.matches()) {
                                contentType = EXTENSION_TO_TYPE.get(m.group(1));
                            }
                        }
                        if (contentType == null) {
                            contentType = "text/html";
                        }
                        request.setAttribute("nu.validator.servlet.MultipartFormDataFilter.type", "file");
                        fileStream = fileItemStream.openStream();
                        break;
                    }
                }
                if (fileStream == null) {
                    fileStream = new ByteArrayInputStream(new byte[0]);
                }
                if (contentType == null) {
                    contentType = "text/html";
                }
                chain.doFilter(new RequestWrapper(request, params, contentType,
                        utf8, fileStream), response);
            } catch (CharacterCodingException | FileUploadException e) {
                response.sendError(415, e.getMessage());
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage());
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    private final class RequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String[]> params;

        private final String contentType;

        private final boolean utf8;

        private final ServletInputStream stream;

        public RequestWrapper(HttpServletRequest req,
                Map<String, String[]> params, String contentType, boolean utf8,
                InputStream stream) {
            super(req);
            this.params = Collections.unmodifiableMap(params);
            this.contentType = contentType;
            this.utf8 = utf8;
            this.stream = new DelegatingServletInputStream(stream);
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getDateHeader(java.lang.String)
         */
        @Override
        public long getDateHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Type".equalsIgnoreCase(name)) {
                return -1;
            } else {
                return super.getDateHeader(name);
            }
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
         */
        @Override
        public String getHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return null;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return null;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return null;
            } else if ("Content-Type".equalsIgnoreCase(name)) {
                return getContentType();
            } else {
                return super.getHeader(name);
            }
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getHeaderNames()
         */
        @Override
        public Enumeration getHeaderNames() {
            Enumeration e = super.getHeaderNames();
            List<String> list = new ArrayList<>();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                if ("Content-Length".equalsIgnoreCase(name)) {
                    continue;
                } else if ("Content-MD5".equalsIgnoreCase(name)) {
                    continue;
                } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                    continue;
                } else if ("Content-Type".equalsIgnoreCase(name)) {
                    list.add(getContentType());
                } else {
                    list.add(name);
                }
            }
            return Collections.enumeration(list);
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
         */
        @Override
        public Enumeration getHeaders(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else if ("Content-Type".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singleton(getContentType()));
            } else {
                return super.getHeaders(name);
            }
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getIntHeader(java.lang.String)
         */
        @Override
        public int getIntHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Type".equalsIgnoreCase(name)) {
                return -1;
            } else {
                return super.getIntHeader(name);
            }
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getCharacterEncoding()
         */
        @Override
        public String getCharacterEncoding() {
            return utf8 ? "utf-8" : null;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getContentLength()
         */
        @Override
        public int getContentLength() {
            return -1;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getContentType()
         */
        @Override
        public String getContentType() {
            return utf8 ? contentType + "; charset=utf-8" : contentType;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getInputStream()
         */
        @Override
        public ServletInputStream getInputStream() throws IOException {
            return stream;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
         */
        @Override
        public String getParameter(String key) {
            String[] arr = params.get(key);
            if (arr == null) {
                return null;
            } else {
                return arr[0];
            }
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterMap()
         */
        @Override
        public Map getParameterMap() {
            return params;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterNames()
         */
        @Override
        public Enumeration getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
         */
        @Override
        public String[] getParameterValues(String key) {
            return params.get(key);
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getReader()
         */
        @Override
        public BufferedReader getReader() throws IOException {
            CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
            dec.onMalformedInput(CodingErrorAction.REPORT);
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            Reader reader = new InputStreamReader(stream, dec);
            return new BufferedReader(reader);
        }

    }

}
