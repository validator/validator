package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

import org.xml.sax.SAXException;

class StringAtom extends Atom {
  private String str;
  private ValidationContext vc;

  StringAtom(String str, ValidationContext vc) {
    this.str = str;
    this.vc = vc;
  }

  boolean isBlank() {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	break;
      default:
	return false;
      }
    }
    return true;
  }

  boolean matchesString() {
    return true;
  }

  boolean matchesDatatypeValue(Datatype dt, Object obj) {
    Object strValue = dt.createValue(str, vc);
    if (strValue == null)
      return false;
    return dt.sameValue(obj, strValue);
  }

  boolean matchesList(PatternBuilder b, Pattern p) {
    int len = str.length();
    int tokenStart = -1;
    Pattern r = p;
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	if (tokenStart >= 0) {
	  r = matchToken(b, r, tokenStart, i);
	  tokenStart = -1;
	}
	break;
      default:
	if (tokenStart < 0)
	  tokenStart = i;
	break;
      }
    }
    if (tokenStart >= 0)
      r = matchToken(b, r, tokenStart, len);
    return r.isNullable();
  }

  private Pattern matchToken(PatternBuilder b, Pattern p, int i, int j) {
    StringAtom sa = new StringAtom(str.substring(i, j), vc);
    return p.residual(b, sa);
  }

  boolean matchesDatatype(Datatype dt) {
    return dt.isValid(str, vc);
  }

}
