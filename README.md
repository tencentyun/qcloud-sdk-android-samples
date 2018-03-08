# qcloud-sdk-android-samples

腾讯云 Android SDK 示例仓库

## 产品列表

当前仓库内提供的示例有:

* QCloudCosXmlSample - [腾讯云 COS XML](https://cloud.tencent.com/document/product/436)
* QCloudFaceInSample - [腾讯云 FaceIn 人脸核身](https://cloud.tencent.com/product/facein)

## 在 Android Studio 运行示例

示例是标准的 Android Studio 工程，您可以直接导入 IDE 中，点击运行即可。


## 在 Eclipse 运行示例

#### 1.确保 eclipse 已经安装 gradle 插件

如果没有安装，可以参考 [教程](http://www.vogella.com/tutorials/EclipseGradle/article.html)。

#### 2. 像普通工程一样导入eclipse

#### 3. 在 `Gradle Task` 视图中双击 `installDebug`

编译成功之后会自动安装到连接的设备或者模拟器中，如下所示：

![](http://ww1.sinaimg.cn/large/62f68aebgy1fp5mjdau62j20t80bpt98.jpg)

## 命令行运行示例

#### 1. 命令行进入示例工程所在目录，例如 QCloudCosXmlSample，

```
cd QCloudCosXmlSample
```

#### 2. 执行 gradle 任务 `installDebug`

在 Windows 下请运行:

```
gradlew installDebug
```

在 Linux 或者 Mac 下请运行：

```
./gradlew installDebug
```


## 腾讯云 Android SDK

如果您想查看腾讯云 Android SDK 的代码，可以参考 [这里](https://github.com/tencentyun/qcloud-sdk-android) 。

## License

腾讯云 Android SDK 和 示例 通过 `MIT ` License 发布。

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