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


/*
    IP Matt:    "10.13.68.144"     8000
    IP Drone:   "192.168.4.1"     8000
    IP Kirby:
 */




public class SocketService extends Service {
    //socket variables
    public static final String SERVERIP = "192.168.4.1";
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
    private boolean connectButtonPressed = false;
    private boolean startButtonPressed = false;
    private boolean  cancelButtonPressed = false;
    private boolean disconnectButtonPressed = false;
    private boolean emergStopButtonPressed = false;
    private String socketMessage = "";
    private String statusText = "ready to connect";
    private String debugMessages = "";
    private String instrMessage = "";

    //stage varibles for buttons + UI output
    private int stage = -1;
    private boolean showInstr = false;

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
        public void run() { //TODO put all of this in a try catch to grab disconnection errors. pretend disconnect is pressed + send error message to user

            //STAGE #1 START////////////////////////////////////////////////////////////////////////
            //setup/connect socket + do initial values after initial button is pressed////////////////
            ////////////////////////////////////////////////////////////////////////////////////////

            SystemClock.sleep(2000);
            stage = 1;
            statusText = "establishing comm with drone";


//            //socket + read/write setup
//
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




            SystemClock.sleep(2000);
            statusText = "comm with drone established";

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
                SystemClock.sleep(2000);
                stage = 2;
                statusText = "ready to fly";


                boolean tempStart = startButtonPressed; //temps to keep drone + app in sync
                boolean tempDis = disconnectButtonPressed;
//                sendMessageGetAck(String.valueOf(tempStart));
//                sendMessageGetAck(String.valueOf(tempDis));

                if (tempDis) {
                    SystemClock.sleep(2000);
                    statusText = "preparing to disconnect comm";
                    serverSays = "disconnect button pressed. stop socket functionality.";
                    debugMessages = "disconnect button pressed. stop socket functionality.";
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

                    SystemClock.sleep(2000);
                    stage = 3;
                    statusText = "preparing to fly";

                    SystemClock.sleep(2000);
                    boolean tempCancel = cancelButtonPressed;

//                    sendMessageGetAck(String.valueOf(destWaypointNum));
//                    //sendMessageGetAck(String.valueOf(startWaypointNum)); //start waypoint #
//                    sendMessageGetAck(String.valueOf(locationTrackServe.getLatitude()));
//                    sendMessageGetAck(String.valueOf(locationTrackServe.getLongitude()));
//                    sendMessageGetAck(String.valueOf(tempCancel));

                    if (tempCancel) {
                        SystemClock.sleep(2000);
                        statusText = "mission canceled";
                        //System.out.println("should be mission aborted: "+ readMessageAndAck()); //receiving mission aborted
                        resetButtonsExceptConnect();
                        serverSays = "cancel has been pressed.";
                        debugMessages = "cancel has been pressed.";
                        break;
                    }


                    //STAGE #4 FLIGHT ASC///////////////////////////////////////////////////////////
                    //STAGE #4 drone start ascending
                    ////////////////////////////////////////////////////////////////////////////////

//                   socketMessage = "";
//                    while (!socketMessage.equals("adv")){
//                        socketMessage = readMessageAndAck();
//                    }
//                    debugMessages = debugMessages + "\n" + "'adv' found. continue on flight";

                    //STAGE #4 FLIGHT PATH + DECS///////////////////////////////////////////////////
                    //STAGE #4 drone continues on flight path and lands at destination
                    ////////////////////////////////////////////////////////////////////////////////


                    if (tempCancel) {
                        SystemClock.sleep(2000);
                        statusText = "mission canceled. landing...";
                        //System.out.println("should be mission aborted: "+ readMessageAndAck()); //receiving mission aborted
                        resetButtonsExceptConnect();
                        serverSays = "cancel has been pressed.";
                        debugMessages = "cancel has been pressed.";
                        break;
                    }



//                    socketMessage = readMessageAndAck();
//                    boolean cancelSent = false;
//                    while (!socketMessage.equals("done")){
//
//                        //check if cancel value requested from drone. if so give drone cancel value before continuing
//                        if (socketMessage.equals("cancel")){
//                            while (socketMessage.equals("cancel")){
//                                tempCancel = cancelButtonPressed;
//                                sendMessageGetAck(String.valueOf(tempCancel));
//                                if (tempCancel){
//                                    statusText = "mission canceled. landing...";
//                                }
//                                socketMessage = readMessageAndAck();
//                            }
//                            if (socketMessage.equals("done")){
//                                //end while loop
//                                break;
//                            }
//                            //droneLat = Double.valueOf(readMessageAndAck());
//
//                        }
//                        droneLat = Double.valueOf(socketMessage);
//                        droneLong = Double.valueOf(readMessageAndAck());
//                        droneAlt = Double.valueOf(readMessageAndAck());
//                        droneVelocity = Float.valueOf(readMessageAndAck());
//                        droneHeading = Integer.valueOf(readMessageAndAck());
//                        debugMessages = debugMessages + "\n lat:" + droneLat + " lon:" + droneLong
//                                            + " vel:" + droneVelocity + " head:" + droneHeading;
//
//                        socketMessage = readMessageAndAck();
//                        startButtonPressed = false;
//                    }
//                    debugMessages = debugMessages + "\n" + "'done' found. flight finished";

                    SystemClock.sleep(2000);
                    resetButtons();
                    statusText = "you have arrived :)";
                    doFlight = false;
                }

           }

            SystemClock.sleep(2000);
            statusText = "ready to connect";
            stage = 4;
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
        while (true) {
            ack = readMessage();
            if (ack.equals("true")) {
                System.out.println("good ack");
                break;
            } else {
                System.out.println("bad ack");
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

    public String getLatString(){
        Log.i("S_update", "in getLatString()");
        if(locationTrackServe != null) {
            return String.valueOf(locationTrackServe.getLatitude());
        }else{
            Log.d("S_debug", "LocationTrackServe was null");
            return "Error LocationTracker is Null";
        }
    }

    public String getLonString(){
        Log.i("S_update", "in getLonString()");
        if(locationTrackServe != null) {
            return String.valueOf(locationTrackServe.getLongitude());
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

//    public int getDestWaypointNum() { return destWaypointNum; }

//    public void setStartWaypointNum(int waypt){
//        this.startWaypointNum = waypt;
//    }
//
//    public int getStartWaypointNum() { return startWaypointNum; }

//    public void setInitialButtonPressed (boolean b){
//        this.initialButtonPressed = b;
//    }

    public boolean getConnectButtonPressed (){ return connectButtonPressed;}

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

//    public void setEmergStopButtonPressed(boolean b) {
//        this.emergStopButtonPressed = b;
//    }
//
//    public boolean getEmergStopButtonPressed() {return emergStopButtonPressed;}

    private void resetButtons() {
        startButtonPressed = false;
        connectButtonPressed = false;
        cancelButtonPressed = false;
        disconnectButtonPressed = false;
    }

    private void resetButtonsExceptConnect(){
        startButtonPressed = false;
        cancelButtonPressed = false;
        disconnectButtonPressed = false;
    }

//    public String getDebugMesseges() { return debugMessages;}
//
//    public void resetDebugMessages() { debugMessages = "";}

    public int getCommStage(){return stage;}

    public String getStatusText(){ return statusText;}

    public void setStatusText(String s) {
        statusText = s;
    }

    public double getDroneLat(){return droneLat;}

    public double getDroneLon(){return droneLong;}

    public double getDroneAlt(){return droneAlt;}

    public float getDroneVel(){return droneVelocity;}

    public int getDroneHead(){return droneHeading;}

    public void setInstrSwitchValue(boolean b){
        showInstr = b;
    }



}
