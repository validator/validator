package com.thaiopensource.relaxng.util;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.schematron.SchematronProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


/**
 * Ant task to validate XML files using RELAX NG or other schema languages.
 */

public class JingTask extends Task {

  private File schemaFile;
  private File src;
  private final Vector filesets = new Vector();
  private PropertyMapBuilder properties = new PropertyMapBuilder();
  private boolean failOnError = true;
  private SchemaReader schemaReader = null;

  private class LogErrorHandler extends ErrorHandlerImpl {
    int logLevel = Project.MSG_ERR;

    public void warning(SAXParseException e) throws SAXParseException {
      logLevel = Project.MSG_WARN;
      super.warning(e);
    }

    public void error(SAXParseException e) {
      logLevel = Project.MSG_ERR;
      super.error(e);
    }

    public void printException(Throwable e) {
      logLevel = Project.MSG_ERR;
      super.printException(e);
    }

    public void print(String message) {
      log(message, logLevel);
    }
  }

  public JingTask() {
    RngProperty.CHECK_ID_IDREF.add(properties);
  }

  public void execute() throws BuildException {
    if (schemaFile == null)
      throw new BuildException("There must be an rngFile or schemaFile attribute",
			       getLocation());
    if (src == null && filesets.size() == 0)
      throw new BuildException("There must be a file attribute or a fileset child element",
			       getLocation());

    ErrorHandlerImpl eh = new LogErrorHandler();

    boolean hadError = false;

    try {
      ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), schemaReader);
      if (!driver.loadSchema(ValidationDriver.fileInputSource(schemaFile)))
	hadError = true;
      else {
	if (src != null) {
	  if (!driver.validate(ValidationDriver.fileInputSource(src)))
	    hadError = true;
	}
	for (int i = 0; i < filesets.size(); i++) {
	  FileSet fs = (FileSet)filesets.elementAt(i);
	  DirectoryScanner ds = fs.getDirectoryScanner(getProject());
	  File dir = fs.getDir(getProject());
	  String[] srcs = ds.getIncludedFiles();
	  for (int j = 0; j < srcs.length; j++) {
	    if (!driver.validate(ValidationDriver.fileInputSource(new File(dir, srcs[j]))))
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
    if (hadError && failOnError)
      throw new BuildException("Validation failed, messages should have been provided.", getLocation());
  }

  /**
   * Handles the <code>rngfile</code> attribute.
   *
   * @param rngFilename the attribute value
   */
  public void setRngfile(String rngFilename) {
    schemaFile = getProject().resolveFile(rngFilename);
  }

  /**
   * Handles the <code>schemafile</code> attribute.
   *
   * @param schemaFilename the attribute value
   */
  public void setSchemafile(String schemaFilename) {
    schemaFile = getProject().resolveFile(schemaFilename);
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
    properties.put(RngProperty.CHECK_ID_IDREF,
                   checkid ? Flag.PRESENT : null);
  }

  /**
   * Handles the <code>compactsyntax</code> attribute.
   *
   * @param compactsyntax the attribute value converted to a boolean
   */
  public void setCompactsyntax(boolean compactsyntax) {
    schemaReader = compactsyntax ? CompactSchemaReader.getInstance() : null;
  }

  /**
   * Handles the <code>feasible</code> attribute.
   *
   * @param feasible the attribute value converted to a boolean
   */
  public void setFeasible(boolean feasible) {
    properties.put(RngProperty.FEASIBLE, feasible ? Flag.PRESENT : null);
  }

  /**
   * Handles the phase attribute.
   *
   * @param phase the attribute value
   */
  public void setPhase(String phase) {
    SchematronProperty.PHASE.put(properties, phase);
  }

  /**
   * Handles the <code>failonerror</code> attribute.
   *
   * @param failOnError the attribute value converted to a boolean
   */
  public void setFailonerror(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public void addFileset(FileSet set) {
    filesets.addElement(set);
  }

}
