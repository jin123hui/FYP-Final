package com.example.user.myproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.DetailedListAdapter;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.SoftSkillListAdapter;
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
import java.util.StringTokenizer;

public class SoftSkill extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView evtListV;
    private List<ApplicationEvent> evtList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MqttAndroidClient client;
    private String studentId = "16wmu10392";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_skill);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        evtListV = (ListView) findViewById(R.id.softskilllist);
        evtList = new ArrayList<>();

        context = this;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        //swipeRefreshLayout.setRefreshing(true);
                                        loadSoftSkill();
                                        //swipeRefreshLayout.setRefreshing(false);
                                    }
                                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.setRefreshing(true);
        conn();
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
                    //Toast.makeText(Upcoming.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);
                        //Toast.makeText(Upcoming.this, "Connected!!", Toast.LENGTH_LONG).show();

                        loadSoftSkill();
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(SoftSkill.this, "Connection fail!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String message) {
        try {
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            //Toast.makeText(Upcoming.this, "Requesting Event Data !!", Toast.LENGTH_LONG).show();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeEventMessage(){
        if (client == null ){
            Toast.makeText(SoftSkill.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                ApplicationEvent title = new ApplicationEvent();
                title.setEventTitle("Event Title");
                title.setSoftSkillPoint("CS,CTPS,TS,LL,KK,EM,LS");
                arrList.add(title);

                int cs = 0;
                int ctps = 0;
                int ts = 0;
                int ll = 0;
                int kk = 0;
                int em = 0;
                int ls = 0;

                for(EncodedApplicationEvent e : arrList1){
                    ApplicationEvent evt = new ApplicationEvent();
                    evt.setEventTitle(e.getEventTitle());
                    evt.setSoftSkillPoint(e.getSoftSkillPoint());
                    arrList.add(evt);

                    StringTokenizer tokens = new StringTokenizer(e.getSoftSkillPoint(), ",");
                    cs += Integer.parseInt(tokens.nextToken());
                    ctps += Integer.parseInt(tokens.nextToken());
                    ts += Integer.parseInt(tokens.nextToken());
                    ll += Integer.parseInt(tokens.nextToken());
                    kk += Integer.parseInt(tokens.nextToken());
                    em += Integer.parseInt(tokens.nextToken());
                    ls += Integer.parseInt(tokens.nextToken());
                }

                ApplicationEvent total = new ApplicationEvent();
                total.setEventTitle("Total Score :");
                total.setSoftSkillPoint(cs+","+ctps+","+ts+","+ll+","+kk+","+em+","+ls);
                arrList.add(total);

                evtList = arrList;
                //Toast.makeText(Upcoming.this, "WOW!!"+arrList.get(0).getVenueName(), Toast.LENGTH_LONG).show();

                evtListV = (ListView) findViewById(R.id.softskilllist);
                evtListV.setEmptyView(findViewById(R.id.empty));
                final SoftSkillListAdapter adapter = new SoftSkillListAdapter(context, R.layout.content_soft_skill, arrList);
                evtListV.setAdapter(adapter);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Upcoming.this, "All event data received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadSoftSkill() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("studentId",studentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001642",Action.asciiToHex(obj.toString())));
        subscribeEventMessage();

    }


    @Override
    public void onRefresh() {
        loadSoftSkill();
    }
}
