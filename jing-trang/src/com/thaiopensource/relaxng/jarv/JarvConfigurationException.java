package com.thaiopensource.relaxng.jarv;

import org.iso_relax.verifier.VerifierConfigurationException;

public class JarvConfigurationException extends RuntimeException {
  private final VerifierConfigurationException wrapped;

  public JarvConfigurationException(VerifierConfigurationException wrapped) {
    this.wrapped = wrapped;
  }

  public VerifierConfigurationException getWrapped() {
    return wrapped;
  }
}
