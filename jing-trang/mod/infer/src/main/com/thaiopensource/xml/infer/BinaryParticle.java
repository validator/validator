package com.thaiopensource.xml.infer;

public abstract class BinaryParticle extends Particle {
  private final Particle p1;
  private final Particle p2;

  public BinaryParticle(Particle p1, Particle p2) {
    this.p1 = p1;
    this.p2 = p2;
  }

  public Particle getChild1() {
    return p1;
  }

  public Particle getChild2() {
    return p2;
  }
}
