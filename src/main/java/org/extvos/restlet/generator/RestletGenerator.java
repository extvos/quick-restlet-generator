package org.extvos.restlet.generator;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.extvos.restlet.generator.config.Config;
import org.extvos.restlet.generator.dialect.MySQLTypeConvert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.extvos.restlet.generator.dialect.PostgreSQLTypeConvert;
import org.apache.commons.cli.*;
import org.yaml.snakeyaml.Yaml;

/**
 * @author shenmc
 */
public class RestletGenerator {
    /**
     * 配置 GeneratorConfig 后, 执行main方法生成代码
     */
    public static void main(String[] args) {
        Options opts = new Options();

        opts.addOption(new Option("c", "config", true, "Configuration filename"));
        opts.addOption(new Option("p", "project", true, "Project root path"));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(opts, args);
            String cfgFilename = cmd.getOptionValue("config", ".config.yml");
            String projPath = cmd.getOptionValue("project", ".");
            Yaml yaml = new Yaml();
            Config cfg = yaml.loadAs(new FileInputStream(cfgFilename), Config.class);
            new RestletGenerator().generate(projPath, cfg);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", opts);
            System.exit(1);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", opts);
            System.exit(1);
        }

    }


    private GlobalConfig buildGlobalConfig(final String projectPath, Config config) {
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        //生成文件的输出目录
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor(config.getAuthor());
        //是否展开输出目录
        gc.setOpen(false);
        //实体属性 Swagger2 注解
        gc.setSwagger2(true);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        gc.setDateType(DateType.ONLY_DATE);
        gc.setEntityName("%s");
        gc.setControllerName("%sController");
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        gc.setFileOverride(true);
        return gc;
    }

    private DataSourceConfig buildDataSource(Config config) {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDriverName(config.getDriverName());
        dsc.setUrl(config.getUrl());
        dsc.setUsername(config.getUsername());
        dsc.setPassword(config.getPassword());
        dsc.setDbType(config.getDbType());
        //设置mysql中的字段类型和java中属性类型的对应关系
        switch (config.getDbType()) {
            case MYSQL:
                dsc.setTypeConvert(new MySQLTypeConvert());
                break;
            case POSTGRE_SQL:
                dsc.setTypeConvert(new PostgreSQLTypeConvert());
                break;
            default:
                break;
        }
        return dsc;
    }

    private PackageConfig buildPackageConfig(Config config) {
        PackageConfig pc = new PackageConfig();
        pc.setParent(config.getParent());
        pc.setModuleName(config.getModuleName());
        pc.setController("controller");
        pc.setEntity("entity");
        pc.setMapper("mapper");
        pc.setXml("mapper");
        pc.setService("service");
        pc.setServiceImpl("service.impl");
        return pc;
    }


    private void generate(final String projectPath, Config config) {

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        GlobalConfig gc = buildGlobalConfig(projectPath, config);
        //设置全局配置
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = buildDataSource(config);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = buildPackageConfig(config);
        mpg.setPackageInfo(pc);

        // 自定义配置  调整 xml 生成目录
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {

            }
        };
        // 如果模板引擎是 freemarker
        String templatePath = "/templates/generator/mapper.xml.ftl";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<FileOutConfig>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/"
                    + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        // 关闭 默认 xml文件生成
        templateConfig.setXml(null);
        // 关闭默认 xml 生成，调整生成 至 根目录
        templateConfig.setController("/templates/generator/controller.java");
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        //表名生成策略  下划线转驼峰
        strategy.setNaming(NamingStrategy.underline_to_camel);
        //字段名生成策略  下划线转驼峰
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //启动lombok
        strategy.setEntityLombokModel(true);
        //实体类添加注解
        //strategy.entityTableFieldAnnotationEnable(true);
        //生成 @RestController 控制器
        strategy.setRestControllerStyle(true);
        //写于父类中的公共字段
//        strategy.setSuperEntityColumns("deleted", "create_by", "create_time", "update_by", "update_time");

//        strategy.setSuperEntityClass("org.extvos.restlet.base.entity.BaseEntity");
        strategy.setSuperControllerClass("org.extvos.restlet.controller.BaseController");
        strategy.setSuperServiceClass("org.extvos.restlet.service.BaseService");
        strategy.setSuperServiceImplClass("org.extvos.restlet.service.impl.BaseServiceImpl");
        strategy.setSuperMapperClass("com.baomidou.mybatisplus.core.mapper.BaseMapper");
//        strategy.setEntitySerialVersionUID(true);
        //表名，多个英文逗号分割
        strategy.setInclude(config.getTables());
        //驼峰转连字符
        strategy.setControllerMappingHyphenStyle(true);
        //设置表前缀
        //strategy.setTablePrefix(pc.getModuleName() + "_");
        strategy.setTablePrefix(config.getTablePrefix());

        //设置逻辑删除字段
//        strategy.setLogicDeleteFieldName("deleted");

        mpg.setStrategy(strategy);
        //设置模板引擎
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

}