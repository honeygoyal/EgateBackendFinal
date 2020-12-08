package com.egatetutor.backend.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_info", indexes = @Index(name = "email_id", columnList = "email_id", unique = true))
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",  nullable = false)
    private long id;

    @Column(name = "name",  nullable = false)
    private String name;

    @Column(name = "email_id",  nullable = false)
    private String emailId;

    private String address;
    private String university;
    private String discipline;
    @Column(name = "mobile_number",  nullable = false)
    private String mobileNumber;
    private String qualification;

    @Column(name = "target_year",  nullable = false)
    private String targetYear;
    private String photo;
    private String signature;

    private String password;

    @Column(name = "govt_id",nullable = true)
    private String govtId;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "subscription_details",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "course_offered_id") })
    private List<CoursesOffered> coursesOffered =new ArrayList<>();

    @Column(name = "admin")
    private boolean isAdmin;

    @Column(name = "verified")
    private String isVerified;

    @Column(name = "comment")
    private String comment;

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public List<CoursesOffered> getCoursesOffered() {
        return coursesOffered;
    }

    public void setCoursesOffered(List<CoursesOffered> coursesOffered) {
        this.coursesOffered = coursesOffered;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(String targetYear) {
        this.targetYear = targetYear;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getGovtId() {
        return govtId;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String isVerified() {
        return isVerified;
    }

    public void setVerified(String verified) {
        isVerified = verified;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
