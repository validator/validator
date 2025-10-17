package com.thaiopensource.relaxng.jarv;

import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierFactoryLoader;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class VerifierFactoryLoaderImpl implements VerifierFactoryLoader {
  public VerifierFactory createFactory(String schemaLanguage) {
    if (schemaLanguage.equals(WellKnownNamespaces.RELAX_NG)
        || schemaLanguage.equals(WellKnownNamespaces.RELAX_NG_0_9))
      return new VerifierFactoryImpl();
    return null;
  }
}
