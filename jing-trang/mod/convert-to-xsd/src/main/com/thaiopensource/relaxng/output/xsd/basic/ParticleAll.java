package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleAll extends ParticleGroup {
  public ParticleAll(SourceLocation location, Annotation annotation, List<Particle> children) {
    super(location, annotation, children);
  }

  public <T> T accept(ParticleVisitor<T> visitor) {
    return visitor.visitAll(this);
  }
}
