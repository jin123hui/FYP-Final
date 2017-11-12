package com.example.user.myproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.DetailedListAdapter;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.Homepage;
import com.example.user.myproject.Modal.SessionManager;
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

public class Upcoming extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView incomingList;
    private List<ApplicationEvent> incomingEvtList;
    private List<EventRegistration> regList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MqttAndroidClient client;
    private String studentId = "";
    private Context context;
    private  ProgressDialog pd;
    private final int interval = 5000; // 1 Second
    private static Handler handler = new Handler();
    private static boolean returned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        studentId = new SessionManager(this).getUserDetails().get("id");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        TextView appDrawerName = (TextView) hView.findViewById(R.id.appDrawerName);
        appDrawerName.setText(new SessionManager(this).getUserDetails().get("name"));
        TextView appDrawerId = (TextView) hView.findViewById(R.id.appDrawerId);
        appDrawerId.setText(new SessionManager(this).getUserDetails().get("id").toUpperCase());

        incomingList = (ListView) findViewById(R.id.incominglist);
        incomingEvtList = new ArrayList<>();
        regList = new ArrayList<>();

        context = this;



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

    public void onBackPressed() {
        Intent startMain = new Intent(context, Homepage.class);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void readEvent() {
        //conn();
        loadEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        studentId = new SessionManager(this).getUserDetails().get("id");
        swipeRefreshLayout.setRefreshing(true);
        conn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        studentId = new SessionManager(this).getUserDetails().get("id");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.other_action, menu);
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
            AlertDialog.Builder alert = new AlertDialog.Builder(Upcoming.this);
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
            Intent intent = new Intent(getApplicationContext(), About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                        client.subscribe(Action.clientTopic+studentId, 1);
                        //Toast.makeText(Upcoming.this, "Connected!!", Toast.LENGTH_LONG).show();

                        loadEvent();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Upcoming.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(Upcoming.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                //
                //returned = true;
                //handler.removeCallbacksAndMessages(null);
                //Toast.makeText(Upcoming.this, String.valueOf(returned), Toast.LENGTH_SHORT).show();
                //

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
                    evt.setEventDescription(e.getEventDescription());

                    EventRegistration reg = new EventRegistration();
                    reg.setRegistrationId(Integer.parseInt(e.getRegistrationId()));
                    regList.add(reg);
                    //Toast.makeText(Upcoming.this, e.getEventTitle(), Toast.LENGTH_LONG).show();
                    arrList.add(evt);

                }

                incomingEvtList = arrList;

                incomingList = (ListView) findViewById(R.id.incominglist);
                incomingList.setEmptyView(findViewById(R.id.empty));
                final DetailedListAdapter adapter = new DetailedListAdapter(context, R.layout.content_upcoming, arrList);
                incomingList.setAdapter(adapter);


                incomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                        Intent intent = new Intent(view.getContext(), Ticket.class);
                        intent.putExtra("REGISTRATION", regList.get(i));
                        intent.putExtra("EVENT", incomingEvtList.get(i));
                        startActivity(intent);
                    }
                });

                incomingList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, final int pos, final long id)
                    {
                        //Toast.makeText(Upcoming.this, "LongClick", Toast.LENGTH_LONG).show();
                        AlertDialog.Builder alert = new AlertDialog.Builder(Upcoming.this);
                        alert.setTitle("Cancel Registration");
                        alert.setMessage("Confirm to cancel your registration?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pd = new ProgressDialog(Upcoming.this);
                                pd.setMessage("Loading");
                                pd.show();

                                JSONObject obj = new JSONObject();
                                try{
                                    obj.put("studentId", studentId);
                                    obj.put("registrationId", regList.get(pos).getRegistrationId());
                                }catch(Exception ex){
                                    ex.printStackTrace();
                                }

                                publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));
                                if (client == null ){
                                    Toast.makeText(Upcoming.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                                        Toast.makeText(Upcoming.this, messages, Toast.LENGTH_LONG).show();
                                        pd.dismiss();
                                        //finish();
                                        swipeRefreshLayout.setRefreshing(true);
                                        onRefresh();
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
                        return true;
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Toast.makeText(Upcoming.this, "All event data received!!", Toast.LENGTH_LONG).show();
                /*handler.postDelayed(new Runnable() {
                    public void run() {
                        if(returned) {
                            handler.removeCallbacksAndMessages(null);
                        } else {
                            Toast.makeText(Upcoming.this, "Failed! Reconnecting!", Toast.LENGTH_SHORT).show();
                            loadEvent();
                        }
                    }
                }, interval);*/
            }
        });
    }

    private void loadEvent() {
        returned = false;
        JSONObject obj = new JSONObject();
        try {
            obj.put("studentId",studentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001609",Action.asciiToHex(obj.toString())));
        subscribeEventMessage();
    }


    @Override
    public void onRefresh() {
        loadEvent();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, Homepage.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_incomingEvent) {
            Intent intent = new Intent(this, Upcoming.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_waitingList) {
            Intent intent = new Intent(this, Waiting.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_pastJoinedEvent) {
            Intent intent = new Intent(this, PastJoined.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_walkinRegistration) {
            Intent intent = new Intent(this, WalkInRegistrationActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.nav_redeemBenefits){
            Intent intent = new Intent(this, RedeemBenefit.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.nav_softskill) {
            Intent intent = new Intent(this, SoftSkill.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
        handler.removeCallbacksAndMessages(null);
    }

    public void disconnect(){
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(getApplicationContext(), "disconnected!!", Toast.LENGTH_LONG).show();
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Disconnect fail!!", Toast.LENGTH_LONG).show();
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
