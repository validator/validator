package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;

interface KeyChecker {
  void checkKey(String name, Object value) throws SAXException;
  void checkKeyRef(String name, Object value);
}
