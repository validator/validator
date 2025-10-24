/* Copyright (c) 2008, Paul Cager.
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

package org.javacc.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.javacc.parser.Options;

/**
 * Generates boiler-plate files from templates. Only very basic
 * template processing is supplied - if we need something more
 * sophisticated I suggest we use a third-party library.
 *
 * @author paulcager
 * @since 4.2
 */
public class OutputFileGenerator {

  /**
   * @param templateName the name of the template. E.g.
   *        "/templates/Token.template".
   * @param options the processing options in force, such
   *        as "STATIC=yes"
   */
  public OutputFileGenerator(String templateName, Map options) {
    this.templateName = templateName;
    this.options = options;
  }

  private final String templateName;
  private final Map options;

  private String currentLine;

  /**
   * Generate the output file.
   * @param out
   * @throws IOException
   */
  public void generate(PrintWriter out) throws IOException
  {
    InputStream is = getClass().getResourceAsStream(templateName);
    if (is == null)
      throw new IOException("Invalid template name: " + templateName);
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    process(in, out, false);
  }

  private String peekLine(BufferedReader in) throws IOException
  {
    if (currentLine == null)
      currentLine = in.readLine();

    return currentLine;
  }

  private String getLine(BufferedReader in) throws IOException
  {
    String line = currentLine;
    currentLine = null;

    if (line == null)
      in.readLine();

    return line;
  }

  private boolean evaluate(String condition)
  {
    condition = condition.trim();

    try
    {
      return new ConditionParser(new StringReader(condition)).CompilationUnit(options);
    }
    catch (ParseException e)
    {
      return false;
    }
  }

  private String substitute(String text) throws IOException
  {
    int startPos;

    if ( (startPos = text.indexOf("${")) == -1)
    {
      return text;
    }

    // Find matching "}".
    int braceDepth = 1;
    int endPos = startPos + 2;

    while ( endPos < text.length() && braceDepth > 0)
    {
      if (text.charAt(endPos) == '{')
        braceDepth++;
      else if (text.charAt(endPos) == '}')
        braceDepth--;

      endPos++;
    }

    if (braceDepth != 0)
      throw new IOException("Mismatched \"{}\" in template string: " + text);

    final String variableExpression = text.substring(startPos + 2, endPos - 1);

    // Find the end of the variable name
    String value = null;

    for (int i = 0; i < variableExpression.length(); i++)
    {
      char ch = variableExpression.charAt(i);

      if (ch == ':' && i < variableExpression.length() - 1 && variableExpression.charAt(i+1) == '-' )
      {
        value = substituteWithDefault(variableExpression.substring(0, i), variableExpression.substring(i + 2));
        break;
      }
      else if (ch == '?')
      {
        value = substituteWithConditional(variableExpression.substring(0, i), variableExpression.substring(i + 1));
        break;
      }
      else if (ch != '_' && !Character.isJavaIdentifierPart(ch))
      {
        throw new IOException("Invalid variable in " + text);
      }
    }

    if (value == null)
    {
      value = substituteWithDefault(variableExpression, "");
    }

    return text.substring(0, startPos) + value + text.substring(endPos);
  }

  /**
   * @param substring
   * @param defaultValue
   * @return
   * @throws IOException
   */
  private String substituteWithConditional(String variableName, String values) throws IOException
  {
    // Split values into true and false values.

    int pos = values.indexOf(':');
    if (pos == -1)
      throw new IOException("No ':' separator in " + values);

    if (evaluate(variableName))
      return substitute(values.substring(0, pos));
    else
      return substitute(values.substring(pos + 1));
  }

  /**
   * @param variableName
   * @param defaultValue
   * @return
   */
  private String substituteWithDefault(String variableName, String defaultValue) throws IOException
  {
    Object obj = options.get(variableName.trim());
    if (obj == null || obj.toString().length() == 0)
      return substitute(defaultValue);

    return obj.toString();
  }

  private void write(PrintWriter out, String text) throws IOException
  {
    while ( text.indexOf("${") != -1)
    {
    text = substitute(text);
    }

    if (Options.isOutputLanguageJava() && Options.getGenerateStringBuilder()) {
        text = text.replace("StringBuffer", "StringBuilder");
    }

    // TODO :: Added by Sreenivas on 12 June 2013 for 6.0 release, merged in to 6.1 release for sake of compatibility by cainsley ... This needs to be removed urgently!!!
    if (text.startsWith("\\#")) { // Hack to escape # for C++
      text = text.substring(1);
    }
    out.println(text);
  }

  private void process(BufferedReader in, PrintWriter out, boolean ignoring)
      throws IOException {
    //    out.println("*** process ignore=" + ignoring + " : " + peekLine(in));
    while ( peekLine(in) != null)
    {
      if (peekLine(in).trim().startsWith("#if"))
      {
        processIf(in, out, ignoring);
      }
      else if (peekLine(in).trim().startsWith("#"))
      {
        break;
      }
      else
      {
        String line = getLine(in);
        if (!ignoring) write(out, line);
      }
    }

    out.flush();
  }

  private void processIf(BufferedReader in, PrintWriter out, boolean ignoring)  throws IOException
  {
        String line = getLine(in).trim();
    assert line.trim().startsWith("#if");
    boolean foundTrueCondition = false;

    boolean condition = evaluate(line.substring(3).trim());
    while (true)
    {
      process(in, out, ignoring || foundTrueCondition || !condition);
      foundTrueCondition |= condition;

      if (peekLine(in) == null || !peekLine(in).trim().startsWith("#elif"))
        break;

      condition = evaluate(getLine(in).trim().substring(5).trim());
    }

        if (peekLine(in) != null && peekLine(in).trim().startsWith("#else"))
        {
          getLine(in);   // Discard the #else line
      process(in, out, ignoring || foundTrueCondition);
        }

        line = getLine(in);

        if (line == null)
          throw new IOException("Missing \"#fi\"");

        if (!line.trim().startsWith("#fi"))
          throw new IOException("Expected \"#fi\", got: " + line);
      }


  public static void main(String[] args) throws Exception
  {
    Map map = new HashMap();
    map.put("falseArg", Boolean.FALSE);
    map.put("trueArg", Boolean.TRUE);
    map.put("stringValue", "someString");

    new OutputFileGenerator(args[0], map).generate(new PrintWriter(args[1]));
  }

  public static void generateFromTemplate(
      String template, Map<String, Object> options,
      String outputFileName) throws IOException {
    OutputFileGenerator gen = new OutputFileGenerator(template, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    PrintWriter fw = null;
    try {
      File tmp = new File(outputFileName);
      fw = new PrintWriter(
              new BufferedWriter(
              new FileWriter(tmp),
              8092
          )
      );

      fw.print(sw.toString());
    } finally {
      if (fw != null) {
        fw.close();
      }
    }
  }
}
