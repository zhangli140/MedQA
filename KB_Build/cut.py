# -*- coding: utf-8 -*-
"""
Created on Mon Jun 26 12:06:46 2017

@author: xiaoyang.yxr
"""

import json
import jieba
import jieba.posseg as pseg
from random import shuffle
import os
import pandas as pd

# jieba.load_userdict("dict_dise.txt")
# jieba.load_userdict("dict_drug.txt")
# jieba.load_userdict("dict_trea.txt")
# jieba.load_userdict("dict_sign.txt")
# jieba.load_userdict("dict_symp.txt")
# jieba.load_userdict("rule_dise.txt")
# jieba.load_userdict("rule_drug.txt")
# res_list = []
# for f in os.listdir('data'):
#     if os.path.getsize('data/'+f) == 0:
#         continue
#     data = pd.read_csv('data/'+f,encoding='utf8',delimiter='\t',header=None, quoting =3)
#     sentence = ''.join(list(data[0]))
#     label = list(data[1])
#     words = pseg.cut(sentence)
#     for word in words:
#         if word.flag == 'dis':
#             if (len(word.word) < 2):
#                 continue
#             index = sentence.index(word.word)
#             length = len(word.word)
#             if label[index] == 'O' and label[index+length-1] == 'O':
#                 print(word.word)
#                 label[index] = 'B-DIS'
#                 label[index+length-1] = 'E-DIS'
#                 for i in range(index+1,index+length-1):
#                     label[i] = 'M-DIS'
#         elif word.flag == 'tre':
#             if(len(word.word) < 2):
#                 continue
#             index = sentence.index(word.word)
#             length = len(word.word)
#             if label[index] == 'O' and label[index+length-1] == 'O':
#                 print(word.word)
#                 label[index] = 'B-TRE'
#                 label[index+length-1] = 'E-TRE'
#                 for i in range(index+1,index+length-1):
#                     label[i] = 'M-TRE'
#         elif word.flag == 'sym':
#             if(len(word.word) < 2):
#                 continue
#             index = sentence.index(word.word)
#             length = len(word.word)
#             if label[index] == 'O' and label[index+length-1] == 'O':
#                 print(word.word)
#                 label[index] = 'B-SYM'
#                 label[index+length-1] = 'E-SYM'
#                 for i in range(index+1,index+length-1):
#                     label[i] = 'M-SYM'
#         elif word.flag == 'ter':
#             if(len(word.word) < 2):
#                 continue
#             index = sentence.index(word.word)
#             length = len(word.word)
#             if label[index] == 'O' and label[index+length-1] == 'O':
#                 print(word.word)
#                 label[index] = 'B-TER'
#                 label[index+length-1] = 'E-TER'
#                 for i in range(index+1,index+length-1):
#                     label[i] = 'M-TER'
#     temp_str = ""
#     for (a,b) in zip(data[0],label):
#         temp_str = temp_str + a + '\t' + b + '\n'
#         if a == '。':
#             temp_str = temp_str + '\n'
#     res_list.append(temp_str)
#     # print(list(data[0]))
#     # with open('data/'+f,encoding='utf8') as fp:
#         # res_list.append(fp.read())
# print(len(res_list))
res_list = list()
pos_dict = {
    "身体部位":"BOD",
    "症状和体征":"SYM",
    "疾病和诊断":"DIS",
    "检查和检验":"TES",
    "治疗":"TRE"
}
for dir in os.listdir('training dataset 1-200'):
    if dir.startswith('.'):
        continue
    for i in range(1,201):
        char = []
        tag = []
        try:
            with open(os.path.join('training dataset 1-200', dir, dir.split('-')[1] + '-' + str(i) + '.txtoriginal.txt'),
                      encoding='utf8') as f1:
                text = f1.readlines()[0].replace('\n','')
                for c in text:
                    char.append(c)
                    tag.append('O')
            with open(os.path.join('training dataset 1-200',dir,dir.split('-')[1]+'-'+str(i)+'.txt'), encoding='utf8') as f2:
                lines = f2.readlines()
                for line in lines:
                    line = line.replace('\n','')
                    pos = line.split('\t')
                    pos[1] = int(pos[1])
                    pos[2] = int(pos[2])
                    if not pos[2] == pos[1]:
                        tag[pos[1]] = 'B-'+ pos_dict[pos[3]]
                        tag[pos[2]] = 'E-'+ pos_dict[pos[3]]
                        tag[pos[1]+1:pos[2]] = ['M-'+ pos_dict[pos[3]] for i in range(pos[1]+1,pos[2])]
                    else:
                        tag[pos[1]] = 'S-' + pos_dict[pos[3]]
        except FileNotFoundError as e:
            print(e)
            continue
        temp_str = ""
        for a,b in zip(char,tag):
            temp_str = temp_str + a + '\t' +b+'\n'
        res_list.append(temp_str)
# print(res_list)
print(len(res_list))

shuffle(res_list)
n = len(res_list)
with open('train.in','w',encoding='utf8') as f:
    f.write('\n\n'.join(res_list[:int(round(0.6*n))]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[:int(round(0.6*n))]]))
    f.write('\n')
with open('validation.in','w',encoding='utf8') as f:
    f.write('\n\n'.join(res_list[int(round(0.6 * n)):int(round(0.8 * n))]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[int(round(0.6*n)):int(round(0.8*n))]]))
    f.write('\n')
with open('test.in','w',encoding='utf8') as f:
    f.write('\n\n'.join(res_list[int(round(0.8 * n)):]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[int(round(0.8*n)):]]))
    f.write('\n')