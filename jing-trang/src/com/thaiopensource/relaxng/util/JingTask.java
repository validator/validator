package com.thaiopensource.relaxng.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;


/**
 * Ant task to validate XML files using RELAX NG.
 */

public class JingTask extends Task {

  private File schemaFile;
  private File src;
  private final Vector filesets = new Vector();
  private int flags = ValidationEngine.CHECK_ID_IDREF;

  public void execute() throws BuildException {
    if (schemaFile == null)
      throw new BuildException("There must be an rngFile or schemaFile attribute", 
			       location);
    if (src == null && filesets.size() == 0)
      throw new BuildException("There must be a file attribute or a fileset child element",
			       location);

    ErrorHandlerImpl eh = new ErrorHandlerImpl(new PrintStream(new LogOutputStream(this, Project.MSG_WARN)));

    boolean hadError = false;

    try {
      ValidationEngine engine = new ValidationEngine(new Jaxp11XMLReaderCreator(), eh, flags);
      if (!engine.loadSchema(ValidationEngine.fileInputSource(schemaFile)))
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
    schemaFile = project.resolveFile(rngFilename);
  }

  /**
   * Handles the <code>schemafile</code> attribute.
   *
   * @param schemaFilename the attribute value
   */
  public void setSchemafile(String schemaFilename) {
    schemaFile = project.resolveFile(schemaFilename);
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
    if (checkid)
      flags |= ValidationEngine.CHECK_ID_IDREF;
    else
      flags &= ~ValidationEngine.CHECK_ID_IDREF;
  }

  /**
   * Handles the <code>compactsyntax</code> attribute.
   *
   * @param compactsyntax the attribute value converted to a boolean
   */
  public void setCompactsyntax(boolean compactsyntax) {
    if (compactsyntax)
      flags |= ValidationEngine.COMPACT_SYNTAX;
    else
      flags &= ~ValidationEngine.COMPACT_SYNTAX;
  }

  /**
   * Handles the <code>feasible</code> attribute.
   *
   * @param feasible the attribute value converted to a boolean
   */
  public void setFeasible(boolean feasible) {
    if (feasible)
      flags |= ValidationEngine.FEASIBLE;
    else
      flags &= ~ValidationEngine.FEASIBLE;
  }

  public void addFileset(FileSet set) {
    filesets.addElement(set);
  }

}
