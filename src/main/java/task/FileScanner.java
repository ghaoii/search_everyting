package task;

import app.FileMeta;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
public class FileScanner {

    // 被扫描目录下文件的个数
    private int fileNum;
    // 被扫描目录下(包括被扫描目录)的文件夹个数
    private int dirNum = 1;

    // 文件扫描的结果集，存放被扫描目录下的所有文件(包括目录)
    List<FileMeta> fileMetas = new ArrayList<>();

    /**
     * 传入目录文件，扫描目录下的所有文件
     * @param dir
     */
    public void scan(File dir) {
        if(dir == null) {
            return;
        }
        // 先将当前这一级目录下的file对象获取出来
        File[] files = dir.listFiles();
        // 遍历files下的所有文件
        for(File file : files) {
            FileMeta fileMeta = new FileMeta();
            if(file.isDirectory()) {
                // 如果文件是个目录，就继续递归扫描目录
                // 这里的lastModified方法返回的是个长整型，是时间戳，因此转化成Date
                fileCommonSet(fileMeta, file.getName(), file.getPath(), file.isDirectory(), new Date(file.lastModified()));
                dirNum++;
                fileMetas.add(fileMeta);// 在这里添加，能让深度优先搜索的结果更好看
                scan(file);
            }else {
                // 是普通文件
                fileCommonSet(fileMeta, file.getName(), file.getPath(), file.isDirectory(), new Date(file.lastModified()));
                fileMeta.setSize(file.length());// 只对普通文件设置大小
                fileNum++;
                fileMetas.add(fileMeta);
            }
            //fileMetas.add(fileMeta);
        }
    }

    // 封装一个设置FileMeta属性的方法
    private void fileCommonSet(FileMeta fileMeta, String name, String path, boolean isDirectory, Date lastModified) {
        fileMeta.setName(name);
        fileMeta.setPath(path);
        fileMeta.setIsDirectory(isDirectory);
        fileMeta.setLastModified(lastModified);
    }
}
