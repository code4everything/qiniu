package org.code4everything.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.zhazhapan.util.ThreadPool;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.config.ConfigBean;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.model.FileInfo;
import org.code4everything.qiniu.util.ConfigUtils;
import org.code4everything.qiniu.view.Dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author pantao
 */
public class QiniuApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(QiniuApplication.class);

    public static Map<String, Zone> zone = new HashMap<>();

    public static Stage stage = null;

    public static Auth auth = null;

    public static UploadManager uploadManager = null;

    public static Configuration configuration = null;

    public static BucketManager bucketManager = null;

    public static ObservableList<FileInfo> data = null;

    public static CdnManager cdnManager = null;

    /**
     * 空间总文件数
     */
    public static int totalLength = 0;

    /**
     * 空间使用总大小
     */
    public static long totalSize = 0;

    private static ConfigBean configBean;

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
        initApplication();
        // 启动 JavaFX 应用
        launch(args);
    }

    /**
     * 初始化应用
     */
    public static void initApplication() {
        // 加载空间区域
        zone.put(QiniuValueConsts.BUCKET_NAME_ARRAY[0], Zone.zone0());
        zone.put(QiniuValueConsts.BUCKET_NAME_ARRAY[1], Zone.zone1());
        zone.put(QiniuValueConsts.BUCKET_NAME_ARRAY[2], Zone.zone2());
        zone.put(QiniuValueConsts.BUCKET_NAME_ARRAY[3], Zone.zoneNa0());
        // 加载配置文件
        ConfigUtils.loadConfig();
    }

    /**
     * 窗口关闭事件
     *
     * @param event 事件
     * @param isExternalCall 是否是第三方应用调用
     */
    public static void setOnClosed(Event event, boolean isExternalCall) {
        // 判断是否有文件在上传下载
        MainWindowController main = MainWindowController.getInstance();
        if (main.downloadProgress.isVisible() || main.uploadProgress.isVisible()) {
            Optional<ButtonType> result = Dialogs.showConfirmation(QiniuValueConsts.UPLOADING_OR_DOWNLOADING);
            if (result.isPresent() && result.get() != ButtonType.OK) {
                // 取消退出事件
                event.consume();
                return;
            }
        }
        if (!isExternalCall) {
            // 退出程序
            ThreadPool.executor.shutdown();
            System.exit(0);
        }
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
            Dialogs.showFatalError(QiniuValueConsts.INIT_APP_ERROR_HEADER, e);
        }
        // 设置图标
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/qiniu.png")));
        stage.setTitle(QiniuValueConsts.MAIN_TITLE);
        // 设置关闭窗口事件
        stage.setOnCloseRequest((WindowEvent event) -> setOnClosed(event, false));
    }
}
