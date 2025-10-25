// MimeType.java
// $Id$
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mime;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to represent parsed MIME types.
 * It creates this representation from a string based representation of
 * the MIME type, as defined in the RFC 1345.
 */

public class MimeType implements Serializable, Cloneable {
	/**
	 * List of well known MIME types:
	 */
	public static MimeType TEXT_HTML = null;
	public static MimeType APPLICATION_POSTSCRIPT = null;
	public static MimeType TEXT_PLAIN = null;
	public static MimeType APPLICATION_X_WWW_FORM_URLENCODED = null;
	public static MimeType APPLICATION_OCTET_STREAM = null;
	public static MimeType MULTIPART_FORM_DATA = null;
	public static MimeType APPLICATION_X_JAVA_AGENT = null;
	public static MimeType MESSAGE_HTTP = null;
	public static MimeType TEXT_CSS = null;
	public static MimeType TEXT_XML = null;
	public static MimeType APPLICATION_XML = null;
	public static MimeType TEXT = null;
	public static MimeType APPLICATION_RDF_XML = null;
	public static MimeType APPLICATION_XHTML_XML = null;

	public static String star = "*".intern();

	static {
		try {
			TEXT_HTML
					= new MimeType("text/html");
			APPLICATION_POSTSCRIPT
					= new MimeType("application/postscript");
			TEXT_PLAIN
					= new MimeType("text/plain");
			APPLICATION_X_WWW_FORM_URLENCODED
					= new MimeType("application/x-www-form-urlencoded");
			APPLICATION_OCTET_STREAM
					= new MimeType("application/octet-stream");
			MULTIPART_FORM_DATA
					= new MimeType("multipart/form-data");
			APPLICATION_X_JAVA_AGENT
					= new MimeType("application/x-java-agent");
			MESSAGE_HTTP
					= new MimeType("message/http");
			TEXT_CSS
					= new MimeType("text/css");
			TEXT_XML
					= new MimeType("text/xml");
			TEXT
					= new MimeType("text/*");
			APPLICATION_RDF_XML
					= new MimeType("application/rdf+xml");
			APPLICATION_XHTML_XML
					= new MimeType("application/xhtml+xml");
			APPLICATION_XML
					= new MimeType("application/xml");
		} catch (MimeTypeFormatException e) {
			System.out.println("httpd.MimeType: invalid static init.");
			System.exit(1);
		}
	}

	public final static int NO_MATCH = -1;
	public final static int MATCH_TYPE = 1;
	public final static int MATCH_SPECIFIC_TYPE = 2;
	public final static int MATCH_SUBTYPE = 3;
	public final static int MATCH_SPECIFIC_SUBTYPE = 4;

	/**
	 * String representation of type
	 *
	 * @serial
	 */
	protected String type = null;
	/**
	 * String representation of subtype
	 *
	 * @serial
	 */
	protected String subtype = null;
	/**
	 * parameter names
	 *
	 * @serial
	 */
	protected String pnames[] = null;
	/**
	 * parameter values
	 *
	 * @serial
	 */
	protected String pvalues[] = null;
	/**
	 * external form of this mime type
	 *
	 * @serial
	 */
	protected String external = null;

	/**
	 * How good the given MimeType matches the receiver of the method ?
	 * This method returns a matching level among:
	 * <dl>
	 * <dt>NO_MATCH<dd>Types not matching,</dd>
	 * <dt>MATCH_TYPE<dd>Types match,</dd>
	 * <dt>MATCH_SPECIFIC_TYPE<dd>Types match exactly,</dd>
	 * <dt>MATCH_SUBTYPE<dd>Types match, subtypes matches too</dd>
	 * <dt>MATCH_SPECIFIC_SUBTYPE<dd>Types match, subtypes matches exactly</dd>
	 * </dl>
	 * The matches are ranked from worst match to best match, a simple
	 * Max ( match[i], matched) will give the best match.
	 *
	 * @param other The other MimeType to match against ourself.
	 */

	public int match(MimeType other) {
		int match = NO_MATCH;
		// match types:
		if ((type == star) || (other.type == star)) {
			return MATCH_TYPE;
		} else if (type != other.type) {
			return NO_MATCH;
		} else {
			match = MATCH_SPECIFIC_TYPE;
		}
		// match subtypes:
		if ((subtype == star) || (other.subtype == star)) {
			match = MATCH_SUBTYPE;
		} else if (subtype != other.subtype) {
			return NO_MATCH;
		} else {
			match = MATCH_SPECIFIC_SUBTYPE;
		}
		return match;
	}

	/**
	 * Find out if mime types are equivalent, based on heuristics
	 * like text/xml <=> application/xml and other problems related
	 * to format that may have multiple mime types.
	 * Note that text/html and application/xhtml+xml are not exactly
	 * the same
	 *
	 * @param mtype, a MimeType
	 * @return a boolean, true if the two mime types are equivalent
	 */
	public boolean equiv(MimeType mtype) {
		if (match(mtype) == MATCH_SPECIFIC_SUBTYPE) {
			return true;
		}
		if ((match(TEXT_XML) == MATCH_SPECIFIC_SUBTYPE) ||
				(match(APPLICATION_XML) == MATCH_SPECIFIC_SUBTYPE)) {
			if ((mtype.match(TEXT_XML) == MATCH_SPECIFIC_SUBTYPE) ||
					(mtype.match(APPLICATION_XML) == MATCH_SPECIFIC_SUBTYPE)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A printable representation of this MimeType.
	 * The printed representation is guaranteed to be parseable by the
	 * String constructor.
	 */

	public String toString() {
		if (external == null) {
			StringBuilder sb = new StringBuilder(type);
			sb.append((char) '/');
			sb.append(subtype);
			if (pnames != null) {
				for (int i = 0; i < pnames.length; i++) {
					sb.append(';');
					sb.append(pnames[i]);
					if (pvalues[i] != null) {
						sb.append('=');
						sb.append(pvalues[i]);
					}
				}
			}
			external = sb.toString().intern();
		}
		return external;
	}

	/**
	 * Does this MIME type has some value for the given parameter ?
	 *
	 * @param name The parameter to check.
	 * @return <strong>True</strong> if this parameter has a value, false
	 *         otherwise.
	 */
	public boolean hasParameter(String name) {
		if (name != null) {
			if (pnames != null) {
				String lname = name.toLowerCase();
				for (String pname : pnames) {
					if (pname.equals(name))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the major type of this mime type.
	 *
	 * @return The major type, encoded as a String.
	 */

	public String getType() {
		return type;
	}

	/**
	 * Get the minor type (subtype) of this mime type.
	 *
	 * @return The minor or subtype encoded as a String.
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * Get a mime type parameter value.
	 *
	 * @param name The parameter whose value is to be returned.
	 * @return The parameter value, or <b>null</b> if not found.
	 */
	public String getParameterValue(String name) {
		if (name != null) {
			if (pnames != null) {
				String lname = name.toLowerCase();
				for (int i = 0; i < pnames.length; i++) {
					if (pnames[i].equals(lname))
						return pvalues[i];
				}
			}
		}
		return null;
	}

	/**
	 * adds some parameters to a MimeType
	 *
	 * @param param  a String array of parameter names
	 * @param values the corresponding String array of values
	 */
	public void addParameters(String[] param, String[] values) {
		// sanity check
		if ((param == null) || (values == null) ||
				(values.length != param.length))
			return;
		if (pnames == null) {
			pnames = param;
			pvalues = values;
		} else {
			String[] nparam = new String[pnames.length + param.length];
			String[] nvalues = new String[pvalues.length + values.length];
			System.arraycopy(pnames, 0, nparam, 0, pnames.length);
			System.arraycopy(param, 0, nparam, pnames.length, param.length);
			System.arraycopy(pvalues, 0, nvalues, 0, pvalues.length);
			System.arraycopy(values, 0, nvalues, pvalues.length, values.length);
			pnames = nparam;
			pvalues = nvalues;
		}
		for (int i = 0; i < pnames.length; i++) {
			pnames[i] = pnames[i].toLowerCase();
		}
		external = null;
	}

	/**
	 * get a clone of this object
	 *
	 * @return another cloned instance of MimeType
	 */
	public MimeType getClone() {
		try {
			return (MimeType) clone();
		} catch (CloneNotSupportedException ex) {
			// should never happen as we are Cloneable!
		}
		// never reached
		return null;
	}

	/**
	 * adds a parameter to a MimeType
	 *
	 * @param param the parameter name, as a String
	 * @param value the parameter value, as a Sting
	 */
	public void addParameter(String param, String value) {
		String[] p = new String[1];
		String[] v = new String[1];
		p[0] = param;
		v[0] = value;
		addParameters(p, v);
	}

	/**
	 * Set the parameter to a MimeType (replace old value if any).
	 *
	 * @param param the parameter name, as a String
	 * @param value the parameter value, as a Sting
	 */
	public void setParameter(String param, String value) {
		if (pnames == null) {
			addParameter(param, value);
		} else {
			String lparam = param.toLowerCase();
			for (int i = 0; i < pnames.length; i++) {
				if (pnames[i].equals(lparam)) {
					pvalues[i] = value;
					return;
				}
			}
			addParameter(lparam, value);
		}
	}

	/**
	 * Construct  MimeType object for the given string.
	 * The string should be the representation of the type. This methods
	 * tries to be compliant with HTTP1.1, p 15, although it is not
	 * (because of quoted-text not being accepted).
	 * FIXME
	 *
	 * @return A MimeType object
	 * @throws MimeTypeFormatException if the string couldn't be parsed.
	 * @parameter spec A string representing a MimeType
	 */
	public MimeType(String spec)
			throws MimeTypeFormatException {
		int strl = spec.length();
		int start = 0, look = -1;
		// skip leading/trailing blanks:
		while ((start < strl) && (spec.charAt(start)) <= ' ')
			start++;
		while ((strl > start) && (spec.charAt(strl - 1) <= ' '))
			strl--;
		// get the type:
		StringBuilder sb = new StringBuilder();
		while ((start < strl) && ((look = spec.charAt(start)) != '/')) {
			sb.append(Character.toLowerCase((char) look));
			start++;
		}
		if (look != '/')
			throw new MimeTypeFormatException(spec);
		this.type = sb.toString().intern();
		// get the subtype:
		start++;
		sb.setLength(0);
		while ((start < strl)
				&& ((look = spec.charAt(start)) > ' ') && (look != ';')) {
			sb.append(Character.toLowerCase((char) look));
			start++;
		}
		this.subtype = sb.toString().intern();
		// get parameters, if any:
		while ((start < strl) && ((look = spec.charAt(start)) <= ' '))
			start++;
		if (start < strl) {
			if (spec.charAt(start) != ';')
				throw new MimeTypeFormatException(spec);
			start++;
			ArrayList<String> vp = new ArrayList<String>(4);
			ArrayList<String> vv = new ArrayList<String>(4);
			while (start < strl) {
				while ((start < strl) && (spec.charAt(start) <= ' ')) start++;
				// get parameter name:
				sb.setLength(0);
				while ((start < strl)
						&& ((look = spec.charAt(start)) > ' ') && (look != '=')) {
					sb.append(Character.toLowerCase((char) look));
					start++;
				}
				String name = sb.toString();
				// get the value:
				while ((start < strl) && (spec.charAt(start) <= ' ')) start++;
				if (spec.charAt(start) != '=')
					throw new MimeTypeFormatException(spec);
				start++;
				while ((start < strl) &&
						((spec.charAt(start) == '"') ||
								(spec.charAt(start) <= ' '))) start++;
				sb.setLength(0);
				while ((start < strl)
						&& ((look = spec.charAt(start)) > ' ')
						&& (look != ';')
						&& (look != '"')) {
					sb.append((char) look);
					start++;
				}
				while ((start < strl) && (spec.charAt(start) != ';')) start++;
				start++;
				String value = sb.toString();
				vp.add(name);
				vv.add(value);
			}
			this.pnames = vp.toArray(new String[vp.size()]);
			this.pvalues = vv.toArray(new String[vv.size()]);
		}
	}

	public MimeType(String type, String subtype
			, String pnames[], String pvalues[]) {
		this.type = type.toLowerCase().intern();
		this.subtype = subtype.toLowerCase().intern();
		this.pnames = pnames;
		this.pvalues = pvalues;
	}

	public MimeType(String type, String subtype) {
		this.type = type.toLowerCase().intern();
		this.subtype = subtype.toLowerCase().intern();
	}

	public static void main(String args[]) {
		if (args.length == 1) {
			MimeType type = null;
			try {
				type = new MimeType(args[0]);
			} catch (MimeTypeFormatException e) {
			}
			if (type != null) {
				System.out.println(type);
				if (type.getClone().match(type) == MATCH_SPECIFIC_SUBTYPE) {
					System.out.println("Clone OK");
				} else {
					System.out.println("Cloning failed");
				}
			} else {
				System.out.println("Invalid mime type specification.");
			}
		} else {
			System.out.println("Usage: java MimeType <type-to-parse>");
		}
	}

}
