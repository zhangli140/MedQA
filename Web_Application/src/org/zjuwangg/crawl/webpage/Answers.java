package org.zjuwangg.crawl.webpage;

/**
 * Created by wanggang on 2015/5/27.
 */
public class Answers {
        public String answers;
        public String docterInfo;

    public Answers(String answers, String docterInfo) {
        this.answers = answers;
        this.docterInfo = docterInfo;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getDocterInfo() {
        return docterInfo;
    }

    public void setDocterInfo(String docterInfo) {
        this.docterInfo = docterInfo;
    }

    @Override
    public String toString() {
        return "Answers{" +
                "answers='" + answers + '\'' +
                ", docterInfo='" + docterInfo + '\'' +
                '}';
    }
}
