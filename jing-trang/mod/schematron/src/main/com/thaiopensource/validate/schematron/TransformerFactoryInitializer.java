package com.thaiopensource.validate.schematron;

import javax.xml.transform.TransformerFactory;

public interface TransformerFactoryInitializer {
  void initTransformerFactory(TransformerFactory factory);
}
