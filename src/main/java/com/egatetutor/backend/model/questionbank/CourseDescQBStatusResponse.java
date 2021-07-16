package com.egatetutor.backend.model.questionbank;

import javax.persistence.Column;
import java.io.Serializable;
import java.sql.Date;

public class CourseDescQBStatusResponse implements Serializable {

    @Column(name = "id", nullable=false)
    private Long id;

    @Column(name = "courseId", nullable=false)
    private String courseId;
    
    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    private String title;
    private String description;
    private double totalQuestion;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCourseId() {
        return courseId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(double totalQuestion) {
        this.totalQuestion = totalQuestion;
    }
}
