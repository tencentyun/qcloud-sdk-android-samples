# -*- coding:utf-8 -*-
from functools import reduce


class Tools(object):

    @staticmethod
    def _flat_key_values(a):
        return a[0] + '=' + a[1]

    @staticmethod
    def _link_key_values(a, b):
        return a + '&' + b

    @staticmethod
    def flat_params(key_values):

        key_values = sorted(key_values.items(), key=lambda d: d[0])
        return reduce(Tools._link_key_values, map(Tools._flat_key_values, key_values))