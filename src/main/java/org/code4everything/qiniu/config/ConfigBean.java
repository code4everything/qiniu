package org.code4everything.qiniu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

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

    private String accesskey;

    private String secretkey;

    private ArrayList<BucketBean> buckets;

    private ArrayList<String> prefixes;

    private String storagePath;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class BucketBean implements Serializable {

        private String bucket;

        private String zone;

        private String url;
    }
}
