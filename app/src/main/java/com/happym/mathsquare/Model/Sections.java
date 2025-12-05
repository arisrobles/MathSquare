package com.happym.mathsquare.Model;
import java.util.Objects;

public class Sections {

    private String section;
    private String grade;
    private String docId;

    // Constructor
    public Sections(String section, String grade, String docId) {
        this.section = section;
        this.grade = grade;
        this.docId = docId;
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
        return this.docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sections that = (Sections) obj;
        return section.equals(that.section) &&
               grade.equals(that.grade) &&
               docId.equals(that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, grade, docId);
    }
}
