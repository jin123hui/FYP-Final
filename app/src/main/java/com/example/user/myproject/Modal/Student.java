package com.example.user.myproject.Modal;

/**
 * Created by User on 29/9/2017.
 */

public class Student {



    private String faculty;
    private String studentId;
    private String studenetName;

    public Student() {
    }

    public Student(String studentId, String studenetName, String faculty) {
        this.studentId = studentId;
        this.studenetName = studenetName;
        this.faculty = faculty;
    }

    public Student(String studentId,String studenetName){
        this.studentId = studentId;
        this.studenetName = studenetName;
        this.faculty = "";

    }

    public Student(String studentId){
        this.studentId = studentId;
        this.studenetName = "";
        this.faculty = "";

    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudenetName() {
        return studenetName;
    }

    public void setStudenetName(String studenetName) {
        this.studenetName = studenetName;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Student && ((Student)obj).studentId.equals(this.studentId)){
            return true;
        }else {
            return super.equals(obj);
        }
    }



}
