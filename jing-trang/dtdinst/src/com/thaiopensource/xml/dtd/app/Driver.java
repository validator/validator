package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import com.thaiopensource.xml.out.CharRepertoire;
import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.xml.util.EncodingMap;
import com.thaiopensource.xml.dtd.om.DtdParser;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;

public class Driver {
  public static void main (String args[]) throws IOException {
    DtdParser dtdParser = new DtdParserImpl();
    Dtd dtd = dtdParser.parse(args[0], new FileEntityManager());
    String enc = EncodingMap.getJavaName(dtd.getEncoding());
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out,
								 enc));
    CharRepertoire cr = CharRepertoire.getInstance(enc);
    new RelaxNgWriter(new XmlWriter(w, cr)).writeDtd(dtd);
    w.flush();
  }
}
