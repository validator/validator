package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

class VerifierImpl extends org.iso_relax.verifier.impl.VerifierImpl {
  private final VerifierHandlerImpl vhi;
  private boolean needReset = false;

  VerifierImpl(Pattern start, ValidatorPatternBuilder builder) throws VerifierConfigurationException {
    vhi = new VerifierHandlerImpl(start, builder);
  }

  public VerifierHandler getVerifierHandler() throws SAXException {
    if (needReset)
      vhi.reset();
    else
      needReset = true;
    return vhi;
  }

  public void setErrorHandler(ErrorHandler handler) {
    vhi.setErrorHandler(handler);
    super.setErrorHandler(handler);
  }
}
