package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class KeyPattern extends StringPattern {
  private Datatype dt;
  private String name;
  private Pattern p;

  KeyPattern(Datatype dt, String name, Locator locator, Pattern p) {
    super(false,
	  combineHashCode(KEY_HASH_CODE, name.hashCode(), p.hashCode()),
	  locator);
    this.p = p;
    this.dt = dt;
    this.name = name;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    Pattern r = p.residual(b, a);
    if (r.isNullable())
      a.setKey(dt, name);
    return r;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof KeyPattern))
      return false;
    KeyPattern k = (KeyPattern)other;
    return name.equals(k.name) && p == k.p;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitKey(name, p);
  }
}
