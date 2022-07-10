package app;

import callback.impl.FileSave2DB;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import task.FileScanner;
import util.DBInit;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author yuisama
 * @date 2022/07/06 15:07
 **/
public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    // 接收扫描到的文件的结果集
    // 由于我们需要用这个结果集刷新界面，
    List<FileMeta> fileMetas;

    // 该方法覆写了Initializable接口中的抽象方法
    // 是点击运行项目，界面初始化时加载的一个方法
    // 就相当于运行一个主类，首先要加载主类的静态块一个道理
    public void initialize(URL location, ResourceBundle resources) {
        // 想要在界面初始化的时候初始化数据库
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件
        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }

    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();
        // 在界面中显示完整的路径
        this.srcDirectory.setText(path);
        // 根据选择的目录文件，扫描该目录下所有的文件
        System.out.println("开始扫描文件夹......");
        FileScanner fileScanner = new FileScanner(new FileSave2DB());
        long start = System.nanoTime();
        fileScanner.scan(file);
        long end = System.nanoTime();
        System.out.println("扫描到的文件夹数量是 : " + fileScanner.getDirNum());
        System.out.println("扫描到的文件数量是 : " + fileScanner.getFileNum());
        System.out.println("共耗时 : " + (end - start) * 1.0 / 100_0000);
        // 获取扫描后的所有文件内容
        this.fileMetas = fileScanner.getFileMetas();

        // TODO 接收扫描完之后的文件，刷新界面，让扫描到的文件显示在界面
    }

    // 刷新表格数据(得到扫描结果集的办法有两个，一个是传参，一个是设置成员变量)
    // 这里建议使用成员变量
    private void freshTable(){
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        // TODO 扫描文件夹之后刷新界面
        if(fileMetas != null) {
            metas.addAll(fileMetas);
        }
    }

}