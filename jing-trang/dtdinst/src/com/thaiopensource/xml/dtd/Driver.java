package com.thaiopensource.xml.dtd;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Driver {

  public static void main (String args[]) throws IOException {
    Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(args[0])));
    Parser p = new Parser(r);
    p.parse();
  }

}
