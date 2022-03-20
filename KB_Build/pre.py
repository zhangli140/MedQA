# -*- coding: utf-8 -*-
"""
Created on Mon Jun 26 11:18:41 2017

@author: xiaoyang.yxr
"""
import json

with open("result.json",encoding='utf8') as f:
    data = json.load(f)
    for d in data:
        try:
            print(d['inhosRecord']['subjective'])
        except:
            continue