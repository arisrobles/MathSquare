package com.happym.mathsquare.Model;

public class User {
    private String name;
    private String email;
    private String accountType;
    private String section;
    private String grade;
    private String docId;
    private String additionalInfo; // For quiz score, etc.

    // Constructor for Admin and Teacher
    public User(String name, String email, String accountType, String docId) {
        this.name = name;
        this.email = email;
        this.accountType = accountType;
        this.docId = docId;
        this.section = "N/A";
        this.grade = "N/A";
        this.additionalInfo = "N/A";
    }

    // Constructor for Student
    public User(String name, String email, String accountType, String section, String grade, String additionalInfo, String docId) {
        this.name = name;
        this.email = email != null ? email : "N/A";
        this.accountType = accountType;
        this.section = section;
        this.grade = grade;
        this.additionalInfo = additionalInfo;
        this.docId = docId;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
