package org.code4everything.qiniu.service;

import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.api.SdkManager;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.model.FileBean;
import org.code4everything.qiniu.util.DialogUtils;
import org.code4everything.qiniu.util.QiniuUtils;

import java.util.Map;

/**
 * 七牛服务类
 *
 * @author pantao
 * @since 2018/11/13
 */
public class QiniuService {

    // TODO: 2018/11/13 服务类是否需要弹出错误框？

    private static final Logger LOGGER = Logger.getLogger(QiniuService.class);

    private final SdkManager sdkManager = new SdkManager();

    /**
     * 公有下载
     */
    public void publicDownload(String fileName, String domain) {
        QiniuUtils.download(QiniuUtils.joinUrl(fileName, domain));
    }

    /**
     * 私有下载
     */
    public void privateDownload(String fileName, String domain) {
        QiniuUtils.download(sdkManager.getPrivateUrl(QiniuUtils.joinUrl(fileName, domain)));
    }

    /**
     * 刷新文件
     */
    public void refreshFile(ObservableList<FileBean> fileBeans, String domain) {
        if (Checker.isNotEmpty(fileBeans)) {
            String[] files = new String[fileBeans.size()];
            int i = 0;
            // 获取公有链接
            for (FileBean fileBean : fileBeans) {
                files[i++] = QiniuUtils.joinUrl(fileBean.getName(), domain);
            }
            try {
                // 属性文件
                sdkManager.refreshFiles(files);
                LOGGER.info("refresh files success");
            } catch (QiniuException e) {
                LOGGER.error("refresh files error, message -> " + e.getMessage());
                DialogUtils.showException(e);
            }
        }
    }

    /**
     * 日志下载
     */
    public void downloadCdnLog(String logDate) {
        if (Checker.isNotEmpty(QiniuApplication.getConfigBean().getBuckets()) && Checker.isDate(logDate)) {
            // 转换域名成数组格式
            String[] domains = new String[QiniuApplication.getConfigBean().getBuckets().size()];
            for (int i = 0; i < QiniuApplication.getConfigBean().getBuckets().size(); i++) {
                domains[i] = QiniuApplication.getConfigBean().getBuckets().get(i).getUrl();
            }
            Map<String, CdnResult.LogData[]> cdnLog = null;
            try {
                cdnLog = sdkManager.listCdnLog(domains, logDate);
            } catch (QiniuException e) {
                DialogUtils.showException(e);
            }
            if (Checker.isNotEmpty(cdnLog)) {
                // 下载日志
                for (Map.Entry<String, CdnResult.LogData[]> logs : cdnLog.entrySet()) {
                    for (CdnResult.LogData log : logs.getValue()) {
                        QiniuUtils.download(log.url);
                    }
                }
            }
        }
    }


    /**
     * 获取空间带宽统计，使用自定义单位
     */
    public XYChart.Series<String, Long> getBucketBandwidth(String[] domains, String startDate, String endDate,
                                                           String unit) {
        // 获取带宽数据
        CdnResult.BandwidthResult bandwidthResult = null;
        try {
            bandwidthResult = sdkManager.getBucketBandwidth(domains, startDate, endDate);
        } catch (QiniuException e) {
            Platform.runLater(() -> DialogUtils.showException(QiniuValueConsts.BUCKET_BAND_ERROR, e));
        }
        // 设置图表
        XYChart.Series<String, Long> series = new XYChart.Series<>();
        series.setName(QiniuValueConsts.BUCKET_BANDWIDTH_COUNT.replaceAll("[A-Z]+", unit));
        // 格式化数据
        if (Checker.isNotNull(bandwidthResult) && Checker.isNotEmpty(bandwidthResult.data)) {
            long unitSize = Formatter.sizeToLong("1 " + unit);
            for (Map.Entry<String, CdnResult.BandwidthData> bandwidth : bandwidthResult.data.entrySet()) {
                CdnResult.BandwidthData bandwidthData = bandwidth.getValue();
                if (Checker.isNotNull(bandwidthData)) {
                    setSeries(bandwidthResult.time, bandwidthData.china, bandwidthData.oversea, series, unitSize);
                }
            }
        }
        return series;
    }

    /**
     * 获取空间的流量统计，使用自定义单位
     */
    public XYChart.Series<String, Long> getBucketFlux(String[] domains, String startDate, String endDate, String unit) {
        // 获取流量数据
        CdnResult.FluxResult fluxResult = null;
        try {
            fluxResult = sdkManager.getBucketFlux(domains, startDate, endDate);
        } catch (QiniuException e) {
            Platform.runLater(() -> DialogUtils.showException(QiniuValueConsts.BUCKET_FLUX_ERROR, e));
        }
        // 设置图表
        XYChart.Series<String, Long> series = new XYChart.Series<>();
        series.setName(QiniuValueConsts.BUCKET_FLUX_COUNT.replaceAll("[A-Z]+", unit));
        // 格式化数据
        if (Checker.isNotNull(fluxResult) && Checker.isNotEmpty(fluxResult.data)) {
            long unitSize = Formatter.sizeToLong("1 " + unit);
            for (Map.Entry<String, CdnResult.FluxData> flux : fluxResult.data.entrySet()) {
                CdnResult.FluxData fluxData = flux.getValue();
                if (Checker.isNotNull(fluxData)) {
                    setSeries(fluxResult.time, fluxData.china, fluxData.oversea, series, unitSize);
                }
            }
        }
        return series;
    }

    /**
     * 处理带宽数据
     */
    private void setSeries(String[] times, Long[] china, Long[] oversea, XYChart.Series<String, Long> series,
                           long unit) {
        int i = 0;
        for (String time : times) {
            long size = 0;
            if (Checker.isNotEmpty(china)) {
                size += china[i];
            }
            if (Checker.isNotEmpty(oversea)) {
                size += oversea[i];
            }
            series.getData().add(new XYChart.Data<>(time.substring(5, 10), size / unit));
            i++;
        }
    }
}
