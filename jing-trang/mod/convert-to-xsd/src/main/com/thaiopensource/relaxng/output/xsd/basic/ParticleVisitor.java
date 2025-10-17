package com.thaiopensource.relaxng.output.xsd.basic;

public interface ParticleVisitor<T> {
  T visitElement(Element p);
  T visitWildcardElement(WildcardElement p);
  T visitRepeat(ParticleRepeat p);
  T visitSequence(ParticleSequence p);
  T visitChoice(ParticleChoice p);
  T visitAll(ParticleAll p);
  T visitGroupRef(GroupRef p);
}
