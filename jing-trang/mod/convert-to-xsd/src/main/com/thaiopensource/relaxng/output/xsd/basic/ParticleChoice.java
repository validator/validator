package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleChoice extends ParticleGroup {
  public ParticleChoice(SourceLocation location, Annotation annotation, List<Particle> children) {
    super(location, annotation, children);
  }

  public <T> T accept(ParticleVisitor<T> visitor) {
    return visitor.visitChoice(this);
  }
}
