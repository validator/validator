package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface OutputFormat {
  void output(SchemaCollection sc, OutputDirectory od, ErrorHandler eh) throws SAXException, IOException, OutputFailedException;
}
