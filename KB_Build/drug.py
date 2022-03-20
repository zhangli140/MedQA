import urllib.request
from bs4 import BeautifulSoup
import string

def get_html(url):
    print(url)
    res = urllib.request.urlopen(url)
    return res.read()

def process(html):
    bs = BeautifulSoup(html,"lxml")
    result = [i['title'] for i in bs.find_all('a', target='_blank', class_="")]
    return result

def get_drug(url):
    result = []
    html = get_html(url)
    bs = BeautifulSoup(html, "lxml")
    result.extend(process(html))
    pages = bs.find('ul',class_="pagination pager")
    if pages != None:
        page = (len(pages.find_all('li'))-2)
        for i in range(2,page+1):
            result.extend(process(get_html(url+'&page='+str(i))))
    return result


if __name__ == '__main__':
    url_list = []
    result = []
    for i in string.ascii_uppercase:
        result.extend(get_drug('http://www.yaozui.com/yaopin?abbr='+i))
    print(len(result))
    with open('drugs.txt','w',encoding='utf8') as o:
        for r in result:
            o.write(r+'\n')

