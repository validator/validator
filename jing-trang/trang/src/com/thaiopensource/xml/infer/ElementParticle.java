package com.thaiopensource.xml.infer;

import com.thaiopensource.xml.util.Name;

public class ElementParticle extends Particle {
  private final Name name;

  public ElementParticle(Name name) {
    this.name = name;
  }

  public Name getName() {
    return name;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitElement(this);
  }

}
