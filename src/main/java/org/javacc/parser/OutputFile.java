/* Copyright (c) 2007, Paul Cager.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.javacc.Version;

/**
 * This class handles the creation and maintenance of the boiler-plate classes,
 * such as Token.java, JavaCharStream.java etc.
 *
 * It is responsible for:
 *
 * <ul>
 * <li>Writing the JavaCC header lines to the file.</li>
 * <li>Writing the checksum line.</li>
 * <li>Using the checksum to determine if an existing file has been changed by
 * the user (and so should be left alone).</li>
 * <li>Checking any existing file's version (if the file can not be
 * overwritten).</li>
 * <li>Checking any existing file's creation options (if the file can not be
 * overwritten).</li>
 * <li></li>
 * </ul>
 *
 * @author Paul Cager
 *
 */
public class OutputFile {
  private static final String MD5_LINE_PART_1 = "/* JavaCC - OriginalChecksum=";
  private static final String MD5_LINE_PART_1q = "/\\* JavaCC - OriginalChecksum=";
  private static final String MD5_LINE_PART_2 = " (do not edit this line) */";
  private static final String MD5_LINE_PART_2q = " \\(do not edit this line\\) \\*/";

  TrapClosePrintWriter pw;

  DigestOutputStream dos;

  String toolName = JavaCCGlobals.toolName;

  final File file;

  final String compatibleVersion;

  final String[] options;

  /**
   * Create a new OutputFile.
   *
   * @param file
   *            the file to write to.
   * @param compatibleVersion
   *      the minimum compatible JavaCC version.
   * @param options
   *      if the file already exists, and cannot be overwritten, this is
   *      a list of options (such s STATIC=false) to check for changes.
   * @throws IOException
   */
  public OutputFile(File file, String compatibleVersion, String[] options)
  throws IOException {
    this.file = file;
    this.compatibleVersion = compatibleVersion;
    this.options = options;

    if (file.exists()) {
      // Generate the checksum of the file, and compare with any value
      // stored
      // in the file.

      BufferedReader br = new BufferedReader(new FileReader(file));
      MessageDigest digest;
      try {
        digest = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw (IOException) (new IOException("No MD5 implementation")
        .initCause(e));
      }
      DigestOutputStream digestStream = new DigestOutputStream(
          new NullOutputStream(), digest);
      PrintWriter pw = new PrintWriter(digestStream);
      String line;
      String existingMD5 = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith(MD5_LINE_PART_1)) {
          existingMD5 = line.replaceAll(MD5_LINE_PART_1q, "").replaceAll(
              MD5_LINE_PART_2q, "");
        } else {
          pw.println(line);
        }
      }

      pw.close();
      String calculatedDigest = toHexString(digestStream
          .getMessageDigest().digest());

      if (existingMD5 == null || !existingMD5.equals(calculatedDigest)) {
        // No checksum in file, or checksum differs.
        needToWrite = false;

        if (compatibleVersion != null) {
          checkVersion(file, compatibleVersion);
        }

        if (options != null) {
          checkOptions(file, options);
        }

      } else {
        // The file has not been altered since JavaCC created it.
        // Rebuild it.
        System.out.println("File \"" + file.getName()
            + "\" is being rebuilt.");
        needToWrite = true;
      }
    } else {
      // File does not exist
      System.out.println("File \"" + file.getName() + "\" does not exist.  Will create one.");
      needToWrite = true;
    }
  }

  public OutputFile(File file) throws IOException {
    this(file, null, null);
  }

  public boolean needToWrite = true;

  /**
   * Output a warning if the file was created with an incompatible version
   * of JavaCC.
   * @param fileName
   * @param versionId
   */
  private void checkVersion(File file, String versionId) {
	String firstLine = "/* " + JavaCCGlobals.getIdString(toolName, file.getName()) + " Version ";

    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(firstLine)) {
          String version = line.replaceFirst(".*Version ", "").replaceAll(" \\*/", "");
           if (!version.equals(versionId)) {
            JavaCCErrors.warning(file.getName()
                + ": File is obsolete.  Please rename or delete this file so"
                + " that a new one can be generated for you.");
            JavaCCErrors.warning(file.getName()
            	+ " file   version: " + version
            	+ " javacc version: " + versionId);
          }
          return;
        }
      }
      // If no version line is found, do not output the warning.
    } catch (FileNotFoundException e1) {
      // This should never happen
      JavaCCErrors.semantic_error("Could not open file " + file.getName() + " for writing.");
      throw new Error();
    } catch (IOException e2) {
    }
  }

  /**
   * Read the options line from the file and compare to the options currently in
   * use. Output a warning if they are different.
   *
   * @param fileName
   * @param options
   */
  private void checkOptions(File file, String[] options) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("/* JavaCCOptions:")) {
          String currentOptions = Options.getOptionsString(options);
          if (line.indexOf(currentOptions) == -1) {
            JavaCCErrors
            .warning(file.getName()
                + ": Generated using incompatible options. Please rename or delete this file so"
                + " that a new one can be generated for you.");
          }
          return;
        }
      }
    } catch (FileNotFoundException e1) {
      // This should never happen
      JavaCCErrors.semantic_error("Could not open file " + file.getName()
          + " for writing.");
      throw new Error();
    } catch (IOException e2) {
    }

    // Not found so cannot check
  }

  /**
   * Return a PrintWriter object that may be used to write to this file. Any
   * necessary header information is written by this method.
   *
   * @return
   * @throws IOException
   */
  public PrintWriter getPrintWriter() throws IOException {
    if (pw == null) {
      MessageDigest digest;
      try {
        digest = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw (IOException) (new IOException("No MD5 implementation")
        .initCause(e));
      }
      dos = new DigestOutputStream(new BufferedOutputStream(
          new FileOutputStream(file)), digest);
      pw = new TrapClosePrintWriter(dos);

      // Write the headers....
      String version = compatibleVersion == null ? Version.versionNumber : compatibleVersion;
      pw.println("/* "
          + JavaCCGlobals.getIdString(toolName, file.getName())
          + " Version " + version + " */");
      if (options != null) {
        pw.println("/* JavaCCOptions:" + Options.getOptionsString(options) + " */");
      }
    }

    return pw;
  }

  /**
   * Close the OutputFile, writing any necessary trailer information
   * (such as a checksum).
   * @throws IOException
   */
  public void close() throws IOException {

    // Write the trailer (checksum).
    // Possibly rename the .java.tmp to .java??
    if (pw != null) {
      pw.println(MD5_LINE_PART_1 + getMD5sum() + MD5_LINE_PART_2);
      pw.closePrintWriter();
//    file.renameTo(dest)
    }
  }

  private String getMD5sum() {
    pw.flush();
    byte[] digest = dos.getMessageDigest().digest();
    return toHexString(digest);
  }

  private final static char[] HEX_DIGITS = new char[] { '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static final String toHexString(byte[] bytes) {
    StringBuffer sb = new StringBuffer(32);
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      sb.append(HEX_DIGITS[(b & 0xF0) >> 4]).append(HEX_DIGITS[b & 0x0F]);
    }
    return sb.toString();
  }

  private static class NullOutputStream extends OutputStream {

    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
    }

    public void write(byte[] arg0) throws IOException {
    }

    public void write(int arg0) throws IOException {
    }
  }

  private class TrapClosePrintWriter extends PrintWriter {

    public TrapClosePrintWriter(OutputStream os) {
      super(os);
    }

    public void closePrintWriter() {
      super.close();
    }

    public void close() {
      try {
        OutputFile.this.close();
      } catch (IOException e) {
        System.err.println("Could not close " + file.getAbsolutePath());
      }
    }
  }

  /**
   * @return the toolName
   */
  public String getToolName() {
    return toolName;
  }

  /**
   * @param toolName
   *            the toolName to set
   */
  public void setToolName(String toolName) {
    this.toolName = toolName;
  }

  public String getPath() {
    return file.getAbsolutePath();
  }
}
