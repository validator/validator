package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

import org.xml.sax.SAXException;

class StringAtom extends Atom {
  private String str;
  private DatatypeContext dc;

  static class Key {
    String key;
    String keyRef;
    Object value;

    Key(String key, String keyRef, Object value) {
      this.key = key;
      this.keyRef = keyRef;
      this.value = value;
    }
  }

  private Key[] keys;

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
    Pattern r = p.residual(b, sa);
    if (sa.keys != null) {
      if (sa.keys.length != 1)
	throw new Error("more than one key for a token");
      if (keys == null)
	keys = sa.keys;
      else {
	Key[] newKeys = new Key[keys.length + 1];
	System.arraycopy(keys, 0, newKeys, 0, keys.length);
	newKeys[keys.length] = sa.keys[0];
	keys = newKeys;
      }
    }
    return r;
  }

  boolean matchesDatatype(Datatype dt) {
    return dt.allows(str, dc);
  }

  void setKey(Datatype dt, String name) {
    keys = new Key[] { new Key(name, null, dt.createValue(str, dc)) };
  }

  void setKeyRef(Datatype dt, String name) {
    keys = new Key[] { new Key(null, name, dt.createValue(str, dc)) };
  }

  void checkKeys(KeyChecker kc) throws SAXException {
    if (keys == null)
      return;
    for (int i = 0; i < keys.length; i++) {
      if (keys[i].key != null)
	kc.checkKey(keys[i].key, keys[i].value);
      if (keys[i].keyRef != null)
	kc.checkKeyRef(keys[i].keyRef, keys[i].value);
    }
  }
}
