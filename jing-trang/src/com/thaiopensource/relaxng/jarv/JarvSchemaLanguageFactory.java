package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.auto.SchemaLanguageFactory;
import com.thaiopensource.relaxng.SchemaLanguage;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierConfigurationException;

public class JarvSchemaLanguageFactory implements SchemaLanguageFactory {
  public SchemaLanguage createSchemaLanguage(String namespaceUri) {
    try {
      VerifierFactory vf = VerifierFactory.newInstance(namespaceUri);
      if (vf != null)
        return new VerifierFactorySchemaLanguage(vf);
    }
    catch (VerifierConfigurationException e) { }
    return null;
  }
}
