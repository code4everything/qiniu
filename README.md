# 七牛云——对象存储管理工具介绍

**由于我是一个七牛的重度使用者（主要是对象存储），每次上传文件、复制链接、下载文件都必须用浏览器打开网页，而且还要登录，感觉好麻烦啊，干脆就自己开发了一个这样的工具（使用JavaFX编写），打包成可执行的jar包。**

**主要功能就是文件的上传下载，获取存储空间中的文件列表，搜索文件（支持正则表达式），复制文件外链，删除文件，移动（或复制）文件，重命名文件名，设置文件的生存时间等。**

**功能截图：**

**1. 主窗口界面：**

![程序主界面](http://img.blog.csdn.net/20171019104749116?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 设置文件前缀

    路径前缀可以用来分类文件，例如： `image/jpg/`your-file-name.jpg

- 添加存储空间

    添加存储空间，需要同时指定空间名称，空间域名以及所在区域

    ![添加存储空间](http://img.blog.csdn.net/20171017122858110?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 重置密钥

    如果你修改了Key，可以在此处修改密钥

    ![重置密钥](http://img.blog.csdn.net/20171017123714044?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

    > 说明：出于安全考虑，建议您周期性地更换密钥。[查看我的密钥](https://portal.qiniu.com/user/key) [密钥安全使用须知](https://developer.qiniu.com/kodo/kb/1334/the-access-key-secret-key-encryption-key-safe-use-instructions)

- 配置文件

    Windows使用路径：`C:/ProgramData/QiniuTool`

    MacOS 或 Linux 使用路径：`/tmp/qiniu/tool`

    其中 `config.json` 为配置文件

- 文件上传

    除了可（支持断点和覆盖）上传本地文件外，还可抓取网络文件到空间中，如：

    ![上传网络文件](http://img.blog.csdn.net/20171019105056517?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

    然后点击 `开始上传` 即可

**2. 资源管理界面：**

![资源管理界面](http://img.blog.csdn.net/20171019105225191?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 刷新列表
        
    刷新当前存储空间的资源列表
        
- 复制链接

    复制你选中文件的外链

- 删除文件

    从存储空间中删除你选中的所有文件

- 移动文件

    移动（或复制）选中的所有文件到指定的存储空间中（目前好像七牛还不支持跨区域移动文件）
        
    ![移动文件界面](http://img.blog.csdn.net/20171015222512819?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

    > 说明：勾选“保存文件副本”时表示当前操作为复制，不勾选时表示移动（会删除本存储空间的文件），默认勾选。

- 生存时间

    设置选中文件的生存时间，到期后七牛会自动删除这些文件

- 更新镜像

    此功能首先需要你配置了镜像存储

    > 官方解释：对于配置了镜像存储的空间，如果镜像源站更新了文件内容，则默认情况下，七牛不会再主动从客户镜像源站同步新的副本，这个时候就需要利用这个prefetch接口来主动地将空间中的文件和更新后的源站副本进行同步。

- 公有下载

    直接下载选中的文件（私有的存储空间不可用）

- 私有下载

    下载选中的私有存储空间的文件

- 打开文件

    用浏览器打开你选中的文件

- 链接下载

    由于这两天迅雷抽风了，下载出现BUG，所以提供了这样一个临时的下载方案，打开后直接输入链接即可下载

- 文件刷新

    从七牛云镜像源刷新你选中的文件，保证用户下载的是最新上传的文件，而不是之前的旧版本（个人感觉和 `更新镜像` 是一样的）

- 日志下载

    从七牛下载指定日期的操作日志

> 说明：操作文件时，需要选中文件才能操作（支持多选）。由于下载私有空间的文件需要临时授权，所以文件的下载分为私有下载（生成临时授权然后下载文件）和公有下载（直接下载文件）。

**3. 数据统计界面**

![数据统计界面](http://img.blog.csdn.net/20171019105531070?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjY5NTQ3NzM=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

> 说明：时间范围不超过31天，否则无法获取数据，这是七牛官方规定的。

**4. 其他**

- [**下载可执行的jar包**](http://oq3iwfipo.bkt.clouddn.com/tools/zhazhapan/qiniu.jar?v=4 "七牛云——对象存储管理工具jar包下载地址")

- [**历史版本**](https://github.com/zhazhapan/qiniu/releases) 

- [**项目源代码**](https://github.com/zhazhapan/qiniu "七牛云——对象存储管理工具项目源码地址")

- [**官方JavaSDK文档**](https://developer.qiniu.com/kodo/sdk/1239/java)