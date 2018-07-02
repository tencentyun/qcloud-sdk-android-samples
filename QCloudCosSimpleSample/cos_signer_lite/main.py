# -*- coding:utf-8 -*-

from flask import Flask
from cam.config import Config
from cam.auth.cam_url import CamUrl
import urllib.request
import urllib
app = Flask(__name__)


@app.route('/sign')
def temporary_key():
    pass
    policy = Config.POLICY
    secret_id = Config.SECRET_ID
    secret_key = Config.SECRET_KEY
    duration = Config.DURATION_SECOND
    url_generator = CamUrl(policy, duration, secret_id, secret_key)
    real_url = url_generator.url()
    print(real_url)
    req = urllib.request.Request(real_url, method='GET')
    req.set_proxy(type='https', host='10.14.87.100:8080')
    req.method = 'GET'
    req.add_header('host', 'sts.api.qcloud.com')
    response = urllib.request.urlopen(req).read()
    print(response)
    return response


if __name__ == '__main__':
    app.run(host='0.0.0.0')

