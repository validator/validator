package com.thaiopensource.datatype;

import org.xml.sax.XMLReader;

public interface DatatypeReader {
  void start(XMLReader xr);
  Datatype end();
}
