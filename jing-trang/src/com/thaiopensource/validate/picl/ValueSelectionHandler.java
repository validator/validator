package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

class ValueSelectionHandler implements SelectionHandler {
  private final SelectedValueHandler handler;

  ValueSelectionHandler(SelectedValueHandler handler) {
    this.handler = handler;
  }

  public void selectAttribute(ErrorContext ec, Path path, String value) {
    handler.select(ec, null, value, value);
  }

  static class ValueHandlerImpl implements ValueHandler {
    private final StringBuffer buf = new StringBuffer();
    private final Locator locator;
    private final SelectedValueHandler handler;

    ValueHandlerImpl(SelectedValueHandler handler, Locator locator) {
      this.handler = handler;
      this.locator = locator;
    }

    public void characters(ErrorContext ec, char[] chars, int start, int len) {
      buf.append(chars, start, len);
    }

    public void tag(ErrorContext ec) {
    }

    public void valueComplete(ErrorContext ec) {
      String value = buf.toString();
      handler.select(ec, locator, value, value);
    }
  }

  public void selectElement(ErrorContext ec, Path path, PatternManager pm) {
    pm.registerValueHandler(new ValueHandlerImpl(handler, ec.saveLocator()));
  }

  public void selectComplete(ErrorContext ec) {
    handler.selectComplete(ec);
  }

 }
