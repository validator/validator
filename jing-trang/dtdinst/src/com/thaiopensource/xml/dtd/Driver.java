package com.thaiopensource.xml.dtd;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class Driver {
  public static void main (String args[]) throws IOException {
    new SchemaWriter(new XmlWriter(new BufferedWriter(new OutputStreamWriter(System.out)))).writeDtd(new Dtd(args[0]));
  }
}
