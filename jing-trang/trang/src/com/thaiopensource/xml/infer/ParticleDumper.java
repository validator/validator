package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

public class ParticleDumper implements ParticleVisitor {

  final private String defaultNamespace;

  private ParticleDumper(String defaultNamespace) {
    this.defaultNamespace = defaultNamespace;
  }

  public static String toString(Particle p, String defaultNamespace) {
    return new ParticleDumper(defaultNamespace).convert(p);
  }

  private String convert(Particle p) {
    return (String)p.accept(this);
  }

  public Object visitElement(ElementParticle p) {
    Name name = p.getName();
    String ns = name.getNamespaceUri();
    if (ns.equals(defaultNamespace))
      return name.getLocalName();
    return "{" + name.getNamespaceUri() + "}" + name.getLocalName();

  }

  public Object visitChoice(ChoiceParticle p) {
    StringBuffer buf = new StringBuffer();
    buf.append("(");
    convertForChoice(p, buf);
    buf.append(")");
    return buf.toString();
  }

  private void convertForChoice(Particle p, StringBuffer buf) {
    if (p instanceof ChoiceParticle)
      convertForChoice((ChoiceParticle)p, buf);
    else
      buf.append(convert(p));
  }

  private void convertForChoice(ChoiceParticle cp, StringBuffer buf) {
    convertForChoice(cp.getChild1(), buf);
    buf.append('|');
    convertForChoice(cp.getChild2(), buf);
  }

  public Object visitSequence(SequenceParticle p) {
    StringBuffer buf = new StringBuffer();
    buf.append("(");
    convertForSequence(p, buf);
    buf.append(")");
    return buf.toString();
  }

  private void convertForSequence(Particle p, StringBuffer buf) {
    if (p instanceof SequenceParticle)
      convertForSequence((SequenceParticle)p, buf);
    else
      buf.append(convert(p));
  }

  private void convertForSequence(SequenceParticle sp, StringBuffer buf) {
    convertForSequence(sp.getChild1(), buf);
    buf.append(',');
    convertForSequence(sp.getChild2(), buf);
  }

  public Object visitEmpty(EmptyParticle p) {
    return "#empty";
  }

  public Object visitText(TextParticle p) {
    return "#text";
  }

  public Object visitOneOrMore(OneOrMoreParticle p) {
    return convert(p.getChild()) + "+";
  }
}
