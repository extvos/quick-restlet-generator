package plus.extvos.restlet.generator.gen;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.swagger.annotations.ApiModel;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import plus.extvos.common.Validator;
import plus.extvos.restlet.generator.utils.StringUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mingcai SHEN
 */
public class Generator {
    private Class<?> cls;
    private TableInfo tableInfo;
    private PackageOption opts;
    private Configuration ftlConf;
    private boolean configured;

    private Log log;

    private final String entityComment;
    private final String entityClassBaseName;
    private final String entityClassName;
    private final String entityPackageName;
    private final String basePackageName;
    private String sourceRoot;
    private String mapperClassName;
    private String mapperClassBaseName;
    private String serviceClassName;
    private String serviceClassBaseName;
    private String serviceImplClassName;
    private String serviceImplClassBaseName;
    private String controllerClassName;
    private String controllerClassBaseName;
    private String controllerPrefix;

    public static Generator from(Class<?> clazz) throws MojoExecutionException {
        try {
            if (null == clazz.getAnnotation(TableName.class)) {
                return null;
            } else {
                return new Generator(clazz);
            }
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    private Generator(Class<?> clazz) {
        cls = clazz;
        entityClassName = cls.getName();
        entityClassBaseName = cls.getSimpleName();
        entityPackageName = StringUtil.trimPackageName(entityClassName);
        basePackageName = StringUtil.trimPackageName(entityPackageName);
        tableInfo = TableInfoHelper.getTableInfo(cls);
        if (cls.isAnnotationPresent(ApiModel.class)) {
            entityComment = cls.getAnnotation(ApiModel.class).value();
        } else {
            entityComment = entityClassBaseName;
        }
    }

    protected Log getLog() {
        if (null == log) {
            log = new SystemStreamLog();
        }
        return log;
    }

    public Generator config(Log log) {
        this.log = log;
        return this;
    }

    private String buildTargetName(String s, String src, String dst, String suffix) {
        if (dst.endsWith(".*")) {
            return s.replace(src, StrUtil.strip(dst, ".*")) + suffix;
        } else {
            return dst;
        }
    }

    public Generator config(String sourceRoot, PackageOption opt) throws MojoExecutionException {
        if (!new File(sourceRoot).exists()) {
            throw new MojoExecutionException("sourceRoot '" + sourceRoot + "' not exists");
        }
        this.sourceRoot = sourceRoot;
        for (Field f : cls.getDeclaredFields()) {
            if (f.getType().isPrimitive()) {
                getLog().error("primitive property is not allowed: " + entityClassName + "." + f.getName());
                throw new MojoExecutionException("primitive property is not allowed: " + entityClassName + "." + f.getName());
            }
        }
        try {
            this.opts = (PackageOption) opt.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        if (Validator.isEmpty(opts.getSource()) && !entityClassName.startsWith(opts.getSource())) {
            throw new MojoExecutionException("entity class is not a in source package: " + entityClassName);
        }

        /* Adjust the package configs,. */
        if (opts.getSource().endsWith(".*")) {
            opts.setSource(StrUtil.strip(opts.getSource(), ".*"));
        } else {
            opts.setSource(StrUtil.strip(opts.getSource(), "."));
        }


        if (Validator.isEmpty(opts.getTarget())) {
            opts.setTarget(opts.getSource().endsWith(".*") ? opts.getSource() : opts.getSource() + ".*");
        }
        if (Validator.isEmpty(opts.getMapper())) {
            opts.setMapper(buildTargetName(basePackageName, opts.getSource(), opts.getTarget(), ".mapper"));
//            opts.setMapper(basePackageName.replace(opts.getSource(), opts.getTarget()) + ".mapper");
        } else {
//            opts.setMapper(basePackageName.replace(opts.getSource(), opts.getMapper()) + ".mapper");
            opts.setMapper(buildTargetName(basePackageName, opts.getSource(), opts.getMapper(), ".mapper"));
        }
        if (Validator.isEmpty(opts.getService())) {
//            opts.setService(basePackageName.replace(opts.getSource(), opts.getTarget()) + ".service");
            opts.setService(buildTargetName(basePackageName, opts.getSource(), opts.getTarget(), ".service"));
        } else {
            opts.setService(buildTargetName(basePackageName, opts.getSource(), opts.getService(), ".service"));
        }
        if (Validator.isEmpty(opts.getServiceImpl())) {
//            opts.setServiceImpl(basePackageName.replace(opts.getSource(), opts.getTarget()) + ".service.impl");
            opts.setServiceImpl(buildTargetName(basePackageName, opts.getSource(), opts.getTarget(), ".service.impl"));
        } else {
            opts.setServiceImpl(buildTargetName(basePackageName, opts.getSource(), opts.getServiceImpl(), ".service.impl"));
        }
        if (Validator.isEmpty(opts.getController())) {
//            opts.setController(basePackageName.replace(opts.getSource(), opts.getTarget()) + ".controller");
            opts.setController(buildTargetName(basePackageName, opts.getSource(), opts.getTarget(), ".controller"));
        } else {
            opts.setController(buildTargetName(basePackageName, opts.getSource(), opts.getController(), ".controller"));
        }

        /* Adjust the skipping configs */
//        if (!opts.isSkipController()) {
//            opts.setSkipServiceImpl(false);
//        }
//        if (!opts.isSkipServiceImpl()) {
//            opts.setSkipService(false);
//        }
//        if (!opts.isSkipService()) {
//            opts.setSkipMapper(false);
//        }

        /* Make the class names */
        mapperClassBaseName = entityClassBaseName + "Mapper";
        mapperClassName = opts.getMapper() + "." + mapperClassBaseName;
        serviceClassBaseName = entityClassBaseName + "Service";
        serviceClassName = opts.getService() + "." + serviceClassBaseName;
        serviceImplClassBaseName = entityClassBaseName + "ServiceImpl";
        serviceImplClassName = opts.getServiceImpl() + "." + serviceImplClassBaseName;
        controllerClassBaseName = entityClassBaseName + "Controller";
        controllerClassName = opts.getController() + "." + controllerClassBaseName;

        String p = StrUtil.toUnderlineCase(entityClassBaseName).replaceAll("_", "-");
        controllerPrefix = opts.getPrefix() != null ? opts.getPrefix() + "/" + p : "/" + p;

        ftlConf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        ClassTemplateLoader loader = new ClassTemplateLoader(
            getClass(), "/generator");
        ftlConf.setTemplateLoader(loader);
        // Where do we load the templates from:
        // ftlConf.setClassForTemplateLoading(getClass(), "");
        // Some other recommended settings
        ftlConf.setDefaultEncoding("UTF-8");
        ftlConf.setLocale(Locale.CHINESE);
        ftlConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        configured = true;

        return this;
    }

    public void display() {
        getLog().info("Generator of Class<" + cls.getSimpleName() + ">");
        getLog().info("    Source Package: " + opts.getSource());
        getLog().info("    Target Package: " + opts.getTarget());
        if (!opts.isSkipMapper()) {
            getLog().info("    Mapper Package: " + opts.getMapper());
        }
        if (!opts.isSkipService()) {
            getLog().info("    Service Package: " + opts.getService());
        }
        if (!opts.isSkipServiceImpl()) {
            getLog().info("    ServiceImpl Package: " + opts.getServiceImpl());
        }
        if (!opts.isSkipController()) {
            getLog().info("    Controller Package: " + opts.getController());
        }

        getLog().info("    Base Prefix: " + opts.getPrefix());
        getLog().info("    Entity Class: " + cls.getName());

        if (!opts.isSkipMapper()) {
            getLog().info("    Mapper Class: " + StringUtil.dotToSplash(mapperClassName) + ".java");
        }
        if (!opts.isSkipService()) {
            getLog().info("    Service Class: " + StringUtil.dotToSplash(serviceClassName) + ".java");
        }
        if (!opts.isSkipServiceImpl()) {
            getLog().info("    ServiceImpl Class: " + StringUtil.dotToSplash(serviceImplClassName) + ".java");
        }
        if (!opts.isSkipController()) {
            getLog().info("    Controller Class: " + StringUtil.dotToSplash(controllerClassName) + ".java");
        }
    }

    public void generate() throws MojoExecutionException {
        if (!configured) {
            throw new MojoExecutionException("generator not configured");
        }

        Map<String, Object> m = new HashMap<>();

        m.put("entityPackage", entityPackageName);
        m.put("entityClass", entityClassName);
        m.put("entityClassName", entityClassBaseName);

        m.put("mapperPackage", opts.getMapper());
        m.put("mapperClass", mapperClassName);
        m.put("mapperClassName", mapperClassBaseName);

        m.put("servicePackage", opts.getService());
        m.put("serviceClass", serviceClassName);
        m.put("serviceClassName", serviceClassBaseName);

        m.put("serviceImplPackage", opts.getServiceImpl());
        m.put("serviceImplClass", serviceImplClassName);
        m.put("serviceImplClassName", serviceImplClassBaseName);

        m.put("controllerPackage", opts.getController());
        m.put("controllerClass", controllerClassName);
        m.put("controllerClassName", controllerClassBaseName);

        m.put("author", opts.getAuthor());

        m.put("controllerComment", entityComment);
        m.put("serviceImplComment", entityComment);
        m.put("serviceComment", entityComment);
        m.put("mapperComment", entityComment);

        m.put("controllerPrefix", controllerPrefix);

        m.put("readOnly", opts.isReadOnly());

        if (!opts.isSkipMapper()) {
            generateMapper(m);
        }

        if (!opts.isSkipService()) {
            generateService(m);
        }

        if (!opts.isSkipServiceImpl()) {
            generateServiceImpl(m);
        }

        if (!opts.isSkipController()) {
            generateController(m);
        }
    }

    private void writeFileByTemplate(Template tmpl, File file, Map<String, Object> values) throws MojoExecutionException {
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new MojoExecutionException("create dir '" + dir.getAbsolutePath() + "' failed");
                }
            } else if (!dir.isDirectory()) {
                throw new MojoExecutionException("'" + dir.getAbsolutePath() + "' is not a valid directory");
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new MojoExecutionException("create file '" + file.getAbsolutePath() + "' failed");
                }
            }
            FileWriter fileWriter = new FileWriter(file);
            tmpl.process(values, fileWriter);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    public void generateController(Map<String, Object> m) throws MojoExecutionException {
        getLog().info("Generating Controller:> " + entityClassName + " -> " + controllerClassName);
        File f = new File(sourceRoot + "/" + StringUtil.dotToSplash(controllerClassName) + ".java");
        if (f.exists() && !opts.isForceOverride()) {
            getLog().info("'" + controllerClassName + "' already exists, skipped");
            return;
        }

        Template template;
        try {
            template = ftlConf.getTemplate("controller.java.ftl");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        writeFileByTemplate(template, f, m);
    }

    public void generateServiceImpl(Map<String, Object> m) throws MojoExecutionException {
        getLog().info("Generating ServiceImpl:> " + entityClassName + " -> " + serviceImplClassName);
        File f = new File(sourceRoot + "/" + StringUtil.dotToSplash(serviceImplClassName) + ".java");
        if (f.exists() && !opts.isForceOverride()) {
            getLog().info("'" + serviceImplClassName + "' already exists, skipped");
            return;
        }

        Template template;
        try {
            template = ftlConf.getTemplate("service-impl.java.ftl");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        writeFileByTemplate(template, f, m);
    }

    public void generateService(Map<String, Object> m) throws MojoExecutionException {
        getLog().info("Generating Service:> " + entityClassName + " -> " + serviceClassName);
        File f = new File(sourceRoot + "/" + StringUtil.dotToSplash(serviceClassName) + ".java");
        if (f.exists() && !opts.isForceOverride()) {
            getLog().info("'" + serviceClassName + "' already exists, skipped");
            return;
        }

        Template template;
        try {
            template = ftlConf.getTemplate("service.java.ftl");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        writeFileByTemplate(template, f, m);
    }

    public void generateMapper(Map<String, Object> m) throws MojoExecutionException {
        getLog().info("Generating Mapper:> " + entityClassName + " -> " + mapperClassName);
        File f = new File(sourceRoot + "/" + StringUtil.dotToSplash(mapperClassName) + ".java");
        if (f.exists() && !opts.isForceOverride()) {
            getLog().info("'" + mapperClassName + "' already exists, skipped");
            return;
        }

        Template template;
        try {
            template = ftlConf.getTemplate("mapper.java.ftl");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        writeFileByTemplate(template, f, m);
    }

}
