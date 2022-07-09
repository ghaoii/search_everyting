package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通用工具类
 */
public class Util {
    // 配置SQLite的日期格式
    public static final String DATA_PATTERN = "yyyy-MM-dd hh:mm:ss";

    public static String parseLastModifiedType(Date lastModified) {
        return new SimpleDateFormat(DATA_PATTERN).format(lastModified);
    }

    /**
     * 根据传入的文件大小返回不同的单位
     * 支持的单位如下 B,KB,MB,GB
     * @param size
     * @return
     */
    public static String parseSize(long size) {
        String[] sizeUnit = {"B", "KB", "MB", "GB"};
        int flag = 0;
        while(size > 1024) {
            size /= 1024;
            flag++;
        }
        return size + sizeUnit[flag];
    }

    public static String parseDirectory(boolean isDirectory) {
        return isDirectory ? "文件夹" : "文件";
    }
}
