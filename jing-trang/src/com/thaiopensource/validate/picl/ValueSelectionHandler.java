package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

abstract class ValueSelectionHandler implements SelectionHandler {
  public void selectAttribute(ErrorContext ec, Path path, String value) {
    select(ec, null, value);
  }

  class ValueHandlerImpl implements ValueHandler {
    private final StringBuffer buf = new StringBuffer();
    private final Locator locator;

    ValueHandlerImpl(Locator locator) {
      this.locator = locator;
    }

    public void characters(ErrorContext ec, char[] chars, int start, int len) {
      buf.append(chars, start, len);
    }

    public void tag(ErrorContext ec) {
    }

    public void valueComplete(ErrorContext ec) {
      select(ec, locator, buf.toString());
    }
  }

  public void selectElement(ErrorContext ec, Path path, PatternManager pm) {
    pm.registerValueHandler(new ValueHandlerImpl(ec.saveLocator()));
  }

  public void selectComplete(ErrorContext ec) {
  }

  /**
   * If locator is non-null, it is a non-volatile location to be used for errors.
   * If locator is null, then the ErrorContext's location should be used.
   */
  abstract void select(ErrorContext ec, Locator locator, String value);
}
