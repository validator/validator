package com.thaiopensource.xml.dtd.test;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Hashtable;

import com.thaiopensource.xml.dtd.Dtd;
import com.thaiopensource.xml.dtd.SchemaWriter;
import com.thaiopensource.xml.dtd.XmlWriter;
import com.thaiopensource.xml.dtd.FileEntityManager;

public class Driver {
  public static void main (String args[]) throws IOException, TestFailException {
    String dir = args[0];
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
	    runTest(new File(dir, inFile), new File(dir, outFile));
	  }
	  catch (CompareFailException e) {
	    System.err.println(inFile + " failed at byte " + e.getByteIndex());
	    if (failures == null)
	      failures = inFile;
	    else
	      failures += " " + inFile;
	  }
	}
      }
    if (failures != null)
      throw new TestFailException(failures);
  }

  public static void runTest(File inFile, File outFile) throws IOException {
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new CompareOutputStream(new BufferedInputStream(new FileInputStream(outFile)))));
    new SchemaWriter(new XmlWriter(w)).writeDtd(new Dtd(inFile.toString(),
							new FileEntityManager()));
    
    w.close();
  }
}
