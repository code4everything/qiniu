/**
 * 
 */
package com.zhazhapan.qiniu.view;

import java.util.Date;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.FileExecutor;
import com.zhazhapan.qiniu.QiniuApplication;
import com.zhazhapan.qiniu.ThreadPool;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.controller.MainWindowController;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author pantao
 *
 */
public class MainWindow {

	private Stage stage;

	private Logger logger = Logger.getLogger(MainWindow.class);

	public MainWindow() {

	}

	public MainWindow(Stage stage) {
		this.stage = stage;
	}

	public void init(Stage stage) {
		this.stage = stage;
		init();
	}

	public void init() {
		logger.info("start to init main stage");
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
			Scene scene = new Scene(root);
			stage.setScene(scene);
		} catch (Exception e) {
			logger.error("init stage error: " + e.getMessage());
			Dialogs.showFatalError(Values.INIT_APP_ERROR_HEADER, e);
		}
		stage.getIcons().add(new Image(getClass().getResourceAsStream("qiniu.png")));
		stage.setTitle(Values.MAIN_TITLE);
		stage.setOnCloseRequest((WindowEvent event) -> {
			// 判断是否有文件在上传下载
			MainWindowController main = MainWindowController.getInstance();
			if (main.downloadProgress.isVisible() || main.uploadProgress.isVisible()) {
				Optional<ButtonType> result = Dialogs.showConfirmation(Values.UPLOADING_OR_DOWNLOADING);
				if (result.get() != ButtonType.OK) {
					// 取消退出事件
					event.consume();
					return;
				}
			}
			ThreadPool.shutdown();
			ConfigLoader.checkWorkPath();
			FileExecutor executor = new FileExecutor();
			// 将上传日志写入磁盘
			String content = MainWindowController.getInstance().uploadStatusTextArea.getText();
			if (Checker.isNotEmpty(content)) {
				String logPath = QiniuApplication.workDir + Values.SEPARATOR + "upload_"
						+ Formatter.dateToString(new Date()).replaceAll("-", "_") + ".log";
				executor.saveLogFile(logPath, content);
			}
			// 将删除记录写入磁盘
			String deleteContent = QiniuApplication.deleteLog.toString();
			if (Checker.isNotEmpty(deleteContent)) {
				String logPath = QiniuApplication.workDir + Values.SEPARATOR + "delete_"
						+ Formatter.dateToString(new Date()).replaceAll("-", "_") + ".log";
				executor.saveLogFile(logPath, deleteContent);
			}
			System.exit(0);
		});
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
