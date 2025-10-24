package com.thaiopensource.validate.auto;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public abstract class ReparseException extends SAXException {
  public ReparseException() {
    super((Exception)null);
  }

  public abstract Schema reparse(SAXSource source) throws IncorrectSchemaException, SAXException, IOException;
}
