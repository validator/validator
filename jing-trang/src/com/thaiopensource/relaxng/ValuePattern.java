package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import com.thaiopensource.datatype.Datatype;

class ValuePattern extends SimplePattern {
  Object obj;
  Datatype dt;

  ValuePattern(Datatype dt, Object obj, Locator locator) {
    super(combineHashCode(VALUE_HASH_CODE, obj.hashCode()), locator);
    this.dt = dt;
    this.obj = obj;
  }

  boolean matches(Atom a) {
    return a.matchesDatatypeValue(dt, obj);
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    if (!(other instanceof ValuePattern))
      return false;
    return (dt.equals(((ValuePattern)other).dt)
	    && obj.equals(((ValuePattern)other).obj));
  }

  void accept(PatternVisitor visitor) {
    visitor.visitValue(dt, obj);
  }

  Datatype getDatatype() {
    return dt;
  }
}
