package com.example.user.myproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.BasicListAdapter;
import com.example.user.myproject.Modal.EncodedEventRegistration;
import com.example.user.myproject.Modal.EventRegistration;
import com.google.zxing.WriterException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WaitingInfo extends AppCompatActivity {

    private EventRegistration reg;
    private ApplicationEvent evt;
    private ListView evtListV;
    private List<ApplicationEvent> evtList;
    private ProgressDialog pd;
    private Context context;
    private MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(WaitingInfo.this);
        pd.setMessage("Loading");
        pd.show();

        reg = new EventRegistration();
        evt = new ApplicationEvent();
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            if (!extras.isEmpty()) {
                reg = (EventRegistration) extras.getSerializable("REGISTRATION");
                evt = (ApplicationEvent) extras.getSerializable("EVENT");
            }
        } else {

        }

        Button btnReg = (Button) findViewById(R.id.register_waiting_btn);
        btnReg.setVisibility(View.INVISIBLE);

        context = this;
        conn();

        evtList = new ArrayList<>();
        evtListV = (ListView) findViewById(R.id.list);
        evtList.add(evt);
        loadEvent();
        //loadRegDetail();

        Button btnCancel = (Button)findViewById(R.id.cancel_waiting_btn);
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WaitingInfo.this);
                alert.setTitle("Cancel Waiting List");
                alert.setMessage("Confirm to cancel your reservation in waiting list?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pd = new ProgressDialog(WaitingInfo.this);
                        pd.setMessage("Loading");
                        pd.show();
                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("registrationId", reg.getRegistrationId());
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        publishMessage(Action.combineMessage("001636",Action.asciiToHex(obj.toString())));
                        if (client == null ){
                            Toast.makeText(WaitingInfo.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                                String messages = obj.getString("message");

                                Toast.makeText(WaitingInfo.this, messages, Toast.LENGTH_LONG).show();
                                pd.dismiss();
                                finish();
                            }
                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                                // Toast.makeText(Ticket.this, "All message received!!", Toast.LENGTH_LONG).show();
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

        Button btnRegister = (Button)findViewById(R.id.register_waiting_btn);
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WaitingInfo.this);
                alert.setTitle("Registration");
                alert.setMessage("Confirm to register the event in waiting list?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pd = new ProgressDialog(WaitingInfo.this);
                        pd.setMessage("Loading");
                        pd.show();
                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("registrationId", reg.getRegistrationId());
                            obj.put("timetableId", evt.getTimetableId());
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001638",Action.asciiToHex(obj.toString())));
                        if (client == null ){
                            Toast.makeText(WaitingInfo.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                                String messages = obj.getString("message");

                                if(obj.getString("success").equals("1")) {
                                    Toast.makeText(WaitingInfo.this, messages, Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    Toast.makeText(WaitingInfo.this, messages, Toast.LENGTH_LONG).show();
                                }
                                pd.dismiss();
                            }
                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                                // Toast.makeText(Ticket.this, "All message received!!", Toast.LENGTH_LONG).show();
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
    }

    private void loadEvent() {
        final BasicListAdapter adapter = new BasicListAdapter(this, R.layout.content_waiting_info, evtList);
        evtListV.setAdapter(adapter);
    }

    private String checkWaitingStatus() {

        if(reg.getWaitingListStatus()!=null) {
            if(reg.getWaitingListStatus().equals("ACTIVE")) {
                return "Pending in Waiting List";
            } else {
                return "Not in Waiting List";
            }
        } else {
            return "Not in Waiting List";
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
                    //Toast.makeText(Ticket.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);

                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("timetableId", evt.getTimetableId());
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001637",Action.asciiToHex(obj.toString())));
                        subscribeWaitMessage();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(WaitingInfo.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
            //Toast.makeText(Ticket.this, "Request Data !!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeWaitMessage(){
        if (client == null ){
            Toast.makeText(WaitingInfo.this, "Connection fail!!", Toast.LENGTH_LONG).show();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                String s = "";
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String strMessage = new String(message.getPayload());
                strMessage = Action.hexToAscii(strMessage);
                JSONObject obj = new JSONObject(strMessage);
                EncodedEventRegistration registration = new EncodedEventRegistration();

                TextView tvStatus = (TextView) findViewById(R.id.wait_status);
                Button btnReg = (Button) findViewById(R.id.register_waiting_btn);
                if (obj.getString("success").equals("1")){
                    tvStatus.setText(obj.getString("message"));
                    btnReg.setVisibility(View.VISIBLE);
                }else{
                    tvStatus.setText(obj.getString("message"));
                }

                pd.dismiss();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Ticket.this, "All registration data received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
