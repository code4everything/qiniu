package com.zhazhapan.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.model.FileInfo;
import com.zhazhapan.qiniu.model.Key;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.MainWindow;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.ThreadPool;
import com.zhazhapan.util.Utils;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author pantao
 */
public class QiniuApplication extends Application {

    public static Key key = null;

    public static Map<String, Zone> zone = new HashMap<>();

    /**
     * Value包括存储空间和空间域名，用英文空格分隔
     */
    public static Map<String, String> buckets = new HashMap<>();

    public static Stage stage = null;

    public static ArrayList<String> prefix = new ArrayList<>();

    public static String workDir = null;

    public static Auth auth = null;

    public static UploadManager uploadManager = null;

    public static Configuration configuration = null;

    public static BucketManager bucketManager = null;

    public static ObservableList<FileInfo> data = null;

    public static StringBuilder deleteLog = new StringBuilder();

    public static String downloadPath = null;

    public static CdnManager cdnManager = null;

    /**
     * 空间总文件数
     */
    public static int totalLength = 0;

    /**
     * 空间使用总大小
     */
    public static long totalSize = 0;

    private static MainWindow mainWindow = null;

    private static Logger logger = Logger.getLogger(QiniuApplication.class);

    /**
     * 主程序入口
     */
    public static void main(String[] args) {
        logger.info("start to run application");
        // 设置线程池大小
        ThreadPool.setMaximumPoolSize(10);
        // 设置排队大小
        ThreadPool.setWorkQueue(new LinkedBlockingQueue<>(1024));
        ThreadPool.init();
        initLoad(ValueConsts.FALSE);
        launch(args);
    }

    /**
     * 是否外部调用
     *
     * @param isExternalCall {@link Boolean}
     */
    public static void initLoad(boolean isExternalCall) {
        logger.info("current operation system: " + Utils.getCurrentOS());
        if (Checker.isWindows()) {
            workDir = Values.APP_PATH_OF_WINDOWS;
        } else {
            workDir = Values.APP_PATH_OF_UNIX;
        }
        ConfigLoader.configPath = workDir + Values.SEPARATOR + Values.CONFIG_PATH;
        logger.info("current work director: " + workDir + ", config file: " + ConfigLoader.configPath);
        mainWindow = new MainWindow();
        initZone();
        if (isExternalCall) {
            ConfigLoader.loadConfig(ValueConsts.TRUE);
        }
    }

    private static void initZone() {
        zone.put(Values.BUCKET_NAME_ARRAY[0], Zone.zone0());
        zone.put(Values.BUCKET_NAME_ARRAY[1], Zone.zone1());
        zone.put(Values.BUCKET_NAME_ARRAY[2], Zone.zone2());
        zone.put(Values.BUCKET_NAME_ARRAY[3], Zone.zoneNa0());
    }

    @Override
    public void start(Stage stage) {
        ConfigLoader.loadConfig(ValueConsts.FALSE);
        mainWindow.init(stage);
        mainWindow.show();
    }
}
