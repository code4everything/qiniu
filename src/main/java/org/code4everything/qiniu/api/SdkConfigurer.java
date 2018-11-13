package org.code4everything.qiniu.api;

import com.qiniu.cdn.CdnManager;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.zhazhapan.util.Utils;
import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.util.DialogUtils;
import org.code4everything.qiniu.util.QiniuUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pantao
 */
public class SdkConfigurer {

    private static final Map<String, Zone> ZONE = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger(SdkConfigurer.class);

    private static Auth auth = null;

    private static UploadManager uploadManager = null;

    private static BucketManager bucketManager = null;

    private static CdnManager cdnManager = null;

    static {
        // 加载空间区域
        ZONE.put(QiniuValueConsts.BUCKET_NAME_ARRAY[0], Zone.zone0());
        ZONE.put(QiniuValueConsts.BUCKET_NAME_ARRAY[1], Zone.zone1());
        ZONE.put(QiniuValueConsts.BUCKET_NAME_ARRAY[2], Zone.zone2());
        ZONE.put(QiniuValueConsts.BUCKET_NAME_ARRAY[3], Zone.zoneNa0());
    }

    private SdkConfigurer() {}

    public static CdnManager getCdnManager() {
        return cdnManager;
    }

    public static BucketManager getBucketManager() {
        return bucketManager;
    }

    public static UploadManager getUploadManager() {
        return uploadManager;
    }

    public static Auth getAuth() {
        return auth;
    }

    /**
     * 创建上传需要的Auth
     */
    public static void createAuth(String accessKey, String secretKey) {
        auth = Auth.create(accessKey, secretKey);
        cdnManager = new CdnManager(auth);
    }

    /**
     * 配置文件上传环境
     */
    public static boolean configUploadEnv(String zone, String bucket) {
        if (!QiniuUtils.checkNet()) {
            Platform.runLater(() -> {
                DialogUtils.showError(QiniuValueConsts.NET_ERROR);
                System.exit(0);
            });
            return false;
        }
        LOGGER.info("config file upload environment");
        // 构造一个带指定Zone对象的配置类
        Configuration configuration = new Configuration(SdkConfigurer.ZONE.get(zone));
        // 生成上传凭证，然后准备上传
        String workDir = Paths.get(Utils.getCurrentWorkDir(), bucket).toString();
        try {
            FileRecorder fileRecorder = new FileRecorder(workDir);
            uploadManager = new UploadManager(configuration, fileRecorder);
        } catch (IOException e) {
            LOGGER.error("load work directory failed, can't use file recorder");
            uploadManager = new UploadManager(configuration);
        }
        bucketManager = new BucketManager(auth, configuration);
        return true;
    }
}
