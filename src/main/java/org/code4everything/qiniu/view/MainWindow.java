package org.code4everything.qiniu.view;

import com.zhazhapan.modules.constant.ValueConsts;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.QiniuUtils;
import org.code4everything.qiniu.config.ConfigLoader;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.ThreadPool;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Optional;

/**
 * @author pantao
 */
public class MainWindow {

    private Stage stage;

    private Logger logger = Logger.getLogger(MainWindow.class);

    public MainWindow() {

    }

    public static void setOnClosed(Event event, boolean isExternalCall) {
        // 判断是否有文件在上传下载
        MainWindowController main = MainWindowController.getInstance();
        if (main.downloadProgress.isVisible() || main.uploadProgress.isVisible()) {
            Optional<ButtonType> result = Dialogs.showConfirmation(QiniuValueConsts.UPLOADING_OR_DOWNLOADING);
            if (result.get() != ButtonType.OK) {
                // 取消退出事件
                event.consume();
                return;
            }
        }
        ConfigLoader.checkWorkPath();
        // 将上传日志写入磁盘
        String content = MainWindowController.getInstance().uploadStatusTextArea.getText();
        if (Checker.isNotEmpty(content)) {
            String logPath = QiniuApplication.workDir + QiniuValueConsts.SEPARATOR + "upload_" + Formatter.dateToString(new
                    Date()).replaceAll("-", "_") + ".log";
            QiniuUtils.saveLogFile(logPath, content);
        }
        // 将删除记录写入磁盘
        String deleteContent = QiniuApplication.deleteLog.toString();
        if (Checker.isNotEmpty(deleteContent)) {
            String logPath = QiniuApplication.workDir + QiniuValueConsts.SEPARATOR + "delete_" + Formatter.dateToString(new
                    Date()).replaceAll("-", "_") + ".log";
            QiniuUtils.saveLogFile(logPath, deleteContent);
        }
        if (!isExternalCall) {
            ThreadPool.executor.shutdown();
            System.exit(0);
        }
    }

    public void init(Stage stage) {
        this.stage = stage;
        init();
    }

    public void init() {
        logger.info("start to init main stage");
        try {
            VBox root = FXMLLoader.load(getClass().getResource("/view/MainWindow.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            logger.error("init stage error: " + e.getMessage());
            Dialogs.showFatalError(QiniuValueConsts.INIT_APP_ERROR_HEADER, e);
        }
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/qiniu.png")));
        stage.setTitle(QiniuValueConsts.MAIN_TITLE);
        stage.setOnCloseRequest((WindowEvent event) -> setOnClosed(event, ValueConsts.FALSE));
        QiniuApplication.stage = stage;
    }

    public void hide() {
        logger.info("hide main stage");
        stage.hide();
    }

    public void show() {
        logger.info("show main stage");
        stage.show();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
