package com.example.user.myproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.EncodedEventRegistration;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.Homepage;
import com.example.user.myproject.Modal.RedeemListAdapter;
import com.example.user.myproject.Modal.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class RedeemBenefit extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView redeemListV;
    private List<EventRegistration> redeemList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MqttAndroidClient client;
    private String studentId = "";
    private Context context;
    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 300;
    public final static int HEIGHT = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_benefit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        appDrawerName.setText(new SessionManager(this).getUserDetails().get("name"));
        TextView appDrawerId = (TextView) hView.findViewById(R.id.appDrawerId);
        appDrawerId.setText(new SessionManager(this).getUserDetails().get("id"));

        redeemListV = (ListView) findViewById(R.id.benefitlist);
        redeemList = new ArrayList<>();

        context = this;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        //swipeRefreshLayout.setRefreshing(true);
                                        loadBenefit();
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
            AlertDialog.Builder alert = new AlertDialog.Builder(RedeemBenefit.this);
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

                        loadBenefit();
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(RedeemBenefit.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

    public void subscribeBenefitMessage(){
        if (client == null ){
            Toast.makeText(RedeemBenefit.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                EncodedEventRegistration[] result = gson.fromJson(decoded,EncodedEventRegistration[].class);

                ArrayList<EncodedEventRegistration> arrList1 = new ArrayList<>(Arrays.asList(result));
                final ArrayList<EventRegistration> arrList = new ArrayList<>();

                for(EncodedEventRegistration e : arrList1){
                    EventRegistration benefit = new EventRegistration();
                    benefit.setTimetableId(Integer.parseInt(e.getTimetableId()));
                    benefit.setRegistrationId(Integer.parseInt(e.getRegistrationId()));
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        d = sdf.parse(e.getEbTicketDueDate());
                    } catch (ParseException ex) {
                    }

                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTimeInMillis(d.getTime());
                    benefit.setEbTicketDueDate(gc);
                    benefit.setRedeemedStatus(e.getRedeemedStatus());
                    benefit.setEventTitle(e.getEventTitle());
                    arrList.add(benefit);
                }

                redeemList = arrList;
                //Toast.makeText(Upcoming.this, "WOW!!"+arrList.get(0).getVenueName(), Toast.LENGTH_LONG).show();

                redeemListV = (ListView) findViewById(R.id.benefitlist);
                redeemListV.setEmptyView(findViewById(R.id.empty));
                final RedeemListAdapter adapter = new RedeemListAdapter(context, R.layout.content_redeem_benefit, arrList);
                redeemListV.setAdapter(adapter);

                redeemListV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView adapterView, View view, final int i, long l) {

                        //Toast.makeText(RedeemBenefit.this, arrList.get(i).getEventTitle(), Toast.LENGTH_LONG).show();

                        final AlertDialog.Builder builder = new AlertDialog.Builder(RedeemBenefit.this);
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        LayoutInflater inflater = getLayoutInflater();
                        View dialoglayout = inflater.inflate(R.layout.image_layout, null);
                        builder.setView(dialoglayout);
                        ImageView qrCode = (ImageView) dialoglayout.findViewById(R.id.qrCode);
                        try {
                            Bitmap bitmap = encodeAsBitmap(String.valueOf(redeemList.get(i).getRegistrationId()));
                            //Toast.makeText(RedeemBenefit.this, String.valueOf(redeemList.get(i).getRegistrationId()), Toast.LENGTH_LONG).show();
                            qrCode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }

                        builder.show();

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

                                if(!obj.getString("clientMsg").isEmpty() && obj.getString("clientMsg").equals("Redeemed") && obj.getString("registrationId").equals(redeemList.get(i).getRegistrationId()+"")) {
                                    Toast.makeText(RedeemBenefit.this, "Redeemed", Toast.LENGTH_LONG).show();
                                    //builder.dismiss();
                                    //loadBenefit();
                                    finish();
                                    startActivity(getIntent());
                                }
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
                });

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

    private void loadBenefit() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("studentId",studentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Action.combineMessage("001618",Action.asciiToHex(obj.toString())));
        subscribeBenefitMessage();
    }


    @Override
    public void onRefresh() {
        loadBenefit();
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
