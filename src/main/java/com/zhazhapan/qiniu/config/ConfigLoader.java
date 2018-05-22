package com.zhazhapan.qiniu.config;

import com.google.gson.*;
import com.zhazhapan.qiniu.QiniuApplication;
import com.zhazhapan.qiniu.controller.MainWindowController;
import com.zhazhapan.qiniu.model.Key;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.Dialogs;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.ThreadPool;
import javafx.application.Platform;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author pantao
 */
public class ConfigLoader {

    public static String configPath = null;
    private static Logger logger = Logger.getLogger(ConfigLoader.class);

    /**
     * 加载配置文件
     *
     * @param isExternalCall 是否为外部调用
     */
    public static void loadConfig(boolean isExternalCall) {
        logger.info("start to load configuration");
        File config = new File(configPath);
        if (config.exists()) {
            // 读取配置文件内容
            JsonObject json = readConfig();
            if (Checker.isNull(json)) {
                showInputKeyDialog();
            } else {
                // 读取Key
                try {
                    String ak = json.get("accesskey").getAsString();
                    String sk = json.get("secretkey").getAsString();
                    QiniuApplication.key = new Key(ak, sk);
                    new QiConfiger().createAuth(ak, sk);
                } catch (Exception e) {
                    logger.error("read key from configuration failed, message: " + e.getMessage());
                    Dialogs.showException(Values.LOAD_CONFIG_ERROR, e);
                    showInputKeyDialog();
                }
                // 读取Bucket
                JsonElement buckets = json.get("buckets");
                if (Checker.isNotNull(buckets)) {
                    logger.info("load buckets into memory");
                    JsonArray array = buckets.getAsJsonArray();
                    boolean setvalue = true;
                    for (JsonElement element : array) {
                        JsonObject obj = (JsonObject) element;
                        String bucket = obj.get("bucket").getAsString();
                        JsonElement url = obj.get("url");
                        String zone = obj.get("zone").getAsString();
                        String zones = zone + " " + (Checker.isNull(url) ? "" : url.getAsString());
                        QiniuApplication.buckets.put(bucket, zones);
                        Platform.runLater(() -> MainWindowController.getInstance().addItem(bucket));
                        if (setvalue) {
                            Platform.runLater(() -> {
                                MainWindowController.getInstance().bucketChoiceCombo.setValue(bucket);
                                MainWindowController.getInstance().zoneText.setText(zone);
                            });
                            setvalue = false;
                        }
                    }
                } else {
                    MainWindowController.getInstance().showBucketAddableDialog();
                }
                // 读取文件前缀
                JsonElement prefix = json.get("prefix");
                if (Checker.isNotNull(prefix)) {
                    JsonArray array = prefix.getAsJsonArray();
                    Platform.runLater(() -> {
                        for (JsonElement element : array) {
                            String string = element.getAsString();
                            QiniuApplication.prefix.add(string);
                            MainWindowController.getInstance().filePrefixCombo.getItems().add(string);
                        }
                    });
                }
                // 读取下载的目录
                JsonElement download = json.get("download");
                if (Checker.isNotNull(download)) {
                    QiniuApplication.downloadPath = download.getAsString();
                }
            }
        } else {
            logger.info("there is no configuration file, starting to create");
            checkWorkPath();
            // 创建配置文件
            try {
                config.createNewFile();
                logger.info("create file 'qiniu.conf' success");
            } catch (IOException e) {
                logger.error("create configuration file failed, messages: " + e.getMessage());
                Dialogs.showFatalError(Values.LOAD_CONFIG_ERROR, e);
            }
            showInputKeyDialog();
        }
        // 如果Key是Null则退出程序
        if (Checker.isNull(QiniuApplication.key) && !isExternalCall) {
            logger.info("stop running");
            System.exit(0);
        }
    }

    /**
     * 检测工作文件夹是否存在
     */
    public static void checkWorkPath() {
        File dir = new File(QiniuApplication.workDir);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("mkdir '" + QiniuApplication.workDir + "' success");
        }
    }

    /**
     * 初始状态时要求用户输入AccessKey和SecretKey，否则退出程序
     */
    private static void showInputKeyDialog() {
        logger.info("show dialog to require user input key");
        new Dialogs().showInputKeyDialog();
    }

    public static void writeKey(String accessKey, String secretKey) {
        new QiConfiger().createAuth(accessKey, secretKey);
        QiniuApplication.key = new Key(accessKey, secretKey);
        writeConfig();
    }

    private static JsonObject readConfig() {
        JsonObject jsonObject = null;
        try {
            logger.info("load configuration into memory");
            String content = FileExecutor.readFile(configPath);
            if (Checker.isEmpty(content)) {
                Dialogs.showException(Values.LOAD_CONFIG_ERROR, new IOException("load configuration file error"));
            } else {
                jsonObject = new JsonParser().parse(content).getAsJsonObject();
            }
        } catch (Exception e) {
            logger.error("convert json string to json object failed, app'll reset");
            Dialogs.showException(Values.JSON_TO_OBJECT_ERROR, e);
        }
        return jsonObject;
    }

    public static void writeConfig() {
        JsonObject config = new JsonObject();
        // Key
        config.addProperty("accesskey", QiniuApplication.key.getAccessKey());
        config.addProperty("secretkey", QiniuApplication.key.getSecretKey());
        JsonArray buckets = new JsonArray();
        // Bucket
        for (Map.Entry<String, String> entry : QiniuApplication.buckets.entrySet()) {
            JsonObject json = new JsonObject();
            json.addProperty("bucket", entry.getKey());
            String[] zones = entry.getValue().split(" ");
            json.addProperty("zone", zones[0]);
            json.addProperty("url", zones.length > 1 ? zones[1] : "");
            buckets.add(json);
        }
        config.add("buckets", buckets);
        // Prefix
        JsonArray prefix = new JsonArray();
        for (String string : QiniuApplication.prefix) {
            prefix.add(string);
        }
        config.add("prefix", prefix);
        // Download URL
        config.addProperty("download", QiniuApplication.downloadPath);
        writeConfig(new Gson().toJson(config));
    }

    private static void writeConfig(String configJson) {
        ThreadPool.executor.submit(() -> {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(configPath, false));
                out.write(Formatter.formatJson(configJson));
                out.close();
                logger.info("rewrite configuration success");
            } catch (IOException e) {
                logger.error("rewrite configuration file failed, messages: " + e.getMessage());
                Dialogs.showFatalError(Values.LOAD_CONFIG_ERROR, e);
            }
        });
    }
}
