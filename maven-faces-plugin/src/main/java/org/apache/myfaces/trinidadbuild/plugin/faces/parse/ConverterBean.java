/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.trinidadbuild.plugin.faces.parse;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

/**
 * ConverterBean is a Java representation of the faces-config converter
 * XML element.
 */
public class ConverterBean extends ObjectBean
{
  /**
   * Creates a new ConverterBean.
   */
  public ConverterBean()
  {
    _properties = new LinkedHashMap();
  }

  /**
   * Sets the converter identifer for this component.
   *
   * @param converterId  converter identifer
   */
  public void setConverterId(
    String converterId)
  {
    _converterId = converterId;
  }

  /**
   * Returns true if the converter identifier is specified, otherwise false.
   *
   * @return  true if the converter identifier is specified,
   *          otherwise false.
   */
  public boolean hasConverterId()
  {
    return (_converterId != null);
  }

  /**
   * Returns the converter identifier for this component.
   *
   * @return  the converter identifier
   */
  public String getConverterId()
  {
    return _converterId;
  }

  /**
   * Sets the converter class for this component.
   *
   * @param converterClass  the converter class
   */
  public void setConverterClass(
    String converterClass)
  {
    _converterClass = converterClass;
  }

  /**
   * Returns the converter class for this component.
   *
   * @return  the converter class
   */
  public String getConverterClass()
  {
    return _converterClass;
  }

  /**
   * Sets the description of this property.
   *
   * @param description  the property description
   */
  public void setDescription(
    String description)
  {
    _description = description;
  }

  /**
   * Returns the description of this property.
   *
   * @return  the property description
   */
  public String getDescription()
  {
    return _description;
  }

  /**
   * Sets the long description of this property.
   *
   * @param longDescription  the long property description
   */
  public void setLongDescription(
    String longDescription)
  {
    _longDescription = longDescription;
  }

  /**
   * Returns the long description of this property.
   *
   * @return  the long property description
   */
  public String getLongDescription()
  {
    return _longDescription;
  }

  /**
   * Sets the JSP tag handler class for this component.
   *
   * @param tagClass  the JSP tag handler class
   */
  public void setTagClass(
    String tagClass)
  {
    _tagClass = tagClass;
  }

  /**
   * Returns the JSP tag handler class for this component.
   *
   * @return  the JSP tag handler class
   */
  public String getTagClass()
  {
    return _tagClass;
  }

  /**
   * Returns the JSP tag name for this component.
   *
   * @return  the JSP tag name
   */
  public QName getTagName()
  {
    return _tagName;
  }

  /**
   * Sets the JSP tag name for this component.
   *
   * @param tagClass  the JSP tag name
   */
  public void setTagName(
    QName tagName)
  {
    _tagName = tagName;
  }

  /**
   * Sets the converter super class for this component.
   *
   * @param converterSuperClass  the converter super class
   */
  public void setConverterSuperClass(
    String converterSuperClass)
  {
    _converterSuperClass = converterSuperClass;
  }

  /**
   * Returns the converter super class for this component.
   *
   * @return  the converter super class
   */
  public String getConverterSuperClass()
  {
    return _converterSuperClass;
  }

  /**
   * Adds a property to this component.
   *
   * @param property  the property to add
   */
  public void addProperty(
    PropertyBean property)
  {
    _properties.put(property.getPropertyName(), property);
  }

  /**
   * Returns the property for this property name.
   *
   * @param propertyName  the property name to find
   */
  public PropertyBean findProperty(
    String propertyName)
  {
    return (PropertyBean)_properties.get(propertyName);
  }

  /**
   * Returns true if this component has any properties.
   *
   * @return  true   if this component has any properties,
   *          false  otherwise
   */
  public boolean hasProperties()
  {
    return !_properties.isEmpty();
  }

  /**
   * Returns an iterator for all properties on this component only.
   *
   * @return  the property iterator
   */
  public Iterator properties()
  {
    return _properties.values().iterator();
  }

  /**
   * Adds a Java Language class modifier to the converter class.
   *
   * @param modifier  the modifier to be added
   */
  public void addConverterClassModifier(
    int modifier)
  {
    _converterClassModifiers |= modifier;
  }

  /**
   * Returns the Java Language class modifiers for the converter class.
   * By default, these modifiers include Modifier.PUBLIC.
   *
   * @return  the Java Language class modifiers for the converter class
   */
  public int getConverterClassModifiers()
  {
    int modifiers = _converterClassModifiers;

    if (!Modifier.isPrivate(modifiers) &&
        !Modifier.isProtected(modifiers) &&
        !Modifier.isPublic(modifiers))
    {
      modifiers |= Modifier.PUBLIC;
    }

    return modifiers;
  }

  public void parseConverterClassModifier(
    String modifier)
  {
    addConverterClassModifier(_parseModifier(modifier));
  }

  public void parseTagClassModifier(
    String modifier)
  {
    addTagClassModifier(_parseModifier(modifier));
  }

  private int _parseModifier(
    String text)
  {
    if ("public".equals(text))
      return Modifier.PUBLIC;
    else if ("protected".equals(text))
      return Modifier.PROTECTED;
    else if ("private".equals(text))
      return Modifier.PRIVATE;
    else if ("abstract".equals(text))
      return Modifier.ABSTRACT;
    else if ("final".equals(text))
      return Modifier.FINAL;

    throw new IllegalArgumentException("Unrecognized modifier: " + text);
  }

  /**
   * Adds a Java Language class modifier to the tag class.
   *
   * @param modifier  the modifier to be added
   */
  public void addTagClassModifier(
    int modifier)
  {
    _tagClassModifiers |= modifier;
  }

  /**
   * Returns the Java Language class modifiers for the tag class.
   * By default, these modifiers include Modifier.PUBLIC.
   *
   * @return  the Java Language class modifiers for the tag class
   */
  public int getTagClassModifiers()
  {
    int modifiers = _tagClassModifiers;

    if (!Modifier.isPrivate(modifiers) &&
        !Modifier.isProtected(modifiers) &&
        !Modifier.isPublic(modifiers))
    {
      modifiers |= Modifier.PUBLIC;
    }

    return modifiers;
  }

  private String  _description;
  private String  _longDescription;
  private String  _converterId;
  private String  _converterClass;
  private String  _converterSuperClass;
  private QName   _tagName;
  private String  _tagClass;
  private Map     _properties;
  private int     _converterClassModifiers;
  private int     _tagClassModifiers;


  static private final Logger _LOG = Logger.getLogger(ConverterBean.class.getName());
}
