package com.egatetutor.backend.model.responsemodel;

public class UserRank {
    String name;
    Long userId;
    String examName;
    double totalMarks;
    double yourMarks;
    String yourTime;
    String totalTime;
    Long rank;
    String photo;

    public UserRank(String name, Long userId, String examName, double totalMarks, double yourMarks, String yourTime, String totalTime, Long rank, String photo) {
        this.name = name;
        this.userId = userId;
        this.examName = examName;
        this.totalMarks = totalMarks;
        this.yourMarks = yourMarks;
        this.yourTime = yourTime;
        this.totalTime = totalTime;
        this.rank = rank;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getYourMarks() {
        return yourMarks;
    }

    public void setYourMarks(double yourMarks) {
        this.yourMarks = yourMarks;
    }

    public String getYourTime() {
        return yourTime;
    }

    public void setYourTime(String yourTime) {
        this.yourTime = yourTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
