package task;

import app.FileMeta;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 根据选择的文件夹路径和用户输入的内容从数据库中查找处指定的内容并返回结果集
 */
public class FileSearch {

    /**
     *
     * @param dir 用户选择的检索的文件夹，dir一定不为空
     * @param str 用户搜索框中的内容，可能为空，如果为空就展示指定文件夹下的所有内容
     * @return
     */
    public static List<FileMeta> search(String dir, String str) {
        List<FileMeta> ret = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DBUtil.getConnection();
            // 先对用户选择的文件夹进行搜索
            String sql = "select name,path,is_directory,size,last_modified from file_meta"
                    + " where (path = ? or path like ?)";// 一级目录和多级目录的搜索
            // 如果用户在搜索框中输入了内容，我们增加搜索条件
            if(str != null && str.trim().length() != 0) {
                // 根据用户输入的内容，在文件名、拼音、拼音首字母中进行模糊查询
                sql += " and (name like ? or pinyin like ? or pinyin_first like ?)";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, dir);
            ps.setString(2, dir + File.separator + "%");
            // 如果搜索框不为空，则设置参数
            if(str != null && str.trim().length() != 0) {
                ps.setString(3, "%" + str + "%");
                ps.setString(4, "%" + str + "%");
                ps.setString(5, "%" + str + "%");
            }
            System.out.println("正在按用户输入内容检索文件, SQL为 : " + ps);
            rs = ps.executeQuery();
            while(rs.next()) {
                FileMeta meta = new FileMeta();
                meta.setName(rs.getString("name"));
                meta.setPath(rs.getString("path"));
                meta.setIsDirectory(rs.getBoolean("is_directory"));
                if(!meta.getIs_directory()) {
                    meta.setSize(rs.getLong("size"));
                }
                meta.setLastModified(new Date(rs.getTimestamp("last_modified").getTime()));
                ret.add(meta);
            }
        }catch (SQLException e) {
            System.out.println("搜索用户输入文件时出错! 请检查SQL语句!");
            e.getStackTrace();
        }finally {
            DBUtil.close(connection, ps, rs);
        }
        return ret;
    }
}
