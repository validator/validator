package com.thaiopensource.datatype.xsd;

import java.util.StringTokenizer;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

abstract class DatatypeBase implements Datatype {
  abstract boolean lexicallyAllows(String str);
  private final int whiteSpace;

  static final int WHITE_SPACE_PRESERVE = 0;
  static final int WHITE_SPACE_REPLACE = 1;
  static final int WHITE_SPACE_COLLAPSE = 2;

  DatatypeBase() {
    whiteSpace = WHITE_SPACE_COLLAPSE;
  }

  DatatypeBase(int whiteSpace) {
    this.whiteSpace = whiteSpace;
  }

  int getWhiteSpace() {
    return whiteSpace;
  }

  public boolean allows(String str, DatatypeContext dc) {
    str = normalizeWhiteSpace(str);
    return lexicallyAllows(str) && allowsValue(str, dc);
  }

  final String normalizeWhiteSpace(String str) {
    switch (whiteSpace) {
    case WHITE_SPACE_COLLAPSE:
      return collapseWhiteSpace(str);
    case WHITE_SPACE_REPLACE:
      return replaceWhiteSpace(str);
    }
    return str;
  }
    
  /* Requires lexicallyAllows to be true.  Must return same value as
     getValue(str, dc) != null. */
  boolean allowsValue(String str, DatatypeContext dc) {
    return true;
  }

  /* Requires lexicallyAllows to be true. Returns null if value does not satisfy
     constraints on value space. */
  abstract Object getValue(String str, DatatypeContext dc);
  
  OrderRelation getOrderRelation() {
    return null;
  }

  /* For datatypes that have a length. */
  Measure getMeasure() {
    return null;
  }

  static final String collapseWhiteSpace(String s) {
    StringBuffer buf = new StringBuffer();
    for (StringTokenizer e = new StringTokenizer(s); e.hasMoreElements();) {
      if (buf.length() > 0)
	buf.append(' ');
      buf.append((String)e.nextElement());
    }
    return buf.toString();
  }

  static final String replaceWhiteSpace(String s) {
    int len = s.length();
    for (int i = 0; i < len; i++)
      switch (s.charAt(i)) {
      case '\r':
      case '\n':
      case '\t':
	{
	  char[] buf = s.toCharArray();
	  buf[i] = ' ';
	  for (++i; i < len; i++)
	    switch (buf[i]) {
	    case '\r':
	    case '\n':
	    case '\t':
	      buf[i] = ' ';
	    }
	  return new String(buf);
	}
      }
    return s;
  }

  DatatypeBase getPrimitive() {
    return this;
  }
}
