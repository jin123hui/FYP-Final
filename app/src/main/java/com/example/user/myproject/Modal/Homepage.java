package com.example.user.myproject.Modal;

import android.app.Application;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.DetailEventActivity;
import com.example.user.myproject.GroupRegistrationActivity;
import com.example.user.myproject.LoginActivity;
import com.example.user.myproject.MainActivity;
import com.example.user.myproject.MarkAttendance;
import com.example.user.myproject.PastJoined;
import com.example.user.myproject.R;
import com.example.user.myproject.RedeemBenefit;
import com.example.user.myproject.SoftSkill;
import com.example.user.myproject.Upcoming;
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
import org.w3c.dom.Text;

import java.security.acl.Group;
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
    String studentId = "";
    String studentName = "desmond";

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else {
            connected = false;
        }

        if(connected) {

            new SessionManager(this).checkLogin();

            studentId = new SessionManager(this).getUserDetails().get("id");

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            View hView = navigationView.getHeaderView(0);
            TextView appDrawerName = (TextView) hView.findViewById(R.id.appDrawerName);
            appDrawerName.setText(new SessionManager(this).getUserDetails().get("id"));

            //Toast.makeText(Homepage.this, new SessionManager(this).getUserDetails().get("address"), Toast.LENGTH_LONG).show();
            context = this;

            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // swipeRefreshLayout.setRefreshing(true);
                                            // readQuestion();
                                            //readEvent();
                                        }
                                    }

            );

            Spinner spinner = (Spinner) findViewById(R.id.eventSpinner);

            spinner.setOnItemSelectedListener(this);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.eventCategory, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(true);

            conn();
        } else {
            Toast.makeText(Homepage.this, "No internet connection!", Toast.LENGTH_LONG).show();
        }

    }

    public void buildSubscriptionDialog(String[] category){
        AlertDialog.Builder mBuilder  = new AlertDialog.Builder(Homepage.this);
        View mView = getLayoutInflater().inflate(R.layout.subscription_layout,null);
        ArrayList<String> subscriptionList = new ArrayList<String>(Arrays.asList(category));
        CheckBox chkBoxSports = (CheckBox)mView.findViewById(R.id.checkBoxSports);
        CheckBox chkBoxEducation = (CheckBox)mView.findViewById(R.id.checkBoxEducation);
        CheckBox chkBox1 = (CheckBox)mView.findViewById(R.id.checkBoxGame);
        CheckBox chkBox2 = (CheckBox)mView.findViewById(R.id.checkBoxIT);
        CheckBox chkBox3 = (CheckBox)mView.findViewById(R.id.checkBoxBusiness);
        CheckBox chkBoxMusic = (CheckBox)mView.findViewById(R.id.checkBoxMusic);
        final ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();
        checkBoxList.add(chkBoxSports);
        checkBoxList.add(chkBoxEducation);
        checkBoxList.add(chkBox1);
        checkBoxList.add(chkBox2);
        checkBoxList.add(chkBox3);
        checkBoxList.add(chkBoxMusic);

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
    }


    @Override
    protected void onStart() {
        super.onStart();
        studentId = new SessionManager(this).getUserDetails().get("id");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getIntent().getExtras() != null) {
                if (getIntent().getExtras() != null && getIntent().getExtras().getString("lastActivity").equals("third")) {
                    Toast.makeText(getApplicationContext(), "wew this is return from third", Toast.LENGTH_LONG).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Really Exit?")
                            .setMessage("Are you sure you want to exit?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Homepage.super.onBackPressed();
                                }
                            }).create().show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_scan) {
            Intent intent = new Intent(getApplicationContext(), MarkAttendance.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_logout) {
            AlertDialog.Builder alert = new AlertDialog.Builder(Homepage.this);
            alert.setTitle("Logout");
            alert.setMessage("Confirm to logout?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    new SessionManager(getApplicationContext()).logoutUser();
                    startActivity(intent);
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
        } else if(id == R.id.action_about) {
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
       // } else if(id == R.id.nav_mark) {
            //Intent intent = new Intent(getApplicationContext(), MarkAttendance.class);
            //startActivity(intent);
        } else if(id == R.id.nav_softskill) {
            Intent intent = new Intent(this, SoftSkill.class);
            startActivity(intent);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void userEventSubscription(){

        try{
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("studentId",studentId);
            publishMessage(Action.combineMessage("001610",Action.asciiToHex(jsonObj.toString())));

            setSubscription(Action.clientTopic);
            subscribeSubscriptionMessage();
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
                Intent intent = new Intent(view.getContext(), DetailEventActivity.class);
                intent.putExtra("TIMETABLEID", arrList.get(i).getTimetableId());
                intent.putExtra("FROM", "");
                intent.putExtra("REGISTRATION", new EventRegistration());
                startActivity(intent);

            }
        });
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
                    //Toast.makeText(Homepage.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);
                        readEvent();
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Homepage.this, "Connection fail!!", Toast.LENGTH_LONG).show();

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
                    //  Toast.makeText(Homepage.this, "disconnected!!", Toast.LENGTH_LONG).show();
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

        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            //Toast.makeText(Homepage.this, "publish success l!!", Toast.LENGTH_LONG).show();
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
                swipeRefreshLayout.setRefreshing(false);
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                Gson gson = builder.create();
                String decoded = Action.hexToAscii(strMessage);
                EncodedApplicationEvent[] result = gson.fromJson(decoded,EncodedApplicationEvent[].class);
                ArrayList<EncodedApplicationEvent> arrList1 = new ArrayList<>(Arrays.asList(result));
                final ArrayList<ApplicationEvent> arrList = new ArrayList<ApplicationEvent>();

                for(EncodedApplicationEvent e : arrList1){
                    arrList.add(e.getApplicationEvent());

                }

                lstView = (ListView)findViewById(R.id.eventListView);
                BasicListAdapter eventListView = new BasicListAdapter(context,R.layout.basiclist_entry_layout,arrList);
                lstView.setAdapter(eventListView);

                lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                        //Toast.makeText(getBaseContext(), questionList.get(i).getSubject(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(view.getContext(), DetailEventActivity.class);
                        intent.putExtra("TIMETABLEID", arrList.get(i).getTimetableId());
                        intent.putExtra("FROM", "");
                        intent.putExtra("REGISTRATION", new EventRegistration());
                        intent.putExtra("STUDENTID", studentId);

                        /*try {
                            client.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }*/
                        startActivity(intent);
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Homepage.this, "All message received!!", Toast.LENGTH_LONG).show();
                String str = "";
                try {
                    str = new String(token.getMessage().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
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

    public void subscribeSubscriptionMessage(){
        if (client == null ){
            Toast.makeText(Homepage.this, "Connection fail!!", Toast.LENGTH_LONG).show();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

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

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Homepage.this, "All message received!!", Toast.LENGTH_LONG).show();
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
        swipeRefreshLayout.setRefreshing(true);
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
