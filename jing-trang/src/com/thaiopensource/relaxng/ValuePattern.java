package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class ValuePattern extends SimplePattern {
  String str;
  Datatype dt;

  ValuePattern(Datatype dt, String str, Locator locator) {
    super(combineHashCode(VALUE_HASH_CODE, str.hashCode()), locator);
    this.str = str;
  }

  boolean matches(Atom a) {
    return a.matchesDatatypeValue(dt, str);
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    return (dt.equals(((ValuePattern)other).dt)
	    && str.equals(((ValuePattern)other).str));
  }

  void accept(PatternVisitor visitor) {
    visitor.visitValue(dt, str);
  }
}
