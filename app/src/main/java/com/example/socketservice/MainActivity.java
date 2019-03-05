package com.example.socketservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


    //local gui variables
    private Button startBtn, cancelBtn, initialBtn, disconnectBtn;// initializeButton;
    private Spinner selectDestList;
    private TextView printTv, gpsTv;

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
        printTv = (TextView) findViewById(R.id.debugTextView);
        gpsTv = (TextView)findViewById(R.id.gps_ui_output);
        selectDestList = (Spinner) findViewById(R.id.selectDestList);

        //set up spinner-dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.destListArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectDestList.setAdapter(adapter);

        //send all onClicks to local switch statement
        startBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        initialBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);

        socketServiceIntent = new Intent(this, SocketService.class);

        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateGpsTextView();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();

        //start socket connection
        startConnection();

    }

    private void updateGpsTextView(){
        if(isServiceBound){
            gpsTv.setText(socketService.getLatLonString());
        }else{
            gpsTv.setText("GPS: None Available");
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.intitializeButton:
                //intiaize all info
                socketService.setInitialButtonPressed(true);
                getDestWaypointFromDropDown();

                break;

            case R.id.startButton:
                if(isServiceBound){
                    String Message = socketService.getLatLonString();
                    printTv.append("dest waypoint = " + socketService.getDestWaypointNum() + "\n");
                    gpsTv.setText(Message);
                }else{

                    printTv.setText("start: Service wasn't bound");
                }
                break;

            case R.id.cancelButton:
                if(isServiceBound) {
                    unbindService();
                    printTv.setText("Service unbound");
                } else {
                    printTv.setText("cancel:Service Not Bound");
                }
                break;

            case R.id.disconnectButton:
                if(isServiceBound) {
                    if(socketService.getRunDone()){
                        String Message = socketService.serverSaysWhat();
                        if(Message != null) {
                            printTv.setText("Server Says" + Message);
                        }else{
                            printTv.setText("Server Say Null");
                        }
                    }
                    stopService(socketServiceIntent);
                    isServiceStarted = false;
                    printTv.setText("Service stopped");
                } else {
                    printTv.setText("disconnect:Service Not Bound");
                }

                //restart connection in prep for new connection
                //maybe later have dialog asking if you want to start a new one or close app (if close app picked then close the app automatically)
                startConnection();
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

    private void getDestWaypointFromDropDown(){
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
    }

    private void startConnection(){
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if(isServiceStarted) {
            if (isServiceBound) {
                printTv.setText("Service already Bound");
            } else {
                bindService();
                printTv.setText("Service wasn't bound, bounding initiated, check again");
            }
        } else{
            startService(socketServiceIntent);
            bindService();
            isServiceStarted = true;
        }
    }



}
