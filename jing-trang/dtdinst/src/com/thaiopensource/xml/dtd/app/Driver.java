package com.thaiopensource.xml.dtd.app;

import java.io.IOException;

import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;

public class Driver implements ErrorMessageHandler {
  
  public void message(ErrorMessage message) {
    switch (message.getSeverity()) {
    case ErrorMessage.WARNING:
      System.err.print("Warning:");
      break;
    case ErrorMessage.ERROR:
      System.err.print("Error:");
      break;
    }
    System.err.println(message.getMessage());
  }

  public static void main (String args[]) throws IOException {
    Dtd dtd = new DtdParserImpl().parse(args[0], new FileEntityManager());
    ExtensionMapper mapper = new ExtensionMapper(".dtd", ".rng");
    DirectoryOutputCollection out
      = new DirectoryOutputCollection(dtd.getUri(), args[1], mapper);
    RelaxNgWriter w = new RelaxNgWriter(out);
    w.setErrorMessageHandler(new Driver());
    w.writeDtd(dtd);
  }
}
