# -*- coding: utf-8 -*-
import re
import jieba
import jieba.posseg as pseg
jieba.load_userdict("dict_dise.txt")
jieba.load_userdict("dict_drug.txt")
jieba.load_userdict("rule_dise.txt")
jieba.load_userdict("rule_drug.txt")

class Dictionary:
    def __init__(self):
        self.dic = dict()
        self.amount = 0
        self.support = 0.01
        self.confidence = 0.6

    def load_dict(self, file='test.out'):
        type = re.compile(r'\((.*)\)')

        def insert_dict(key, value):
            self.amount = self.amount + 1
            if key in self.dic:
                self.dic[key].append(value)
            else:
                self.dic[key] = [value]

        with open(file, encoding='utf8') as f:
            for line in f.readlines():
                ls = line.split('<@>')
                if len(ls) == 2:
                    for i in ls[1].split(' '):
                        i = i.replace('\n', '')
                        m = re.search(type, i)
                        if m is None:
                            continue
                        insert_dict(re.sub(type, '', i), m.group(1))

    def add_dict(self, words, types):
        for (word,type) in zip(words,types):
            if len(word) > 6:
                continue
            if word in self.dic:
                self.dic[word].append(type)
            else:
                self.dic[word] = [type]
            self.amount += 1

    def use_dict(self, key):
        if key in self.dic:
            max_tuple = max(map(lambda x: (self.dic[key].count(x), x), self.dic[key]))
            if max_tuple[0] < self.amount * self.support or max_tuple[0] < len(self.dic[key]) * self.confidence:
                return None
            else:
                return max_tuple[1]

    def cut(self, string):
        for key in self.dic:
            if self.use_dict(key) is None:
                continue
            jieba.add_word(key, 10000, self.use_dict(key))
            print(key, self.use_dict(key))
        words = pseg.cut(string)
        for w in words:
            print(w.word, w.flag)


if __name__ == '__main__':
    dic = Dictionary()
    dic.load_dict()
    print(dic.amount)
    dic.cut("乳腺癌是女性最常见的恶性肿瘤之一，据资料统计，发病率占全身各种恶性肿瘤的7-10%，在妇女仅次于子宫癌，它的发病常与遗传有关，以及40-60岁之间，绝经期前后的妇女发病率较高，仅约1-2%的乳腺患者是男性。通常发生在乳房腺上皮组织的恶性肿瘤。是一种严重影响妇女身心健康甚至危及生命的最常见的恶性肿瘤之一，乳腺癌男性罕见。 乳腺癌的病因尚未完全清楚，研究发现乳腺癌的发病存在一定的规律性，具有乳腺癌高危因素的女性容易患乳腺癌。所谓高危因素是指与乳腺癌发病有关的各种危险因素，而大多数乳腺癌患者都具有的危险因素就称为乳腺癌的高危因素。乳腺癌的早期发现、早期诊断，是提高疗效的关键。 乳腺癌的治疗方法有：手术、放疗、化疗、中医中药治疗四大成熟疗法，西医常用的治疗方式就是手术后进行放疗、化疗杀灭身体里手术无法切除癌细胞，手术、放疗均属于局部治疗。化疗是一种应用抗癌药物抑制癌细胞分裂，破坏癌细胞的治疗方法，虽然也属整体治疗，但是副作用较大，并且也不能完全控制好复发转移。 中医治疗肿瘤强调调节与平衡的原则，恢复和增强机体内部的抗病能力，从而达到阴阳平衡治疗肿瘤的目的。治疗过程中医生会兼顾病人的局部治疗和全身治疗，对早、中期乳腺癌患者争取治愈，提高生活质量，对晚期患者延长寿命。")

