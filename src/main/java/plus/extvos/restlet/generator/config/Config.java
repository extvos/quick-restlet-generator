package plus.extvos.restlet.generator.config;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mingcai SHEN
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    /**
     * 作者
     */
    private String author;
    /**
     * 项目根目录
     */
    private String parent;
    /**
     * 业务模块
     */
    private String moduleName;
    /**
     * 去除表名前缀,无前缀留空
     */
    private String tablePrefix;
    /**
     * 表名
     */
    private String[] tables;
    /**
     * 数据库
     */
    private DbType dbType;
    /**
     * mysql8版本写法, mysql5 : com.mysql.jdbc.Driver
     */
    private String driverName;
    private String url;
    private String username;
    private String password;

}
