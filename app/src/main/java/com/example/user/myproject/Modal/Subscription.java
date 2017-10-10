package com.example.user.myproject.Modal;

/**
 * Created by User on 30/9/2017.
 */

public class Subscription {

    private String studentId;
    private String eventType;

    public Subscription(String studentId, String eventType) {
        this.studentId = studentId;
        this.eventType = eventType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
