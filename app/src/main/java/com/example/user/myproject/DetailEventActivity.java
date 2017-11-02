package com.example.user.myproject;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class DetailEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    int timetableId  = 0;
    String registrationStatus = "";

    private GoogleMap map;
    MqttAndroidClient client;
    float longitude ;
    float latitude ;
    String locationName;
    EncodedApplicationEvent event;
    String studentId = "";
    String studentName = "desmond";
    private ArrayList<String> files_on_server = new ArrayList<>();
    String from;
    EventRegistration reg;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentId = new SessionManager(this).getUserDetails().get("id");

    }

    @Override
    protected void onStart() {
        super.onStart();
        studentId = new SessionManager(this).getUserDetails().get("id");
        pd = new ProgressDialog(DetailEventActivity.this);
        pd.setMessage("Loading");
        pd.show();

        conn();

        Button individualRegistrationButton = (Button)findViewById(R.id.btnIndividualRegistration);
        individualRegistrationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onIndividualClick(null);
            }
        });


        Button groupRegistration = (Button)findViewById(R.id.btnGroupRegistration);
        groupRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGroupRegistrationClick();
            }
        });


        permission_check();

        Bundle bundle = getIntent().getExtras();
        if(!bundle.isEmpty()){
            timetableId = bundle.getInt("TIMETABLEID");
            from = bundle.getString("FROM");
            reg = (EventRegistration) bundle.getSerializable("REGISTRATION");
            studentId = bundle.getString("STUDENTID");
        }else{
            timetableId = 0 ;
            studentId = new SessionManager(this).getUserDetails().get("id");
            from = "";
        }

        TextView txtIndividualInfo = (TextView)findViewById(R.id.txtIndividualInfo);
        TextView txtGroupInfo = (TextView)findViewById(R.id.txtGroupInfo);
        EditText eventRegistrationDescription = (EditText)findViewById(R.id.eventRegistrationDescription);

        if(from.equals("ticket")) {
            groupRegistration.setVisibility(View.INVISIBLE);
            individualRegistrationButton.setText("Update Registration Details");
            txtIndividualInfo.setVisibility(View.INVISIBLE);
            txtGroupInfo.setVisibility(View.INVISIBLE);

            individualRegistrationButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    onUpdateRegistrationDetails();
                }
            });
            eventRegistrationDescription.setText(reg.getDescription());
            studentId = new SessionManager(this).getUserDetails().get("id");
        } else if(from.equals("waiting")) {
            groupRegistration.setVisibility(View.INVISIBLE);
            individualRegistrationButton.setText("Reserved in Waiting List");
            individualRegistrationButton.setEnabled(false);
            individualRegistrationButton.setBackgroundColor(Color.GRAY);
            txtIndividualInfo.setVisibility(View.INVISIBLE);
            txtGroupInfo.setVisibility(View.INVISIBLE);
            eventRegistrationDescription.setText(reg.getDescription());
            eventRegistrationDescription.setEnabled(false);
            studentId = new SessionManager(this).getUserDetails().get("id");
            //timetableId = reg.getTimetableId();
        } else if(from.equals("past")) {
            groupRegistration.setVisibility(View.INVISIBLE);
            individualRegistrationButton.setText("Participated");
            individualRegistrationButton.setEnabled(false);
            individualRegistrationButton.setBackgroundColor(Color.GRAY);
            txtIndividualInfo.setVisibility(View.INVISIBLE);
            txtGroupInfo.setVisibility(View.INVISIBLE);
            eventRegistrationDescription.setText(reg.getDescription());
            eventRegistrationDescription.setEnabled(false);
            //timetableId = reg.getTimetableId();
            studentId = new SessionManager(this).getUserDetails().get("id");
        }
    }

    public void onUpdateRegistrationDetails() {
        AlertDialog.Builder alert = new AlertDialog.Builder(DetailEventActivity.this);
        alert.setTitle("Update Registration Details");
        alert.setMessage("Confirm to update registration details?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd = new ProgressDialog(DetailEventActivity.this);
                pd.setMessage("Loading");
                pd.show();
                EditText eventRegistrationDescription = (EditText) findViewById(R.id.eventRegistrationDescription);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("registrationId", reg.getRegistrationId());
                    obj.put("description", eventRegistrationDescription.getText().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                publishMessage(Action.combineMessage("001643", Action.asciiToHex(obj.toString())));
                if (client == null) {
                    Toast.makeText(DetailEventActivity.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(DetailEventActivity.this, messages, Toast.LENGTH_LONG).show();
                        //finish();
                        pd.dismiss();
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

    public void onGroupRegistrationClick(){

        if(event == null)
            return;

        if(registrationStatus.equals("") || registrationStatus.equals("0")){
            Toast.makeText(getApplicationContext(),"Student is registered for the event!!",Toast.LENGTH_LONG).show();
            return;

        }


        if(Integer.parseInt(event.getCurrentGroup()) >= Integer.parseInt(event.getMaxGroup())){
            Toast.makeText(getApplicationContext(),"Cannot perform group registration because event full!",Toast.LENGTH_LONG).show();;

        }else{
            EditText eventRegistrationDescription = (EditText)findViewById(R.id.eventRegistrationDescription);

            Intent intent = new Intent(getApplicationContext(), GroupRegistrationActivity.class);
            intent.putExtra("STUDENTID",studentId);
            intent.putExtra("STUDENTNAME",studentName);
            intent.putExtra("SEATAVAILABLE",event.getGroupMemberAvailable());
            intent.putExtra("TIMETABLEID",event.getTimetableId());
            String val = eventRegistrationDescription.getText().toString();
            intent.putExtra("REG_DESCRIPTION",eventRegistrationDescription.getText().toString());

            startActivity(intent);
        }

    }
    public void onIndividualClick(View v) {
        if(event == null)
            return;

        if(registrationStatus.equals("") || registrationStatus.equals("0")){
            Toast.makeText(getApplicationContext(),"Student is registered for the event!!",Toast.LENGTH_LONG).show();
            return;

        }

        String reserveMessage = "";
        AlertDialog.Builder alert = new AlertDialog.Builder(DetailEventActivity.this);
        alert.setTitle("Individual Registration");
        if(Integer.parseInt(event.getCurrentParticipants()) >= Integer.parseInt(event.getNoOfParticipants())){
            alert.setMessage("Confirm reserve the event since it is full?");
            reserveMessage  = "Active";
        }else{
            alert.setMessage("Confirm perform individual registration to the event?");
            reserveMessage = "";
        }
        final String reserve = reserveMessage;
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText eventRegistrationDescription = (EditText)findViewById(R.id.eventRegistrationDescription);
                JSONObject obj = new JSONObject();
                try{
                    obj.put("studentId",studentId);
                    obj.put("timetableId",timetableId);
                    obj.put("waitinglistStatus",reserve);
                    obj.put("description",eventRegistrationDescription.getText().toString());
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                publishMessage(Action.combineMessage("001613",Action.asciiToHex(obj.toString())));
                if (client == null ){
                    Toast.makeText(DetailEventActivity.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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

                            Toast.makeText(DetailEventActivity.this, messages, Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),messages,Toast.LENGTH_LONG).show();
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = 900;
        mapFragment.getView().setLayoutParams(params);

    }

    public boolean googleServicesAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS){
            return true;
        }else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this,isAvailable,0);
            //dialog.show();

        }else {
            Toast.makeText(this,"Cant conenct to play servies",Toast.LENGTH_LONG).show();
        }
        return false;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapLocation(longitude, latitude,locationName,10);



    }

    public void setMapLocation(double x, double y,String locationName){
        LatLng position = new LatLng(x, y);
        map.addMarker(new MarkerOptions().position(position).title(locationName));
        map.moveCamera(CameraUpdateFactory.newLatLng(position));

    }

    public void setMapLocation(double x, double y,String locationName,float zoom){
        LatLng position = new LatLng(x, y);
        map.addMarker(new MarkerOptions().position(position).title(locationName));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position,zoom));

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
                    //Toast.makeText(DetailEventActivity.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);

                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("studentId",new SessionManager(getApplicationContext()).getUserDetails().get("id"));
                            obj.put("timetableId",timetableId);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001612",Action.asciiToHex(obj.toString())));
                        subscribeEventMessage();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(DetailEventActivity.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
            //Toast.makeText(DetailEventActivity.this, "publish success !!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void subscribeEventMessage(){
        if (client == null ){
            Toast.makeText(DetailEventActivity.this, "Connection fail!!", Toast.LENGTH_LONG).show();
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
                String ssss = obj.getString("success");
                event = new EncodedApplicationEvent();
                event.setTimetableId(timetableId+"");
                if (obj.getString("success").equals("1")){
                    // EncodedApplicationEvent event = new EncodedApplicationEvent();
                    //event.setTimetableId(obj.getString("timetableId"));
                    event.setEventTitle(obj.getString("eventTitle"));
                    event.setEventDescription(obj.getString("eventDescription"));
                    event.setEventStartTime(obj.getString("eventStartTime"));
                    event.setEventEndTime(obj.getString("eventEndTime"));
                    event.setVenueName(obj.getString("venueName"));
                    event.setVenueDescription(obj.getString("venueDescription"));
                    event.setNoOfParticipants(obj.getString("noOfParticipants"));
                    event.setCurrentParticipants(obj.getString("currentParticipants"));
                    event.setCurrentGroup(obj.getString("teamLimit"));
                    event.setMaxGroup(obj.getString("maxTeam"));
                    event.setGroupMemberAvailable(obj.getString("minTeam"));
                    event.setEventBrochure(obj.getString("eventBrochure"));


                }else{
                    //Toast.makeText(getApplicationContext(),"Data retrieve failed!! Please contact admin.",Toast.LENGTH_LONG).show();;
                    finish();

                }



                registrationStatus = obj.getString("successStatus");



                setTitle(event.getEventTitle());

                TextView eventDescription = (TextView)findViewById(R.id.textEventDescription);
                eventDescription.setText(event.getEventDescription());

                ApplicationEvent trueEvent = event.getApplicationEvent();

                TextView txtdate = (TextView)findViewById(R.id.txtDetailDate);
                String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                txtdate.setText(mssg);

                TextView txtTime = (TextView)findViewById(R.id.txtDetailTime);
                txtTime.setText("Time: " + ApplicationEvent.displayTime(trueEvent.getStartTime())
                        + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );


                TextView txtIndividualInfo = (TextView)findViewById(R.id.txtIndividualInfo);
                txtIndividualInfo.setText("Seat Available: "+ event.getCurrentParticipants()+ " / "+ event.getNoOfParticipants() );

                TextView txtGroupInfo = (TextView)findViewById(R.id.txtGroupInfo);
                txtGroupInfo.setText("Group Available: " + event.getCurrentGroup() + " / "+ event.getMaxGroup());
                pd.dismiss();

                ImageView image = (ImageView)findViewById(R.id.imageDetailEvent);

                ImageTask task = new ImageTask(image);
                int tempTimetable = 0;
                try {
                    tempTimetable = Integer.parseInt(event.getTimetableId());
                }catch (Exception ex){
                    ex.printStackTrace();
                    tempTimetable = 0;
                }


                task.execute(tempTimetable);

                if(googleServicesAvailable()){
                    //  Toast.makeText(this, "Welp success", Toast.LENGTH_SHORT).show();
                    longitude = event.getLat();
                    latitude = event.getLong();
                    locationName = event.getVenueName();
                    initMap();
                }

                setMapLocation(event.getLat(), event.getLong(),event.getVenueName(),10);

                ScrollView view = (ScrollView)findViewById(R.id.ScrollView01);
                view.setVisibility(View.VISIBLE);


                String s = "";
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //  Toast.makeText(DetailEventActivity.this, "All message received!!", Toast.LENGTH_LONG).show();
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


    public void setSubscription(String topicStr) {
        try {
            client.subscribe(topicStr, 1);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        String ss= "";
    }


    private void permission_check(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                return;
            }

        }

        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initialize();
        }else{
            permission_check();
        }
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public DownloadManager downloadManager;

    private void initialize() {

        Button download = (Button)findViewById(R.id.btnDownloadBrochure);
        download.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                if(event == null || event.getEventBrochure().equals("")){
                    Toast.makeText(getApplicationContext(),"No brochure found in database!",Toast.LENGTH_LONG).show();
                    return;
                }else {
                    //Toast.makeText(getApplicationContext(),event.getEventBrochure(),Toast.LENGTH_LONG).show();

                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse("http://"+new SessionManager(getApplicationContext()).getUserDetails().get("address")+".ngrok.io/phpMQTT-master/files/downloadBrochure.php?files="+event.getEventBrochure());
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    Long reference = downloadManager.enqueue(request);
                }
            }
        });
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
                } else {
                    // if you see  dao then change to icnoimage icon
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }

}
