package com.thaiopensource.relaxng;

class UriChecker {
  static final int RELATIVE = 01;
  static final int FRAGMENT = 02;

  static class InvalidUriException extends Exception { }
    
  static int checkUri(String s) throws InvalidUriException {
    checkPercent(s);
    int fragmentFlag = 0;
    int i = s.indexOf('#');
    if (i >= 0) {
      String fi = s.substring(i + 1);
      if (fi.indexOf('#') >= 0)
	throw new InvalidUriException();
      s = s.substring(0, i);
      fragmentFlag = FRAGMENT;
    }
    int questionIndex = s.indexOf('?');
    int colonIndex = s.indexOf(':');
    int slashIndex = s.indexOf('/');
    if (colonIndex < 0
	|| (questionIndex >= 0 && colonIndex > questionIndex)
	|| (slashIndex >= 0 && colonIndex > slashIndex))
      return fragmentFlag|RELATIVE;
    checkSchemeName(s.substring(0, colonIndex));
    // Cannot have "foo:"
    if (colonIndex + 1 == s.length())
      throw new InvalidUriException();
    return fragmentFlag;
  }

  static private boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  static private boolean isHexDigit(char c) {
    return ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || isDigit(c);
  }

  static private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
  
  static private boolean isSchemeChar(char c) {
    return isAlpha(c) || isDigit(c) || c == '+' || c == '-' || c =='.';
  }

  static private void checkPercent(String s) throws InvalidUriException {
    for (int i = 0; i < s.length(); i++)
      if (s.charAt(i) == '%') {
	if (i + 2 >= s.length())
	  throw new InvalidUriException();
	else if (!isHexDigit(s.charAt(i + 1))
		 || !isHexDigit(s.charAt(i + 2)))
	  throw new InvalidUriException();
      }
  }

  static private void checkSchemeName(String s) throws InvalidUriException {
    if (s.length() == 0)
      throw new InvalidUriException();
    if (!isAlpha(s.charAt(0)))
      throw new InvalidUriException();
    for (int i = 1; i < s.length(); i++)
      if (!isSchemeChar(s.charAt(i)))
	throw new InvalidUriException();
  }
}
