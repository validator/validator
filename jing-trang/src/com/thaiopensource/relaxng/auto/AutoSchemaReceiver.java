package com.thaiopensource.relaxng.auto;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.io.IOException;
import java.util.Vector;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.SchemaOptions;

public class AutoSchemaReceiver implements SchemaReceiver {
  private final XMLReaderCreator xrc;
  private final ErrorHandler eh;
  private final SchemaOptions options;
  private final DatatypeLibraryFactory dlf;
  private final SchemaReceiverFactory srf;

  private class Handler extends DefaultHandler implements SchemaFuture {
    private final XMLReader xr;
    private SchemaFuture sf = null;
    private Locator locator = null;
    private final Vector prefixMappings = new Vector();

    private Handler(XMLReader xr) {
      this.xr = xr;
    }

    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    public void startPrefixMapping(String prefix, String uri) {
      prefixMappings.addElement(prefix);
      prefixMappings.addElement(uri);
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      SchemaReceiver sr = srf.createSchemaReceiver(uri, xrc, eh, options, dlf, null);
      if (sr == null)
        // XXX localize properly
        throw new SAXParseException("do not know any schema language with namespace URI \"" + uri + "\"",
                                    locator);
      sf = sr.installHandlers(xr);
      ContentHandler contentHandler = xr.getContentHandler();
      if (contentHandler == null)
        return;
      if (locator != null) {
        contentHandler.setDocumentLocator(locator);
        contentHandler = xr.getContentHandler();
      }
      contentHandler.startDocument();
      contentHandler = xr.getContentHandler();
      for (int i = 0, len = prefixMappings.size(); i < len; i += 2) {
        contentHandler.startPrefixMapping((String)prefixMappings.elementAt(i),
                                          (String)prefixMappings.elementAt(i + 1));
        contentHandler = xr.getContentHandler();
      }
      contentHandler.startElement(uri, localName, qName, attributes);
    }

    public Schema getSchema() throws IncorrectSchemaException, SAXException, IOException {
      if (sf == null)
        throw new IncorrectSchemaException();
      return sf.getSchema();
    }

    public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
      if (sf == null)
        return e;
      return sf.unwrapException(e);
    }
  }

  public AutoSchemaReceiver(XMLReaderCreator xrc, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf, SchemaReceiverFactory srf) {
    this.xrc = xrc;
    this.eh = eh;
    this.options = options;
    this.dlf = dlf;
    this.srf = srf;
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    Handler h = new Handler(xr);
    xr.setContentHandler(h);
    if (eh != null)
      xr.setErrorHandler(eh);
    return h;
  }
}
