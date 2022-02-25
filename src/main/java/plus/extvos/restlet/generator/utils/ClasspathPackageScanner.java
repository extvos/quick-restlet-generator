package plus.extvos.restlet.generator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author Mingcai SHEN
 */
public class ClasspathPackageScanner implements PackageScanner {

    private static final Logger log = LoggerFactory.getLogger(ClasspathPackageScanner.class);

    private final String basePackage;

    private final ClassLoader cl;

    /**
     * 初始化
     *
     * @param basePackage in string
     */
    public ClasspathPackageScanner(String basePackage) {
        this.basePackage = basePackage;
        this.cl = getClass().getClassLoader();
    }

    public ClasspathPackageScanner(String basePackage, ClassLoader cl) {
        this.basePackage = basePackage;
        this.cl = cl;
    }

    /**
     * 获取指定包下的所有字节码文件的全类名
     */
    @Override
    public List<String> getFullyQualifiedClassNames() throws IOException {
//        log.info("开始扫描包 {} 下的所有类 ...", basePackage);
        return doScan(basePackage, new ArrayList<String>());
    }

    @Override
    public List<Class<?>> getFullyQualifiedClasses() throws IOException, ClassNotFoundException {
        List<Class<?>> glasses = new LinkedList<>();
        List<String> names = doScan(basePackage, new ArrayList<String>());
        for (String name : names) {
            Class<?> cls = cl.loadClass(name);
            glasses.add(cls);
        }
        return glasses;
    }

    /**
     * doScan函数
     *
     * @param basePackage
     * @param nameList
     * @return
     * @throws IOException
     */
    private List<String> doScan(String basePackage, List<String> nameList) throws IOException {
        String splashPath = StringUtil.dotToSplash(basePackage);
        URL url = cl.getResource(splashPath);
        //file:/D:/WorkSpace/java/ScanTest/target/classes/com/scan
        if (null == url) {
            throw new IOException("can not load path for package: " + basePackage);
        }
        String filePath = StringUtil.getRootPath(url);
        List<String> names = null;
        // contains the name of the class file. e.g., Apple.class will be stored as "Apple"
        if (isJarFile(filePath)) {
            // 先判断是否是jar包，如果是jar包，通过JarInputStream产生的JarEntity去递归查询所有类
//            if (log.isDebugEnabled()) {
//                log.debug("{} 是一个JAR包", filePath);
//            }
            names = readFromJarFile(filePath, splashPath);
        } else {
//            if (log.isDebugEnabled()) {
//                log.debug("{} 是一个目录", filePath);
//            }
            names = readFromDirectory(filePath);
        }
        if (null != names) {
            for (String name : names) {
                if (isClassFile(name)) {
                    nameList.add(toFullyQualifiedName(name, basePackage));
                } else {
                    doScan(basePackage + "." + name, nameList);
                }
            }
        }

//        if (log.isDebugEnabled()) {
//            for (String n : nameList) {
//                log.debug("找到{}", n);
//            }
//        }
        return nameList;
    }

    private String toFullyQualifiedName(String shortName, String basePackage) {
        StringBuilder sb = new StringBuilder(basePackage);
        sb.append('.');
        sb.append(StringUtil.trimExtension(shortName));
        return sb.toString();
    }

    private List<String> readFromJarFile(String jarPath, String splashedPackageName) throws IOException {
//        if (log.isDebugEnabled()) {
//            log.debug("从JAR包中读取类: {}", jarPath);
//        }
        JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
        JarEntry entry = jarIn.getNextJarEntry();
        List<String> nameList = new ArrayList<String>();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                nameList.add(name);
            }

            entry = jarIn.getNextJarEntry();
        }

        return nameList;
    }

    private List<String> readFromDirectory(String path) {
        File file = new File(path);
        String[] names = file.list();

        if (null == names) {
            return null;
        }

        return Arrays.asList(names);
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }

    private boolean isJarFile(String name) {
        return name.endsWith(".jar");
    }

    /**
     * For test purpose.
     * @param args arguments
     * @throws Exception if error
     */
    public static void main(String[] args) throws Exception {
        PackageScanner scan = new ClasspathPackageScanner("plus.extvos.restlet");
        scan.getFullyQualifiedClasses().forEach(cls -> {
            System.out.println(">>> " + cls.getName());
        });
    }
}