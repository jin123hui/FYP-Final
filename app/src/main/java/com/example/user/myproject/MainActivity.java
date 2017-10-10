package com.example.user.myproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.EncodedStudent;
import com.example.user.myproject.Modal.Homepage;
import com.example.user.myproject.Modal.Student;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    MqttAndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void conn(View v){


        String clientId = MqttClient.generateClientId();
       client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://iot.eclipse.org:1883",
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected!!", Toast.LENGTH_LONG).show();
                    // We are connected
                    //Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    // Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void clickConnection(View view)
    {
        conn(view);

    }



    public void publishMessage(String message) {
        // EditText topicText = (EditText)findViewById(R.id.topic);
        // topicStr = ""
        //EditText text = (EditText)findViewById(R.id.Msg);

        //String message = text.getText().toString();
        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            Toast.makeText(MainActivity.this, "publish success l!!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void testing(View v){


        TextView view = (TextView)findViewById(R.id.textView2);

        view.setText("123");

        String ss = "";




            Student s = new Student("16war10395", "Zhan yi", "fasc");
            Student[] arr = {new Student("16war10395", "cowsi", "fasc"), new Student("16war10396", "Zhan ponss", "fasd")};
            // EncodedStudent encodedArr = (EncodedStudent) studentArr.toArray();
            EncodedStudent[] tempArr = Action.convertStudentToEncoded(arr);
           // tempArr = (EncodedStudent[])(studentArr.toArray());



            Gson g = new Gson();
            ss = g.toJson(tempArr);
            //g.toJson("Command","12345");




            GsonBuilder builder = new GsonBuilder();

            Gson resultG = new Gson();
            EncodedStudent[] tempResult = resultG.fromJson(ss,EncodedStudent[].class);
            Student[] decodeResult = {};
            decodeResult =  Action.decodeStudents(tempResult);

            Gson g2 = new Gson();
            String decodeJsonString = g2.toJson(decodeResult);

            //ncodedStudent[] result =
                    String sssss = "";



        view.setText(ss);

        publishMessage(Action.combineMessage("001609",ss));
        publishMessage(Action.combineMessage("001609",decodeJsonString));
        publishMessage(Action.combineMessage("001609",Action.asciiToHex(ss)));

    }

}
