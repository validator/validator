package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

class ListDatatype extends DatatypeBase implements Measure, AssignmentClass {
  private DatatypeBase itemType;
  
  ListDatatype(DatatypeBase itemType) {
    this.itemType = itemType;
  }

  static class List {
    private Object[] items;
    List(Object[] items) {
      this.items = items;
    }
    int getLength() {
      return items.length;
    }
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof List))
	return false;
      List other = (List)obj;
      if (items.length != other.items.length)
	return false;
      for (int i = 0; i < items.length; i++)
	if (!items[i].equals(other.items[i]))
	  return false;
      return true;
    }
  }

  Object getValue(String str, DatatypeContext dc) {
    String[] tokens = split(str);
    Object[] items = new Object[tokens.length];
    for (int i = 0; i < items.length; i++) {
      items[i] = itemType.getValue(tokens[i], dc);
      if (items[i] == null)
	return null;
    }
    return new List(items);
  }

  boolean lexicallyAllows(String str) {
    String[] tokens = split(str);
    for (int i = 0; i < tokens.length; i++)
      if (!itemType.lexicallyAllows(tokens[i]))
	return false;
    return true;
  }

  boolean allowsValue(String str, DatatypeContext dc) {
    String[] tokens = split(str);
    for (int i = 0; i < tokens.length; i++)
      if (!itemType.allowsValue(tokens[i], dc))
	return false;
    return true;
  }

  static private String[] split(String s) {
    int len = s.length();
    if (len == 0)
      return new String[0];
    int nTokens = 1;
    for (int i = 0; i < len; i++)
      if (s.charAt(i) == ' ')
	nTokens++;
    String[] tokens = new String[nTokens];
    int tokenStart = 0;
    nTokens = 0;
    for (int i = 0; i < len; i++)
      if (s.charAt(i) == ' ') {
	tokens[nTokens++] = s.substring(tokenStart, i);
	tokenStart = i + 1;
      }
    tokens[nTokens++] = s.substring(tokenStart);
    return tokens;
  }

  Measure getMeasure() {
    return this;
  }

  public int getLength(Object obj) {
    return ((List)obj).getLength();
  }

  public Object getAssignmentClass() {
    Object obj = itemType.getAssignmentClass();
    if (obj == null)
      return null;
    return this;
  }

  public void assign(DatatypeAssignmentImpl a,
		     String value,
		     DatatypeContext dc,
		     Locator loc) throws SAXException {
    String[] tokens = split(normalizeWhiteSpace(value));
    Object cls = itemType.getAssignmentClass();
    for (int i = 0; i < tokens.length; i++)
      a.assign(tokens[i], cls, dc, loc);
  }

}
