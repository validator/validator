package com.thaiopensource.validate.picl;

interface ValueHandler {
  void characters(ErrorContext ec, char[] buf, int start, int len);
  void tag(ErrorContext ec);
  void valueComplete(ErrorContext ec);
}
