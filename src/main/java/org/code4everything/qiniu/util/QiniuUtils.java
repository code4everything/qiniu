package org.code4everything.qiniu.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpUtil;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.ThreadPool;
import com.zhazhapan.util.Utils;
import com.zhazhapan.util.dialog.Alerts;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.api.SdkConfigurer;
import org.code4everything.qiniu.constant.QiniuValueConsts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * @author pantao
 * @since 2018/4/14
 */
public class QiniuUtils {

    private static final Logger LOGGER = Logger.getLogger(SdkConfigurer.class);

    private QiniuUtils() {}

    /**
     * 下载文件
     *
     * @param url 文件链接
     */
    public static void download(String url) {
        // 验证存储路径
        if (Validator.isEmpty(QiniuApplication.getConfigBean().getStoragePath())) {
            // 显示存储路径输入框
            String storagePath = DialogUtils.showInputDialog(null, QiniuValueConsts.CONFIG_DOWNLOAD_PATH,
                    Utils.getCurrentWorkDir());
            if (Validator.isEmpty(storagePath)) {
                return;
            }
            QiniuApplication.getConfigBean().setStoragePath(storagePath);
            ConfigUtils.writeConfig();
        }
        final String dest = QiniuApplication.getConfigBean().getStoragePath();
        // 下载文件
        ThreadPool.executor.execute(() -> HttpUtil.downloadFile(url, dest));
    }

    /**
     * 检查是否连接网络
     */
    public static boolean checkNet() {
        try {
            URL url = new URL("https://www.qiniu.com/");
            InputStream in = url.openStream();
            in.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("there is no connection to the network");
            return false;
        }
    }

    public static void saveLogFile(String file, String content) {
        try {
            FileExecutor.saveLogFile(file, content);
        } catch (IOException e) {
            Alerts.showError(QiniuValueConsts.MAIN_TITLE, e.getMessage());
        }
    }

    public static void saveFile(File file, String content) {
        try {
            FileExecutor.saveFile(file, content);
        } catch (IOException e) {
            Alerts.showError(QiniuValueConsts.MAIN_TITLE, e.getMessage());
        }
    }

    public static void openLink(String url) {
        try {
            Utils.openLink(url);
        } catch (Exception e) {
            Alerts.showError(QiniuValueConsts.MAIN_TITLE, e.getMessage());
        }
    }

    public static String getFileName(String string) {
        try {
            return Formatter.getFileName(string);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
