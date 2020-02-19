package com.example.charleschung.mci_pa1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor Accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing sensor services");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = null;

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){

            Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (Accelerometer == null){
            // Use the accelerometer.
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
                Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else{
                // Sorry, there are no accelerometers on your device.
                // You can't play this game.
            }
        }

        Log.d(TAG, "onCreate: Registered accelerometer listener.");

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float[] value = event.values;
        // Do something with this sensor value.
        Log.d(TAG, "onSensorChanged: X:" + value[0] + ",Y: " + value[1] + ",Z: " + value[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        
    }
}
