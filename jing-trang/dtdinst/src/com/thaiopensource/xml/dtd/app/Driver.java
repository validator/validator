package com.thaiopensource.xml.dtd.app;

import java.io.IOException;

import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;

public class Driver {
  public static void main (String args[]) throws IOException {
    Dtd dtd = new DtdParserImpl().parse(args[0], new FileEntityManager());
    ExtensionMapper mapper = new ExtensionMapper(".dtd", ".rng");
    DirectoryOutputCollection out
      = new DirectoryOutputCollection(dtd.getUri(), args[1], mapper);
    new RelaxNgWriter(out).writeDtd(dtd);
  }
}
