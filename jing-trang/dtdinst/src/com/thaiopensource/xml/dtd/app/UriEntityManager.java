package com.thaiopensource.xml.dtd.app;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;

import com.thaiopensource.xml.em.*;
import com.thaiopensource.xml.util.EncodingMap;

public class UriEntityManager implements EntityManager {
  public OpenEntity open(ExternalId xid) throws IOException {
    String systemId = xid.getSystemId();
    String baseUri = xid.getBaseUri();
    URL u;
    if (baseUri != null)
      u = new URL(new URL(baseUri), systemId);
    else
      u = new URL(systemId);

    EncodingDetectInputStream in
      = new EncodingDetectInputStream(u.openStream());
    String enc = in.detectEncoding();
    String javaEnc = EncodingMap.getJavaName(enc);
    return new OpenEntity(new BufferedReader(new InputStreamReader(in,
								   javaEnc)),
			  u.toString(),
			  u.toString(),
			  enc);
  }

  public static String commandLineArgToUri(String arg) {
    if (!hasScheme(arg)) {
      try {
	return fileToUri(arg);
      }
      catch (MalformedURLException e) { }
    }
    return arg;
  }

  private static boolean hasScheme(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    if (!isAlpha(str.charAt(0)))
      return false;
    for (int i = 1; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case ':':
	// Don't recognize single letters as schemes
	return i == 1 ? false : true;
      case '+':
      case '-':
	break;
      default:
	if (!isAlnum(c))
	  return false;
	break;
      }
    }
    return false;
  }

  private static boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  private static boolean isAlnum(char c) {
    return isAlpha(c) || ('0' <= c && c <= '9');
  }

  private static String fileToUri(String file) throws MalformedURLException {
    String path
      = new File(file).getAbsolutePath().replace(File.separatorChar, '/');
    if (path.length() > 0 && path.charAt(0) != '/')
      path = '/' + path;
    return new URL("file", "", path).toString();
  }
}
