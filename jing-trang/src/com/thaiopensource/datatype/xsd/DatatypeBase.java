package com.thaiopensource.datatype.xsd;

import java.util.StringTokenizer;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

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

  public boolean isValid(String str, ValidationContext vc) {
    str = normalizeWhiteSpace(str);
    return lexicallyAllows(str) && allowsValue(str, vc);
  }

  public void checkValid(String str, ValidationContext vc) throws DatatypeException {
    if (!isValid(str, vc))
      throw new DatatypeException();
  }

  public Object createValue(String str, ValidationContext vc) {
    str = normalizeWhiteSpace(str);
    if (!lexicallyAllows(str))
      return null;
    return getValue(str, vc);
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
     getValue(str, vc) != null. */
  boolean allowsValue(String str, ValidationContext vc) {
    return true;
  }

  /* Requires lexicallyAllows to be true. Returns null if value does not satisfy
     constraints on value space. */
  abstract Object getValue(String str, ValidationContext vc);
  
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

  public boolean isContextDependent() {
    return false;
  }

  public int getIdType() {
    return ID_TYPE_NULL;
  }

  public int valueHashCode(Object value) {
    return value.hashCode();
  }

  public boolean sameValue(Object value1, Object value2) {
    return value1.equals(value2);
  }

  public DatatypeStreamingValidator createStreamingValidator(ValidationContext vc) {
    return new StreamingValidatorImpl(this, vc);
  }
}
