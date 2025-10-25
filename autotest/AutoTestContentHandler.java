package autotest;

// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html

import org.w3c.www.http.HTTP;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


//import org.xml.sax.helpers.LocatorImpl;
/*
 * TODO: add support for different profiles, Sender/Receiver errors
 */
/**
 * @author smeric
 * 
 * Exemple d'implementation extremement simplifiee d'un SAX XML ContentHandler.
 * Le but de cet exemple est purement pedagogique. Very simple implementation
 * sample for XML SAX ContentHandler.
 */
public class AutoTestContentHandler implements ContentHandler {

	public static final String VALIDATOR = "http://jigsaw.w3.org/css-validator/validator?";
	public static final String PARAMS = "&output=soap12";
	public static final int TESTSUITE = "testsuite".hashCode();
	public static final int TEST = "test".hashCode();
	public static final int TYPE = "type".hashCode();
	public static final int TITLE = "title".hashCode();
	public static final int URL = "url".hashCode();
	public static final int FILE = "file".hashCode();
	public static final int DESCRIPTION = "description".hashCode();
	public static final int RESULT = "result".hashCode();
	public static final int VALIDITY = "valid".hashCode();
	public static final int ERRORS = "errors".hashCode();
	public static final int WARNINGS = "warnings".hashCode();

	// file writer
	private String s = System.getProperty("file.separator");
	private String ret = System.getProperty("line.separator");
	private String OutputFile = "autotest" + s + "results" + s + "results.html";
	private BufferedWriter bw;

	// private Locator locator;
	boolean inUrl = false;
	boolean isFile = false;
	boolean inDesc = false;
	boolean inErrors = false;
	boolean inWarnings = false;
	String urlString = "";
	String file = "";
	String desc = "";
	Result awaitedResult = new Result();
	Result result = new Result();
	String profile;
	String warning;
	String medium;
	
	/**
	 * Default Constructor.
	 */
	public AutoTestContentHandler() {
		super();
		// On definit le locator par defaut.
		// locator = new LocatorImpl();
	}

	/**
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator value) {
		// locator = value;
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		try {
			File f = new File(OutputFile);
			if (!f.exists())
				f.createNewFile();
			bw = new BufferedWriter(new FileWriter(OutputFile));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 *      java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String URI)
			throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
	}
	
	public void print (String str) {
		try {
			 bw.write(str + ret);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void print () {
		try {
			 bw.append(ret);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String nameSpaceURI, String localName,
			String rawName, Attributes attributs) throws SAXException {

		int element = localName.hashCode();

		if (element == TESTSUITE) {
			print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
			print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">");
			print("  <head>");
			print("    <meta http-equiv=\"Content-Language\" content=\"en\" />");
			print("    <title>Tests Results</title>");
			print("    <style type=\"text/css\">");
			print("      h1 {");
			print("        text-align: center;");
			print("        border: 2px solid;");
			print("      }");
			print("      h2 {");
			print("        text-decoration: underline;");
			print("        text-transform: capitalize");
			print("      }");
			print("      h3 {");
			print("        font-size: 15pt;");
			print("      }");
			print("      dd {");
			print("        display: inline;");
			print("      }");
			print("      .res, .error {");
			print("        border-bottom: 1px solid black;");
			print("      }");
			print("      #valid {");
			print("        text-align: center;");
			print("      }");
			print("    </style>");
			print("  </head>");
			print();
			print("  <body>");
			print("    <h1>Test Suite</h1>");

		} else if (element == TEST) {
			awaitedResult = new Result();
			urlString = "";
			file = "";
			desc = "";
			result = new Result();

			warning = null;
			profile = null;
			medium = null;
			for (int i = 0; i < attributs.getLength(); i++) {
				String currentAttr = attributs.getLocalName(i);
				if (currentAttr.equals("warning")) {
					warning = attributs.getValue(i);
				} else if (currentAttr.equals("profile")) {
					profile = attributs.getValue(i);
				} else if (currentAttr.equals("medium")) {
					medium = attributs.getValue(i);
				}
			}
		} else if (element == TYPE) {
			if (attributs.getLength() >= 1
					&& attributs.getLocalName(0).hashCode() == TITLE) {
				print("    <h2>" + attributs.getValue(0) + "</h2>");
			}
		} else if (element == URL) {
			inUrl = true;
			isFile = false;
		} else if (element == FILE) {
			inUrl = true;
			isFile = true;
		} else if (element == DESCRIPTION) {
			inDesc = true;
		} else if (element == RESULT) {
			boolean valid = false;
			if (attributs.getLength() >= 1
					&& attributs.getLocalName(0).hashCode() == VALIDITY) {
				valid = attributs.getValue(0).equals("true");
			}
			awaitedResult.setValid(valid);
		} else if (element == ERRORS) {
			inErrors = true;
		} else if (element == WARNINGS) {
			inWarnings = true;
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String nameSpaceURI, String localName, String rawName)
			throws SAXException {

		int element = localName.hashCode();

		if (element == TESTSUITE) {
			print(" <p id=\"valid\">");
			print(" <a href=\"https://validator.w3.org/check?uri=referer\">");
			print(" <img style=\"border:0;width:88px;height:31px\"");
			print(" src=\"https://www.w3.org/Icons/valid-xhtml10\"");
			print(" alt=\"Valid XHTML 1.0!\" height=\"31\" width=\"88\" />");
			print(" </a>");
			print(" <a href=\"https://jigsaw.w3.org/css-validator/check/referer\">");
			print(" <img style=\"border:0;width:88px;height:31px\"");
			print(" src=\"https://jigsaw.w3.org/css-validator/images/vcss\"");
			print(" alt=\"Valid CSS!\" />");
			print(" </a>");
			print(" </p>");
			print("</body>");
			print();
			print("</html>");
			try {
				bw.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} else if (element == TEST) {
			String val;
			System.err.println(urlString);
			String validURL = createValidURL(urlString);
			if (isFile) {
				InputStream content;
				String text = "";
				try {
					content = //
						AutoTestContentHandler.class //
						.getClassLoader()
						.getResourceAsStream(urlString);
					byte[] textBytes = new byte[content.available()];
					content.read(textBytes, 0, textBytes.length);
					text = createValidURL(new String(textBytes));
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				val = VALIDATOR + "text=" + text;
			} else {
				val = VALIDATOR + "uri=" + validURL;
			}

			if (warning != null) {
				val += "&warning=" + warning;
			}
			if (profile != null) {
				val += "&profile=" + profile;
			}
			if (medium != null) {
				val += "&medium=" + medium;
			}
			val += PARAMS;

			try {
				HttpManager manager = HttpManager.getManager();
				Request request = manager.createRequest();
				request.setMethod(HTTP.GET);
				System.err.println(val);
				request.setURL(new URL(val));
				Reply reply = manager.runRequest(request);
				// Get the reply input stream that contains the actual data:
				InputStream res = reply.getInputStream();

				int currentChar;
				StringBuffer buf = new StringBuffer();
				while ((currentChar = res.read()) != -1) {
					buf.append((char) currentChar);
				}

				if (reply.getStatus() == 500) { // Internal Server Error
					if (buf.indexOf("env:Sender") != -1) {
						printError(val, "Reply status code: 500<br/>"
								+ "Invalid URL: Sender error");
					} else if (buf.indexOf("env:Receiver") != -1) {
						printError(val, "Reply status code: 500<br/>"
								+ "Unreachable URL: Receiver error");
					} else {
						printError(val, "Reply status code: 500");
					}
				} else {
					result = new Result();
					int begin = buf.indexOf("<m:validity>");
					int end;
					if (begin != -1) {
						end = buf.indexOf("</m:validity>");
						if (end != -1) {
							String v = buf.substring(begin + 12, end).trim();
							result.setValid(v.equals("true"));
						}
					}
					begin = buf.indexOf("<m:errorcount>");
					end = buf.indexOf("</m:errorcount>");
					if (begin != -1 && end != -1) {
						String err = buf.substring(begin + 14, end).trim();
						result.setErrors(Integer.parseInt(err));
					}
					begin = buf.indexOf("<m:warningcount>");
					end = buf.indexOf("</m:warningcount>");
					if (begin != -1 && end != -1) {
						String warn = buf.substring(begin + 16, end).trim();
						result.setWarnings(Integer.parseInt(warn));
					}
					printResult(val.substring(0, val.length() - 14));
				}

			} catch (MalformedURLException e) {
				printError(val, e.getMessage());
			} catch (IOException e) {
				printError(val, e.getMessage());
			} catch (HttpException e) {
				printError(val, e.getMessage());
			}

			isFile = false;
		} else if (element == URL) {
			inUrl = false;
		} else if (element == FILE) {
			inUrl = false;
		} else if (element == DESCRIPTION) {
			inDesc = false;
		} else if (element == ERRORS) {
			inErrors = false;
		} else if (element == WARNINGS) {
			inWarnings = false;
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int end) throws SAXException {
		if (inUrl) {
			urlString += new String(ch, start, end).trim();
		} else if (inDesc) {
			desc += new String(ch, start, end).trim();
		} else if (inErrors) {
			int errors;
			try {
				errors = Integer.parseInt(new String(ch, start, end));
			} catch (NumberFormatException e) {
				errors = 0;
			}
			awaitedResult.setErrors(errors);
		} else if (inWarnings) {
			int warnings;
			try {
				warnings= Integer.parseInt(new String(ch, start, end));
			} catch (NumberFormatException e) {
				warnings = 0;
			}
			awaitedResult.setWarnings(warnings);
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int end)
			throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 *      java.lang.String)
	 */
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String arg0) throws SAXException {
		System.err.println("Malformed entity: " + arg0);
	}

	/**
	 * Prints an HTML result of a validation
	 * 
	 * @param validatorPage
	 *            the validator page result
	 */
	private void printResult(String validatorPage) {

		validatorPage = validatorPage.replaceAll("&", "&amp;");
		urlString = urlString.replaceAll("&", "&amp;");

		print("    <div class=\"res\">");
		print("      <h3><a href=\"" + urlString + "\">"
				+ urlString + "</a></h3>");
		print("      <p><a href=\"" + validatorPage
				+ "\">Go to the Validator page</a></p>");
		print("      <p>" + desc + "</p>");
		print("      <dl>");
		print("	<dt>Awaited result</dt>");
		print("	<dd>"
				+ (awaitedResult.isValid() ? "Valid" : "Not valid") + "</dd>");
		print("	<dd>Errors: " + awaitedResult.getErrors()
				+ "</dd>");
		print("	<dd>Warnings: " + awaitedResult.getWarnings()
				+ "</dd>");
		print("	<dt>Result</dt>");
		print("	<dd>" + (result.isValid() ? "Valid" : "Not valid")
				+ "</dd>");
		print("	<dd>Errors: " + result.getErrors() + "</dd>");
		print("	<dd>Warnings: " + result.getWarnings() + "</dd>");
		print("      </dl>");
		print("    </div>");
	}

	/**
	 * Used when an error occurs
	 * 
	 * @param validatorPage
	 *            the validator page result
	 * @param message
	 *            the message to be displayed
	 */
	private void printError(String validatorPage, String message) {

		validatorPage = validatorPage.replaceAll("&", "&amp;");
		urlString = urlString.replaceAll("&", "&amp;");

		print("    <div class=\"error\">");
		print("      <h3><a href=\"" + urlString + "\">"
				+ urlString + "</a></h3>");
		print("      <p><a href=\"" + validatorPage
				+ "\">Go to the Validator page</a></p>");
		print("      <p>" + desc + "</p>");
		print("      <p>" + message + "</p>");
		print("    </div>");
	}

	/**
	 * Replaces all URL special chars in a String with their matching URL
	 * entities
	 * 
	 * @param url
	 *            the url to transform
	 * @return the valid URL
	 */
	public String createValidURL(String url) {
		String res = url;
		res = res.replaceAll("%", "%25");
		res = res.replaceAll("\"", "%22");
		res = res.replaceAll("\\{", "%7B");
		res = res.replaceAll("\\}", "%7D");
		res = res.replaceAll("\\\t", "%09");
		res = res.replaceAll(" ", "+");
		res = res.replaceAll("#", "%23");
		res = res.replaceAll("&", "%26");
		res = res.replaceAll("\\(", "%28");
		res = res.replaceAll("\\)", "%29");
		res = res.replaceAll(",", "%2C");
		res = res.replaceAll("\\.", "%2E");
		res = res.replaceAll("/", "%2F");
		res = res.replaceAll(":", "%3A");
		res = res.replaceAll(";", "%3B");
		res = res.replaceAll("<", "%3C");
		res = res.replaceAll("=", "%3D");
		res = res.replaceAll(">", "%3E");
		res = res.replaceAll("\\?", "%3F");
		res = res.replaceAll("@", "%40");
		res = res.replaceAll("\\[", "%5B");
		res = res.replaceAll("\\\\", "%5C");
		res = res.replaceAll("\\]", "%5D");
		res = res.replaceAll("\\^", "%5E");
		res = res.replaceAll("'", "%27");
		res = res.replaceAll("\\|", "%7C");
		res = res.replaceAll("~'", "%7E");
		res = res.replaceAll("\\\n", "");
		res = res.replaceAll("\\\r", "");
		return res;
	}

}
