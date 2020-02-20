package com.example.charleschung.mci_pa1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor Accelerometer;
    private Sensor Gyroscope;
    private Sensor Magnetic;
    private Sensor Light;
    private Sensor Borameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing sensor services");

        Button BtnStart = (Button) findViewById(R.id.button_start);
        Button BtnStop = (Button) findViewById(R.id.button_stop);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = null;
        Gyroscope = null;
        Magnetic = null;
        Light = null;
        Borameter = null;

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            Gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            Magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            Light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            Borameter = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sensorManager.registerListener(MainActivity.this, Accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Magnetic, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Light, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Borameter, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sensorManager.unregisterListener(MainActivity.this);
            }
        });

        Log.d(TAG, "onCreate: Registered accelerometer listener.");

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] value = event.values;

        float currentX = 0;
        float currentY = 0;
        float currentZ = 0;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            currentX = value[0];
            currentY = value[1];
            currentZ = value[2];
            Log.d(TAG, "onSensorChanged: X:" + value[0] + ",Y: " + value[1] + ",Z: " + value[2]);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG, "onSensorChanged GYROSCOPE:" + value.length);
        }
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            Log.d(TAG, "onSensorChanged MAGNETIC FIELD:" + value[0]);
        }
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            Log.d(TAG, "onSensorChanged LIGHT:" + value[0]);
        }

        String entry = currentX + "," + currentY + "," + currentZ;
        writeToFile (entry, this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void writeToFile(String entry, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.csv", Context.MODE_PRIVATE));
            outputStreamWriter.write(entry);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
