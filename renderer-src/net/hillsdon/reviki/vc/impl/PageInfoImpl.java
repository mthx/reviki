package net.hillsdon.reviki.vc.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SyntaxFormats;

public class PageInfoImpl extends PageReferenceImpl implements PageInfo {

  private final String _wiki;
  private final String _content;
  private final Map<String, String> _attributes;

  // For testing purposes.
  public PageInfoImpl(final String path) {
    this("", path, "", Collections.<String, String>emptyMap());
  }

  public PageInfoImpl(final String wiki, final String path, final String content, final Map<String, String> attributes) {
    super(path);
    _wiki = wiki;
    _content = content;
    _attributes = attributes;
  }

  @Override
  public String getWiki() {
    return _wiki;
  }

  @Override
  public String getContent() {
    return _content;
  }

  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  @Override
  public SyntaxFormats getSyntax(final AutoPropertiesApplier propsApplier) {
    String syntax = getAttributes().get("syntax");
    if (syntax != null) {
      SyntaxFormats format = SyntaxFormats.fromValue(syntax);
      if (format != null) {
        return format;
      }
    }
    if (propsApplier != null) {
      propsApplier.read();
      for (Entry<String, String> entry : propsApplier.apply(getName()).entrySet()) {
        if ("reviki:syntax".equals(entry.getKey())) {
          return SyntaxFormats.fromValue(entry.getValue());
        }
      }
    }
    return SyntaxFormats.REVIKI;
  }

  @Override
  public PageInfo withAlternativeContent(final String content) {
    return new PageInfoImpl(_wiki, super.getPath(), content, _attributes);
  }

  @Override
  public PageInfo withAlternativeAttributes(final Map<String, String> attributes) {
    return new PageInfoImpl(_wiki, super.getPath(), _content, attributes);
  }
}
