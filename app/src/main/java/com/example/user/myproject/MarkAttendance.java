package com.example.user.myproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.CaptureActivityPortrait;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

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

public class MarkAttendance extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        conn();
    }

    @Override
    public void handleResult(Result result) {
        JSONObject json = new JSONObject();
        RadioGroup radGrp = (RadioGroup) findViewById(R.id.radGrp);
        try{
            if(radGrp.getCheckedRadioButtonId() == R.id.markAtt) {
                json.put("registrationId","");
                EditText session = (EditText)findViewById(R.id.txt_session);
                json.put("eventSession", session.getText());
            } else if(radGrp.getCheckedRadioButtonId() == R.id.markBenefit) {
                json.put("registrationId","");
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        if(radGrp.getCheckedRadioButtonId() == R.id.markAtt) {
            publishMessage(Action.combineMessage("001635",Action.asciiToHex(json.toString())));
        } else if(radGrp.getCheckedRadioButtonId() == R.id.markBenefit) {
            publishMessage(Action.combineMessage("001641",Action.asciiToHex(json.toString())));
        }

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
                Toast.makeText(getApplicationContext(), messages,Toast.LENGTH_LONG).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Toast.makeText(MarkAttendance.this, "All message received!!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void runQrCodeScanner(View view){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.initiateScan();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        RadioGroup radGrp = (RadioGroup) findViewById(R.id.radGrp);
        if (result != null){
            if(result.getContents() == null){
                Toast.makeText(getApplicationContext(),"Nothing is scanned",Toast.LENGTH_LONG).show();
            }else{
                JSONObject json = new JSONObject();
                try{
                    if(radGrp.getCheckedRadioButtonId() == R.id.markAtt) {
                        json.put("registrationId",result.getContents());
                        EditText session = (EditText)findViewById(R.id.txt_session);
                        json.put("eventSession", session.getText());
                    } else if(radGrp.getCheckedRadioButtonId() == R.id.markBenefit) {
                        json.put("registrationId",result.getContents());
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                if(radGrp.getCheckedRadioButtonId() == R.id.markAtt) {
                    publishMessage(Action.combineMessage("001635",Action.asciiToHex(json.toString())));
                } else if(radGrp.getCheckedRadioButtonId() == R.id.markBenefit) {
                    publishMessage(Action.combineMessage("001641",Action.asciiToHex(json.toString())));
                }

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
                        Toast.makeText(getApplicationContext(), messages,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Toast.makeText(MarkAttendance.this, "All message received!!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    public void conn(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), Action.mqttTest,
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
            //Toast.makeText(getApplicationContext(), "publish success !!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
