package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class DatatypePattern extends SimplePattern {
  private Datatype dt;

  DatatypePattern(Datatype dt, Locator locator) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()), locator);
    this.dt = dt;
  }

  boolean matches(Atom a) {
    return a.matchesDatatype(dt);
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof DatatypePattern))
      return false;
    return dt.equals(((DatatypePattern)other).dt);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitDatatype(dt);
  }
}
