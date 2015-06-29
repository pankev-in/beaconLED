package com.example.kpan.beacon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;


public class MainActivity extends Activity implements BeaconConsumer{

    protected static final String TAG = "MainActivity";
    protected static final String TARGET_UUID = "74278bda-b644-4520-8f0c-720eaf059935";
    protected static final double TARGET_Distance = 1.000;
    private Button buttonStartStop;
    private TextView distanceText;
    private Region region;
    private BeaconManager beaconManager;
    private boolean runningOrNot=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Verify device if it supports Bluetooth/BLE:
        verifyBluetooth();

        //Setup BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        region = new Region("VeryUniqId",null,null,null);


        //Setup other view objects:
        buttonStartStop = (Button) findViewById(R.id.button_startStop);
        distanceText = (TextView) findViewById(R.id.textView_DistanceMeter);
        buttonStartStop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buttonClick();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Program onDestroy Method is called");
        beaconManager.unbind(this);
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect() {
    }

    RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                Iterator<Beacon> beaconIterator = beacons.iterator();
                while (beaconIterator.hasNext()) {
                    Beacon beacon = beaconIterator.next();
                    Log.d(TAG,"Found Beacon:"+beacon.getId1()+"  Distance:"+beacon.getDistance());
                    if(beacon.getId1().toString().equals(TARGET_UUID)){
                        foundTarget(beacon);
                    }
                }
            }
        }
    };

    private void foundTarget(Beacon beacon){
        displayDistance(beacon.getDistance());
        if (beacon.getDistance()< TARGET_Distance){
            final Intent intent = new Intent(this, DeviceControlActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, beacon.getBluetoothName());
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, beacon.getBluetoothAddress());
            stopScanning();
            beaconManager.unbind(this);

            startActivity(intent);
        }
    }

    private void displayDistance(Double i){
        final float Distance = i.floatValue();
        distanceText.post(new Runnable() {
            @Override
            public void run() {
                distanceText.setText(Float.toString(Distance));
            }
        });
    }


    private void startScanning(){
        runningOrNot=true;
        beaconManager.setRangeNotifier(rangeNotifier);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            // TODO - OK, what now then?
        }
    }

    private void stopScanning(){
        runningOrNot=false;
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            // TODO - OK, what now then?
        }
    }

    private void buttonClick(){
        if(runningOrNot==false){
            buttonStartStop.setText("stop");
            startScanning();
        }
        else{
            buttonStartStop.setText("start");
            stopScanning();
        }
    }

    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
        }

    }


}
