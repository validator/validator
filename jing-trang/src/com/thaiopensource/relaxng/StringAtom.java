package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

import org.xml.sax.SAXException;

class StringAtom extends Atom {
  private String str;
  private DatatypeContext dc;

  StringAtom(String str, DatatypeContext dc) {
    this.str = str;
    this.dc = dc;
  }
 
  boolean matchesString() {
    return true;
  }

  boolean matchesDatatypeValue(Datatype dt, Object obj) {
    return obj.equals(dt.createValue(str, dc));
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
    StringAtom sa = new StringAtom(str.substring(i, j), dc);
    return p.residual(b, sa);
  }

  boolean matchesDatatype(Datatype dt) {
    return dt.allows(str, dc);
  }

}
