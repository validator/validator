package org.javacc.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.javacc.parser.JavaFiles.JavaResourceTemplateLocationImpl;
import org.javacc.parser.Options;

import junit.framework.TestCase;

public class OutputFileGeneratorTest extends TestCase {

  public void testStringBuffer() throws IOException {
    Options.init();

    JavaResourceTemplateLocationImpl impl = new JavaResourceTemplateLocationImpl();
    OutputFileGenerator generator = new OutputFileGenerator(
        impl.getParseExceptionTemplateResourceUrl(), new HashMap<>());
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    generator.generate(writer);

    assertTrue(stringWriter.toString().contains("StringBuffer"));
    assertFalse(stringWriter.toString().contains("StringBuilder"));
  }

  public void testStringBuilder() throws IOException {
    Options.init();
    Options.setCmdLineOption(Options.USEROPTION__GENERATE_STRING_BUILDER);

    JavaResourceTemplateLocationImpl impl = new JavaResourceTemplateLocationImpl();
    OutputFileGenerator generator = new OutputFileGenerator(
        impl.getParseExceptionTemplateResourceUrl(), new HashMap<>());

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    generator.generate(writer);

    assertTrue(stringWriter.toString().contains("StringBuilder"));
    assertFalse(stringWriter.toString().contains("StringBuffer"));
  }
}
