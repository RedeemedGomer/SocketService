package com.example.socketservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SocketService socketService;
    private Boolean isServiceBound = false;
    private ServiceConnection socketServiceConnection;
    private Intent socketServiceIntent;
    private Boolean isServiceStarted = false;
    private int stage = 0;
    //private boolean showInstr = false;
    private boolean stage2InstrShown = false;
    private boolean stage3InstrShown = false;
    private boolean stage4InstrShown = false;


    //local gui variables
    private Button startBtn, cancelBtn, connectBtn, disconnectBtn;
    private Spinner selectDestList;
    private TextView gpsTvLat, gpsTvLon, statusTv, destLabelTv, droneLatTv, droneLonTv, droneAltTv, droneVelTv, droneHeadTv, instrLabel, chosenDestTv;
    private Switch instrSwitch;

    //GPS variables
    LocationTrackService locationTrackServe;
    LocationManager LocMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize local gui vars
        startBtn = (Button) findViewById(R.id.startButton);
        cancelBtn = (Button)  findViewById(R.id.cancelButton);
        connectBtn = (Button)  findViewById(R.id.connectButton);
        disconnectBtn = (Button) findViewById(R.id.disconnectButton);
        selectDestList = (Spinner) findViewById(R.id.selectDestList);
        instrSwitch = (Switch) findViewById(R.id.instrSwitch);
        instrLabel = (TextView) findViewById(R.id.instrLabelText);
        gpsTvLat = (TextView)findViewById(R.id.latTextPh);
        gpsTvLon = (TextView)findViewById(R.id.lonTextPh);
        statusTv = (TextView) findViewById(R.id.statusText);
        destLabelTv = (TextView) findViewById(R.id.destLabel);
        droneLatTv = (TextView) findViewById(R.id.droneLatText);
        droneLonTv = (TextView) findViewById(R.id.droneLonText);
        droneAltTv = (TextView) findViewById(R.id.droneAltText);
        droneVelTv = (TextView) findViewById(R.id.droneVelText);
        droneHeadTv = (TextView) findViewById(R.id.droneHeadText);
        chosenDestTv = (TextView) findViewById(R.id.chosenDestTextView);

        //set up spinner-dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.destListArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectDestList.setAdapter(adapter);

        //send all onClicks to local switch statement
        startBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        connectBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);

        //set up switch
        instrSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    instrLabel.setText("ON");

                } else {
                    instrLabel.setText("OFF");

                }
            }
        });

        socketServiceIntent = new Intent(this, SocketService.class);

        Thread threadUpdateTextViews = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateGps();
                                //updateDebugTextView();
                                if (socketService != null) {
                                    if (!socketService.getStatusText().equals("")) {
                                        updateStatusTextView(socketService.getStatusText());
                                        updateDroneDataTextViews();
                                        socketService.setInstrSwitchValue(Boolean.valueOf(instrSwitch.isChecked()));
                                    }
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        threadUpdateTextViews.start();

        Thread threadCheckCommStatus = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (socketService!= null) {
                                    stage = socketService.getCommStage();
                                    switch (stage){
                                        case 1:
                                            //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                            stage2InstrShown = false;
                                            stage3InstrShown= false;
                                            stage4InstrShown = false;

                                            enableDestSelect();
                                            disableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        case 2:
                                            //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                            if (instrSwitch.isChecked() && !stage2InstrShown) {
                                                Toast.makeText(getApplicationContext(), "Choose a destination. When your ready to go press the Start button.", Toast.LENGTH_LONG).show();
                                                Toast.makeText(getApplicationContext(), "If you are finished using the app or wish to stop phone/drone communication press the Disconnect button.", Toast.LENGTH_LONG).show();
                                                stage2InstrShown = true;
                                            }
                                            stage3InstrShown= false;
                                            stage4InstrShown = false;

                                            enableDestSelect();
                                            disableConnectBtn();
                                            enableStartBtn();
                                            disableCancelBtn();
                                            enableDisconnectBtn();
                                            break;
                                        case 3:
                                            //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                            if (instrSwitch.isChecked() && !stage3InstrShown){
                                                Toast.makeText(getApplicationContext(),"Follow the drone to reach your destination. If the drone gets to far ahead it will stop and wait for you.",Toast.LENGTH_LONG).show();
                                                Toast.makeText(getApplicationContext(),"If you wish to cancel the journey at any time press the Cancel button and the drone will land in a few moments.",Toast.LENGTH_LONG).show();
                                                stage3InstrShown = true;
                                            }
                                            stage2InstrShown = false;
                                            stage4InstrShown = false;

                                            disableDestSelect();
                                            disableConnectBtn();
                                            disableStartBtn();
                                            enableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        case 4:
                                            //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                            if (instrSwitch.isChecked() && !stage4InstrShown){
                                                Toast.makeText(getApplicationContext(),"You may now safely close the app.",Toast.LENGTH_LONG).show();
                                                Toast.makeText(getApplicationContext(),"If you wish to go somewhere else, press the Connect button to begin the process again.",Toast.LENGTH_LONG).show();
                                                stage4InstrShown = true;
                                            }
                                            stage2InstrShown = false;
                                            stage3InstrShown= false;

                                            enableDestSelect();
                                            enableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        case 5: //ERROR, only happens when socket disconnects
                                            Toast.makeText(getApplicationContext(),"Error: app/drone communication ceased. Socket disconnect.",Toast.LENGTH_LONG).show();
                                            Toast.makeText(getApplicationContext(),"Either phone out of communication range or remote override used.",Toast.LENGTH_LONG).show();
                                            Toast.makeText(getApplicationContext(),"App is now ready to try to re-establish communication with the drone. Press the Connect button.",Toast.LENGTH_LONG).show();
                                            break;
                                        default:
                                            //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                            stage2InstrShown = false;
                                            stage3InstrShown= false;
                                            stage4InstrShown = false;

                                            enableDestSelect();
                                            enableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                    }
                                } else{
                                    //Toast.makeText(getApplicationContext(), String.valueOf(showInstr), Toast.LENGTH_SHORT).show();
                                    clearDroneDataTextView();
                                    statusTv.setText("ready to connect");
                                    startUpUIConfig();
                                    enableDestSelect();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        threadCheckCommStatus.start();

        ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION },
                1);

        //start GPS
        locationTrackServe = new LocationTrackService(getApplicationContext());

        Toast.makeText(getApplicationContext(),"To get started Press the Connect button to initiate app/drone communication.",Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"You can stop receiving instructions at any time by turning the switch off in the top right of the app.",Toast.LENGTH_LONG).show();
        //"To get started Press the Connect button to initiate app/drone communication."
        // "You can stop receiving instructions at any time by turning the switch off in the top right of the app."

    }

    private void updateGps(){
        //TODO redo to get gps from locale instead of socketservice

        gpsTvLat.setText(String.valueOf(getLat()));
        gpsTvLon.setText(String.valueOf(getLon()));


        if (isServiceBound){
            //socketService.setPhoneLat();
            //socketService.setPhoneLon();
        }


//        if(isServiceBound){
//            gpsTvLat.setText(socketService.getLatString());
//            gpsTvLon.setText(socketService.getLonString());
//
//        }else{
//            gpsTvLat.setText("-");
//            gpsTvLon.setText("-");
//        }
    }

//    private void updateDebugTextView() {
//        if (isServiceBound){
//            debugTv.append(socketService.getDebugMesseges());
//            socketService.resetDebugMessages();
//        }
//    }

    private void updateStatusTextView(String s){
        if (isServiceBound){
            statusTv.setText(s);
        }
    }

    private void updateDroneDataTextViews(){
        droneAltTv.setText(String.valueOf(socketService.getDroneAlt()));
        droneLatTv.setText(String.valueOf(socketService.getDroneLat()));
        droneLonTv.setText(String.valueOf(socketService.getDroneLon()));
        droneVelTv.setText(String.valueOf(socketService.getDroneVel()));
        droneHeadTv.setText(String.valueOf(socketService.getDroneHead()));
    }

    private void clearDroneDataTextView(){
        droneAltTv.setText("-");
        droneLatTv.setText("-");
        droneLonTv.setText("-");
        droneVelTv.setText("-");
        droneHeadTv.setText("-");
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.connectButton:
                startConnection();
                break;

            case R.id.startButton:
                getWaypointFromDropDown();
                socketService.setStartButtonPressed(true);
                break;

            case R.id.cancelButton:
                socketService.setCancelButtonPressed(true);
                break;

            case R.id.disconnectButton:
                socketService.setDisconnectButtonPressedButtonPressed(true);
                stage = 4; //TODO get rid of this stage made????.

                //disconnect service
                if(isServiceBound) {
                    stopService(socketServiceIntent);
                    isServiceBound = false;
                    isServiceStarted = false;
                    //debugTv.append("Service stopped");
                } else {
                    //debugTv.append("disconnect:Service Not Bound");
                }

                startUpUIConfig();
                break;
        }

    }

    private void bindService(){
        Log.i("MA_inf", "BindService()");
        if(socketServiceConnection == null){
            socketServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
                    SocketService.myBinder myBinder = (SocketService.myBinder)serviceBinder;
                    socketService = ((SocketService.myBinder) serviceBinder).getService();
                    isServiceBound = true;
                    Log.i("MA_inf", "bindservice.onServiceConnected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i("MA_inf", "bindservice.onServiceDisconnected");
                    isServiceBound = false;
                }
            };
        }
        bindService(socketServiceIntent, socketServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        if(isServiceBound){
            unbindService(socketServiceConnection);
            isServiceBound=false;
        }
    }

    private void getWaypointFromDropDown(){
        String destName = selectDestList.getSelectedItem().toString();

        if (destName.equals("(SSC) Stevens Student Center")){

            socketService.setDestWaypointNum(0);

        }else if (destName.equals("(DMC) Dixon Ministry Center")){

            socketService.setDestWaypointNum(9);

        }else if (destName.equals("(BTS) Center for Biblical and Theological Studies")){

            socketService.setDestWaypointNum(11);

        }else if (destName.equals("(ENS) Engineering and Science Center")){

            socketService.setDestWaypointNum(22);

        }else if (destName.equals("(HSC) Health and Science Center")){

            socketService.setDestWaypointNum(28);

        }

    }

    private void startConnection(){
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if(isServiceStarted) {
//            if (isServiceBound) {
//                debugTv.append("Service already Bound");
//            } else {
//                bindService();
//                debugTv.append("Service wasn't bound, bounding initiated, check again");
//            }
        } else{
            startService(socketServiceIntent);
            bindService();
            isServiceStarted = true;
        }
    }


    private void disableConnectBtn () {
        connectBtn.setEnabled(false);
        connectBtn.setBackgroundColor(0x51ededed);
    }

    private void enableConnectBtn (){
        connectBtn.setEnabled(true);
        if (!socketService.getConnectButtonPressed()) {
            connectBtn.setBackgroundColor(0xfff2cae8); //new: 0xfff2cae8, old: 0xffe6caf2
        } else {
            connectBtn.setBackgroundColor(0xaaebd1e4); //new: 0xaaebd1e4, old: 0xaae3d1eb
        }
    }

    private void disableStartBtn () {
        startBtn.setEnabled(false);
        startBtn.setBackgroundColor(0x51ededed);
    }

    private void enableStartBtn (){
        startBtn.setEnabled(true);
        if (!socketService.getStartButtonPressed()) {
            startBtn.setBackgroundColor(0xffbdeeba);
        } else {
            startBtn.setBackgroundColor(0xaac5e5c2);
        }
    }

    private void disableCancelBtn () {
        cancelBtn.setEnabled(false);
        cancelBtn.setBackgroundColor(0x51ededed);
    }

    private void enableCancelBtn (){
        cancelBtn.setEnabled(true);
        if (!socketService.getCancelButtonPressed()) {
            cancelBtn.setBackgroundColor(0xffeff1c6);
        } else {
            cancelBtn.setBackgroundColor(0xaae9eacd);
        }
    }

    private void disableDisconnectBtn () {
        disconnectBtn.setEnabled(false);
        disconnectBtn.setBackgroundColor(0x51ededed);
    }

    private void enableDisconnectBtn (){
        disconnectBtn.setEnabled(true);
        if (!socketService.getDisconenctButtonPressed()) {
            disconnectBtn.setBackgroundColor(0xffc6dff1);
        } else {
            disconnectBtn.setBackgroundColor(0xaacddeea);
        }
    }

    private void startUpUIConfig(){
        statusTv.setText("ready to connect");

        clearDroneDataTextView();

        connectBtn.setEnabled(true);
        connectBtn.setBackgroundColor(0xfff2cae8);

        startBtn.setEnabled(false);
        startBtn.setBackgroundColor(0x51ededed);

        cancelBtn.setEnabled(false);
        cancelBtn.setBackgroundColor(0x51ededed);

        disconnectBtn.setEnabled(false);
        disconnectBtn.setBackgroundColor(0x51ededed);
    }

    private void enableDestSelect(){
        selectDestList.setEnabled(true);
        destLabelTv.setText("Choose your destination:");
        selectDestList.setVisibility(View.VISIBLE);
        chosenDestTv.setVisibility(View.GONE);
    }

    private void disableDestSelect(){
        selectDestList.setEnabled(false);
        destLabelTv.setText("Your destination is:");
        selectDestList.setVisibility(View.INVISIBLE);
        chosenDestTv.setVisibility(View.VISIBLE);
        chosenDestTv.setText(selectDestList.getSelectedItem().toString());

    }

    ////////////////////GPS METHODS//////////////////////////////
//    public String getLatLonString(){
//        Log.i("S_update", "in getLatLon()");
//        if(locationTrackServe != null) {
//            return "Lat:" + locationTrackServe.getLatitude() + ",  Lon:" + locationTrackServe.getLongitude();
//        }else{
//            Log.d("S_debug", "LocationTrackServe was null");
//            return "Error LocationTracker is Null";
//        }
//    }
//
//    public String getLatString(){
//        Log.i("S_update", "in getLatString()");
//        if(locationTrackServe != null) {
//            return String.valueOf(locationTrackServe.getLatitude());
//        }else{
//            Log.d("S_debug", "LocationTrackServe was null");
//            return "Error LocationTracker is Null";
//        }
//    }
//
//    public String getLonString(){
//        Log.i("S_update", "in getLonString()");
//        if(locationTrackServe != null) {
//            return String.valueOf(locationTrackServe.getLongitude());
//        }else{
//            Log.d("S_debug", "LocationTrackServe was null");
//            return "Error LocationTracker is Null";
//        }
//    }


    public double getLat(){
        if(locationTrackServe != null){
            return locationTrackServe.getLatitude();
        } else{
            Log.d("S_debug", "LocationTrackServe was null");
            return 0;
        }
    }

    public double getLon(){
        if(locationTrackServe != null){
            return locationTrackServe.getLongitude();
        } else{
            Log.d("S_debug", "LocationTrackServe was null");
            return 0;
        }
    }

}
