package com.thaiopensource.xml.dtd;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class Driver {
  public static void main (String args[]) throws IOException {
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out));
    new SchemaWriter(new XmlWriter(w)).writeDtd(new Dtd(args[0]));
    w.flush();
  }
}
