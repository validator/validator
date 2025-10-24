package com.thaiopensource.validate.jarv;

import com.thaiopensource.validate.SchemaReaderFactory;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.Option;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierConfigurationException;

public class JarvSchemaReaderFactory implements SchemaReaderFactory {
  public SchemaReader createSchemaReader(String namespaceUri) {
    try {
      VerifierFactory vf = VerifierFactory.newInstance(namespaceUri);
      if (vf != null)
        return new VerifierFactorySchemaReader(vf);
    }
    catch (VerifierConfigurationException e) { }
    return null;
  }

  public Option getOption(String uri) {
    return null;
  }
}
