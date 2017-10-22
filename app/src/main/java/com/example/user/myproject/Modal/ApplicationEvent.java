package com.example.user.myproject.Modal;

/**
 * Created by User on 30/9/2017.
 */

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by User on 29/7/2017.
 */

public class ApplicationEvent implements Serializable {
    private int timetableId;
    private GregorianCalendar startTime;
    private GregorianCalendar endTime;
    private int eventId;
    private String eventTitle;
    private String eventDescription;
    private byte[] eventBrochure;
    private int noOfParticipants;
    private String status;
    private String activityType;
    private Integer venueId;
    private String venueName;
    private String venueDescription;

    public Bitmap getEventPicture() {
        return eventPicture;
    }

    public void setEventPicture(Bitmap eventPicture) {
        this.eventPicture = eventPicture;
    }

    private Bitmap eventPicture;

    public String getSoftSkillPoint() {
        return softSkillPoint;
    }

    public void setSoftSkillPoint(String softSkillPoint) {
        this.softSkillPoint = softSkillPoint;
    }

    private String softSkillPoint;

    public GregorianCalendar getEbTicketDueDate() {
        return ebTicketDueDate;
    }

    public void setEbTicketDueDate(GregorianCalendar ebTicketDueDate) {
        this.ebTicketDueDate = ebTicketDueDate;
    }

    private GregorianCalendar ebTicketDueDate;


    public ApplicationEvent(){}

    public ApplicationEvent(GregorianCalendar startTIme, GregorianCalendar endTime, int eventId, String eventTitle) {
        this.startTime = startTIme;
        this.endTime = endTime;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
    }


    public ApplicationEvent(int timetableId, GregorianCalendar startTIme, GregorianCalendar endTime, int eventId, Integer venueId) {
        this.timetableId = timetableId;
        this.startTime = startTIme;
        this.endTime = endTime;
        this.eventId = eventId;
        this.venueId = venueId;
    }

    public int getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

    public GregorianCalendar getStartTime() {
        return startTime;
    }

    public void setStartTIme(GregorianCalendar startTIme) {
        this.startTime = startTIme;
    }

    public GregorianCalendar getEndTime() {
        return endTime;
    }

    public void setEndTime(GregorianCalendar endTime) {
        this.endTime = endTime;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public byte[] getEventBrochure() {
        return eventBrochure;
    }

    public void setEventBrochure(byte[] eventBrochure) {
        this.eventBrochure = eventBrochure;
    }

    public int getNoOfParticipants() {
        return noOfParticipants;
    }

    public void setNoOfParticipants(int noOfParticipants) {
        this.noOfParticipants = noOfParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getVenueId() {
        return venueId;
    }

    public void setVenueId(Integer venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueDescription() {
        return venueDescription;
    }

    public void setVenueDescription(String venueDescription) {
        this.venueDescription = venueDescription;
    }




    public static String displayTime(GregorianCalendar gc){
        String dateString = String.format("%02d", gc.get(Calendar.HOUR))
                + ":" + String.format("%02d", (gc.get(Calendar.MINUTE))) + " ";

        int ampm = gc.get(Calendar.AM_PM);

        if(ampm == Calendar.AM){
            dateString  += "AM";
        }else{
            dateString += "PM";

        }
        return dateString;

    }


}

