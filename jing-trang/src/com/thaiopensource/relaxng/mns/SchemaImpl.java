package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SchemaImpl implements Schema {
  static final String BEARER_URI = "http://www.thaiopensoure.com/mns/instance";
  static final String BEARER_LOCAL_NAME = "globalAttributesBearer";
  private static final String MNS_URI = "http://www.thaiopensource.com/ns/mns";
  private static final String BEARER_PREFIX = "m";
  private final Map namespaceMap = new HashMap();

  private static class NamespaceInfo {
    String elementSchemaUri;
    String attributesSchemaUri;
  }

  private static class NamespaceSchemaInfo {
    Schema elementSchema;
    Schema attributesSchema;
  }

  private class Handler extends DelegatingContentHandler {
    private final ValidatorHandler validator;

    Handler(ValidatorHandler validator) {
      super(validator);
      this.validator = validator;
    }

    void checkValid() throws IncorrectSchemaException {
      if (!validator.isValidSoFar())
        throw new IncorrectSchemaException();
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      if (!validator.isValidSoFar() || !MNS_URI.equals(uri))
        return;
      boolean isAttribute;
      if ("validateElement".equals(localName))
        isAttribute = false;
      else if ("validateAttributes".equals(localName))
        isAttribute = true;
      else
        return;
      String ns = attributes.getValue("", "ns");
      String schema = attributes.getValue("", "schema");
      NamespaceInfo nsi = (NamespaceInfo)namespaceMap.get(ns);
      if (nsi == null) {
        nsi = new NamespaceInfo();
        namespaceMap.put(ns, nsi);
      }
      if (isAttribute)
        nsi.attributesSchemaUri = schema;
      else
        nsi.elementSchemaUri = schema;
    }
  }

  SchemaImpl(InputSource in, MnsSchemaFactory factory)
          throws IOException, SAXException, IncorrectSchemaException {
    XMLReader xr = factory.getXMLReaderCreator().createXMLReader();
    ErrorHandler eh = factory.getErrorHandler();
    Handler h = new Handler(factory.getMnsSchema().createValidator(eh));
    xr.setContentHandler(h);
    xr.setErrorHandler(eh);
    xr.parse(in);
    h.checkValid();
    load(factory);
  }

  private void load(MnsSchemaFactory factory) throws IncorrectSchemaException, IOException, SAXException {
    for (Iterator iter = namespaceMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      NamespaceInfo nsi = (NamespaceInfo)entry.getValue();
      NamespaceSchemaInfo nssi = new NamespaceSchemaInfo();
      if (nsi.elementSchemaUri != null)
        nssi.elementSchema = factory.createChildSchema(new InputSource(nsi.elementSchemaUri));
      if (nsi.attributesSchemaUri != null)
        nssi.attributesSchema = factory.createChildSchema(wrapAttributesSchema(nsi.attributesSchemaUri));
      entry.setValue(nssi);
    }
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new ValidatorHandlerImpl(this, eh);
  }

  public ValidatorHandler createValidator() {
    return createValidator(null);
  }

  Schema getElementSchema(String ns) {
    NamespaceSchemaInfo nsi = (NamespaceSchemaInfo)namespaceMap.get(ns);
    if (nsi == null)
      return null;
    return nsi.elementSchema;
  }

  Schema getAttributesSchema(String ns) {
    NamespaceSchemaInfo nsi = (NamespaceSchemaInfo)namespaceMap.get(ns);
    if (nsi == null)
      return null;
    return nsi.attributesSchema;
  }

  private static InputSource wrapAttributesSchema(String attributesSchemaUri) {
    StringBuffer buf = new StringBuffer();
    buf.append("<element name=\"");
    buf.append(BEARER_PREFIX);
    buf.append(':');
    buf.append(BEARER_LOCAL_NAME);
    buf.append('"');
    buf.append(" xmlns=\"");
    buf.append(WellKnownNamespaces.RELAX_NG);
    buf.append('"');
    buf.append(" xmlns:");
    buf.append(BEARER_PREFIX);
    buf.append("=\"");
    buf.append(BEARER_URI);
    buf.append("\"><externalRef href=\"");
    buf.append(attributesSchemaUri);
    buf.append("\"/></element>");
    return new InputSource(new StringReader(buf.toString()));
  }
}
