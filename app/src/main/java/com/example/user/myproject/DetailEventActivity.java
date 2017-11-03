package com.example.user.myproject;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

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
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentId = new SessionManager(this).getUserDetails().get("id");
    }

    private void zoomImageFromThumb(final View thumbView, Bitmap imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        ScrollView sv = (ScrollView) findViewById(R.id.ScrollView01);
        sv.setEnabled(false);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                ScrollView sv = (ScrollView) findViewById(R.id.ScrollView01);
                sv.setEnabled(true);

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
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
                pd = new ProgressDialog(DetailEventActivity.this);
                pd.setMessage("Registering...");
                pd.show();
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
                            pd.dismiss();
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

                ImageButton image = (ImageButton)findViewById(R.id.imageDetailEvent);

                ImageTask task = new ImageTask(image);
                int tempTimetable = 0;
                try {
                    tempTimetable = Integer.parseInt(event.getTimetableId());
                }catch (Exception ex){
                    ex.printStackTrace();
                    tempTimetable = 0;
                }

                final View thumb1View = findViewById(R.id.imageDetailEvent);
                thumb1View.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        zoomImageFromThumb(thumb1View, (Bitmap)((ImageButton)findViewById(R.id.imageDetailEvent)).getTag());
                    }
                });

                // Retrieve and cache the system's default "short" animation time.
                mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);


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
        private final WeakReference<ImageButton> imageViewReference;

        public ImageTask(ImageButton imageView) {

            imageViewReference = new WeakReference<ImageButton>(imageView);
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
                final ImageButton imageView = imageViewReference.get();
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
