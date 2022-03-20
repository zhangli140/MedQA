# -*- coding: utf-8 -*-

import json
from flask import Flask
from flask import request
from flask import redirect
from flask import jsonify
from flask import render_template
from model import Model as model
app = Flask(__name__)

m = model()

@app.route('/index' , methods=['GET'])
def index():
    return render_template('index.html')

@app.route('/index' , methods=['POST'])
def post_index():
    print("Get request: " + request.get_data().decode('utf8'))
    data = m.predict(json.loads(request.get_data())['content'])
    res = json.dumps({"data":data})
    print("Send response: " + res)
    return res

@app.route('/pos', methods=['GET'])
def pos():
    return render_template('pos.html')


if __name__ =='__main__':
    app.run(host='0.0.0.0', debug=False, port=9007)
    # print(mt.test("我胸痛反复发热咳嗽恶心呕吐"))
