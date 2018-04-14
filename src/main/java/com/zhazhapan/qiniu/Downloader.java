/**
 *
 */
package com.zhazhapan.qiniu;

import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.controller.MainWindowController;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.Dialogs;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.ThreadPool;
import javafx.application.Platform;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author pantao
 */
public class Downloader {

    private Logger logger = Logger.getLogger(Downloader.class);

    private double progress = 0;

    /**
     * 下载文件
     */
    public void downloadFromNet(String downloadURL) {
        if (Checker.isHyperLink(downloadURL)) {
            logger.info(downloadURL + " is a validated url");
        } else {
            logger.info(downloadURL + " is an invalidated url, can't download");
            return;
        }
        if (!checkDownloadPath()) {
            QiniuApplication.downloadPath = Dialogs.showInputDialog(null, Values.CONFIG_DOWNLOAD_PATH, Values
                    .USER_HOME);
            if (!checkDownloadPath()) {
                return;
            }
            ConfigLoader.writeConfig();
        }
        MainWindowController main = MainWindowController.getInstance();
        Platform.runLater(() -> {
            main.downloadProgress.setVisible(true);
            main.downloadProgress.setProgress(0);
        });
        ThreadPool.executor.submit(() -> {
            checkDownloadPath();
            logger.info("start to download url: " + downloadURL);
            int byteRead;
            File file = new File(QiniuApplication.downloadPath + "/" + QiniuUtils.getFileName(downloadURL));
            String log = "download success from url '" + downloadURL + "' to local '" + file.getAbsolutePath() + "'";
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                URL url = new URL(downloadURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(1000 * 6);
                conn.setRequestProperty("charset", "UTF-8");
                conn.setRequestProperty("user-agent", ValueConsts.USER_AGENT[0]);
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                InputStream inStream = conn.getInputStream();
                double size = conn.getContentLength();
                progress = 0;
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                    Platform.runLater(() -> {
                        progress += 1024;
                        main.downloadProgress.setProgress(progress / size);
                    });
                }
                inStream.close();
                fs.close();
                logger.info(log);
            } catch (IOException e) {
                log = log.replace("success", "error") + ", message: " + e.getMessage();
                logger.error(log);
                Platform.runLater(() -> Dialogs.showException(Values.DOWNLOAD_FILE_ERROR, e));
            }
            Platform.runLater(() -> main.downloadProgress.setVisible(false));
        });
    }

    private boolean checkDownloadPath() {
        if (Checker.isNotEmpty(QiniuApplication.downloadPath)) {
            File file = new File(QiniuApplication.downloadPath);
            if (!file.exists()) {
                file.mkdirs();
                logger.info("mkdir '" + QiniuApplication.downloadPath + "' success");
            }
            return true;
        }
        return false;
    }
}
