package com.thaiopensource.xml.dtd.test;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.util.Hashtable;

import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.xml.dtd.om.DtdParser;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.dtd.app.SchemaWriter;
import com.thaiopensource.xml.dtd.app.XmlOutputStreamWriter;
import com.thaiopensource.xml.dtd.app.FileEntityManager;

public class Driver {
  public static void main (String args[]) throws IOException, TestFailException {
    String dir = args[0];
    String failDir = args[1];
    String[] files = new File(dir).list();
    Hashtable fileTable = new Hashtable();
    for (int i = 0; i < files.length; i++)
      fileTable.put(files[i], files[i]);
    String failures = null;
    for (int i = 0; i < files.length; i++)
      if (files[i].endsWith(".dtd")) {
	String inFile = files[i];
	String outFile = inFile.substring(0, inFile.length() - 4) + ".xml";
	if (fileTable.get(outFile) != null) {
	  try {
	    System.err.println("Running test " + inFile);
	    runCompareTest(new File(dir, inFile), new File(dir, outFile));
	  }
	  catch (CompareFailException e) {
	    System.err.println(inFile + " failed at byte " + e.getByteIndex());
	    if (failures == null)
	      failures = inFile;
	    else
	      failures += " " + inFile;
	    runOutputTest(new File(dir, inFile), new File(failDir, outFile));
	  }
	}
      }
    if (failures != null)
      throw new TestFailException(failures);
  }

  public static void runCompareTest(File inFile, File outFile) throws IOException {
    runTest(inFile,
	    new CompareOutputStream(new BufferedInputStream(new FileInputStream(outFile))));

  }

  public static void runOutputTest(File inFile, File outFile) throws IOException {
    runTest(inFile, new FileOutputStream(outFile));
  }

  private static void runTest(File inFile, OutputStream out) throws IOException {
    DtdParser dtdParser = new DtdParserImpl();
    Dtd dtd = dtdParser.parse(inFile.toString(), new FileEntityManager());
    XmlWriter w = new XmlOutputStreamWriter(out, dtd.getEncoding());
    new SchemaWriter(w).writeDtd(dtd);
    w.close();
  }
}
