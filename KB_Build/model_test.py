# -*- coding: utf-8 -*-
import helper
import tensorflow as tf
from BILSTM_CRF import BILSTM_CRF
import numpy as np

class model_test():
    def __init__(self):
        self.model_path = 'model'
        self.num_steps = 200
        self.embedding_matrix = None
        self.char2id, self.id2char = helper.loadMap("char2id")
        self.label2id, self.id2label = helper.loadMap("label2id")
        self.num_chars = len(self.id2char.keys())
        self.num_classes = len(self.id2label.keys())
        self.config = tf.ConfigProto(allow_soft_placement=True)
        self.sess = tf.Session(config=self.config)
        self.initializer = tf.random_uniform_initializer(-0.1, 0.1)
        with tf.variable_scope("model", reuse=None, initializer=self.initializer):
            self.model = BILSTM_CRF(num_chars=self.num_chars, num_classes=self.num_classes, num_steps=self.num_steps,
                               embedding_matrix=self.embedding_matrix, is_training=False)
        print("loading model parameter")
        saver = tf.train.Saver()
        saver.restore(self.sess, self.model_path)
    def test(self,test_x):
        X = []
        X_str = []
        def mapFunc(x, char2id):
            if str(x) == str(np.nan):
                return -1
            elif x not in char2id:
                return char2id["<NEW>"]
            else:
                return char2id[x]
        for s in test_x:
            X.append(mapFunc(s,self.char2id))
            X_str.append(s)
        X.append(-1)
        X_str.append(-1)
        X,_ = helper.prepare(X,X,self.num_steps,True)
        X_str,_ = helper.prepare(X_str,X_str,self.num_steps,True)
        self.model.test(self.sess,X,X_str,"test.out")
        with open("test.out") as f:
            return f.readline().replace("\n","").split("<@>")[1].split(' ')
    def __del__(self):
        self.sess.close()


mt = model_test()