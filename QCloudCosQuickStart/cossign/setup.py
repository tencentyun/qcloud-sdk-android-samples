# -*- coding:utf-8 -*-
import setuptools
from cossign.cosconfig import VERSION

with open("README.md", 'r') as fh:
    long_description = fh.read()

setuptools.setup(

    name='cossign',
    version=VERSION,
    author='rickenwang',
    author_email='rickenwang@tencent.com',
    description='provide temporary credential for accessing cos resource',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='https://cloud.tencent.com/document/product/436',
    packages=setuptools.find_packages(),
    classifiers=(
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ),
    entry_points={
        'console_scripts': [
            'cossign=cossign.cosmain:_main',
        ],
    },
    install_requires=[
        'flask>=1.0.2',
    ],
)