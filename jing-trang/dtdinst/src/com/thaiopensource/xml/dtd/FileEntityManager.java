package com.thaiopensource.xml.dtd;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class FileEntityManager implements EntityManager {
  public OpenEntity open(String systemId, String baseUri, String publicId)
    throws IOException {
    File file = new File(systemId);
    if (!file.isAbsolute() && baseUri != null) {
      String dir = new File(baseUri).getParent();
      if (dir != null)
	file = new File(dir, systemId);
    }
    return new OpenEntity(new BufferedReader(new InputStreamReader(new FileInputStream(file))),
			  file.toString(),
			  file.toString());
  }
}
