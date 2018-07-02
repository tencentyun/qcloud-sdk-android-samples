
cos_signer_lite 是一个基于 flask 服务框架的临时秘钥服务，该项目采用 python3 编写，您可以在自己的机器或者虚拟机上运行该 python 项目，为您的 COS 终端（Android/IOS）SDK 提供临时密钥服务。
> 注意：该项目只是用于示例，不可直接用于生产环境，生产环境中您可以使用 COS 服务端 SDK 的临时秘钥接口。

## 如何运行临时秘钥服务

### 配置环境

cos_signer_lite 采用 python3 编写，你首先需要安装 python3 环境，并在 python3 环境下安装 flask 模块。

```
pip3 install flask
```

### 配置密钥信息

cos_signer_lite 需要你在 ../cos_signer_lit/cam/config.py 中配置密钥信息，然后 cos_signer_lite 会在您每次请求时向 CAM 服务请求临时密钥。

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
python main.py
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
