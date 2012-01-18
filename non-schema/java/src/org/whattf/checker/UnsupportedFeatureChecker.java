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
        boolean w3cBranding = "1".equals(System.getProperty("nu.validator.servlet.w3cbranding")) ? true
            : false;
        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }
        if (atts.getIndex("", "contextmenu") > -1) {
            warn("The \u201Ccontextmenu\u201D attribute is not supported by browsers yet. It would probably be better to wait for implementations.");
        }
        if (atts.getIndex("", "dropzone") > -1) {
            warn("The \u201Cdropzone\u201D attribute is not supported by browsers yet. It would probably be better to wait for implementations.");
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
            warn("The \u201Cbdi\u201D element is not supported by browsers yet.");
        } else if ("style" == localName) {
            if (atts.getIndex("", "scoped") > -1) {
                warn("The \u201Cscoped\u201D attribute on the \u201Cstyle\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        } else if ("blockquote" == localName) {
            if (atts.getIndex("", "cite") > -1) {
                warn("The \u201Ccite\u201D attribute on the \u201Cblockquote\u201D element is not supported by browsers yet.");
            }
        } else if ("q" == localName) {
            if (atts.getIndex("", "cite") > -1) {
                warn("The \u201Ccite\u201D attribute on the \u201Cq\u201D element is not supported by browsers yet.");
            }
        } else if ("a" == localName) {
            if (atts.getIndex("", "ping") > -1 && !w3cBranding) {
                warn("The \u201Cping\u201D attribute on the \u201Ca\u201D element is not supported by browsers yet.");
            }
        } else if ("area" == localName) {
            if (atts.getIndex("", "ping") > -1 && !w3cBranding) {
                warn("The \u201Cping\u201D attribute on the \u201Carea\u201D element is not supported by browsers yet.");
            }
        } else if ("video" == localName) {
            if (atts.getIndex("", "crossorigin") > -1) {
                warn("The \u201Ccrossorigin\u201D attribute on the \u201Cvideo\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
            if (atts.getIndex("", "mediagroup") > -1) {
                warn("The \u201Cmediagroup\u201D attribute on the \u201Cvideo\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        } else if ("audio" == localName) {
            if (atts.getIndex("", "crossorigin") > -1) {
                warn("The \u201Ccrossorigin\u201D attribute on the \u201Caudio\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
            if (atts.getIndex("", "mediagroup") > -1) {
                warn("The \u201Cmediagroup\u201D attribute on the \u201Caudio\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        } else if ("img" == localName) {
            if (atts.getIndex("", "crossorigin") > -1) {
                warn("The \u201Ccrossorigin\u201D attribute on the \u201Cimg\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
        } else if ("iframe" == localName) {
            if (atts.getIndex("", "srcdoc") > -1) {
                warn("The \u201Csrcdoc\u201D attribute on the \u201Ciframe\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
            if (atts.getIndex("", "seamless") > -1) {
                warn("The \u201Cseamless\u201D attribute on the \u201Ciframe\u201D element is not supported by browsers yet. It would probably be better to wait for implementations.");
            }
            if (atts.getIndex("", "sandbox") > -1) {
                warn("The \u201Csandbox\u201D attribute on the \u201Ciframe\u201D element is only supported by Chrome so far. Please be sure to test your page in Chrome.");
            }
        } else if ("textarea" == localName) {
            if (atts.getIndex("", "dirname") > -1) {
                warn("The \u201Cdirname\u201D attribute on the \u201Ctextarea\u201D element is not supported by browsers yet.");
            }
        } else if ("input" == localName) {
            if (atts.getIndex("", "dirname") > -1) {
                warn("The \u201Cdirname\u201D attribute on the \u201Cinput\u201D element is not supported by browsers yet.");
            }
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
                warn("The \u201Ccolor\u201D input type is so far supported properly only by Opera. Please be sure to test your page in Opera.");
            }
        }
    }

}
