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

import com.thaiopensource.relaxng.XMLReaderCreator;


/**
 * Ant task to validate XML files using RELAX NG.
 */

public class JingTask extends Task {

  private File rngFile;
  private File src;
  private Vector filesets = new Vector();
  private boolean checkid = false;

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
      ValidationEngine engine = new ValidationEngine(new Jaxp11XMLReaderCreator(), eh, checkid);
      if (!engine.loadSchema(ValidationEngine.fileInputSource(rngFile)))
	hadError = true;
      else {
	if (src != null) {
	  if (!engine.validate(ValidationEngine.fileInputSource(src)))
	    hadError = true;
	}
	for (int i = 0; i < filesets.size(); i++) {
	  FileSet fs = (FileSet)filesets.elementAt(i);
	  DirectoryScanner ds = fs.getDirectoryScanner(project);
	  File dir = fs.getDir(project);
	  String[] srcs = ds.getIncludedFiles();
	  for (int j = 0; j < srcs.length; j++) {
	    if (!engine.validate(ValidationEngine.fileInputSource(new File(dir, srcs[j]))))
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

  /**
   * Handles the <code>rngfile</code> attribute.
   *
   * @param rngFilename the attribute value
   */
  public void setRngfile(String rngFilename) {
    rngFile = project.resolveFile(rngFilename);
  }
    
  public void setFile(File file) {
    this.src = file;
  }

  /**
   * Handles the <code>checkid</code> attribute.
   *
   * @param checkid the attribute value converted to a boolean
   */
  public void setCheckid(boolean checkid) {
    this.checkid = checkid;
  }

  public void addFileset(FileSet set) {
    filesets.addElement(set);
  }

}
