/*
 * Copyright  2000-2006 The Apache Software Foundation.
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
package org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.table;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.component.core.data.CoreColumn;
import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.renderkit.core.CoreRenderer;
import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.XhtmlConstants;


public class SelectionColumnRenderer extends SpecialColumnRenderer
{

  protected void renderKids(FacesContext          context,
                            RenderingContext   arc,
                            TableRenderingContext trc,
                            UIComponent           column) throws IOException
  {
    UIComponent table = trc.getTable();
    delegateRenderer(context, arc, table,
                     getFacesBean(table), 
                     trc.hasSelectAll()
                     ? _multiRenderer
                     : _singleRenderer);
  }
  

  protected String getHeaderText(FacesBean bean)
  {
    RenderingContext arc = RenderingContext.getCurrentInstance();
    TableRenderingContext tContext =
      TableRenderingContext.getCurrentInstance();

    String key = _isMultipleSelection(tContext) 
     ? "af_tableSelectMany.SELECT_COLUMN_HEADER"
     : "af_tableSelectOne.SELECT_COLUMN_HEADER";
    
    return arc.getTranslatedString(key);
  }  

  protected boolean getNoWrap(FacesBean bean)
  {
    return false;
  }


  protected String getFormatType(FacesBean bean)
  {
    return CoreColumn.ALIGN_CENTER;
  }

  protected String getHeaderStyleClass(TableRenderingContext tContext)
  {
    return XhtmlConstants.AF_COLUMN_HEADER_ICON_STYLE;
  }

  static private boolean _isMultipleSelection(TableRenderingContext tContext)
  {
    return tContext.hasSelectAll();
  }
  
  private CoreRenderer _singleRenderer = new TableSelectOneRenderer();
  private CoreRenderer _multiRenderer = new TableSelectManyRenderer();
}
