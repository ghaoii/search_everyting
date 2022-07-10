package app;

/**
 * @author yuisama
 * @date 2022/07/06 15:08
 * 这个类就对应数据库表名
 **/

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import util.Util;

import java.util.Date;

@NoArgsConstructor
@Data
@EqualsAndHashCode
public class FileMeta {
    private String name;
    private String path;
    private Boolean is_directory;
    private Long size;
    private Date lastModified;
    // 把中文字符替换成拼音
    private String pinyin;
    // 把中文替换成拼音的首字母
    private String pinyinFir;

    // 以下三个属性用于界面展示，因此需要对原来的三个属性做特殊处理
    // 这些属性的名称和app.fxml中保持一致(因此建议拷贝)
    private String isDirectoryText;// 文件类型
    private String sizeText;// 文件大小
    private String lastModifiedText;// 文件最后修改日期


    public void setIsDirectory(boolean isDirectory) {
        this.is_directory = isDirectory;
        this.isDirectoryText = Util.parseDirectory(isDirectory);
    }

    public void setSize(long size) {
        this.size = size;
        this.sizeText = Util.parseSize(size);
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        this.lastModifiedText = Util.parseLastModifiedType(lastModified);
    }

    public FileMeta(String name, String path, boolean is_directory, long size, Date lastModified) {
        this.name = name;
        this.path = path;
        this.is_directory = is_directory;
        this.size = size;
        this.lastModified = lastModified;
    }
}