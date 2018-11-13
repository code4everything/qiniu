package org.code4everything.qiniu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 桶信息
 *
 * @author pantao
 * @since 2018/11/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketBean implements Serializable {

    private String bucket;

    private String zone;

    private String url;
}
