/**
 *
 */
package com.zhazhapan.qiniu;

import com.zhazhapan.util.Checker;
import org.junit.Test;

/**
 * @author pantao
 */
public class RegexpTest {

    @Test
    public void testGetFileName() {
        String[] files = {"http://oxns0wnsc.bkt.clouddn.com/fims/css/distpicker.css", "http://oxns0wnsc.bkt.clouddn"
                + ".com/fims/css/distpicker", "http://oxns0wnsc.bkt.clouddn.com/fims/css/easy-responsive-tabs.css",
                "http://oxns0wnsc.bkt.clouddn.com/zhazhapan/test/%E6%BD%98%E6%BB%94-18780459330-Java%E5%BC%80%E5%8F"
                        + "%91.docx"};
        for (String file : files) {
            System.out.println(QiniuUtils.getFileName(file));
        }
    }

    @Test
    public void testHyperlink() {
        System.out.println(Checker.isHyperLink("https://portal.qiniu.com/bucket/zhazhapan/resource"));
        System.out.println(Checker.isHyperLink("http://portal.qiniu.com/bucket/zhazhapan/resource"));
        System.out.println(Checker.isHyperLink("portal.qiniu.com/bucket/zhazhapan/resource"));
        System.out.println(Checker.isHyperLink("oxns0wnsc.bkt.clouddn.com"));
        System.out.println(Checker.isHyperLink("hhttp://portal"));
        System.out.println(Checker.isHyperLink("https://cn.wordpress.org/wordpress-4.8.1-zh_CN.zip"));
    }
}
