# -*- coding:utf-8 -*-

import argparse
import urllib
import urllib.request
from flask import Flask
from cossign.cosconfig import Config, VERSION
import time
from urllib import parse
import base64
import hashlib
import hmac
from functools import reduce


app = Flask(__name__)


def _flat_key_values(a):
    return a[0] + '=' + a[1]


def _link_key_values(a, b):
    return a + '&' + b


def _flat_params(key_values):
    key_values = sorted(key_values.items(), key=lambda d: d[0])
    return reduce(_link_key_values, map(_flat_key_values, key_values))


def _make_digest(message, key):
    key = bytes(key, 'UTF-8')
    message = bytes(message, 'UTF-8')

    digester = hmac.new(key, message, hashlib.sha1)
    # signature1 = digester.hexdigest()
    signature1 = digester.digest()
    # print(signature1)

    # signature2 = base64.urlsafe_b64encode(bytes(signature1, 'UTF-8'))
    signature2 = base64.urlsafe_b64encode(signature1)
    # print(signature2)

    return str(signature2, 'UTF-8')


def _encrypt(method, path, key_values, secret_key):
    source = _flat_params(key_values)
    source = method + path + '?' + source

    key = bytes(secret_key, 'UTF-8')
    message = bytes(source, 'UTF-8')
    sign = hmac.new(key, message, hashlib.sha1).digest()
    sign = str(base64.standard_b64encode(sign), 'UTF-8')
    return sign


def cam_url(policy, duration, secret_id, secret_key, name=''):

    duration = str(duration)
    method = 'GET'
    path = 'sts.api.qcloud.com/v2/index.php'
    scheme = 'https://'

    timestamp = str(int(time.time()))
    nonce = str(int(time.time()) % 1000000)

    action = 'GetFederationToken'
    code_mode = 'base64'
    region = ''
    request_client = ''

    params = {'Action': action,
              'codeMode': code_mode,
              'Nonce': nonce,
              'Region': region,
              'RequestClient': request_client,
              'SecretId': secret_id,
              'Timestamp': timestamp,
              'name': name,
              'policy': policy,
              'durationSeconds': duration
              }

    sign = _encrypt(method, path, params, secret_key)
    params['Signature'] = sign
    params = parse.urlencode(params)
    return scheme + path + '?' + str(params)


def get_inputs_args():

    parser = argparse.ArgumentParser()
    parser.add_argument('--duration', type=int, default=1800, help='Valuable seconds of the temporary credential')
    parser.add_argument('--secret_id', type=str, default='', help='Secret id of your appid')
    parser.add_argument('--secret_key', type=str, default='', help='Secret key of your appid')
    parser.add_argument('--version', action='version', version=VERSION)
    parser.add_argument('--proxy', type=str, default='',
                        help='The Proxy you surf the internet, for example "https://10.13.78.100:8080"')
    parser.add_argument('--port', type=int, default=5000, help='Temporary credential service listen port')

    in_args = parser.parse_args()
    print('duration = {}, secret id = {}, secret key = {}'
          .format(in_args.duration, in_args.secret_id, in_args.secret_key))
    Config.DURATION_SECOND = in_args.duration
    Config.PORT = in_args.port
    if in_args.secret_id != '':
        Config.SECRET_ID = in_args.secret_id
    if in_args.secret_key != '':
        Config.SECRET_KEY = in_args.secret_key
    if in_args.proxy != '':
        Config.PROXY_TYPE, Config.PROXY_HOST = in_args.proxy.split('://')


@app.route('/sign')
def temporary_key():

    policy = Config.POLICY
    secret_id = Config.SECRET_ID
    secret_key = Config.SECRET_KEY
    duration = Config.DURATION_SECOND
    real_url = cam_url(policy, duration, secret_id, secret_key)
    print('real url is ' + real_url)
    req = urllib.request.Request(real_url, method='GET')

    if Config.PROXY_TYPE != '' and Config.PROXY_HOST != '':
        req.set_proxy(type=Config.PROXY_TYPE, host=Config.PROXY_HOST)

    req.method = 'GET'
    req.add_header('host', 'sts.api.qcloud.com')
    response = urllib.request.urlopen(req).read()
    print(response)
    return response


def _main():
    get_inputs_args()
    app.run(host='0.0.0.0', port=Config.PORT)


if __name__ == '__main__':
    _main()

