# quick-restlet-generator

是针对`quick-lib-restlet`的一个代码生成器，生成器以`Maven`插件的形式体现。

# 简单流程

编写 Entity的Java类 -> 编译 -> 扫描生成相应的`Mapper`、`Service`、`ServiceImpl`、`Controller`等Java类

# 使用方式

## `Entity`的定义

由于底层基本采用Mybatis-Plus的实现，所以`Entity`的定义需要在类型上面添加`@TableName`的注解，插件扫描时候以这个为条件。

## `pom.xml`配置

在`pom.xml`里面配置插件

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.extvos</groupId>
            <artifactId>restlet-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <!-- 指定插件在 compile 后自动执行 -->
                    <phase>compile</phase> 
                    <goals>
                        <goal>gen</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!-- 作者, 可以在运行时通过 -Dauthor=XXX 来指定 -->
                <author>AUTHOR</author>
                
                <!-- 是否覆盖已经存在的Java文件，该配置会覆盖package里面的配置， 可以在运行时通过 -DforceOverWrite=true|false 来指定 -->
                <forceOverWrite>true|false</forceOverWrite> 
                
                <packages> <!-- 至少配置一个需要扫描的包 -->
                    <package>
                        <source>plus.extvos.example</source> <!-- 源Entity所在基础包名,必须项 -->
                        <!-- 以下为可选项 -->
                        
                        <!-- 作者 -->
                        <author>AUTHOR</author>
                        
                        <!-- 生成文件的目标基础包名,未提供时则使用与source相同的基础包名 -->
                        <target>plus.extvos.example.generated.*</target> 
                        
                        <!-- 生成mapper的目标基础包名，未提供时则根据target生成 {target}.mapper -->
                        <mapper>plus.extvos.example.generated.mapper</mapper>  
                        
                        <!-- 生成service的目标基础包名，未提供时则根据target生成 {target}.service -->
                        <service>plus.extvos.example.generated.service</service>  
                        
                        <!-- 生成serviceImpl的目标基础包名，未提供时则根据target生成 {target}.service.impl -->
                        <serviceImpl>plus.extvos.example.generated.service.impl</serviceImpl>  
                        
                        <!-- 生成controller的目标基础包名，未提供时则根据target生成 {target}.controller -->
                        <controller>plus.extvos.example.generated.controller</controller>  
                        
                        <!-- 是否只生成只读控制的Controller -->
                        <readOnly>true|false</readOnly>  
                        
                        <!-- 是否跳过生成mapper -->
                        <skipMapper>true|false</skipMapper>  
                        
                        <!-- 是否跳过生成service -->
                        <skipService>true|false</skipService>  
                        
                        <!-- 是否跳过生成serviceImpl -->
                        <skipServiceImpl>true|false</skipServiceImpl>  
                        
                        <!-- 是否跳过生成controller -->
                        <skipController>true|false</skipController>  
                        
                        <!-- 生成Controller的RequestMapping前缀路径 -->
                        <prefix>/example</prefix>  
                        
                        <!-- 作者 -->
                        <author>AUTHOR</author>  
                        
                        <!-- 是否覆盖已经存在的Java文件 -->
                        <forceOverWrite>true|false</forceOverWrite>  
                    </package>
                </packages>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 调用

```shell
$ mvn restlet:gen
$ mvn -Dauthor=extvos restlet:gen
$ mvn -DforceOverWrite=true restlet:gen
```

