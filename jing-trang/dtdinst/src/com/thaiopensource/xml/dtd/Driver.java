package com.thaiopensource.xml.dtd;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class Driver {
  public static void main (String args[]) throws IOException {
    Dtd dtd = new Dtd(args[0], new FileEntityManager());
    String enc = EncodingMap.getJavaName(dtd.getEncoding());
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out,
								 enc));
    CharRepertoire cr = CharRepertoire.getInstance(enc);
    new SchemaWriter(new XmlWriter(w, cr)).writeDtd(dtd);
    w.flush();
  }
}
