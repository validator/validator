package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

class ValuePattern extends StringPattern {
  Object obj;
  Datatype dt;

  ValuePattern(Datatype dt, Object obj) {
    super(valueIsNullable(dt, obj),
	  combineHashCode(VALUE_HASH_CODE, obj.hashCode()));
    this.dt = dt;
    this.obj = obj;
  }

  static boolean valueIsNullable(Datatype dt, Object obj) {
    return (!dt.isContextDependent()
	    && dt.sameValue(obj, dt.createValue("", null)));
  }

  boolean matches(PatternBuilder b, Atom a) {
    return a.matchesDatatypeValue(dt, obj);
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    if (!(other instanceof ValuePattern))
      return false;
    return (dt.equals(((ValuePattern)other).dt)
	    && dt.sameValue(obj, ((ValuePattern)other).obj));
  }

  void accept(PatternVisitor visitor) {
    visitor.visitValue(dt, obj);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_value");
    }
  }

}
