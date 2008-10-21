package com.thaiopensource.relaxng.impl;

import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;

import java.io.IOException;

public interface PatternFuture {
  Pattern getPattern(boolean isAttributesPattern) throws IllegalSchemaException, SAXException, IOException;
}
