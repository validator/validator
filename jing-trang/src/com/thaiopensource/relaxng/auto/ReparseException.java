package com.thaiopensource.relaxng.auto;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;

import java.io.IOException;

public abstract class ReparseException extends SAXException {
  public ReparseException() {
    super((Exception)null);
  }

  public abstract Schema reparse(InputSource in) throws IncorrectSchemaException, SAXException, IOException;
}
