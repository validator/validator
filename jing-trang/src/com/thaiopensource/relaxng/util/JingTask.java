package com.thaiopensource.relaxng.util;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;

import com.thaiopensource.relaxng.ValidationEngine;
import com.thaiopensource.relaxng.XMLReaderCreator;

/**
 * Ant task to validate XML files using RELAX NG.
 */

public class JingTask extends Task {

  private File rngFile;
  private File src;
  private Vector filesets = new Vector();

  public void execute() throws BuildException {
    if (rngFile == null)
      throw new BuildException("rngFile attribute must be set!", 
			       location);
    if (src == null && filesets.size() == 0)
      throw new BuildException("There must be a file attribute or a fileset child element",
			       location);

    ErrorHandlerImpl eh = new ErrorHandlerImpl(new PrintStream(new LogOutputStream(this, Project.MSG_WARN)));

    boolean hadError = false;

    try {
      ValidationEngine engine = new ValidationEngine();
      engine.setXMLReaderCreator(new Jaxp10XMLReaderCreator());
      engine.setErrorHandler(eh);
      engine.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
      if (!engine.loadPattern(fileInputSource(rngFile)))
	hadError = true;
      else {
	if (src != null) {
	  if (!engine.validate(fileInputSource(src)))
	    hadError = true;
	}
	for (int i = 0; i < filesets.size(); i++) {
	  FileSet fs = (FileSet)filesets.elementAt(i);
	  DirectoryScanner ds = fs.getDirectoryScanner(project);
	  File dir = fs.getDir(project);
	  String[] srcs = ds.getIncludedFiles();
	  for (int j = 0; j < srcs.length; j++) {
	    if (!engine.validate(fileInputSource(new File(dir, srcs[j]))))
	      hadError = true;
	  }
	}
      }
    }
    catch (SAXException e) {
      hadError = true;
      eh.printException(e);
    }
    catch (IOException e) {
      hadError = true;
      eh.printException(e);
    }
    if (hadError)
      throw new BuildException("Validation failed, messages should have been provided.", location);
  }

  static private InputSource fileInputSource(File f) {
    return new InputSource(FileURL.fileToURL(f).toString());
  }

  public void setRngfile(String rngFilename) {
    rngFile = project.resolveFile(rngFilename);
  }
    
  public void setFile(File file) {
    this.src = file;
  }
  
  public void addFileset(FileSet set) {
    filesets.addElement(set);
  }

}
