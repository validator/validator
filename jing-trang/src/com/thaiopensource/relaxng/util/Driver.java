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

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.util.OptionParser;

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

  private boolean checkId = true;
  private boolean timing = false;

  public int doMain(String[] args) {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    OptionParser op = new OptionParser("it", args);
    try {
      while (op.moveToNextOption()) {
        switch (op.getOptionChar()) {
        case 'i':
          checkId = false;
          break;
        case 't':
          timing = true;
          break;
        }
      }
    }
    catch (OptionParser.InvalidOptionException e) {
      eh.print(eh.format("invalid_option",
                         new Object[]{ op.getOptionCharString() }));
      return 2;
    }
    catch (OptionParser.MissingArgumentException e) {
      eh.print(eh.format("option_missing_argument",
                         new Object[]{ op.getOptionCharString() }));
      return 2;
    }
    args = op.getRemainingArgs();
    if (args.length < 1) {
      eh.print(eh.format(usageKey, new Object[]{ getVersion() }));
      return 2;
    }
    long startTime = System.currentTimeMillis();
    long loadedPatternTime = -1;
    boolean hadError = false;
    try {
      ValidationEngine engine = new ValidationEngine(createXMLReaderCreator(), eh, checkId);
      if (engine.loadSchema(ValidationEngine.fileInputSource(args[0]))) {
        loadedPatternTime = System.currentTimeMillis();
	for (int i = 1; i < args.length; i++) {
	  if (!engine.validate(ValidationEngine.fileInputSource(args[i])))
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
    if (timing) {
      long endTime = System.currentTimeMillis();
      if (loadedPatternTime < 0)
        loadedPatternTime = endTime;
      eh.print(eh.format("elapsed_time",
		       new Object[] {
                         new Long(loadedPatternTime - startTime),
                         new Long(endTime - loadedPatternTime),
                         new Long(endTime - startTime)
                       }));
    }
    if (hadError)
      return 1;
    return 0;
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
      return new Sax2XMLReaderCreator(className);
    else
      return new Sax1XMLReaderCreator(className);
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
