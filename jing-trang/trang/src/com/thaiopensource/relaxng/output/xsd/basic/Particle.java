package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class Particle extends Annotated {
  public Particle(SourceLocation location, Annotation annotation) {
    super(location, annotation);
  }

  public abstract Object accept(ParticleVisitor visitor);
}
