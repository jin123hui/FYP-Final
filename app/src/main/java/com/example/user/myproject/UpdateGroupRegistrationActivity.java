package com.example.user.myproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.EncodedApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.example.user.myproject.Modal.Homepage;
import com.example.user.myproject.Modal.SessionManager;
import com.example.user.myproject.Modal.Student;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdateGroupRegistrationActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Student> arrayList;
    StudentListView studentListView;
    String leaderId = "";
    int seatAvailable = 0;
    int timetableId = 0;
    ProgressDialog pd;




    MqttAndroidClient client;
    MqttAndroidClient client2;
    MqttConnectOptions options = new MqttConnectOptions();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        setTitle("Update Group Registration");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(arrayList == null){
                    Toast.makeText(getApplicationContext(),"Group information invalid!! cannot insert studnent",Toast.LENGTH_LONG).show();
                }else if (arrayList.size() >= seatAvailable){
                    Toast.makeText(getApplicationContext(),"Student is full, cannot add more student",Toast.LENGTH_LONG).show();
                }else{
                    dialog2();
                }
            }
        });




    }



    @Override
    protected void onStart() {
        super.onStart();

        Bundle bundle = getIntent().getExtras();
        if(!bundle.isEmpty()){

            leaderId = bundle.getString("LEADERID");
            try {
                timetableId = Integer.parseInt(bundle.getString("TIMETABLEID"));
            }catch (Exception ex){
                timetableId = 0;
            }

        }else{
            leaderId = "Nothing";

            timetableId = 0;

            Toast.makeText(getApplicationContext(),"No student's data found, return to home page!!",Toast.LENGTH_LONG).show();

            finish();
        }





        options.setUserName(Action.MQTT_USERNAME);
        options.setPassword(Action.MQTT_PASSWORD.toCharArray());
        options.setCleanSession(true);
        loadRegistration();
        conn1();

        connLoadCount();



    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group_registration_update_context_menu,menu);
    }


    public void slotCountRefresh(){
        if (arrayList != null ) {
            TextView countText = (TextView) findViewById(R.id.txtGroupSlotCount);
            countText.setText("Slot available:" + arrayList.size()+ " / " + seatAvailable);

        }
    }

    public void loadRegistration(){
        arrayList = new ArrayList<>();

        listView = (ListView)findViewById(R.id.updateGroupRegistrationList);
        studentListView = new StudentListView(this, R.layout.studentlist_layout,arrayList);
        listView.setAdapter(studentListView);

        registerForContextMenu(listView);



    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final int index = info.position;
        AlertDialog.Builder alert = new AlertDialog.Builder(UpdateGroupRegistrationActivity.this);

        switch (item.getItemId()) {
            case R.id.delete_student:

                alert.setTitle("Cancel Registration");
                alert.setMessage("Confirm to cancel your registration?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //pd = new ProgressDialog(getApplicationContext());
                        // pd.setMessage("Loading");
                        //   pd.show();

                        JSONObject obj = new JSONObject();
                        try{
                            //obj.put("registrationId", arrayList.get(index).getRegistrationId());
                            obj.put("timetableId", timetableId);
                            obj.put("leaderId", leaderId);
                            obj.put("studentId", arrayList.get(index).getStudentId());
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        try {
                            client.subscribe(Action.clientTopic+leaderId, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001627",Action.asciiToHex(obj.toString())));
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
                                String messages = obj.getString("message");

                                if(obj.getString("success").equals("1")){
                                    Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                    arrayList.remove(index);
                                    studentListView.notifyDataSetChanged();
                                }else{
                                    Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                }


                                slotCountRefresh();

                                // pd.dismiss();
                                //finish();

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
            case R.id.set_as_party_leader:


                alert.setTitle("Change leader");
                alert.setMessage("Confirm to change the leader? (you cannot perform group editing anymore!)");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //pd = new ProgressDialog(getApplicationContext());
                        // pd.setMessage("Loading");
                        //   pd.show();

                        JSONObject obj = new JSONObject();
                        try{
                            //obj.put("registrationId", arrayList.get(index).getRegistrationId());
                            obj.put("timetableId", timetableId);
                            obj.put("oldLeaderId", leaderId);
                            obj.put("newLeaderId", arrayList.get(index).getStudentId());
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        try {
                            client.subscribe(Action.clientTopic+leaderId, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001628",Action.asciiToHex(obj.toString())));
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
                                String messages = obj.getString("message");

                                if(obj.getString("success").equals("1")){
                                    Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();

                                    /***********/
                                    // change here jump to next activity
                                    Intent intent = new Intent(getApplicationContext(), Homepage.class);
                                    intent.putExtra("lastActivity", "third");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }else{
                                    Toast.makeText(getApplicationContext(), messages, Toast.LENGTH_LONG).show();
                                }


                                slotCountRefresh();

                                // pd.dismiss();
                                //finish();

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
            default:
                return super.onContextItemSelected(item);
        }
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


    public void dialog2(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2,2,2,2);

        TextView tv = new TextView(this);
        tv.setText("Group registration student id");
        tv.setPadding(40,40,40,40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);


        final EditText edit = new EditText(this);
        edit.setHint("eg 16war11111");
        String studentID = edit.getText().toString();

        TextView tv1 = new TextView(this);
        tv1.setText("Input Student ID");


        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1,tv1Params);
        layout.addView(edit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle("Student information");

        alertDialogBuilder.setCustomTitle(tv);

        final String tempStudentId = studentID;


        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //dialog.cancel();
            }
        });

        // Setting Positive "OK" Button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                pd = new ProgressDialog(UpdateGroupRegistrationActivity.this);
                // pd.setMessage("Loading");
                // pd.show();

                if(leaderId.equals(edit.getText().toString())){
                    Toast.makeText(getApplicationContext(),"You cannot add leader id into member list again.",Toast.LENGTH_LONG).show();
                    //  pd.dismiss();
                    return ;
                }

                if(arrayList.contains(new Student(edit.getText().toString()))){
                    Toast.makeText(getApplicationContext(),"The student information is in the list!! Cannot added anymore",Toast.LENGTH_LONG).show();
                    //  pd.dismiss();
                    return ;
                }



                //final DialogInterface tempDialog = dialog;
                String tempStudentId = edit.getText().toString();
                JSONObject obj = new JSONObject();
                try{
                    obj.put("studentId",tempStudentId.toLowerCase());
                    obj.put("timetableId",timetableId);
                    obj.put("waitinglistStatus","");
                    obj.put("leaderId",leaderId);
                    obj.put("description","");
                    // Toast.makeText(UpdateGroupRegistrationActivity.this,"resiss" +","+ tempStudentId +","+ timetableId +","+ leaderId, Toast.LENGTH_LONG).show();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                try {
                    client.subscribe(Action.clientTopic+leaderId, 1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                publishMessage(Action.combineMessage("001626",Action.asciiToHex(obj.toString())));
                if (client == null ){
                    Toast.makeText(UpdateGroupRegistrationActivity.this, "Connection fail!!", Toast.LENGTH_LONG).show();
                }


                if (client == null ){
                    Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
                }
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                        String s = "";
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String strMessage = new String(message.getPayload());
                        String decoded = Action.hexToAscii(strMessage);
                        JSONObject obj = new JSONObject(decoded);
                        int response = obj.getInt("success");
                        String resultMessage = obj.getString("message");
                        if(response == 0){
                            Toast.makeText(UpdateGroupRegistrationActivity.this,resultMessage,Toast.LENGTH_LONG).show();


                        }else{
                            String studentName = obj.getString("studentName");
                            String studentId = edit.getText().toString();
                            arrayList.add(new Student(studentId,studentName));
                            studentListView.notifyDataSetChanged();
                            slotCountRefresh();
                            Toast.makeText(UpdateGroupRegistrationActivity.this,"Student added successful!!",Toast.LENGTH_LONG).show();
                            //  tempDialog.cancel();
                        }
                        // pd.dismiss();
                        String s = "";
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        //Toast.makeText(getApplicationContext(), "All message received!!", Toast.LENGTH_LONG).show();
                        String str = "";
                        try {
                            str = new String(token.getMessage().getPayload());
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        String sssss = "";
                    }

                });

                //dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void conn1(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), Action.MQTT_ADDRESS,
                clientId);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connected!!", Toast.LENGTH_LONG).show();
                    try {

                        client.subscribe(Action.clientTopic+leaderId, 1);


                        JSONObject obj = new JSONObject();
                        try{

                            obj.put("studentId", leaderId);

                            obj.put("leaderId",leaderId);

                            obj.put("timetableId",timetableId);
                            publishMessage(Action.combineMessage("001623",Action.asciiToHex(obj.toString())));

                            if (client == null ){
                                Toast.makeText(getApplicationContext(), "Retrieve group information failed", Toast.LENGTH_LONG).show();
                            }
                            //obj.put("registrationId", reg.getRegistrationId());
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {

                                String strMessage = new String(message.getPayload());
                                String decoded = Action.hexToAscii(strMessage);

                                JSONArray arr = new JSONArray(decoded);
                                for(int index = 0; index < arr.length(); index++){
                                    JSONObject obj = arr.getJSONObject(index);


                                    if(obj.getString("studentId").equalsIgnoreCase(leaderId)){

                                        TextView leaderIdText = (TextView)findViewById(R.id.txtGroupLeaderStudentId);
                                        leaderIdText.setText(obj.getString("studentId"));

                                        TextView leaderNameText = (TextView)findViewById(R.id.txtGroupLeaderStudentName);
                                        leaderNameText.setText(obj.getString("name"));
                                    }else {
                                        arrayList.add(new Student( obj.getString("studentId"), obj.getString("name")));
                                    }
                                    studentListView.notifyDataSetChanged();
                                }


                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });


                        // subscribeRegMessage();
                        //checkAttendance();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void publishMessage(String message) {

        //String message = text.getText().toString();
        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            // Toast.makeText(getApplicationContext(), "publish success l!!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }



    public void connLoadCount(){
        String clientId = MqttClient.generateClientId();
        client2 =
                new MqttAndroidClient(this.getApplicationContext(), Action.mqttTest,
                        clientId);

        try {
            IMqttToken token = client2.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(DetailEventActivity.this, "Connected!!", Toast.LENGTH_LONG).show();
                    try {
                        client2.subscribe(Action.clientTopic+leaderId, 1);

                        JSONObject obj = new JSONObject();
                        try{
                            obj.put("studentId",leaderId);
                            obj.put("timetableId",timetableId);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        publishMessage(Action.combineMessage("001604",Action.asciiToHex(obj.toString())));
                        //subscribeEventMessage();
                        if (client2 == null ){
                            Toast.makeText(getApplicationContext(), "Connection fail!!", Toast.LENGTH_LONG).show();
                        }
                        client2.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                                String s = "";
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                String strMessage = new String(message.getPayload());
                                strMessage = Action.hexToAscii(strMessage);
                                JSONObject obj = new JSONObject(strMessage);
                                String ssss = obj.getString("message");

                                if (obj.getString("success").equals("1")){
                                    // EncodedApplicationEvent event = new EncodedApplicationEvent();
                                    //event.setTimetableId(obj.getString("timetableId"));
                                    try {
                                        seatAvailable = Integer.parseInt(obj.getString("minTeam"));
                                    }catch (Exception ex){
                                        seatAvailable = 0;
                                        Toast.makeText(getApplicationContext(), "Error when loading event slot!", Toast.LENGTH_SHORT).show();
                                        //finish();
                                    }

                                    slotCountRefresh();

                                }else{
                                    Toast.makeText(getApplicationContext(),"Data retrieve failed!! Please contact admin." + strMessage,Toast.LENGTH_LONG).show();;
                                    finish();

                                }


                                disconnect2();

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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
        disconnect2();
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

    public void disconnect2(){
        try {
            IMqttToken disconToken = client2.disconnect();
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
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



}