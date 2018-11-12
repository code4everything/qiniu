/**
 *
 */
package org.code4everything.qiniu.downloader;

import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.ThreadPool;
import com.zhazhapan.util.Utils;
import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.util.ConfigUtils;
import org.code4everything.qiniu.util.QiniuUtils;
import org.code4everything.qiniu.view.Dialogs;

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
            String storagePath = Dialogs.showInputDialog(null, QiniuValueConsts.CONFIG_DOWNLOAD_PATH,
                    Utils.getCurrentWorkDir());
            QiniuApplication.getConfigBean().setStoragePath(storagePath);
            if (!checkDownloadPath()) {
                return;
            }
            ConfigUtils.writeConfig();
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
            File file =
                    new File(QiniuApplication.getConfigBean().getStoragePath() + File.separator + QiniuUtils.getFileName(downloadURL));
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
                Platform.runLater(() -> Dialogs.showException(QiniuValueConsts.DOWNLOAD_FILE_ERROR, e));
            }
            Platform.runLater(() -> main.downloadProgress.setVisible(false));
        });
    }

    private boolean checkDownloadPath() {
        if (Checker.isNotEmpty(QiniuApplication.getConfigBean().getStoragePath())) {
            File file = new File(QiniuApplication.getConfigBean().getStoragePath());
            if (!file.exists()) {
                file.mkdirs();
                logger.info("mkdir '" + QiniuApplication.getConfigBean().getStoragePath() + "' success");
            }
            return true;
        }
        return false;
    }
}
