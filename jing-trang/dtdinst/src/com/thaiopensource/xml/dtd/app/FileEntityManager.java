package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.thaiopensource.xml.em.*;
import com.thaiopensource.xml.util.EncodingMap;

public class FileEntityManager implements EntityManager {
  public OpenEntity open(ExternalId xid) throws IOException {
    String systemId = xid.getSystemId();
    File file = new File(systemId);
    if (!file.isAbsolute()) {
      String baseUri = xid.getBaseUri();
      if (baseUri != null) {
	String dir = new File(baseUri).getParent();
	if (dir != null)
	  file = new File(dir, systemId);
      }
    }
    EncodingDetectInputStream in
      = new EncodingDetectInputStream(new FileInputStream(file));
    String enc = in.detectEncoding();
    String javaEnc = EncodingMap.getJavaName(enc);
    return new OpenEntity(new BufferedReader(new InputStreamReader(in,
								   javaEnc)),
			  file.toString(),
			  file.toString(),
			  enc);
  }
}
