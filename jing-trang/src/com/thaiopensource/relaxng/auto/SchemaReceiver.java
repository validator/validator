package com.thaiopensource.relaxng.auto;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

import com.thaiopensource.relaxng.auto.SchemaFuture;

public interface SchemaReceiver {
  SchemaFuture installHandlers(XMLReader xr) throws SAXException;
}
