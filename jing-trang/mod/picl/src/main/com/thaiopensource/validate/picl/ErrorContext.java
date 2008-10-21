package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

interface ErrorContext {
  /**
   * If locator is null, use this object's locator.
   */
  void error(Locator locator, String key);
  /**
   * If locator is null, use this object's locator.
   */
  void error(Locator locator, String key, String arg);
  /**
   * Returns non-volatile Locator, never-null.
   */
  Locator saveLocator();
}
