import math
import helper
import numpy as np
import tensorflow as tf
from tensorflow.contrib import rnn

class BILSTM_CRF(object):
    
    def __init__(self, num_chars, num_classes, num_steps=200, num_epochs=100, embedding_matrix=None, is_training=True, is_crf=True, weight=False, exponential_decay=False, early_stopping=False, early_stopping_round=10):
        # Parameter
        self.max_f1 = 0
        self.init_learning_rate = tf.constant(0.02)
        self.dropout_rate = 0.5
        self.batch_size = 16
        self.num_layers = 1   
        self.emb_dim = 512
        self.hidden_dim = num_steps
        self.num_epochs = num_epochs
        self.num_steps = num_steps
        self.num_chars = num_chars
        self.num_classes = num_classes
        self.global_step = tf.Variable(0,trainable=False)
        self.early_stopping = early_stopping
        self.early_stopping_round = early_stopping_round
        
        # placeholder of x, y and weight
        self.inputs = tf.placeholder(tf.int32, [None, self.num_steps])
        self.targets = tf.placeholder(tf.int32, [None, self.num_steps])
        self.targets_weight = tf.placeholder(tf.float32, [None, self.num_steps])
        self.targets_transition = tf.placeholder(tf.int32, [None])
        
        # char embedding
        if embedding_matrix != None:
            self.embedding = tf.Variable(embedding_matrix, trainable=False, name="emb", dtype=tf.float32)
        else:
            self.embedding = tf.get_variable("emb", [self.num_chars, self.emb_dim])
        self.inputs_emb = tf.nn.embedding_lookup(self.embedding, self.inputs)
        self.inputs_emb = tf.transpose(self.inputs_emb, [1, 0, 2])
        self.inputs_emb = tf.reshape(self.inputs_emb, [-1, self.emb_dim])
        self.inputs_emb = tf.split(self.inputs_emb, self.num_steps, 0)

        # lstm cell
        lstm_cell_fw = tf.nn.rnn_cell.BasicLSTMCell(self.hidden_dim)
        lstm_cell_bw = tf.nn.rnn_cell.BasicLSTMCell(self.hidden_dim)

        # dropout
        if is_training:
            lstm_cell_fw = tf.nn.rnn_cell.DropoutWrapper(lstm_cell_fw, output_keep_prob=(1 - self.dropout_rate))
            lstm_cell_bw = tf.nn.rnn_cell.DropoutWrapper(lstm_cell_bw, output_keep_prob=(1 - self.dropout_rate))

        lstm_cell_fw = tf.nn.rnn_cell.MultiRNNCell([lstm_cell_fw] * self.num_layers)
        lstm_cell_bw = tf.nn.rnn_cell.MultiRNNCell([lstm_cell_bw] * self.num_layers)

        # get the length of each sample
        self.length = tf.reduce_sum(tf.sign(self.inputs), reduction_indices=1)
        self.length = tf.cast(self.length, tf.int32)  
        
        # forward and backward
        self.outputs, _, _ = rnn.static_bidirectional_rnn(
            lstm_cell_fw, 
            lstm_cell_bw,
            self.inputs_emb, 
            dtype=tf.float32,
            sequence_length=self.length
        )
        
        # softmax
        self.outputs = tf.reshape(tf.concat(self.outputs, 1), [-1, self.hidden_dim * 2])
        self.softmax_w = tf.get_variable("softmax_w", [self.hidden_dim * 2, self.num_classes])
        self.softmax_b = tf.get_variable("softmax_b", [self.num_classes])
        self.logits = tf.matmul(self.outputs, self.softmax_w) + self.softmax_b

        if not is_crf:
            pass
        else:
            self.tags_scores = tf.reshape(self.logits, [self.batch_size, self.num_steps, self.num_classes])
            self.transitions = tf.get_variable("transitions", [self.num_classes + 1, self.num_classes + 1])
            
            dummy_val = -1000
            class_pad = tf.Variable(dummy_val * np.ones((self.batch_size, self.num_steps, 1)), dtype=tf.float32)
            self.observations = tf.concat([self.tags_scores, class_pad], 2)

            begin_vec = tf.Variable(np.array([[dummy_val] * self.num_classes + [0] for _ in range(self.batch_size)]), trainable=False, dtype=tf.float32)
            end_vec = tf.Variable(np.array([[0] + [dummy_val] * self.num_classes for _ in range(self.batch_size)]), trainable=False, dtype=tf.float32) 
            begin_vec = tf.reshape(begin_vec, [self.batch_size, 1, self.num_classes + 1])
            end_vec = tf.reshape(end_vec, [self.batch_size, 1, self.num_classes + 1])

            self.observations = tf.concat([begin_vec, self.observations, end_vec], 1)

            self.mask = tf.cast(tf.reshape(tf.sign(self.targets),[self.batch_size * self.num_steps]), tf.float32)
            
            # point score
            self.point_score = tf.gather(tf.reshape(self.tags_scores, [-1]), tf.range(0, self.batch_size * self.num_steps) * self.num_classes + tf.reshape(self.targets,[self.batch_size * self.num_steps]))
            self.point_score *= self.mask
            
            # transition score
            self.trans_score = tf.gather(tf.reshape(self.transitions, [-1]), self.targets_transition)
            
            # real score
            self.target_path_score = tf.reduce_sum(self.point_score) + tf.reduce_sum(self.trans_score)
            
            # all path score
            self.total_path_score, self.max_scores, self.max_scores_pre  = self.forward(self.observations, self.transitions, self.length)
            
            # loss
            self.loss = - (self.target_path_score - self.total_path_score)
        
        # summary
        self.train_summary = tf.summary.scalar("loss", self.loss)
        self.val_summary = tf.summary.scalar("loss", self.loss)

        if exponential_decay:
            self.learning_rate = tf.train.exponential_decay(self.init_learning_rate,
                                                   global_step=self.global_step,
                                                   decay_steps=100, decay_rate=0.97)
        else:
            self.learning_rate = self.init_learning_rate
        
        self.optimizer = tf.train.AdamOptimizer(learning_rate=self.learning_rate).minimize(self.loss)

        self.add_global = self.global_step.assign_add(1)

    def logsumexp(self, x, axis=None):
        x_max = tf.reduce_max(x, reduction_indices=axis, keep_dims=True)
        x_max_ = tf.reduce_max(x, reduction_indices=axis)
        return x_max_ + tf.log(tf.reduce_sum(tf.exp(x - x_max), reduction_indices=axis))

    def forward(self, observations, transitions, length, is_viterbi=True, return_best_seq=True):
        length = tf.reshape(length, [self.batch_size])
        transitions = tf.reshape(tf.concat([transitions] * self.batch_size, 0), [self.batch_size, self.num_classes + 1, self.num_classes + 1])
        observations = tf.reshape(observations, [self.batch_size, self.num_steps + 2, self.num_classes + 1, 1])
        observations = tf.transpose(observations, [1, 0, 2, 3])
        previous = observations[0, :, :, :]
        max_scores = []
        max_scores_pre = []
        alphas = [previous]
        for t in range(1, self.num_steps + 2):
            previous = tf.reshape(previous, [self.batch_size, self.num_classes + 1, 1])
            current = tf.reshape(observations[t, :, :, :], [self.batch_size, 1, self.num_classes + 1])
            alpha_t = previous + current + transitions
            if is_viterbi:
                max_scores.append(tf.reduce_max(alpha_t, reduction_indices=1))
                max_scores_pre.append(tf.argmax(alpha_t, dimension=1))
            alpha_t = tf.reshape(self.logsumexp(alpha_t, axis=1), [self.batch_size, self.num_classes + 1, 1])
            alphas.append(alpha_t)
            previous = alpha_t           
            
        alphas = tf.reshape(tf.concat(alphas, 0), [self.num_steps + 2, self.batch_size, self.num_classes + 1, 1])
        alphas = tf.transpose(alphas, [1, 0, 2, 3])
        alphas = tf.reshape(alphas, [self.batch_size * (self.num_steps + 2), self.num_classes + 1, 1])

        last_alphas = tf.gather(alphas, tf.range(0, self.batch_size) * (self.num_steps + 2) + length)
        last_alphas = tf.reshape(last_alphas, [self.batch_size, self.num_classes + 1, 1])

        max_scores = tf.reshape(tf.concat(max_scores, 0), (self.num_steps + 1, self.batch_size, self.num_classes + 1))
        max_scores_pre = tf.reshape(tf.concat(max_scores_pre, 0), (self.num_steps + 1, self.batch_size, self.num_classes + 1))
        max_scores = tf.transpose(max_scores, [1, 0, 2])
        max_scores_pre = tf.transpose(max_scores_pre, [1, 0, 2])

        return tf.reduce_sum(self.logsumexp(last_alphas, axis=1)), max_scores, max_scores_pre        

    def train(self, sess, save_file, X_train, y_train, X_val, y_val):
        saver = tf.train.Saver()

        char2id, id2char = helper.loadMap("char2id")
        label2id, id2label = helper.loadMap("label2id")

        valid = [label2id[i] if i.startswith('B') or i.startswith('E') or i.startswith('S') else None for i in label2id]
        valid = [i for i in valid if i is not None]

        merged = tf.summary.merge_all()
        summary_writer_train = tf.summary.FileWriter('loss_log/train_loss', sess.graph)  
        summary_writer_val = tf.summary.FileWriter('loss_log/val_loss', sess.graph)     
        
        num_iterations = int(math.ceil(1.0 * len(X_train) / self.batch_size))

        cnt = 0
        current = 0
        min_val_loss = 99999999
        for epoch in range(self.num_epochs):
            # shuffle train in each epoch
            sh_index = np.arange(len(X_train))
            np.random.shuffle(sh_index)
            X_train = X_train[sh_index]
            y_train = y_train[sh_index]
            print("current epoch: %d" % (epoch))
            for iteration in range(num_iterations):
                # train
                X_train_batch, y_train_batch = helper.nextBatch(X_train, y_train, start_index=iteration * self.batch_size, batch_size=self.batch_size)
                y_train_weight_batch = 1 + np.array(np.in1d(y_train_batch,valid), float).reshape(y_train_batch.shape)
                # y_train_weight_batch = 1 + np.array((y_train_batch == label2id['B-DIS']) | (y_train_batch == label2id['E-DIS'])|(y_train_batch == label2id['B-SYM']) | (y_train_batch == label2id['E-SYM'])|(y_train_batch == label2id['B-TER']) | (y_train_batch == label2id['E-TER'])|(y_train_batch == label2id['B-TES']) | (y_train_batch == label2id['E-TES'])|(y_train_batch == label2id['B-TRE']) | (y_train_batch == label2id['E-TRE']), float)
                # y_train_weight_batch = 1 + np.array((y_train_batch == label2id['sym']), float)
                transition_batch = helper.getTransition(y_train_batch,self.num_classes)
                
                _, loss_train, max_scores, max_scores_pre, length, train_summary, _, learning_rate =\
                    sess.run([
                        self.optimizer, 
                        self.loss, 
                        self.max_scores, 
                        self.max_scores_pre, 
                        self.length,
                        self.train_summary,
                        self.add_global,
                        self.learning_rate
                    ], 
                    feed_dict={
                        self.targets_transition:transition_batch, 
                        self.inputs:X_train_batch, 
                        self.targets:y_train_batch, 
                        self.targets_weight:y_train_weight_batch
                    })

                predicts_train = self.viterbi(max_scores, max_scores_pre, length, predict_size=self.batch_size)
                if iteration % 10 == 0:
                    cnt += 1
                    precision_train, recall_train, f1_train = self.evaluate(X_train_batch, y_train_batch, predicts_train, id2char, id2label)
                    summary_writer_train.add_summary(train_summary, cnt)
                    print("iteration: %5d, learning_rate: %.8f, train loss: %5d, train precision: %.5f, train recall: %.5f, train f1: %.5f" % (iteration, learning_rate, loss_train, precision_train, recall_train, f1_train))
                    
                # validation
                if iteration % 100 == 0:
                    X_val_batch, y_val_batch = helper.nextRandomBatch(X_val, y_val, batch_size=self.batch_size)
                    y_val_weight_batch = 1 + np.array(np.in1d(y_val_batch,valid), float).reshape(y_val_batch.shape)
                    # y_val_weight_batch = 1 + np.array(
                    #     (y_train_batch == label2id['B-DIS']) | (y_train_batch == label2id['E-DIS']) | (
                    #     y_train_batch == label2id['B-SYM']) | (y_train_batch == label2id['E-SYM']) | (
                    #     y_train_batch == label2id['B-TER']) | (y_train_batch == label2id['E-TER']) | (
                    #     y_train_batch == label2id['B-TES']) | (y_train_batch == label2id['E-TES']) | (
                    #     y_train_batch == label2id['B-TRE']) | (y_train_batch == label2id['E-TRE']), float)
                    # y_val_weight_batch = 1 + np.array((y_val_batch == label2id['sym']), float)
                    transition_batch = helper.getTransition(y_val_batch,self.num_classes)
                    
                    loss_val, max_scores, max_scores_pre, length, val_summary =\
                        sess.run([
                            self.loss, 
                            self.max_scores, 
                            self.max_scores_pre, 
                            self.length,
                            self.val_summary
                        ], 
                        feed_dict={
                            self.targets_transition:transition_batch, 
                            self.inputs:X_val_batch, 
                            self.targets:y_val_batch, 
                            self.targets_weight:y_val_weight_batch
                        })
                    
                    predicts_val = self.viterbi(max_scores, max_scores_pre, length, predict_size=self.batch_size)
                    precision_val, recall_val, f1_val = self.evaluate(X_val_batch, y_val_batch, predicts_val, id2char, id2label)
                    summary_writer_val.add_summary(val_summary, cnt)
                    print("iteration: %5d, valid loss: %5d, valid precision: %.5f, valid recall: %.5f, valid f1: %.5f" % (iteration, loss_val, precision_val, recall_val, f1_val))

                    if loss_val < min_val_loss:
                        min_val_loss = loss_val
                        current = cnt

                    if f1_val > self.max_f1:
                        self.max_f1 = f1_val
                        save_path = saver.save(sess, save_file)
                        # current = cnt
                        print("saved the best model with f1: %.5f" % (self.max_f1))

                    if self.early_stopping:
                        if (cnt - current)/10 > self.early_stopping_round:
                            print("Early stopping.")
                            return

    def test(self, sess, X_test, X_test_str, output_path):
        char2id, id2char = helper.loadMap("char2id")
        label2id, id2label = helper.loadMap("label2id")
        num_iterations = int(math.ceil(1.0 * len(X_test) / self.batch_size))
        print("number of iteration: " + str(num_iterations))
        with open(output_path, "w", encoding='utf8') as outfile:
            for i in range(num_iterations):
                print("iteration: " + str(i + 1))
                results = []
                X_test_batch = X_test[i * self.batch_size : (i + 1) * self.batch_size]
                X_test_str_batch = X_test_str[i * self.batch_size : (i + 1) * self.batch_size]
                if i == num_iterations - 1 and len(X_test_batch) < self.batch_size:
                    X_test_batch = list(X_test_batch)
                    X_test_str_batch = list(X_test_str_batch)
                    last_size = len(X_test_batch)
                    X_test_batch += [[0 for j in range(self.num_steps)] for i in range(self.batch_size - last_size)]
                    X_test_str_batch += [['x' for j in range(self.num_steps)] for i in range(self.batch_size - last_size)]
                    X_test_batch = np.array(X_test_batch)
                    X_test_str_batch = np.array(X_test_str_batch)
                    results = self.predictBatch(sess, X_test_batch, X_test_str_batch, id2label)
                    results = results[:last_size]
                else:
                    X_test_batch = np.array(X_test_batch)
                    results = self.predictBatch(sess, X_test_batch, X_test_str_batch, id2label)

                for i in range(len(results)):
                    doc = ''.join(X_test_str_batch[i])
                    outfile.write(doc + "<@>" +" ".join(results[i]) + "\n")

    def viterbi(self, max_scores, max_scores_pre, length, predict_size=128):
        best_paths = []
        for m in range(predict_size):
            path = []
            last_max_node = np.argmax(max_scores[m][length[m]])
            # last_max_node = 0
            for t in range(1, length[m] + 1)[::-1]:
                last_max_node = max_scores_pre[m][t][last_max_node]
                path.append(last_max_node)
            path = path[::-1]
            best_paths.append(path)
        return best_paths

    def predictBatch(self, sess, X, X_str, id2label):
        results = []
        length, max_scores, max_scores_pre = sess.run([self.length, self.max_scores, self.max_scores_pre], feed_dict={self.inputs:X})
        predicts = self.viterbi(max_scores, max_scores_pre, length, self.batch_size)
        for i in range(len(predicts)):
            x = ''.join(X_str[i])
            y_pred = ''.join([id2label[val] for val in predicts[i] if val != self.num_classes and val != 0])
            entitys = helper.extractEntity(x, y_pred)
            results.append(entitys)
        return results

    def evaluate(self, X, y_true, y_pred, id2char, id2label):
        precision = -1.0
        recall = -1.0
        f1 = -1.0
        hit_num = 0
        pred_num = 0
        true_num = 0
        for i in range(len(y_true)):
            x = ''.join([str(id2char[val]) for val in X[i]])
            y = ''.join([str(id2label[val]) for val in y_true[i]])
            y_hat = ''.join([id2label[val] for val in y_pred[i]  if val != self.num_classes])
            true_labels = helper.extractEntity(x, y)
            pred_labels = helper.extractEntity(x, y_hat)
            hit_num += len(set(true_labels) & set(pred_labels))
            pred_num += len(set(pred_labels))
            true_num += len(set(true_labels))
        if pred_num != 0:
            precision = 1.0 * hit_num / pred_num
        if true_num != 0:
            recall = 1.0 * hit_num / true_num
        if precision > 0 and recall > 0:
            f1 = 2.0 * (precision * recall) / (precision + recall)
        return precision, recall, f1

    def predict(self, sess, X, X_str):
        num_iterations = int(math.ceil(1.0 * len(X) / self.batch_size))
        all_results = []
        for i in range(num_iterations):
            results = []
            X_test_batch = X[i * self.batch_size: (i + 1) * self.batch_size]
            X_test_str_batch = X_str[i * self.batch_size: (i + 1) * self.batch_size]
            if i == num_iterations - 1 and len(X_test_batch) < self.batch_size:
                X_test_batch = list(X_test_batch)
                X_test_str_batch = list(X_test_str_batch)
                last_size = len(X_test_batch)
                X_test_batch += [[0 for j in range(self.num_steps)] for i in range(self.batch_size - last_size)]
                X_test_str_batch += [['x' for j in range(self.num_steps)] for i in
                                     range(self.batch_size - last_size)]
                X_test_batch = np.array(X_test_batch)
                X_test_str_batch = np.array(X_test_str_batch)
                length, max_scores, max_scores_pre = sess.run([self.length, self.max_scores, self.max_scores_pre],
                                                              feed_dict={self.inputs: X_test_batch})
                predicts = self.viterbi(max_scores, max_scores_pre, length, self.batch_size)
                results = predicts[:last_size]
            else:
                X_test_batch = np.array(X_test_batch)
                X_test_str_batch = np.array(X_test_str_batch)
                length, max_scores, max_scores_pre = sess.run([self.length, self.max_scores, self.max_scores_pre],
                                                              feed_dict={self.inputs: X_test_batch})
                results = self.viterbi(max_scores, max_scores_pre, length, self.batch_size)
            all_results.extend(results)
        return all_results
