package org.code4everything.qiniu.util;

import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.Utils;
import com.zhazhapan.util.dialog.Alerts;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.api.config.SdkConfigurer;
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
