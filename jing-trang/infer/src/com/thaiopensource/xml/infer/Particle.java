package com.thaiopensource.xml.infer;

public abstract class Particle {
  abstract Object accept(ParticleVisitor visitor);
}
