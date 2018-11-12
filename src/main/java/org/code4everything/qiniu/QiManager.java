package org.code4everything.qiniu;

import com.qiniu.cdn.CdnResult;
import com.qiniu.cdn.CdnResult.BandwidthData;
import com.qiniu.cdn.CdnResult.FluxData;
import com.qiniu.cdn.CdnResult.LogData;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.BatchStatus;
import org.code4everything.qiniu.config.QiConfiger;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.model.FileInfo;
import org.code4everything.qiniu.modules.constant.Values;
import org.code4everything.qiniu.view.Dialogs;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author pantao
 */
public class QiManager {

    private Logger logger = Logger.getLogger(QiManager.class);

    /**
     * 获取空间带宽统计，使用默认的KB单位
     *
     * @param domains domains
     * @param fromDate fromDate
     * @param toDate toDate
     *
     * @return {@link Series}
     */
    public Series<String, Long> getBucketBandwidth(String[] domains, String fromDate, String toDate) {
        return getBucketBandwidth(domains, fromDate, toDate, "KB");
    }

    /**
     * 获取空间带宽统计，使用自定义单位
     *
     * @param domains domains
     * @param fromDate from Date
     * @param toDate toDate
     * @param countUnit countUnit
     *
     * @return {@link Series}
     */
    public Series<String, Long> getBucketBandwidth(String[] domains, String fromDate, String toDate, String countUnit) {
        CdnResult.BandwidthResult bandwidthResult = null;
        try {
            bandwidthResult = QiniuApplication.cdnManager.getBandwidthData(domains, fromDate, toDate, "day");
        } catch (QiniuException e) {
            logger.error("get bucket bandwidth error, message: " + e.getMessage());
            Platform.runLater(() -> Dialogs.showException(Values.BUCKET_BAND_ERROR, e));
        }
        Series<String, Long> bandSer = new Series<>();
        bandSer.setName(Values.BUCKET_BANDWIDTH_COUNT.replaceAll("[A-Z]+", countUnit));
        // 获取带宽统计
        if (Checker.isNotNull(bandwidthResult) && Checker.isNotEmpty(bandwidthResult.data)) {
            for (Map.Entry<String, BandwidthData> bandwidth : bandwidthResult.data.entrySet()) {
                String[] times = bandwidthResult.time;
                BandwidthData bandwidthData = bandwidth.getValue();
                int i = 0;
                for (String time : times) {
                    long size = 0;
                    if (Checker.isNotNull(bandwidthData)) {
                        if (Checker.isNotNull(bandwidthData.china)) {
                            size += bandwidthData.china[i];
                        }
                        if (Checker.isNotNull(bandwidthData.oversea)) {
                            size += bandwidthData.oversea[i];
                        }
                    }
                    long unit = Formatter.sizeToLong("1 " + countUnit);
                    bandSer.getData().add(new Data<>(time.substring(5, 10), size / unit));
                    i++;
                }
            }
        } else {
            logger.info("bandwidth is empty of this domain");
        }
        return bandSer;
    }

    /**
     * 获取空间的流量统计，使用默认的KB单位
     *
     * @param domains domains
     * @param fromDate fromDate
     * @param toDate toDate
     *
     * @return {@link Series}
     */
    public Series<String, Long> getBucketFlux(String[] domains, String fromDate, String toDate) {
        return getBucketFlux(domains, fromDate, toDate, "KB");
    }

    /**
     * 获取空间的流量统计，自定义统计单位
     *
     * @param domains domains
     * @param fromDate fromDate
     * @param toDate toDate
     * @param countUnit countUnit
     *
     * @return {@link Series}
     */
    public Series<String, Long> getBucketFlux(String[] domains, String fromDate, String toDate, String countUnit) {
        CdnResult.FluxResult fluxResult = null;
        try {
            fluxResult = QiniuApplication.cdnManager.getFluxData(domains, fromDate, toDate, "day");
        } catch (QiniuException e) {
            logger.error("get bucket flux error, message: " + e.getMessage());
            Platform.runLater(() -> Dialogs.showException(Values.BUCKET_FLUX_ERROR, e));
        }
        Series<String, Long> fluxSer = new Series<>();
        fluxSer.setName(Values.BUCKET_FLUX_COUNT.replaceAll("[A-Z]+", countUnit));
        // 获取流量统计
        if (Checker.isNotNull(fluxResult) && Checker.isNotEmpty(fluxResult.data)) {
            for (Map.Entry<String, FluxData> flux : fluxResult.data.entrySet()) {
                String[] times = fluxResult.time;
                FluxData fluxData = flux.getValue();
                int i = 0;
                for (String time : times) {
                    long size = 0;
                    if (Checker.isNotNull(fluxData)) {
                        if (Checker.isNotNull(fluxData.china)) {
                            size += fluxData.china[i];
                        }
                        if (Checker.isNotNull(fluxData.oversea)) {
                            size += fluxData.oversea[i];
                        }
                    }
                    long unit = Formatter.sizeToLong("1 " + countUnit);
                    fluxSer.getData().add(new Data<>(time.substring(5, 10), size / unit));
                    i++;
                }
            }
        } else {
            logger.info("flux is empty of this domain");
        }
        return fluxSer;
    }

    /**
     * 日志下载，cdn相关
     */
    public void downloadCdnLog(String logDate) {
        if (Checker.isNotEmpty(QiniuApplication.buckets) && Checker.isDate(logDate)) {
            String[] domains = new String[QiniuApplication.buckets.size()];
            int i = 0;
            for (Map.Entry<String, String> bucket : QiniuApplication.buckets.entrySet()) {
                domains[i] = bucket.getValue().split(" ")[1];
                i++;
            }
            try {
                CdnResult.LogListResult logRes = QiniuApplication.cdnManager.getCdnLogList(domains, logDate);
                Downloader downloader = new Downloader();
                for (Map.Entry<String, LogData[]> logs : logRes.data.entrySet()) {
                    for (LogData log : logs.getValue()) {
                        downloader.downloadFromNet(log.url);
                    }
                }
            } catch (QiniuException e) {
                logger.error("get cdn log url error, message: " + e.getMessage());
                Dialogs.showException(e);
            }
        }
    }

    /**
     * 刷新文件，cdn相关
     */
    public void refreshFile(ObservableList<FileInfo> fileInfos, String domain) {
        if (Checker.isNotEmpty(fileInfos)) {
            String[] files = new String[fileInfos.size()];
            int i = 0;
            for (FileInfo fileInfo : fileInfos) {
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
            QiniuApplication.cdnManager.refreshUrls(files);
            logger.info("refresh files success");
        } catch (QiniuException e) {
            logger.error("refresh files error, message: " + e.getMessage());
            Dialogs.showException(e);
        }
    }

    public void privateDownload(String fileName, String domain) {
        privateDownload(fileName, domain, new Downloader());
    }

    /**
     * 私有下载
     */
    public void privateDownload(String fileName, String domain, Downloader downloader) {
        // 自定义链接过期时间（小时）
        long expireInSeconds = 24;
        String publicURL = getPublicURL(fileName, domain);
        downloader.downloadFromNet(QiniuApplication.auth.privateDownloadUrl(publicURL, expireInSeconds));
    }

    public void publicDownload(String fileName, String domain) {
        publicDownload(fileName, domain, new Downloader());
    }

    /**
     * 公有下载
     */
    public void publicDownload(String fileName, String domain, Downloader downloader) {
        String url = getPublicURL(fileName, domain);
        if (Checker.isNotEmpty(url)) {
            downloader.downloadFromNet(url);
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
            QiniuApplication.bucketManager.prefetch(bucket, key);
            logger.info(log + "' success");
        } catch (QiniuException e) {
            logger.error(log + "' error, message: " + e.getMessage());
            Dialogs.showException(Values.UPDATE_ERROR, e);
        }
    }

    /**
     * 设置文件生存时间
     */
    public void setFileLife(String bucket, String key, int days) {
        String log = "set file of '" + key + "' life to " + days + " day(s) ";
        try {
            QiniuApplication.bucketManager.deleteAfterDays(bucket, key, days);
            logger.info(log + "success");
        } catch (QiniuException e) {
            logger.error(log + "error, message: " + e.getMessage());
            Dialogs.showException(Values.MOVE_OR_RENAME_ERROR, e);
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
        if (new QiConfiger().checkNet()) {
            String log = "move file '" + fromKey + "' from bucket '" + fromBucket + "' to bucket '" + toBucket + "', " +
                    "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "and rename file '" +
                    toKey + "'";
            try {
                if (action == FileAction.COPY) {
                    log = log.replaceAll("^move", "copy");
                    QiniuApplication.bucketManager.copy(fromBucket, fromKey, toBucket, toKey, true);
                } else {
                    QiniuApplication.bucketManager.move(fromBucket, fromKey, toBucket, toKey, true);
                }
                logger.info(log + " success");
                return true;
            } catch (QiniuException e) {
                logger.error(log + " failed, message: " + e.getMessage());
                Dialogs.showException(Values.MOVE_OR_RENAME_ERROR, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 修改文件类型
     */
    public boolean changeType(String fileName, String newType, String bucket) {
        if (new QiConfiger().checkNet()) {
            String log = "change file '" + fileName + "' type '" + newType + "' on bucket '" + bucket;
            try {
                QiniuApplication.bucketManager.changeMime(bucket, fileName, newType);
                logger.info(log + "' success");
                return true;
            } catch (QiniuException e) {
                logger.error(log + "' failed, message: " + e.getMessage());
                Dialogs.showException(Values.CHANGE_FILE_TYPE_ERROR, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 批量删除文件，单次批量请求的文件数量不得超过1000
     */
    public void deleteFiles(ObservableList<FileInfo> fileInfos, String bucket) {
        if (Checker.isNotEmpty(fileInfos) && new QiConfiger().checkNet()) {
            // 生成待删除的文件列表
            String[] files = new String[fileInfos.size()];
            ArrayList<FileInfo> seletecFileInfos = new ArrayList<>();
            int i = 0;
            for (FileInfo fileInfo : fileInfos) {
                files[i++] = fileInfo.getName();
                seletecFileInfos.add(fileInfo);
            }
            try {
                BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
                batchOperations.addDeleteOp(bucket, files);
                Response response = QiniuApplication.bucketManager.batch(batchOperations);
                BatchStatus[] batchStatusList = response.jsonToObject(BatchStatus[].class);
                MainWindowController main = MainWindowController.getInstance();
                // 文件列表是否为搜索后结果
                boolean sear = Checker.isNotEmpty(main.searchTextField.getText());
                ObservableList<FileInfo> currentRes = main.resTable.getItems();
                for (i = 0; i < files.length; i++) {
                    BatchStatus status = batchStatusList[i];
                    String deleteLog = Formatter.datetimeToString(new Date());
                    String file = files[i];
                    if (status.code == 200) {
                        logger.info("delete file '" + file + "' success");
                        deleteLog += "\tsuccess\t";
                        QiniuApplication.data.remove(seletecFileInfos.get(i));
                        QiniuApplication.totalLength--;
                        QiniuApplication.totalSize -= Formatter.sizeToLong(seletecFileInfos.get(i).getSize());
                        if (sear) {
                            currentRes.remove(seletecFileInfos.get(i));
                        }
                    } else {
                        logger.error("delete file '" + file + "' failed, message: " + status.data.error);
                        deleteLog += "\tfailed\t";
                        Dialogs.showError("删除文件：" + file + " 失败");
                    }
                    QiniuApplication.deleteLog.append(deleteLog).append(bucket).append("\t").append(file).append
                            ("\r\n");
                }
            } catch (QiniuException e) {
                Dialogs.showException(Values.DELETE_ERROR, e);
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
        BucketManager.FileListIterator iterator = QiniuApplication.bucketManager.createFileListIterator(bucket, "",
                Values.BUCKET_LIST_LIMIT_SIZE, "");
        ArrayList<FileInfo> files = new ArrayList<>();
        logger.info("get file list of bucket: " + bucket);
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
                FileInfo file = new FileInfo(item.key, item.mimeType, size, time);
                files.add(file);
                logger.info("file name: " + item.key + ", file type: " + item.mimeType + ", file size: " + size + ", " +
                        "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "file time: " + time);
            }
        }
        QiniuApplication.data = FXCollections.observableArrayList(files);
    }

    private void urlError(Exception e) {
        logger.error("generate url error: " + e.getMessage());
        Dialogs.showException(Values.GENERATE_URL_ERROR, e);
    }

    public enum FileAction {
        // 复制或移动文件
        COPY, MOVE
    }
}
