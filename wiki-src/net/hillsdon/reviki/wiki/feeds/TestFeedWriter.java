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
package net.hillsdon.reviki.wiki.feeds;

import static net.hillsdon.xml.xpathcontext.Coercion.CONTEXT;
import static net.hillsdon.xml.xpathcontext.Coercion.NUMBER;
import static net.hillsdon.xml.xpathcontext.Coercion.STRING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.StoreKind;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.xml.xpathcontext.XPathContext;
import net.hillsdon.xml.xpathcontext.XPathContextFactory;

import org.xml.sax.InputSource;

public class TestFeedWriter extends TestCase {

  private static final String FEED_URL = "http://www.example.com/dooby/RecentChanges/feed";
  private static final String POUND_SIGN = "\u00a3";
  private static final String TITLE = "Reviki Feed Title";

  public void test() throws Exception {
    List<ChangeInfo> changes = Arrays.asList(new ChangeInfo("SomeWikiPage", "SomeWikiPage", "mth", new Date(0), 123, "Change description with special character " + POUND_SIGN, StoreKind.PAGE, ChangeType.MODIFIED, null, -1));
    WikiUrls urls = new WikiUrls() {
      public String feed() {
        return "this isn't used";
      }
      public String page(final String wikiName, final String name) {
        return "page";
      }
      public URI page(final String pageName) {
        URI root = URI.create(pagesRoot());
        try {
          return new URI(root.getScheme(), root.getUserInfo(), root.getHost(), root.getPort(), root.getPath() + "/" + pageName, root.getQuery(), root.getFragment());
        }
        catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }
      public String pagesRoot() {
        return "root";
      }
      public String pagesRoot(final String wikiName) {
        return null;
      }
      public String search() {
        return "search";
      }
      public String resource(final String path) {
        return "favicon";
      }
      public String page(final String wikiName, final String pageName, final String extraPath, final String query, final String fragment) {
        return "page";
      }
      public WikiConfiguration getWiki() {
        throw new UnsupportedOperationException();
      }
      public String interWikiTemplate() {
        throw new UnsupportedOperationException();
      }
      public String getWikiName() {
        return "foo";
      }
    };
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new AtomFeedWriter(urls).writeAtom(TITLE, FEED_URL, changes, out);
    InputSource input = new InputSource(new ByteArrayInputStream(out.toByteArray()));

    try {
      XPathContext feed = XPathContextFactory.newInstance().newXPathContext(input);
      feed.setNamespaceBindings(Collections.singletonMap("atom", AtomFeedWriter.ATOM_NS));

      assertEquals(FEED_URL, feed.evaluate("atom:feed/atom:link/@href", STRING));
      assertEquals(FEED_URL, feed.evaluate("atom:feed/atom:id", STRING));
      assertEquals(TITLE, feed.evaluate("atom:feed/atom:title", STRING));
      assertEquals(1.0, feed.evaluate("count(//atom:entry)", NUMBER));
      XPathContext entry = feed.evaluate("atom:feed/atom:entry", CONTEXT);
      assertEquals("page?revision=123", entry.evaluate("atom:id", STRING));
      assertEquals("Change description with special character " + POUND_SIGN, entry.evaluate("atom:summary", STRING));
    }
    catch (RuntimeException ex) {
      // Seems to be available in Eclipse, on the command line on my development box, but not elsewhere.
      // Need to fix the rubbish RuntimeException too...
      System.err.println("Skipping " + getClass().getName() + " because " + ex.getMessage());
    }
  }

}
