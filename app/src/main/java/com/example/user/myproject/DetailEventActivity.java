package com.example.user.myproject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
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

public class DetailEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    int timetableId  = 0;
    private GoogleMap map;
    MqttAndroidClient client;
    float longitude ;
    float latitude ;
    String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getActionBar().setDisplayHomeAsUpEnabled(true);
        conn();

        Bundle bundle = getIntent().getExtras();
        if(!bundle.isEmpty()){
            timetableId = bundle.getInt("TIMETABLEID");
        }else{
            timetableId = 0 ;

        }

        int sss = 0;



        Button individualRegistrationButton = (Button)findViewById(R.id.btnIndividualRegistration);
        individualRegistrationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onIndividualClick(null);
            }
        });



    }

    public void onIndividualClick(View v) {


        AlertDialog.Builder alert = new AlertDialog.Builder(DetailEventActivity.this);
        alert.setTitle("Individual Registration");
        alert.setMessage("Confirm perform individual registration to the event?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                JSONObject obj = new JSONObject();
                try{
                    obj.put("studentId",Action.studentId);
                    obj.put("timetableId",timetableId);

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

                        Toast.makeText(DetailEventActivity.this, messages, Toast.LENGTH_LONG).show();
                        finish();

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
                    Toast.makeText(DetailEventActivity.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client.subscribe(Action.clientTopic, 1);

                        JSONObject obj = new JSONObject();
                        try{
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
            Toast.makeText(DetailEventActivity.this, "publish success !!", Toast.LENGTH_LONG).show();
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
                EncodedApplicationEvent event = new EncodedApplicationEvent();
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


                }else{


                }


                TextView title = (TextView)findViewById(R.id.txtEventName);
                title.setText(event.getEventTitle());

                TextView eventDescription = (TextView)findViewById(R.id.textEventDescription);
                eventDescription.setText(event.getEventDescription());

                ApplicationEvent trueEvent = event.getApplicationEvent();

                TextView txtdate = (TextView)findViewById(R.id.txtDetailDate);
                String mssg = "Date: "+ Action.displayDate(trueEvent.getStartTime());
                txtdate.setText(mssg);

                TextView txtTime = (TextView)findViewById(R.id.txtDetailTime);
                txtTime.setText("Time:" + ApplicationEvent.displayTime(trueEvent.getStartTime())
                        + " - "  + ApplicationEvent.displayTime(trueEvent.getEndTime()) );

                TextView txtNumberOfParticipants = (TextView)findViewById(R.id.txtNumberOfParticipants);
                txtNumberOfParticipants.setText(event.getCurrentParticipants()+" / " + event.getNoOfParticipants());


                if(googleServicesAvailable()){
                  //  Toast.makeText(this, "Welp success", Toast.LENGTH_SHORT).show();
                    longitude = event.getLat();
                    latitude = event.getLong();
                    locationName = event.getVenueName();
                    initMap();
                }

                setMapLocation(event.getLat(), event.getLong(),event.getVenueName(),10);

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

}
