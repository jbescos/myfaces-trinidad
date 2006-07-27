/*
 * Copyright  2003-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.trinidadinternal.skin;

import java.io.IOException;

import java.util.List;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;

import org.apache.myfaces.trinidadinternal.share.io.CachingNameResolver;
import org.apache.myfaces.trinidadinternal.share.io.InputStreamProvider;
import org.apache.myfaces.trinidadinternal.share.io.NameResolver;
import org.apache.myfaces.trinidadinternal.share.xml.JaxpXMLProvider;
import org.apache.myfaces.trinidadinternal.share.xml.ParseContextImpl;
import org.apache.myfaces.trinidadinternal.share.xml.XMLProvider;

import org.apache.myfaces.trinidadinternal.style.StyleContext;
import org.apache.myfaces.trinidadinternal.style.xml.StyleSheetDocumentUtils;

import org.apache.myfaces.trinidadinternal.style.xml.parse.StyleSheetDocument;
import org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.IconNode;
import org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.SkinPropertyNode;


/**
 * Package-private utility class used by Skin implementation
 * to manage a single XSS or CSS skin stylesheet source file.
 * This class could actually
 * be pushed into an inner class in Skin, but at the moment
 * it is separated out simply to reduce the amount of code in
 * Skin.java.
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/skin/StyleSheetEntry.java#0 $) $Date: 10-nov-2005.18:59:01 $
 * @author The Oracle ADF Faces Team
 */
class StyleSheetEntry
{
  /**
   * Creates a StyleSheetEntry for the specified context/name.
   * This method will log any errors/exceptions and return
   * null if the style sheet source file could not be found/parsed.
   */
  public static StyleSheetEntry createEntry(
    StyleContext     context,
    String           styleSheetName
    )
  {
    // In order to create the StyleSheetEntry, we need to locate and
    // parse the style sheet file.  We use a NameResolver to use to
    // find the style sheet.
    NameResolver resolver = _getNameResolver(context);
    if (resolver == null)
      return null;

    // a private static inner class to store the document, icon, and skin properties
    // =-=jmw @todo Should I just create a StyleSheetEntry directly,
    // and make the constructor public? (probably)
    StyleSheetEntry skinStyleSheet = _createSkinStyleSheet(resolver,
                                                           styleSheetName);


      if (skinStyleSheet == null)
        return null;

      // We either create a plain old StyleSheetEntry or a special
      // subclass of StyleSheetEntry that can check for modifications
      // depending on the Configuration settings
      if (context.checkStylesModified())
        return new CheckModifiedEntry(styleSheetName,
                                      skinStyleSheet.getDocument(),
                                      skinStyleSheet.getIcons(),
                                      skinStyleSheet.getSkinProperties(),
                                      resolver);

      return skinStyleSheet;



  }

  // Creates a StyleSheetEntry which never checks for
  // modifications.
  // =-=jmw there is a hotspot bug I filed with JDeveloper
  // 4102252 when this is private I get an IllegalAccessException
  // in CheckModifiedEntry. changing it to package private
  StyleSheetEntry(
    String                 styleSheetName,
    StyleSheetDocument     document,
    List <IconNode>        icons,
    List <SkinPropertyNode> skinProperties

    )
  {
    _name       = styleSheetName;
    _document   = document;
    _icons      = icons;
    _skinProperties = skinProperties;

  }

  StyleSheetEntry(String styleSheetName)
  {
    this(styleSheetName, null, null, null);
  }

  // Use full constructor
  private StyleSheetEntry()
  {
  }

  /**
   * Returns the name of the style sheet source file
   * for this StyleSheetEntry.
   */
  public String getStyleSheetName()
  {
    return _name;
  }

  /**
   * Returns the StyleSheetDocument for this
   * StyleSheetEntry.
   */
  public StyleSheetDocument getDocument()
  {
    return _document;
  }

  /**
   * Returns the icons List for this
   * StyleSheetEntry. (this is a list of IconNodes )
   */
  public List <IconNode> getIcons()
  {
    return _icons;
  }

  /**
   * Returns the SkinProperties List for this
   * StyleSheetEntry. This is a list of SkinProperyNodes
   * a node contains the selector, the -ora- property, and the value.
   * e.g, selector: af|navigationPath, property: -ora-show-last-item,
   * value: true
   */
  public List <SkinPropertyNode> getSkinProperties()
  {
    return _skinProperties;
  }

  /**
   * Checks whether the underlying style sheet source file
   * has been modified and if so, reloads the StyleSheetDocument.
   * Returns true if the document has been modified (and the
   * StyleSheetDocument has been reloaded).
   */
  public boolean checkModified(StyleContext context)
  {
    return false;
  }

  // Called by CheckModifiedEntry when the style sheet has changed
  void __setDocument(StyleSheetDocument document)
  {
    _document = document;
  }

  // Called by CheckModifiedEntry when the style sheet has changed
  void __setIcons(List <IconNode> icons)
  {
    _icons = icons;
  }

  // Called by CheckModifiedEntry when the style sheet has changed
  void __setSkinProperties(List <SkinPropertyNode> skinProperties)
  {
    _skinProperties = skinProperties;
  }

  // Creates the SkinStyleSheet (a private static inner class that
  // contains StyleSheetDocument plus a list
  // of icons and properties) from a CSS file
  //
  private static StyleSheetEntry _createSkinStyleSheet(
    NameResolver     resolver,
    String           styleSheetName
    )
  {

    StyleSheetEntry skinStyleSheet;

    if (!styleSheetName.endsWith(".css"))
    {

      // Parse the style sheet to create the StyleSheetDocument
      StyleSheetDocument document = _createStyleSheetDocument(resolver,
                                                              styleSheetName);
      if (document == null)
        skinStyleSheet = null;
      else
      {
        skinStyleSheet = new StyleSheetEntry(styleSheetName,
                                             document,
                                             null,
                                             null);
      }

    }
    else
    {
      // this will parse a skin css file which allows icons, properties,
      // and styles.
        skinStyleSheet =  _createSkinStyleSheetFromCSS(resolver,
                                                       styleSheetName);

    }

    return skinStyleSheet;

  }


  // Creates the StyleSheetEntry
  private static StyleSheetEntry _createSkinStyleSheetFromCSS(
    NameResolver     resolver,
    String           styleSheetName
    )
  {

     try
     {
        ParseContextImpl parseContext = new ParseContextImpl();
        // if this is a utility that isn't in this file, then I can't return a SkinStyleSheet.
        // I think instead this parseCSSSource should return a new instance of StyleSheetEntry.
        return (StyleSheetEntry)SkinStyleSheetParserUtils.parseCSSSource(
                                    parseContext,
                                    resolver,
                                    styleSheetName,
                                    StyleSheetEntry.class);
     }
     catch (Exception e)
     {
       if (_LOG.isSevere())
         _LOG.severe("Could not load style sheet: " + styleSheetName, e);

     }
      return null;
  }

  // Creates the StyleSheetDocument
  private static StyleSheetDocument _createStyleSheetDocument(
    NameResolver     resolver,
    String           styleSheetName
    )
  {

    XMLProvider xmlProvider = new JaxpXMLProvider();

    try
    {
      return StyleSheetDocumentUtils.createStyleSheetDocument(xmlProvider,
                                                              resolver,
                                                              styleSheetName);
    }
    catch (Exception e)
    {
      if (_LOG.isSevere())
        _LOG.severe("Could not load style sheet: " + styleSheetName, e);
    }

    return null;
  }



  // Returns the NameResolver to use for locating style sheet files
  private static NameResolver _getNameResolver(
    StyleContext context
    )
  {
    // First, get a NameResolver that we can use to resolve
    // locate the style sheet file.
    NameResolver resolver = StyleSheetNameResolver.createResolver(context);
    if (resolver == null)
    {
      // If we can't get a NameResolver, something is seriously wrong.
      // createResolver() logged the error already, so just return null.
      return null;
    }

    // Wrap up the resolver in a CachingNameResolver that we can
    // can use to check for updates to imported style sheets
    return new CachingNameResolver(resolver, null, true);
  }


  // Subclass of StyleSheetEntry which checks for updates
  // to the underlying style sheet files.
  private static class CheckModifiedEntry extends StyleSheetEntry
  {
    public CheckModifiedEntry(
      String             styleSheetName,
      StyleSheetDocument document,
      List               icons,
      List               properties,
      NameResolver       resolver
      )
    {
      super(styleSheetName, document, icons, properties);

      // We need the InputStreamProvider in order to check
      // for modifications.  Get it from the NameResolver.
      _provider = _getInputStreamProvider(resolver);
    }

    // Override of checkModified() which uses the
    // InputStreamProvider to check for changes to the
    // style sheet source files.
    public boolean checkModified(StyleContext context)
    {
      // We would synchronize here, but at the moment synchronization
      // is provided by Skin.getStyleSheetDocument().
      if ((_provider != null) && (_provider.hasSourceChanged()))
      {
        // Throw away the old InputStreamProvider and StyleSheetDocument
        _provider = null;
        __setDocument(null);
        __setIcons(null);
        __setSkinProperties(null);

        // Get a new NameResolver
        NameResolver resolver = _getNameResolver(context);
        if (resolver != null)
        {
          String name = getStyleSheetName();

          StyleSheetEntry skinStyleSheet = _createSkinStyleSheet(resolver,
                                                                 name);

          if (skinStyleSheet != null)
          {
            _provider = _getInputStreamProvider(resolver);
            __setDocument(skinStyleSheet.getDocument());
            __setIcons(skinStyleSheet.getIcons());
            __setSkinProperties(skinStyleSheet.getSkinProperties());
          }

          return true;
        }
      }

      return false;
    }

    private InputStreamProvider _getInputStreamProvider(
      NameResolver resolver
      )
    {
      // Note: We assume that we are using a CachingNameResolver,
      // and that the InputStreamProvider for the source file has
      // already been retrieved.  That way, when we call
      // NameResolver.getProvider(), we are actually getting
      // the same InputStreamProvider that was used to read the
      // style sheet earlier.

      assert (resolver instanceof CachingNameResolver);

      try
      {
        return resolver.getProvider(getStyleSheetName());
      }
      catch (IOException e)
      {
        // We shouldn't get here - we know we were able to
        // get the InputStreamProvider before - so we should be
        // able to get the cached InputStreamProvider now
        assert false;
      }

      return null;
    }

    private InputStreamProvider _provider;
  }

  private String              _name;
  private StyleSheetDocument  _document;
  // List of IconNodes
  private List <IconNode> _icons;
  // List of -ora- properties that the skin can be set on the skin.
  // This is a List of SkinPropertyNodes
  // jmw it will be key, name, value. or name/value???
  private List <SkinPropertyNode> _skinProperties;

  private static final TrinidadLogger _LOG = TrinidadLogger.createTrinidadLogger(StyleSheetEntry.class);

}
