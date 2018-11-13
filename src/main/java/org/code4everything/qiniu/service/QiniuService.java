package org.code4everything.qiniu.service;

import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.api.SdkManager;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.util.DialogUtils;

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
