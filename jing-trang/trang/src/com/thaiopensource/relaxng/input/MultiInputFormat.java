package com.thaiopensource.relaxng.input;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface MultiInputFormat extends InputFormat {
  SchemaCollection load(String[] uris, String[] params, String outputFormat, ErrorHandler eh)
          throws InputFailedException, InvalidParamsException, IOException, SAXException;
}
