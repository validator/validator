package com.thaiopensource.validate.schematron;

import org.apache.xalan.processor.TransformerFactoryImpl;

import javax.xml.transform.sax.SAXTransformerFactory;

public class XalanSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }
}
