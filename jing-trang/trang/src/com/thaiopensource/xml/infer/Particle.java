package com.thaiopensource.xml.infer;

public abstract class Particle {
  public abstract Object accept(ParticleVisitor visitor);
}
