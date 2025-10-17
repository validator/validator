package com.thaiopensource.xml.infer;

public class ChoiceParticle extends BinaryParticle {

  public ChoiceParticle(Particle p1, Particle p2) {
    super(p1, p2);
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitChoice(this);
  }
}
