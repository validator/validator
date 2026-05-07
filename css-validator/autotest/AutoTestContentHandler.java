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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

	public static final String CLI_PARAMS = "--output=soap12";
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
	int testFailCount = 0;
	int testSuccessCount = 0;
	int testErrorCount = 0; // Errors while trying to run the test
	boolean hasError = false;
	String urlString = "";
	String file = "";
	String desc = "";
	Result awaitedResult = new Result();
	Result result = new Result();
	String profile;
	String warning;
	String medium;
	String testInstance = "servlet";
	StringBuilder errorSb;
	
	/**
	 * Default Constructor.
	 */
	public AutoTestContentHandler(String testInstance) {
		super();
		// On definit le locator par defaut.
		// locator = new LocatorImpl();
		if (testInstance != null) {
			this.testInstance = testInstance;
		}
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

			// Set default value of warning to 1, because
			// - The test suite ran by upstream uses the javasript at autotest/client/buildtest.js,
			//   and the default value of warning is 1.
			// - if the GET request to the servlet doesn't define a warning, it will be 0 (as per the
			// ApplContext class default values).
			// - on the contrary, the default value for the warning of the CLI is 2.
			// So we have to set the default value to 1 here, so that when the warning is not defined
			// it means 1. This is required to harmonize test result between call to jar and call to servlet.
			warning = "1";     // Set to same default value as in autotest/client/buildtest.js
			profile = "css21"; // Set to same default value as in autotest/client/buildtest.js
			medium = "all";    // Set to same default value as in autotest/client/buildtest.js
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

	private void waitProcess(Process p, List<String> command) {
		boolean waitForValue = false;
		try {
			waitForValue = p.waitFor(20,java.util.concurrent.TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			hasError = true;
			errorSb.append("Request: ");
			errorSb.append(command.toString());
			errorSb.append(System.getProperty("line.separator"));
			errorSb.append("Timeout reached. Subprocess stopped.");
			errorSb.append(System.getProperty("line.separator"));
			errorSb.append(e.getStackTrace().toString());
			printError(command, "Timeout reached. Subprocess stopped.");
			printErrorToConsole();
			return;
		}
		if (waitForValue == true && p.exitValue() == 1) {
			hasError = true;
			errorSb.append("Request: ");
			errorSb.append(command.toString());
			errorSb.append(System.getProperty("line.separator"));
			errorSb.append("Command failed with exit code: " + p.exitValue());
			errorSb.append(System.getProperty("line.separator"));

			StringBuilder cmdOutput = new StringBuilder();
			try {
				String line;
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					cmdOutput.append(line);
					cmdOutput.append(System.getProperty("line.separator"));
				}
				input.close();
				BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				while ((line = error.readLine()) != null) {
					cmdOutput.append(line);
					cmdOutput.append(System.getProperty("line.separator"));
				}
				error.close();
				errorSb.append(cmdOutput);
			}
			catch (IOException e) {
				errorSb.append(e.getMessage());
				errorSb.append(System.getProperty("line.separator"));
				errorSb.append(e.getStackTrace().toString());
				printError(command, e.getMessage());
				printErrorToConsole();
				return;
			}
			printError(command, "Command failed with exit code: " + p.exitValue() + "<pre>" + cmdOutput + "</pre>");
			printErrorToConsole();
			//System.exit(p.exitValue());
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
			hasError = false;
			errorSb = new StringBuilder("");
			System.out.print(urlString + "... ");
			String validURL = createValidURL(urlString);
			String val;
			List<String> command = new ArrayList<>();
			if (isFile) {
				InputStream content;
				String text = "";
				try {
					content = //
						AutoTestContentHandler.class //
						.getClassLoader()
						.getResourceAsStream(urlString);
					byte[] textBytes = new byte[content.available()];
					ByteArrayOutputStream result = new ByteArrayOutputStream();
					for (int length; (length = content.read(textBytes)) != -1; ) {
						result.write(textBytes, 0, length);
					}
					// Files are encoded in ISO-8859-1
					// ie. "testsuite/properties/positive/content/css3/001.css"
					text = createValidURL(result.toString("ISO-8859-1"));
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				val = VALIDATOR + "text=" + text;
			} else {
				val = VALIDATOR + "uri=" + validURL;
			}

			if (warning != null) {
				val += "&warning=" + warning;
				if (warning.equals("no")) {
					command.add("--warning=-1");
				} else {
					command.add("--warning=" + warning);
				}
			}
			if (profile != null) {
				val += "&profile=" + profile;
				command.add("--profile=" + profile);
			}
			if (medium != null) {
				val += "&medium=" + medium;
				command.add("--medium=" + medium);
			}
			val += PARAMS;
			command.add(CLI_PARAMS);

			if (isFile) {
				command.add("file:" + urlString);
			} else {
				command.add(urlString);
			}

			try {
				InputStream res = null;
				Reply reply = null;
				if (testInstance.equals("servlet")) {
					HttpManager manager = HttpManager.getManager();
					Request request = manager.createRequest();
					request.setMethod(HTTP.GET);
					// System.out.println(val);
					request.setURL(new URL(val));
					reply = manager.runRequest(request);
					res = reply.getInputStream();
				} else if (testInstance.equals("jar")) {
					Runtime r = Runtime.getRuntime();
					command.add(0, "java");
					command.add(1, "org.w3c.css.css.CssValidator");
					ProcessBuilder pb = new ProcessBuilder(command);
					Map<String, String> env = pb.environment();
					env.put("CLASSPATH", env.get("CLASSPATH") + ":css-validator.jar");
					Process p = pb.start();
					waitProcess(p, command);
					res = p.getInputStream();
				} else if (testInstance.equals("cli")) {
					Runtime r = Runtime.getRuntime();
					command.add(0, "css-validator");
					ProcessBuilder pb = new ProcessBuilder(command);
					Process p = pb.start();
					waitProcess(p, command);
					res = p.getInputStream();
				} else {
					System.err.println("Unsupported operation. Invalid instance or instance not set: " + testInstance);
					System.exit(2);
				}

				int currentChar;
				StringBuffer buf = new StringBuffer();
				while ((currentChar = res.read()) != -1) {
					buf.append((char) currentChar);
				}

				if (testInstance.equals("servlet") && reply.getStatus() == 500) { // Internal Server Error
					hasError = true;
					if (buf.indexOf("env:Sender") != -1) {
						printError(val, "Reply status code: 500<br/>"
								+ "Invalid URL: Sender error");
						errorSb.append(val);
						errorSb.append(System.getProperty("line.separator"));
						errorSb.append("Reply status code: 500. Invalid URL: Sender error");
					} else if (buf.indexOf("env:Receiver") != -1) {
						printError(val, "Reply status code: 500<br/>"
								+ "Unreachable URL: Receiver error");
						errorSb.append(val);
						errorSb.append(System.getProperty("line.separator"));
						errorSb.append("Reply status code: 500. Unreachable URL: Receiver error");
					} else {
						printError(val, "Reply status code: 500");
						errorSb.append(val);
						errorSb.append(System.getProperty("line.separator"));
						errorSb.append("Reply status code: 500");
					}
					printErrorToConsole();
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
					printResultToConsole(urlString);
				}

			} catch (MalformedURLException e) {
				if (hasError == false) {
					hasError = true;
					errorSb.append("Request: ");
					errorSb.append(testInstance.equals("servlet") ? truncateString(val) : command.toString());
					errorSb.append(System.getProperty("line.separator"));
					errorSb.append(truncateString(e.getMessage()));
					errorSb.append(System.getProperty("line.separator"));
					printError(val, e.getMessage());
					printErrorToConsole();
				}
			} catch (IOException e) {
				if (hasError == false) {
					hasError = true;
					errorSb.append("Request: ");
					errorSb.append(testInstance.equals("servlet") ? truncateString(val) : command.toString());
					errorSb.append(System.getProperty("line.separator"));
					errorSb.append(truncateString(e.getMessage()));
					errorSb.append(System.getProperty("line.separator"));
					printError(val, e.getMessage());
					printErrorToConsole();
				}
			} catch (HttpException e) {
				if (hasError == false) {
					hasError = true;
					errorSb.append("Request: ");
					errorSb.append(testInstance.equals("servlet") ? truncateString(val) : command.toString());
					errorSb.append(System.getProperty("line.separator"));
					errorSb.append(truncateString(e.getMessage()));
					errorSb.append(System.getProperty("line.separator"));
					printError(val, e.getMessage());
					printErrorToConsole();
				}
			}

			if (hasError == true) {
				testErrorCount++;
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

	private String truncateString(String str) {
		int maxLength = 512;
		int tailLength = 100;
		if (str.length() <= maxLength) {
			return str;
		} else {
			return str.substring(0, maxLength) + "...[TRUNCATED]..." + str.substring(str.length()-tailLength, str.length()) + "[TRUNCATED]";
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
	 * Return whether "Valid" status is equal
	 *
	 */
	private boolean isValidEqual() {
		return (awaitedResult.isValid() == result.isValid());
	}

	/**
	 * Return whether "Warnings" status is equal
	 *
	 */
	private boolean isWarningsEqual() {
		return (awaitedResult.getWarnings() == result.getWarnings());
	}

	/**
	 * Return whether "Errors" status is equal
	 *
	 */
	private boolean isErrorsEqual() {
		return (awaitedResult.getErrors() == result.getErrors());
	}

	/**
	 * Prints an HTML result of a validation to StdOut
	 *
	 * @param validatorPage
	 *            the validator page result
	 */
	private void printResultToConsole(String urlString) {
		if (isValidEqual() && isWarningsEqual() && isErrorsEqual()) {
			testSuccessCount++;
			System.out.println(" Success");
		} else {
			testFailCount++;
			System.out.println(" \u001B[31mFailure\u001B[0m");
			System.err.println("\t" + urlString);
			System.err.print("\tExpected:");
			System.err.print("\tV:"+awaitedResult.isValid());
			System.err.print("\tE:"+awaitedResult.getErrors());
			System.err.println("\tW:"+awaitedResult.getWarnings());
			System.err.print("\tResult:\t");
			System.err.print("\tV:"+result.isValid());
			System.err.print("\tE:"+result.getErrors());
			System.err.println("\tW:"+result.getWarnings());
		}
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
		String urlString2 = urlString.replaceAll("&", "&amp;");

		print("    <div class=\"error\">");
		print("      <h3><a href=\"" + urlString2 + "\">"
				+ urlString2 + "</a></h3>");
		print("      <p><a href=\"" + validatorPage
				+ "\">Go to the Validator page</a></p>");
		print("      <p>" + desc + "</p>");
		print("      <p>" + truncateString(message) + "</p>");
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
	private void printError(List<String> command, String message) {

		String urlString2 = urlString.replaceAll("&", "&amp;");

		print("    <div class=\"error\">");
		print("      <h3><a href=\"" + urlString2 + "\">"
				+ urlString2 + "</a></h3>");
		print("      <p>Command: " + command + "</p>");
		print("      <p>" + desc + "</p>");
		print("      <p>" + message + "</p>");
		print("    </div>");
	}

	/**
	 * Used when an error occurs. Prints to console.
	 *
	 */
	private void printErrorToConsole() {
		System.out.println(" \u001B[31mError\u001B[0m");
		System.err.println(urlString.indent(4));
		System.err.println(errorSb.toString().indent(4)); // String.indent() requires java >= 12.
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
		// 'à' character is present in 'testsuite/properties/positive/content/css2/001.css'
		res = res.replaceAll("à", "%C3%A0");
		return res;
	}

	public boolean hasErrors() {
		if (testFailCount > 0 || testErrorCount > 0) {
			return true;
		}
		return false;
	}

	public int getTestFailCount() {
		return testFailCount;
	}

	public int getTestSuccessCount() {
		return testSuccessCount;
	}

	public int getTestErrorCount() {
		return testErrorCount;
	}

}
