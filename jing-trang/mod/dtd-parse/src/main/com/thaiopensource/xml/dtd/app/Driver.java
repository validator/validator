package com.thaiopensource.xml.dtd.app;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Version;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.out.XmlWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;

public class Driver {
  private static final int FAILURE_EXIT_CODE = 1;
  private static final Localizer localizer = new Localizer(Driver.class);

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
    if (args.length == 0) {
      error(localizer.message("MISSING_ARGUMENT"));
      usage();
      return false;
    }
    if (args.length > 1) {
      error(localizer.message("TOO_MANY_ARGUMENTS"));
      usage();
      return false;
    }
    String uri = UriOrFile.toUri(args[0]);
    Dtd dtd = new DtdParserImpl().parse(uri, new UriEntityManager());
    XmlWriter w = new XmlOutputStreamWriter(System.out, dtd.getEncoding());
    new SchemaWriter(w).writeDtd(dtd);
    w.close();
    return true;
  }

  private static void usage() {
    print(localizer.message("USAGE", Version.getVersion(Driver.class)));
  }

  private static void error(String str) {
    print(localizer.message("ERROR", str));
  }

  private static void warning(String str) {
    print(localizer.message("WARNING", str));
  }
  
  private static void print(String str) {
    System.err.println(str);
  }

}
