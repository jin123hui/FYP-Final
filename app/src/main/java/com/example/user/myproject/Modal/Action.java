package com.example.user.myproject.Modal;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by User on 29/9/2017.
 */

public class Action {


    public static String topic = "MY/TARUC/ERS/0000000001/PUB";
    public static String reserveCommand = "303030303030303030303030303030303030303030303030";
    public static String mqttServer = "iot.eclipse.org";


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


}
