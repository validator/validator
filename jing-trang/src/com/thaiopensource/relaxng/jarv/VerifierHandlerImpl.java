package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.PatternValidator;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.ErrorHandler;

class VerifierHandlerImpl extends PatternValidator implements VerifierHandler {
  private boolean complete = false;
  private final CountingErrorHandler ceh;

  VerifierHandlerImpl(Pattern pattern, ValidatorPatternBuilder builder, CountingErrorHandler ceh) {
    super(pattern, builder, ceh);
    this.ceh = ceh;
  }

  public void endDocument() {
    super.endDocument();
    complete = true;
  }

  public boolean isValid() throws IllegalStateException {
    if (!complete)
      throw new IllegalStateException();
    return !ceh.getHadErrorOrFatalError();
  }

  void setErrorHandler(ErrorHandler eh) {
    ceh.setErrorHandler(eh);
  }

  public void reset() {
    super.reset();
    if (ceh != null)
      ceh.reset();
  }
}
