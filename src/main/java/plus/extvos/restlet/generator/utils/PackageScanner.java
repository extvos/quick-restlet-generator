package plus.extvos.restlet.generator.utils;

import java.io.IOException;
import java.util.List;

/**
 * @author Mingcai SHEN
 */
public interface PackageScanner {
    /**
     * get class name list
     *
     * @return list of class names
     * @throws IOException if error
     */
    List<String> getFullyQualifiedClassNames() throws IOException;

    /**
     * get class list
     *
     * @return list of classes
     * @throws IOException            if error
     * @throws ClassNotFoundException if error
     */
    List<Class<?>> getFullyQualifiedClasses() throws IOException, ClassNotFoundException;
}
