package com.example.user.myproject.Modal;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Jin Hui on 18/10/2017.
 */

public class EncodedAttendance {

    private String attendanceId;
    private String eventSession;
    private String attendanceTime;
    private String status;
    private String registrationId;

    public EncodedAttendance() {
    }

    public EncodedAttendance(String attendanceId, String eventSession, String attendanceTime, String status, String registrationId) {
        this.attendanceId = attendanceId;
        this.eventSession = eventSession;
        this.attendanceTime = attendanceTime;
        this.status = status;
        this.registrationId = registrationId;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getEventSession() {
        return eventSession;
    }

    public void setEventSession(String eventSession) {
        this.eventSession = eventSession;
    }

    public String getAttendanceTime() {
        return attendanceTime;
    }

    public void setAttendanceTime(String attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public GregorianCalendar getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = sdf.parse(attendanceTime);
        } catch (ParseException ex) {
            //Toast.makeText(getApplicationContext(), "Date error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(d.getTime());

        return gc;
    }
}
