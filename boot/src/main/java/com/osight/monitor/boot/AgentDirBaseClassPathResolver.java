package com.osight.monitor.boot;

import static java.util.regex.Pattern.compile;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class AgentDirBaseClassPathResolver implements ClassPathResolver {
    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";
    static final Pattern DEFAULT_AGENT_PATTERN = compile("monitor-boot" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_CORE_PATTERN = compile("monitor-core" + VERSION_PATTERN + "\\.jar");

    private final Pattern agentPattern;
    private final Pattern agentCorePattern;

    private String classPath;

    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;

    private List<String> fileExtensionList;
    private String bootStrapCoreJar;

    private BootstrapJarFile bootstrapJarFile;

    AgentDirBaseClassPathResolver() {
        this(getClassPathFromSystemProperty());
    }

    AgentDirBaseClassPathResolver(String classPath) {
        this.classPath = classPath;
        this.agentPattern = DEFAULT_AGENT_PATTERN;
        this.agentCorePattern = DEFAULT_AGENT_CORE_PATTERN;
        this.fileExtensionList = getDefaultFileExtensionList();
    }

    @Override
    public boolean verify() {
        final BootstrapJarFile bootstrapJarFile = new BootstrapJarFile();
        final boolean agentJarNotFound = this.findAgentJar();
        if (!agentJarNotFound) {
            logger.warn("monitor-boot-x.x.x(-SNAPSHOT).jar not found.");
            return false;
        }

        final String bootStrapCoreJar = getBootStrapCoreJar();
        if (bootStrapCoreJar == null) {
            logger.warn("monitor-core-x.x.x(-SNAPSHOT).jar not found");
            return false;
        }
        JarFile bootStrapCoreJarFile = getJarFile(bootStrapCoreJar);
        if (bootStrapCoreJarFile == null) {
            logger.warn("monitor-core-x.x.x(-SNAPSHOT).jar not found");
            return false;
        }
        bootstrapJarFile.append(bootStrapCoreJarFile);

        this.bootstrapJarFile = bootstrapJarFile;
        return true;
    }

    @Override
    public List<URL> resolveLib() {
        String agentLibPath = getAgentLibPath();
        File libDir = new File(agentLibPath);
        if (!libDir.exists()) {
            logger.warn(agentLibPath + " not found");
            return Collections.emptyList();
        }
        if (!libDir.isDirectory()) {
            logger.warn(agentLibPath + " not Directory");
            return Collections.emptyList();
        }
        final List<URL> jarURLList = new ArrayList<URL>();

        final File[] findJarList = findJar(libDir);
        if (findJarList != null) {
            for (File file : findJarList) {
                URL url = toURI(file);
                if (url != null) {
                    jarURLList.add(url);
                }
            }
        }

        URL agentDirUri = toURI(new File(agentLibPath));
        if (agentDirUri != null) {
            jarURLList.add(agentDirUri);
        }
        jarURLList.add(toURI(new File(getBootStrapCoreJar())));
        return jarURLList;
    }

    @Override
    public BootstrapJarFile getBootstrapJarFile() {
        return bootstrapJarFile;
    }

    public String getBootStrapCoreJar() {
        return bootStrapCoreJar;
    }

    @Override
    public URL[] resolvePlugins() {
        final File file = new File(getAgentPluginPath());

        if (!file.exists()) {
            logger.warn(file + " not found");
            return new URL[0];
        }

        if (!file.isDirectory()) {
            logger.warn(file + " is not a directory");
            return new URL[0];
        }


        final File[] jars = file.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (isEmpty(jars)) {
            return new URL[0];
        }

        final URL[] urls = new URL[jars.length];


        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Fail to load plugin jars", e);
            }
        }

        for (File pluginJar : jars) {
            logger.info("Found plugins: " + pluginJar.getPath());
        }

        return urls;
    }


    public String getAgentPluginPath() {
        return this.agentDirPath + File.separator + "plugin";
    }

    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            logger.warn(file.getName() + ".toURL() failed.", e);
            return null;
        }
    }


    public String getAgentLibPath() {
        return this.agentDirPath + File.separator + "lib";
    }

    private File[] findJar(File libDir) {
        return libDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                for (String extension : fileExtensionList) {
                    if (path.lastIndexOf("." + extension) != -1) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    boolean findAgentJar() {
        Matcher matcher = agentPattern.matcher(classPath);
        if (!matcher.find()) {
            return false;
        }
        this.agentJarName = parseAgentJar(matcher);
        this.agentJarFullPath = parseAgentJarPath(classPath, agentJarName);
        if (agentJarFullPath == null) {
            return false;
        }
        this.agentDirPath = parseAgentDirPath(agentJarFullPath);
        if (agentDirPath == null) {
            return false;
        }

        logger.info("Agent original-path:" + agentDirPath);
        // defense alias change
        this.agentDirPath = toCanonicalPath(agentDirPath);
        logger.info("Agent canonical-path:" + agentDirPath);


        this.bootStrapCoreJar = findFromBootDir("monitor-core.jar", agentCorePattern);
        return true;
    }

    private String parseAgentJar(Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();
        return this.classPath.substring(start, end);
    }

    private String parseAgentJarPath(String classPath, String agentJar) {
        String[] classPathList = classPath.split(File.pathSeparator);
        for (String findPath : classPathList) {
            boolean find = findPath.contains(agentJar);
            if (find) {
                return findPath;
            }
        }
        return null;
    }

    private String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int max = Math.max(index1, index2);
        if (max == -1) {
            return null;
        }
        return agentJarFullPath.substring(0, max);
    }

    private String toCanonicalPath(String path) {
        final File file = new File(path);
        return toCanonicalPath(file);
    }

    private String toCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage(), e);
            return file.getAbsolutePath();
        }
    }

    private String findFromBootDir(final String name, final Pattern pattern) {
        String bootDirPath = agentDirPath + File.separator + "boot";
        final File[] files = listFiles(name, pattern, bootDirPath);
        if (isEmpty(files)) {
            logger.info(name + " not found.");
            return null;
        } else if (files.length == 1) {
            File file = files[0];
            return toCanonicalPath(file);
        } else {
            logger.info("too many " + name + " found. " + Arrays.toString(files));
            return null;
        }
    }

    private boolean isEmpty(File[] files) {
        return files == null || files.length == 0;
    }

    private File[] listFiles(final String name, final Pattern pattern, String bootDirPath) {
        File bootDir = new File(bootDirPath);
        return bootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {

                    logger.info("found " + name + ". " + dir.getAbsolutePath() + File.separator + fileName);
                    return true;
                }
                return false;
            }
        });
    }

    private JarFile getJarFile(String jarFilePath) {
        try {
            return new JarFile(jarFilePath);
        } catch (IOException ioe) {
            logger.warn(jarFilePath + " file not found. Error:" + ioe.getMessage(), ioe);
            return null;
        }
    }

    List<String> getDefaultFileExtensionList() {
        List<String> extensionList = new ArrayList<String>();
        extensionList.add("jar");
        extensionList.add("xml");
        extensionList.add("properties");
        return extensionList;
    }

    static String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }
}
