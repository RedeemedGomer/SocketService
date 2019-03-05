package com.example.socketservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketService extends Service {
    //socket variables
    public static final String SERVERIP = "192.168.1.17";//"10.13.78.162";//TODO insert pi or computer IP
    public static final int SERVERPORT = 8010;
    Socket socket = new Socket();
     // InetAddress serverAddr; //todo remove?
    private BufferedReader reader = null;
    private OutputStream writer = null;
    private String serverSays;

    //Service Variables
    private IBinder mBinder = new myBinder();
    private Boolean runDone = false;
    private Boolean isBound = false;
    private double lat = 1234;
    private double lon = 5678;
    private int destWaypointNum = -1;
    private boolean initialButtonPressed = false;
    private boolean startButtonPressed = false;
    private boolean  cancelButtonPressed = false;
    private boolean disconnectButtonPressed = false;

    //GPS variables
    LocationTrackService locationTrackServe;//TODO does this work with service context?;
    LocationManager LocMan;

    public IBinder onBind(Intent intent) {
        System.out.println("I am in Ibinder onBind method");
        isBound = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("I am in Ibinder onRebind method");
        isBound = true;
        super.onRebind(intent);
    }

    public class myBinder extends Binder {
       public SocketService getService(){
            return SocketService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);//todo is this even needed?

        locationTrackServe = new LocationTrackService(getApplicationContext());
        Log.i("S_update", "onStartCommand");
        Runnable connect = new connectSocket();
        new Thread(connect).start();

        return START_STICKY;
    }

    public String serverSaysWhat(){
        return serverSays;
    }


    class connectSocket implements Runnable {

        @Override
        public void run() {
            try {
                //here you must put your computer's IP address.
               // serverAddr = InetAddress.getByName(SERVERIP);//todo remove?
                    Log.i("connectSocket","run(): entered");

                //create a socket to make the connection with the server
                socket = new Socket(SERVERIP, SERVERPORT);

            } catch (Exception e) {
                Log.e("S_Error", "Error from making socket", e);
                Log.i("connectSocket","run(): exception1");
            }

            try {
                Log.i("connectSocket","run(): try to make new reader");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }catch (Exception e) {
                   Log.e("S_Error", "Error from making socket reader", e);
                Log.i("connectSocket","run(): exception making reader");
            }

            try{
                Log.i("connectSocket","run(): try to make writer");
                writer = socket.getOutputStream();
            } catch (Exception e){
                Log.e("S_Error", "Error from making socket writer", e);
                Log.i("connectSocket","run(): exception making writer");
            }

            String messageToSend = "connection received!" + "\n";
            sendMessage(messageToSend);

            while (true) {
                if (initialButtonPressed) {
                    //TODO concept test, delete when used
                    Log.i("connectSocket", "run(): getting ready to read da message");
                    //serverSays = readMessage();
                    lat = locationTrackServe.getLatitude();
                    lon = locationTrackServe.getLongitude();


                    messageToSend = "Hi I am phone, my lat:" + lat + ",  my lon:" + lon + " and I'm heading to waypoint:" + destWaypointNum + "\n";
                    sendMessage(messageToSend);
                }
            }
            //runDone = true;
        }

    }

    public Boolean getRunDone(){
        return runDone;
    }
     public void sendMessage(String message){
        if (writer != null) {
            Log.i("S_update","sendMessage"+message);
            try {
                writer.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("S_error","Error writing from client to server", e);
            }

        }else{
            Log.d("S_Error","socketservice.sendMessage(): writer was null");
        }
    }

    public String getLatLonString(){
        Log.i("S_update", "in getLatLon()");
        if(locationTrackServe != null) {
            return "Lat:" + locationTrackServe.getLatitude() + ",  Lon:" + locationTrackServe.getLongitude();
        }else{
            Log.d("S_debug", "LocationTrackServe was null");
            return "Error LocationTracker is Null";
        }
    }

    public double getLat(){
        if(locationTrackServe != null){
            return locationTrackServe.getLatitude();
        } else{
            Log.d("S_debug", "LocationTrackServe was null");
            return -1111;
        }
    }

    public Boolean getIsBound(){
        return isBound;
    }


     public String readMessage(){
        String message = "";
        if (reader != null) {
            Log.i("S_info","in readMessage"+message);
            try {
                message = reader.readLine();
                Log.i("S_info", "message read:"+message);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("S_error","Error reading from server", e);
            }

        }else{
            Log.d("S_Error","socketservice.readMessage(): reader was null");
        }
        return message;
    }

    @Override
    public void onDestroy() {
        Log.i("S_update", "onDestroy()");
        super.onDestroy();

        if(socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e("S_error","Error closing server", e);
                e.printStackTrace();
            }
            socket = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    public void setDestWaypointNum(int waypt){
        this.destWaypointNum = waypt;
    }

    public int getDestWaypointNum() { return destWaypointNum; }

    public void setInitialButtonPressed (boolean b){
        this.initialButtonPressed = b;
    }

    public boolean getInitialButtonPressed (){ return initialButtonPressed;}

    public void setStartButtonPressed (boolean b){
        this.startButtonPressed = b;
    }

    public boolean getStartButtonPressed (){ return startButtonPressed;}

    public void setCancelButtonPressed (boolean b){
        this.cancelButtonPressed = b;
    }

    public boolean getCancelButtonPressed (){ return cancelButtonPressed;}

    public void setDisconnectButtonPressedButtonPressed (boolean b){
        this.disconnectButtonPressed = b;
    }

    public boolean getDisconenctButtonPressed (){ return disconnectButtonPressed;}


}
