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
import task.FileSearch;
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
    //List<FileMeta> fileMetas;

    private Thread scanThread;

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
        //System.out.println("开始扫描文件夹......");
        FileScanner fileScanner = new FileScanner(new FileSave2DB());
        if(scanThread != null) {
            // 如果上个任务还没有结束，用户就重新选择了其他文件夹，则中断当前正在扫描的任务
            scanThread.interrupt();
        }
        // 开启新线程扫描新选择的目录
        scanThread = new Thread(() -> {
            fileScanner.scan(file);
            freshTable();
        });
        scanThread.start();
    }

    private void freshTable(){
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();

        // 获取用户选择的文件
        String dir = srcDirectory.getText();
        // 保证用户选择的文件不为空
        if(dir != null && dir.trim().length() != 0) {
            // 界面中已经选择文件，此时已经将最新的数据保存到了数据库中
            // 只需要取出数据库中的内容展示到界面上
            // 获取用户在搜索框中输入的内容(可以为空)
            String str = searchField.getText();
            //根据选择的路径，和用户的输入，将数据库中指定内容刷新到界面
            List<FileMeta> fileSearched = FileSearch.search(dir, str);
            metas.addAll(fileSearched);
        }
    }

}