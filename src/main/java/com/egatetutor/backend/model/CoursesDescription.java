package com.egatetutor.backend.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name = "courses_description", indexes = @Index(name = "course_id", columnList = "course_id", unique = true))
public class CoursesDescription implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3230408103166494796L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "exam_id", referencedColumnName = "id", nullable = false)
    private CoursesOffered examId;

    @Column(name = "course_id", nullable=false)
    private String courseId;

    @Column(name = "duration")
    private String duration;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    private String title;

    @Column(name = "syllabus")
    private String syllabus;

    @Lob
    private String description;

    @Column(name = "total_marks")
    private double totalMarks;

    @Column(name = "total_question")
    private double totalQuestion;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CoursesOffered getExamId() {
        return examId;
    }

    public void setExamId(CoursesOffered examId) {
        this.examId = examId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(double totalQuestion) {
        this.totalQuestion = totalQuestion;
    }
}
