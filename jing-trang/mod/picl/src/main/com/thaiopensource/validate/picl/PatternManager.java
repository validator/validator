package com.thaiopensource.validate.picl;

interface PatternManager {
  void registerPattern(Pattern pattern, SelectionHandler handler);
  void registerValueHandler(ValueHandler handler);
}
