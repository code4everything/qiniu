package com.zhazhapan.qiniu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.model.FileInfo;
import com.zhazhapan.qiniu.model.Key;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.MainWindow;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * @author pantao
 *
 */
@SpringBootApplication
public class QiniuApplication extends Application {

	public static MainWindow mainWindow = null;

	public static Key key = null;

	public static Map<String, Zone> zone = new HashMap<String, Zone>();

	/**
	 * Value包括存储空间和空间域名，用英文空格分隔
	 */
	public static Map<String, String> buckets = new HashMap<String, String>();

	private static Logger logger = Logger.getLogger(QiniuApplication.class);

	public static Stage stage = null;

	public static ArrayList<String> prefix = new ArrayList<String>();

	public static String workDir = null;

	public static Auth auth = null;

	public static UploadManager uploadManager = null;

	public static String upToken = null;

	public static Configuration configuration = null;

	public static BucketManager bucketManager = null;

	public static ObservableList<FileInfo> data = null;

	public static StringBuilder deleteLog = new StringBuilder();

	public static String downloadPath = null;

	/**
	 * 主程序入口
	 */
	public static void main(String[] args) {
		logger.info("start to run application");
		String osname = System.getProperties().getProperty("os.name").toLowerCase();
		logger.info("current operation system: " + osname);
		if (osname.contains(Values.WINDOW_OS)) {
			workDir = Values.APP_PATH_OF_WINDOWS;
		} else {
			workDir = Values.APP_PATH_OF_UNIX;
		}
		ConfigLoader.configPath = workDir + Values.CONFIG_PATH;
		mainWindow = new MainWindow();
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		ConfigLoader.loadConfig();
		mainWindow.init(stage);
		mainWindow.show();
		initZone();
	}

	public void initZone() {
		zone.put(Values.BUCKET_NAME_ARRAY[0], Zone.zone0());
		zone.put(Values.BUCKET_NAME_ARRAY[1], Zone.zone1());
		zone.put(Values.BUCKET_NAME_ARRAY[2], Zone.zone2());
		zone.put(Values.BUCKET_NAME_ARRAY[3], Zone.zoneNa0());
	}
}
