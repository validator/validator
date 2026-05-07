package autotest;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * AutoTest<br />
 * Created: Jul 28, 2005 5:36:18 PM<br />
 * Entry point of the software.<br/>
 * This soft allows to run a battery of CSSs test cases to check the the
 * CSS Validator.<br/>
 * To run the AutoTest, use this command:<br/>
 * java AutoTest uri [options]<br/>
 * where uri is the uri of the xml file containing the list of test cases,
 * and options a String containing the options for the validator, written in the
 * "URI" style (e.g. &usermedium=all&profile=css2).
 * There are some limitations in the options you can use:
 * <ul>
 * <li> The options list must begin with &amp;</li>
 * <li> Do not put text option nor uri one, they will be appended automatically
 *  from the xml file</li>
 * <li> Do not put output option, it MUST be soap12 for the soft to work well,
 *  so it is appended automatically to the options</li> 
 * </ul>
 */
/**
 * 
 */
public class AutoTest {

	XMLReader saxReader;
	AutoTestContentHandler autoTestContentHandler;

	/**
	 * Constructor.
	 * 
	 * @throws SAXException
	 */
	public AutoTest(String testInstance) throws SAXException {
		saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		autoTestContentHandler = new AutoTestContentHandler(testInstance);
		saxReader.setContentHandler(autoTestContentHandler);
	}

	/**
	 * Parse an xml file
	 * 
	 * @param uri
	 *            the uri of the file to parse
	 * @throws IOException
	 * @throws SAXException
	 */
	public void parse(String uri) throws IOException, SAXException {
		saxReader.parse(uri);
	}

	/**
	 * Entry point of the program
	 * 
	 * @param args
	 *            list of arguments of the program: uri [options]
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage : AutoTest uri [instance]");
			System.exit(1);
		}

		String uri = null;
		String instance = null;

		if (args.length >= 1) {
			uri = args[0];
		}

		if (args.length >= 2) {
			instance = args[1];
		}

		System.out.println("Processing: " + uri);
		try {
			AutoTest parser = new AutoTest(instance);
			parser.parse(uri);
			if (parser.autoTestContentHandler.hasErrors()) {
				System.err.println("\tTests outcome:" +
					" " +
					parser.autoTestContentHandler.getTestSuccessCount() +
					" success(es)." +
					" " +
					parser.autoTestContentHandler.getTestFailCount() +
					" failure(s)." +
					" " +
					parser.autoTestContentHandler.getTestErrorCount() +
					" error(s)."
					);
				System.exit(1);
			} else {
				System.out.println("\tTests outcome:" +
					" " +
					parser.autoTestContentHandler.getTestSuccessCount() +
					" success(es)." +
					" " +
					parser.autoTestContentHandler.getTestFailCount() +
					" failure(s)." +
					" " +
					parser.autoTestContentHandler.getTestErrorCount() +
					" error(s)."
					);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
