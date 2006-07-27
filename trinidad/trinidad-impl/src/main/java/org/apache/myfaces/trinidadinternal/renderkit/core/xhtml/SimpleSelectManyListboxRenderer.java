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

import org.apache.myfaces.trinidad.component.core.input.CoreSelectManyListbox;

import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.util.IntegerUtils;

/**
 * Renderer for SelectMany listboxes.
 * @todo Expose at least some of the decode behavior for access
 *   by other selectMany renderers
 */
public class SimpleSelectManyListboxRenderer extends SimpleSelectManyRenderer
{
  public SimpleSelectManyListboxRenderer()
  {
    super(CoreSelectManyListbox.TYPE);
  }
  
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _sizeKey = type.findKey("size");
  }

  protected void encodeElementContent(
    FacesContext        context,
    RenderingContext arc,
    UIComponent         component,
    FacesBean           bean,
    List                selectItems,
    int[]               selectedIndices,
    Converter           converter,
    boolean             valuePassThru) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("select", component);
    writer.writeAttribute("multiple", Boolean.TRUE, null);
    renderId(context, component);
    renderAllAttributes(context, arc, bean, false);
    
    int count = (selectItems == null) ? 0 : selectItems.size();
    int size = SimpleSelectOneListboxRenderer.getListSize(getSize(bean),
                                                          count, false);

    writer.writeAttribute("size", IntegerUtils.getString(size), "size");

    int selectedCount = selectedIndices.length;
    int selectedEntry = 0;
    for (int i = 0; i < count; i++)
    {
      boolean selected = ((selectedEntry < selectedCount) && 
                          (i == selectedIndices[selectedEntry]));
      if (selected)
        selectedEntry++;

      SelectItem item = (SelectItem) selectItems.get(i);
      SimpleSelectOneRenderer.encodeOption(
           context, arc, component, item, converter,
           valuePassThru, i, selected);
    }
    
    writer.endElement("select");

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
      String source = LabelAndMessageRenderer.__getCachedClientId(arc);
      boolean immediate = isImmediate(bean);
      String auto = AutoSubmitUtils.getSubmitScript(arc, source, immediate);
      onchange = XhtmlUtils.getChainedJS(onchange, auto, true);
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
    return "af|selectManyListbox::content";
  }
  
  protected String getRootStyleClass(FacesBean bean)  
  {
    return "af|selectManyListbox";
  }
  
  private PropertyKey _sizeKey;
}

