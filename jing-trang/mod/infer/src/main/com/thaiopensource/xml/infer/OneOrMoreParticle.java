package com.thaiopensource.xml.infer;

public class OneOrMoreParticle extends Particle {
  private final Particle child;

  public OneOrMoreParticle(Particle child) {
    this.child = child;
  }

  public Particle getChild() {
    return child;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitOneOrMore(this);
  }
}
