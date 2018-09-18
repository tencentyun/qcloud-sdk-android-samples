# CSP 使用文档

## 快速接入

### 接入准备

1. SDK 支持 Android 2.2 及以上版本的手机系统；
2. 手机必须要有网络（GPRS、3G 或 WIFI 网络等）；
3. 手机可以没有存储空间，但会使部分功能无法正常工作；
4. 从 [COS 控制台](https://console.cloud.tencent.com/cos4/secret) 获取 APPID、SecretId、SecretKey。

> 关于文章中出现的 SecretId、SecretKey、Bucket 等名称的含义和获取方式请参考：[COS 术语信息](https://cloud.tencent.com/document/product/436/7751)

### 集成 SDK

需要在工程项目中导入下列 jar 包，存放在 libs 文件夹下：

- cos-android-sdk.jar
- qcloud-foundation.jar
- bolts-tasks.jar
- okhttp.jar
- okio.jar

您可以在这里 [COS XML Android SDK-release](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCSPSample/app/libs) 下载所有的 jar 包。

> cos-android-sdk.jar 必须使用 5.4.14 及其以上版本。

### 配置权限

使用该 SDK 需要网络、存储等相关的一些访问权限，可在 AndroidManifest.xml 中增加如下权限声明（Android 5.0 以上还需要动态获取权限）：
```html
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

> 示例 demo 请参考 [QCloudCSPSample](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCSPSample)

## 快速入门

### 初始化 

在执行任何和 COS 服务相关请求之前，都需要先实例化 CosXmlService 对象，具体可分为如下几步：

#### 初始化配置类

`CosXmlServiceConfig` 是 COS 服务的配置类，您可以使用如下代码来初始化：

```
String appid = "对象存储的服务 APPID";
String region = "存储桶所在的地域";
boolean isHttps = false;

/**
 * 您的服务器对应的主域名，默认为 myqcloud.com，设置后访问地址为：
 * {bucket-name}-{appid}.cos.{cos-region}.{domainSuffix}
 */
String domainSuffix = "your domain suffix"; 

//创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(isHttps)
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .setDomainSuffix(domainSuffix)  //私有云需要设置主域名
                .builder();
```

#### 初始化授权类

`QCloudCredentialProvider` 是 COS 服务的授权类，可以给请求添加签名来认证您的身份。

##### 通过设置签名字符串进行授权（推荐）

私有云存储暂时不支持用临时密钥进行授权，您必须给每个 CSP 请求单独设置签名串来进行授权，签名串的计算方式请参考 [请求签名](https://cloud.tencent.com/document/product/436/7778)，这里您首先需要将授权类配置为 `null`：

```
/**
 * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
 */
QCloudCredentialProvider credentialProvider = null;
```
##### 通过永久密钥进行授权

除了通过直接设置签名串来进行授权，您还可以使用永久密钥来初始化授权类，需要指出的是，由于会存在泄漏密钥的风险，我们**强烈不推荐您使用这种方式**，您应该仅仅在安全的环境下临时测试时使用：

```
String secretId = "云 API 密钥 SecretId";
String secretKey ="云 API 密钥 SecretKey";

/**
 * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
 */
QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId,
                secretKey, 300);
```

#### 初始化 COS 服务类

`CosXmlService` 是 COS 服务类，可用来操作各种 COS 服务，当您实例化配置类和授权类后，您可以很方便的实例化一个 COS 服务类，具体代码如下：

````java
CosXmlService cosXmlService = new CosXmlService(context, serviceConfig, credentialProvider);
````

### 上传文件

将本地文件上传到 COS，适用于图片类小文件(20M以下)上传，最大支持 5 GB(含), 5 GB 以上必须使用分块上传。如果COS上已存在对象, 则会进行覆盖。简单上传接口无法进行暂停和续传，一旦在上传过程中出现异常情况导致失败，那么您需要重新上传，具体代码如下：

```java
String bucketName = "your bucket name";
String cosPath = "上传到 cos 的路径";
String localPath = "本地文件路径";

PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, cosPath, localPath);
putObjectRequest.setProgressListener(new CosXmlProgressListener() {
    @Override
    public void onProgress(long progress, long max) {
        // todo Do something to update progress...
    }
});

/**
 * 从远程服务端获取签名来授权请求，如果您通过永久密钥进行授权，那么不再需要调用 setSign() 方法。
 */
String sign = getSignFromRemoteService(putObjectRequest);
putObjectRequest.setSign(sign);

return cosXmlService.putObject(putObjectRequest);
```
> 所有的请求都有同步和异步两种方式，以上示例代码中采用的是同步方式。

### 下载文件

将 COS 上的文件下载到本地。

```java
String bucket = "bucket";
String cosPath = "cosPath";
String savePath = "savePath";

GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, savePath);
getObjectRequest.setProgressListener(new CosXmlProgressListener() {
    @Override
    public void onProgress(long progress, long max) {
        // todo Do something to update progress...
    }
});

//使用同步方法下载
try {
    GetObjectResult getObjectResult = cosXmlService.getObject(getObjectRequest);
} catch (CosXmlClientException e) {
    e.printStackTrace();
} catch (CosXmlServiceException e) {
    e.printStackTrace();
}
```

### 释放客户端

如果不再需要使用 COS 服务，可以调用 `release()` 方法来释放资源:

```java
cosXmlService.release();
```

## 和公有云对比

- 公有云 `domainSuffix` 不可修改，私有云默认和公有云保持一致，为 `myqcloud.com`，但是允许用户自定义；
- 公有云支持临时密钥，私有云不支持；
- 公有云支持 UploadService 上传，私有云不支持；
- cos-android-sdk.jar 必须使用 5.4.14 及其以上版本。


> 更多使用接口请参考：[Android SDK 接口文档](https://cloud.tencent.com/document/product/436/11238)
