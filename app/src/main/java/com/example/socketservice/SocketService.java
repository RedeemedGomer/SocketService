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
    public static final String SERVERIP = "10.13.75.82";//TODO insert pi or computer IP
    public static final int SERVERPORT = 8020;
    Socket socket;
     // InetAddress serverAddr; //todo remove?
    private BufferedReader reader = null;
    private OutputStream writer = null;
    private String serverSays;

    //Service Variables
    private IBinder mBinder = new myBinder();
    private Boolean runDone = false;
    private double lat = 1234;
    private double lon = 5678;

    //GPS variables
    LocationTrackService locationTrackServe;// = new LocationTrack(this);//TODO does this work with service context?;
    LocationManager LocMan;

    public IBinder onBind(Intent intent) {
        System.out.println("I am in Ibinder onBind method");
        return mBinder;
    }

    public class myBinder extends Binder {
       public SocketService getService(){
            return SocketService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);//todo is this even needed?
        System.out.println("onStartCommand");
        Log.i("S_update", "onStartCommand");
//        Runnable connect = new connectSocket();
//        new Thread(connect).start();
        locationTrackServe = new LocationTrackService(getApplicationContext());
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
                    Log.i("connectSocket","we are in run()");

                //create a socket to make the connection with the server
                socket = new Socket(SERVERIP, SERVERPORT);

            } catch (Exception e) {
                Log.e("S_Error", "Error from making socket", e);
            }

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }catch (Exception e) {
                   Log.e("S_Error", "Error from making socket reader", e);
            }

            try{
                writer = socket.getOutputStream();
            } catch (Exception e){
                Log.e("S_Error", "Error from making socket writer", e);
            }

            //TODO concept test, delete when used
            serverSays = readMessage();
            lat = locationTrackServe.getLatitude();
            lon = locationTrackServe.getLongitude();
            String messageToSend = "Hi I am phone, my lat:"+lat+",  my lon:"+ lon +"\n";
            sendMessage(messageToSend);
            runDone = true;
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
        return "Lat:"+locationTrackServe.getLatitude()+",  Lon:"+locationTrackServe.getLongitude()+".\n";
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
//        super.onDestroy();
//        try {
//            socket.close();
//        } catch (Exception e) {
//            Log.e("S_error","Error closing server", e);
//            e.printStackTrace();
//        }
//        socket = null;

        Log.i("S_update", "onDestroy()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }



}
