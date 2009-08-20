/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;

/**
 * Uses an {@link InternalLinker} to create links to wiki pages.
 * 
 * @copyright
 * @author mth
 */
public class WikiUrlTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _page;
  private String _extra = "";

  public String getPage() {
    return _page;
  }

  public void setPage(final String page) {
    _page = page;
  }

  public String getExtra() {
    return _extra;
  }

  public void setExtra(final String extra) {
    _extra = extra;
  }

  public int doStartTag() throws JspException {
    try {
      InternalLinker linker = (InternalLinker) pageContext.getRequest().getAttribute("internalLinker");
      final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
      if (linker != null) {
        JspWriter out = pageContext.getOut();
        out.write(Escape.html(linker.url(getPage(), getExtra(), new ResponseSessionURLOutputFilter(response))));
      }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
  
}

