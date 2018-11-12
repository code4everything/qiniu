package org.code4everything.qiniu;

import org.code4everything.qiniu.constant.QiniuValueConsts;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.Utils;
import com.zhazhapan.util.dialog.Alerts;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author pantao
 * @since 2018/4/14
 */
public class QiniuUtils {

    private QiniuUtils() {}

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
