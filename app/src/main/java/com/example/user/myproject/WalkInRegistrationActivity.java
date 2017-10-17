package com.example.user.myproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class WalkInRegistrationActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    MqttAndroidClient client;
    private int timetableId = 0;

    private final int CAMERA_REQUEST_CODE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_in_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        conn();

        createPermissions(Manifest.permission.CAMERA,CAMERA_REQUEST_CODE);


    }


    public void runQrCodeScanner(View view){


        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan");

        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();


       // scannerView = new ZXingScannerView(getApplicationContext());
        //setContentView(scannerView);
      //  scannerView.setResultHandler(this);
     //   scannerView.startCamera();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            if(result.getContents() == null){
                Toast.makeText(getApplicationContext(),"You cancelled the scanning",Toast.LENGTH_LONG).show();
            }else{




                JSONObject json = new JSONObject();
                try{
                    json.put("timetableId",result.getContents());

                }catch(Exception ex){

                    ex.printStackTrace();
                }




                publishMessage(Action.combineMessage("001614",Action.asciiToHex(json.toString())));

                if (client == null ){
                    Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
                }
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String strMessage = new String(message.getPayload());
                        strMessage = Action.hexToAscii(strMessage);
                        JSONObject obj = new JSONObject(strMessage);
                        String success = obj.getString("success");
                        String messages = obj.getString("message");

                        TextView eventTitle = (TextView)findViewById(R.id.textViewEventName);
                        TextView eventStartDate = (TextView) findViewById(R.id.textViewEventStartDate);
                        TextView eventStartTime = (TextView)findViewById(R.id.textViewEventStartTime);
                        TextView msg = (TextView)findViewById(R.id.textViewWalkInError);
                        TextView availableSeat = (TextView)findViewById(R.id.textViewAvailableSeat);
                        Button registerButton = (Button)findViewById(R.id.buttonWalkInRegistration);

                        EncodedApplicationEvent encodApp = new EncodedApplicationEvent();
                        encodApp.setTimetableId(obj.getString("timetableId"));
                        encodApp.setEventTitle(obj.getString("eventTitle"));
                        encodApp.setStartTime(obj.getString("eventStartTime"));
                        encodApp.setEndTime(obj.getString("eventEndTime"));
                        encodApp.setCurrentParticipants(obj.getString("availableSeat"));
                        encodApp.setNoOfParticipants(obj.getString("noOfParticipants"));


                        eventTitle.setText("");
                        eventStartDate.setText("");
                        eventStartTime.setText("");
                        msg.setText("");
                        availableSeat.setText("");



                        if(success.equals("1")){
                            eventTitle.setText(obj.getString("eventTitle"));
                            ApplicationEvent trueEvent = encodApp.getApplicationEvent();

                            timetableId = trueEvent.getTimetableId();
                            String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                            eventStartDate.setText(mssg);

                            eventStartTime.setText("Time:" + ApplicationEvent.displayTime(trueEvent.getStartTime())
                                    + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );

                            availableSeat.setText(encodApp.getCurrentParticipants()+" / " + encodApp.getNoOfParticipants());
                            registerButton.setVisibility(View.VISIBLE);


                        }else{
                            registerButton.setVisibility(View.GONE);
                            registerButton.setOnClickListener(new View.OnClickListener(){

                                @Override
                                public void onClick(View view) {

                                    AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                                    alert.setTitle("Walk in Registration");
                                    alert.setMessage("Confirm perform individual registration to the event?");
                                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            JSONObject obj = new JSONObject();
                                            try{
                                                obj.put("studentId",Action.studentId);
                                                obj.put("timetableId",timetableId);

                                            }catch(Exception ex){
                                                ex.printStackTrace();
                                            }



                                            publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));



                                            if (client == null ){
                                                Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
                                            }
                                            client.setCallback(new MqttCallback() {
                                                @Override
                                                public void connectionLost(Throwable cause) {
                                                }

                                                @Override
                                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                                    String strMessage = new String(message.getPayload());
                                                    strMessage = Action.hexToAscii(strMessage);
                                                    JSONObject obj = new JSONObject(strMessage);
                                                    String success = obj.getString("success");
                                                    String messages = obj.getString("message");


                                                    if(success.equals("1")){
                                                        Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                                        finish();

                                                    }else{
                                                        Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                                    }


                                                }

                                                @Override
                                                public void deliveryComplete(IMqttDeliveryToken token) {
                                                    // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
                                                }
                                            });


                                            dialog.dismiss();
                                        }
                                    });

                                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    alert.show();
                                }
                            });

                            msg.setText(obj.getString("message"));

                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
                    }
                });




                result.getContents();
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);

        }
    }


    @Override
    public void onPause(){
        super.onPause();
        if(scannerView != null)
            scannerView.stopCamera();

    }


    @Override
    public void handleResult(Result result) {
        Toast.makeText(getApplicationContext(),result.getText(),Toast.LENGTH_LONG).show();



        JSONObject json = new JSONObject();
        try{
            json.put("timetableId","");

        }catch(Exception ex){

            ex.printStackTrace();
        }

        publishMessage(Action.combineMessage("001614",Action.asciiToHex(json.toString())));

        if (client == null ){
            Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String strMessage = new String(message.getPayload());
                strMessage = Action.hexToAscii(strMessage);
                JSONObject obj = new JSONObject(strMessage);
                String success = obj.getString("success");
                String messages = obj.getString("message");

                TextView eventTitle = (TextView)findViewById(R.id.textViewEventName);
                TextView eventStartDate = (TextView) findViewById(R.id.textViewEventStartDate);
                TextView eventStartTime = (TextView)findViewById(R.id.textViewEventStartTime);
                TextView msg = (TextView)findViewById(R.id.textViewWalkInError);
                TextView availableSeat = (TextView)findViewById(R.id.textViewAvailableSeat);
                Button registerButton = (Button)findViewById(R.id.buttonWalkInRegistration);

                EncodedApplicationEvent encodApp = new EncodedApplicationEvent();
                encodApp.setTimetableId(obj.getString("timetableId"));
                encodApp.setEventTitle(obj.getString("eventTitle"));
                encodApp.setStartTime(obj.getString("eventStartTime"));
                encodApp.setEndTime(obj.getString("eventEndTime"));
                encodApp.setCurrentParticipants(obj.getString("availableSeat"));
                encodApp.setNoOfParticipants(obj.getString("noOfParticipants"));


                eventTitle.setText("");
                eventStartDate.setText("");
                eventStartTime.setText("");
                msg.setText("");
                availableSeat.setText("");



                if(success.equals("1")){
                    eventTitle.setText(obj.getString("eventTitle"));
                    ApplicationEvent trueEvent = encodApp.getApplicationEvent();

                    timetableId = trueEvent.getTimetableId();
                    String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                    eventStartDate.setText(mssg);

                    eventStartTime.setText("Time:" + ApplicationEvent.displayTime(trueEvent.getStartTime())
                            + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );

                    availableSeat.setText(encodApp.getCurrentParticipants()+" / " + encodApp.getNoOfParticipants());
                    registerButton.setVisibility(View.VISIBLE);


                }else{
                    registerButton.setVisibility(View.GONE);
                    registerButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                            alert.setTitle("Walk in Registration");
                            alert.setMessage("Confirm perform individual registration to the event?");
                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    JSONObject obj = new JSONObject();
                                    try{
                                        obj.put("studentId",Action.studentId);
                                        obj.put("timetableId",timetableId);

                                    }catch(Exception ex){
                                        ex.printStackTrace();
                                    }



                                    publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));



                                    if (client == null ){
                                        Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
                                    }
                                    client.setCallback(new MqttCallback() {
                                        @Override
                                        public void connectionLost(Throwable cause) {
                                        }

                                        @Override
                                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                                            String strMessage = new String(message.getPayload());
                                            strMessage = Action.hexToAscii(strMessage);
                                            JSONObject obj = new JSONObject(strMessage);
                                            String success = obj.getString("success");
                                            String messages = obj.getString("message");


                                            if(success.equals("1")){
                                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                                finish();

                                            }else{
                                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                            }


                                        }

                                        @Override
                                        public void deliveryComplete(IMqttDeliveryToken token) {
                                            // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
                                        }
                                    });


                                    dialog.dismiss();
                                }
                            });

                            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            alert.show();
                        }
                    });

                    msg.setText(obj.getString("message"));

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
            }
        });


      //  scannerView.resumeCameraPreview(this);



    }


    public void createPermissions(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
        }else{

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"camera permission granted",Toast.LENGTH_LONG).show();



                } else {
                    Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_SHORT).show();
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    finish();
                }
                return ;
            // permissions this app might request
        }
    }

    public void conn(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), Action.mqttTest,
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();

                    // Something went wrong e.g. connection timeout or firewall problems
                    // Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void publishMessage(String message) {

        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            Toast.makeText(getApplicationContext(), "publish success !!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


}
