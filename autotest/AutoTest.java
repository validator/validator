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

	/**
	 * Constructor.
	 * 
	 * @throws SAXException
	 */
	public AutoTest() throws SAXException {
		saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		saxReader.setContentHandler(new AutoTestContentHandler());
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
		if (args.length != 1) {
			System.out.println("Usage : AutoTest uri");
			System.exit(1);
		}

		String uri = args[0];

		try {
			AutoTest parser = new AutoTest();
			parser.parse(uri);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}