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
package net.hillsdon.reviki.web.pages.impl;

import static java.lang.String.format;
import static net.hillsdon.reviki.text.WikiWordUtils.isWikiWord;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;

import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.pages.DefaultPage;

public class FindPage extends AbstractSpecialPage {

  // TODO: "<Image height='16' width='16' type='image/x-icon'>%s</Image>"
  private static final String OPENSEARCH_DESCRIPTION =
    "<?xml version='1.0' encoding='UTF-8'?>\n"
  + "<OpenSearchDescription xmlns='http://a9.com/-/spec/opensearch/1.1/'>\n"
  + "<ShortName>Wiki Search</ShortName>\n"
  + "<Description>Wiki Search</Description>\n"
  + "<Url type='text/html' template='%s?query={searchTerms}'/>\n"
  + "</OpenSearchDescription>\n";
  
  static final String PARAM_QUERY = "query";
  private static final String PARAM_QUERY_ALTERNATE = "q";

  
  private final PageStore _store;
  private final SearchEngine _searchEngine;

  public FindPage(final PageStore store, final SearchEngine searchEngine, final DefaultPage defaultPage) {
    super(defaultPage);
    _store = store;
    _searchEngine = searchEngine;
  }

  public View get(PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if ("opensearch.xml".equals(path.next())) {
      return new View() {
        public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
          response.setContentType("application/opensearchdescription+xml");
          response.getWriter().write(format(OPENSEARCH_DESCRIPTION, Escape.html(RequestBasedWikiUrls.get(request).search())));
        }
      };
    }
    String query = request.getParameter(PARAM_QUERY);
    if (query == null) {
      request.getParameter(PARAM_QUERY_ALTERNATE);
    }
    if (query == null) {
      return super.get(page, path, request, response);
    }
    
    boolean pageExists = _store.list().contains(new PageReference(query));
    if (request.getParameter("force") == null && pageExists) {
      return new RedirectView(RequestBasedWikiUrls.get(request).page(query));
    }
    
    final Set<SearchMatch> results = _searchEngine.search(query, true);
    Long limit = getLong(request.getParameter("limit"), "limit");
    if (limit != null) {
      results.retainAll(new ArrayList<SearchMatch>(results).subList(0, (int) Math.min(results.size(), limit)));
    }
    if ("txt".equals(request.getParameter("ctype"))) {
      return new TextFormatSearchResults(results);
    }
    else {
      if (!pageExists && isWikiWord(query)) {
        request.setAttribute("suggestCreate", query);
      }
      request.setAttribute("results", results);
      return new JspView("SearchResults");
    }
  }

  public String getName() {
    return "FindPage";
  }

}