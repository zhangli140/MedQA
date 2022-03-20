import os
import jieba
import jieba.posseg as pseg
from random import shuffle


# jieba.load_userdict('dict_dise.txt')
# jieba.load_userdict('dict_symp.txt')
# jieba.load_userdict('dict_sign.txt')

def word2char(word, tag='x'):
    res = ""
    if not (tag == 'dis' or tag == 'sym' or tag == 'tsr' or tag == 'foo' or tag == 'goo' or tag == 'dru'):
        for c in word:
            res = res + c + '\t' + 'O' + '\n'
    else:
        if len(word) > 1:
            res = res + word[0] + '\t' + 'B-' + tag + '\n'
            for c in word[1:-1]:
                res = res + c + '\t' + 'M-' + tag + '\n'
            res = res + word[-1] + '\t' + 'E-' + tag + '\n'
        elif len(word) == 1:
            res = res + word[0] + '\t' + 'S-' + tag + '\n'
    return res

sentences = set()
# path = 'label_data'
# for file in os.listdir(path):
#     if file.split('_')[1]=='motion':
#         continue
#     with open(os.path.join(path,file), encoding='utf8') as f:
#         for line in f.readlines():
#             if len(line.split('__'))==5:
#                 sentences.add(line.split('__')[2])
#                 jieba.add_word(line.split('__')[1],10000,file.split('_')[1][:3])
with open('label_content_from_dict.txt', encoding='utf8') as f:
    for line in f.readlines():
        line = line.replace('\n','')
        if len(line.split('_'))==3:
            sentences.add(line.split('_')[2])
            jieba.add_word(line.split('_')[1],10000,line.split('_')[0][:3])
res_list = list()
for sentence in sentences:
    tmp = ""
    for word,tag in pseg.cut(sentence):
        word = word.replace('\n','')
        tmp = tmp + word2char(word, tag)
    res_list.append(tmp)
shuffle(res_list)
n = len(res_list)
print(n)
with open('train3.in','w',encoding='utf8') as f:
    f.write('\n'.join(res_list[:int(round(0.6*n))]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[:int(round(0.6*n))]]))
    # f.write('\n')
with open('validation3.in','w',encoding='utf8') as f:
    f.write('\n'.join(res_list[int(round(0.6 * n)):int(round(0.8 * n))]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[int(round(0.6*n)):int(round(0.8*n))]]))
    # f.write('\n')
with open('test3.in','w',encoding='utf8') as f:
    f.write('\n'.join(res_list[int(round(0.8 * n)):]))
    # f.write('\n\n'.join(['\n'.join(s) for s in res_list[int(round(0.8*n)):]]))
    # f.write('\n')

