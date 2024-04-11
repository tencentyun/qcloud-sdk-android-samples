var bodyParser = require('body-parser');
var STS = require('../index');
var express = require('express');

// 如果您使用了腾讯云 cvm，可以设置内部域名：设置host或endpoint为sts.internal.tencentcloudapi.com

// 配置参数
var config = {
    secretId: 'AKID**', // 固定密钥
    secretKey: '**', // 固定密钥
    proxy: '',
    durationSeconds: 1800,
    // host: 'sts.tencentcloudapi.com', // 域名，非必须，默认为 sts.tencentcloudapi.com
    endpoint: 'sts.tencentcloudapi.com', // 域名，非必须，与host二选一，默认为 sts.tencentcloudapi.com

    // 放行判断相关参数
    bucket: 'test-1250000000',
    region: 'ap-guangzhou',
    allowPrefix: 'exampleobject', // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
    allowActions: [
        // 'name/cos:*'允许所有操作，仅作为测试使用，实际请按需分配权限，权限列表请看 https://cloud.tencent.com/document/product/436/31923
        'name/cos:*',
    ],
};


// 创建临时密钥服务
var app = express();
app.use(bodyParser.json());

// 支持跨域访问
app.all('*', function (req, res, next) {
    res.header('Content-Type', 'application/json');
    res.header('Access-Control-Allow-Origin', 'http://127.0.0.1:88');
    res.header('Access-Control-Allow-Headers', 'origin,accept,content-type');
    if (req.method.toUpperCase() === 'OPTIONS') {
        res.end();
    } else {
        next();
    }
});

// 临时密钥接口
app.all('/sts', function (req, res, next) {

    // TODO 这里根据自己业务需要做好放行判断

    // 获取临时密钥
    var shortBucketName = config.bucket.substr(0 , config.bucket.lastIndexOf('-'));
    var appId = config.bucket.substr(1 + config.bucket.lastIndexOf('-'));
    var policy = {
        'version': '2.0',
        'statement': [{
            'action': config.allowActions,
            'effect': 'allow',
            'principal': {'qcs': ['*']},
            'resource': [
                '*'
            ],
            // condition生效条件，关于 condition 的详细设置规则和COS支持的condition类型可以参考https://cloud.tencent.com/document/product/436/71306
            // 'condition': {
            //   // 比如限定ip访问
            //   'ip_equal': {
            //     'qcs:ip': '10.121.2.10/24'
            //   }
            // }
        }],
    };
    STS.getCredential({
        secretId: config.secretId,
        secretKey: config.secretKey,
        proxy: config.proxy,
        durationSeconds: config.durationSeconds,
        endpoint: config.endpoint,
        policy: policy,
    }, function (err, tempKeys) {
        var result = JSON.stringify(err || tempKeys) || '';
        res.send(result);
    });
});
app.all('*', function (req, res, next) {
    res.writeHead(404);
    res.send({code: 404, codeDesc: 'PageNotFound', message: '404 page not found'});
});

// 启动签名服务
app.listen(3000);
console.log('app is listening at http://127.0.0.1:3000');
