package com.thaiopensource.relaxng.output.xsd.basic;

public interface ParticleVisitor {
  Object visitElement(Element p);
  Object visitWildcardElement(WildcardElement p);
  Object visitRepeat(ParticleRepeat p);
  Object visitSequence(ParticleSequence p);
  Object visitChoice(ParticleChoice p);
  Object visitAll(ParticleAll p);
  Object visitGroupRef(GroupRef p);
}
