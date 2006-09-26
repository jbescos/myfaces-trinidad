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
package org.apache.myfaces.trinidad.component;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 * Base class for the group componnet.
 * <p>
 * @version $Name:  $ ($Revision: 429530 $) $Date: 2006-08-07 18:44:54 -0600 (Mon, 07 Aug 2006) $
 * @author The Oracle ADF Faces Team
 */
abstract public class UIXGroupTemplate extends UIXComponentBase
{
  /**
   * Overridden to return true.
   * @return true because the children are rendered by this component
   */
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
   * Renders the children in their raw form.
   * There is no Renderer for this component because it has no
   * visual representation or any sort of layout for its children.
   * @param context the FacesContext
   * @throws IOException if there is an error encoding the children
   */
  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();

    if (!isRendered())
      return;

    if (getChildCount() > 0)
    {
      for(UIComponent child : (List<UIComponent>)getChildren())
      {
        __encodeRecursive(context, child);
      }
    }
  }
}

