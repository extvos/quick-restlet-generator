package ${serviceImplPackage};

import ${entityClass};
import ${mapperClass};
import ${serviceClass};
import plus.extvos.restlet.service.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ${serviceImplComment}
 *
 * @author ${author}
 */
@Service
public class ${serviceImplClassName} extends BaseServiceImpl<${mapperClassName}, ${entityClassName}> implements ${serviceClassName} {

    @Autowired
    private ${mapperClassName} myMapper;

    @Override
    public ${mapperClassName} getMapper() {
        return myMapper;
    }

}
