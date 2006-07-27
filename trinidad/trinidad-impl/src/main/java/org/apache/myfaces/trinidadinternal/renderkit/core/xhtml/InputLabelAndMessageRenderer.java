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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.context.ResponseWriter;
import org.apache.myfaces.trinidad.bean.FacesBean;

import org.apache.myfaces.trinidad.bean.PropertyKey;
import org.apache.myfaces.trinidadinternal.renderkit.RenderingContext;
import org.apache.myfaces.trinidadinternal.util.MessageUtils;


public abstract class InputLabelAndMessageRenderer extends LabelAndMessageRenderer
{

  public InputLabelAndMessageRenderer(FacesBean.Type type)
  {
    super(type);
  }
  
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _simpleKey   = type.findKey("simple");
    _disabledKey   = type.findKey("disabled");
    _readOnlyKey   = type.findKey("readOnly");
  }  
  
  public void decode(FacesContext context, UIComponent component)
  {
     getFormInputRenderer().decode(context, component);
  }
  
  public Object getConvertedValue(
    FacesContext context,
    UIComponent  component,
    Object       submittedValue)
  {
    return  getFormInputRenderer().getConvertedValue(context,
                                              component,
                                              submittedValue);
  }

  protected String getLabelFor(
    FacesContext        context,
    RenderingContext arc,
    UIComponent         component,
    FacesBean           bean)
  {
      
    if (!getFormInputRenderer().renderAsElement(context, arc, bean))
      return null;

    return __getCachedClientId(arc);
  }


  protected void encodeAll(
    FacesContext        context,
    RenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    if (getSimple(bean))
    {
      String saved = arc.getCurrentClientId();
      String clientId = component.getClientId(context);
      arc.setCurrentClientId(clientId);
      
      // add the label to FormData so that it can be used in 
      // client side validation error messages.
      String value = getLabel(bean);
      FormData fd = arc.getFormData();
      if (fd != null)
        fd.addLabel(clientId, value); 
      
      FacesMessage msg = MessageUtils.getFacesMessage(context, clientId);
      if (msg != null)
      {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("a", null);
        String anchor = MessageUtils.getAnchor(clientId);
        writer.writeAttribute("name", anchor, null);
        writer.endElement("a");
      }
      
      delegateRenderer(context, arc, component, bean, getFormInputRenderer());
      arc.setCurrentClientId(saved);
    }
    else
    {
      super.encodeAll(context, arc, component, bean);
    }
  }
  
  /**
   * If it's known that the field content is not editable, return false. 
   * Otherwise, assume it is editable and return true
   */
  protected boolean isContentEditable(FacesBean bean)
  {
    return !getFormInputRenderer().getReadOnly(
                                   FacesContext.getCurrentInstance(), bean);  
  }
 
  
  protected void renderFieldCellContents(
    FacesContext        context,
    RenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    delegateRenderer(context, arc, component, bean, getFormInputRenderer());
  }
  

 
  protected boolean getSimple(FacesBean bean)
  {
    Object o = bean.getProperty(_simpleKey);
    if (o == null)
      o = _simpleKey.getDefault();

    return !Boolean.FALSE.equals(o);
  }
  
  /**
   * Render the styles and style classes that should go on the root dom element.
   * (called from LabelAndMessageRenderer, the superclass)
   * @param context
   * @param arc
   * @param component
   * @param bean
   * @throws IOException
   */
  protected void renderRootDomElementStyles(
   FacesContext        context,
   RenderingContext arc,
   UIComponent         component,
   FacesBean           bean) throws IOException
  {
    String styleClass = getStyleClass(bean);
    String disabledStyleClass = null;
    String readOnlyStyleClass = isReadOnly(bean) ? "p_AFReadOnly" : null;
    // readOnly takes precedence over disabled
    if (readOnlyStyleClass == null)
    {
      disabledStyleClass = isDisabled(bean) ? "p_AFDisabled" : null;
    }
    renderStyleClasses(context, arc, new String[]{styleClass,
                                                   getRootStyleClass(bean),  
                                                   disabledStyleClass, 
                                                   readOnlyStyleClass });
    renderInlineStyle(context, arc, bean);
  }
    
  protected boolean isDisabled(FacesBean bean)
  {
    Object o = bean.getProperty(_disabledKey);
    if (o == null)
      o = _disabledKey.getDefault();

    return !Boolean.FALSE.equals(o);
  }
  
  protected boolean isReadOnly(FacesBean bean)
  {
    Object o = bean.getProperty(_readOnlyKey);
    if (o == null)
      o = _readOnlyKey.getDefault();

    return !Boolean.FALSE.equals(o);
  }
   
  /**
   * @todo Default shortDesc to label when inside the data area
   * of the table (and for screenReaderMode + radio buttons?!?)
   */
  abstract protected FormInputRenderer getFormInputRenderer();
  

  private PropertyKey   _simpleKey;
  
  private PropertyKey   _disabledKey;
  private PropertyKey   _readOnlyKey;
}
