package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

import org.xml.sax.Locator;

class DataExceptPattern extends DataPattern {
  private Pattern except;
  private Locator loc;

  DataExceptPattern(Datatype dt, Pattern except, Locator loc) {
    super(dt);
    this.except = except;
    this.loc = loc;
  }

  boolean samePattern(Pattern other) {
    if (!super.samePattern(other))
      return false;
    return except.samePattern(((DataExceptPattern)other).except);
  }

  boolean matches(PatternBuilder b, Atom a) {
    if (!super.matches(b, a))
      return false;
    return !except.residual(b, a).isNullable();
  }

  void accept(PatternVisitor visitor) {
    visitor.visitDataExcept(getDatatype(), except);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    super.checkRestrictions(context, dad, alpha);
    try {
      except.checkRestrictions(DATA_EXCEPT_CONTEXT, null, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(loc);
      throw e;
    }
  }
}
