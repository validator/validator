//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.HTTPURL;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * <H3>
 * &nbsp;&nbsp; URL
 * </H3>
 * <p/>
 * A Uniform Resource Locator (URL) is identified with a functional notation:
 * <PRE>
 * BODY { background: url(http://www.bg.com/pinkish.gif) }
 * </PRE>
 * <p/>
 * The format of a URL value is 'url(' followed by optional white space followed
 * by an optional single quote (') or double quote (") character followed by
 * the URL itself (as defined in <A HREF="#ref11">[11]</A>) followed by an optional
 * single quote (') or double quote (") character followed by optional whitespace
 * followed by ')'. Quote characters that are not part of the URL itself must
 * be balanced.
 * <p/>
 * Parentheses, commas, whitespace characters, single quotes (') and double
 * quotes (") appearing in a URL must be escaped with a backslash: '\(', '\)',
 * '\,'.
 * <p/>
 * Partial URLs are interpreted relative to the source of the style sheet, not
 * relative to the document:
 * <PRE>
 * BODY { background: url(yellow) }
 * </PRE>
 * See also
 * <p/>
 * <A NAME="ref11">[11]</A> T Berners-Lee, L Masinter, M McCahill: "Uniform
 * Resource Locators (URL)", <A href="ftp://ds.internic.net/rfc/rfc1738.txt">RFC
 * 1738</A>, CERN, Xerox Corporation, University of Minnesota, December 1994
 *
 * @version $Revision$
 */
public class CssURL extends CssValue {

    public static final int type = CssTypes.CSS_URL;

    public final int getType() {
        return type;
    }

    String value;
    String full = null;
    private boolean addQuotes = false;

    URL base;
    URL urlValue = null;

    /**
     * Set the value of this URL.
     *
     * @param s  the string representation of the URL.
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException The unit is incorrect
     * @deprecated
     */
    @Deprecated
    public void set(String s, ApplContext ac)
            throws InvalidParamException {
        throw new InvalidParamException("Deprecated method invocation", ac);
    }

    /**
     * Set the value of this URL.
     *
     * @param s    the string representation of the URL.
     * @param ac   For errors and warnings reports.
     * @param base the base location of the style sheet
     * @throws InvalidParamException The unit is incorrect
     */
    public void set(String s, ApplContext ac, URL base)
            throws InvalidParamException {
        int ppos = s.indexOf('(');
        String urlHeading = s.substring(0, ppos).toLowerCase();
        String urlname = s.substring(ppos + 1, s.length() - 1).trim();
        this.base = base;

        urlname = urlname.trim();
        if (urlname.isEmpty()) {
            // okay, no further modifications needed
        } else {
            char firstc = urlname.charAt(0);

            if (firstc == '"' || firstc == '\'') {
                final int l = urlname.length() - 1;
                if (firstc == urlname.charAt(l)) {
                    urlname = urlname.substring(1, l);
                    addQuotes = true;
                } else {
                    throw new InvalidParamException("url", s, ac);
                }
            }
        }

        value = filterURLData(urlname);
        full = null;
        urlHeading = StringUtils.convertIdent(urlHeading, ac).toLowerCase(Locale.ENGLISH);
        if (!urlHeading.startsWith("url")) {
            throw new InvalidParamException("url", s, ac);
        }
        // special case for data url...
        if (urlname.contains("data:")) {
            // no more processing.
            return;
        }
        // now add the URL to the list of seen URLs in the context
        try {
            ac.addLinkedURI(getURL());
        } catch (MalformedURLException mex) {
            // error? throw an exception
            throw new InvalidParamException("url", s, ac);
        }
    }

    private String filterURLData(String source) {
        StringBuilder sb = new StringBuilder();
        // here we just escape < and >, we might do more validation
        // like base64 encoding checks, when necessary
        for (char c : source.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("%3c");
                    break;
                case '>':
                    sb.append("%3e");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Get the internal value.
     */
    public Object get() {
        return value;
    }

    /**
     * Returns the URL
     *
     * @return the URL
     * @throws java.net.MalformedURLException (self explanatory)
     */
    public URL getURL() throws MalformedURLException {
        if (urlValue == null) {
            urlValue = HTTPURL.getURL(base, value);
        }
        return urlValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (full != null) {
            return full;
        }
        StringBuilder sb = new StringBuilder("url(");
        if (addQuotes) {
            sb.append('"').append(value).append("\")");
        } else {
            sb.append(value).append(')');
        }
        return full = sb.toString();
    }

    /**
     * Compares two values for equality.
     *
     * @param url The other value.
     */
    public boolean equals(Object url) {
        return (url instanceof CssURL && value.equals(((CssURL) url).value));
    }

}
