package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;

class DatatypePattern extends StringPattern {
  private Datatype dt;

  DatatypePattern(Datatype dt) {
    super(false, combineHashCode(DATA_HASH_CODE, dt.hashCode()));
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

  Datatype getDatatype() {
    return dt;
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}
