package com.thaiopensource.relaxng.input;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface InputFormat {
  SchemaCollection load(String uri, String encoding, ErrorHandler eh) throws InputFailedException, IOException, SAXException;
}
