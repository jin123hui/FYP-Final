package com.example.user.myproject.Modal;

/**
 * Created by User on 30/9/2017.
 */

import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 * Created by User on 29/7/2017.
 */

public class EventRegistration implements Serializable{
    private int registrationId;
    private int timetableId;
    private GregorianCalendar registerDate;
    private String studentId;
    private String status;
    private String leaderId;
    private String description;
    private String notificationStatus;
    private String waitingListStatus;

    public String getRedeemedStatus() {
        return redeemedStatus;
    }

    public void setRedeemedStatus(String redeemedStatus) {
        this.redeemedStatus = redeemedStatus;
    }

    private String redeemedStatus;


    public GregorianCalendar getEbTicketDueDate() {
        return ebTicketDueDate;
    }

    public void setEbTicketDueDate(GregorianCalendar ebTicketDueDate) {
        this.ebTicketDueDate = ebTicketDueDate;
    }

    private GregorianCalendar ebTicketDueDate;

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    private String eventTitle;


    public EventRegistration(){
        registrationId = 0;
        timetableId = 0;
        registerDate = new GregorianCalendar();
        studentId = "";
        status = "";
        leaderId = "";
        description = "";
        notificationStatus = "";
        waitingListStatus = "";

    }



    public EventRegistration(int registrationId, int timetableId, GregorianCalendar registerDate, String studentId, String status, String leaderId, String description, String notificationStatus, String waitingListStatus) {
        this.registrationId = registrationId;
        this.timetableId = timetableId;
        this.registerDate = registerDate;
        this.studentId = studentId;
        this.status = status;
        this.leaderId = leaderId;

        this.description = description;
        this.notificationStatus = notificationStatus;
        this.waitingListStatus = waitingListStatus;
    }


    public int getRegistrationId() {
        return registrationId;
    }

    public int getTimetableId() {
        return timetableId;
    }

    public GregorianCalendar getRegisterDate() {
        return registerDate;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStatus() {
        return status;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public String getDescription() {
        return description;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

    public void setRegisterDate(GregorianCalendar registerDate) {
        this.registerDate = registerDate;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public void setWaitingListStatus(String waitingListStatus) {
        this.waitingListStatus = waitingListStatus;
    }

    public String getWaitingListStatus() {
        return waitingListStatus;
    }





}