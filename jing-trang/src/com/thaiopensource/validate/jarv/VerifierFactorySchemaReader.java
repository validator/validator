package com.thaiopensource.validate.jarv;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidatorHandler;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class VerifierFactorySchemaReader implements SchemaReader {
  private final VerifierFactory vf;

  static private class SchemaImpl implements Schema {
    org.iso_relax.verifier.Schema schema;

    private SchemaImpl(org.iso_relax.verifier.Schema schema) {
      this.schema = schema;
    }

    public ValidatorHandler createValidator(PropertyMap properties) {
      try {
        return new VerifierValidatorHandler(schema.newVerifier(), properties);
      }
      catch (VerifierConfigurationException e) {
        Exception cause = e.getCauseException();
        if (cause instanceof RuntimeException
            && e.getMessage() == null || e.getMessage().equals(cause.getMessage()))
          throw (RuntimeException)cause;
        throw new JarvConfigurationException(e);
      }
    }
  }

  public VerifierFactorySchemaReader(VerifierFactory vf) {
    this.vf = vf;
  }

  public Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    try {
      return new SchemaImpl(vf.compileSchema(in));
    }
    catch (SAXException e) {
      System.err.println("compileSchema threw a SAXException class " + e.getClass().toString());
      if (e.getException() != null)
        System.err.println("cause has class " + e.getException().getClass().toString());
      throw e;
    }
    catch (VerifierConfigurationException e) {
      for (;;) {
        Exception cause = e.getCauseException();
        String message = e.getMessage();
        if (cause != null && message != null && message.equals(cause.getMessage()))
          message = null; // don't really have a message
        if (message == null) {
          if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;
          if (cause instanceof SAXException)
            throw (SAXException)cause;
          if (cause instanceof IOException)
            throw (IOException)cause;
          if (cause instanceof VerifierConfigurationException) {
            e = (VerifierConfigurationException)cause;
            continue;
          }
        }
        throw new SAXException(message, cause);
      }
    }

  }

}
