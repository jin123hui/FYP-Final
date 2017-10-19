package com.example.user.myproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.DetailedListAdapter;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Waiting extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView waitingList;
    private List<ApplicationEvent> waitingEvtList;
    private List<EventRegistration> regList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MqttAndroidClient client;
    private String studentId = "16war10395";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        waitingList = (ListView) findViewById(R.id.waitinglist);
        waitingEvtList = new ArrayList<>();
        regList = new ArrayList<>();

        context = this;
        conn();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //swipeRefreshLayout.setRefreshing(true);
                                        //readEvent();
                                        loadEvent();
                                    }
                                }
        );
    }

    private void readEvent() {
        /*swipeRefreshLayout.setRefreshing(true);
        regList.clear();
        waitingEvtList.clear();
        //Test data
        Event test1 = new Event(1003, "Marketing Speech", "Haha", "Seminar", new Date(117, 10, 16, 7, 30, 0), new Date(117, 10, 16, 10, 30, 0), "DK SG6");
        Event test2 = new Event(1004, "Dance Dance Show", "Haha222", "Music", new Date(117, 10, 17, 5, 0, 0), new Date(117, 10, 17, 11, 30, 0), "Main Hall");
        waitingEvtList.add(test1);
        waitingEvtList.add(test2);
        Registration reg1 = new Registration(100203, 1001, "16WMU10392", null, new Date(117, 10, 9, 7, 30, 0), null, "ACTIVE", null, "ACTIVE");
        Registration reg2 = new Registration(100204, 1002, "16WMU10392", null, new Date(117, 10, 9, 9, 30, 0), null, "ACTIVE", null, "ACTIVE");
        regList.add(reg1);
        regList.add(reg2);*/
        loadEvent();
        //swipeRefreshLayout.setRefreshing(false);
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
                    //Toast.makeText(Incoming.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Waiting.this, "Connection fail!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String message) {
        try {
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            //Toast.makeText(Waiting.this, "Requesting Event Data !!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeEventMessage(){
        if (client == null ){
            Toast.makeText(Waiting.this, "Connection fail!!", Toast.LENGTH_LONG).show();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

                String s = "";
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String strMessage = new String(message.getPayload());
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                Gson gson = builder.create();
                String decoded = Action.hexToAscii(strMessage);
                EncodedApplicationEvent[] result = gson.fromJson(decoded,EncodedApplicationEvent[].class);

                ArrayList<EncodedApplicationEvent> arrList1 = new ArrayList<>(Arrays.asList(result));
                final ArrayList<ApplicationEvent> arrList = new ArrayList<>();

                regList.clear();
                for(EncodedApplicationEvent e : arrList1){
                    ApplicationEvent evt = new ApplicationEvent();
                    evt.setTimetableId(Integer.parseInt(e.getTimetableId()));
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        d = sdf.parse(e.getEventStartTime());
                    } catch (ParseException ex) {
                    }

                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTimeInMillis(d.getTime());
                    evt.setStartTIme(gc);

                    Date d2 = new Date();
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        //sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
                        d2 = sdf2.parse(e.getEventEndTime());
                    } catch (ParseException ex) {

                    }
                    GregorianCalendar gc2 = new GregorianCalendar();
                    gc2.setTimeInMillis(d2.getTime());
                    evt.setEndTime(gc2);

                    evt.setEventTitle(e.getEventTitle());
                    evt.setActivityType(e.getActivityType());
                    evt.setVenueName(e.getVenueName());
                    arrList.add(evt);

                    EventRegistration reg = new EventRegistration();
                    reg.setRegistrationId(Integer.parseInt(e.getRegistrationId()));
                    regList.add(reg);
                    Toast.makeText(Waiting.this, e.getEventTitle(), Toast.LENGTH_LONG).show();
                }

                waitingEvtList = arrList;

                waitingList = (ListView) findViewById(R.id.waitinglist);
                final DetailedListAdapter adapter = new DetailedListAdapter(context, R.layout.content_waiting, arrList);
                waitingList.setAdapter(adapter);

                waitingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                        Intent intent = new Intent(view.getContext(), Ticket.class);
                        intent.putExtra("REGISTRATION", regList.get(i));
                        intent.putExtra("EVENT", waitingEvtList.get(i));
                        try {
                            client.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Waiting.this, "All event data received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {

                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void loadEvent() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("studentId",studentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001631",Action.asciiToHex(obj.toString())));
        subscribeEventMessage();
        //swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        loadEvent();
    }
}
