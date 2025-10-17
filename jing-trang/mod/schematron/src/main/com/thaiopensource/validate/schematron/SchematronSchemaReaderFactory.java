package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaReaderFactory;
import com.thaiopensource.validate.prop.schematron.SchematronProperty;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;

public class SchematronSchemaReaderFactory implements SchemaReaderFactory, TransformerFactoryInitializer {
  public SchemaReader createSchemaReader(String namespaceUri) {
    if (namespaceUri.equals(SchemaReaderImpl.SCHEMATRON_URI)) {
      try {
        return new SchemaReaderImpl(newTransformerFactory(), this);
      }
      catch (TransformerFactoryConfigurationError e) { }
      catch (IncorrectSchemaException e) { }
      catch (TransformerConfigurationException e) { }
    }
    return null;
  }

  public Option getOption(String uri) {
    return SchematronProperty.getOption(uri);
  }

  public SAXTransformerFactory newTransformerFactory() {
    TransformerFactory factory = TransformerFactory.newInstance();
    if (factory.getFeature(SAXTransformerFactory.FEATURE))
      return (SAXTransformerFactory)factory;
    throw new TransformerFactoryConfigurationError("JAXP TransformerFactory must support SAXTransformerFactory feature");
  }

  public void initTransformerFactory(TransformerFactory factory) {
  }
}
