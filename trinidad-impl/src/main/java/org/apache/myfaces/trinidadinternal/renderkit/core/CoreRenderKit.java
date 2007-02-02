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
package org.apache.myfaces.trinidadinternal.renderkit.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.ViewHandler;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.component.UIGraphic;
import javax.faces.component.UIInput;
import javax.faces.component.UIMessage;
import javax.faces.component.UIMessages;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;
import javax.servlet.ServletResponse;

import org.apache.myfaces.trinidad.context.Agent;
import org.apache.myfaces.trinidad.context.PartialPageContext;
import org.apache.myfaces.trinidad.context.RenderingContext;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;
import org.apache.myfaces.trinidad.render.DialogRenderKitService;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.render.RenderUtils;
import org.apache.myfaces.trinidadinternal.agent.AgentUtil;
import org.apache.myfaces.trinidadinternal.agent.TrinidadAgent;
import org.apache.myfaces.trinidadinternal.config.dispatch.DispatchResponseConfiguratorImpl;
import org.apache.myfaces.trinidadinternal.context.TrinidadPhaseListener;
import org.apache.myfaces.trinidadinternal.io.DebugHtmlResponseWriter;
import org.apache.myfaces.trinidadinternal.io.DebugResponseWriter;
import org.apache.myfaces.trinidadinternal.io.HtmlResponseWriter;
import org.apache.myfaces.trinidadinternal.io.IndentingResponseWriter;
import org.apache.myfaces.trinidadinternal.io.XhtmlResponseWriter;
import org.apache.myfaces.trinidadinternal.renderkit.RenderKitBase;
import org.apache.myfaces.trinidadinternal.renderkit.core.ppr.PartialPageContextImpl;
import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.PartialPageUtils;
import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.XhtmlRenderer;
import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.XhtmlUtils;
import org.apache.myfaces.trinidadinternal.renderkit.htmlBasic.HtmlCommandButtonRenderer;
import org.apache.myfaces.trinidadinternal.renderkit.htmlBasic.HtmlCommandLinkRenderer;
import org.apache.myfaces.trinidadinternal.renderkit.htmlBasic.HtmlFormRenderer;
import org.apache.myfaces.trinidadinternal.share.util.CaboHttpUtils;

/**
 * RenderKit based on UIX.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/CoreRenderKit.java#0 $) $Date: 10-nov-2005.19:01:18 $
 * @author The Oracle ADF Faces Team
 */
public class CoreRenderKit extends RenderKitBase
                          implements DialogRenderKitService,
                                     ExtendedRenderKitService
{
  /**
   * The default output mode;  if no output mode is set, or the requested
   * output mode is not available, this output mode will be used.
   */
  static public String OUTPUT_MODE_DEFAULT = "default";

  /**
   * A portlet output mode;  when supported, this output mode should
   * result in a version of page content optimized for use in portlets.
   */
  static public String OUTPUT_MODE_PORTLET = "portlet";

  /**
   * A printable output mode;  when supported, this output mode should
   * result in a version of a page designed for printouts.
   */
  static public String OUTPUT_MODE_PRINTABLE = "printable";

  /**
   * An e-mail output mode;  when supported, this output mode should
   * result in a version of page content optimized for use in e-mail.
   */
  static public String OUTPUT_MODE_EMAIL = "email";

  static public String RETURN_PARAM = "rtrn";

  /**
   * RenderKit ID for an internal and not-fully-functional kit
   * that contains a base set of renderers.
   */
  static public String BASE_RENDER_KIT_ID = "org.apache.myfaces.trinidadinternal.core";

  static public String getId()
  {
    return "org.apache.myfaces.trinidad.core";
  }

  /**
   * Choose a RenderKit for the current request.
   */
  static public String chooseRenderKit(FacesContext context)
  {
    RequestContext afc = RequestContext.getCurrentInstance();
    // According to the spec FacesContext can be null.
    // In that case RequestContext could also be null.
    // bug 4695929:
    if (afc != null)
    {
      // TODO: Obviously, this cheesy algorithm is not quite enough!
      Agent agent = afc.getAgent();
      if (Agent.TYPE_PDA.equals(agent.getType()))
        return "org.apache.myfaces.trinidad.core.pda";
    }
    return "org.apache.myfaces.trinidad.core.desktop";
  }

  public CoreRenderKit()
  {
    _addBasicHTMLRenderKit();
  }


  /**
   * Save the form name of the source page for use in subsequent postback.
   * We save it at pageFlow scope, which means that it
   * won't be seen by the original page - as long as this only
   * gets called from the popup dialog, and not the originating page!
   */
  static public void saveDialogPostbackValues(
    String returnId)
  {
    Map<String, Object> pageFlowScope = 
      RequestContext.getCurrentInstance().getPageFlowScope();
    
    pageFlowScope.put(_RETURN_ID, returnId);
  }

  //
  // BEGIN DialogRenderKitService
  //


  @SuppressWarnings("unchecked")
  public boolean launchDialog(
    FacesContext       context,
    UIViewRoot         targetRoot,
    UIComponent        source,
    Map<String,Object> processParameters,
    boolean            useWindow,
    Map<String,Object> windowProperties)
  {
    // If we're not being asked to use a separate window,
    // just fallback on the default launchDialog() code
    if (!useWindow)
      return false;

    // And if we don't support separate windows at all, then bail
    // there too
    if (!_supportsSeparateWindow(context))
      return false;

    String sourceId = (source == null) ? null : source.getClientId(context);
    String formId = RenderUtils.getFormId(context, source);

    if (windowProperties == null)
      windowProperties = new HashMap<String,Object>();

    // Copy properties from the source component to the dialog properties
    if (source != null)
    {
      Map<String, Object> sourceAttrs = source.getAttributes();
      _copyProperty(windowProperties, "width", sourceAttrs, "windowWidth");
      _copyProperty(windowProperties, "height", sourceAttrs, "windowHeight");
    }

    Map<String, Object> pageFlowScope = 
      RequestContext.getCurrentInstance().getPageFlowScope();
    
    if (processParameters != null)
      pageFlowScope.putAll(processParameters);

    DialogRequest request = new DialogRequest(targetRoot,
                                              sourceId,
                                              formId,
                                              windowProperties);
    _getDialogList(context, true).add(request);
    return true;
  }


  public boolean returnFromDialog(
    FacesContext context,
    Object       returnValue)
  {
    if (!_supportsSeparateWindow(context))
      return false;

    RequestContext afC = RequestContext.getCurrentInstance();
    try
    {
      String returnId = (String) afC.getPageFlowScope().get(_RETURN_ID);

      if (returnId == null)
        throw new IllegalStateException("No returnId is available for returning from the dialog;  this usually means that you aren't in a dialog in the first place.");

      // Deliver callback in the context of the launching window
      // This is required to work around problems in Mozilla
      // with cross frame XmlHttpRequest invocation
      Writer out = _getHtmlWriter(context);
      out.write("<script>");
      out.write("var callback = 'ADFDialogReturn[" + returnId + "]()';");
      out.write("top.opener.setTimeout(callback, 1);");
      out.write("top.close()");
      out.write("</script>");
      out.close();
      context.responseComplete();

      _LOG.fine("Returning from dialog using return ID {0}", returnId);
    }
    catch (IOException ioe)
    {
      _LOG.warning(ioe);
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  public boolean isReturning(
    FacesContext context,
    UIComponent  source)
  {
    Map<String, String> parameterMap = 
      context.getExternalContext().getRequestParameterMap();
    
    Object returning = parameterMap.get(RETURN_PARAM);
    if ((returning == null) || "".equals(returning))
      return false;

    String clientId = source.getClientId(context);
    return returning.equals(clientId);
  }

  //
  // END DialogRenderKitService
  //


  //
  // BEGIN ExtendedRenderKitService
  //

  @SuppressWarnings("unchecked")
  public boolean shortCircuitRenderView(
    FacesContext context) throws IOException
  {
    if (PartialPageUtils.isPartialRequest(context))
    {
      Map<String, Object> requestMap = 
        context.getExternalContext().getRequestMap();

      UIViewRoot originalRoot = (UIViewRoot) requestMap.get(
                         TrinidadPhaseListener.INITIAL_VIEW_ROOT_KEY);
      // If we're doing a partial update, and the page has changed, switch to a
      // full page context.
      if (context.getViewRoot() != originalRoot)
      {
        ViewHandler vh = context.getApplication().getViewHandler();

        String viewId = context.getViewRoot().getViewId();
        String redirect = vh.getActionURL(context, viewId);
        context.getExternalContext().redirect(redirect);
        if (_LOG.isFine())
        {
          _LOG.fine("Page navigation to {0} happened during a PPR request " +
                    "on {1};  Apache Trinidad is forcing a redirect.",
                    new String[]{viewId, originalRoot.getViewId()});
        }

        return true;
      }
    }

    // =-=AEW We could look for PPR requests that have no
    // requested partial targets, in particular requests
    // that simply need to launch a dialog.
    return false;
  }

  public boolean isStateless(
    FacesContext context)
  {
    return false;
  }

  /**
   * Adds a script for execution during rendering.
   */
  public void addScript(FacesContext context, String script)
  {
    if ((script == null) || "".equals(script))
      return;

    // Bulletproof against coders who don't include semicolons.
    // We end up concatenating all the scripts, so it would go poorly
    // if one didn't end with a semicolon
    if (!script.endsWith(";"))
      script += ";";

    _getScriptList(context, true).add(script);

  }

  public void encodeScripts(
    FacesContext context) throws IOException
  {
    List<DialogRequest> dialogList = _getDialogList(context, false);
    boolean hasDialog = ((dialogList != null) && !dialogList.isEmpty());
    List<String> scriptList = _getScriptList(context, false);
    boolean hasScript = ((scriptList != null) && !scriptList.isEmpty());

    if (hasDialog || hasScript)
    {
      RenderingContext arc = RenderingContext.getCurrentInstance();
      if (hasDialog)
        DialogRequest.addDependencies(context, arc);

      // =-=AEW How to pick a proper ID?

      // Write out a script;  let PPR know to use it
      String scriptId = "::launchScript";
      PartialPageContext ppContext = arc.getPartialPageContext();
      // TODO: Create the span with a bogus component where
      // getClientId() returns the scriptId;  this avoids
      // the need to downcast - you just need to
      // call addPartialTarget().  Or, come up with a better
      // PPR api to make it simpler
      PartialPageContextImpl ppImpl = (PartialPageContextImpl) ppContext;
      if (ppImpl != null)
      {
        ppImpl.addRenderedPartialTarget(scriptId);
        ppImpl.pushRenderedPartialTarget(scriptId);
      }

      ResponseWriter out = context.getResponseWriter();

      out.startElement("script", null);
      out.writeAttribute("id", scriptId, null);

      XhtmlRenderer.renderScriptDeferAttribute(context, arc);

      // And render each dialog launch that we need
      if (hasDialog)
      {
        for (DialogRequest dialog : dialogList)
        {
          dialog.renderLaunchJavascript(context, arc);
        }
      }

      if (hasScript)
      {
        for (String script : scriptList)
        {
          out.write(script);
        }
      }

      out.endElement("script");
      if (hasDialog)
        dialogList.clear();
      if (hasScript)
        scriptList.clear();

      if (ppImpl != null)
        ppImpl.popRenderedPartialTarget();
    }
  }


  /**
   * Called when the encoding of a page begins.
   */
  public void encodeBegin(FacesContext context)
  {
    /*CoreAdfRenderingContext arc = */new CoreRenderingContext();
  }


  /**
   * Called when the encoding of a page ends, if there were no exceptions.
   */
  public void encodeEnd(FacesContext context)
  {
  }

  /**
   * Called when the encoding of a page completes, whether or not there
   * were exceptions.
   */
  public void encodeFinally(FacesContext context)
  {
    RenderingContext arc = RenderingContext.getCurrentInstance();
    if (arc != null)
    {
      arc.release();
    }
    else
    {
      _LOG.warning("No AdfRenderingContext available");
    }
  }


  //
  // END ExtendedRenderKitService
  //


  @Override
  public ResponseStateManager getResponseStateManager()
  {
    return _rsm;
  }

  @Override
  public ResponseStream createResponseStream(final OutputStream output)
  {
    return new ResponseStream()
    {
      public void write(int b) throws IOException
      {
        output.write(b);
      }
      
      public void write(byte b[]) throws IOException
      {
        output.write(b);
      }
      
      public void write(byte b[], int off, int len) throws IOException
      {
        output.write(b, off, len);
      }

      public void flush() throws IOException
      {
        output.flush();
      }

      public void close() throws IOException
      {
        output.close();
      }
    }; 
  }

  @Override
  public ResponseWriter createResponseWriter(Writer writer,
                                             String contentTypeList,
                                             String characterEncoding)
  {
    ResponseWriter rw;

    try
    {
      FacesContext fContext = FacesContext.getCurrentInstance();

      // After webtier-alignment, this will hopefully no longer be null.
      if (contentTypeList == null)
      {
        // default to content type captured by ServletFilter
        contentTypeList = DispatchResponseConfiguratorImpl.getContentType(fContext);
      }

      String[] acceptedTypes = (contentTypeList == null)
        ? null : CaboHttpUtils.decodeQValueString(contentTypeList);
      String contentType = _chooseContentType(acceptedTypes);

      if (_XHTML_MIME_TYPE.equals(contentType) ||
          _APPLICATION_XML_MIME_TYPE.equals(contentType) ||
          _XML_MIME_TYPE.equals(contentType))
      {
        rw = new XhtmlResponseWriter(writer, contentType, characterEncoding);
      }
      else
      {
        assert _HTML_MIME_TYPE.equals(contentType);
        rw = new HtmlResponseWriter(writer, characterEncoding);
      }

      return _addDebugResponseWriters(rw);
    }
    catch (IOException ioe)
    {
      _LOG.severe(ioe);
      return null;
    }
  }


  static private ResponseWriter _addDebugResponseWriters(
     ResponseWriter responseWriter)
  {
    RequestContext requestContext = RequestContext.getCurrentInstance();
    if (requestContext.isDebugOutput())
    {
      responseWriter = new IndentingResponseWriter(responseWriter);
      responseWriter = new DebugResponseWriter(responseWriter);
      if ("text/html".equals(responseWriter.getContentType()))
        responseWriter = new DebugHtmlResponseWriter(responseWriter);
    }

    return responseWriter;
  }

  //
  // Copy most of the known Basic HTML RenderKit over to ourselves
  // to improve the efficiency of common lookups.  Also, register
  // the Renderers that we explicitly override.
  //
  private void _addBasicHTMLRenderKit()
  {
    RenderKitFactory rkf = (RenderKitFactory)
      FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    RenderKit basic = rkf.getRenderKit(null,
                                       RenderKitFactory.HTML_BASIC_RENDER_KIT);
    if (basic == null)
    {
      _LOG.warning("Basic HTMLRenderKit could not be located");
    }
    else
    {
      for (int i = 0; i < _BASIC_HTML_RENDERERS.length; i += 2)
      {
        String componentFamily = _BASIC_HTML_RENDERERS[i];
        String rendererType = _BASIC_HTML_RENDERERS[i + 1];
        Renderer renderer = basic.getRenderer(componentFamily, rendererType);
        if (renderer == null)
        {
          _LOG.warning("Could not find basic HTML renderer for " +
                       "family " + componentFamily + ", type=" + rendererType);
        }
        else
        {
          addRenderer(componentFamily, rendererType, renderer);
        }
      }

      // But we render UIForms with our own renderer
      addRenderer(UIForm.COMPONENT_FAMILY,
                  "javax.faces.Form",
                  new HtmlFormRenderer());
      // And we render UICommandLink with our own renderer
      addRenderer(UICommand.COMPONENT_FAMILY,
                  "javax.faces.Link",
                  new HtmlCommandLinkRenderer());
      // In jsf 1.1_02 the ri FormRenderer writes out script used by
      // h:commandButton. Since we override the RI FormRenderer, we also
      // need to override the commandButton renderer:
      addRenderer(UICommand.COMPONENT_FAMILY,
                  "javax.faces.Button",
                  new HtmlCommandButtonRenderer());

    }

    // And, generally speaking, aggregate in anything else found
    // in the Basic HTML RenderKit
    attachAggregatedRenderKit(basic);
  }


  @SuppressWarnings("unchecked")
  private List<DialogRequest> _getDialogList(
    FacesContext context,
    boolean      createIfNew)
  {
    Map<String, Object> requestMap = 
      context.getExternalContext().getRequestMap();
    
    List<DialogRequest> l = 
      (List<DialogRequest>)requestMap.get(_DIALOG_LIST_KEY);
    
    if ((l == null) && createIfNew)
    {
      l = new ArrayList<DialogRequest>();
      requestMap.put(_DIALOG_LIST_KEY, l);
    }

    return l;
  }

  @SuppressWarnings("unchecked")
  private List<String> _getScriptList(
    FacesContext context,
    boolean      createIfNew)
  {
    Map<String, Object> requestMap = 
      context.getExternalContext().getRequestMap();
    
    List<String> l = 
      (List<String>) requestMap.get(_SCRIPT_LIST_KEY);
    
    if ((l == null) && createIfNew)
    {
      l = new ArrayList<String>();
      requestMap.put(_SCRIPT_LIST_KEY, l);
    }

    return l;
  }

  private boolean _supportsSeparateWindow(FacesContext context)
  {
    TrinidadAgent agent = AgentUtil.getAgent(context);
    return XhtmlUtils.supportsSeparateWindow(agent);
  }

  /**
   * @todo Generalize???
   */
  private Writer _getHtmlWriter(FacesContext context) throws IOException
  {
    ServletResponse response = (ServletResponse)
      context.getExternalContext().getResponse();
    response.setContentType(_HTML_MIME_TYPE);
    return response.getWriter();
  }


  private ResponseStateManager _rsm = new CoreResponseStateManager();

  //
  // renderers are registered in the faces-config.xml file

  static private final String[] _BASIC_HTML_RENDERERS =
  {
    UICommand.COMPONENT_FAMILY,       "javax.faces.Button",
    // For CommandLink we register our own renderer
    //UICommand.COMPONENT_FAMILY ,      "javax.faces.Link",
    UIData.COMPONENT_FAMILY,          "javax.faces.Table",
    // For UIForm we register our renderer
    //    UIForm.COMPONENT_FAMILY,          "javax.faces.Form",
    UIPanel.COMPONENT_FAMILY,         "javax.faces.Grid",
    UIPanel.COMPONENT_FAMILY,         "javax.faces.Group",
    UIInput.COMPONENT_FAMILY,         "javax.faces.Hidden",
    UIInput.COMPONENT_FAMILY,         "javax.faces.Secret",
    UIInput.COMPONENT_FAMILY,         "javax.faces.Text",
    UIInput.COMPONENT_FAMILY,         "javax.faces.Textarea",
    UIGraphic.COMPONENT_FAMILY,       "javax.faces.Image",
    UIMessage.COMPONENT_FAMILY,       "javax.faces.Message",
    UIMessages.COMPONENT_FAMILY,      "javax.faces.Messages",
    UIOutput.COMPONENT_FAMILY ,       "javax.faces.Format",
    UIOutput.COMPONENT_FAMILY ,       "javax.faces.Label",
    UIOutput.COMPONENT_FAMILY ,       "javax.faces.Link",
    UIOutput.COMPONENT_FAMILY ,       "javax.faces.Text",
    UISelectBoolean.COMPONENT_FAMILY, "javax.faces.Checkbox",
    UISelectOne.COMPONENT_FAMILY,     "javax.faces.Listbox",
    UISelectOne.COMPONENT_FAMILY,     "javax.faces.Menu",
    UISelectOne.COMPONENT_FAMILY,     "javax.faces.Radio",
    UISelectMany.COMPONENT_FAMILY,    "javax.faces.Listbox",
    UISelectMany.COMPONENT_FAMILY,    "javax.faces.Menu",
    UISelectMany.COMPONENT_FAMILY,    "javax.faces.Checkbox",
  };


  static private <K1, K2, V> void _copyProperty(
    Map<K1, V> toMap,
    K1 toKey,
    Map<K2, ? extends V> fromMap,
    K2 fromKey)
  {
    if (!toMap.containsKey(toKey))
    {
      V o = fromMap.get(fromKey);
      if (o != null)
        toMap.put(toKey, o);
    }
  }

  /**
   * Returns the content type;  this look-and-feel will choose
   * the first of XHTML or HTML that is listed in the list
   * of acceptable types, and HTML if neither is listed.
   */
  private String _chooseContentType(String[] acceptedTypes)
  {
    if (acceptedTypes != null)
    {
      for (int i = 0; i < acceptedTypes.length; i++)
      {
        String type = acceptedTypes[i];
        if (_HTML_MIME_TYPE.equals(type))
          return _HTML_MIME_TYPE;
        if (_XHTML_MIME_TYPE.equals(type))
          return _XHTML_MIME_TYPE;
        if (_APPLICATION_XML_MIME_TYPE.equals(type))
          return _APPLICATION_XML_MIME_TYPE;
        if (_XML_MIME_TYPE.equals(type))
          return _XML_MIME_TYPE;
      }
    }

    // Default to HTML if we couldn't find anything directly applicable
    return _HTML_MIME_TYPE;
  }


  private static final String _XHTML_MIME_TYPE = "application/xhtml+xml";
  private static final String _APPLICATION_XML_MIME_TYPE = "application/xml";
  private static final String _XML_MIME_TYPE = "text/xml";
  private static final String _HTML_MIME_TYPE = "text/html";

  static private final String _RETURN_ID = "org.apache.myfaces.trinidadinternal.renderkit.ReturnId";
  static private final String _DIALOG_LIST_KEY =
    "org.apache.myfaces.trinidadinternal.renderkit.DialogList";
  static private final String _SCRIPT_LIST_KEY =
    "org.apache.myfaces.trinidadinternal.renderkit.ScriptList";

  static private final TrinidadLogger _LOG =
     TrinidadLogger.createTrinidadLogger(CoreRenderKit.class);
}
