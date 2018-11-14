package org.code4everything.qiniu.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.api.SdkConfigurer;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainController;
import org.code4everything.qiniu.model.BucketBean;
import org.code4everything.qiniu.model.ConfigBean;

import java.util.ArrayList;

/**
 * 配置文件工具类
 *
 * @author pantao
 * @since 2018/11/12
 **/
public class ConfigUtils {

    private static final String CONFIG_PATH = QiniuValueConsts.CONFIG_PATH;

    private ConfigUtils() {}

    /**
     * 加载配置文件
     */
    public static void loadConfig() {
        QiniuApplication.setConfigBean(new ConfigBean());
        if (FileUtil.exist(CONFIG_PATH)) {
            ConfigBean config = JSONObject.parseObject(FileUtil.readUtf8String(CONFIG_PATH), ConfigBean.class);
            if (ObjectUtil.isNotNull(config)) {
                QiniuApplication.setConfigBean(config);
                if (StrUtil.isNotEmpty(config.getAccessKey()) && StrUtil.isNotEmpty(config.getSecretKey())) {
                    // 创建上传权限
                    SdkConfigurer.createAuth(config.getAccessKey(), config.getSecretKey());
                }
                MainController controller = MainController.getInstance();
                if (CollectionUtil.isEmpty(config.getBuckets())) {
                    // 设置一个空的桶列表，防止出现空指针
                    config.setBuckets(new ArrayList<>());
                } else {
                    Platform.runLater(() -> {
                        // 添加桶
                        config.getBuckets().forEach(bucket -> controller.appendBucket(bucket.getBucket()));
                        // 选中第一个桶
                        BucketBean bucket = config.getBuckets().get(0);
                        controller.bucketCB.setValue(bucket.getBucket());
                        controller.zoneTF.setText(bucket.getZone());
                    });
                }
                if (CollectionUtil.isEmpty(config.getPrefixes())) {
                    // 设置一个空的前缀列表，防止出现空指针
                    config.setPrefixes(new ArrayList<>());
                } else {
                    // 添加前缀
                    Platform.runLater(() -> config.getPrefixes().forEach(prefix -> controller.prefixCB.getItems().add(prefix)));
                }
                return;
            }
        }
        // 设置一个空的配置对象，防止出现空指针
        ConfigBean configBean = new ConfigBean();
        configBean.setPrefixes(new ArrayList<>());
        configBean.setBuckets(new ArrayList<>());
        QiniuApplication.setConfigBean(configBean);
    }

    /**
     * 写配置文件
     */
    public static void writeConfig() {
        FileUtil.writeUtf8String(JSONObject.toJSONString(QiniuApplication.getConfigBean(), true), CONFIG_PATH);
    }
}
