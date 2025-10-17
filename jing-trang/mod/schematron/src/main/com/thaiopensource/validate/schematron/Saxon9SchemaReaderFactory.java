package com.thaiopensource.validate.schematron;

import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.sax.SAXTransformerFactory;

public class Saxon9SchemaReaderFactory extends SchematronSchemaReaderFactory {
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }
}
