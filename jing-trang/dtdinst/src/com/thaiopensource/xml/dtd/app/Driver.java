package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.MissingResourceException;

import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.util.OptionParser;

public class Driver {

  private static final int FAILURE_EXIT_CODE = 1;

  private static class ErrorMessageHandlerImpl implements ErrorMessageHandler {
    private int errorCount = 0;

    public void message(ErrorMessage message) {
      switch (message.getSeverity()) {
      case ErrorMessage.WARNING:
	error(message.getMessage());
	break;
      case ErrorMessage.ERROR:
	errorCount++;
	warning(message.getMessage());
	break;
      }
    }
  }

  public static void main(String[] args) {
    try {
      if (doMain(args))
	return;
    }
    catch (IOException e) {
      error(e.getMessage());
    }
    System.exit(FAILURE_EXIT_CODE);
  }

  public static boolean doMain(String args[]) throws IOException {
    OptionParser opts = new OptionParser("r:", args);
    File dir = null;
    try {
      while (opts.moveToNextOption()) {
	switch (opts.getOptionChar()) {
	case 'r':
	  if (dir != null) {
	    error(localizer().message("DUPLICATE_OPTION", "r"));
	    return false;
	  }
	  dir = new File(opts.getOptionArg());
	  if (!dir.isDirectory()) {
	    if (dir.exists()) {
	      error(localizer().message("NOT_DIRECTORY", args[1]));
	      return false;
	    }
	    if (!dir.mkdirs()) {
	      error(localizer().message("CANNOT_MKDIR", args[1]));
	      return false;
	    }
	  }
	  break;
	}
      }
    }
    catch (OptionParser.InvalidOptionException e) {
      error(localizer().message("INVALID_OPTION", opts.getOptionCharString()));
      usage();
      return false;
    }
    catch (OptionParser.MissingArgumentException e) {
      error(localizer().message("OPTION_MISSING_ARGUMENT",
                                opts.getOptionCharString()));
      usage();
      return false;
    }
    args = opts.getRemainingArgs();
    if (args.length == 0) {
      error(localizer().message("MISSING_ARGUMENT"));
      usage();
      return false;
    }
    if (args.length > 1) {
      error(localizer().message("TOO_MANY_ARGUMENTS"));
      usage();
      return false;
    }
    String uri = UriEntityManager.commandLineArgToUri(args[0]);
    Dtd dtd = new DtdParserImpl().parse(uri, new UriEntityManager());
    if (dir == null) {
      XmlWriter w = new XmlOutputStreamWriter(System.out, dtd.getEncoding());
      new SchemaWriter(w).writeDtd(dtd);
      w.close();
      return true;
    }
    else {
      NameMapper mapper = new ExtensionMapper(".dtd", ".rng");
      DirectoryOutputCollection out
	= new DirectoryOutputCollection(dtd.getUri(), dir, mapper);
      RelaxNgWriter w = new RelaxNgWriter(out);
      ErrorMessageHandlerImpl emh = new ErrorMessageHandlerImpl();
      w.setErrorMessageHandler(emh);
      w.writeDtd(dtd);
      return emh.errorCount == 0;
    }
  }

  private static void usage() {
    print(localizer().message("USAGE", getVersion()));
  }

  private static Localizer localizer() {
    return RelaxNgWriter.localizer;
  }

  private static void error(String str) {
    print(localizer().message("ERROR", str));
  }

  private static void warning(String str) {
    print(localizer().message("WARNING", str));
  }
  
  private static void print(String str) {
    System.err.println(str);
  }

  private static String getVersion() {
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
