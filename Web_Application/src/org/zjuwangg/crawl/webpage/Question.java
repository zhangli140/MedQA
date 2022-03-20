package org.zjuwangg.crawl.webpage;

/**
 * Created by wanggang on 2015/5/27.
 */
public class Question {
    public String questionInfo;
    public String questionDetail;

    public Question(String questionInfo, String questionDetail) {
        this.questionInfo = questionInfo;
        this.questionDetail = questionDetail;
    }
    public Question(){

    }

    public String getQuestionInfo() {
        return questionInfo;
    }

    public void setQuestionInfo(String questionInfo) {
        this.questionInfo = questionInfo;
    }

    public String getQuestionDetail() {
        return questionDetail;
    }

    public void setQuestionDetail(String questionDetail) {
        this.questionDetail = questionDetail;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionInfo='" + questionInfo + '\'' +
                ", questionDetail='" + questionDetail + '\'' +
                '}';
    }
}
