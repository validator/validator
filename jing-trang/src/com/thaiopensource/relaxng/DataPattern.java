package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

class DataPattern extends StringPattern {
  private Datatype dt;

  DataPattern(boolean nullable, Datatype dt) {
    super(nullable, combineHashCode(DATA_HASH_CODE, dt.hashCode()));
    this.dt = dt;
  }

  DataPattern(Datatype dt) {
    this(!dt.isContextDependent() && dt.isValid("", null), dt);
  }

  boolean matches(PatternBuilder b, Atom a) {
    return a.matchesDatatype(dt);
  }

  boolean samePattern(Pattern other) {
    if (other.getClass() != this.getClass())
      return false;
    return dt.equals(((DataPattern)other).dt);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitData(dt);
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
