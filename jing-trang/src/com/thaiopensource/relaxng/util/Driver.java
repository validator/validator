package com.thaiopensource.relaxng.util;

import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.util.Version;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.validate.schematron.SchematronProperty;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

class Driver {
  static private String usageKey = "usage";

  static public void setUsageKey(String key) {
    usageKey = key;
  }

  static public void main(String[] args) {
    System.exit(new Driver().doMain(args));
  }

  private boolean timing = false;
  private String encoding = null;

  public int doMain(String[] args) {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    OptionParser op = new OptionParser("itcfe:p:", args);
    PropertyMapBuilder properties = new PropertyMapBuilder();
    ValidateProperty.ERROR_HANDLER.put(properties, eh);
    RngProperty.CHECK_ID_IDREF.add(properties);
    SchemaReader sr = null;
    try {
      while (op.moveToNextOption()) {
        switch (op.getOptionChar()) {
        case 'i':
          properties.put(RngProperty.CHECK_ID_IDREF, null);
          break;
        case 'c':
          sr = CompactSchemaReader.getInstance();
          break;
        case 't':
          timing = true;
          break;
        case 'e':
          encoding = op.getOptionArg();
          break;
        case 'f':
          RngProperty.FEASIBLE.add(properties);
          break;
        case 'p':
          SchematronProperty.PHASE.put(properties, op.getOptionArg());
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
      eh.print(eh.format(usageKey, new Object[]{ Version.getVersion(Driver.class) }));
      return 2;
    }
    long startTime = System.currentTimeMillis();
    long loadedPatternTime = -1;
    boolean hadError = false;
    try {
      ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), sr);
      InputSource in = ValidationDriver.uriOrFileInputSource(args[0]);
      if (encoding != null)
        in.setEncoding(encoding);
      if (driver.loadSchema(in)) {
        loadedPatternTime = System.currentTimeMillis();
	for (int i = 1; i < args.length; i++) {
	  if (!driver.validate(ValidationDriver.uriOrFileInputSource(args[i])))
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

}
