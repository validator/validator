package com.thaiopensource.relaxng.jarv;

import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierFactoryLoader;

public class VerifierFactoryLoaderImpl implements VerifierFactoryLoader {
  public VerifierFactory createFactory(String schemaLanguage) {
    if (schemaLanguage.equals("http://relaxng.org/ns/structure/1.0")
        || schemaLanguage.equals("http://relaxng.org/ns/structure/0.9"))
      return new VerifierFactoryImpl();
    return null;
  }
}
