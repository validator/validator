package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;

class ComplexTypeComplexContentExtension extends ComplexTypeComplexContent {
  private final String base;

  ComplexTypeComplexContentExtension(AttributeUse attributeUses, Particle particle, boolean mixed, String base) {
    super(attributeUses, particle, mixed);
    this.base = base;
  }

  ComplexTypeComplexContentExtension(ComplexTypeComplexContent ct) {
    super(ct.getAttributeUses(), ct.getParticle(), ct.isMixed());
    this.base = null;
  }

  String getBase() {
    return base;
  }
}
