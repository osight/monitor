package com.osight.monitor.boot;

import java.net.URL;
import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface ClassPathResolver {
    boolean verify();

    BootstrapJarFile getBootstrapJarFile();

    String getBootStrapCoreJar();

    List<URL> resolveLib();

    URL[] resolvePlugins();
}
