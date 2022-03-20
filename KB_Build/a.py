import re


r = re.compile(r'拟“([^“”]+)”|“([^“”]+)”收|诊断为“([^“”]+)”')
# r = re.compile(r'予[以]{0,1}“([^“”]+)”|“([^“”]+)”治疗|服[用]{0,1}“([^“”]+)”')
r2 = re.compile(r'[,.\\ ，。、；：？＋＜＞《》/及等\+\*#％%;:-]')
# r2 = re.compile(r'[a-zA-Z,.\\ ，。、；：？＋/\d及等\+\*#％%;:-]')
dic = set()
with open("待标注文本.txt", encoding='utf8') as f:
    for lines in f.readlines():
        # print(line)
        line = lines.replace('\n','')
        for line in line.split('。'):
            # print(line)
            m = r.search(line.replace('\n',''))
            if m is None:
                continue
            if m.group(1) is None:
                if m.group(2) is None:
                    if m.group(3) is None:
                        continue
                    else:
                        s = m.group(3)
                else:
                    s = m.group(2)
            else:
                s = m.group(1)
            temp = re.sub(r'[\(（].*[\)）]','',s)
            for i in re.split(r2,temp):
                # i = re.sub(r'[每一-十][天日早晚周月年][一-十]?[次]?|[每一-十][次][半一-十]?[片]?', '', i)
                # i = re.sub(r'[毫微]克', '', i)
                # i = re.sub(r'^次|治疗|口服|静滴|吸入|[半每一-十]?片|药物|具体|不详', '', i)
                i = re.sub(r'^局限期|^[很极]?高危|^[0-9]+周|^[0-9]+[a-z]*$|^[A-Z]*级$|\d+$|^[IVXabcABC]+[级度期]*$', '', i)
                if i == "" or len(i) < 3:
                    continue
                print(i)
                dic.add(i)
# with open('rule_drug.txt','w',encoding='utf8') as o:
#     for d in dic:
#         o.write(d+' 1000 tre\n')
with open('rule_dise.txt','w',encoding='utf8') as o:
    for d in dic:
        o.write(d+' 1000 dis\n')
