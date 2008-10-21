package com.thaiopensource.validate.schematron;

import com.icl.saxon.TransformerFactoryImpl;

import javax.xml.transform.sax.SAXTransformerFactory;

public class SaxonSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }
}
