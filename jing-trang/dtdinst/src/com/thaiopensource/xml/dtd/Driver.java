package com.thaiopensource.xml.dtd;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.Vector;

public class Driver {

  public static void main (String args[]) throws IOException {
    Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(args[0])));
    System.err.println("Parsing");
    Dtd dtd = new Parser(r).parse();
    System.err.println("Unexpanding");
    dtd.unexpandEntities();
    System.err.println("Creating decls");
    dtd.createDecls();
    // System.err.println("Dumping");
    // dtd.dump();
  }
}
