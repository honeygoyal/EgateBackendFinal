package com.egatetutor.backend.model;

import javax.persistence.*;
@Entity
@Table(name = "download")
public class Download {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id",  nullable = false, updatable = false)
        private long id;
        @Column(name = "label1")
        private String label1;
        @Column(name = "label2")
        private String label2;
        @Column(name = "topic")
        private String topic;
        @Column(name = "URL1")
        private String URL1;
        @Column(name = "URL2")
        private String URL2;
        @Column(name = "exam")
        private String exam;
        @Column(name = "subsection")
        private String subsection;
        @Column(name = "branch")
        private String branch;
        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
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

        public String getTopic() {
                return topic;
        }

        public void setTopic(String label3) {
                this.topic = label3;
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

        public String getSubsection() {
                return subsection;
        }

        public void setSubsection(String topic) {
                this.subsection = topic;
        }

        public String getBranch() {
                return branch;
        }

        public void setBranch(String branch) {
                this.branch = branch;
        }
}
