# -*- coding:utf-8 -*-
import hashlib
import hmac
from cam.auth.tools import Tools
from urllib import parse
import urllib
import base64

class Encryptor(object):

    def __init__(self, secret_key):
        self.__secret_key = secret_key

    def make_digest(self, message, key):
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

    def encrypt(self, method, path, key_values):

        source = Tools.flat_params(key_values)
        source = method + path + '?' + source

        key = bytes(self.__secret_key, 'UTF-8')
        message = bytes(source, 'UTF-8')
        sign = hmac.new(key, message, hashlib.sha1).digest()
        sign = str(base64.standard_b64encode(sign), 'UTF-8')
        return sign





