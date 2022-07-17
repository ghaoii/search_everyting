package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite数据库的工具类，创建数据源，创建数据库的连接
 * 只向外部提供SQLite数据库的连接即可，数据源不提供(封装在工具类的内部)
 * 无论是哪种关系型数据库，操作的流程都是JDBC四步走
 */
public class DBUtil {
    // 单例数据源
    private volatile static DataSource DATASOURCE;
    // 单例数据库连接
    private volatile static Connection CONNECTION;

    private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";

    // 获取数据源方法，使用double-check单例模式获取数据源对象
    private static DataSource getDataSource() {
        if(DATASOURCE == null) {
            // 这里把类的class对象作为锁的对象
            // 如果在静态方法上用synchronized关键字，锁的也是类的class对象
            // 不过在这里上锁，锁的粒度会更细
            synchronized (DBUtil.class) {
                if(DATASOURCE == null) {
                    // SQLite没有账户密码
                    // 需要配置日期格式即可，因为SQLite默认的日期格式是时间戳
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATA_PATTERN);
                    DATASOURCE = new SQLiteDataSource(config);
                    // 配置数据源的URL是SQLite子类独有的方法，因此向下转型
                    ((SQLiteDataSource) DATASOURCE).setUrl(getUrl());
                }
            }
        }
        return DATASOURCE;
    }

    public static Connection getConnection() throws SQLException {
        if(CONNECTION == null) {
            synchronized (DBUtil.class) {
                if(CONNECTION == null) {
                    CONNECTION = getDataSource().getConnection();
                }
            }
        }
        return CONNECTION;
    }

    /**
     * 配置SQLite数据库的地址
     * 对MySQL来说：jdbc:mysql://127.0.0.1:3306/数据库名称?
     * 对于SQLite数据库来说，没有服务端和客户端，因此只需要指定SQLite数据库的地址即可
     * @return
     */
    private static String getUrl() {
        String path = "/Users/harley/Desktop/code/search_everything/target";
        // File.separator是File类的常量，是不同操作系统的文件分隔符
        String url = "jdbc:sqlite://" + path + File.separator + "search_everything.db";
        System.out.println("获取数据库的连接为 : " + url);
        return url;
    }


    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }

    public static void close(Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void close(Statement statement, ResultSet resultSet) {
        close(statement);
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean hasChinese(String name) {
        // match方法是看字符串与括号中的正则表达式相匹配
        return name.matches(".*" + CHINESE_PATTERN + ".*");
    }
}
