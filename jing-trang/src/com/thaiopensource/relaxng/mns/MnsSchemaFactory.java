package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.SchemaFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

public class MnsSchemaFactory extends SchemaFactory {
  private static final String MNS_SCHEMA = "mns.rng";
  private Schema mnsSchema = null;

  Schema getMnsSchema() throws IOException, SAXException, IncorrectSchemaException {
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

  Schema createChildSchema(InputSource in) throws IOException, SAXException, IncorrectSchemaException {
    return super.createSchema(in);
  }

  public Schema createSchema(InputSource in) throws IOException, SAXException, IncorrectSchemaException {
    return new SchemaImpl(in, this);
  }
}
