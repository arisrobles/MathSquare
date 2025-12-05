package com.happym.mathsquare.Model;

public class Student {
    private String name;
    private String section;
    private String grade;
    private String quizNo;
    private String score;
    
    private String docId;

    // Constructor
    public Student(String name, String section, String grade, String quizNo, String score, String docId) {
        this.name = name;
        this.section = section;
        this.grade = grade;
        this.quizNo = quizNo;
        this.score = score;
        this.docId = docId;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getQuizNo() {
        return quizNo;
    }

    public void setQuizNo(String quizNo) {
        this.quizNo = quizNo;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
    
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
