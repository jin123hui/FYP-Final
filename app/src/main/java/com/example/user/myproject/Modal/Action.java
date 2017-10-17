package com.example.user.myproject.Modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by User on 29/9/2017.
 */

public class
Action {


    public static String serverTopic = "MY/TARUC/ERS/0000000003/PUB";
    public static String clientTopic = "MY/TARUC/ERS/0000000004/PUB";
    public static String reserveCommand = "303030303030303030303030303030303030303030303030";
    public static String studentId = "16wmu10392";

    //54 character reserved
    public static String mqttServer = "iot.eclipse.org";
    //public static String mqttTest = "tcp://localhost:1883";
    public static String mqttTest = "tcp://iot.eclipse.org:1883";

    //Turn Hex to ASCll For example : 31 turn to 1
    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    //Turn ASCll to Hex For example : 1 turn to 31
    public static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();

    }

    public static String asciiToHex(int asciiInt){
        String asciiStr = String.valueOf(asciiInt);
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();

    }


    public static EncodedStudent[] convertStudentToEncoded(Student[] arr){

        ArrayList<Student> studentArr = new ArrayList<>(Arrays.asList(arr));
        ArrayList<EncodedStudent> encodedStudArr = new ArrayList<>();
        for(Student temp: studentArr){
            EncodedStudent student = new EncodedStudent();
            student.setStudentId(asciiToHex(temp.getStudentId()));
            student.setStudenetName(asciiToHex(temp.getStudenetName()));
            student.setFaculty(asciiToHex(temp.getFaculty()));
            encodedStudArr.add(student);
        }
        EncodedStudent[] result = {};
        result = encodedStudArr.toArray(result);
        return result;
    }



    public static Student[] decodeStudents(EncodedStudent[] arr){

        ArrayList<EncodedStudent> studentArr = new ArrayList<>(Arrays.asList(arr));
        ArrayList<Student> encodedStudArr = new ArrayList<>();
        for(EncodedStudent temp: studentArr){
            Student student = new Student();
            student.setStudentId(hexToAscii(temp.getStudentId()));
            student.setStudenetName(hexToAscii(temp.getStudenetName()));
            student.setFaculty(hexToAscii(temp.getFaculty()));
            encodedStudArr.add(student);
        }
        Student[] result = {};
        result = encodedStudArr.toArray(result);

        return result;


    }

    public static String combineMessage( String commandName,String jsonString){
        return commandName + reserveCommand + jsonString;

    }

    public static EncodedApplicationEvent[] encodedApplicationEvents(ApplicationEvent[] arr){
        ArrayList<ApplicationEvent> eventArr = new ArrayList<>(Arrays.asList(arr));
        ArrayList<EncodedApplicationEvent> encodedEvent = new ArrayList<>();
        for(ApplicationEvent temp: eventArr){
            EncodedApplicationEvent event = new EncodedApplicationEvent();

            event.setTimetableId(asciiToHex(temp.getTimetableId()));
            event.setStartTime(asciiToHex(getTime(temp.getStartTime())));
            event.setEndTime(asciiToHex(getTime(temp.getEndTime())));

            event.setEventId(asciiToHex(temp.getEventId()));
            event.setEventTitle(asciiToHex(temp.getEventTitle()));
            event.setEventDescription(asciiToHex(temp.getEventDescription()));
            // event brochyure empty first
            event.setNoOfParticipants(asciiToHex(temp.getNoOfParticipants()));
            event.setStatus(asciiToHex(temp.getStatus()));
            event.setActivityType(asciiToHex(temp.getActivityType()));

            encodedEvent.add(event);

        }
        EncodedApplicationEvent[] result = {};
        result = encodedEvent.toArray(result);
        return result;

    }

    public static ApplicationEvent[] decodeApplicationEvent(EncodedApplicationEvent[] arr){
            return null;

    }


    public static String displayDate(GregorianCalendar gc) {
        String dateString = String.format("%02d", gc.get(Calendar.DATE))
                + "/" + String.format("%02d", (gc.get(Calendar.MONTH) + 1)) + "/"
                + String.format("%02d", gc.get(Calendar.YEAR));
        return dateString;

    }


    public static String getTime(GregorianCalendar gc) {
        String dateString = String.format("%02d", gc.get(Calendar.DATE))
                + "/" + String.format("%02d", (gc.get(Calendar.MONTH) + 1)) + "/"
                + String.format("%02d", gc.get(Calendar.YEAR)) + "    "
                + String.format("%02d", gc.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", gc.get(Calendar.MINUTE)) + ":"
                + String.format("%02d", gc.get(Calendar.SECOND));
        return dateString;

    }


}
