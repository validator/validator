// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.parser;

import static org.javacc.parser.JavaCCGlobals.*;

import org.javacc.utils.OutputFileGenerator;

import java.io.*;
import java.util.*;

public class CodeGenerator {
  protected StringBuffer mainBuffer = new StringBuffer();
  protected StringBuffer includeBuffer = new StringBuffer();
  protected StringBuffer staticsBuffer = new StringBuffer();
  protected StringBuffer outputBuffer = mainBuffer;

  public void genStringLiteralArrayCPP(String varName, String[] arr) {
    // First generate char array vars
    for (int i = 0; i < arr.length; i++) {
      genCodeLine("static const JJChar " + varName + "_arr_" + i + "[] = ");
      genStringLiteralInCPP(arr[i]);
      genCodeLine(";");
    }

    genCodeLine("static const JJString " + varName + "[] = {");
    for (int i = 0; i < arr.length; i++) {
      genCodeLine(varName + "_arr_" + i + ", ");
    }
    genCodeLine("};");
  }
  public void genStringLiteralInCPP(String s) {
    // String literals in CPP become char arrays
    outputBuffer.append("{");
    for (int i = 0; i < s.length(); i++) {
      outputBuffer.append("0x" + Integer.toHexString((int)s.charAt(i)) + ", ");
    }
    outputBuffer.append("0}");
  }
  public void genCodeLine(Object... code) {
    genCode(code);
    genCode("\n");
  }

  public void genCode(Object... code) {
    for (Object s: code) {
      outputBuffer.append(s);
    }
  }

  public void saveOutput(String fileName) {
    if (!isJavaLanguage()) {
      String incfilePath = fileName.replace(".cc", ".h");
      String incfileName = new File(incfilePath).getName();
      includeBuffer.insert(0, "#define " + incfileName.replace('.', '_').toUpperCase() + "\n");
      includeBuffer.insert(0, "#ifndef " + incfileName.replace('.', '_').toUpperCase() + "\n");

      // dump the statics into the main file with the code.
      mainBuffer.insert(0, staticsBuffer);

      // Finally enclose the whole thing in the namespace, if specified.
      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        mainBuffer.insert(0, "namespace " + Options.stringValue("NAMESPACE_OPEN") + "\n");
        mainBuffer.append(Options.stringValue("NAMESPACE_CLOSE") + "\n");
        includeBuffer.append(Options.stringValue("NAMESPACE_CLOSE") + "\n");
      }

      if (jjtreeGenerated) {
    	  mainBuffer.insert(0, "#include \"SimpleNode.h\"\n");
      }
      if(Options.getTokenManagerUsesParser())
    	  mainBuffer.insert(0, "#include \"" + cu_name + ".h\"\n");
      mainBuffer.insert(0, "#include \"TokenMgrError.h\"\n");
      mainBuffer.insert(0, "#include \"" + incfileName + "\"\n");
      includeBuffer.append("#endif\n");
      saveOutput(incfilePath, includeBuffer);
    }

    mainBuffer.insert(0, "/* " + new File(fileName).getName() + " */\n");
    saveOutput(fileName, mainBuffer);
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') ||
           (c >= 'a' && c <= 'f') ||
           (c >= 'A' && c <= 'F');
  }

  // HACK
  private void fixupLongLiterals(StringBuffer sb) {
    for (int i = 0; i < sb.length() - 1; i++) {
      int beg = i;
      char c1 = sb.charAt(i);
      char c2 = sb.charAt(i + 1);
      if (Character.isDigit(c1) || (c1 == '0' && c2 == 'x')) {
        i += c1 == '0' ? 2 : 1;
        while (isHexDigit(sb.charAt(i))) i++;
        if (sb.charAt(i) == 'L') {
          sb.insert(i, "UL");
        }
        i++;
      }
    }
  }

  public void saveOutput(String fileName, StringBuffer sb) {
    PrintWriter fw = null;
    if (!isJavaLanguage()) {
      fixupLongLiterals(sb);
    }
    try {
      File tmp = new File(fileName);
      fw = new PrintWriter(
              new BufferedWriter(
              new FileWriter(tmp),
              8092
          )
      );

      fw.print(sb.toString());
    } catch(IOException ioe) {
      JavaCCErrors.fatal("Could not create output file: " + fileName);
    } finally {
      if (fw != null) {
        fw.close();
      }
    }
  }

  protected int cline, ccol;

  protected void printTokenSetup(Token t) {
		Token tt = t;

		while (tt.specialToken != null) {
			tt = tt.specialToken;
		}

		cline = tt.beginLine;
		ccol = tt.beginColumn;
  }

  protected void printTokenList(List<Token> list) {
    Token t = null;
    for (Iterator<Token> it = list.iterator(); it.hasNext();) {
      t = (Token)it.next();
      printToken(t);
    }

    if (t != null)
      printTrailingComments(t);
  }

  protected void printTokenOnly(Token t) {
    genCode(getStringForTokenOnly(t));
  }

  protected String getStringForTokenOnly(Token t) {
    String retval = "";
    for (; cline < t.beginLine; cline++) {
      retval += "\n"; ccol = 1;
    }
    for (; ccol < t.beginColumn; ccol++) {
      retval += " ";
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL ||
        t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
       retval += addUnicodeEscapes(t.image);
    else
       retval += t.image;
    cline = t.endLine;
    ccol = t.endColumn+1;
    if (t.image.length() > 0) {
      char last = t.image.charAt(t.image.length()-1);
      if (last == '\n' || last == '\r') {
        cline++; ccol = 1;
      }
    }

    return retval;
  }

  protected void printToken(Token t) {
    genCode(getStringToPrint(t));
  }

  protected String getStringToPrint(Token t) {
    String retval = "";
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        retval += getStringForTokenOnly(tt);
        tt = tt.next;
      }
    }

    return retval + getStringForTokenOnly(t);
  }

  protected void printLeadingComments(Token t) {
    genCode(getLeadingComments(t));
  }

  protected String getLeadingComments(Token t) {
    String retval = "";
    if (t.specialToken == null) return retval;
    Token tt = t.specialToken;
    while (tt.specialToken != null) tt = tt.specialToken;
    while (tt != null) {
      retval += getStringForTokenOnly(tt);
      tt = tt.next;
    }
    if (ccol != 1 && cline != t.beginLine) {
      retval += "\n";
      cline++; ccol = 1;
    }

    return retval;
  }

  protected void printTrailingComments(Token t) {
    outputBuffer.append(getTrailingComments(t));
  }

  protected String getTrailingComments(Token t) {
    if (t.next == null) return "";
    return getLeadingComments(t.next);
  }

  /**
   * for testing
   */
  public String getGeneratedCode() {
    return outputBuffer.toString() + "\n";
  }

  /**
   * Generate annotation. @XX syntax for java, comments in C++
   */
  public void genAnnotation(String ann) {
    if (Options.isOutputLanguageJava()) {
      genCode("@" + ann);
    } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) { // For now, it's only C++ for now
      genCode( "/*" + ann + "*/");
    } else {
    	throw new RuntimeException("Unknown language : " + Options.getOutputLanguage());
    }
  }

  /**
   * Generate a modifier
   */
  public void genModifier(String mod) {
    String origMod = mod.toLowerCase(Locale.ENGLISH);
    if (isJavaLanguage()) {
      genCode(mod);
    } else { // For now, it's only C++ for now
      if (origMod.equals("public") || origMod.equals("private")) {
        genCode(origMod + ": ");
      }
      // we don't care about other mods for now.
    }
  }

  /**
   * Generate a class with a given name, an array of superclass and
   * another array of super interfaes
   */
  public void genClassStart(String mod, String name, String[] superClasses, String[] superInterfaces) {
    boolean isJavaLanguage = isJavaLanguage();
	if (isJavaLanguage && mod != null) {
       genModifier(mod);
    }
    genCode("class " + name);
    if (isJavaLanguage) {
      if (superClasses.length == 1 && superClasses[0] != null) {
        genCode(" extends " + superClasses[0]);
      }
      if (superInterfaces.length != 0) {
        genCode(" implements ");
      }
    } else {
      if (superClasses.length > 0 || superInterfaces.length > 0) {
        genCode(" : ");
      }

      genCommaSeperatedString(superClasses);
    }

    genCommaSeperatedString(superInterfaces);
    genCodeLine(" {");
    if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
      genCodeLine("public:");
    }
  }

  private void genCommaSeperatedString(String[] strings) {
    for (int i = 0; i < strings.length; i++) {
      if (i > 0) {
        genCode(", ");
      }

      genCode(strings[i]);
    }
  }

  protected boolean isJavaLanguage() {
	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    return Options.isOutputLanguageJava();
  }

  public void switchToMainFile() {
    outputBuffer = mainBuffer;
  }

  public void switchToStaticsFile() {
    if (!isJavaLanguage()) {
      outputBuffer = staticsBuffer;
    }
  }

  public void switchToIncludeFile() {
    if (!isJavaLanguage()) {
      outputBuffer = includeBuffer;
    }
  }

  public void generateMethodDefHeader(String modsAndRetType, String className, String nameAndParams) {
    generateMethodDefHeader(modsAndRetType, className, nameAndParams, null);
  }

  public void generateMethodDefHeader(String qualifiedModsAndRetType, String className, String nameAndParams, String exceptions) {
    // for C++, we generate the signature in the header file and body in main file
    if (isJavaLanguage()) {
      genCode(qualifiedModsAndRetType + " " + nameAndParams);
      if (exceptions != null) {
        genCode(" throws " + exceptions);
      }
      genCodeLine("");
    } else {
      includeBuffer.append(qualifiedModsAndRetType + " " + nameAndParams);
      //if (exceptions != null)
        //includeBuffer.append(" throw(" + exceptions + ")");
      includeBuffer.append(";\n");

      String modsAndRetType = null;
      int i = qualifiedModsAndRetType.lastIndexOf(':');
      if (i >= 0)
    	  modsAndRetType = qualifiedModsAndRetType.substring(i+1);

      if (modsAndRetType != null) {
    	  i = modsAndRetType.lastIndexOf("virtual");
    	  if (i >= 0)
    		  modsAndRetType = modsAndRetType.substring(i + "virtual".length());
      }
    	i = qualifiedModsAndRetType.lastIndexOf("virtual");
    	if (i >= 0)
    		qualifiedModsAndRetType = qualifiedModsAndRetType.substring(i + "virtual".length());
      mainBuffer.append("\n" + qualifiedModsAndRetType + " " +
                           getClassQualifier(className) + nameAndParams);
      //if (exceptions != null)
        //mainBuffer.append(" throw( " + exceptions + ")");
      switchToMainFile();
    }
  }

  protected String getClassQualifier(String className) {
    return className == null ? "" : className + "::";
  }

  public static String getCharStreamName() {
    if (Options.getUserCharStream()) {
      return "CharStream";
    } else {
      return Options.getJavaUnicodeEscape() ? "JavaCharStream"
                                            : "SimpleCharStream";
    }
  }
  @SuppressWarnings("unchecked")
  protected void writeTemplate(String name, Map<String, Object> options, Object... additionalOptions) throws IOException
  {

    // options.put("", .valueOf(maxOrdinal));


    for (int i = 0; i < additionalOptions.length; i++)
    {
      Object o = additionalOptions[i];

      if (o instanceof Map<?,?>)
      {
        options.putAll((Map<String,Object>) o);
      }
      else
      {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");

        options.put((String) o, additionalOptions[i+1]);
        i++;
      }
    }

    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    genCode(sw.toString());
  }
}
