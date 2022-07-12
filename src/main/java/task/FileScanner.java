package task;

import app.FileMeta;
import callback.FileScannerCallback;
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

    // 文件扫描回调对象
    private FileScannerCallback callback;

    public FileScanner(FileScannerCallback callback) {
        this.callback = callback;
    }

    // 文件扫描的结果集，存放被扫描目录下的所有文件(包括目录)
    //List<FileMeta> fileMetas = new ArrayList<>();

    /**
     * 传入目录文件，扫描目录下的所有文件
     * @param dir
     */
    public void scan(File dir) {
        if(dir == null) {
            return;
        }
        // 使用回调函数，将当前目录下的所有内容保存到指定终端
        this.callback.callback(dir);
        // 先将当前这一级目录下的file对象获取出来
        File[] files = dir.listFiles();
        // 遍历files下的所有文件
        for(File file : files) {
            FileMeta fileMeta = new FileMeta();
            if(file.isDirectory()) {
                // 如果文件是个目录，就继续递归扫描目录
                dirNum++;
                scan(file);
            }else {
                // 是普通文件
                fileNum++;
            }
        }
    }
}
