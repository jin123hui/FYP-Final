package com.example.user.myproject.Modal;

/**
 * Created by User on 30/9/2017.
 */

import java.io.Serializable;
import java.util.GregorianCalendar;

public class EncodedEventRegistration implements Serializable{
    private String registrationId;
    private String timetableId;
    private String registerDate;
    private String studentId;
    private String status;
    private String leaderId;
    private String description;
    private String notificationStatus;
    private String waitingListStatus;
    private String redeemedStatus;

    public String getEbTicketDueDate() {
        return ebTicketDueDate;
    }

    public void setEbTicketDueDate(String ebTicketDueDate) {
        this.ebTicketDueDate = ebTicketDueDate;
    }

    private String ebTicketDueDate;

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    private String eventTitle;

    public String getRedeemedStatus() {
        return redeemedStatus;
    }

    public void setRedeemedStatus(String redeemedStatus) {
        this.redeemedStatus = redeemedStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public EncodedEventRegistration() {
    }

    public EncodedEventRegistration(String registrationId, String timetableId, String registerDate, String studentId, String status, String leaderId, String description, String notificationStatus, String waitingListStatus) {
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

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getWaitingListStatus() {
        return waitingListStatus;
    }

    public void setWaitingListStatus(String waitingListStatus) {
        this.waitingListStatus = waitingListStatus;
    }
}