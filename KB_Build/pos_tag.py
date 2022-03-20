import urllib.request
import urllib.parse
from bs4 import BeautifulSoup
import json
from random import shuffle
import multiprocessing
import re
from helper import extractEntity

def send(string):
    url = "http://wi.hit.edu.cn/cemr/"
    data = {"source":string}
    post_data = urllib.parse.urlencode(data).encode('utf-8')
    request = urllib.request.Request(url)
    request.add_header("Content-Type","application/x-www-form-urlencoded")
    response = urllib.request.urlopen(request,post_data)
    return response.read()

def process(html):
    bs = BeautifulSoup(html,"lxml")
    bs2 = BeautifulSoup(bs.find(id="entity").string,"lxml")
    res = list()
    for p in bs2.find_all('p'):
        for r in p.contents:
            if r.name == 'span':
                if r.attrs['style'] == 'color:#ff0000;':
                    res.append(r.string[0]+'\t'+'B-DIS')
                    for rr in r.string[1:-1]:
                        res.append(rr + '\t' + 'M-DIS')
                    res.append(r.string[len(r.string)-1]+'\t'+'E-DIS')
                elif r.attrs['style'] == 'color:#0000ff;':
                    res.append(r.string[0]+'\t'+'B-SYM')
                    for rr in r.string[1:-1]:
                        res.append(rr + '\t' + 'M-SYM')
                    res.append(r.string[len(r.string)-1]+'\t'+'E-SYM')
                elif r.attrs['style'] == 'color:#00b8ff;':
                    res.append(r.string[0]+'\t'+'B-TER')
                    for rr in r.string[1:-1]:
                        res.append(rr + '\t' + 'M-TER')
                    res.append(r.string[len(r.string)-1]+'\t'+'E-TER')
                elif r.attrs['style'] == 'color:#B8860B;':
                    res.append(r.string[0]+'\t'+'B-TES')
                    for rr in r.string[1:-1]:
                        res.append(rr + '\t' + 'M-TES')
                    res.append(r.string[len(r.string)-1]+'\t'+'E-TES')
                elif r.attrs['style'] == 'color:#00ff00;':
                    res.append(r.string[0]+'\t'+'B-TRE')
                    for rr in r.string[1:-1]:
                        res.append(rr + '\t' + 'M-TRE')
                    res.append(r.string[len(r.string)-1]+'\t'+'E-TRE')
            else:
                for rr in r.string:
                    res.append(rr+'\t'+'O')
        res.append("")
    # res = dict()
    # res['diseases'] = [s.string for s in bs2.find_all(style="color:#ff0000;")]
    # res['dis_cat'] = [s.string for s in bs2.find_all(style="color:#ff00a0;")]
    # res['symptoms'] = [s.string for s in bs2.find_all(style="color:#0000ff;")]
    # res['test_result'] = [s.string for s in bs2.find_all(style="color:#00b8ff;")]
    # res['test'] = [s.string for s in bs2.find_all(style="color:#B8860B;")]
    # res['treat'] = [s.string for s in bs2.find_all(style="color:#00ff00;")]
    # print('\n'.join(res))
    return res


def find_all_index(string,substring):
    return [i for i in range(len(string)) if string[i:].startswith(substring)]


def tag(string,entity):
    tags = ['O' for s in string]
    for ent in entity['diseases']:
        for i in find_all_index(string,ent):
            j = i+len(ent)
            tags[i] = 'B-DIS'
            tags[j-1] = 'E-DIS'
            for k in range(i+1,j-1):
                tags[k] = 'M-DIS'
    for ent in entity['symptoms']:
        for i in find_all_index(string, ent):
            j = i+len(ent)
            tags[i] = 'B-SYM'
            tags[j-1] = 'E-SYM'
            for k in range(i+1,j-1):
                tags[k] = 'M-SYM'
    for ent in entity['test_result']:
        for i in find_all_index(string, ent):
            j = i+len(ent)
            tags[i] = 'B-TER'
            tags[j-1] = 'E-TER'
            for k in range(i+1,j-1):
                tags[k] = 'M-TER'
    for ent in entity['test']:
        for i in find_all_index(string, ent):
            j = i+len(ent)
            tags[i] = 'B-TES'
            tags[j-1] = 'E-TES'
            for k in range(i+1,j-1):
                tags[k] = 'M-TES'
    for ent in entity['treat']:
        for i in find_all_index(string, ent):
            j = i+len(ent)
            tags[i] = 'B-TRE'
            tags[j-1] = 'E-TRE'
            for k in range(i+1,j-1):
                tags[k] = 'M-TRE'
    return tags

def func(i,s):
    # s = s.replace("\n","")
    # with open('data/'+str(i)+'.json','w',encoding='utf8') as o:
    #     print(i)
    #     res = process(send(s))
    #     o.write('\n'.join(res))
    #     print("Finished:"+str(i))
    s = s.replace("\n","")
    print(i)
    res = send(s)
    with open('html/'+str(i)+'.html','w',encoding='utf8') as o:
        o.write(res.decode('utf8'))
    print("Finished:"+str(i))

if __name__ == "__main__":
    # with open('待标注文本.txt', encoding='utf8') as f:
    #     for i, s in enumerate(f.readlines()):
    #         func(i,s)

    pool = multiprocessing.Pool(processes=10)
    with open('待标注文本.txt',encoding='utf8') as f:
        for i,s in enumerate(f.readlines()):
            pool.apply_async(func, (i,s))  # 维持执行的进程总数为processes，当一个进程执行完毕后会添加新的进程进去

    pool.close()
    pool.join()  # 调用join之前，先调用close函数，否则会出错。执行完close后不会有新的进程加入到pool,join函数等待所有子进程结束
    print("Sub-process(es) done.")

    # a = ['患', '者', '一', '年', '前', '5', '1', '自', '行', '脱', '落', '，', '2', '个', '月', '前', '发', '现', '上', '前', '牙', '区', '萌', '出', '三', '角', '形', '牙', '冠', '，', '于', '当', '地', '医', '院', '拔', '除', '。']
    #
    # b = ['O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'E-TES', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O']
    # print(len(a))
    # print(len(b))
    # temp = []
    # for (x,y) in zip(a,b):
    #     temp.append(x)
    #     temp.append(y)
    # print(' '.join(temp))
    # print(extractEntity(''.join(a),''.join(b)))
    # r = re.compile('(B-SYM)(M-SYM)*(E-SYM)')
    # sentence = ''.join(a)
    # labels = ''.join(b)
    # m = r.search(labels)
    # while m:
    #     entity_labels = m.group()
    #     start_index = labels.find(entity_labels)
    #     entity = sentence[start_index:start_index + int(len(entity_labels)/5)]
    #     labels = list(labels)
    #     # replace the "BM*E" with "OO*O"
    #     labels[start_index: start_index + len(entity_labels)] = ['O' for i in range(int(len(entity_labels)/5))]
    #     print(entity)
    #     labels = ''.join(labels)
    #     m = r.search(labels)

    # s_list = list()
    # with open("example.json",encoding='utf8') as f:
    #     datas = json.load(f)
    #     for data in datas['data']:
    #         if data['inhosRecord'] == None or data['outhosRecord'] == None:
    #             continue
    #         if data['inhosRecord']['presentDisHis'] == "":
    #             continue
    #         s_list.append(data['inhosRecord']['presentDisHis'])
    #         if data['outhosRecord']['admitSitu'] == "   ":
    #             continue
    #         s_list.append(data['outhosRecord']['admitSitu'])
    # with open("待标注文本.txt","w",encoding='utf8') as o:
    #     for s in s_list:
    #         o.write(s.replace('\n','')+'\n')

    # with open("待标注文本.txt", "r", encoding='utf8') as f:
    #     lines = f.readlines()
    #     shuffle(lines)
    #     n = len(lines)
    #     with open("train.in","w",encoding='utf8') as o:
    #         for i, s in enumerate(lines[:int(round(0.6*n))]):
    #             entity = (process(send(s.replace("\n", ""))))
    #             for (a,b) in tag(s,entity):
    #                 o.write(a+'\t'+b+'\n')
    #             o.write('\n')
    #             print("train:"+str(i))
    #     with open("validation.in", "w", encoding='utf8') as o:
    #         for s in lines[int(round(0.6*n)):int(round(0.8*n))]:
    #             entity = (process(send(s.replace("\n", ""))))
    #             for (a, b) in tag(s, entity):
    #                 o.write(a + '\t' + b + '\n')
    #             o.write('\n')
    #             print("validation:"+str(i))
    #     with open("test.in", "w", encoding='utf8') as o:
    #         for s in lines[int(round(0.8*n)):]:
    #             entity = (process(send(s.replace("\n", ""))))
    #             for (a, b) in tag(s, entity):
    #                 o.write(a + '\t' + b + '\n')
    #             o.write('\n')
    #             print("test:"+str(i))
