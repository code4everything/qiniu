package org.code4everything.qiniu.api;

import com.qiniu.cdn.CdnResult;
import com.qiniu.cdn.CdnResult.LogData;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.BatchStatus;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.model.FileBean;
import org.code4everything.qiniu.util.DialogUtils;
import org.code4everything.qiniu.util.QiniuUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author pantao
 */
public class SdkManager {

    private static final Logger LOGGER = Logger.getLogger(SdkManager.class);

    /**
     * 获取空间带宽统计
     */
    public CdnResult.BandwidthResult getBucketBandwidth(String[] domains, String startDate, String endDate) throws QiniuException {
        return SdkConfigurer.getCdnManager().getBandwidthData(domains, startDate, endDate, "day");
    }

    /**
     * 获取空间的流量统计
     */
    public CdnResult.FluxResult getBucketFlux(String[] domains, String startDate, String endDate) throws QiniuException {
        return SdkConfigurer.getCdnManager().getFluxData(domains, startDate, endDate, "day");
    }

    /**
     * 日志下载，cdn相关
     */
    public void downloadCdnLog(String logDate) {
        if (Checker.isNotEmpty(QiniuApplication.getConfigBean().getBuckets()) && Checker.isDate(logDate)) {
            String[] domains = new String[QiniuApplication.getConfigBean().getBuckets().size()];
            for (int i = 0; i < QiniuApplication.getConfigBean().getBuckets().size(); i++) {
                domains[i] = QiniuApplication.getConfigBean().getBuckets().get(i).getUrl();
            }
            try {
                CdnResult.LogListResult logRes = SdkConfigurer.getCdnManager().getCdnLogList(domains, logDate);
                for (Map.Entry<String, LogData[]> logs : logRes.data.entrySet()) {
                    for (LogData log : logs.getValue()) {
                        QiniuUtils.download(log.url);
                    }
                }
            } catch (QiniuException e) {
                LOGGER.error("get cdn log url error, message: " + e.getMessage());
                DialogUtils.showException(e);
            }
        }
    }

    /**
     * 刷新文件，cdn相关
     */
    public void refreshFile(ObservableList<FileBean> fileInfos, String domain) {
        if (Checker.isNotEmpty(fileInfos)) {
            String[] files = new String[fileInfos.size()];
            int i = 0;
            for (FileBean fileInfo : fileInfos) {
                files[i++] = getPublicURL(fileInfo.getName(), domain);
            }
            refreshFile(files);
        }
    }

    /**
     * 刷新文件，cdn相关
     */
    private void refreshFile(String[] files) {
        try {
            // 单次方法调用刷新的链接不可以超过100个
            SdkConfigurer.getCdnManager().refreshUrls(files);
            LOGGER.info("refresh files success");
        } catch (QiniuException e) {
            LOGGER.error("refresh files error, message: " + e.getMessage());
            DialogUtils.showException(e);
        }
    }

    /**
     * 私有下载
     */
    public void privateDownload(String fileName, String domain) {
        // 自定义链接过期时间（小时）
        long expireInSeconds = 24;
        String publicURL = getPublicURL(fileName, domain);
        QiniuUtils.download(SdkConfigurer.getAuth().privateDownloadUrl(publicURL, expireInSeconds));
    }

    /**
     * 公有下载
     */
    public void publicDownload(String fileName, String domain) {
        String url = getPublicURL(fileName, domain);
        if (Checker.isNotEmpty(url)) {
            QiniuUtils.download(url);
        }
    }

    public String getPublicURL(String fileName, String domain) {
        fileName = fileName.replaceAll(" ", "qn_code_per_20").replaceAll("/", "qn_code_per_2F");
        try {
            fileName = URLEncoder.encode(fileName, "utf-8").replaceAll("qn_code_per_2F", "/");
            return String.format("%s/%s", domain, fileName.replaceAll("qn_code_per_20", "%20"));
        } catch (UnsupportedEncodingException e) {
            urlError(e);
            return null;
        }
    }

    /**
     * 更新镜像源
     */
    public void updateFile(String bucket, String key) {
        String log = "update file '" + key + "' on bucket '" + bucket;
        try {
            SdkConfigurer.getBucketManager().prefetch(bucket, key);
            LOGGER.info(log + "' success");
        } catch (QiniuException e) {
            LOGGER.error(log + "' error, message: " + e.getMessage());
            DialogUtils.showException(QiniuValueConsts.UPDATE_ERROR, e);
        }
    }

    /**
     * 设置文件生存时间
     */
    public void setFileLife(String bucket, String key, int days) {
        String log = "set file of '" + key + "' life to " + days + " day(s) ";
        try {
            SdkConfigurer.getBucketManager().deleteAfterDays(bucket, key, days);
            LOGGER.info(log + "success");
        } catch (QiniuException e) {
            LOGGER.error(log + "error, message: " + e.getMessage());
            DialogUtils.showException(QiniuValueConsts.MOVE_OR_RENAME_ERROR, e);
        }
    }

    /**
     * 重命令文件
     */
    public Boolean renameFile(String bucket, String oldName, String newName) {
        return moveFile(bucket, oldName, bucket, newName);
    }

    /**
     * 移动文件
     */
    private boolean moveFile(String fromBucket, String fromKey, String toBucket, String toKey) {
        return moveOrCopyFile(fromBucket, fromKey, toBucket, toKey, FileAction.MOVE);
    }

    /**
     * 移动或复制文件
     */
    public boolean moveOrCopyFile(String fromBucket, String fromKey, String toBucket, String toKey, FileAction action) {
        if (QiniuUtils.checkNet()) {
            String log = "move file '" + fromKey + "' from bucket '" + fromBucket + "' to bucket '" + toBucket + "', "
                    + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "and rename file '" + toKey + "'";
            try {
                if (action == FileAction.COPY) {
                    log = log.replaceAll("^move", "copy");
                    SdkConfigurer.getBucketManager().copy(fromBucket, fromKey, toBucket, toKey, true);
                } else {
                    SdkConfigurer.getBucketManager().move(fromBucket, fromKey, toBucket, toKey, true);
                }
                LOGGER.info(log + " success");
                return true;
            } catch (QiniuException e) {
                LOGGER.error(log + " failed, message: " + e.getMessage());
                DialogUtils.showException(QiniuValueConsts.MOVE_OR_RENAME_ERROR, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 修改文件类型
     */
    public boolean changeType(String fileName, String newType, String bucket) {
        if (QiniuUtils.checkNet()) {
            String log = "change file '" + fileName + "' type '" + newType + "' on bucket '" + bucket;
            try {
                SdkConfigurer.getBucketManager().changeMime(bucket, fileName, newType);
                LOGGER.info(log + "' success");
                return true;
            } catch (QiniuException e) {
                LOGGER.error(log + "' failed, message: " + e.getMessage());
                DialogUtils.showException(QiniuValueConsts.CHANGE_FILE_TYPE_ERROR, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 批量删除文件，单次批量请求的文件数量不得超过1000
     */
    public void deleteFiles(ObservableList<FileBean> fileInfos, String bucket) {
        if (Checker.isNotEmpty(fileInfos) && QiniuUtils.checkNet()) {
            // 生成待删除的文件列表
            String[] files = new String[fileInfos.size()];
            ArrayList<FileBean> seletecFileInfos = new ArrayList<>();
            int i = 0;
            for (FileBean fileInfo : fileInfos) {
                files[i++] = fileInfo.getName();
                seletecFileInfos.add(fileInfo);
            }
            try {
                BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
                batchOperations.addDeleteOp(bucket, files);
                Response response = SdkConfigurer.getBucketManager().batch(batchOperations);
                BatchStatus[] batchStatusList = response.jsonToObject(BatchStatus[].class);
                MainWindowController main = MainWindowController.getInstance();
                // 文件列表是否为搜索后结果
                boolean sear = Checker.isNotEmpty(main.searchTextField.getText());
                ObservableList<FileBean> currentRes = main.resTable.getItems();
                for (i = 0; i < files.length; i++) {
                    BatchStatus status = batchStatusList[i];
                    String file = files[i];
                    if (status.code == 200) {
                        LOGGER.info("delete file '" + file + "' success");
                        QiniuApplication.data.remove(seletecFileInfos.get(i));
                        QiniuApplication.totalLength--;
                        QiniuApplication.totalSize -= Formatter.sizeToLong(seletecFileInfos.get(i).getSize());
                        if (sear) {
                            currentRes.remove(seletecFileInfos.get(i));
                        }
                    } else {
                        LOGGER.error("delete file '" + file + "' failed, message: " + status.data.error);
                        DialogUtils.showError("删除文件：" + file + " 失败");
                    }
                }
            } catch (QiniuException e) {
                DialogUtils.showException(QiniuValueConsts.DELETE_ERROR, e);
            }
            MainWindowController.getInstance().setBucketCount();
        }
    }

    /**
     * 获取空间文件列表，并映射到FileInfo
     */
    public void listFileOfBucket() {
        MainWindowController main = MainWindowController.getInstance();
        // 列举空间文件列表
        String bucket = main.bucketChoiceCombo.getValue();
        BucketManager.FileListIterator iterator = SdkConfigurer.getBucketManager().createFileListIterator(bucket, "",
                QiniuValueConsts.BUCKET_LIST_LIMIT_SIZE, "");
        ArrayList<FileBean> files = new ArrayList<>();
        LOGGER.info("get file list of bucket: " + bucket);
        QiniuApplication.totalLength = 0;
        QiniuApplication.totalSize = 0;
        // 处理获取的file list结果
        while (iterator.hasNext()) {
            com.qiniu.storage.model.FileInfo[] items = iterator.next();
            for (com.qiniu.storage.model.FileInfo item : items) {
                QiniuApplication.totalLength++;
                QiniuApplication.totalSize += item.fsize;
                // 将七牛的时间单位（100纳秒）转换成毫秒，然后转换成时间
                String time = Formatter.timeStampToString(item.putTime / 10000);
                String size = Formatter.formatSize(item.fsize);
                FileBean file = new FileBean(item.key, item.mimeType, size, time);
                files.add(file);
                LOGGER.info("file name: " + item.key + ", file type: " + item.mimeType + ", file size: " + size + ", "
                        + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "file time: " + time);
            }
        }
        QiniuApplication.data = FXCollections.observableArrayList(files);
    }

    private void urlError(Exception e) {
        LOGGER.error("generate url error: " + e.getMessage());
        DialogUtils.showException(QiniuValueConsts.GENERATE_URL_ERROR, e);
    }

    public enum FileAction {
        // 复制或移动文件
        COPY, MOVE
    }
}
