package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class WildcardElement extends Particle {
  private final Wildcard wildcard;

  public WildcardElement(SourceLocation location, Annotation annotation, Wildcard wildcard) {
    super(location, annotation);
    this.wildcard = wildcard;
  }

  public Wildcard getWildcard() {
    return wildcard;
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && ((WildcardElement)obj).wildcard.equals(wildcard);
  }

  public int hashCode() {
    return super.hashCode() ^ wildcard.hashCode();
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitWildcardElement(this);
  }
}
