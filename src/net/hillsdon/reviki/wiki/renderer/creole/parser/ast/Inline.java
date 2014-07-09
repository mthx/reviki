package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result.RenderedInline;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Inline extends ImmutableRenderNode {

  protected List<RenderNode> chunks;

  public Inline(List<RenderNode> chunks) {
    this.chunks = chunks;
  }

  public List<RenderNode> getChildren() {
    return Collections.unmodifiableList(chunks);
  }

  public List<ResultNode> render(PageInfo page, String text, RenderNode parent, URLOutputFilter urlOutputFilter) {
    List<ResultNode> chunks = new ArrayList<ResultNode>();

    for (RenderNode node : this.chunks) {
      List<ResultNode> res = node.render(page, text, this, urlOutputFilter);
      assert (res.size() == 1);
      chunks.add(res.get(0));
    }

    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(new RenderedInline(chunks));
    return out;
  }

  public Matcher find(String text) {
    // TODO Auto-generated method stub
    return null;
  }

  public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    // TODO Auto-generated method stub
    return null;
  }
}