package com.thaiopensource.xml.dtd;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Driver {

  public static void main (String args[]) throws IOException {
    Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(args[0])));
    Dtd dtd = new Parser(r).parse();
    dtd.unexpandEntities();
    reparse(dtd);
    dtd.dump();
  }

  static public void reparse(Dtd dtd) {
    try {
      PrologParser pp = new PrologParser(PrologParser.EXTERNAL_ENTITY);
      parseAtoms(new AtomStream(dtd.getAtoms()), pp);
      pp.end();
    }
    catch (PrologSyntaxException e) {
      throw new Error("reparse botched");
    }
  }

  static public void parseAtoms(AtomStream as, PrologParser pp) throws PrologSyntaxException {
    while (as.advance()) {
      if (as.entity != null)
	parseAtoms(new AtomStream(as.entity.atoms), pp);
      else if (pp.action(as.tokenType, as.token)
	       == PrologParser.ACTION_IGNORE_SECT) {
	if (!as.advance())
	  throw new PrologSyntaxException();
      }
    }
  }

}
