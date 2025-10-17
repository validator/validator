package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.xml.util.Name;

/**
 * A PropertyId whose value is constrained to be an instance of
 * Name.
 *
 * @see Name
 */

public class NamePropertyId extends PropertyId {
   public NamePropertyId(String name) {
      super(name, Name.class);
    }

  /**
   * Returns the value of the property.  This is a typesafe
   * version of <code>PropertyMap.get</code>.
   *
   * @param properties the PropertyMap to be used
   * @return the Name to which the PropertyMap maps this PropertyId,
   * or <code>null</code> if this PropertyId is not in the PropertyMap
   * @see com.thaiopensource.util.PropertyMap#get
   */
  public Name get(PropertyMap properties) {
    return (Name)properties.get(this);
  }

  /**
   * Sets the value of the property. Modifies the PropertyMapBuilder
   * so that this PropertyId is mapped to the specified value. This
   * is a typesafe version of PropertyMapBuilder.put.
   *
   * @param builder the PropertyMapBuilder to be modified
   * @param value the Name to which this PropertyId is to be mapped
   * @return the Name to which this PropertyId was mapped before,
   * or <code>null</code> if it was not mapped
   *
   * @see com.thaiopensource.util.PropertyMapBuilder#put
   */
  public Name put(PropertyMapBuilder builder, Name value) {
    return (Name)builder.put(this, value);
  }
}
