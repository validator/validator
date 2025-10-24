package com.thaiopensource.validate.picl;

interface SelectionHandler {
  void selectElement(ErrorContext ec, Path path, PatternManager pm);
  void selectAttribute(ErrorContext ec, Path path, String value);
  void selectComplete(ErrorContext ec);
}
