package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class GroupRef extends Particle {
  private final String name;

  public GroupRef(SourceLocation location, Annotation annotation, String name) {
    super(location, annotation);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitGroupRef(this);
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && name.equals(((GroupRef)obj).name);
  }

  public int hashCode() {
    return super.hashCode() ^ name.hashCode();
  }
}
