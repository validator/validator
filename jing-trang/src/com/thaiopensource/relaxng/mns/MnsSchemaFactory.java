package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.SchemaFactory;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;

public class MnsSchemaFactory extends SchemaFactory {
  private static final String MNS_URI = "http://www.thaiopensource.com/ns/mns";
  static final String BEARER_URI = "http://www.thaiopensoure.com/mns/instance";
  static final String BEARER_LOCAL_NAME = "globalAttributesBearer";
  private static final String BEARER_PREFIX = "m";
  private static final String MNS_SCHEMA = "mns.rng";
  private Schema mnsSchema = null;

  private static class NamespaceInfo {
    String elementSchemaUri;
    String attributesSchemaUri;
  }

  private class Handler extends DelegatingContentHandler {
    private final Map namespaceMap = new HashMap();
    private final ValidatorHandler validator;

    Handler(ValidatorHandler validator) {
      super(validator);
      this.validator = validator;
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

    Schema createSchema() throws IncorrectSchemaException, IOException, SAXException {
      if (!validator.isValidSoFar())
        throw new IncorrectSchemaException();
      load();
      return new SchemaImpl(namespaceMap);
    }

    private void load() throws IncorrectSchemaException, IOException, SAXException {
      for (Iterator iter = namespaceMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        NamespaceInfo nsi = (NamespaceInfo)entry.getValue();
        NamespaceSchemaInfo nssi = new NamespaceSchemaInfo();
        if (nsi.elementSchemaUri != null)
          nssi.elementSchema = MnsSchemaFactory.super.createSchema(new InputSource(nsi.elementSchemaUri));
        if (nsi.attributesSchemaUri != null)
          nssi.attributesSchema = MnsSchemaFactory.super.createSchema(wrapAttributesSchema(nsi.attributesSchemaUri));
        entry.setValue(nssi);
      }
    }

  }

  private Schema getMnsSchema() throws IOException, SAXException, IncorrectSchemaException {
    if (mnsSchema == null) {
      SchemaFactory factory = new SchemaFactory();
      factory.setErrorHandler(getErrorHandler());
      factory.setXMLReaderCreator(getXMLReaderCreator());
      factory.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
      String className = MnsSchemaFactory.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + MNS_SCHEMA;
      URL mnsSchemaUrl = MnsSchemaFactory.class.getClassLoader().getResource(resourceName);
      mnsSchema = factory.createSchema(new InputSource(mnsSchemaUrl.toString()));
    }
    return mnsSchema;
  }

  public Schema createSchema(InputSource in) throws IOException, SAXException, IncorrectSchemaException {
    XMLReader xr = getXMLReaderCreator().createXMLReader();
    Handler h = new Handler(getMnsSchema().createValidator(getErrorHandler()));
    xr.setContentHandler(h);
    xr.setErrorHandler(getErrorHandler());
    xr.parse(in);
    return h.createSchema();
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
