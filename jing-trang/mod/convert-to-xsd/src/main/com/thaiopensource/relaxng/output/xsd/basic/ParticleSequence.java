package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleSequence extends ParticleGroup {
  public ParticleSequence(SourceLocation location, Annotation annotation, List<Particle> children) {
    super(location, annotation, children);
  }

  public <T> T accept(ParticleVisitor<T> visitor) {
    return visitor.visitSequence(this);
  }
}
