import os
from bs4 import BeautifulSoup

if __name__ == '__main__':
    file_name = os.listdir('html')
    for file in file_name:
        with open(os.path.join('html',file),'r',encoding='utf8') as f:
            if os.path.getsize(os.path.join('html',file))==0:
                continue
            bs = BeautifulSoup(f.read(),"lxml")
            bs = BeautifulSoup(bs.find(id='relation').string,"lxml")
            for p in bs.find_all('p'):
                