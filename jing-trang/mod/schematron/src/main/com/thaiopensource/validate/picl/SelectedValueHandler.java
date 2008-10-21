package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

abstract class SelectedValueHandler {
  /**
    * If locator is non-null, it is a non-volatile location to be used for errors.
    * If locator is null, then the ErrorContext's location should be used.
    */
   abstract void select(ErrorContext ec, Locator locator, Object value, String representation);
   void selectComplete(ErrorContext ec) { }
}
