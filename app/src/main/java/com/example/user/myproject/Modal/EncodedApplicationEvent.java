package com.example.user.myproject.Modal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by User on 1/10/2017.
 */

public class EncodedApplicationEvent {

    private String timetableId;
    private String eventStartTime;
    private String eventEndTime;
    private String eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventBrochure;
    private String noOfParticipants;

    public String getSoftSkillPoint() {
        return softSkillPoint;
    }

    public void setSoftSkillPoint(String softSkillPoint) {
        this.softSkillPoint = softSkillPoint;
    }

    private String softSkillPoint;

    public String getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(String currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    public String getMaxGroup() {
        return maxGroup;
    }

    public void setMaxGroup(String maxGroup) {
        this.maxGroup = maxGroup;
    }

    public String getGroupMemberAvailable() {
        return groupMemberAvailable;
    }

    public void setGroupMemberAvailable(String groupMemberAvailable) {
        this.groupMemberAvailable = groupMemberAvailable;
    }

    private String currentGroup;
    private String maxGroup;
    private String groupMemberAvailable;

    private String currentParticipants;
    private String status;
    private String activityType;
    private String venueId;
    private String venueName;
    private String venueDescription;

    public String getEbTicketDueDate() {
        return ebTicketDueDate;
    }

    public void setEbTicketDueDate(String ebTicketDueDate) {
        this.ebTicketDueDate = ebTicketDueDate;
    }

    private String ebTicketDueDate;

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    private String registrationId;

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
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

    public EncodedApplicationEvent(){}

    public String getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
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

    public String getEventBrochure() {
        return eventBrochure;
    }

    public void setEventBrochure(String eventBrochure) {
        this.eventBrochure = eventBrochure;
    }

    public String getNoOfParticipants() {
        return noOfParticipants;
    }

    public void setNoOfParticipants(String noOfParticipants) {
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



    public ApplicationEvent getApplicationEvent(){
        ApplicationEvent e = new ApplicationEvent();
        try {
            e.setTimetableId(Integer.parseInt(timetableId));
        }catch(Exception ex){
            e.setTimetableId(0);
        }
        e.setEventTitle(eventTitle);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = sdf.parse(eventStartTime);
        } catch (ParseException ex) {
            //Toast.makeText(getApplicationContext(), "Date error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(d.getTime());
        e.setStartTIme(gc);

        Date d2 = new Date();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
            d2 = sdf2.parse(eventEndTime);
        } catch (ParseException ex) {
            String ss = "";
            //Toast.makeText(getApplicationContext(), "Date error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        GregorianCalendar gc2 = new GregorianCalendar();
        gc2.setTimeInMillis(d2.getTime());
        e.setEndTime(gc2);
        //e.setActivityType(activityType);
        //e.setVenueName(venueName);
        return e;
    }


    public float getLat(){
        if(venueDescription == null)
            return 0;

        String[] temp = venueDescription.split(",");
        if(temp.length > 0){
            return Float.parseFloat(temp[0]+"f");
        }else{
            return 0;
        }


    }

    public float getLong(){
        if(venueDescription == null ){
            return 0;
        }
        String[] temp = venueDescription.split(",");
        if(temp.length > 0){
            return Float.parseFloat(temp[1]+"f");
        }else{
            return 0;
        }
    }


}
