package net.hillsdon.svnwiki.wiki.plugin;

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;

/**
 * A really simple system for plugins.
 * 
 * The relative entries in manifest entry Class-Path are assumed to point to jars
 * in the root of the plugin jar.
 * 
 * The classes constributed by this plugin are defined by the Plugin-Contributions
 * manifest parameter (space separated).
 * 
 * @author mth
 */
public class PluginClassLoader extends URLClassLoader {

  private final List<Class<?>> _contributedClasses;

  public PluginClassLoader(final URL jarUrl, final ClassLoader parent) throws InvalidPluginException {
    super(new URL[] {jarUrl}, parent);
    InputStream manifestInputStream = null;
    try {
      final URL manifestUrl = getManifestURL(jarUrl);
      manifestInputStream = manifestUrl.openStream();
      final Manifest manifest = new Manifest(manifestInputStream);
      
      final Attributes attrs = manifest.getMainAttributes();
      final String classPath = attrs.getValue("Class-Path");
      if (classPath == null) {
        throw new InvalidPluginException("No Class-Path specified in plugin manifest.");
      }
      cacheClassPathEntries(jarUrl, classPath);
      
      String pluginContributions = attrs.getValue("Plugin-Contributions");
      if (pluginContributions == null) {
        pluginContributions = "";
      }
      _contributedClasses = unmodifiableList(loadPluginContributionClasses(pluginContributions));
    }
    catch (URISyntaxException ex) {
      throw new InvalidPluginException(ex);
    }
    catch (IOException ex) {
      throw new InvalidPluginException(ex);
    }
    catch (ClassNotFoundException ex) {
      throw new InvalidPluginException(ex);
    }
    finally {
      IOUtils.closeQuietly(manifestInputStream);
    }
  }

  /**
   * @return Contributed classes in contribution order.
   */
  public List<Class<?>> getContributedClasses() {
    return _contributedClasses;
  }
  
  private List<Class<?>> loadPluginContributionClasses(final String pluginContributions) throws ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    for (String className : pluginContributions.split("\\s")) {
      classes.add(findClass(className));
    }
    return classes;
  }

  private void cacheClassPathEntries(final URL jarUrl, final String classPath) throws IOException {
    for (String entry : classPath.split("\\s")) {
      URL entryUrl = new URL("jar:" + jarUrl.toString() + "!/" + entry);
      File file = File.createTempFile("cached-", entry);
      file.deleteOnExit();
      InputStream in = entryUrl.openStream();
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(file);
        IOUtils.copy(in, out);
      }
      catch (IOException ex) {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
      addURL(file.toURI().toURL());
    }
  }

  private URL getManifestURL(final URL jarUrl) throws IOException, URISyntaxException, InvalidPluginException {
    // Note this has no parent, ensuring we get the correct manifest for our jar.
    URL url = new URLClassLoader(new URL[] {jarUrl}, null).getResource("META-INF/MANIFEST.MF");
    if (url == null) {
      throw new InvalidPluginException("No manifest found in given jar.");
    }
    return url;
  }

}