package com.example.charleschung.mci_pa1;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    boolean isRunning;
    // FileWriter writer;
    String filePath;
    String fileName;
    CSVWriter writer = null;

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

                Log.d(TAG, "Writing to " + getFilesDir().getAbsolutePath());
                filePath = getFilesDir().getPath();
                // filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                fileName = " sensor-" + System.currentTimeMillis() + ".csv";

                try {
                    writer = new CSVWriter(new FileWriter(filePath+fileName,true));
                    // writer = new FileWriter(new File(getFilesDir().getAbsolutePath(), fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sensorManager.registerListener(MainActivity.this, Accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Magnetic, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Light, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Borameter, SensorManager.SENSOR_DELAY_FASTEST);

                isRunning = true;
            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sensorManager.unregisterListener(MainActivity.this);
                isRunning = false;

                // TODO
                //onShareCSV();
            }
        });

        Log.d(TAG, "onCreate: Registered accelerometer listener.");

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] value = event.values;


        if(isRunning) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged: X:" + value[0] + ",Y: " + value[1] + ",Z: " + value[2]);
                writer.writeNext(String.format("%d; ACC; %f; %f; %f; %f; %f; %f\n", event.timestamp, event.values[0], event.values[1], event.values[2], 0.f, 0.f, 0.f).split(","));
            }
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "onSensorChanged GYROSCOPE:" + value.length);
                writer.writeNext(String.format("%d; GYRO; %f; %f; %f; %f; %f; %f\n", event.timestamp, event.values[0], event.values[1], event.values[2], 0.f, 0.f, 0.f).split(","));
            }
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                Log.d(TAG, "onSensorChanged MAGNETIC FIELD:" + value[0]);
                writer.writeNext(String.format("%d; MAG; %f; %f; %f; %f; %f; %f\n", event.timestamp, event.values[0], event.values[1], event.values[2], 0.f, 0.f, 0.f).split(","));
            }
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                Log.d(TAG, "onSensorChanged LIGHT:" + value[0]);
                writer.writeNext(String.format("%d; LIGHT; %f; %f;\n", event.timestamp, event.values[0], 0.f).split(","));
            }
        }

        // String entry = currentX + "," + currentY + "," + currentZ;
        // writeToFile (entry, this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void writeToFile(String entry, Context context) {
    }

    /**
     * Create and start intent to share a photo with apps that can accept a single image
     * of any format.
     */
    private void onShareCSV() {

        Uri path = FileProvider.getUriForFile(this, "com.example.FileProvider", new File(filePath + fileName));

        Log.d(TAG, "onShareCSV: " + path);
        Intent shareIntent = new Intent("com.example.FileProvider").setData(path);

        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getApplicationContext().grantUriPermission(packageName, path, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "This is a CSV I'm sharing.");
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        shareIntent.setType("text/csv");
        startActivity(Intent.createChooser(shareIntent, "Share..."));
        this.setResult(RESULT_OK, shareIntent);
    }
}
