/*
 * @(#)XMLMenuModel.java
 *
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.trinidad.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import javax.faces.el.PropertyResolver;

import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.trinidad.logging.TrinidadLogger;

import org.xml.sax.Attributes;

import org.apache.myfaces.trinidad.util.ClassLoaderUtils;

/**
 * Creates a Menu Model from a TreeModel where nodes in the treeModel
 * contain viewId information.
 * <p>
 * Each node must have either a bean getter method or a Map property
 * that returns a viewId. There are several restrictions on the data:
 * <ul>
 * o The nodes in the tree must either be all beans or all maps,
 * but not a mix of beans and maps.
 * o The viewId of a node can be null, but if set it must be unique.
 * o The tree cannot be mutable.
 * </ul> 
 * <p>
 * The getFocusRowKey method
 * <ul>
 * o gets the current viewId by calling
 * FacesContext.getCurrentInstance().getViewRoot().getViewId()
 * o compares the current viewId with the viewId's in the viewIdFocusPathMap
 * that was built by traversing the tree when the model was created.
 * o returns the focus path to the node with the current viewId or null if the 
 * current viewId can't be found.
 * o in the case where a viewId has multiple focus paths, the currently 
 * selected node is used as a key into the nodeFocusPathMap to return the 
 * correct focus path.
 * </ul>
 * <p>
 * The Model is created by specifying it in the faces-config.xml file
 * as follows
 * <pre>
 *   &lt;managed-bean&gt;
 *    &lt;managed-bean-name&gt;hr_menu&lt;/managed-bean-name&gt;
 *    &lt;managed-bean-class&gt;
 *      org.apache.myfaces.trinidad.model.XMLMenuModel
 *    &lt;/managed-bean-class&gt;
 *    &lt;managed-bean-scope&gt;request&lt;/managed-bean-scope&gt;
 *    &lt;managed-property&gt;
 *      &lt;property-name&gt;source&lt;/property-name&gt;
 *      &lt;property-class&gt;java.lang.String&lt;/property-class&gt;
 *      &lt;value&gt;/WEB-INF/hr-menu.xml&lt;/value&gt;
 *    &lt;/managed-property&gt;
 *  &lt;/managed-bean&gt;
 * </pre>
 *
 * @author Gary Kind
 * 
 */

/*
 * Three hashmaps are also created in order to be able to resolve cases where
 * multiple menu items cause navigation to the same viewId.  All 3 of these maps
 * are created after the metadata is parsed and the tree is built, in the 
 * MenuContentHandlerImpl. 
 * 
 * o The first hashMap is called the viewIdFocusPathMap and is built by 
 * traversing the tree when the model is created.  Each node's focusViewId is 
 * obtained and used as the key to an entry in the viewIdHashMap.  An ArrayList 
 * is used as the entry's value and each item in the ArrayList is a node's 
 * rowkey from the tree. This allows us to have duplicate rowkeys for a single 
 * focusViewId which translates to a menu that contains multiple items pointing 
 * to the same page. In general, each entry will have an ArrayList of rowkeys 
 * with only 1 rowkey, AKA focus path.
 * o The second hashMap is called the nodeFocusPathMap and is built at the 
 * same time the viewIdHashMap is built. Each entry's key is the actual node and 
 * the value is the row key.  Since the model keeps track of the currently
 * selected menu node, this hashmap can be used to resolve viewId's with 
 * multiple focus paths.  Since we have the currently selected node, we just
 * use this hashMap to get its focus path.
 * o The third hashMap is called idNodeMap and is built at the same time as the
 * previous maps.  This map is populated by having each entry contain the node's
 * id as the key and the actual node as the value.  In order to keep track of 
 * the currently selected node in the case of a GET, the node's id is appended 
 * to the request URL as a parameter.  The currently selected node's id is 
 * picked up and this map is used to get the actual node that is currently 
 * selected.
 *
 * Keeping track of the currently selected menu item/node.  
 * 
 * If an itemNode in the metadata uses its "action" attribute, a POST is done
 * and the node's "doAction" method is called when the menu item is clicked. At
 * that time, the model is notified through its setCurrentlyPostedNode() method, 
 * where the current node is set and the request method is set to POST.
 * 
 * If an itemNode in the metadata uses its "destination" attribute, a GET is
 * done.  Nothing is called on the model when the menu item is clicked.  However
 * at the time the page is rendered the "getDestination" method for all nodes
 * using the "destination" attribute is called.  At this point
 * we append the node's id to the value of the destination attribute URL, as
 * a parameter, and return it. So when getFocusRowKey() is called, we get the 
 * request the node's parameter matching the currently selected node's id.  
 * Using the node id, we find the matching node in the idNodeMap and voila, we
 * have the currently selected node!
 */
public class XMLMenuModel extends BaseMenuModel
                          implements Serializable
{
  public XMLMenuModel()
  {
    super();
  }
  
  /**
   * setSource - specifies the XML metadata and creates
   * the XML Menu Model.
   * 
   * @param menuMetadataUri - String URI to the XML metadata.
   */
  public void setSource(String menuMetadataUri)
  {
    if (menuMetadataUri == null || "".equals(menuMetadataUri))
      return;
      
    _mdSource = menuMetadataUri;
    _createModel();
  }

  /**
   * Makes the TreeModel part of the menu model.  Also creates the
   * _viewIdFocusPathMap, _nodeFocusPathMap, and idNodeMaps.
   * 
   * @param data.  The Tree Model instance
   */
  @Override
  public void setWrappedData(Object data)
  {
    super.setWrappedData(data);
    
    // The only thing the child menu models are needed for are their
    // menuLists, which get incorporated into the Root Model's tree.
    // There is no need to create the hashmaps or anything
    // on the child menu models.  A lot of overhead (performance and
    // memory) would be wasted.
    if (_mdSource.equals(_getRootUri()))
    {
      _viewIdFocusPathMap = _contentHandler.getViewIdFocusPathMap();
      _nodeFocusPathMap   = _contentHandler.getNodeFocusPathMap();
      _idNodeMap          = _contentHandler.getIdNodeMap();
    }
  }
  
  /**
   * Returns the rowKey to the current viewId, or in the case of where the 
   * model has nodes with duplicate viewId's and one is encountered, we 
   * return the rowKey of the currently selected node.
   * <p>
   *
   * The getFocusRowKey method
   * <ul>
   * <li>gets the current viewId by calling
   * FacesContext.getCurrentInstance().getViewRoot().getViewId()
   * <li>compares the current viewId with the viewId's in the viewIdFocusPathMap
   * that was built by traversing the tree when the model was created.
   * <li>returns the focus path to the node with the current viewId or null if 
   * the current viewId can't be found.
   * <li>in the case where a viewId has multiple focus paths, the currently 
   * selected node is used as a key into the nodeFocusPathMap to return the 
   * correct focus path.
   * </ul>
   * 
   * @return  the rowKey to the node with the current viewId or null if the 
   * current viewId can't be found. 
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getFocusRowKey()
  {
    Object focusPath        = null;
    String currentViewId    = _getCurrentViewId();
    FacesContext context    = FacesContext.getCurrentInstance();
    boolean beginNewRequest = (_begunRequest == false);

    _begunRequest = true;    

    
    if (beginNewRequest)
    {
        // Initializations
        _prevFocusPath = null;
      
      // How did we get to this page?
      // 1) Clicked on a menu item with its action attribute set.  This does
      //    a POST.
      // 2) Clicked on a menu item with its destination attribute set.  This
      //    does a GET.
      // 3) Navigation to a viewId within our model but done from outside the
      //    model.  Examples, button, text link, etc.
      //
      
      // Case 1: POST method.  Current Node has already been set and so has the
      // request method.  The doAction() method of the clicked node calls
      // the setCurrentlyPostedNode() method of this model, which sets both. So
      // we have nothing to do in this case.
      
      if (_getRequestMethod() != _METHOD_POST)
      {
        // Case 2: GET method.  We have hung the selected node's id off the 
        // requests URL, which enables us to get the selected node and also 
        // to know that the request method is GET.
        Map<String, String> paramMap = 
          context.getExternalContext().getRequestParameterMap();
        String nodeId = paramMap.get(_NODE_ID_PROPERTY);
        
        if (nodeId != null)
        {
          _setCurrentlySelectedNode(getNode(nodeId));
          _setRequestMethod(_METHOD_GET);
        }
      }
      
      // Case 3: Navigation to a page within the model from an outside
      // method, e.g. button, link text, etc.  In this case we set the
      // currently selected node to null.  This tells us to get the 0th
      // element of the ArrayList returned from the viewId hashMap.  This
      // should be a focus path match to the node whose "defaultFocusPath"
      // attribute was set to 'true'.
      if (_getRequestMethod() == _METHOD_NONE)
      {
        _setCurrentlySelectedNode(null);
      }
  
      // Get the matching focus path ArrayList for the currentViewId.
      // This is an ArrayList because our map allows nodes with the same
      // viewId, that is, different focus paths to the same viewId.
      ArrayList<Object> fpArrayList = 
        (ArrayList<Object>) _viewIdFocusPathMap.get(currentViewId);
  
      if (fpArrayList != null)
      {
        // Get the currently selected node
        Object currentNode = _getCurrentlySelectedNode();
        
        if (fpArrayList.size() == 1  || currentNode == null)
        {
          // For fpArrayLists with multiple focusPaths,
          // the 0th entry in the fpArrayList carries the 
          // focusPath of the node with its defaultFocusPath
          // attribute set to "true", if there is one.  If
          // not, the 0th element is the default.
          focusPath = fpArrayList.get(0);
        }
        else
        {
          focusPath = _nodeFocusPathMap.get(currentNode);
        }
      }
      
      // Save all pertinent information
      _prevFocusPath = focusPath;
      
      _setRequestMethod(_METHOD_NONE);
    }
    else
    {
      // Not at the beginning of a new Request.
      // Return the previous focus path.
      // This optimization is here because, for each menu 
      // item selected, getFocusRowKey gets called multiple times.
      return _prevFocusPath;
    }

    return focusPath;
  }


  /**
   * Gets the URI to the XML menu metadata.
   * 
   * @return String URI to the XML menu metadata.
   */
  public String getSource()
  {
    return _mdSource;
  }

  /**
   * Sets the boolean value that determines whether or not to create
   * nodes whose rendered attribute value is false.  The default
   * value is false.
   * 
   * This is set through a managed property of the XMLMenuModel 
   * managed bean -- typically in the faces-config.xml file for
   * a faces application.
   */
  public void setCreateHiddenNodes(boolean createHiddenNodes)
  {
    _createHiddenNodes = createHiddenNodes;
  }
  
  /**
   * Gets the boolean value that determines whether or not to create
   * nodes whose rendered attribute value is false.  The default
   * value is false.
   * 
   * This is called by the contentHandler when parsing the XML metadata
   * for each node.
   * 
   * @return the boolean value that determines whether or not to create
   * nodes whose rendered attribute value is false.
   */
  public boolean getCreateHiddenNodes()
  {
    return _createHiddenNodes;
  }
  

  /**
   * Maps the focusPath returned when the viewId is newViewId 
   * to the focusPath returned when the viewId is aliasedViewId.
   * This allows view id's not in the treeModel to be mapped
   * to a focusPath.
   * 
   * @param newViewId the view id to add a focus path for.
   * @param aliasedViewId the view id to use to get the focusPath to use 
   *        for newViewId.
   */
  @SuppressWarnings("unchecked")
  public void addViewId(String newViewId, String aliasedViewId)
  { 
    List<Object> focusPath = 
      _viewIdFocusPathMap.get(aliasedViewId);
    if (focusPath != null)
    {
      _viewIdFocusPathMap.put(newViewId, focusPath);
    }
  }

  /**
   * Sets the currently selected node and the request method.  
   * This is called by a selected node's doAction method.  This
   * menu node must have had its "action" attribute set, thus the
   * method is POST.
   * 
   * @param currentNode  The currently selected node in the menu
   */
  public void setCurrentlyPostedNode(Object currentNode)  
  {
    _setCurrentlySelectedNode(currentNode);
    _setRequestMethod(_METHOD_POST);
  }

  /**
   * Get a the MenuNode corresponding to the key "id" from the 
   * node id hashmap.
   * 
   * @param id - String node id key for the hashmap entry.
   * @return The MenuNode that corresponds to id.
   */
  public Object getNode (String id)
  {
    // This needs to be public because the nodes call into this map
    return _idNodeMap.get(id);
  }

  /**
   * Gets the list of custom properties from the node 
   * and returns the value of propName.
   * 
   * @param node Object used to get its list of custom properties
   * @param propName String name of the property whose value is desired
   * 
   * @return Object value of propName for Object node.
   */
  public Object getCustomProperty(Object node, String propName)
  {
    if (node == null)
      return null;
      
    FacesContext context = FacesContext.getCurrentInstance();
    PropertyResolver resolver = context.getApplication().getPropertyResolver();
    
    // =-=AEW Attributes?  A Map<String, String> would be more appropriate
    Attributes propList = 
      (Attributes) resolver.getValue(node, _CUSTOM_ATTR_LIST);
   
    if (propList == null)
      return null;
      
    String value = propList.getValue(propName);
    
    // If it is an El expression, we must evaluate it
    // and return its value
    if (   value != null
        && UIComponentTag.isValueReference(value)
       )
     {
       Object elValue = null;
       
       try
       {
         FacesContext ctx     = FacesContext.getCurrentInstance();
         ValueBinding binding = ctx.getApplication().createValueBinding(value);
         elValue              = binding.getValue(ctx);
       }
       catch (Exception ex)
       {
         _LOG.warning("EL Expression " + value + 
                      " is invalid or returned a bad value", ex);
         return null;
       }
       return elValue;
     }
     
    return value;
  }
  
  /* ====================================================================
   * Private Methods
   * ==================================================================== */
   
  /**
    * Creates a menu model based on the menu metadata Uri.
    * This is accomplished by:
    * <ol>
    * <li> Get the MenuContentHandlerImpl through the Services API.
    * <li> Set the root model and current model on the content handler, which, 
    * in turn, sets the models on each of the nodes.
    * <li> Parse the metadata.  This calls into the MenuContentHandler's 
    * startElement and endElement methods, where a List of nodes and a TreeModel
    * are created, along with the 3 hashMaps needed by the Model.</li>
    * <li> Use the TreeModel to create the XMLMenuModel.</li>
    * </ol>
    */
  private void _createModel()
  {
    try
    {
      if (_contentHandler == null)
      {        
        List<MenuContentHandler> services = 
          ClassLoaderUtils.getServices(_MENUCONTENTHANDLER_SERVICE);
         
        if (services.isEmpty())
          throw new IllegalStateException("No MenuContentHandler was registered.");
        
        _contentHandler = services.get(0);
        if (_contentHandler == null)
        {
          throw new NullPointerException();
        }
      }
      
      // Set the root, top-level menu model's URI on the contentHandler.
      // In this model, the menu content handler and nodes need to have
      // access to the model's data structures and to notify the model
      // of the currently selected node (in the case of a POST).
      _setRootModelUri(_contentHandler);

      // Set the local model (model created by a sharedNode) on the
      // contentHandler so that nodes can get back to their local model
      // if necessary.
      _setModelUri(_contentHandler);
      
      TreeModel treeModel = _contentHandler.getTreeModel(_mdSource);
      setWrappedData(treeModel);
    }
    catch (Exception ex)
    {
      _LOG.severe(  "Exception creating menu model " 
                  + _mdSource, ex);
      return;
    }
  }

  /**
   * _setRootModelUri - sets the top-level, menu model's Uri on the 
   * menu content handler. This is so nodes will only operate
   * on the top-level, root model. 
   * 
   */
  @SuppressWarnings("unchecked")
  private void _setRootModelUri(MenuContentHandler contentHandler)
  {
    if (_rootUri == null)
    {
      _rootUri = _mdSource;
      
      // Put the root model on the Application Map so that it
      // Can be picked up by the nodes to call back into the 
      // root model
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Map<String, Object> requestMap = 
        facesContext.getExternalContext().getRequestMap();
      
      requestMap.put(_rootUri, this);

      // Set the key (_rootUri) to the root model on the content
      // handler so that it can then be set on each of the nodes
      contentHandler.setRootModelUri(_rootUri);
    }
  }
  
  /**
   * Returns the root menu model's Uri.
   * 
   * @return the root menu model's Uri.
   */
  private String _getRootUri()
  {
    return _rootUri;
  }
  
  /**
   * _setModelUri - sets the local, menu model's Uri on the 
   * menu content handler. 
   * 
   */
  @SuppressWarnings("unchecked")
  private void _setModelUri(MenuContentHandler contentHandler)
  {
    String localUri = _mdSource;
    
    // Put the local model on the Request Map so that it
    // Can be picked up by the nodes to call back into the 
    // local model
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Map<String, Object> requestMap = 
      facesContext.getExternalContext().getRequestMap();
    
    requestMap.put(localUri, this);
    
    // Set the key (_rootUri) to the root model on the content
    // handler so that it can then be set on each of the nodes
    contentHandler.setModelUri(localUri);
  }
  
  /**
   * Returns the current viewId.
   * 
   * @return  the current viewId or null if the current viewId can't be found 
   */
  
  private String _getCurrentViewId()
  {    
    String currentViewId = 
        FacesContext.getCurrentInstance().getViewRoot().getViewId();  
                   
    return currentViewId;
  }  

  /**
   * Gets the currently selected node in the menu
   */
  private Object _getCurrentlySelectedNode()
  {
    return _currentNode;
  }
  
  /**
   * Sets the currently selected node.
   * 
   * @param currentNode.  The currently selected node in the menu.
   */
  private void _setCurrentlySelectedNode(Object currentNode)
  {
    _currentNode = currentNode;
  }

  /**
   * Sets the request method
   * 
   * @param method
   */
  private void _setRequestMethod(String method)
  {
    _requestMethod = method;
  }
  
  /**
   * Get the request method
   */
  private String _getRequestMethod()
  {
    return _requestMethod;
  }

  /* ================================================================
   * Public inner interface for the menu content handler
   * implementation
   * ================================================================ */
   
  /*
   * Interface corresponding to the MenuContentHandlerImpl
   * inorg.apache.myfaces.trinidadinternal.menu.   This is used to achieve 
   * separation between the api (trinidad) and the implementation (trinidadinternal).
   * It is only used by the XMLMenuModel, thus it is an internal interface.
   */
  public interface MenuContentHandler 
  {
    /**
      * Get the TreeModel built while parsing metadata.
      * 
      * @param uri String mapkey to a (possibly) treeModel cached on
      *        the MenuContentHandlerImpl.
      * @return TreeModel.
      */
    public TreeModel getTreeModel(String uri);

    /**
      * Sets the root Uri on the ContentHandler so that the nodes
      * can get back to the root model of the application menu tree
      * through the request map.
      */
    public void setRootModelUri(String uri);
    
    /**
      * Sets the local, sharedNode model's Uri on the ContentHandler so that
      * the local model can be gotte too if necessary.
      */
    public void setModelUri(String uri);

    /**
     * Get the Model's idNodeMap
     * 
     * @return the Model's idNodeMap
     */
    public Map<String, Object> getIdNodeMap();

    /**
     * Get the Model's nodeFocusPathMap
     * 
     * @return the Model's nodeFocusPathMap
     */
    public Map<Object, List<Object>> getNodeFocusPathMap();

    /**
     * Get the Model's viewIdFocusPathMap
     * 
     * @return the Model's viewIdFocusPathMap
     */
    public Map<String, List<Object>> getViewIdFocusPathMap();
  }
     
  private Object  _currentNode       = null;
  private Object  _prevFocusPath     = null;
  private String  _requestMethod     = _METHOD_NONE;
  private String  _mdSource          = null;
  private boolean _createHiddenNodes = false;
  private boolean _begunRequest      = false;

  private Map<String, List<Object>> _viewIdFocusPathMap;
  private Map<Object, List<Object>> _nodeFocusPathMap;
  private Map<String, Object> _idNodeMap;

  static private String _rootUri                    = null;  
  static private MenuContentHandler _contentHandler = null;
  
  static private final String _NODE_ID_PROPERTY     = "nodeId";
  static private final String _METHOD_GET           = "get";
  static private final String _METHOD_POST          = "post";
  static private final String _METHOD_NONE          = "none";
  static private final String _CUSTOM_ATTR_LIST     = "customPropList";
  static private final String _MENUCONTENTHANDLER_SERVICE =
            "org.apache.myfaces.trinidad.model.XMLMenuModel$MenuContentHandler";
            
  static private final TrinidadLogger _LOG = 
         TrinidadLogger.createTrinidadLogger(XMLMenuModel.class);
}
