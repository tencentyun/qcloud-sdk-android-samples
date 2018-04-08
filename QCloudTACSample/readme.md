## 开始

### 准备工作

请先将本示例工程下载到本地。

### 第一步：创建项目和应用

在使用我们的服务前，您必须先在 MobileLine 控制台上 [创建项目和应用](https://cloud.tencent.com/document/product/666/15345)，注意在控制台上创建的应用包名必须和我们的示例工程的包名保持一致，为 `com.tencent.tac.sample`。

### 第二步：添加配置文件

在您创建好的应用上点击【下载配置】按钮来下载该应用的配置文件的压缩包：

![](http://tacimg-1253960454.cosgz.myqcloud.com/guides/project/downloadConfig.png)

解压该压缩包，您会得到 `tac_service_configurations.json` 和 `tac_service_configurations_unpackage.json` 两个文件，请您如图所示添加到您自己的工程中去（如果工程中已存在，请直接覆盖）。

<img src="http://tac-android-libs-1253960454.cosgz.myqcloud.com/tac_android_configuration.jpg" width="50%" height="50%">

## 使用

将工程在 Android Studio 中构建，并在 Android 设备中运行，结果如下：

### 移动分析服务 Analytics 使用

运行示例工程后，会自动将应用的数据上报您的控制台，您可以在控制台上查看信息。

### 崩溃监测服务 Crashlytics 使用

运行示例工程后，若应用发生了 Crash，会自动将 Crash 信息发送到控制台，帮助您定位和修复 Bug。您也可以通过点击应用提供的 Crash 按钮，来帮助您测试数据是否正常上报。

### 移动推送服务 Messaging 使用

运行示例工程后，您可以在通知台发送通知栏消息，应用会接收到消息，并展示到通知栏上。接收到通知后，您也会收到回调信息，以帮助您进一步处理消息。

### 腾讯计费 Payment 使用

在使用前您需要：

- [配置后台服务器](https://cloud.tencent.com/document/product/666/14600)
- [添加支付渠道信息](https://cloud.tencent.com/document/product/666/14599)

配置好后，您即可发起 app 支付，并在控制台上查看支付信息。

### 移动存储服务 Storage 使用

运行示例工程后，您首先需要修改 `com.tencent.tac.App` 类下的 `initStorage()` 方法来配置签名获取服务器（[如何配置](https://cloud.tencent.com/document/product/666/15350)）。配置好后，您即上传、下载或者删除数据。

### 授权服务 Authorization 使用

下载示例代码后，即可在示例代码中进行微信和 QQ 登录。

## 后续步骤

### 了解 MobileLine：

- 借助 [Analytics](https://cloud.tencent.com/document/product/666/14822) 深入分析用户行为。
- 借助 [messaging](https://cloud.tencent.com/document/product/666/14826) 向用户发送通知。
- 借助 [crash](https://cloud.tencent.com/document/product/666/14824) 确定应用崩溃的时间和原因。
- 借助 [storage](https://cloud.tencent.com/document/product/666/14828) 存储和访问用户生成的内容（如照片或视频）。
- 借助 [authorization](https://cloud.tencent.com/document/product/666/14830) 来进行用户身份验证。
- 借助 [payment](https://cloud.tencent.com/document/product/666/14832) 获取微信和手 Q 支付能力

```
Copyright (c) 2017 腾讯云

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
