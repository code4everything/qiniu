package org.code4everything.qiniu.model;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 应用配置信息
 *
 * @author pantao
 * @since 2018/11/12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigBean implements Serializable {

    private String accessKey;

    private String secretKey;

    private ArrayList<BucketBean> buckets;

    private ArrayList<String> prefixes;

    private String storagePath;

    public String getBucket(String bucket) {
        BucketBean bucketBean = getBucketBean(bucket);
        return Objects.isNull(bucketBean) ? "" : bucketBean.getBucket();
    }

    public String getUrl(String bucket) {
        BucketBean bucketBean = getBucketBean(bucket);
        return Objects.isNull(bucketBean) ? "" : bucketBean.getUrl();
    }

    public String getZone(String bucket) {
        BucketBean bucketBean = getBucketBean(bucket);
        return Objects.isNull(bucketBean) ? "" : bucketBean.getZone();
    }

    public BucketBean getBucketBean(String bucket) {
        if (CollectionUtil.isNotEmpty(buckets)) {
            for (BucketBean bean : buckets) {
                if (Objects.equals(bean.getBucket(), bucket)) {
                    return bean;
                }
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class BucketBean implements Serializable {

        private String bucket;

        private String zone;

        private String url;
    }
}
