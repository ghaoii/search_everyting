package callback;

import java.io.File;

/**
 * 文件信息的回调接口
 */
public interface FileScannerCallback {
    /**
     * 文件扫描的回调接口，扫描文件时由具体的子类决定将当前目录下的文件信息持久化到哪个终端
     * 可以是数据库，也可以通过网络传输
     */
    void callback(File dir);
}
