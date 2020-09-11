package nu.validator.checker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UnsupportedFeatureChecker extends Checker {

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }
        if ("dialog" == localName) {
            warnAboutElement(localName);
        } else if ("textarea" == localName) {
            if (atts.getIndex("", "dirname") > -1) {
                warnAboutAttributeOnElement("dirname", "textarea");
            }
            if (atts.getIndex("", "inputmode") > -1) {
                warnAboutAttribute("inputmode");
            }
        } else if ("input" == localName) {
            if (atts.getIndex("", "dirname") > -1) {
                warnAboutAttributeOnElement("dirname", "input");
            }
            if (atts.getIndex("", "inputmode") > -1) {
                warnAboutAttribute("inputmode");
            }
        }
    }

    private void warnAboutAttribute(String name) throws SAXException {
        warn("The \u201C" + name + "\u201D attribute is not supported in"
                + " all browsers. Please be sure to test, and consider"
                + " using a polyfill.");
    }

    private void warnAboutElement(String name) throws SAXException {
        warn("The \u201C" + name + "\u201D element is not supported in"
                + " all browsers. Please be sure to test, and consider"
                + " using a polyfill.");
    }

    private void warnAboutAttributeOnElement(String attributeName, String elementName) throws SAXException {
      warn("The \u201C" + attributeName + "\u201D attribute on the"
          + " \u201C" + elementName + "\u201D element is not supported"
          + " in all browsers. Please be sure to test, and consider"
          + " using a polyfill.");
    }

}
