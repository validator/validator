package com.thaiopensource.util;

import java.net.URL;
import java.net.MalformedURLException;

public class Uri {

  public static boolean isValid(String s) {
    return isValidPercent(s) && isValidFragment(s) && isValidScheme(s);
  }

  private static boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  private static boolean isHexDigit(char c) {
    return ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || isDigit(c);
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
  
  private static boolean isSchemeChar(char c) {
    return isAlpha(c) || isDigit(c) || c == '+' || c == '-' || c =='.';
  }

  private static boolean isValidPercent(String s) {
    int len = s.length();
    for (int i = 0; i < len; i++)
      if (s.charAt(i) == '%') {
	if (i + 2 >= len)
	  return false;
	else if (!isHexDigit(s.charAt(i + 1))
		 || !isHexDigit(s.charAt(i + 2)))
	  return false;
      }
    return true;
  }

  private static boolean isValidFragment(String s) {
    int i = s.indexOf('#');
    return i < 0 || s.indexOf('#', i + 1) < 0;
  }

  private static boolean isValidScheme(String s) {
    if (!isAbsolute(s))
      return true;
    int i = s.indexOf(':');
    if (i == 0
	|| i + 1 == s.length()
	|| !isAlpha(s.charAt(0)))
      return false;
    while (--i > 0)
      if (!isSchemeChar(s.charAt(i)))
	return false;
    return true;
  }

  public static String resolve(String baseUri, String uriReference) {
    if (!isAbsolute(uriReference) && baseUri != null && isAbsolute(baseUri)) {
      try {
	return new URL(new URL(baseUri), uriReference).toString();
      }
      catch (MalformedURLException e) { }
    }
    return uriReference;
  }

  public static boolean hasFragmentId(String uri) {
    return uri.indexOf('#') >= 0;
  }

  public static boolean isAbsolute(String uri) {
    int i = uri.indexOf(':');
    if (i < 0)
      return false;
    while (--i >= 0) {
      switch (uri.charAt(i)) {
      case '#':
      case '/':
      case '?':
	return false;
      }
    }
    return true;
  }
}
