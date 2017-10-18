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
import com.example.user.myproject.Modal.EventListView;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.Homepage;
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
import java.util.TimeZone;

public class Incoming extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView incomingList;
    private List<ApplicationEvent> incomingEvtList;
    private List<EventRegistration> regList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MqttAndroidClient client;
    private String studentId = "16war10395";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        incomingList = (ListView) findViewById(R.id.list);
        incomingEvtList = new ArrayList<>();
        regList = new ArrayList<>();

        context = this;
        conn();

        //regList.clear();
        //incomingEvtList.clear();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        //swipeRefreshLayout.setRefreshing(true);
                                        loadEvent();
                                        //swipeRefreshLayout.setRefreshing(false);
                                    }
                                }
        );
    }

    private void readEvent() {
        //conn();
        loadEvent();
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
                        //Toast.makeText(Incoming.this, "Connected!!", Toast.LENGTH_LONG).show();
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Incoming.this, "Connection fail!!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String message) {
        try {
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            //Toast.makeText(Incoming.this, "Requesting Event Data !!", Toast.LENGTH_LONG).show();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeEventMessage(){
        if (client == null ){
            Toast.makeText(Incoming.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                    arrList.add(e.getApplicationEvent());
                    EventRegistration reg = new EventRegistration();
                    reg.setRegistrationId(Integer.parseInt(e.getRegistrationId()));
                    regList.add(reg);
                }

                incomingEvtList = arrList;
                //Toast.makeText(Incoming.this, "WOW!!"+arrList.get(0).getVenueName(), Toast.LENGTH_LONG).show();



                incomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                        /*Intent intent = new Intent(view.getContext(), Ticket.class);
                        intent.putExtra("REGISTRATION", regList.get(i));
                        intent.putExtra("EVENT", incomingEvtList.get(i));
                        try {
                            client.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);*/
                    }
                });



                /*listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {
                        if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                        {
                            if(flag_loading == false)
                            {
                                flag_loading = true;
                            }
                        }
                    }
                });*/
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Incoming.this, "All event data received!!", Toast.LENGTH_LONG).show();
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

        publishMessage(Action.combineMessage("001630",Action.asciiToHex(obj.toString())));
        subscribeEventMessage();
        //swipeRefreshLayout.setRefreshing(false);



        ApplicationEvent test1 = new ApplicationEvent();
        test1.setEventId(1001);
        test1.setTimetableId(10);
        test1.setVenueName("DK Z");
        test1.setActivityType("Education");
        test1.setEventTitle("CCNA Network Talk");
        test1.setEventDescription("Haha");

        Date d1 = new Date(117, 10, 26, 7, 30, 0);
        GregorianCalendar gc1 = new GregorianCalendar();
        gc1.setTimeInMillis(d1.getTime());
        test1.setStartTIme(gc1);

        Date d2 = new Date(117, 10, 26, 10, 30, 0);
        GregorianCalendar gc2 = new GregorianCalendar();
        gc2.setTimeInMillis(d2.getTime());
        test1.setEndTime(gc2);
        incomingEvtList.add(test1);

        Date d3 = new Date(117, 10, 9, 7, 30, 0);
        GregorianCalendar gc3 = new GregorianCalendar();
        gc3.setTimeInMillis(d3.getTime());
        EventRegistration reg1 = new EventRegistration();
        reg1.setRegistrationId(3);
        regList.add(reg1);

        incomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(), Ticket.class);
                intent.putExtra("REGISTRATION", regList.get(i));
                intent.putExtra("EVENT", incomingEvtList.get(i));
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        final DetailedListAdapter adapter = new DetailedListAdapter(context, R.layout.content_incoming, incomingEvtList);
        incomingList.setAdapter(adapter);
    }


    @Override
    public void onRefresh() {
        loadEvent();
    }


}
