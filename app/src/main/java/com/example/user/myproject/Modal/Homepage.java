package com.example.user.myproject.Modal;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.DetailEventActivity;
import com.example.user.myproject.RedeemBenefit;
import com.example.user.myproject.Upcoming;
import com.example.user.myproject.MarkAttendance;
import com.example.user.myproject.PastJoined;
import com.example.user.myproject.R;
import com.example.user.myproject.Waiting;
import com.example.user.myproject.WalkInRegistrationActivity;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class Homepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener {

    ListView lstView;
    ArrayList<ApplicationEvent> eventList = new ArrayList<>();
    MqttAndroidClient client;
    Context context;

    AlertDialog dialog;
    Dialog dialog2;

    String studentId = "16wmu10392";

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        context = this;
        conn();
        //setSubscription(Action.topic);


            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //swipeRefreshLayout.setRefreshing(true);
                                            // readQuestion();
                                            //readEvent();
                                        }
                                    }

            );

        String ss ="";


        Spinner spinner = (Spinner)findViewById(R.id.eventSpinner);

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eventCategory, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


      //  loadAllTestingEvent();
       //loadAllEvent();




    }

    public void buildSubscriptionDialog(String[] category){
        AlertDialog.Builder mBuilder  = new AlertDialog.Builder(Homepage.this);
        View mView = getLayoutInflater().inflate(R.layout.subscription_layout,null);

        //String[] category = {"Sports","Education","chk1"};

       // Gson gson = new Gson();
       // String jsonMessage = gson.toJson(category,String[].class);
        ArrayList<String> subscriptionList = new ArrayList<String>(Arrays.asList(category));



        CheckBox chkBoxSports = (CheckBox)mView.findViewById(R.id.checkBoxSports);
        CheckBox chkBoxEducation = (CheckBox)mView.findViewById(R.id.checkBoxEducation);
        CheckBox chkBox1 = (CheckBox)mView.findViewById(R.id.checkBox3);
        CheckBox chkBox2 = (CheckBox)mView.findViewById(R.id.checkBox4);
        CheckBox chkBox3 = (CheckBox)mView.findViewById(R.id.checkBox5);
        final ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();
        checkBoxList.add(chkBoxSports);
        checkBoxList.add(chkBoxEducation);
        checkBoxList.add(chkBox1);
        checkBoxList.add(chkBox2);
        checkBoxList.add(chkBox3);

        for (CheckBox temp : checkBoxList){
            for (String msg: subscriptionList){
                if(temp.getText().equals(msg)){
                    temp.setChecked(true);
                }
            }
        }





        Button btnSubscribe = (Button) mView.findViewById(R.id.btnSubscribe);
        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> result = new ArrayList<String>();
                for(CheckBox temp:checkBoxList){
                    if(temp.isChecked()){
                        result.add(temp.getText().toString());
                    }

                }
                String[] parseString = {};
                parseString = result.toArray(parseString);


                JSONObject obj = new JSONObject();
                try {
                    obj.put("studentId", studentId);
                    Gson convertString = new Gson();
                    String subscriptionListJson = convertString.toJson(parseString,String[].class);
                    obj.put("subscription",subscriptionListJson);
                    String testMessage = obj.toString();
                    publishMessage(Action.combineMessage("001611",Action.asciiToHex(obj.toString())));
                    setSubscription(Action.clientTopic);

                    if (client == null ){
                        Toast.makeText(Homepage.this, "Update subscription fail", Toast.LENGTH_LONG).show();
                    }
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            String strMessage = new String(message.getPayload());
                            strMessage = Action.hexToAscii(strMessage);

                            JSONObject jObj = new JSONObject(strMessage);



                            int rowChanged = jObj.getInt("success");
                            String resultMessage = jObj.getString("message");

                            Toast.makeText(Homepage.this, "Result: "+rowChanged + " " + resultMessage, Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            //vibrator.vibrate(300);
                            //ringtone.play();
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            // Toast.makeText(Homepage.this, "All message received!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }




            }
        });

        Button btnCancel = (Button) mView.findViewById(R.id.btnCancelSubscription);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });


        mBuilder.setView(mView);
        dialog = mBuilder.create();
        dialog.show();
        //dialog.cancel();




    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
           // Intent intent = new Intent(this, MainActivity.class);
           // startActivity(intent);
            readEvent();

        } else if (id == R.id.nav_subscriptionCategory) {
            userEventSubscription();
        } else if (id == R.id.nav_incomingEvent) {
            Intent intent = new Intent(this, Upcoming.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_waitingList) {
            Intent intent = new Intent(this, Waiting.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_pastJoinedEvent) {
            Intent intent = new Intent(this, PastJoined.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_walkinRegistration) {
            Intent intent = new Intent(getApplicationContext(), WalkInRegistrationActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_redeemBenefits){
            Intent intent = new Intent(this, RedeemBenefit.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.nav_mark) {
            Intent intent = new Intent(getApplicationContext(), MarkAttendance.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void userEventSubscription(){

        try{
            JSONObject jsonObj = new JSONObject();

            // String[] category = {"Sports","Education","chk1"};
            //Gson gson = new Gson();
            //String jsonMessage = gson.toJson(category,String[].class);

            jsonObj.put("studentId",studentId);
            publishMessage(Action.combineMessage("001610",Action.asciiToHex(jsonObj.toString())));

            setSubscription(Action.clientTopic);
            subscribeSubscriptionMessage();
            String ssss = "";
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void loadAllTestingEvent(){

        final ArrayList<ApplicationEvent> arrList = new ArrayList<>();
        arrList.add(new ApplicationEvent(new GregorianCalendar(),new GregorianCalendar(),1,"Event 1"));
        arrList.add(new ApplicationEvent(new GregorianCalendar(),new GregorianCalendar(),2,"Event 2"));


        lstView = (ListView)findViewById(R.id.eventListView);
        EventListView eventListView = new EventListView(this,R.layout.eventlist_layout,arrList);
        lstView.setAdapter(eventListView);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                //Toast.makeText(getBaseContext(), questionList.get(i).getSubject(), Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(view.getContext(), DiscussionActivity.class);
                // intent.putExtra("QUESTION", questionList.get(i).getId());
                //startActivity(intent);
                Intent intent = new Intent(view.getContext(), DetailEventActivity.class);
                intent.putExtra("TIMETABLEID", arrList.get(i).getTimetableId());
                startActivity(intent);


                //Toast.makeText(getApplicationContext(), "row:" + i, Toast.LENGTH_LONG).show();
                //Object test  = adapterView.getItemAtPosition(i);
                //String ss = "";

            }
        });

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
                    Toast.makeText(Homepage.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);


                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }

                    // We are connected
                    //Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Homepage.this, "Connection fail!!", Toast.LENGTH_LONG).show();
                    // Something went wrong e.g. connection timeout or firewall problems
                    // Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void disconnect(){
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Homepage.this, "disconnected!!", Toast.LENGTH_LONG).show();
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(Homepage.this, "Disconnect fail!!", Toast.LENGTH_LONG).show();
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void publishMessage(String message) {
        // EditText topicText = (EditText)findViewById(R.id.topic);
        // topicStr = ""
        //EditText text = (EditText)findViewById(R.id.Msg);

        //String message = text.getText().toString();
        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            Toast.makeText(Homepage.this, "publish success l!!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void subscribeEventMessage(){
        if (client == null ){
            Toast.makeText(Homepage.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                //Gson gson2 = new Gson();
                //ApplicationEvent[] result2 = gson.fromJson(decoded,ApplicationEvent[].class);


                String ss = "";
                ArrayList<EncodedApplicationEvent> arrList1 = new ArrayList<>(Arrays.asList(result));
                final ArrayList<ApplicationEvent> arrList = new ArrayList<ApplicationEvent>();

                for(EncodedApplicationEvent e : arrList1){
                    arrList.add(e.getApplicationEvent());

                }

                lstView = (ListView)findViewById(R.id.eventListView);
                EventListView eventListView = new EventListView(context,R.layout.eventlist_layout,arrList);
                lstView.setAdapter(eventListView);

                lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                        //Toast.makeText(getBaseContext(), questionList.get(i).getSubject(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(view.getContext(), DetailEventActivity.class);
                        intent.putExtra("TIMETABLEID", arrList.get(i).getTimetableId());

                        try {
                            client.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                        //Toast.makeText(getApplicationContext(), "row:" + i, Toast.LENGTH_LONG).show();
                        //Object test  = adapterView.getItemAtPosition(i);
                        String ss = "";
                    }
                });



                String s = "";
                //vibrator.vibrate(300);
                //ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toast.makeText(Homepage.this, "All message received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());

                } catch (MqttException e) {
                    e.printStackTrace();
                }
                String sssss =  "";
            }
        });


    }


    public void unsubscribeTopic(String topic){
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Homepage.this, "Topic is unsubscribed!!", Toast.LENGTH_LONG).show();
                    // The subscription could successfully be removed from the client
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void setSubscription(String topicStr) {
        try {
            client.subscribe(topicStr, 1);


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        String ss= "";
    }



    public void readEvent(){
       // setSubscription(Action.topic);
        //conn();
        JSONObject obj = new JSONObject();
        Spinner spinner = (Spinner)findViewById(R.id.eventSpinner);
        String jsonString = "";
        try {
            obj.put("studentId",studentId);
            obj.put("criteria",spinner.getSelectedItem().toString());
            jsonString = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001609",Action.asciiToHex(jsonString)));
        subscribeEventMessage();

        //unsubscribeTopic(Action.clientTopic);
    }

    public void subscribeSubscriptionMessage(){
        if (client == null ){
            Toast.makeText(Homepage.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                JSONArray temp1 = new JSONArray(strMessage);
                JSONObject obj = temp1.getJSONObject(0);
                String strArr = obj.getString("subscription");
                Gson gson = new Gson();
                String[] ress = gson.fromJson(strArr,String[].class);
                buildSubscriptionDialog(ress);
                //vibrator.vibrate(300);
                //ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toast.makeText(Homepage.this, "All message received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                String sssss =  "";
            }
        });


    }



    @Override
    public void onRefresh() {
        readEvent();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        TextView views = (TextView)findViewById(R.id.txtEventResult);
        views.setText(adapterView.getItemAtPosition(i).toString());

        JSONObject obj = new JSONObject();
        Spinner spinner = (Spinner)findViewById(R.id.eventSpinner);
        String jsonString = "";
        try {
            obj.put("studentId",studentId);
            obj.put("criteria",spinner.getSelectedItem().toString());

            jsonString = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001609",Action.asciiToHex(jsonString)));
        subscribeEventMessage();



    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
