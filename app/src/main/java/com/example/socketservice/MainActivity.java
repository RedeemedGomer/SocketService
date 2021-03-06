package com.example.socketservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SocketService socketService;
    private Boolean isServiceBound = false;
    private ServiceConnection socketServiceConnection;
    private Intent socketServiceIntent;
    private Boolean runDone;
    private Boolean isServiceStarted = false;


    //local gui variables
    private Button startBtn, cancelBtn, connectBtn, disconnectBtn;
    private TextView printTv, gpsTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize local gui vars
        startBtn = (Button) findViewById(R.id.startButton);
        cancelBtn = (Button)  findViewById(R.id.cancelButton);
        connectBtn = (Button)  findViewById(R.id.connectButton);
        disconnectBtn = (Button) findViewById(R.id.disconnectButton);
        printTv = (TextView) findViewById(R.id.debugTextView);
        gpsTv = (TextView)findViewById(R.id.gps_ui_output);

        //send all onClicks to local switch statement
        startBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        connectBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);

        socketServiceIntent = new Intent(this, SocketService.class);

//        Thread thread = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (!isInterrupted()) {
//                        Thread.sleep(1000);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateGpsTextView();
//                            }
//                        });
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };
//
//        thread.start();

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
            case R.id.connectButton:
                //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if(isServiceStarted) {
                    if (isServiceBound) {
                        printTv.setText("Service already Bound");
                    } else {
                        //                    startService(socketServiceIntent);
                        bindService();
                        printTv.setText("Service wasn't bound, bounding initiated, check again");
                    }
                } else{
                    startService(socketServiceIntent);
                    bindService();
                    isServiceStarted = true;
                }

                break;

            case R.id.startButton:
                if(isServiceBound){
                    String Message = socketService.getLatLonString();
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
//                    if(socketService.getRunDone()){
//                        String Message = socketService.serverSaysWhat();
//                        if(Message != null) {
//                            printTv.setText("Server Says" + Message);
//                        }else{
//                            printTv.setText("Server Say Null");
//                        }
//                    }
                    stopService(socketServiceIntent);
                    isServiceStarted = false;
                    printTv.setText("Service stopped");
                } else {
                    printTv.setText("disconnect:Service Not Bound");
                }
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

}
