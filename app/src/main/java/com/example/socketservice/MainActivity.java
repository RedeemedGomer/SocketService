package com.example.socketservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SocketService socketService;
    private boolean isServiceBound = false;
    private ServiceConnection socketServiceConnection;
    private Intent socketServiceIntent;
    private Boolean runDone;


    //local gui variables
    private Button startBtn, stopBtn;
    private TextView printTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize local gui vars
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button)  findViewById(R.id.stopBtn);
        printTv = (TextView) findViewById(R.id.printTV);

        //send all onClicks to local switch statement
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        socketServiceIntent = new Intent(this, SocketService.class);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.startBtn:
                startService(socketServiceIntent);
                bindService();
                printTv.setText("started and bound service");
                break;

            case R.id.stopBtn:
                if(isServiceBound) {
                    if(socketService.getRunDone()) {
                        String serverSaid = socketService.serverSaysWhat();
                        String gps = socketService.getLatLon();
                        unbindService();
                        stopService(socketServiceIntent);
                        printTv.setText("GPS-> "+ gps );
                        printTv.append("Server said: \n" + serverSaid);
                    } else {
                        printTv.setText("Run Not Done");
                    }
                } else {
                    printTv.setText("Service Not Bound");
                }
                break;
        }
    }

    private void bindService(){
        if(socketServiceConnection == null){
            socketServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
                    SocketService.myBinder myBinder = (SocketService.myBinder)serviceBinder;
                    socketService = ((SocketService.myBinder) serviceBinder).getService();
                    isServiceBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    isServiceBound = false;
                }
            };

            bindService(socketServiceIntent, socketServiceConnection, Context.BIND_AUTO_CREATE);//TODO check if bind auto create is what I actually want
        }
    }

    private void unbindService(){
        if(isServiceBound){
            unbindService(socketServiceConnection);
            isServiceBound=false;
        }
    }

}
