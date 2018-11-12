package org.code4everything.qiniu.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhazhapan.util.Utils;
import javafx.application.Platform;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.config.ConfigBean;
import org.code4everything.qiniu.config.QiConfiger;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.view.Dialogs;

import java.io.File;

/**
 * 配置文件工具类
 *
 * @author pantao
 * @since 2018/11/12
 **/
public class ConfigUtils {

    private static final String CONFIG_PATH = Utils.getCurrentWorkDir() + File.separator + "config.json";

    /**
     * 加载配置文件
     */
    public static void loadConfig() {
        if (FileUtil.exist(CONFIG_PATH)) {
            ConfigBean config = JSONObject.parseObject(FileUtil.readUtf8String(CONFIG_PATH), ConfigBean.class);
            if (ObjectUtil.isNotNull(config)) {
                QiniuApplication.setConfigBean(config);
                if (StrUtil.isNotEmpty(config.getAccesskey()) && StrUtil.isNotEmpty(config.getSecretkey())) {
                    // 创建上传权限
                    new QiConfiger().createAuth(config.getAccesskey(), config.getSecretkey());
                    MainWindowController controller = MainWindowController.getInstance();
                    if (CollectionUtil.isNotEmpty(config.getBuckets())) {
                        Platform.runLater(() -> {
                            // 添加桶
                            config.getBuckets().forEach(bucket -> controller.addItem(bucket.getBucket()));
                            // 选中第一个桶
                            ConfigBean.BucketBean bucket = config.getBuckets().get(0);
                            controller.bucketChoiceCombo.setValue(bucket.getBucket());
                            controller.zoneText.setText(bucket.getZone());
                        });
                    }
                    if (CollectionUtil.isNotEmpty(config.getPrefixes())) {
                        // 添加前缀
                        Platform.runLater(() -> config.getPrefixes().forEach(prefix -> controller.filePrefixCombo.getItems().add(prefix)));
                    }
                }
            }
        }
        // 配置文件不存在或密钥不存在时弹出密钥数据框
        new Dialogs().showInputKeyDialog();
    }

    public static void writeConfig() {
        FileUtil.writeUtf8String(JSONObject.toJSONString(QiniuApplication.getConfigBean(), true), CONFIG_PATH);
    }
}
