package com.thaiopensource.xml.infer;

public class SequenceParticle extends BinaryParticle {
  public SequenceParticle(Particle p1, Particle p2) {
    super(p1, p2);
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitSequence(this);
  }
}
