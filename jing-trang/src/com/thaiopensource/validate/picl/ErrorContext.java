package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

interface ErrorContext {
  void error(String key);
  void error(String key, String arg);
  void error(String key, Locator locator);
  void error(String key, String arg, Locator locator);
  Locator saveLocator();
}
