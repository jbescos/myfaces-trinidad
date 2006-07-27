/*
 * Copyright  2002-2006 The Apache Software Foundation.
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
package org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.jsLibs;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.util.IntegerUtils;


/**
 * Scriptlet for adding date formatting information.

 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/xhtml/jsLibs/DateFormatInfoScriptlet.java#0 $) $Date: 10-nov-2005.19:02:45 $
 * @author The Oracle ADF Faces Team
 */
class DateFormatInfoScriptlet extends Scriptlet
{
  static public final String DATE_FORMAT_INFO_KEY = "DateFormatInfo";

  static public Scriptlet sharedInstance()
  {
    return _sInstance;
  }

  private DateFormatInfoScriptlet()
  {
  }

  public Object getScriptletKey()
  {
    return DATE_FORMAT_INFO_KEY;
  }

  protected void outputScriptletContent(
    FacesContext        context,
    RenderingContext arc) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    writer.writeText("var _df2DYS=", null);
    int twoDigitYearStart =
      arc.getLocaleContext().getDateFormatContext().getTwoDigitYearStart();
    writer.writeText(IntegerUtils.getString(twoDigitYearStart), null);
    writer.writeText(";", null);
  }

  static private final Scriptlet _sInstance =
    new DateFormatInfoScriptlet();
}


