首先我们需要知道 COS 服务的整体架构如下图所示：

![](http://mc.qcloudimg.com/static/img/b1e187a9ec129ffc766c07a733ef4dd6/image.jpg)

其中 CAM 权限系统和 COS 对象存储是腾讯云提供给开发者的两大服务模块，而用户服务端和用户客户端需要用户自己去搭建，下面本文即介绍如何快速搭建一个基于 COS 的应用传输服务。

这里将依次介绍用户服务端和用户客户端两个部分：

## 搭建用户服务端

在本示例中，用户服务端仅仅提供临时密钥的功能。cos\_signer\_lite 是一个基于 flask 服务框架的临时秘钥服务，该项目采用 python3 编写，您可以在自己的机器或者虚拟机上运行该 python 项目，为您的 COS 终端（Android/IOS）SDK 提供临时密钥服务。
> 注意：该项目只是用于示例，不可直接用于生产环境，生产环境中您可以使用 COS 服务端 SDK 的临时秘钥接口。


### 配置环境

cos\_signer\_lite 采用 python3 编写，你首先需要安装 python3 环境，并在 python3 环境下安装 flask 模块。

```
pip3 install flask
```

### 配置密钥信息

cos\_signer\_lite 需要你在 ../cos\_signer\_lite/cam/config.py 中配置密钥信息，然后 cos\_signer\_lite 会在您每次请求时向 CAM 服务请求临时密钥。

```
    # 用户的secret id
    SECRET_ID = 'xxx'
    # 用户的secret key
    SECRET_KEY = 'xxx'
```

> 密钥信息可以在 [这里查询](https://console.cloud.tencent.com/cam/capi)


### 启动服务

您可以通过如下代码启动服务：

```
python3 main.py
```

### 测试服务

你可以通过浏览器访问 `http://0.0.0.0:5000/sign` 链接，如果浏览器返回如下信息，则说明服务已经成功运行。

```
{
 "code":0,
 "message":"",
 "codeDesc":"Success",
 "data":{
  "credentials":{
   "sessionToken":"634aa09dccc3274045ba413ec081c1df64007f0a30001",
   "tmpSecretId":"AKIDwxHZGTUvXAfcbLaOedJUQuwBXWUXG4m3",
   "tmpSecretKey":"kriDdZsOuuF9zrZPlSAVVG0Sg4RXZu6M"},
  "expiredTime":1530515889}
 }
```

## 搭建用户客户端

### 配置客户端

CosXmlSampleLite 提供了一个简单 Android 示例工程，在运行该工程前，您首先需要修改 [../CosXmlSampleLite/app/src/main/res/values/remote_storage.xml](https://github.com/tencentyun/qcloud-sdk-android-samples/blob/master/QCloudCosSimpleSample/CosXMLSampleLite/app/src/main/res/values/remote_storage.xml) 中的 `appid` 和 `host` 两个字段，其中 `appid` 可以在控制台上进行查看，而 `host` 字段为您的服务端所在的 IP 地址，修改后即可运行示例工程。

### 运行示例 Demo

#### 查询 bucket 列表并创建 bucket

启动示例 app 后，默认会显示您账号下所有 region 为北京的 bucket 列表，您也可以点击菜单在该 region 下创建新的 bucket：

![](http://cos-terminal-resource-1253960454.cossh.myqcloud.com/list_bucket.jpg)

#### 上传文件

您可以点击最下面的按钮依次【选择文件】->【开始上传】->【取消上传】来进行上传测试：

![](http://cos-terminal-resource-1253960454.cossh.myqcloud.com/upload_file.jpg)


您可以参考 [../src/main/java/com/tencent/cosxml/assistant/data/RemoteStorage.java](https://github.com/tencentyun/qcloud-sdk-android-samples/blob/master/QCloudCosSimpleSample/CosXMLSampleLite/app/src/main/java/com/tencent/cosxml/assistant/data/RemoteStorage.java) 文件来编写上传文件功能代码。
