package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.SchemaReaderFactory;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;

import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerConfigurationException;

import com.icl.saxon.TransformerFactoryImpl;

public class SchematronSchemaReaderFactory implements SchemaReaderFactory {
  // XXX need property to set phase
  // XXX need flag for diagnostics
  public SchemaReader createSchemaReader(String namespaceUri) {
    if (namespaceUri.equals(SchemaReaderImpl.SCHEMATRON_URI)) {
      try {
        return new SchemaReaderImpl(new TransformerFactoryImpl());
      }
      catch (TransformerFactoryConfigurationError e) { }
      catch (IncorrectSchemaException e) { }
      catch (TransformerConfigurationException e) { }
    }
    return null;
  }
}
