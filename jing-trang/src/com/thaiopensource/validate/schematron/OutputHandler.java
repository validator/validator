package com.thaiopensource.validate.schematron;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

class OutputHandler extends DefaultHandler {
  private final ErrorHandler eh;
  private int lineNumber = -1;
  private String systemId = null;
  private StringBuffer message = new StringBuffer();
  private boolean inMessage = false;

  OutputHandler(ErrorHandler eh) {
    this.eh = eh;
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    if (inMessage)
      message.append(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    characters(ch, start, length);
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (localName.equals("failed-assertion")
        || localName.equals("report")) {
      String value = attributes.getValue("", "line-number");
      if (value == null)
        lineNumber = -1;
      else {
        try {
          lineNumber = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
          lineNumber = -1;
        }
      }
      value = attributes.getValue("", "system-id");
      if (value != null && value.equals(""))
        value = null;
      systemId = value;
    }
    else if (localName.equals("statement"))
      inMessage = true;
    else if (localName.equals("diagnostic")) {
      inMessage = true;
      message.append("\n  ");
    }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (localName.equals("statement") || localName.equals("diagnostic"))
      inMessage = false;
    else if (localName.equals("failed-assertion")
             || localName.equals("report")) {
      eh.error(new SAXParseException(message.toString(), null, systemId, lineNumber, -1));
      message.setLength(0);
    }
  }
}
