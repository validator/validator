package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;

public class ComplexTypeComplexContent extends ComplexTypeAllowedContent {
  private final Particle particle;
  private final boolean mixed;
  /**
   * particle may be null
   */
  public ComplexTypeComplexContent(AttributeUse attributeUses, Particle particle, boolean mixed) {
    super(attributeUses);
    this.particle = particle;
    this.mixed = mixed;
  }

  public Particle getParticle() {
    return particle;
  }

  public boolean isMixed() {
    return mixed;
  }

  public Object accept(ComplexTypeVisitor visitor) {
    return visitor.visitComplexContent(this);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ComplexTypeComplexContent))
      return false;
    ComplexTypeComplexContent other = (ComplexTypeComplexContent)obj;
    if (particle == null) {
      if (other.particle != null)
        return false;
    }
    else if (!particle.equals(other.particle))
      return false;
    return getAttributeUses().equals(other.getAttributeUses()) && mixed == other.mixed;
  }

  public int hashCode() {
    int hc = getAttributeUses().hashCode();
    if (particle != null)
      hc ^= particle.hashCode();
    return hc;
  }
}
