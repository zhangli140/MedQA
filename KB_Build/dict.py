# -*- coding: utf-8 -*-
"""
Created on Mon Jun 26 15:35:47 2017

@author: xiaoyang.yxr
"""

import json

symptoms = set()
with open("疾病-症状-查体.json",encoding='utf8') as f:
    data = json.load(f)
    for key,value in data.items():
        if value is not None:
            if '查体' in value and value['查体'] is not None:
                for sym in value['查体']:
                    symptoms.add(sym)
# with open("xywy_unique_symptom.txt",encoding='gbk') as f:
#     for line in f.readlines():
#         symptoms.add(line.replace("\n",""))
# with open("symptom(icd10).txt",encoding='utf8') as f:
#     for line in f.readlines():
#         symptoms.add(line.replace("\n",""))

print(len(symptoms))

with open("dict_sign.txt","w",encoding='utf8') as f:
    for symptom in symptoms:
        f.write(symptom+' 1000 ter\n')