package org.code4everything.qiniu.constant;

import com.zhazhapan.util.Utils;

import java.io.File;

/**
 * 常量类
 *
 * @author pantao
 */
public class QiniuValueConsts {

    public static final String CONFIG_PATH = Utils.getCurrentWorkDir() + File.separator + "config.json";

    public static final String QINIU_VIEW_URL = "/view/Main.fxml";

    public static final String QINIU_KEY_URL = "https://portal.qiniu.com/user/key";

    public static final String MAIN_TITLE = "七牛云管理工具";

    public static final String INIT_APP_ERROR_HEADER = "初始化错误，无法继续运行";

    public static final String OK = "确定";

    public static final String CANCEL = "取消";

    public static final String BUCKET_NAME = "空间名称";

    public static final String BUCKET_ZONE_NAME = "存储区域";

    public static final String BUCKET_URL = "空间域名";

    public static final String[] BUCKET_NAME_ARRAY = {"华东", "华北", "华南", "北美"};

    public static final String FILE_CHOOSER_TITLE = "选择需要上传的文件";

    public static final String OPEN_FILE_ERROR = "打开文件失败";

    public static final String UPLOAD_ERROR = "上传文件失败";

    public static final String UPLOADING = "文件上传中，请耐心等待。。。。。。\r\n";

    public static final String NEED_CHOOSE_BUCKET_OR_FILE = "请先选择一个存储空间或文件";

    public static final String CONFIG_UPLOAD_ENVIRONMENT = "正在配置文件上传环境，请耐心等待。。。。。。\r\n";

    public static final String RELOAD_CONFIG = "是否重新载入配置文件？";

    public static final String DOMAIN_CONFIG_ERROR = "您还没有正确地配置空间域名";

    public static final String REFRESH_SUCCESS = "刷新资源列表成功";

    public static final String NET_ERROR = "没有连接到网络，无法运行程序";

    public static final String DELETE_ERROR = "删除文件时发生异常";

    public static final String CHANGE_FILE_TYPE_ERROR = "删除文件发生异常";

    public static final String MOVE_OR_RENAME_ERROR = "移动或重命名文件失败";

    public static final String FILE_NAME = "文件名";

    public static final String COPY_AS = "保存文件副本";

    public static final String FILE_LIFE = "文件生存时间（天）";

    public static final String UPDATE_ERROR = "更新镜像源失败";

    public static final String DEFAULT_FILE_LIFE = "365";

    public static final String CONFIG_DOWNLOAD_PATH = "配置文件下载路径";

    public static final String DOWNLOAD_URL = "下载链接";

    public static final String INPUT_LOG_DATE = "请输入日志的日期";

    public static final String BUCKET_FLUX_ERROR = "获取空间流量统计失败";

    public static final String BUCKET_BAND_ERROR = "获取空间带宽统计失败";

    public static final String BUCKET_FLUX_COUNT = "空间流量（KB）";

    public static final String BUCKET_BANDWIDTH_COUNT = "空间带宽（KB）";

    public static final long DATE_SPAN_OF_THIRTY_ONE = 31 * 24 * 60 * 60 * 1000L;

    public static final String UPLOADING_OR_DOWNLOADING = "有文件正在上传或下载中，是否确认退出？";

    private QiniuValueConsts() {}
}
