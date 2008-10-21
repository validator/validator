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

}
