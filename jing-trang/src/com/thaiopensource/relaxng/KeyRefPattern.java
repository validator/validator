package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import com.thaiopensource.datatype.Datatype;

class KeyRefPattern extends StringPattern {
  private Datatype dt;
  private String name;
  private Pattern p;

  KeyRefPattern(String name, Locator locator, Pattern p) {
    super(false,
	  combineHashCode(KEY_REF_HASH_CODE, name.hashCode(), p.hashCode()),
	  locator);
    this.p = p;
    this.name = name;
    this.dt = p.getDatatype();
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    Pattern r = p.residual(b, a);
    if (r.isNullable() && dt != null)
      a.setKeyRef(dt, name);
    return r;
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeKeyRef(name, getLocator(), ep);
    else
      return this;
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
