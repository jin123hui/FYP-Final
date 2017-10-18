package com.example.user.myproject;

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
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EncodedAttendance;
import com.example.user.myproject.Modal.EncodedEventRegistration;
import com.example.user.myproject.Modal.EventRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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

public class Ticket extends AppCompatActivity {

    private EventRegistration reg;
    private ApplicationEvent evt;
    private ListView evtListV;
    private List<ApplicationEvent> evtList;
    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 300;
    public final static int HEIGHT = 300;
    private MqttAndroidClient client;
    private String studentId = "16wmu10392";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        context = this;
        conn();

        evtList = new ArrayList<>();
        evtListV = (ListView) findViewById(R.id.list);
        evtList.add(evt);
        loadEvent();
        //loadRegDetail();

        Button btnCancel = (Button)findViewById(R.id.cancel_btn);
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Ticket.this);
                alert.setTitle("Cancel Registration");
                alert.setMessage("Confirm to cancel your registration?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("registrationId", reg.getRegistrationId());
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        publishMessage(Action.combineMessage("001634",Action.asciiToHex(obj.toString())));
                        if (client == null ){
                            Toast.makeText(Ticket.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                                Toast.makeText(Ticket.this, messages, Toast.LENGTH_LONG).show();
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
    }

    private void loadEvent() {
        final BasicListAdapter adapter = new BasicListAdapter(this, R.layout.content_ticket, evtList);
        evtListV.setAdapter(adapter);
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
                            obj.put("registrationId", reg.getRegistrationId());
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001632",Action.asciiToHex(obj.toString())));
                        subscribeRegMessage();
                        //checkAttendance();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Ticket.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

    public void subscribeRegMessage(){
        if (client == null ){
            Toast.makeText(Ticket.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                if (obj.getString("success").equals("1")){
                    registration.setRegistrationId(reg.getRegistrationId()+"");
                    registration.setTimetableId(obj.getString("timetableId"));
                    registration.setWaitingListStatus(obj.getString("waitingListStatus"));
                    registration.setRedeemedStatus(obj.getString("redeemedStatus"));
                    registration.setStatus(obj.getString("status"));
                }else{
                }

                TextView tvStatus = (TextView) findViewById(R.id.reg_status);
                TextView tvRegId = (TextView) findViewById(R.id.reg_id);

                if(!registration.getStatus().isEmpty()) {
                    tvStatus.setText("Participated");
                }

                tvRegId.setText(registration.getRegistrationId());

                ImageView qrCode = (ImageView) findViewById(R.id.qrCode);
                try {
                    Bitmap bitmap = encodeAsBitmap(String.valueOf(registration.getRegistrationId()));
                    qrCode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                checkAttendance();
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

    private void checkAttendance() {

        JSONObject obj = new JSONObject();
        try{
            obj.put("registrationId", reg.getRegistrationId());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        publishMessage(Action.combineMessage("001633",Action.asciiToHex(obj.toString())));
        try {
            client.subscribe(Action.clientTopic, 1);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        subscribeAttndMessage();
    }

    public void subscribeAttndMessage(){
        if (client == null ){
            Toast.makeText(Ticket.this, "Connection fail!!", Toast.LENGTH_LONG).show();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                String s = "";
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String status = "";
                String strMessage = new String(message.getPayload());
                strMessage = Action.hexToAscii(strMessage);
                JSONObject obj = new JSONObject(strMessage);
                EncodedAttendance attnd = new EncodedAttendance();
                if (obj.getString("success").equals("1")) { // && obj.getString("attendanceId")!=null){
                    attnd.setRegistrationId(reg.getRegistrationId()+"");
                    attnd.setAttendanceId(obj.getString("attendanceId"));
                    attnd.setAttendanceTime(obj.getString("attendanceTime"));
                    attnd.setEventSession(obj.getString("eventSession"));
                    attnd.setStatus(obj.getString("status"));

                    if(attnd.getStatus().equals("Active")) {
                        status = attnd.getEventSession() + " \nTime: " + Action.getTime(attnd.getTime());
                    } else {
                        status = "Pending";
                    }
                }else{
                    status = "Pending";
                }
                TextView tvAttdStatus = (TextView) findViewById(R.id.attd_status);
                tvAttdStatus.setText(status);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
               // Toast.makeText(Ticket.this, "Attendance information received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }



}
