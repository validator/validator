package com.thaiopensource.relaxng.util;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaFactory;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;

class ValidationEngine {
  private XMLReaderCreator xrc;
  private XMLReader xr;
  private ErrorHandler eh;
  private SchemaFactory factory;
  private ValidatorHandler vh;
  private Schema schema;

  public ValidationEngine() {
    factory = new SchemaFactory();
    factory.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
  }

  public void setXMLReaderCreator(XMLReaderCreator xrc) {
    this.xrc = xrc;
    factory.setXMLReaderCreator(xrc);
  }
  
  /**
   * A call to setErrorHandler can be made at any time.
   */
  public void setErrorHandler(ErrorHandler eh) {
    this.eh = eh;
    if (xr != null && eh != null)
      xr.setErrorHandler(eh);
    factory.setErrorHandler(eh);
  }

  public void setCheckId(boolean checkId) {
    factory.setCheckIdIdref(checkId);
  }

  /**
   * setXMLReaderCreator must be called before any call to loadPattern
   */
  public boolean loadPattern(InputSource in) throws SAXException, IOException {
    schema = null;
    vh = null;
    try {
      schema = factory.createSchema(in);
      return true;
    }
    catch (IncorrectSchemaException e) {
      return false;
    }
  }

  /**
   * loadPattern must be called before any call to validate
   */
  public boolean validate(InputSource in) throws SAXException, IOException {
    if (vh == null)
     vh = schema.createValidator(eh);
    else
      vh.reset();
    if (xr == null) {
      xr = xrc.createXMLReader();
      if (eh != null)
        xr.setErrorHandler(eh);
    }
    xr.setContentHandler(vh);
    xr.parse(in);
    return vh.isValid();
  }
}
