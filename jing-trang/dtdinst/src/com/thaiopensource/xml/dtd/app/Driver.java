package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.io.File;

import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.util.Localizer;

public class Driver {

  private static final int FAILURE_EXIT_CODE = 1;

  private static class ErrorMessageHandlerImpl implements ErrorMessageHandler {
    private int errorCount = 0;

    public void message(ErrorMessage message) {
      switch (message.getSeverity()) {
      case ErrorMessage.WARNING:
	errorCount++;
	error(message.getMessage());
	break;
      case ErrorMessage.ERROR:
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
    if (args.length != 2) {
      error(localizer().message("MISSING_ARGUMENT"));
      print(localizer().message("USAGE"));
      return false;
    }
    File dir = new File(args[1]);
    if (!dir.isDirectory()) {
      error(localizer().message("NOT_DIRECTORY", args[1]));
      return false;
    }
    Dtd dtd = new DtdParserImpl().parse(args[0], new FileEntityManager());
    NameMapper mapper = new ExtensionMapper(".dtd", ".rng");
    DirectoryOutputCollection out
      = new DirectoryOutputCollection(dtd.getUri(), dir, mapper);
    RelaxNgWriter w = new RelaxNgWriter(out);
    ErrorMessageHandlerImpl emh = new ErrorMessageHandlerImpl();
    w.setErrorMessageHandler(emh);
    w.writeDtd(dtd);
    return emh.errorCount == 0;
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
}
