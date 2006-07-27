/*
 * Copyright  2004-2006 The Apache Software Foundation.
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

package org.apache.myfaces.trinidadinternal.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import org.apache.myfaces.trinidad.logging.TrinidadLogger;
import org.apache.myfaces.trinidad.resource.RegexResourceLoader;
import org.apache.myfaces.trinidad.resource.ResourceLoader;

import org.apache.myfaces.trinidad.util.ClassLoaderUtils;

/**
 * A resource loader implementation which loads resources
 * for the rich renderkit.
 *
 * @author The Oracle ADF Faces Team
 *
 * @todo Dynamic version number
 */
public class CoreRenderKitResourceLoader extends RegexResourceLoader
{
  public CoreRenderKitResourceLoader(ResourceLoader parent)
  {
    register("(/.*/Common.*\\.js)",
             new CoreCommonScriptsResourceLoader(_getCommonLibraryURI(false),
                                                 false));
    register("(/.*/DebugCommon.*\\.js)",
             new CoreCommonScriptsResourceLoader(_getCommonLibraryURI(true),
                                                 true));

    register("(/.*\\.(css|jpg|gif|png|jpeg|js))",
             new CoreClassLoaderResourceLoader(parent));
  }

  static private String _getCommonLibraryURI(boolean debug)
  {
    StringBuffer base = new StringBuffer(debug
                                         ? "/adf/jsLibs/DebugCommon"
                                         : "/adf/jsLibs/Common");
    return base.append(_VERSION)
               .append(".js")
               .toString();
  }

  static public String __getVersion()
  {
    return _VERSION;
  }

  // Path to ResourceServlet
  // Version string to append to library, style sheet URIs
  static private final String _VERSION;

  static private final TrinidadLogger _LOG =
                          TrinidadLogger.createTrinidadLogger(CoreRenderKitResourceLoader.class);

  static
  {
    // Note: Java Package versioning is useless during development when
    //       we have no JARs, whereas this technique works with non-JAR
    //       classpaths as well.
    String version = "unknown";

    try
    {
      URL resource =
        ClassLoaderUtils.getResource("META-INF/adf-faces-version.txt");
      if (resource != null)
      {
        InputStream in = null;

        try
        {
          in = resource.openStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          version = br.readLine();
        }
        catch (IOException e)
        {
          _LOG.severe(e);
        }
        finally
        {
          if (in != null)
            in.close();
        }
      }
    }
    catch (IOException e)
    {
      _LOG.severe(e);
    }
    finally
    {
      _VERSION = version;
    }
  }
}
