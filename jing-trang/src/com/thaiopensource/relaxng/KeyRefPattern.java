package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class KeyRefPattern extends StringPattern {
  private Datatype dt;
  private String name;
  private Pattern p;

  KeyRefPattern(Datatype dt, String name, Locator locator, Pattern p) {
    super(false,
	  combineHashCode(KEY_REF_HASH_CODE, name.hashCode(), p.hashCode()),
	  locator);
    this.p = p;
    this.dt = dt;
    this.name = name;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    Pattern r = p.residual(b, a);
    if (r.isNullable())
      a.setKeyRef(dt, name);
    return r;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof KeyRefPattern))
      return false;
    KeyRefPattern k = (KeyRefPattern)other;
    return name.equals(k.name) && p == k.p;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitKeyRef(name, p);
  }
}
