package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.ValidatorHandler;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.io.IOException;

public class VerifierFactorySchemaLanguage implements SchemaLanguage {
  private final VerifierFactory vf;

  static private class SchemaImpl implements Schema {
    org.iso_relax.verifier.Schema schema;

    private SchemaImpl(org.iso_relax.verifier.Schema schema) {
      this.schema = schema;
    }

    public ValidatorHandler createValidator(ErrorHandler eh) {
      try {
        return new VerifierValidatorHandler(schema.newVerifier(), eh);
      }
      catch (VerifierConfigurationException e) {
        Exception cause = e.getCauseException();
        if (cause instanceof RuntimeException
            && e.getMessage() == null || e.getMessage().equals(cause.getMessage()))
          throw (RuntimeException)cause;
        throw new JarvConfigurationException(e);
      }
    }

    public ValidatorHandler createValidator() {
      return createValidator(null);
    }
  }

  public VerifierFactorySchemaLanguage(VerifierFactory vf) {
    this.vf = vf;
  }

  public Schema createSchema(XMLReaderCreator xrc, InputSource in, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf)
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
