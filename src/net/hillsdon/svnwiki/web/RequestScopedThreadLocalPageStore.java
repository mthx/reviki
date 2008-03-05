package net.hillsdon.svnwiki.web;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public class RequestScopedThreadLocalPageStore implements PageStore {

  private final ThreadLocal<PageStore> _threadLocal = new ThreadLocal<PageStore>();
  private final PageStoreFactory _factory;
  
  public RequestScopedThreadLocalPageStore(final PageStoreFactory factory) {
    _factory = factory;
  }
  
  public void create(final HttpServletRequest request) throws PageStoreException {
    _threadLocal.set(_factory.newInstance(request));
  }
  
  public void destroy() {
    _threadLocal.set(null);
  }
  
  private PageStore get() {
    return _threadLocal.get();
  }
  
  public PageInfo get(final String path) throws PageStoreException {
    return get().get(path);
  }

  public Collection<String> list() throws PageStoreException {
    return get().list();
  }

  public List<ChangeInfo> recentChanges() throws PageStoreException {
    return get().recentChanges();
  }

  public void set(final String path, final String lockToken, final long baseRevision, final String content) throws PageStoreException {
    get().set(path, lockToken, baseRevision, content);
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    return get().tryToLock(path);
  }

  public void unlock(final String page, String lockToken) throws PageStoreException {
    get().unlock(page, lockToken);
  }

}
