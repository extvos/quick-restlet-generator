package ${controllerPackage};

import ${entityClass};
import ${serviceClass};
import org.extvos.restlet.controller.<#if readOnly>BaseROController<#else>BaseController</#if>;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * ${controllerComment}
 *
 * @author ${author}
 */
@RestController
@RequestMapping("${controllerPrefix}")
@Api(tags = {"${controllerComment}"})
public class ${controllerClassName} extends <#if readOnly>BaseROController<#else>BaseController</#if><${entityClassName}, ${serviceClassName}> {

    @Autowired
    private ${serviceClassName} myService;

    @Override
    public ${serviceClassName} getService() {
        return myService;
    }

}
