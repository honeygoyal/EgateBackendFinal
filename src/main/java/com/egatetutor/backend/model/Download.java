package com.egatetutor.backend.model;

import javax.persistence.*;
@Entity
@Table(name = "download")
public class Download {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id",  nullable = false, updatable = false)
        private long id;
        @Column(name = "heading1")
        private String heading1;
        @Column(name = "heading2")
        private String heading2;
        @Column(name = "heading3")
        private String heading3;
        @Column(name = "label1")
        private String label1;
        @Column(name = "label2")
        private String label2;
        @Column(name = "label3")
        private String label3;
        @Column(name = "URL1")
        private String URL1;
        @Column(name = "URL2")
        private String URL2;
        @Column(name = "exam")
        private String exam;
        @Column(name = "topic")
        private String topic;
        @Column(name = "branch")
        private String branch;
        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public String getHeading1() {
                return heading1;
        }

        public void setHeading1(String heading1) {
                this.heading1 = heading1;
        }

        public String getHeading2() {
                return heading2;
        }

        public void setHeading2(String heading2) {
                this.heading2 = heading2;
        }

        public String getHeading3() {
                return heading3;
        }

        public void setHeading3(String heading3) {
                this.heading3 = heading3;
        }

        public String getLabel1() {
                return label1;
        }

        public void setLabel1(String label1) {
                this.label1 = label1;
        }

        public String getLabel2() {
                return label2;
        }

        public void setLabel2(String label2) {
                this.label2 = label2;
        }

        public String getLabel3() {
                return label3;
        }

        public void setLabel3(String label3) {
                this.label3 = label3;
        }

        public String getURL1() {
                return URL1;
        }

        public void setURL1(String URL1) {
                this.URL1 = URL1;
        }

        public String getURL2() {
                return URL2;
        }

        public void setURL2(String URL2) {
                this.URL2 = URL2;
        }

        public String getExam() {
                return exam;
        }

        public void setExam(String exam) {
                this.exam = exam;
        }

        public String getTopic() {
                return topic;
        }

        public void setTopic(String topic) {
                this.topic = topic;
        }

        public String getBranch() {
                return branch;
        }

        public void setBranch(String branch) {
                this.branch = branch;
        }
}
