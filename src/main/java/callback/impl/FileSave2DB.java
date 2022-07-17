package callback.impl;

import app.FileMeta;
import callback.FileScannerCallback;
import util.DBUtil;
import util.PinyinUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSave2DB implements FileScannerCallback {
    @Override
    public void callback(File dir) {
        // 列举出当前dir路径下的所有文件对象
        File[] files = dir.listFiles();

        if(files != null && files.length != 0) {
            // 1.先将当前dir下的所有文件信息保存到内存中,缓存中的信息一定是从os中读取到的最新数据
            List<FileMeta> locals = new ArrayList<>();
            for(File file : files) {
                FileMeta meta = new FileMeta();
                fileCommonSet(meta, file.getName(), file.getParent(), file.isDirectory(), new Date(file.lastModified()));
                if(!file.isDirectory()) {
                    // 如果不是文件，这是size
                    meta.setSize(file.length());
                }
                locals.add(meta);
            }

            // 2.扫描数据库文件
            List<FileMeta> dbFiles = query(dir);

            // 3.与数据库中的文件进行对比
            // 遍历本地目录，如果数据库中没有的，进行插入
            for(FileMeta meta : locals) {
                // 这里需要在FileMeta下覆写equals方法进行比较，这里使用lombok简化了
                // contains内部是通过equals比较的
                if(!dbFiles.contains(meta)) {
                    save(meta);
                }
            }

            // 遍历数据库文件，本地中没有的，则删除
            for(FileMeta meta : dbFiles) {
                if(!locals.contains(meta)) {
                    delete(meta);
                }
            }
        }
        // 若files = null || files.length == 0 说明该文件夹下就没有文件或者dir压根就不是文件夹，直接啥也不干
    }

    private void delete(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "delete from file_meta where (name = ? and path = ?)";
            if(meta.getIs_directory()) {
                // 如果是文件夹，还需要删除该文件夹下的所有文件
                sql += " or path = ?";// 删除一级目录
                sql += " or path like ?";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, meta.getName());
            ps.setString(2, meta.getPath());
            if(meta.getIs_directory()) {
                ps.setString(3, meta.getPath() + File.separator + meta.getName());
                ps.setString(4, meta.getPath() + File.separator + meta.getName()
                        + File.separator + "%");
            }
            int num = ps.executeUpdate();
//            System.out.println("执行了删除操作 : " + sql);
//            System.out.println("共删除" + num + "个文件");
        }catch (SQLException e) {
            System.out.println("数据删除失败!");
            e.getStackTrace();
        }finally {
            DBUtil.close(ps);
        }
    }

    /**
     * 将指定文件对象信息保存到数据库中
     * @param meta
     */
    private void save(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "insert into file_meta values(?,?,?,?,?,?,?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, meta.getName());
            ps.setString(2, meta.getPath());
            ps.setBoolean(3, meta.getIs_directory());
            if(!meta.getIs_directory()) {
                // 是普通文件的情况下才设置size
                ps.setLong(4, meta.getSize());
            }
            ps.setTimestamp(5, new Timestamp(meta.getLastModified().getTime()));
            // 只有文件名包含中文的时候，才需要传入数据
            if(DBUtil.hasChinese(meta.getName())) {
                String[] pinyin = PinyinUtil.getPinyinByFileName(meta.getName());
                ps.setString(6, pinyin[0]);
                ps.setString(7, pinyin[1]);
            }
            int num = ps.executeUpdate();
            //System.out.println("执行了保存操作 : " + sql);
        }catch (SQLException e) {
            System.out.println("数据插入失败!");
            e.getStackTrace();
        }finally {
            DBUtil.close(ps);
        }
    }

    private List<FileMeta> query(File dir) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> dbFiles = new ArrayList<>();
        try {
            connection = DBUtil.getConnection();
            String sql = "select name,path,is_directory,size,last_modified from file_meta" +
                    " where path = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, dir.getPath());
            rs = ps.executeQuery();
            //System.out.println("查询指定路径的SQL为 : " + ps);
            while(rs.next()) {
                FileMeta meta = new FileMeta();
                meta.setName(rs.getString("name"));
                meta.setPath(rs.getString("path"));
                meta.setIsDirectory(rs.getBoolean("is_directory"));
                // 如果是文件夹就不设置大小，默认为null，我们不能省略这个文件夹判断
                // 因为通过getLong方法返回的一定是一个数值，如果数据库里的数据是null就返回0了
                if(!meta.getIs_directory()) {
                    // 是普通文件
                    meta.setSize(rs.getLong("size"));
                }
                meta.setLastModified(new Date(rs.getTimestamp("last_modified").getTime()));
                dbFiles.add(meta);
            }

        } catch (SQLException e) {
            System.out.println("文件查询出错!请检查SQL语句");
            e.getStackTrace();
        } finally {
            DBUtil.close(ps, rs);
        }
        return dbFiles;
    }

    // 封装一个设置FileMeta属性的方法
    private void fileCommonSet(FileMeta fileMeta, String name, String path, boolean isDirectory, Date lastModified) {
        fileMeta.setName(name);
        fileMeta.setPath(path);
        fileMeta.setIsDirectory(isDirectory);
        fileMeta.setLastModified(lastModified);
    }
}
