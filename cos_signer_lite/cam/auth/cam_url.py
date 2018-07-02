# -*- coding:utf-8 -*-

from cam.auth.encryptor import Encryptor
import time
from urllib import parse


class CamUrl(object):

    def __init__(self, policy, duration, secret_id, secret_key, name=''):
        #self.__policy = base64.b64encode(policy)
        self.__policy = policy
        self.__duration = str(duration)
        self.__secret_id = secret_id
        self.__name = name
        self.__method = 'GET'
        self.__path = 'sts.api.qcloud.com/v2/index.php'
        self.__scheme = 'https://'
        self.__encryptor = Encryptor(secret_key)

        self.__timestamp = str(int(time.time()))
        self.__nonce = str(int(time.time()) % 1000000)

        self.__action = 'GetFederationToken'
        self.__code_mode = 'base64'
        self.__region = ''
        self.__request_client = ''

    def url(self):

        params = {'Action': self.__action,
                  'codeMode': self.__code_mode,
                  'Nonce': self.__nonce,
                  'Region': self.__region,
                  'RequestClient': self.__request_client,
                  'SecretId': self.__secret_id,
                  'Timestamp': self.__timestamp,
                  'name': self.__name,
                  'policy': self.__policy,
                  'durationSeconds': self.__duration
                  }

        sign = self.__encryptor.encrypt(self.__method, self.__path, params)
        params['Signature'] = sign
        params = parse.urlencode(params)
        return self.__scheme + self.__path + '?' + str(params)




