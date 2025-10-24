package com.thaiopensource.validate.picl;

import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.util.PropertyMap;

import java.net.URL;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

class SchemaReceiverImpl implements SchemaReceiver {
  private final String PICL_SCHEMA = "picl.rnc";
  private Schema piclSchema = null;
  private final PropertyMap properties;

  SchemaReceiverImpl(PropertyMap properties) {
    this.properties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER,
                                            ValidateProperty.ERROR_HANDLER.get(properties));
  }

  public SchemaFuture installHandlers(XMLReader xr) throws SAXException {
    SchemaParser parser = new SchemaParser(properties, getPiclSchema());
    xr.setContentHandler(parser);
    return parser;
  }

  private Schema getPiclSchema() throws SAXException {
    if (piclSchema == null) {
      String className = SchemaReceiverImpl.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + PICL_SCHEMA;
      URL nrlSchemaUrl = getResource(resourceName);
      try {
        piclSchema = CompactSchemaReader.getInstance().createSchema(new InputSource(nrlSchemaUrl.toString()),
                                                                    properties);
      }
      catch (IncorrectSchemaException e) {
        throw new SAXException("unexpected internal error in RNC schema for picl");
      }
      catch (IOException e) {
        throw new SAXException(e);
      }
    }
    return piclSchema;
  }

  private static URL getResource(String resourceName) {
    ClassLoader cl = SchemaReceiverImpl.class.getClassLoader();
    // XXX see if we should borrow 1.2 code from Service
    if (cl == null)
      return ClassLoader.getSystemResource(resourceName);
    else
      return cl.getResource(resourceName);
  }

}
