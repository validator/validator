package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.PatternValidatorHandler;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import org.iso_relax.verifier.VerifierHandler;

class VerifierHandlerImpl extends PatternValidatorHandler implements VerifierHandler {
  VerifierHandlerImpl(Pattern pattern, ValidatorPatternBuilder builder) {
    super(pattern, builder, null);
  }

  public boolean isValid() throws IllegalStateException {
    if (!isComplete())
      throw new IllegalStateException();
    return isValidSoFar();
  }
}
