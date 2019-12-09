package com.guosen.zebra.core.grpc.client;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import com.google.common.collect.Sets;
public class GrpcClassLoader extends URLClassLoader {

  private static final String API_REPOSITORY =
      System.getProperty("user.home") + File.separator + "zebra";

  private Set<URL> cachedJarUrls = Sets.newConcurrentHashSet();

  private ClassLoader systemClassLoader;

  public GrpcClassLoader() {
    super(new URL[] {}, null);
    this.addURL();
  }

  private void addURL() {
    File file = new File(API_REPOSITORY);
    if (file.exists() && file.isDirectory()) {
      String[] jars = file.list(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          if (name.endsWith(".jar")) {
            return true;
          }
          return false;
        }
      });
      for (int i = 0; i < jars.length; i++) {
        try {
          URL url = new File(file.getAbsolutePath(), jars[i]).toURI().toURL();
          if (cachedJarUrls.contains(url)) {
            continue;
          }
          cachedJarUrls.add(url);
          super.addURL(url);
        } catch (MalformedURLException e) {
          // igore
        }
      }
    }
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    Class<?> clazz = null;
    clazz = resolveClassPath(name, resolve);
    if (clazz == null) {
      clazz = resolveSystemClassLoader(name, resolve);
    }
    if (clazz == null) {
      throw new ClassNotFoundException(name);
    }
    return clazz;
  }

  private Class<?> resolveClassPath(String name, boolean resolve) {
    try {
      Class<?> clazz = super.loadClass(name, resolve);
      return clazz;
    } catch (ClassNotFoundException ex) {
      // Ignore.
    }

    return null;
  }

  private Class<?> resolveSystemClassLoader(String name, boolean resolve) {
    if (systemClassLoader != null) {
      try {
        Class<?> clazz = systemClassLoader.loadClass(name);
        if (resolve) {
          resolveClass(clazz);
        }
        return clazz;
      } catch (ClassNotFoundException ex) {
        // Ignore.
      }
    }

    return null;
  }

  public void setSystemClassLoader(ClassLoader classLoader) {
    this.systemClassLoader = classLoader;
  }
}
