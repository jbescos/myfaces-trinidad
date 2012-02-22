/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.trinidaddemo.support;

import java.io.Serializable;
import java.util.List;

/**
 * This interface defines a category that groups together one or more component demos.
 */
public interface IComponentDemoCategory extends Serializable  {
  
  /**
   * @return the name of this category.
   */
  String getName();
  
  /**
   * @return the unique identifier of this category, unique across the whole application. 
   */
  ComponentDemoCategoryId getId();

  /**
   * Adds the specified component demo to this category.
   */
  void addComponentDemo(IComponentDemo componentDemo);
  
  /**
   * Returns a list containing the component demos part of this category.
   * 
   * @return a list containing the component demos part of this category.
   */
  List<IComponentDemo> getComponentDemos();
}
