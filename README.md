# 七牛云——对象存储管理工具介绍

**由于我是一个七牛的重度使用者（主要是对象存储），每次上传文件、复制链接、下载文件都必须用浏览器打开网页，而且还要登录，感觉好麻烦啊，干脆就自己开发了一个这样的工具（使用JavaFX编写），打包成jar包。**

**主要功能就是文件的上传下载，获取存储空间中的文件列表，搜索文件（支持正则表达式），复制文件外链，删除文件，移动（或复制）文件，重命名文件名，设置文件的生存时间。**

**功能截图：**

- 主窗口界面：

    ![程序主界面](http://img.blog.csdn.net/20171015221834257?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 资源管理界面：

    ![资源管理界面](http://img.blog.csdn.net/20171015222108447?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
    
    > 说明：操作文件时，需要选中文件才能操作（支持多选），由于下载私有空间的文件需要临时授权，所以文件的下载分为私有下载（生成临时授权然后下载文件）和公有下载（直接下载文件）。

    *移动文件的界面：*

    ![移动文件界面](http://img.blog.csdn.net/20171015222512819?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

    > 说明：勾选“保存文件副本”时表示当前操作为复制，不勾选时表示移动（会删除本存储空间的文件），默认勾选。


- [**历史版本**](https://github.com/zhazhapan/qiniu/releases) 或 [**直接下载**](http://oq3iwfipo.bkt.clouddn.com/tools/zhazhapan/qiniu.jar?v=1.0.1 "七牛云——对象存储管理工具jar包下载地址")

- [**项目源代码**](https://github.com/zhazhapan/qiniu "七牛云——对象存储管理工具项目源码地址")