package com.thaiopensource.relaxng.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.Properties;
import java.util.MissingResourceException;
import java.net.URL;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

import com.thaiopensource.relaxng.ValidationEngine;
import com.thaiopensource.relaxng.XMLReaderCreator;

import org.relaxng.datatype.DatatypeLibraryFactory;

class Driver {

  static private String className = null;
  static private boolean isSax2 = true;
  static private String usageKey = "usage";

  static public void setUsageKey(String key) {
    usageKey = key;
  }

  static public void main(String[] args) {
    System.exit(new Driver().doMain(args));
  }

  public int doMain(String[] args) {
    long startTime = System.currentTimeMillis();
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    if (args.length < 1) {
      eh.print(eh.format(usageKey, new Object[]{ getVersion() }));
      return 2;
    }
    boolean hadError = false;
    try {
      ValidationEngine engine = new ValidationEngine();
      engine.setXMLReaderCreator(createXMLReaderCreator());
      engine.setErrorHandler(eh);
      engine.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
      if (engine.loadPattern(fileInputSource(args[0]))) {
	for (int i = 1; i < args.length; i++) {
	  if (!engine.validate(fileInputSource(args[i])))
	    hadError = true;
	}
      }
      else
	hadError = true;
    }
    catch (SAXException e) {
      hadError = true;
      eh.printException(e);
    }
    catch (IOException e) {
      hadError = true;
      eh.printException(e);
    }
    eh.print(eh.format("elapsed_time",
		       new Object[] { new Long(System.currentTimeMillis()
					       - startTime) }));
    if (hadError)
      return 1;
    return 0;
  }

  static private InputSource fileInputSource(String str) {
    return new InputSource(FileURL.fileToURL(new File(str)).toString());
  }

  static public void setParser(String cls, boolean b) {
    className = cls;
    isSax2 = b;
  }

  static XMLReaderCreator createXMLReaderCreator() {
    if (className == null) {
      className = System.getProperty("com.thaiopensource.relaxng.util.XMLReader");
      if (className == null) {
	className = System.getProperty("com.thaiopensource.relaxng.util.Parser");
	isSax2 = false;
      }
    }
    if (className == null)
      return new Jaxp11XMLReaderCreator();
    else if (isSax2)
      return new XMLReaderCreatorImpl2(className);
    else
      return new XMLReaderCreatorImpl1(className);
  }

  static private String getVersion() {
    InputStream in = Driver.class.getResourceAsStream("resources/Version.properties");
    if (in != null) {
      Properties props = new Properties();
      try {
	props.load(in);
	String version = props.getProperty("version");
	if (version != null)
	  return version;
      }
      catch (IOException e) { }
    }
    throw new MissingResourceException("no version property",
				       Driver.class.getName(),
				       "version");
  }
}
