import jieba
import jieba.posseg as pseg
import re
import urllib.request
import urllib.parse
from bs4 import BeautifulSoup

jieba.load_userdict('dict_dise.txt')
# jieba.load_userdict('dict_drug.txt')
# jieba.load_userdict('dict_symp.txt')

stop_words = [
    '患者',
    '伴',
    '诊断',
    '首先',
    '建议',
    '及',
    '等',
    '出院',
    '基本',
    '可能',
    '改变',
    '同前',
    '良好',
    '就诊',
    '诉',
    '来',
    '前',
    '年',
    '月',
    '天',
    '提示'
]

stop_re = re.compile('%s'%'|'.join(stop_words))

def scan_patterns(contents, n):
    patterns = set()
    for content in contents:
        index = 0
        for word,tag in pseg.cut(content):
            if tag == 'dis':
                for i in range(n):
                    for j in range(n):
                        patterns.add(content[index-i-1:index] + "<DIS>" + content[index+len(word):index+len(word)+j+1])
            index = index + len(word)
    return patterns


def filter_patterns(contents, patterns, minsup=0.2, mincon=0):
    new_patterns = []
    new_words = []
    for index,pattern in enumerate(patterns):
        pattern = pattern.replace('+','\+')
        pattern = pattern.replace('*','\*')
        # print('%s/%s' % (index,len(patterns)))
        p = re.compile(r'<[A-Z]{3}>')
        s = p.split(pattern)
        # print(s)
        results = re.findall(r'%s([^。，、：；？！”“ "\n]+)%s'%(s[0],s[1]),'。'.join(contents))
        # print(results)
        support = 0
        for result1 in results:
            for result in re.split(stop_re,result1):
                if(len(result) < 2 or len(result) > 6):
                    continue
                # print(result)
                word,tag = next(pseg.cut(result))
                if word == result and tag == 'dis':
                    support += 1
        if support/len(results) > minsup:
            new_patterns.append(pattern)
            new_words.extend(results)
    return new_patterns,new_words


# def find_new_word(contents, patterns):
#     for index,pattern in enumerate(patterns):
#         pattern = pattern.replace('+','\+')
#         pattern = pattern.replace('*','\*')


def engine_match(word, n):
    p = {"wd":word}
    response = urllib.request.urlopen("http://www.baidu.com/s?"+urllib.parse.urlencode(p))
    bs = BeautifulSoup(response.read(),"lxml")
    em_string_list = [em.string for em in bs.body.find_all('em')]
    return em_string_list.count(word) >= n

with open("待标注文本.txt", encoding='utf8') as f:
    contents = f.readlines()[:100]
    patterns = scan_patterns(contents,3)
    print(filter_patterns(contents,patterns))
# print(engine_match("于近日姐姐去世有关",10))





