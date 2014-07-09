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
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result.RenderedUnorderedList;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class UnorderedList extends ImmutableRenderNode {

  protected RenderNode body;

  protected List<RenderNode> children;

  public UnorderedList(RenderNode body, List<RenderNode> children) {
    this.body = body;
    this.children = children;
  }

  public UnorderedList(RenderNode body) {
    this(body, new ArrayList<RenderNode>());
  }

  public UnorderedList(List<RenderNode> children) {
    this(new Plaintext(""), children);
  }

  public List<RenderNode> getChildren() {
    List<RenderNode> out = new ArrayList<RenderNode>();
    out.add(body);
    out.addAll(children);
    return Collections.unmodifiableList(out);
  }

  public List<ResultNode> render(PageInfo page, String text, RenderNode parent, URLOutputFilter urlOutputFilter) {
    List<ResultNode> body = this.body.render(page, text, this, urlOutputFilter);
    assert (body.size() == 1);

    List<ResultNode> children = new ArrayList<ResultNode>();

    for (RenderNode node : this.children) {
      List<ResultNode> res = node.render(page, text, this, urlOutputFilter);
      assert (res.size() == 1);
      children.add(res.get(0));
    }

    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(new RenderedUnorderedList(body.get(0), children));
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