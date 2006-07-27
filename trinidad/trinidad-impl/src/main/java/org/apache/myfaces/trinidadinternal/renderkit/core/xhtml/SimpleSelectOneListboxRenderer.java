/*
 * Copyright  2005,2006 The Apache Software Foundation.
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
package org.apache.myfaces.trinidadinternal.renderkit.core.xhtml;

import java.io.IOException;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import javax.faces.model.SelectItem;

import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.bean.PropertyKey;

import org.apache.myfaces.trinidad.component.core.input.CoreSelectOneListbox;

import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.util.IntegerUtils;

/**
 */
public class SimpleSelectOneListboxRenderer extends SimpleSelectOneRenderer
{
  public SimpleSelectOneListboxRenderer()
  {
    super(CoreSelectOneListbox.TYPE);
  }
  
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _sizeKey = type.findKey("size");
    _unselectedLabelKey = type.findKey("unselectedLabel");
  }

  static public int getListSize(
    int     sizeAttr, 
    int     itemCount,  
    boolean addOne)  
  {
   
    // Must have size > 1 or we'd render a choice!
    if (sizeAttr < 2)
    {
      sizeAttr = Math.min(8, Math.max(2, itemCount));
      
      if (addOne)
        sizeAttr++;
    }

    return sizeAttr;
  }

  //
  // ENCODE BEHAVIOR
  // 
  protected void encodeElementContent(
    FacesContext        context,
    RenderingContext arc,
    UIComponent         component,
    FacesBean           bean,
    List                selectItems,
    int                 selectedIndex,
    Converter           converter,
    boolean             valuePassThru) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("select", component);
    renderId(context, component);
    renderAllAttributes(context, arc, bean, false);
    
    int count = (selectItems == null) ? 0 : selectItems.size();
    String unselectedLabel = getUnselectedLabel(bean);
    boolean hasUnselectedLabel = (unselectedLabel != null);
    
    int size = getListSize(getSize(bean), count, hasUnselectedLabel);    

    writer.writeAttribute("size", IntegerUtils.getString(size), "size");

    if (hasUnselectedLabel)
    {
      SelectItem item = new SelectItem("", unselectedLabel, "", false);
      // @todo Restore the logic below
      encodeOption(context, arc, component, item, null, true, -1, 
                   (selectedIndex < 0));
    }

    for (int i = 0; i < count; i++)
    {
      SelectItem item = (SelectItem) selectItems.get(i);
      encodeOption(context, arc, component, item, converter,
                   valuePassThru, i, selectedIndex == i);
    }
    
    writer.endElement("select");
  }

  protected String getUnselectedLabel(FacesBean bean)
  {
    return toString(bean.getProperty(_unselectedLabelKey));
  }

  /**
   * Add autosubmit script
   */
  protected String getOnchange(
    FacesBean bean
    )
  {
    String onchange = super.getOnchange(bean);
    if (isAutoSubmit(bean))
    {
      RenderingContext arc = RenderingContext.getCurrentInstance();
      String auto = getAutoSubmitScript(arc, bean);
      return XhtmlUtils.getChainedJS(onchange, auto, true);
    }

    return onchange;
  }

  protected int getSize(FacesBean bean)
  {
    Object o = bean.getProperty(_sizeKey);
    if (o == null)
      o = _sizeKey.getDefault();
    if (o == null)
      return -1;
   
    return toInt(o);
  }
  
  protected String getContentStyleClass(FacesBean bean)
  {
    return "af|selectOneListbox::content";
  }
  
  protected String getRootStyleClass(FacesBean bean)  
  {
    return "af|selectOneListbox";
  }
  
  private PropertyKey _sizeKey;
  private PropertyKey _unselectedLabelKey;
}
