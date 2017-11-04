package com.example.user.myproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.BasicListAdapter;
import com.example.user.myproject.Modal.CaptureActivityPortrait;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.Homepage;
import com.example.user.myproject.Modal.SessionManager;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class WalkInRegistrationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    MqttAndroidClient client;
    private int timetableId = 0;
    private String studentId = "";
    private final int CAMERA_REQUEST_CODE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_in_registration);
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

        View hView =  navigationView.getHeaderView(0);
        TextView appDrawerName = (TextView) hView.findViewById(R.id.appDrawerName);
        appDrawerName.setText(new SessionManager(this).getUserDetails().get("id"));
    }

    public void onBackPressed() {
        Intent startMain = new Intent(getApplicationContext(), Homepage.class);
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
            AlertDialog.Builder alert = new AlertDialog.Builder(WalkInRegistrationActivity.this);
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
    protected void onResume(){
        super.onResume();
        studentId = new SessionManager(this).getUserDetails().get("id");
        conn();
        createPermissions(Manifest.permission.CAMERA,CAMERA_REQUEST_CODE);
        Button registerButton = (Button)findViewById(R.id.buttonWalkInRegistration);
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                if (timetableId == 0){
                    TextView msg = (TextView)findViewById(R.id.textViewWalkInError);
                    msg.setText("Please scan an event before register!");
                    // Toast.makeText(getApplicationContext(),"Please scan an event before register!",Toast.LENGTH_LONG).show();

                }else{

                    JSONObject obj = new JSONObject();
                    try{
                        obj.put("studentId",studentId);
                        obj.put("timetableId",timetableId);
                        obj.put("description", "");
                        obj.put("waitinglistStatus", "");

                    }catch(Exception ex){
                        ex.printStackTrace();
                    }


                    publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));



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

                            if(success.equals("1")){
                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                finish();

                            }else{
                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
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
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            if(result.getContents() == null){
                Toast.makeText(getApplicationContext(),"You cancelled the scanning",Toast.LENGTH_LONG).show();
            }else{
                JSONObject json = new JSONObject();
                try{
                    json.put("timetableId",result.getContents());
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                publishMessage(Action.combineMessage("001614",Action.asciiToHex(json.toString())));
                setSubscription(Action.serverTopic);
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

                        //TextView eventTitle = (TextView)findViewById(R.id.textViewEventName);
                        //TextView eventStartDate = (TextView) findViewById(R.id.textViewEventStartDate);
                        //TextView eventStartTime = (TextView)findViewById(R.id.textViewEventStartTime);
                        TextView msg = (TextView)findViewById(R.id.textViewWalkInError);
                        TextView availableSeat = (TextView)findViewById(R.id.textViewAvailableSeat);
                        Button registerButton = (Button)findViewById(R.id.buttonWalkInRegistration);
                        //TextView eventCategory = (TextView)findViewById(R.id.textviewCategory);

                        //eventTitle.setText("");
                        //eventStartDate.setText("");
                        //eventStartTime.setText("");
                        msg.setText("");
                        //eventCategory.setText("");
                        availableSeat.setText("");

                        if(success.equals("1")){

                            EncodedApplicationEvent encodApp = new EncodedApplicationEvent();
                            encodApp.setTimetableId(obj.getString("timetableId"));
                            encodApp.setEventTitle(obj.getString("eventTitle"));
                            encodApp.setEventStartTime(obj.getString("eventStartTime"));
                            encodApp.setEventEndTime(obj.getString("eventEndTime"));
                            encodApp.setCurrentParticipants(obj.getString("availableSeat"));
                            encodApp.setNoOfParticipants(obj.getString("noOfParticipants"));
                            encodApp.setActivityType(obj.getString("activityType"));


                            //eventTitle.setText(obj.getString("eventTitle"));
                            ApplicationEvent trueEvent = encodApp.getApplicationEvent();

                            timetableId = trueEvent.getTimetableId();
                            String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                            //eventStartDate.setText(mssg);

                            //eventStartTime.setText("Time:" + ApplicationEvent.displayTime(trueEvent.getStartTime())
                                  //  + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );

                            availableSeat.setText("Seat available : " + encodApp.getCurrentParticipants()+" / " + encodApp.getNoOfParticipants());
                            registerButton.setVisibility(View.VISIBLE);


                            //eventCategory.setText(encodApp.getActivityType());

                            //
                            ArrayList<ApplicationEvent> evt = new ArrayList<>();
                            evt.add(trueEvent);
                            ListView list = (ListView) findViewById(R.id.list);
                            final BasicListAdapter adapter = new BasicListAdapter(getApplicationContext(), R.layout.content_ticket, evt);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(view.getContext(), DetailEventActivity.class);
                                    intent.putExtra("TIMETABLEID", timetableId);
                                    intent.putExtra("FROM", "");
                                    intent.putExtra("REGISTRATION", new EventRegistration());
                                    intent.putExtra("STUDENTID", studentId);
                                    startActivity(intent);
                                }
                            });
                            //

                            /*ImageView image = (ImageView)findViewById(R.id.imageViewWalkIn);
                            image.setVisibility(View.VISIBLE);
                            ImageTask task = new ImageTask(image);




                            int tempTimetable = 0;
                            try {
                                tempTimetable = Integer.parseInt(encodApp.getTimetableId());
                            }catch (Exception ex){
                                ex.printStackTrace();
                                tempTimetable = 0;
                            }


                            task.execute(tempTimetable);*/


                        }else{
                            registerButton.setVisibility(View.GONE);
                            timetableId = 0;
                            //ImageView image = (ImageView)findViewById(R.id.imageViewWalkIn);
                            //image.setVisibility(View.GONE);
                            //image.setImageResource(R.mipmap.ic_noimage);
                            msg.setText(obj.getString("message"));
                            // Toast.makeText(getApplicationContext(),obj.getString("message"),Toast.LENGTH_LONG).show();

                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
                    }
                });




                result.getContents();
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        if(scannerView != null)
            scannerView.stopCamera();




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    @Override
    public void handleResult(Result result) {
        Toast.makeText(getApplicationContext(),result.getText(),Toast.LENGTH_LONG).show();

        JSONObject json = new JSONObject();
        try{
            json.put("timetableId","");
        }catch(Exception ex){
            ex.printStackTrace();
        }

        publishMessage(Action.combineMessage("001614",Action.asciiToHex(json.toString())));

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

                //TextView eventTitle = (TextView)findViewById(R.id.textViewEventName);
                //TextView eventStartDate = (TextView) findViewById(R.id.textViewEventStartDate);
                //TextView eventStartTime = (TextView)findViewById(R.id.textViewEventStartTime);
                TextView msg = (TextView)findViewById(R.id.textViewWalkInError);
                TextView availableSeat = (TextView)findViewById(R.id.textViewAvailableSeat);
                Button registerButton = (Button)findViewById(R.id.buttonWalkInRegistration);

                EncodedApplicationEvent encodApp = new EncodedApplicationEvent();
                encodApp.setTimetableId(obj.getString("timetableId"));
                encodApp.setEventTitle(obj.getString("eventTitle"));
                encodApp.setEventStartTime(obj.getString("eventStartTime"));
                encodApp.setEventEndTime(obj.getString("eventEndTime"));
                encodApp.setCurrentParticipants(obj.getString("availableSeat"));
                encodApp.setNoOfParticipants(obj.getString("noOfParticipants"));


                //eventTitle.setText("");
                //.setText("");
                //eventStartTime.setText("");
                msg.setText("");
                availableSeat.setText("");

                if(success.equals("1")){
                    //eventTitle.setText(obj.getString("eventTitle"));
                    ApplicationEvent trueEvent = encodApp.getApplicationEvent();

                    timetableId = trueEvent.getTimetableId();
                    String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                    //eventStartDate.setText(mssg);

                    //eventStartTime.setText("Time:" + ApplicationEvent.displayTime(trueEvent.getStartTime())
                     //       + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );

                    availableSeat.setText(encodApp.getCurrentParticipants()+" / " + encodApp.getNoOfParticipants());
                    registerButton.setVisibility(View.VISIBLE);


                }else{
                    registerButton.setVisibility(View.GONE);
                    registerButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                            alert.setTitle("Walk in Registration");
                            alert.setMessage("Confirm perform individual registration to the event?");
                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    JSONObject obj = new JSONObject();
                                    try{
                                        obj.put("studentId",studentId);
                                        obj.put("timetableId",timetableId);
                                        obj.put("description", "");
                                        obj.put("waitinglistStatus", "");

                                    }catch(Exception ex){
                                        ex.printStackTrace();
                                    }

                                    publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));



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


                                            if(success.equals("1")){
                                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                                finish();

                                            }else{
                                                Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                            }


                                        }

                                        @Override
                                        public void deliveryComplete(IMqttDeliveryToken token) {
                                            // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
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

                    msg.setText(obj.getString("message"));

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
            }
        });


        //  scannerView.resumeCameraPreview(this);



    }


    public void createPermissions(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
        }else{

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"camera permission granted",Toast.LENGTH_LONG).show();



                } else {
                    Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return ;
            // permissions this app might request
        }
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
                    //Toast.makeText(getApplicationContext(), "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();

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
            Toast.makeText(getApplicationContext(), "publish success !!", Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, Homepage.class);
            startActivity(intent);
        } else if (id == R.id.nav_incomingEvent) {
            Intent intent = new Intent(this, Upcoming.class);
            startActivity(intent);
        } else if (id == R.id.nav_waitingList) {
            Intent intent = new Intent(this, Waiting.class);
            startActivity(intent);
        } else if (id == R.id.nav_pastJoinedEvent) {
            Intent intent = new Intent(this, PastJoined.class);
            startActivity(intent);
        } else if (id == R.id.nav_walkinRegistration) {
            Intent intent = new Intent(this, WalkInRegistrationActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_redeemBenefits){
            Intent intent = new Intent(this, RedeemBenefit.class);
            startActivity(intent);
        } else if(id == R.id.nav_softskill) {
            Intent intent = new Intent(this, SoftSkill.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disconnect(){
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "disconnected!!", Toast.LENGTH_LONG).show();
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




    private class ImageTask extends AsyncTask<Integer, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;

        public ImageTask(ImageView imageView) {

            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        protected void onPreExecute() {
        }

        protected Bitmap doInBackground(Integer... params) {
            Bitmap myBitmap = null;
            try {
                URL url = new URL("http://"+new SessionManager(getApplicationContext()).getUserDetails().get("address")+".ngrok.io/phpMQTT-master/files/get_image.php?timetableId="+params[0]);// + evt.getTimetableId());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                //e.printStackTrace();
                //e.getMessage();
            }
            return myBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (imageViewReference != null && result != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(result);
                    imageView.setTag(result);
                } else {
                    // if you see  dao then change to icnoimage icon
                    imageView.setImageResource(R.mipmap.ic_noimage);
                    //imageView.setTag(result);
                }
            }
        }
    }


}
