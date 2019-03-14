package com.example.socketservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SocketService socketService;
    private Boolean isServiceBound = false;
    private ServiceConnection socketServiceConnection;
    private Intent socketServiceIntent;
    private Boolean runDone;
    private Boolean isServiceStarted = false;
    private static int waypoint = -1;
    private int status = 0;


    //local gui variables
    private Button startBtn, cancelBtn, initialBtn, disconnectBtn;// initializeButton;
    private Spinner selectDestList, selectStartList;
    private TextView debugTv, gpsTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize local gui vars
        startBtn = (Button) findViewById(R.id.startButton);
        cancelBtn = (Button)  findViewById(R.id.cancelButton);
        initialBtn = (Button)  findViewById(R.id.intitializeButton);
        disconnectBtn = (Button) findViewById(R.id.disconnectButton);
        //initializeButton = (Button) findViewById(R.id.initializeButton);
        debugTv = (TextView) findViewById(R.id.debugTextView);
        debugTv.setMovementMethod(new ScrollingMovementMethod());
        gpsTv = (TextView)findViewById(R.id.gps_ui_output);
        selectDestList = (Spinner) findViewById(R.id.selectDestList);
        selectStartList = (Spinner) findViewById(R.id.selectDestList2);

        //set up spinner-dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.destListArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectDestList.setAdapter(adapter);
        selectStartList.setAdapter(adapter);


        //send all onClicks to local switch statement
        startBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        initialBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);

        socketServiceIntent = new Intent(this, SocketService.class);



        Thread threadUpdateTextViews = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateGpsTextView();
                                updateDebugTextView();
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
                                    status = socketService.getCommStage();
                                    switch (status){





                                        case 1:
                                            disableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        case 2:
                                            disableConnectBtn();
                                            enableStartBtn();
                                            disableCancelBtn();
                                            enableDisconnectBtn();
                                            break;
                                        case 3:
                                            disableConnectBtn();
                                            disableStartBtn();
                                            enableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        case 4:
                                            enableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                        default:
                                            enableConnectBtn();
                                            disableStartBtn();
                                            disableCancelBtn();
                                            disableDisconnectBtn();
                                            break;
                                    }

                                } else{ //no socket so only connect enabled and colored
                                    startUpButtonConfig();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        threadCheckCommStatus.start();


    }

    private void updateGpsTextView(){
        if(isServiceBound){
            gpsTv.setText(socketService.getLatLonString());
        }else{
            gpsTv.setText("GPS: None Available");
        }
    }

    private void updateDebugTextView() {
        if (isServiceBound){
            debugTv.append(socketService.getDebugMesseges());
            socketService.resetDebugMessages();
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.intitializeButton: //TODO - Connect Button.
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

                //disconnect service stuff
//                if(isServiceBound) {
//                    if(socketService.getRunDone()){
//                        String Message = socketService.serverSaysWhat();
//                        if(Message != null) {
//                            debugTv.append("Server Says" + Message);
//                        }else{
//                            debugTv.append("Server Say Null");
//                        }
//                    }
//                    stopService(socketServiceIntent);
//                    isServiceStarted = false;
//                    debugTv.append("Service stopped");
//                } else {
//                    debugTv.append("disconnect:Service Not Bound");
//                }

                //restart connection in prep for new connection
                //maybe later have dialog asking if you want to start a new one or close app (if close app picked then close the app automatically)
                //startConnection();
                break;
            case R.id.emergancyLandButton:
                socketService.setEmergStopButtonPressed(true);
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
        //dest waypoint
        String destName = selectDestList.getSelectedItem().toString();
        if (destName.equals("(SSC) Stevens Student Center")){
            socketService.setDestWaypointNum(0);
            //debugTextView.append("\n SSC");
        }else if (destName.equals("(DMC) Dixon Ministry Center")){
            socketService.setDestWaypointNum(9);
            //debugTextView.append("\n DMC");
        }else if (destName.equals("(BTS) Center for Biblical and Theological Studies")){
            socketService.setDestWaypointNum(11);
            //debugTextView.append("\n BTS");
        }else if (destName.equals("(ENS) Engineering and Science Center")){
            socketService.setDestWaypointNum(22);
            //debugTextView.append("\n ENS");
        }else if (destName.equals("(HSC) Health and Science Center")){
            socketService.setDestWaypointNum(28);
            //debugTextView.append("\n HSC");
        }


//        //start waypoint
//        String startName = selectStartList.getSelectedItem().toString();
//        if (startName.equals("(SSC) Stevens Student Center")){
//            socketService.setStartWaypointNum(0);
//            //debugTextView.append("\n SSC");
//        }else if (startName.equals("(DMC) Dixon Ministry Center")){
//            socketService.setStartWaypointNum(9);
//            //debugTextView.append("\n DMC");
//        }else if (startName.equals("(BTS) Center for Biblical and Theological Studies")){
//            socketService.setStartWaypointNum(11);
//            //debugTextView.append("\n BTS");
//        }else if (startName.equals("(ENS) Engineering and Science Center")){
//            socketService.setStartWaypointNum(22);
//            //debugTextView.append("\n ENS");
//        }else if (startName.equals("(HSC) Health and Science Center")){
//            socketService.setStartWaypointNum(28);
//            //debugTextView.append("\n HSC");
//        }

    }

    private void startConnection(){
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if(isServiceStarted) {
            if (isServiceBound) {
                debugTv.setText("Service already Bound");
            } else {
                bindService();
                debugTv.setText("Service wasn't bound, bounding initiated, check again");
            }
        } else{
            startService(socketServiceIntent);
            bindService();
            isServiceStarted = true;
        }
    }

    //have socket set disable/enable ^^ ?? I think when we get there yes. have stage 1 2 3 etc with lists of what to enable/disable
    private void disableConnectBtn () {
        initialBtn.setEnabled(false);
        initialBtn.setBackgroundColor(0x51ededed); //51EDEDED
    }

    private void enableConnectBtn (){
        initialBtn.setEnabled(true);
        if (!socketService.getInitialButtonPressed()) {
            initialBtn.setBackgroundColor(0xffe6caf2);
        } else {
            initialBtn.setBackgroundColor(0xaae3d1eb);
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

    private void startUpButtonConfig(){
        initialBtn.setEnabled(true);
        initialBtn.setBackgroundColor(0xffe6caf2);

        startBtn.setEnabled(false);
        startBtn.setBackgroundColor(0x51ededed);

        cancelBtn.setEnabled(false);
        cancelBtn.setBackgroundColor(0x51ededed);

        disconnectBtn.setEnabled(false);
        disconnectBtn.setBackgroundColor(0x51ededed);
    }

}
