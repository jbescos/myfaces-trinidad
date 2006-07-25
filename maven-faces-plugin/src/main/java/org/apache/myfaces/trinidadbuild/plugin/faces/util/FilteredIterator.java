/*
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
package org.apache.myfaces.trinidadbuild.plugin.faces.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilteredIterator implements Iterator
{
  public FilteredIterator(
    Iterator iter,
    Filter   filter)
  {
    _iter = iter;
    _filter = filter;
    _advance();
  }

  public boolean hasNext()
  {
    return (_next != null);
  }

  public Object next()
  {
    if (_next == null)
      throw new NoSuchElementException();

    Object obj = _next;
    _advance();
    return obj;
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }

  private void _advance()
  {
    while (_iter.hasNext())
    {
      Object obj = _iter.next();
      if (_filter.accept(obj))
      {
        _next = obj;
        return;
      }
    }

    _next = null;
  }

  private final Iterator _iter;
  private final Filter _filter;
  private Object _next;
}
