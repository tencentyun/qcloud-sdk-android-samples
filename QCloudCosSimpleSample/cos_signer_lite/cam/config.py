# -*- coding:utf-8 -*-

import os
# from app.main.cam.policy.policies import Policies

basedir = os.path.abspath(os.path.dirname(__file__))


class Config(object):

    def __init__(self):
        pass

    #
    COMMON_POLICY = '''{"statement":[{"action":["name/cos:*"],"effect":"allow","resource":"*"}],"version":"2.0"}'''


    # 用户昵称，非必选
    NAME = "WANG"
    # 策略
    POLICY = COMMON_POLICY
    # 临时证书有效期
    DURATION_SECOND = 1800
    # 用户的secret id
    SECRET_ID = 'xxx'
    # 用户的secret key
    SECRET_KEY = 'xxx'

    @staticmethod
    def init_app(app):
        pass


class DevelopmentConfig(Config):
    pass


class TestingConfig(Config):
    pass


class ProductionConfig(Config):
    pass


config = {

    'development': DevelopmentConfig,
    'testing': TestingConfig,
    'production': ProductionConfig,

    'default': DevelopmentConfig
}

