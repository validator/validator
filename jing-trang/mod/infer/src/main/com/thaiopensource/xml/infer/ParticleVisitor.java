package com.thaiopensource.xml.infer;

public interface ParticleVisitor {
  Object visitElement(ElementParticle p);
  Object visitChoice(ChoiceParticle p);
  Object visitSequence(SequenceParticle p);
  Object visitEmpty(EmptyParticle p);
  Object visitText(TextParticle p);
  Object visitOneOrMore(OneOrMoreParticle p);
}
