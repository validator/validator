package com.thaiopensource.validate.picl;

abstract class ValueSelectionHandler implements SelectionHandler {
  public void selectAttribute(ErrorContext ec, Path path, String value) {
    select(ec, value);
  }

  class ValueHandlerImpl implements ValueHandler {
    private final StringBuffer buf = new StringBuffer();
    public void characters(ErrorContext ec, char[] chars, int start, int len) {
      buf.append(chars, start, len);
    }

    public void tag(ErrorContext ec) {
    }

    public void valueComplete(ErrorContext ec) {
      // XXX better to use location of element
      select(ec, buf.toString());
    }
  }

  public void selectElement(ErrorContext ec, Path path, PatternManager pm) {
    pm.registerValueHandler(new ValueHandlerImpl());
  }

  public void selectComplete(ErrorContext ec) {
  }

  abstract void select(ErrorContext ec, String value);
}
