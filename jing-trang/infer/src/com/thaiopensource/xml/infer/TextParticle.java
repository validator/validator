package com.thaiopensource.xml.infer;

public class TextParticle extends Particle {
  Object accept(ParticleVisitor visitor) {
    return visitor.visitText(this);
  }
}
