package org.extvos.restlet.generator.gen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Mingcai SHEN
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageOption implements Cloneable {


    @Parameter(name = "source", required = true)
    private String source;

    @Parameter(name = "target")
    private String target;

    @Parameter(name = "mapper")
    private String mapper;

    @Parameter(name = "service")
    private String service;

    @Parameter(name = "serviceImpl")
    private String serviceImpl;

    @Parameter(name = "controller")
    private String controller;

    @Parameter(name = "readOnly")
    private boolean readOnly;

    @Parameter(name = "prefix")
    private String prefix;

    @Parameter(name = "skipMapper")
    private boolean skipMapper;

    @Parameter(name = "skipService")
    private boolean skipService;

    @Parameter(name = "skipServiceImpl")
    private boolean skipServiceImpl;

    @Parameter(name = "skipController")
    private boolean skipController;

    @Parameter(name = "author")
    private String author;

    @Parameter(name = "forceOverride")
    private boolean forceOverride;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
