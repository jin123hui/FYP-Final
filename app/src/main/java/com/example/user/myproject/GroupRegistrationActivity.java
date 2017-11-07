package com.example.user.myproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.myproject.Modal.Action;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupRegistrationActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Student> arrayList;
    StudentListView studentListView;
    String leaderName = "";
    String leaderId = "";
    int seatAvailable = 0;
    int timetableId = 0;
    String registrationDescription = "";
    ProgressDialog pd;


    MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("Group Registration");

        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                slotCountRefresh();
                if(arrayList == null){
                    Toast.makeText(getApplicationContext(),"Group information invalid!! cannot insert studnent",Toast.LENGTH_LONG).show();
                }else if (arrayList.size() >= seatAvailable){
                    Toast.makeText(getApplicationContext(),"Student is full, cannot add more student",Toast.LENGTH_LONG).show();
                }else{
                    dialog2();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if(!bundle.isEmpty()){
            try {
                seatAvailable = Integer.parseInt(bundle.getString("SEATAVAILABLE"))-1;
                if(seatAvailable < 0)
                    seatAvailable = 0;
            }catch (Exception ex){
                seatAvailable = 0;
            }
            leaderName = bundle.getString("STUDENTNAME");
            leaderId = bundle.getString("STUDENTID");
            try {
                timetableId = Integer.parseInt(bundle.getString("TIMETABLEID"));
            }catch (Exception ex){
                timetableId = 0;
            }
            registrationDescription = bundle.getString("REG_DESCRIPTION");
        }else{
            seatAvailable = 0 ;
            leaderId = "Nothing";
            leaderName = "Nothing";
            timetableId = 0;
            registrationDescription = "";
            Toast.makeText(getApplicationContext(),"No student's data found, return to home page!!",Toast.LENGTH_LONG).show();

            finish();
        }

        TextView leaderIdText = (TextView)findViewById(R.id.txtGroupLeaderStudentId);
        leaderIdText.setText(leaderId.toUpperCase());

        TextView leaderNameText = (TextView)findViewById(R.id.txtGroupLeaderStudentName);
        leaderNameText.setText(leaderName);

        TextView countText = (TextView)findViewById(R.id.txtGroupSlotCount);
        countText.setText("Slot available:" + "0 / " + seatAvailable );

        loadRegistration();
    }


    @Override
    protected void onStart() {
        super.onStart();

        conn();

    }

    public void slotCountRefresh(){
        if (arrayList != null ) {
            TextView countText = (TextView) findViewById(R.id.txtGroupSlotCount);
            countText.setText("Slot available:" + arrayList.size()+ " / " + seatAvailable);

        }
    }

    public void loadRegistration(){
        arrayList = new ArrayList<>();


        listView = (ListView)findViewById(R.id.groupRegistrationList);
        studentListView = new StudentListView(this, R.layout.studentlist_layout,arrayList);
        listView.setAdapter(studentListView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            MenuItem selectMenu = null;
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {



                final int checkedCount = listView.getCheckedItemCount();
                actionMode.setTitle(checkedCount + " Selected");
                studentListView.toggleSelection(i);



                ArrayList<Integer> selectedIds = studentListView.selectedIds;
                Integer pos = new Integer(i);
                if(selectedIds.contains(pos)) {
                    selectedIds.remove(pos);
                }else {
                    selectedIds.add(pos);
                }


                studentListView.notifyDataSetChanged();
                if(studentListView.getStudentList().size() == studentListView.selectedIds.size()){
                    if(selectMenu!= null) {

                        selectMenu.setTitle("Unselect All");
                    }

                }else{
                    if(selectMenu!= null) {

                        selectMenu.setTitle("Select All");
                    }

                }
                String ss = "";

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.group_menu,menu);
                selectMenu = menu.findItem(R.id.selectAll_item);
                getSupportActionBar().hide();

                studentListView.checking = true;
                studentListView.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {


                switch (menuItem.getItemId()){
                    case R.id.selectAll_item:
                        String choice = menuItem.getTitle().toString();
                        if(choice.equals("Select All")){
                            studentListView.checkAll();
                            menuItem.setTitle("Unselected All");
                        }else{
                            studentListView.uncheckAll();
                            menuItem.setTitle("Select All");
                        }


                        return true;
                    case R.id.delete_item:
                        SparseBooleanArray selected = studentListView.getSparseBooleanArray();
                        for(int index = (selected.size() -1 ) ; index >= 0; index--) {
                            if(selected.valueAt(index)){
                                Student selectedItem = studentListView.getItem(selected.keyAt(index));
                                studentListView.remove(selectedItem);
                            }

                        }
                        slotCountRefresh();
                        actionMode.finish();
                        return true;
                    default:
                        return false;

                }



            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                ArrayList<Integer> selectedIds = studentListView.selectedIds;
                selectedIds.clear();
                getSupportActionBar().show();
                studentListView.checking = false;
                studentListView.removeSelection();
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_registration_menu, menu);

        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_group_register:
                if(arrayList.size() < seatAvailable){
                    Toast.makeText(getApplicationContext(),"Cannot perform group registration because the team do not reach maximum amount of student!",Toast.LENGTH_LONG).show();
                    break ;
                }else if(seatAvailable == 0){
                    Toast.makeText(getApplicationContext(),"Cannot register because it is empty!!",Toast.LENGTH_LONG).show();
                    break ;
                }

                pd = new ProgressDialog(GroupRegistrationActivity.this);
                pd.setMessage("Registering...");
                pd.show();
                ArrayList<EventRegistration> arrEventReg = new ArrayList<>();

                final EventRegistration leaderReg = new EventRegistration();
                leaderReg.setTimetableId(timetableId);
                leaderReg.setStudentId(leaderId);
                leaderReg.setLeaderId(leaderId);
                leaderReg.setDescription(registrationDescription);
                arrEventReg.add(leaderReg);

                for(Student stud: arrayList){
                    EventRegistration temp = new EventRegistration();
                    temp.setStudentId(stud.getStudentId());
                    temp.setTimetableId(timetableId);
                    temp.setLeaderId(leaderId);
                    temp.setDescription(registrationDescription);
                    arrEventReg.add(temp);
                }
                JSONArray jsonArr = new JSONArray();
                try {
                    for (EventRegistration regTemp : arrEventReg) {
                        JSONObject obj = new JSONObject();
                        obj.put("timetableId", regTemp.getTimetableId());
                        obj.put("studentId",regTemp.getStudentId());
                        obj.put("leaderId",regTemp.getLeaderId());
                        obj.put("description",registrationDescription);
                        jsonArr.put(obj);

                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                //Toast.makeText(this, jsonArr.toString(), Toast.LENGTH_SHORT).show();
                publishMessage(Action.combineMessage("001608", Action.asciiToHex(jsonArr.toString())));
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
                        String decoded = Action.hexToAscii(strMessage);
                        JSONObject obj = new JSONObject(decoded);

                        int response = obj.getInt("rowAffected");
                        Toast.makeText(getApplicationContext(),"Number of student registered: "+response,Toast.LENGTH_LONG).show();

                        pd.dismiss();
                        Intent intent = new Intent(getApplicationContext(), Homepage.class);
                        intent.putExtra("lastActivity", "third");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        String s = "";
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }

                });


                break;
            case R.id.home:
                this.finish();
                break;
            case android.R.id.home:
                this.finish();
                break;

        }
        return true;
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



        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //dialog.cancel();
            }
        });

        // Setting Positive "OK" Button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                pd = new ProgressDialog(GroupRegistrationActivity.this);
                pd.setMessage("Loading");
                pd.show();

                if(leaderId.equals(edit.getText().toString())){
                    Toast.makeText(getApplicationContext(),"You cannot add leader id into member list again.",Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return ;
                }

                if(arrayList.contains(new Student(edit.getText().toString()))){
                    Toast.makeText(getApplicationContext(),"The student information is in the list!! Cannot added anymore",Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return ;
                }


                //final DialogInterface tempDialog = dialog;
                String studentId = edit.getText().toString();
                JSONObject obj = new JSONObject();
                try{
                    obj.put("leaderId", leaderId);
                    obj.put("studentId",studentId);
                    obj.put("timetableId",timetableId);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }

                publishMessage(Action.combineMessage("001607", Action.asciiToHex(obj.toString())));


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
                            Toast.makeText(getApplicationContext(),resultMessage,Toast.LENGTH_LONG).show();


                        }else{
                            String studentName = obj.getString("studentName");
                            String studentId = edit.getText().toString().toLowerCase();
                            arrayList.add(new Student(studentId,studentName));
                            studentListView.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),"Student added successful!!",Toast.LENGTH_LONG).show();
                            //  tempDialog.cancel();
                        }
                        pd.dismiss();
                        slotCountRefresh();
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


                slotCountRefresh();
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
                        client.subscribe(Action.clientTopic+leaderId, 1);


                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }

                    // We are connected
                    //Log.d(TAG, "onSuccess");
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

        //String message = text.getText().toString();
        try {
            byte[] ss= message.getBytes();
            client.publish(Action.serverTopic, message.getBytes(), 0, false);
            // Toast.makeText(getApplicationContext(), "publish success l!!", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void subscribeMessage(){
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
                    String studentName = obj.getString("studentName");

                }





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
