import urllib.request
import urllib.parse
import json

def send(string):
    test_data = dict()
    test = list()
    test.append(string)
    test_data['content'] = test
    headers = {'Content-Type': 'application/json'}
    requrl = "http://30.30.17.84:8989"
    req = urllib.request.Request(url=requrl, headers=headers, data=json.dumps(test_data).encode("utf8"))
    res_data = urllib.request.urlopen(req)
    res = res_data.read()
    return res

if __name__ == '__main__':
    with open("compare.csv","w") as o:
        with open("test(2).out",encoding='utf8') as f:
            for line in f.readlines():
                s = line.split('<@>')
                o.write(s[0]+','+s[1].replace('\n','')+','+' '.join(json.loads(send(s[0]))['content'][0]['all']).replace('\n','')+'\n')