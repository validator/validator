package org.whattf.checker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UnsupportedFeatureChecker extends Checker {

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String qName, Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }
        if ("menu" == localName) {
            warn("The \u201Cmenu\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
        } else if ("command" == localName) {
            warn("The \u201Ccommand\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
        } else if ("details" == localName) {
            warn("The \u201Cdetails\u201D element is not supported properly by browsers yet. It would probably be better to wait for implementations.");
        } else if ("track" == localName) {
            warn("The \u201Ctrack\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
        } else if ("bdi" == localName) {
            warn("The \u201Cbdi\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
        } else if ("meter" == localName) {
            warn("The \u201Cmeter\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
        } else if ("style" == localName) {
            if (atts.getIndex("", "scoped") > -1) {
                warn("The \u201Cscoped\u201D attribute on the \u201Cstyle\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        } else if ("input" == localName) {
            String type = atts.getValue("", "type");
            if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("datetime", type)) {
                warn("The \u201Cdatetime\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("date", type)) {
                warn("The \u201Cdate\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("month", type)) {
                warn("The \u201Cmonth\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("week", type)) {
                warn("The \u201Cweek\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("time", type)) {
                warn("The \u201Ctime\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("datetime-local", type)) {
                warn("The \u201Cdatetime-local\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            } else if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString("color", type)) {
                warn("The \u201Ccolor\u201D input type is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        }
    }

}
