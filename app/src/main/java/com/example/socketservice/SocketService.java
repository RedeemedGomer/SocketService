package com.example.socketservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;

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
    public static final String SERVERIP = "10.13.68.144";//"10.13.78.162";
    public static final int SERVERPORT = 8000;
    Socket socket = new Socket();
    private BufferedReader reader = null;
    private OutputStream writer = null;
    private String serverSays;

    //Service Variables
    private IBinder mBinder = new myBinder();
    private Boolean runDone = false;
    private Boolean isBound = false;
    private boolean doFlight = false;
    private double lat = 1234;
    private double lon = 5678;

    //GPS variables
    LocationTrackService locationTrackServe;
    LocationManager LocMan;

    //drone variables
    private double droneLat = -1;
    private double droneLong = -1;
    private double droneAlt = -1;
    private float droneVelocity = -1;
    private int droneHeading = -1;
    private int destWaypointNum = -1;
    private int startWaypointNum = -1;
    private boolean initialButtonPressed = false;
    private boolean startButtonPressed = false;
    private boolean  cancelButtonPressed = false;
    private boolean disconnectButtonPressed = false;
    private boolean emergStopButtonPressed = false;
    private String socketMessage = "";
    private String debugMessages = "";

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
        super.onStartCommand(intent, flags, startId);

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

            //STAGE #1 START////////////////////////////////////////////////////////////////////////
            //setup/connect socket + do initial values after initial button is pressed////////////////
            ////////////////////////////////////////////////////////////////////////////////////////

            //socket + read/write setup

//            try {
//                Log.i("connectSocket","run(): entered");
//
//                //create a socket to make the connection with the server
//                socket = new Socket(SERVERIP, SERVERPORT);
//
//            } catch (Exception e) {
//                Log.e("S_Error", "Error from making socket", e);
//                Log.i("connectSocket","run(): exception1");
//            }
//
//            try {
//                Log.i("connectSocket","run(): try to make new reader");
//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            }catch (Exception e) {
//                   Log.e("S_Error", "Error from making socket reader", e);
//                Log.i("connectSocket","run(): exception making reader");
//            }
//
//            try{
//                Log.i("connectSocket","run(): try to make writer");
//                writer = socket.getOutputStream();
//            } catch (Exception e){
//                Log.e("S_Error", "Error from making socket writer", e);
//                Log.i("connectSocket","run(): exception making writer");
//            }


            //TODO - for button testing
            //stage 0.
            //popup: "If you want to exit press the 'Disconnect" button at any time to stop the app."
            //popup: "If you want to go somewhere pick a destination, then press initialize button."

            //TODO - for button testing
            //stage 1
            //active buttons: initial, disconnect
            //status bar: "Waiting for Initialization"

//            //send/receive initial values after initial = true
//            boolean initialNotPressedYet = true;
//            while (initialNotPressedYet){
//                if (initialButtonPressed){
                    //TODO - for buttons
                    //stage 1
                    //active buttons: start, disconnect
                    //popup: "This is your last chance to change your destination."
                    //status bar: "Press Start to begin your journey."
//                    initialNotPressedYet = false;
//                }
//            }


//            droneLat = Double.valueOf(readMessageAndAck());
//            droneLong = Double.valueOf(readMessageAndAck());
//            droneVelocity = Float.valueOf(readMessageAndAck());
//            droneHeading = Integer.valueOf(readMessageAndAck());
//
//            sendMessageGetAck(String.valueOf(locationTrackServe.getLatitude()));
//            sendMessageGetAck(String.valueOf(locationTrackServe.getLongitude()));
//            sendMessageGetAck(String.valueOf(startButtonPressed));
//            sendMessageGetAck(String.valueOf(cancelButtonPressed));
//            sendMessageGetAck(String.valueOf(emergStopButtonPressed));
//            sendMessageGetAck(String.valueOf(disconnectButtonPressed));


            //STAGE #2 START////////////////////////////////////////////////////////////////////////
            //STAGE #2 check value of start button.
            ////////////////////////////////////////////////////////////////////////////////////////

            while (true) {
                boolean tempStart = startButtonPressed; //temps to keep drone + app in sync
                boolean tempDis = disconnectButtonPressed;
                sendMessageGetAck(String.valueOf(tempStart));
                sendMessageGetAck(String.valueOf(tempDis));

                if (tempDis) {
                    resetButtons();
                    serverSays = "disconnect button pressed. stop socket functionality. (around line 164)";
                    debugMessages = "disconnect button pressed. stop socket functionality. (around line 164)";
                    break;
                }

                //STAGE #3 BEGIN FLIGHT/////////////////////////////////////////////////////////////
                //STAGE #3 check cancel button.
                ////////////////////////////////////////////////////////////////////////////////////

                if (tempStart) {
                    doFlight = true;
                } else {
                    doFlight = false;
                }

                while (doFlight){
                    SystemClock.sleep(2000); //add delay so person has time to press before take-off
                    boolean tempCancel = cancelButtonPressed;


                    if (tempCancel) {
                        System.out.println("should be mission aborted: "+ readMessageAndAck()); //receiving mission aborted
                        resetButtons();
                        serverSays = "cancel has been pressed. (around line 187)";
                        debugMessages = "cancel has been pressed. (around line 187)";
                        break;
                    }


                    //STAGE #4 FLIGHT ASC///////////////////////////////////////////////////////////
                    //STAGE #4 drone start ascending
                    ////////////////////////////////////////////////////////////////////////////////

                    socketMessage = "";
                    while (!socketMessage.equals("adv")){
                        socketMessage = readMessageAndAck();
                        debugMessages = debugMessages + "\n" + socketMessage;
                    }
                    debugMessages = debugMessages + "\n" + "'adv' found. continue on flight";

                    //STAGE #4 FLIGHT PATH + DECS///////////////////////////////////////////////////
                    //STAGE #4 drone continues on flight path and lands at destination
                    ////////////////////////////////////////////////////////////////////////////////

                    socketMessage = readMessageAndAck();
                    while (!socketMessage.equals("done")){
                        droneLat = Double.valueOf(socketMessage);
                        droneLong = Double.valueOf(readMessageAndAck());
                        droneAlt = Double.valueOf(readMessageAndAck());
                        droneVelocity = Float.valueOf(readMessageAndAck());
                        droneHeading = Integer.valueOf(readMessageAndAck());
                        debugMessages = debugMessages + "\n" + droneLat + "," + droneLong
                                            + "," + droneVelocity + "," + droneHeading;

                        socketMessage = readMessageAndAck();
                        startButtonPressed = false;
                    }
                    debugMessages = debugMessages + "\n" + "'done' found. flight finished";

                    //TODO - buttons
                    //active: start, disconnect
                    //status: "You have arrived at your destination."
                    doFlight = false;
                }

                //TODO - buttons
                //popup: "If you want to exit press the disconnect button and exit the app. If you want to go on a new journey choose a new location and press start."
            }



            //TODO - Button functionality
            //active buttons: no active buttons
            //popup: "The communication between app and drone has been correctly disconnected. Please close the app."
            //popup
            resetButtons();
            serverSays = debugMessages;
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

    public void sendMessageGetAck(String message){
        //send message
        sendMessage(message);
        //receive ack + verify
        String ack = "";
        while (true) { //TODO - some king of timeout for ack ??????
            ack = readMessage();
            if (ack.equals("true")) {
                System.out.println("good ack");
                break;
            } else {
                System.out.println("bad ack");
                //TODO - have some kind of error message on screen or something
                break;
            }
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


    public String readMessageAndAck(){
        //receive message
        String message = "uninitialized message";
        message = readMessage();
        //send ack
        sendMessage(String.valueOf(true));

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

    public void setStartWaypointNum(int waypt){
        this.startWaypointNum = waypt;
    }

    public int getStartWaypointNum() { return startWaypointNum; }

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

    public void setEmergStopButtonPressed(boolean b) {
        this.emergStopButtonPressed = b;
    }

    public boolean getEmergStopButtonPressed() {return emergStopButtonPressed;}

    private void resetButtons() {
        startButtonPressed = false;
        initialButtonPressed = false;
        cancelButtonPressed = false;
        disconnectButtonPressed = false;
    }

    public String getDebugMesseges() { return debugMessages;}

    public void resetDebugMessages() { debugMessages = "";}










    //have socket set disable/enable ^^ ?? I think when we get there yes. have stage 1 2 3 etc with lists of what to enable/disable
//    private void disableInitialBtn () {
//        initialBtn.setEnabled(false);
//        initialBtn.setBackgroundColor(0xaae3d1eb);
//    }
//
//    private void enableInitialBtn (){
//        initialBtn.setEnabled(true);
//        initialBtn.setBackgroundColor(0xffe6caf2);
//    }

}



/*   AILIN/KIRBY OLD CODE FOR TESTING etc


//            while (true) {
//                if (initialButtonPressed) {
//                    Log.i("connectSocket", "run(): getting ready to read da message");
//                    //serverSays = readMessage();
//                    lat = locationTrackServe.getLatitude();
//                    lon = locationTrackServe.getLongitude();
//
//
//                    messageToSend = "Hi I am phone, my lat:" + lat + ",  my lon:" + lon + " and I'm heading to waypoint:" + destWaypointNum + "\n";
//                    sendMessage(messageToSend);
//                }
//            }

 */