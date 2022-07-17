package util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 在界面初始化时，创建文件信息数据表
 */
public class DBInit {

//    public static void main(String[] args) throws FileNotFoundException {
//        //InputStream is = new FileInputStream("src/main/resources/init.sql");
//        InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("init.sql");
//        System.out.println(is);
//    }

    /**
     * 从resources路径下读取init.sql文件，加载到程序中
     * @return
     */
    public static List<String> readSQL() {
        List<String> ret = new ArrayList<>();
        // 从init.sql文件中获取内容，需要拿到文件的输入流
        InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("init.sql");
        Scanner scanner = new Scanner(is);
        // 自定义分隔符
        scanner.useDelimiter(";");
        // nextLine默认碰到换行分隔，next按照自定义的分隔符拆分
        while (scanner.hasNext()) {
            String str = scanner.next();
            if (str.equals("") || str.equals("\n")) {
                continue;
            }
//            if (str.contains("-- ")) {
//                str = str.replaceAll("--", "");
//            }
            ret.add(str);
        }
        System.out.println("读取到的SQL内容为：");
        System.out.println(ret);
        return ret;
    }

    /**
     * 界面初始化时，先初始化数据库，创建数据库
     */
    public static void init() {
        Connection connection = null;
        Statement statement = null;
        try {
            List<String> sqls = readSQL();
            connection = DBUtil.getConnection();
            statement = connection.createStatement();
            for(String sql : sqls) {
                System.out.println("执行SQL操作 : " + sql);
                statement.executeUpdate(sql);
            }
        }catch (SQLException e) {
            System.out.println("数据库初始化失败!");
            e.getStackTrace();
        }finally {
            DBUtil.close(statement);
        }
    }

    public static void main(String[] args) {
        init();
    }
}
