package com.thaiopensource.validate.picl;

class MultiConstraint implements Constraint {
  private final Constraint[] constraints;

  MultiConstraint(Constraint[] constraints) {
    this.constraints = constraints;
  }

  public void activate(PatternManager pm) {
    for (int i = 0; i < constraints.length; i++)
      constraints[i].activate(pm);
  }
}
