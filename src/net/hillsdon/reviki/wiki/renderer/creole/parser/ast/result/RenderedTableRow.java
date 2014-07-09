package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class RenderedTableRow implements ResultNode {

  protected List<ResultNode> rows;

  public RenderedTableRow(List<ResultNode> rows) {
    this.rows = rows;
  }

  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(rows);
  }

  public String toXHTML() {
    String out = "<tr>";

    for (ResultNode node : rows) {
      out += node.toXHTML();
    }

    out += "</tr>";

    return out;
  }
}