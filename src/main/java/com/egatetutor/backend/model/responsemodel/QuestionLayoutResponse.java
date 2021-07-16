package com.egatetutor.backend.model.responsemodel;

import com.egatetutor.backend.model.CoursesDescription;

import javax.persistence.*;
import java.io.Serializable;

public class QuestionLayoutResponse {
    private long id;
    private CoursesDescription courseId;
    private String section;
    private double marks;
    private double negativeMarks;
    private String questionType;
    private String question;
    private String answer;
    private String solution;
    private String questionDifficulty;
    private String videoLink;
    private int questionLabel;
    private String answerSubmitted;
    private String questionStatus;
    private String timeTaken;
    private String totalTimeTaken;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoursesDescription getCourseId() {
        return courseId;
    }

    public void setCourseId(CoursesDescription courseId) {
        this.courseId = courseId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public double getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(double negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getQuestionDifficulty() {
        return questionDifficulty;
    }

    public void setQuestionDifficulty(String questionDifficulty) {
        this.questionDifficulty = questionDifficulty;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public int getQuestionLabel() {
        return questionLabel;
    }

    public void setQuestionLabel(int questionLabel) {
        this.questionLabel = questionLabel;
    }

    public String getAnswerSubmitted() {
        return answerSubmitted;
    }

    public void setAnswerSubmitted(String answerSubmitted) {
        this.answerSubmitted = answerSubmitted;
    }

    public String getQuestionStatus() {
        return questionStatus;
    }

    public void setQuestionStatus(String questionStatus) {
        this.questionStatus = questionStatus;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public void setTotalTimeTaken(String totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }
}

