package plus.extvos.restlet.generator.utils;

import java.net.URL;

/**
 * @author Mingcai SHEN
 */
public class StringUtil {
    private StringUtil() {

    }

    /**
     * "file:/home/whf/cn/fh" -&gt; "/home/whf/cn/fh"
     * "jar:file:/home/whf/foo.jar!cn/fh" -&gt; "/home/whf/foo.jar"
     * @param url url of root
     * @return String of path
     */
    public static String getRootPath(URL url) {
        String fileUrl = url.getFile();
        int pos = fileUrl.indexOf('!');

        if (-1 == pos) {
            return fileUrl;
        }

        return fileUrl.substring(5, pos);
    }

    /**
     * "cn.fh.lightning" -&gt; "cn/fh/lightning"
     *
     * @param name in string
     * @return String of splash
     */
    public static String dotToSplash(String name) {
        return name.replaceAll("\\.", "/");
    }

    /**
     * "Apple.class" -&gt; "Apple"
     * @param name in String
     * @return String of extension
     */
    public static String trimExtension(String name) {
        int pos = name.indexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }

        return name;
    }

    public static String trimPackageName(String name) {
        int pos = name.lastIndexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }
        return name;
    }

    /**
     * /application/home -&gt; /home
     *
     * @param uri in string
     * @return String of URI
     */
    public static String trimURI(String uri) {
        String trimmed = uri.substring(1);
        int splashIndex = trimmed.indexOf('/');
        return trimmed.substring(splashIndex);
    }
}