package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class KeyDataPattern extends SimplePattern {
  private Datatype dt;
  private String key;
  private String keyRef;

  KeyDataPattern(Datatype dt, String key, String keyRef, Locator locator) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()), locator);
    this.dt = dt;
    this.key = key;
    this.keyRef = keyRef;
  }

  boolean matches(Atom a) {
    return a.matchesDatatype(dt);
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof KeyDataPattern))
      return false;
    if (key == null) {
      if (((KeyDataPattern)other).key != null)
	return false;
    }
    else if (!key.equals(((KeyDataPattern)other).key))
      return false;
    if (keyRef == null) {
      if (((KeyDataPattern)other).keyRef != null)
	return false;
    }
    else if (!keyRef.equals(((KeyDataPattern)other).keyRef))
      return false;
    return dt.equals(((KeyDataPattern)other).dt);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitDatatype(dt, key, keyRef);
  }
}
