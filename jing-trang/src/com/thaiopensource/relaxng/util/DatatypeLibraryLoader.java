package com.thaiopensource.relaxng.util;

import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.DatatypeLibrary;
import com.thaiopensource.util.Service;
import java.util.Enumeration;

public class DatatypeLibraryLoader implements DatatypeLibraryFactory {
  private final Service service = new Service(DatatypeLibraryFactory.class);

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    for (Enumeration e = service.getProviders();
	 e.hasMoreElements();) {
      DatatypeLibraryFactory factory
	= (DatatypeLibraryFactory)e.nextElement();
      DatatypeLibrary library = factory.createDatatypeLibrary(uri);
      if (library != null)
	return library;
    }
    return null;
  }
  
}

