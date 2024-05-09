/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public final class InboundGzipFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Accept-Encoding", "gzip");
        String ce = request.getHeader("Content-Encoding");
        if (ce != null && "gzip".equalsIgnoreCase(ce.trim())) {
            chain.doFilter(new RequestWrapper(request), res);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    private final class RequestWrapper extends HttpServletRequestWrapper {

        private ServletInputStream stream = null;

        public RequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
        }

        /**
         * @see jakarta.servlet.http.HttpServletRequestWrapper#getDateHeader(java.lang.String)
         */
        @Override
        public long getDateHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return -1;
            } else {
                return super.getDateHeader(name);
            }
        }

        /**
         * @see jakarta.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
         */
        @Override
        public String getHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return null;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return null;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return null;
            } else {
                return super.getHeader(name);
            }
        }

        /**
         * @see jakarta.servlet.http.HttpServletRequestWrapper#getHeaderNames()
         */
        @SuppressWarnings("unchecked") @Override
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
                } else {
                    list.add(name);
                }
            }
            return Collections.enumeration(list);
        }

        /**
         * @see jakarta.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        @Override
        public Enumeration getHeaders(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.EMPTY_SET);
            } else {
                return super.getHeaders(name);
            }
        }

        /**
         * @see jakarta.servlet.http.HttpServletRequestWrapper#getIntHeader(java.lang.String)
         */
        @Override
        public int getIntHeader(String name) {
            if ("Content-Length".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-MD5".equalsIgnoreCase(name)) {
                return -1;
            } else if ("Content-Encoding".equalsIgnoreCase(name)) {
                return -1;
            } else {
                return super.getIntHeader(name);
            }
        }

        /**
         * @see jakarta.servlet.ServletRequestWrapper#getContentLength()
         */
        @Override
        public int getContentLength() {
            return -1;
        }

        /**
         * @see jakarta.servlet.ServletRequestWrapper#getInputStream()
         */
        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (stream == null) {
                stream = new DelegatingServletInputStream(new GZIPInputStream(super.getInputStream()));
            }
            return stream;
        }

    }

}
