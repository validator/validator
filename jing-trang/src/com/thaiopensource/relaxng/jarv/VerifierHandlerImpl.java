package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.PatternValidatorHandler;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.ErrorHandler;

class VerifierHandlerImpl extends PatternValidatorHandler implements VerifierHandler {
  private boolean complete = false;

  VerifierHandlerImpl(Pattern pattern, ValidatorPatternBuilder builder) {
    super(pattern, builder, null);
  }

  public void endDocument() {
    super.endDocument();
    complete = true;
  }

  public boolean isValid() throws IllegalStateException {
    if (!complete)
      throw new IllegalStateException();
    return isValidSoFar();
  }

  public void setErrorHandler(ErrorHandler eh) {
    this.eh = eh;
  }
}
