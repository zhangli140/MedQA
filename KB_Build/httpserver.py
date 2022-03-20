# -*- coding: utf-8 -*-
__auther__ = 'yxr'
__date__ = '$2017-04-21$'

from http.server import HTTPServer,BaseHTTPRequestHandler
import io,shutil,json
from flask_server import mt


class PostRequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        datas = self.rfile.read(int(self.headers['content-length']))
        data = json.loads(datas.decode('utf8'),encoding='utf8')
        res = mt.test(data[u'content'])
        with open("logs.txt","a") as f:
            f.writelines("Get request: " + datas.decode("utf8") + "\n");
        self.outputtxt(json.dumps({'content':res},ensure_ascii=False))

    def outputtxt(self, content):
        # 指定返回编码
        enc = "UTF-8"
        content = content.encode(enc)
        f = io.BytesIO()
        f.write(content)
        f.seek(0)
        self.send_response(200)
        self.send_header("Content-type", "application/json; charset=%s" % enc)
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        shutil.copyfileobj(f, self.wfile)
        with open("logs.txt","a") as f:
            f.write("Send response: " + content.decode("utf8") + "\n");



if __name__ == '__main__':
    httpd = HTTPServer(("", 8997), PostRequestHandler)
    print("Server started on 127.0.0.1,port "+str(8997)+".....")
    httpd.serve_forever()