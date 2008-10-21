package com.thaiopensource.datatype;

import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.DatatypeLibrary;

import java.util.Enumeration;

import com.thaiopensource.util.Service;

// We use this instead of the one in org.relaxng.datatype.helper because tools.jar in Java 6 includes
// org.relaxng.datatype, which messes up class loading for the jing task in Ant, when Ant's class loader's
// parent will have tools.jar in its classpath.
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
