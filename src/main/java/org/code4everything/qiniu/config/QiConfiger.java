package org.code4everything.qiniu.config;

import com.qiniu.cdn.CdnManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.modules.constant.Values;
import org.code4everything.qiniu.view.Dialogs;
import javafx.application.Platform;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author pantao
 */

public class QiConfiger {

    private Logger logger = Logger.getLogger(QiConfiger.class);

    /**
     * 创建上传需要的Auth
     */
    void createAuth(String ak, String sk) {
        QiniuApplication.auth = Auth.create(ak, sk);
        QiniuApplication.cdnManager = new CdnManager(QiniuApplication.auth);
    }

    /**
     * 配置文件上传环境
     */
    public boolean configUploadEnv(String zone, String bucket) {
        if (!checkNet()) {
            Platform.runLater(() -> {
                Dialogs.showError(Values.NET_ERROR);
                System.exit(0);
            });
            return false;
        }
        logger.info("config file upload environment");
        // 构造一个带指定Zone对象的配置类
        Configuration configuration = new Configuration(QiniuApplication.zone.get(zone));
        // 生成上传凭证，然后准备上传
        String localTempDir = Paths.get(QiniuApplication.workDir, bucket).toString();
        try {
            FileRecorder fileRecorder = new FileRecorder(localTempDir);
            QiniuApplication.uploadManager = new UploadManager(configuration, fileRecorder);
        } catch (IOException e1) {
            logger.error("load local temp directory failed, can't use file recorder");
            QiniuApplication.uploadManager = new UploadManager(configuration);
        }
        QiniuApplication.configuration = configuration;
        QiniuApplication.bucketManager = new BucketManager(QiniuApplication.auth, configuration);
        return true;
    }

    /**
     * 检查是否连接网络
     */
    public boolean checkNet() {
        try {
            URL url = new URL("https://www.qiniu.com/");
            InputStream in = url.openStream();
            in.close();
            return true;
        } catch (IOException e) {
            logger.error("there is no connection to the network");
            return false;
        }
    }
}
