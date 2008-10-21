package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public abstract class ParticleGroup extends Particle {
  private final List<Particle> children;

  public ParticleGroup(SourceLocation location, Annotation annotation, List<Particle> children) {
    super(location, annotation);
    this.children = Collections.unmodifiableList(children);
  }

  public List<Particle> getChildren() {
    return children;
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && ((ParticleGroup)obj).children.equals(children);
  }

  public int hashCode() {
    return super.hashCode() ^ getChildren().hashCode();
  }
}
