package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;

class SchemaImpl implements Schema {
  private final SchemaPatternBuilder spb;
  private final Pattern start;

  SchemaImpl(Pattern start, SchemaPatternBuilder spb) {
    this.start = start;
    this.spb = spb;
  }

  public Verifier newVerifier() throws VerifierConfigurationException {
    return new VerifierImpl(start, new ValidatorPatternBuilder(spb));
  }
}
