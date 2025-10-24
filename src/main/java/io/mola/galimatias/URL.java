/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A parsed URL. Immutable.
 *
 */
public class URL implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String scheme;
    private final String schemeData;
    private final String username;
    private final String password;
    private final Host host;
    private final int port;
    private final String path;
    private final String query;
    private final String fragment;

    private final boolean isHierarchical;

    URL(final String scheme, final String schemeData,
        final String username, final String password,
        final Host host, final int port,
        final Iterable<String> pathSegments,
        final String query, final String fragment,
        final boolean isHierarchical) {
        this(scheme, schemeData, username, password, host, port, pathSegmentsToString(pathSegments),
                query, fragment, isHierarchical);
    }

    URL(final String scheme, final String schemeData,
            final String username, final String password,
            final Host host, final int port,
            final String path,
            final String query, final String fragment,
            final boolean isHierarchical) {
        if (scheme == null) {
            throw new NullPointerException("scheme cannot be null");
        }
        this.scheme = scheme;
        this.schemeData = (schemeData == null)? "" : schemeData;
        if (isHierarchical) {
            this.username = (username == null)? "" : username;
            this.password = password;
            this.host = host;
            //XXX: This is already done in some cases by the URLParser
            this.port = (port == defaultPort(this.scheme))? -1 : port;
            this.path = path;
        } else {
            this.username = "";
            this.password = null;
            this.host = null;
            this.port = -1;
            this.path = null;
        }
        this.query = query;
        this.fragment = fragment;
        this.isHierarchical = isHierarchical;
    }

    public String scheme() {
        return scheme;
    }

    public String schemeData() {
        return schemeData;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    /**
     * Gets user info component (i.e. user:pass). This will
     * return an empty string if neither user or password
     * are set.
     *
     * @return
     */
    public String userInfo() {
        if (password == null) {
            return username;
        }
        return String.format("%s:%s", username, password);
    }

    public Host host() {
        return host;
    }

    public String authority() {
        if (!isHierarchical) {
            return null;
        }
        if (host == null) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        final String userInfo = userInfo();
        if (!userInfo.isEmpty()) {
            output.append(userInfo()).append('@');
        }
        output.append(host.toHostString());
        if (port != -1) {
            output.append(':').append(port);
        }
        return output.toString();
    }

    public int port() {
        return (port == -1)? defaultPort() : port;
    }

    private static int defaultPort(final String scheme) {
        String defaultPort = URLUtils.getDefaultPortForScheme(scheme);
        if (defaultPort == null) {
            return -1;
        }
        return Integer.parseInt(defaultPort);
    }

    public int defaultPort() {
        return defaultPort(scheme);
    }

    public String path() {
        return path;
    }

    public List<String> pathSegments() {
        if (!isHierarchical) {
            return null;
        }
        return pathStringToSegments(path);
    }

    public String query() {
        return query;
    }

    /**
     * Gets the first query parameter value for a given name.
     *
     * @see {@link #queryParameters(String)}
     *
     * @param name Parameter name.
     * @return The first parameter value or null if there parameter is not present.
     */
    public String queryParameter(final String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (query == null || query.isEmpty()) {
            return null;
        }
        int start = 0;
        do {
            final int nextAmpersand = query.indexOf('&', start);
            final int end = (nextAmpersand == -1)? query.length() : nextAmpersand;
            int nextEquals = query.indexOf('=', start);
            if (nextEquals == -1 || nextEquals > end) {
                nextEquals = end;
            }
            final int thisNameLength = nextEquals - start;
            final int thisValueLength = end - nextEquals;
            if (thisNameLength == name.length() && query.regionMatches(start, name, 0, name.length())) {
                if (thisValueLength == 0) {
                    return "";
                }
                return query.substring(nextEquals + 1, end);
            }
            if (nextAmpersand == -1) {
                break;
            }
            start = nextAmpersand + 1;
        } while (true);
        return null;
    }

    /**
     * Gets all query parameter values for a given name.
     *
     * @param name Parameter name.
     * @return A {@link java.util.List} with all parameter values or null if the parameter is not present.
     */
    public List<String> queryParameters(final String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (query == null || query.isEmpty()) {
            return null;
        }
        int start = 0;
        final List<String> result = new ArrayList<String>();
        do {
            final int nextAmpersand = query.indexOf('&', start);
            final int end = (nextAmpersand == -1) ? query.length() : nextAmpersand;
            int nextEquals = query.indexOf('=', start);
            if (nextEquals == -1 || nextEquals > end) {
                nextEquals = end;
            }
            final int thisNameLength = nextEquals - start;
            final int thisValueLength = end - nextEquals;
            if (thisNameLength == name.length() && query.regionMatches(start, name, 0, name.length())) {
                if (thisValueLength == 0) {
                    result.add("");
                } else {
                    result.add(query.substring(nextEquals + 1, end));
                }
            }
            if (nextAmpersand == -1) {
                break;
            }
            start = nextAmpersand + 1;
        } while (true);
        return result;
    }

    public URLSearchParameters searchParameters() {
        return new URLSearchParameters(query);
    }

    public String fragment() {
        return fragment;
    }

    public String file() {
        if (path == null && query == null) {
            return "";
        }
        final StringBuilder output = new StringBuilder(
                ((path != null)? path.length() : 0) +
                ((query != null)? query.length() + 1 : 0)
                );
        if (path != null) {
            output.append(path);
        }
        if (query != null) {
            output.append('?').append(query);
        }
        return output.toString();
    }

    /**
     * Whether this is a hierarchical URL or not. That is, a URL that allows multiple path segments.
     *
     * The term <em>hierarchical</em> comes form the URI standard
     * (<a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>).
     * Other libraries might refer to it as <em>relative</em> or <em>cannot-be-a-base-URL</em>.
     * The later is the current WHATWG URL standard
     * (see <a href="https://github.com/whatwg/url/issues/89">whatwg/url#89</a> for the rationale).

     * @return
     */
    public boolean isHierarchical() {
        return isHierarchical;
    }

    /**
     * Shorthand for <code>!{@link #isHierarchical}</code>.
     */
    public boolean isOpaque() {
        return !isHierarchical;
    }

    private static String pathSegmentsToString(final Iterable<String> segments) {
        if (segments == null) {
            return null;
        }
        final StringBuilder output = new StringBuilder();
        for (final String segment : segments) {
            output.append('/').append(segment);
        }
        if (output.length() == 0) {
            return "/";
        }
        return output.toString();
    }

    private static List<String> pathStringToSegments(String path) {
        if (path == null) {
            return new ArrayList<String>();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        final String[] segments = path.split("/", -1);
        final List<String> result = new ArrayList<String>(segments.length + 1);
        if (segments.length == 0) {
            result.add("");
            return result;
        }
        result.addAll(Arrays.asList(segments));
        return result;
    }

    /**
     * Resolves a relative reference to an absolute URL.
     *
     * This is just a convenience method equivalent to:
     *
     * <pre>
     * <code>
     *  URL base = URL.parse("http://base.com");
     *  String relativeReference = "/foo/bar";
     *  URL absoluteURL = base.resolve(relativeReference);
     * </code>
     * </pre>
     *
     * @param input Relative reference.
     * @return Resolved absolute URL.
     * @throws GalimatiasParseException
     */
    public URL resolve(final String input) throws GalimatiasParseException {
        return new URLParser(this, input).parse();
    }

    /**
     * Returns a relative URL reference for the given URL.
     *
     * Behaves as @{link java.net.URI#relativize(URL)}.
     *
     * @param url Absolute URL.
     * @return Relative reference.
     */
    public String relativize(final URL url) {
        if (this.isOpaque() || url.isOpaque()) {
            return url.toString();
        }
        if (!this.scheme().equals(url.scheme())) {
            return url.toString();
        }
        if (this.authority() == null ^ url.authority() == null) {
            return url.toString();
        }
        if (this.authority() != null && !this.authority().equals(url.authority())) {
            return url.toString();
        }

        String prefixPath = (this.path().endsWith("/"))? this.path : this.path() + "/";

        if (!url.path().startsWith(prefixPath) && !this.path().equals(url.path())) {
            return url.toString();
        }

        StringBuilder output = new StringBuilder();
        if (!this.path().equals(url.path())) {
            output.append(url.path().replaceFirst(prefixPath, ""));
        }
        if (url.query() != null) {
            output.append('?').append(url.query());
        }
        if (url.fragment() != null) {
            output.append('#').append(url.fragment());
        }
        return output.toString();
    }

    /**
     * Parses a URL by using the default parsing options.
     *
     * @param input
     * @return
     * @throws GalimatiasParseException
     */
    public static URL parse(final String input) throws GalimatiasParseException {
        return new URLParser(input).parse();
    }

    public static URL parse(final URL base, final String input) throws GalimatiasParseException {
        return new URLParser(base, input).parse();
    }

    public static URL parse(final URLParsingSettings settings, final String input) throws GalimatiasParseException {
        return new URLParser(input).settings(settings).parse();
    }

    public static URL parse(final URLParsingSettings settings, final URL base, final String input) throws GalimatiasParseException {
        return new URLParser(base, input).settings(settings).parse();
    }

    /**
     * Gets a URL object from a relative scheme and a host.
     *
     * @param scheme
     * @param host
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildHierarchical(final String scheme, final String host) throws GalimatiasParseException {
        if (!URLUtils.isRelativeScheme(scheme)) {
            throw new GalimatiasParseException("Scheme is not relative: " + scheme);
        }
        return new URLParser(scheme + "://" + host).parse();
    }

    /**
     * Gets a URL object for file:// scheme.
     *
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildFile() throws GalimatiasParseException {
        return new URLParser("file://").parse();
    }

    /**
     * Gets a URL object from a non-relative scheme.
     *
     * @param scheme
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildOpaque(final String scheme) throws GalimatiasParseException {
        if (URLUtils.isRelativeScheme(scheme)) {
            throw new GalimatiasParseException("Scheme is relative: " + scheme);
        }
        return new URLParser(scheme + ":").parse();
    }

    public URL withScheme(final String newScheme) throws GalimatiasParseException {
        if (this.scheme.equalsIgnoreCase(newScheme)) {
            return this;
        }
        if (newScheme == null) {
            throw new NullPointerException("null scheme");
        }
        if (newScheme.isEmpty()) {
            throw new GalimatiasParseException("empty scheme");
        }
        if (URLUtils.isRelativeScheme(newScheme) == URLUtils.isRelativeScheme(this.scheme)) {
            return new URLParser(newScheme + ":", this, URLParser.ParseURLState.SCHEME_START).parse();
        }
        return new URLParser(toString().replaceFirst(this.scheme, newScheme)).parse();
    }

    public URL withUsername(String newUserName) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set username on opaque URL");
        }
        newUserName = (newUserName == null)? "" : new URLParser(newUserName).parseUsername();
        if (this.username.equals(newUserName)) {
            return this;
        }
        return new URL(this.scheme, this.schemeData, newUserName, this.password, this.host, this.port, this.path, this.query, this.fragment, true);
    }

    public URL withPassword(String newPassword) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set password on opaque URL");
        }
        if (this.password != null && this.password.equals(newPassword)) {
            return this;
        }
        newPassword = (newPassword == null || newPassword.isEmpty())? null : new URLParser(newPassword).parsePassword();
        return new URL(this.scheme, this.schemeData, this.username, newPassword, this.host, this.port, this.path, this.query, this.fragment, true);
    }

    public URL withHost(final String newHost) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set host on opaque URL");
        }
        return withHost(Host.parseHost(newHost));
    }

    public URL withHost(final Host newHost) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set host on opaque URL");
        }
        if (newHost == null) {
            throw new NullPointerException("null host");
        }
        if (this.host != null && this.host.equals(newHost)) {
            return this;
        }
        return new URL(this.scheme, this.schemeData, this.username, this.password, newHost, this.port, this.path, this.query, this.fragment, true);
    }

    public URL withPort(final int newPort) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set port on opaque URL");
        }
        if (newPort == this.port) {
            return this;
        }
        if (this.port == -1 && newPort == defaultPort()) {
            return this;
        }
        return new URL(this.scheme, this.schemeData, this.username, this.password, this.host, newPort, this.path, this.query, this.fragment, true);
    }

    public URL withPath(final String newPath) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set path on opaque URL");
        }
        return new URLParser(newPath, this, URLParser.ParseURLState.RELATIVE_PATH_START).parse();
    }

    public URL withQuery(final String newQuery) throws GalimatiasParseException {
        if (this.query == newQuery) {
            return this;
        }
        if (this.query != null && this.query.equals(newQuery)) {
            return this;
        }
        if (newQuery == null) {
            return new URL(this.scheme, this.schemeData, this.username, this.password, this.host, this.port, this.path, null, this.fragment, true);
        }
        if (newQuery.isEmpty()) {
            return new URL(this.scheme, this.schemeData, this.username, this.password, this.host, this.port, this.path, newQuery, this.fragment, true);
        }
        final String parseQuery = (newQuery.charAt(0) == '?')? newQuery.substring(1, newQuery.length()) : newQuery;
        return new URLParser(parseQuery, this, URLParser.ParseURLState.QUERY).parse();
    }

    public URL withFragment(final String newFragment) throws GalimatiasParseException {
        //if ("javascript".equals(scheme)) {
        //    throw new GalimatiasParseException("Cannot set fragment on 'javascript:' URL");
        //}
        if (this.fragment == newFragment) {
            return this;
        }
        if (this.fragment != null && this.fragment.equals(newFragment)) {
            return this;
        }
        if (newFragment == null) {
            return new URL(this.scheme, this.schemeData, this.username, this.password, this.host, this.port, this.path, this.query, null, true);
        }
        if (newFragment.isEmpty()) {
            return new URL(this.scheme, this.schemeData, this.username, this.password, this.host, this.port, this.path, this.query, newFragment, true);
        }
        final String parseFragment = (newFragment.charAt(0) == '#')? newFragment.substring(1, newFragment.length()) : newFragment;
        return new URLParser(parseFragment, this, URLParser.ParseURLState.FRAGMENT).parse();
    }

    @SuppressWarnings("javadoc")
    /**
     * Converts to {@link java.net.URI}.
     *
     * Conversion to {@link java.net.URI} will throw
     * {@link java.net.URISyntaxException} if the URL contains
     * unescaped unsafe characters as defined in RFC 2396.
     * In order to prevent this, force RFC 2396 compliance when
     * parsing the URL. For example:
     *
     * NOTE 1: This will not make distinction between no user and password and just empty
     *         user and no password.
     *      <pre>
     *          <code>
     *              URL.parse("http://example.com").toJavaURI().toString() -> "http://example.com"
     *              URL.parse("http://@example.com").toJavaURI().toString() -> "http://example.com"
     *          </code>
     *      </pre>
     *
     * TODO: Check if this exception can actually be thrown
     *
     * @return
     */
    public java.net.URI toJavaURI() throws URISyntaxException {
        if (isHierarchical) {
            return new URI(scheme(),
                    (!"".equals(userInfo()))? URLUtils.percentDecode(userInfo()) : null,
                    (host() != null)? host().toString() : null,
                    port,
                    (path() != null)? URLUtils.percentDecode(path()) : null,
                    (query() != null)? URLUtils.percentDecode(query()) : null,
                    (fragment() != null)? URLUtils.percentDecode(fragment()) : null
            );
        }
        return new URI(scheme(),
                URLUtils.percentDecode(schemeData()) + ((query() == null)? "" : "?" + URLUtils.percentDecode(query())),
                (fragment() != null)? URLUtils.percentDecode(fragment()) : null
        );
    }

    /**
     * Converts to {@link java.net.URL}.
     *
     * This method is guaranteed to not throw an exception
     * for URL protocols http, https, ftp, file and jar.
     *
     * It might or might not throw {@link java.net.MalformedURLException}
     * for other URL protocols.
     *
     * @return
     */
    public java.net.URL toJavaURL() throws MalformedURLException {
        return new java.net.URL(toString());
    }

    /**
     * Construct a URL from a {@link java.net.URI}.
     *
     * @param uri
     * @return
     */
    public static URL fromJavaURI(java.net.URI uri) {
        //TODO: Let's do this more efficient.
        try {
            return new URLParser(uri.toString()).parse();
        } catch (GalimatiasParseException e) {
            // This should not happen.
            throw new RuntimeException("BUG", e);
        }
    }

    /**
     * Construct a URL from a {@link java.net.URL}.
     *
     * @param url
     * @return
     */
    public static URL fromJavaURL(java.net.URL url) {
        //TODO: Let's do this more efficient.
        try {
            return new URLParser(url.toString()).parse();
        } catch (GalimatiasParseException e) {
            // This should not happen.
            throw new RuntimeException("BUG", e);
        }
    }

    /**
     * Serializes the URL.
     *
     * Note that the "exclude fragment flag" (as in WHATWG standard) is not implemented.
     *
     */
    @Override
    public String toString() {
        final StringBuilder output = new StringBuilder();

        output.append(scheme).append(':');

        if (isHierarchical) {
            output.append("//");
            final String userInfo = userInfo();
            if (!userInfo.isEmpty()) {
                output.append(userInfo).append('@');
            }
            if (host != null) {
                output.append(host.toHostString());
            }
            if (port != -1) {
                output.append(':').append(port);
            }
            if (path != null) {
                output.append(path);
            }
        } else {
            output.append(schemeData);
        }

        if (query != null) {
            output.append('?').append(query);
        }

        if (fragment != null) {
            output.append('#').append(fragment);
        }

        return output.toString();
    }

    /**
     * Serializes the URL to a human-readable representation. That is,
     * percent-decoded and with IDN domains in its Unicode representation.
     *
     * @return
     */
    public String toHumanString() {
        final StringBuilder output = new StringBuilder();

        output.append(scheme).append(':');

        if (isHierarchical) {
            output.append("//");
            final String userInfo = userInfo();
            if (!userInfo.isEmpty()) {
                output.append(URLUtils.percentDecode(userInfo)).append('@');
            }
            if (host != null) {
                if (host instanceof IPv6Address) {
                    output.append(host.toHostString());
                } else {
                    output.append(host.toHumanString());
                }
            }
            if (port != -1) {
                output.append(':').append(port);
            }
            if (path != null) {
                output.append(URLUtils.percentDecode(path));
            }
        } else {
            output.append(URLUtils.percentDecode(schemeData));
        }

        if (query != null) {
            output.append('?').append(URLUtils.percentDecode(query));
        }

        if (fragment != null) {
            output.append('#').append(URLUtils.percentDecode(fragment));
        }

        return output.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URL)) {
            return false;
        }
        final URL other = (URL) obj;
        return  isHierarchical == other.isHierarchical &&
                ((scheme == null)? other.scheme == null : scheme.equals(other.scheme)) &&
                ((schemeData == null)? other.schemeData == null : schemeData.equals(other.schemeData)) &&
                ((username == null)? other.username == null : username.equals(other.username)) &&
                ((password == null)? other.password == null : password.equals(other.password)) &&
                ((host == null)? other.host == null : host.equals(other.host)) &&
                port == other.port &&
                ((path == null)? other.host == null : path.equals(other.path)) &&
                ((fragment == null)? other.fragment == null : fragment.equals(other.fragment)) &&
                ((query == null)? other.query == null : query.equals(other.query))
                ;
    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (schemeData != null ? schemeData.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != -1 ? port : 0);
        result = 31 * result + (path != null? path.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() + 1 : 0);
        result = 31 * result + (fragment != null ? fragment.hashCode() + 1 : 0);
        result = 31 * result + (isHierarchical ? 1 : 0);
        return result;
    }

}
