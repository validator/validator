package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import com.thaiopensource.datatype.Datatype;

class KeyPattern extends StringPattern {
  private Datatype dt;
  private String name;
  private Pattern p;

  KeyPattern(String name, Locator locator, Pattern p) {
    super(false,
	  combineHashCode(KEY_HASH_CODE, name.hashCode(), p.hashCode()),
	  locator);
    this.p = p;
    this.name = name;
    this.dt = p.getDatatype();
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeKey(name, getLocator(), ep);
    else
      return this;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    Pattern r = p.residual(b, a);
    if (r.isNullable() && dt != null)
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
