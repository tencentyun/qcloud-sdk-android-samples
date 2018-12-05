## 快速接入

### 接入准备

1. SDK 支持 Android 2.2 及以上版本的手机系统；
2. 手机必须要有网络（GPRS、3G 或 WIFI 网络等）；
3. 手机可以没有存储空间，但会使部分功能无法正常工作；
4. 从控制台获取 APPID、SecretId、SecretKey。

### 集成 SDK

#### 自动集成（**推荐**）

1、在您的项目根目录下的 build.gradle 文件中添加 maven 仓库：

```
allprojects {

    repositories {
        ...
        // 添加如下 maven 仓库地址
        maven {
            url "https://dl.bintray.com/tencentqcloudterminal/maven"
        }
    }
}
```

2、在应用的根目录下的 build.gradle 中添加依赖：

```
dependencies {
	...
    // 增加这行
    compile 'com.tencent.qcloud:cosxml:5.4.16'
}
```

#### 手动集成

需要在工程项目中导入下列 jar 包，存放在 libs 文件夹下：

- cos-android-sdk.jar
- qcloud-foundation.jar
- bolts-tasks.jar
- okhttp.jar
- okio.jar
- mid-sdk.jar
- mta-android-sdk.jar

您可以在这里 [COS XML Android SDK-release](https://github.com/tencentyun/qcloud-sdk-android/releases) 下载所有的 jar 包，建议您使用最新的 release 包。

> 私有云 CSP 中 cos-android-sdk.jar 必须使用 5.4.16 及其以上版本、qcloud-foundation 必须使用 1.5.3 及其以上版本。

### 配置权限

使用该 SDK 需要网络、存储等相关的一些访问权限，可在 AndroidManifest.xml 中增加如下权限声明（Android 5.0 以上还需要动态获取权限）：
```html
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

### 更多资源

**源码和示例工程**

COS Android SDK 相关源码请参见 [COS Android SDK Github 地址](https://github.com/tencentyun/qcloud-sdk-android)；
示例 demo 请参见 [COS Android SDK 示例工程](https://github.com/tencentyun/qcloud-sdk-android-samples/edit/master/QCloudCSPSample)。

**更新日志**

COS Android SDK 更新日志请参见 [COS Android SDK 更新日志](https://github.com/tencentyun/qcloud-sdk-android/blob/master/CHANGELOG.md)。

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
                .setAppidAndRegion(appid, region) // 如果没有 appid 和 region，请设置为空
                .setDomainSuffix(domainSuffix)  //私有云需要设置主域名，默认为 myqcloud.com
                .setBucketInPath(false) // 是否将 Bucket 放在 URL 的 Path 中，默认为 false
                .setDebuggable(true) // 是否打印调试日志
                .builder();
```

#### 初始化授权类

您需要实例化一个COS 服务的授权类，来给请求添加签名来认证您的身份。

##### 通过签名服务器授权（推荐）

私有云存储暂时不支持用临时密钥进行授权，为了保证密钥信息的安全性，您必须根据相应 HTTP 请求的参数在服务端计算签名后，返回给客户端使用。

- 自定义协议进行授权

通过自定义签名服务器和终端 SDK 之间签名串的交互协议，您可以获得最大的灵活性，首先您需要实现 `QCloudSigner` 接口

```
public class MyQCloudSigner implements QCloudSigner {

    /**
     * @param request 即为发送到 CSP 服务端的请求，您需要根据这个 HTTP 请求的参数来计算签名，并给其添加 Authorization header
     * @param credentials 空字段，请不要使用
     * @throws QCloudClientException 您可以在处理过程中抛出异常
     */
    @Override
    public void sign(QCloudHttpRequest request, QCloudCredentials credentials) throws QCloudClientException {

        /**
         * 获取计算签名所需字段
         */
        URL url = request.url();
        String method = request.method();
        String host = url.getHost();
        String schema = url.getProtocol();
        String path = url.getPath();
        Map<String, List<String>> headers = request.headers();

        /**
         * 向您自己的服务端请求签名
         */
        String sign = getSignFromYourServer(method, schema, host, path, headers);

        /**
         * 给请求设置 Authorization Header
         */
        request.addHeader("Authorization", sign);
    }
}
```

然后实例化一个实现了 `QCloudSigner` 接口的对象：

```
QCloudSigner credentialProvider = new MyQCloudSigner();
```
- 按照默认协议进行授权

为了尽可能的简化用户的开发成本，如果您使用 HTTP 协议，并按如下格式实现终端和服务端授权通信，那么我们会自动帮您去解析签名服务器返回的签名串，具体协议如下：

###### 请求示例

终端 SDK 会将所有签名需要的参数以 JSON 的格式放在 HTTP 请求的 body 中，然后发给您的签名服务器：

```
PUT http://10.19.90.144:5000/auth http/1.1
Content-Length: 165
Host: 10.19.90.144

{"method":"PUT","schema":"http","host":"cos.wh.yun.ccb.com","path":"\/man-2.mov","headers":{"User-Agent":"cos-android-sdk-5.4.14","Host":"cos.wh.yun.ccb.com","Content-MD5":"fTWBVPpSSep2CwMe7gEAaw=="},"params":{"partNumber":"10","uploadId":"uploadid"}}
```

###### 响应示例

您的签名服务器收到 HTTP 请求后，必须根据请求 body 中的参数来计算签名，然后以 JSON 的格式放在 HTTP 响应的 body 中，注意 JSON 的 key 必须为 "sign"。

```
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Content-Length: 20
Server: Werkzeug/0.14.1 Python/3.6.5
Date: Thu, 20 Sep 2018 13:42:35 GMT

{"sign":"q-sign-algorithm=sha1&q-ak=AKIDZuxhBMAbeOovjDtI42h3mCJ7dsnQwkSq&q-sign-time=1537494643;1537495243&q-key-time=1537494643;1537495243&q-header-list=&q-url-param-list=&q-signature=5a80f9fd31a4db772969a164bdad15a96efee73c"}
```

如果您的签名服务器是以如上协议来给终端发送签名，您可以直接使用 [RemoteCOSSigner](https://github.com/tencentyun/qcloud-sdk-android-samples/blob/master/QCloudCSPSample/app/src/main/java/com/tencent/qcloud/csp/sample/RemoteCOSSigner.java) 类来进行授权，该类只需要您传递一个实现了签名服务的 `URL` 地址即可。

> `RemoteCOSSigner` 并没有放在 SDK 中，请您直接从 [RemoteCOSSigner](https://github.com/tencentyun/qcloud-sdk-android-samples/blob/master/QCloudCSPSample/app/src/main/java/com/tencent/qcloud/csp/sample/RemoteCOSSigner.java) 中拷贝到您的工程下。那么您可以如下初始化授权类：

```
/**
 * 您的服务端签名的 URL 地址
 */
URL url = null;
try {
    url = new URL("your auth url");
} catch (MalformedURLException e) {
    e.printStackTrace();
}

QCloudSigner credentialProvider = new RemoteCOSSigner(url);
```

##### 通过永久密钥本地计算签名进行授权

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

`UploadService` 是一个通用的上传类，它可以上传不超过 50T 大小的文件，并支持暂停、恢复以及取消上传请求，同时对于超过 2M 的文件会有断点续传功能，我们推荐您使用这种方式来上传文件，上传部分示例代码如下：

```java
UploadService.ResumeData uploadData = new UploadService.ResumeData();
uploadData.bucket = "存储桶名称";
uploadData.cosPath = "[对象键](https://cloud.tencent.com/document/product/436/13324)，即存储到 COS 上的绝对路径"; //格式如 cosPath = "test.txt";
uploadData.srcPath = "本地文件的绝对路径"; // 如 srcPath =Environment.getExternalStorageDirectory().getPath() + "/test.txt";
uploadData.sliceSize = 1024 * 1024; //每个分片的大小
uploadData.uploadId = null; // 若是续传，则uploadId不为空

UploadService uploadService = new UploadService(cosXmlService, uploadData);
uploadService.setProgressListener(new CosXmlProgressListener() {
    @Override
    public void onProgress(long progress, long max) {
        // todo Do something to update progress...
    }
});

/**
 * 开始上传
 */
try {
    CosXmlResult cosXmlResult = uploadService.upload();
} catch (CosXmlClientException e) {
    e.printStackTrace();
} catch (CosXmlServiceException e) {
    e.printStackTrace();
}
```

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

## 接口测试

[QCloudCSPSample](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCSPSample) 是 CSP 的体验 demo，如果您想要对所有接口进行测试，可以参考公有云的接口测试 demo [QCloudCosXmlSample](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCosXmlSample)，注意请修改 com/tencent/qcloud/cosxml/sample/common/QServiceCfg.java 文件中 `CosXmlService` 的初始化部分（可以通过永久密钥授权，或者参考 QCloudCSPSample 中的授权方式）；

> 部分共有云接口私有云不支持，具体私有云支持的接口请参考 [接口文档](https://github.com/tencentyun/qcloud-sdk-android-samples/blob/master/QCloudCSPSample/CSP文档/接口文档.md)
