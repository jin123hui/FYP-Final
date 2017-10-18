package com.example.user.myproject.Modal;

import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 * Created by Jin Hui on 18/10/2017.
 */

public class Attendance implements Serializable {

    private int attendanceId;
    private String eventSession;
    private GregorianCalendar attendanceTime;
    private String status;
    private int registrationId;

    public Attendance() {
    }

    public Attendance(int attendanceId, String eventSession, GregorianCalendar attendanceTime, String status, int registrationId) {
        this.attendanceId = attendanceId;
        this.eventSession = eventSession;
        this.attendanceTime = attendanceTime;
        this.status = status;
        this.registrationId = registrationId;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getEventSession() {
        return eventSession;
    }

    public void setEventSession(String eventSession) {
        this.eventSession = eventSession;
    }

    public GregorianCalendar getAttendanceTime() {
        return attendanceTime;
    }

    public void setAttendanceTime(GregorianCalendar attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }
}
