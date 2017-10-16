/**
 * 
 */
package com.zhazhapan.qiniu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.util.Checker;
import com.zhazhapan.qiniu.util.Formatter;
import com.zhazhapan.qiniu.view.Dialogs;

import javafx.application.Platform;

/**
 * @author pantao
 *
 */
public class Downloader {

	private Logger logger = Logger.getLogger(Downloader.class);

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
			QiniuApplication.downloadPath = Dialogs.showInputDialog(null, Values.CONFIG_DOWNLOAD_PATH,
					System.getProperty("user.home"));
			if (!checkDownloadPath()) {
				return;
			}
			ConfigLoader.writeConfig();
		}
		ThreadPool.executor.submit(() -> {
			checkDownloadPath();
			logger.info("start to download url: " + downloadURL);
			int byteread = 0;
			File file = new File(QiniuApplication.downloadPath + "/" + Formatter.getFileName(downloadURL));
			String log = "download success from url '" + downloadURL + "' to local '" + file.getAbsolutePath() + "'";
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				URL url = new URL(downloadURL);
				URLConnection conn = url.openConnection();
				InputStream inStream = conn.getInputStream();
				FileOutputStream fs = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
				logger.info(log);
			} catch (IOException e) {
				log = log.replace("success", "error") + ", message: " + e.getMessage();
				logger.error(log);
				Platform.runLater(() -> Dialogs.showException(Values.DOWNLOAD_FILE_ERROR, e));
			}
		});
	}

	public boolean checkDownloadPath() {
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
