# -*- coding: utf-8 -*-
import helper
import tensorflow as tf
from BILSTM_CRF import BILSTM_CRF
import numpy as np
import re
import jieba
import jieba.posseg as pseg
jieba.load_userdict("dict_dise.txt")
jieba.load_userdict("dict_drug.txt")
jieba.load_userdict("rule_dise.txt")
jieba.load_userdict("rule_drug.txt")


class Model:
    def __init__(self):
        self.model_path = 'model'
        self.num_steps = 512
        self.embedding_matrix = None
        self.char2id, self.id2char = helper.loadMap("char2id")
        self.label2id, self.id2label = helper.loadMap("label2id")
        self.num_chars = len(self.id2char.keys())
        self.num_classes = len(self.id2label.keys())
        self.config = tf.ConfigProto(allow_soft_placement=True)
        self.sess = tf.Session(config=self.config)
        self.initializer = tf.random_uniform_initializer(-0.1, 0.1)
        with tf.variable_scope("model", reuse=None, initializer=self.initializer):
            self.model = BILSTM_CRF(num_chars=self.num_chars, num_classes=self.num_classes, num_steps=self.num_steps,
                               embedding_matrix=self.embedding_matrix, is_training=False)
        print("loading model parameter")
        saver = tf.train.Saver()
        saver.restore(self.sess, self.model_path)

    def process(self, results, X_str):
        result_html = ""
        for labels, strings in zip(results, X_str):
            result_html = result_html + "<p>"
            index = 0
            while index < len(labels):
                word = labels[index]
                string = strings[index]
                if word == self.label2id['B-DIS']:
                    i = index + 1
                    while labels[i] == self.label2id['M-DIS']:
                        i = i + 1
                    if labels[i] == self.label2id['E-DIS']:
                        result_html = result_html + "<span style=\"color:#ff0000;\">" + ''.join(strings[index:i+1]) + "</span>"
                        index = i
                    else:
                        result_html = result_html + string
                elif word == self.label2id['B-SYM']:
                    i = index + 1
                    while labels[i] == self.label2id['M-SYM']:
                        i = i + 1
                    if labels[i] == self.label2id['E-SYM']:
                        result_html = result_html + "<span style=\"color:#0000ff;\">" + ''.join(
                            strings[index:i + 1]) + "</span>"
                        index = i
                    else:
                        result_html = result_html + string
                elif word == self.label2id['B-TER']:
                    i = index + 1
                    while labels[i] == self.label2id['M-TER']:
                        i = i + 1
                    if labels[i] == self.label2id['E-TER']:
                        result_html = result_html + "<span style=\"color:#00b8ff;\">" + ''.join(
                            strings[index:i + 1]) + "</span>"
                        index = i
                    else:
                        result_html = result_html + string
                elif word == self.label2id['B-TES']:
                    i = index + 1
                    while labels[i] == self.label2id['M-TES']:
                        i = i + 1
                    if labels[i] == self.label2id['E-TES']:
                        result_html = result_html + "<span style=\"color:#B8860B;\">" + ''.join(
                            strings[index:i + 1]) + "</span>"
                        index = i
                    else:
                        result_html = result_html + string
                elif word == self.label2id['B-TRE']:
                    i = index + 1
                    while labels[i] == self.label2id['M-TRE']:
                        i = i + 1
                    if labels[i] == self.label2id['E-TRE']:
                        result_html = result_html + "<span style=\"color:#00ff00;\">" + ''.join(
                            strings[index:i + 1]) + "</span>"
                        index = i
                    else:
                        result_html = result_html + string
                else:
                    result_html = result_html + string
                index = index + 1
            result_html = result_html + "</p>"
        return result_html

    def predict(self, test_x):
        X = []
        X_str = []

        def mapFunc(x, char2id):
            if str(x) == str(np.nan):
                return -1
            elif x not in char2id:
                return char2id["<NEW>"]
            else:
                return char2id[x]

        for s in test_x:
            X.append(mapFunc(s, self.char2id))
            X_str.append(s)
            if s == '???':
                X.append(-1)
                X_str.append(-1)
        X.append(-1)
        X_str.append(-1)
        X, _ = helper.prepare(X, X, self.num_steps, True)
        X_str, _ = helper.prepare(X_str, X_str, self.num_steps, True)
        results = self.model.predict(self.sess, X, X_str)
        print(results)
        for index,sentence in enumerate(test_x.split('???')):
            words = pseg.cut(sentence)
            for word in words:
                if word.flag == 'dis':
                    if (len(word.word) < 2):
                        continue
                    ii = sentence.index(word.word)
                    length = len(word.word)
                    if results[index][ii] == self.label2id['O'] and results[index][ii + length - 1] == self.label2id['O']:
                        results[index][ii] = self.label2id['B-DIS']
                        results[index][ii + length - 1] = self.label2id['E-DIS']
                        for i in range(ii + 1, ii + length - 1):
                            results[index][i] = self.label2id['M-DIS']
                elif word.flag == 'dru' or word.flag == 'tre':
                    if (len(word.word) < 2):
                        continue
                    ii = sentence.index(word.word)
                    length = len(word.word)
                    if results[index][ii] == self.label2id['O'] and results[index][ii + length - 1] == self.label2id['O']:
                        results[index][ii] = self.label2id['B-TRE']
                        results[index][ii + length - 1] = self.label2id['E-TRE']
                        for i in range(ii + 1, ii + length - 1):
                            results[index][i] = self.label2id['M-TRE']
        return self.process(results, X_str)

    def __del__(self):
        self.sess.close()


# def load_dict():
#     dic = dict()
#     type = re.compile(r'\((.*)\)')
#
#     def insert_dict(key, value):
#         if key in dic:
#             if value == 'DIS':
#                 dic[key].append(value)
#         else:
#             dic[key] = [value]
#
#     def use_key(key):
#         if key in dic:
#             print(dic[key].count())
#
#     with open('test.out', encoding='utf8') as f:
#         for line in f.readlines():
#             ls = line.split('<@>')
#             if len(ls) == 2:
#                 for i in ls[1].split(' '):
#                     i = i.replace('\n', '')
#                     m = re.search(type, i)
#                     if m is None:
#                         continue
#                     insert_dict(re.sub(type, '', i), m.group(1))
#                     # if m.group(1) == 'DIS':
#                     #     dic['dis'].add(re.sub(type, '', i))
#                     # elif m.group(1) == 'SYM':
#                     #     dic['sym'].add(re.sub(type, '', i))
#                     # elif m.group(1) == 'TER':
#                     #     dic['ter'].add(re.sub(type, '', i))
#                     # elif m.group(1) == 'TES':
#                     #     dic['tes'].add(re.sub(type, '', i))
#                     # elif m.group(1) == 'TRE':
#                     #     dic['tre'].add(re.sub(type, '', i))
#     # print(len(dic['dis']), len(dic['sym']), len(dic['ter']), len(dic['tes']), len(dic['tre']))
#     return dic
#
#
# def use_dict(dic, string):
#     for key, value in dic.items():
#         jieba.add_word(key, 10000, value)
#     words = pseg.cut(string)
#     for w in words:
#         print(w.word,w.flag)
#
#
if __name__ == '__main__':
    char2id, id2char = helper.loadMap("4.0/char2id")
    label2id, id2label = helper.loadMap("4.0/label2id")
    results = [[7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 3, 3, 3, 3, 16, 7, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 7, 7], [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 2, 2, 6, 7, 5, 13, 11, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 6, 7, 7, 7, 7, 7, 7, 7, 5, 13, 13, 11, 7, 7, 7], [5, 13, 13, 11, 7, 7, 9, 10, 10, 4, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 9, 10, 10, 10, 10, 4, 7, 12, 15, 15, 15, 8, 7, 12, 15, 15, 15, 15, 15, 15, 15, 8, 7], [7, 7, 14, 16, 14, 16, 7, 14, 16, 7, 7, 7, 7, 7, 7, 7, 7], [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 5, 13, 13, 11, 7, 7, 7, 7, 7, 9, 10, 4, 7, 7, 7, 7, 7, 7, 7, 7, 9, 10, 10, 10, 10, 4, 7], [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 16, 14, 16, 7, 7, 9, 10, 10, 4, 7], [7, 7, 7, 7, 7, 9, 10, 10, 4, 7, 7, 7, 7], [7, 7, 7, 7, 7, 7, 7, 1, 2, 6, 7, 1, 2, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 3, 3, 16, 7], [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 14, 3, 3, 16, 7], []]
    test_x = "??????13??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????4????????????????????????????????????????????????????????????38.7???????????????????????????????????????????????????????????????WBC?????????????????????CRP59.5mg/L??????????????????IgM???????????????????????????A???B????????????????????????CT??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????2017-02-24??????CT????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"
    words = pseg.cut(test_x)
    for index, sentence in enumerate(test_x.split('???')):
        words = pseg.cut(sentence)
        for word in words:
            if word.flag == 'dis':
                if (len(word.word) < 2):
                    continue
                ii = sentence.index(word.word)
                length = len(word.word)
                if results[index][ii] == label2id['O'] and results[index][ii + length - 1] == label2id['O']:
                    print(word.word)
                    results[index][ii] = label2id['B-DIS']
                    results[index][ii + length - 1] = label2id['E-DIS']
                    for i in range(ii + 1, ii + length - 1):
                        results[index][i] = label2id['M-DIS']
            elif word.flag == 'dru' or word.flag == 'tre':
                if (len(word.word) < 2):
                    continue
                ii = sentence.index(word.word)
                length = len(word.word)
                if results[index][ii] == label2id['O'] and results[index][ii + length - 1] == label2id['O']:
                    print(word.word)
                    results[index][ii] = label2id['B-TRE']
                    results[index][ii + length - 1] = label2id['E-TRE']
                    for i in range(ii + 1, ii + length - 1):
                        results[index][i] = label2id['M-TRE']
    print(results)

