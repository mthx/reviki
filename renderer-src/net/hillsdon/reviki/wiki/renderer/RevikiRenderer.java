package net.hillsdon.reviki.wiki.renderer;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleBasedRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Anchor;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Blockquote;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Bold;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Code;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Heading;
import net.hillsdon.reviki.wiki.renderer.creole.ast.HorizontalRule;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Image;
import net.hillsdon.reviki.wiki.renderer.creole.ast.InlineCode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.InlineNowiki;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Italic;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Linebreak;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Link;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ListItem;
import net.hillsdon.reviki.wiki.renderer.creole.ast.MacroNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Nowiki;
import net.hillsdon.reviki.wiki.renderer.creole.ast.OrderedList;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Paragraph;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Strikethrough;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Table;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableCell;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableHeaderCell;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableRow;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TextNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.UnorderedList;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Render to HTML. This is largely a direct translation of the old
 * ASTNode.toXHTML() style of rendering, with few changes.
 *
 * @author msw
 */
public class RevikiRenderer extends CreoleBasedRenderer {
  /**
   * Most elements have a consistent CSS class. Links and images are an
   * exception (as can be seen in their implementation), as their HTML is
   * generated by a link handler.
   */
  public static final String CSS_CLASS_ATTR = "class='wiki-content'";

  public RevikiRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    super(pageStore, linkHandler, imageHandler, macros);
  }

  public RevikiRenderer(final LinkResolutionContext resolver) {
    super(resolver);
  }

  @Override
  public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    HtmlVisitor visitor = new HtmlVisitor(urlOutputFilter);
    return visitor.visit(ast);
  }

  @Override
  public String getContentType() {
    return "text/html; charset=utf-8";
  }

  private static final class HtmlVisitor extends ASTRenderer<String> {
    public HtmlVisitor(final URLOutputFilter urlOutputFilter) {
      super("", urlOutputFilter);
    }

    @Override
    protected String combine(final String x1, final String x2) {
      return x1 + x2;
    }

    /**
     * Render a node with a tag.
     */
    public String renderTagged(final String tag, final Optional<? extends ASTNode> node) {
      // Render the tag
      if (!node.isPresent()) {
        return "<" + tag + " " + CSS_CLASS_ATTR + " />";
      }
      else {
        return "<" + tag + " " + CSS_CLASS_ATTR + ">" + visitASTNode(node.get()) + "</" + tag + ">";
      }
    }


    /**
    * Render some syntax-highlighted code.
    */
    public String highlight(final String code, final String language) {
      String codetag;
      if (language.isEmpty()) {
        codetag = "<code>";
      }
      else {
        codetag = String.format("<code class='%s'>", Escape.html(language));
      }
      return String.format("%s%s</code>", codetag, code);
    }

    @Override
    public String visitAnchor(final Anchor node) {
      return String.format("<a %s id=\"%s\"></a>", CSS_CLASS_ATTR, node.getAnchor());
    }

    @Override
    public String visitBlockquote(final Blockquote node) {
      return renderTagged("blockquote", Optional.of(node));
    }

    @Override
    public String visitBold(final Bold node) {
      return renderTagged("strong", Optional.of(node));
    }

    @Override
    public String visitCode(final Code node) {
      Optional<String> lang = node.getLanguage();
      String code = Escape.html(node.getText());
      String out = "<pre " + CSS_CLASS_ATTR + ">";
      if (lang.isPresent()) {
        out += highlight(code, lang.get());
      } else {
        out += code;
      }
      out += "</pre>";
      return out;
    }

    @Override
    public String visitNowiki(final Nowiki node) {
      return String.format("<pre %s>%s</pre>", CSS_CLASS_ATTR, Escape.html(node.getText()));
    }

    @Override
    public String visitHeading(final Heading node) {
      return renderTagged("h" + node.getLevel(), Optional.of(node));
    }

    @Override
    public String visitHorizontalRule(final HorizontalRule node) {
      return renderTagged("hr", Optional.<ASTNode> absent());
    }

    @Override
    public String visitImage(final Image node) {
      LinkPartsHandler handler = node.getHandler();
      PageInfo page = node.getPage();
      LinkParts parts = node.getParts();

      try {
        return handler.handle(page, Escape.html(parts.getText()), parts, urlOutputFilter());
      }
      catch (Exception e) {
        return Escape.html(parts.getText());
      }
    }

    @Override
    public String visitInlineCode(final InlineCode node) {
      String codeClass;
      if (node.getLanguage().isPresent() && !node.getLanguage().get().isEmpty()) {
        codeClass = " " + Escape.html(node.getLanguage().get());
      }
      else {
        codeClass = "";
      }
      return String.format("<code class='wiki-content inline%s'>%s</code>", codeClass, Escape.html(node.getText()));
    }

    @Override
    public String visitInlineNowiki(final InlineNowiki node) {
      return String.format("<code>%s</code>", Escape.html(node.getText()));
    }

    @Override
    public String visitItalic(final Italic node) {
      return renderTagged("em", Optional.of(node));
    }

    @Override
    public String visitLinebreak(final Linebreak node) {
      return renderTagged("br", Optional.<ASTNode> absent());
    }

    @Override
    public String visitLink(final Link node) {
      LinkPartsHandler handler = node.getHandler();
      PageInfo page = node.getPage();
      LinkParts parts = node.getParts();

      try {
        return handler.handle(page, Escape.html(parts.getText()), parts, urlOutputFilter());
      }
      catch (Exception e) {
        // Special case: render mailto: as a link if it didn't get interwiki'd
        String target = node.getTarget();
        String title = node.getTitle();
        if (target.startsWith("mailto:")) {
          return String.format("<a href='%s'>%s</a>", target, Escape.html(title));
        }
        else {
          return Escape.html(parts.getText());
        }
      }
    }

    @Override
    public String visitListItem(final ListItem node) {
      return renderTagged("li", Optional.of(node));
    }

    @Override
    public String visitMacroNode(final MacroNode node) {
      String tag = node.isBlock() ? "pre" : "code";
      String inner = Escape.html(node.getText());
      return "<" + tag + " " + CSS_CLASS_ATTR + ">" + inner + "</" + tag + ">";
    }

    @Override
    public String visitOrderedList(final OrderedList node) {
      return renderTagged("ol", Optional.of(node));
    }

    @Override
    public String visitParagraph(final Paragraph node) {
      return renderTagged("p", Optional.of(node));
    }

    @Override
    public String visitStrikethrough(final Strikethrough node) {
      return renderTagged("strike", Optional.of(node));
    }

    @Override
    public String visitTable(final Table node) {
      return renderTagged("table", Optional.of(node));
    }

    /** Render a table cell with vertical alignment. */
    protected String valign(final String tag, final ASTNode node) {
      if (!isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        return renderTagged(tag, Optional.of(node));
      }

      try {
        String out = "<" + tag + " " + CSS_CLASS_ATTR;
        out += " style='vertical-align:" + unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0) + "'>";
        out += visitASTNode(node);
        out += "</" + tag + ">";
        return out;
      }
      catch (Exception e) {
        System.err.println("Error when handling directive " + TABLE_ALIGNMENT_DIRECTIVE);
        return renderTagged(tag, Optional.of(node));
      }
    }

    @Override
    public String visitTableCell(final TableCell node) {
      return valign("td", node);
    }

    @Override
    public String visitTableHeaderCell(final TableHeaderCell node) {
      return valign("th", node);
    }

    @Override
    public String visitTableRow(final TableRow node) {
      return renderTagged("tr", Optional.of(node));
    }

    @Override
    public String visitTextNode(final TextNode node) {
      String text = node.getText();
      return node.isEscaped() ? Escape.html(text) : text;
    }

    @Override
    public String visitUnorderedList(final UnorderedList node) {
      return renderTagged("ul", Optional.of(node));
    }
  }
}
