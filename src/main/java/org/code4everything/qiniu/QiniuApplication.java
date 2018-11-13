package org.code4everything.qiniu;

import com.zhazhapan.util.ThreadPool;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.model.ConfigBean;
import org.code4everything.qiniu.model.FileInfo;
import org.code4everything.qiniu.util.ConfigUtils;
import org.code4everything.qiniu.util.DialogUtils;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author pantao
 */
public class QiniuApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(QiniuApplication.class);

    public static ObservableList<FileInfo> data = null;

    /**
     * 空间总文件数
     */
    public static int totalLength = 0;

    /**
     * 空间使用总大小
     */
    public static long totalSize = 0;

    private static Stage stage = null;

    private static ConfigBean configBean;

    public static Stage getStage() {
        return stage;
    }

    public static ConfigBean getConfigBean() {
        return configBean;
    }

    public static void setConfigBean(ConfigBean configBean) {
        QiniuApplication.configBean = configBean;
    }

    /**
     * 主程序入口
     */
    public static void main(String[] args) {
        // 设置线程池大小
        ThreadPool.setMaximumPoolSize(10);
        // 设置线程池最大排队大小
        ThreadPool.setWorkQueue(new LinkedBlockingQueue<>(1024));
        ThreadPool.init();
        // 启动 JavaFX 应用
        launch(args);
    }

    /**
     * 由 JavaFX 调用
     */
    @Override
    public void start(Stage stage) {
        try {
            // 加载视图页面
            VBox root = FXMLLoader.load(getClass().getResource(QiniuValueConsts.QINIU_VIEW_URL));
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            LOGGER.error("init stage error: " + e.getMessage());
            DialogUtils.showFatalError(QiniuValueConsts.INIT_APP_ERROR_HEADER, e);
        }
        // 设置图标
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/qiniu.png")));
        stage.setTitle(QiniuValueConsts.MAIN_TITLE);
        // 设置关闭窗口事件
        stage.setOnCloseRequest(event -> {
            // 判断是否有文件在上传下载
            MainWindowController main = MainWindowController.getInstance();
            if (main.downloadProgress.isVisible() || main.uploadProgress.isVisible()) {
                Optional<ButtonType> result = DialogUtils.showConfirmation(QiniuValueConsts.UPLOADING_OR_DOWNLOADING);
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    // 取消退出事件
                    event.consume();
                    return;
                }
            }
            // 退出程序
            ThreadPool.executor.shutdown();
            System.exit(0);
        });
        QiniuApplication.stage = stage;
        stage.show();
        // 加载配置文件
        ConfigUtils.loadConfig();
    }
}
