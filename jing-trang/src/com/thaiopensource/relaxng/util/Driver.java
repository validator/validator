package com.thaiopensource.relaxng.util;

import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.Version;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
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

  private int validationFlags = ValidationEngine.CHECK_ID_IDREF;
  private boolean timing = false;
  private String encoding = null;

  public int doMain(String[] args) {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    OptionParser op = new OptionParser("itcfe:", args);
    try {
      while (op.moveToNextOption()) {
        switch (op.getOptionChar()) {
        case 'i':
          validationFlags &= ~ValidationEngine.CHECK_ID_IDREF;
          break;
        case 'c':
          validationFlags |= ValidationEngine.COMPACT_SYNTAX;
          break;
        case 't':
          timing = true;
          break;
        case 'e':
          encoding = op.getOptionArg();
          break;
        case 'f':
          validationFlags |= ValidationEngine.FEASIBLE;
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
      ValidationEngine engine = new ValidationEngine(new Jaxp11XMLReaderCreator(), eh, validationFlags);
      InputSource in = ValidationEngine.uriOrFileInputSource(args[0]);
      if (encoding != null)
        in.setEncoding(encoding);
      if (engine.loadSchema(in)) {
        loadedPatternTime = System.currentTimeMillis();
	for (int i = 1; i < args.length; i++) {
	  if (!engine.validate(ValidationEngine.uriOrFileInputSource(args[i])))
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
