package org.code4everything.qiniu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息
 *
 * @author pantao
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileBean {

    private String name;

    private String type;

    private String size;

    private String time;
}
