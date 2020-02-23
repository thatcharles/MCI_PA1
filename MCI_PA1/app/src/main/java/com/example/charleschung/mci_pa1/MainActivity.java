package com.example.charleschung.mci_pa1;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor Accelerometer;
    private Sensor Gyroscope;
    private Sensor Magnetic;
    private Sensor Light;
    private Sensor Borameter;

    boolean isRunning;
    EditText distanceInput;
    String distance;
    String filePath;
    String fileName;
    CSVWriter writer = null;
    long startTime;
    Map<Long, List> map = new HashMap<>();
    String activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing sensor services");

        Button BtnStart = (Button) findViewById(R.id.button_start);
        Button BtnStop = (Button) findViewById(R.id.button_stop);

        /**
         * Initialize and get sensors
         */
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

        /**
         * Set up distanceInput, Spinner, BtnStart, BtnStop UI.
         */
        distanceInput = (EditText) findViewById(R.id.distanceInput);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.activities_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                distance = distanceInput.getText().toString();

                filePath = getFilesDir().getAbsolutePath();
                fileName = activity + " " + dtf.format(now) + " " + distance + "cm.csv";

                try {
                    /**
                     * Initialize CSVWriter
                     */
                    writer = new CSVWriter(new FileWriter(filePath + "/" + fileName,true));
                    Log.d(TAG, "Writing to " + filePath  + "/" + fileName);
                    // writer = new FileWriter(new File(getFilesDir().getAbsolutePath(), fileName));
                    writer.writeNext(String.format("Timestamp, Accel x, Accel y, Accel z, Gyro x, Gyro y, Gyro z,Mag x,Mag y,Mag z,Light Intensity\n").split(","));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /**
                 * Register (activate) sensors
                 */
                sensorManager.registerListener(MainActivity.this, Accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Magnetic, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Light, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, Borameter, SensorManager.SENSOR_DELAY_FASTEST);

                isRunning = true;
                startTime = 0;
            }
        });

        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                /**
                 * Write the data to CSV file.
                 */
                writeToFile();
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /**
                 * Stop the sensors.
                 */
                sensorManager.unregisterListener(MainActivity.this);
                isRunning = false;

                /**
                 * Share the CSV file created.
                 */
                onShareCSV();
            }
        });

    }

    /**
     * onSensorChanged will be called every time the sensor reads new data.
     * The data would be written to a hashmap called map.
     */
    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] value = event.values;

        if (startTime == 0){
            startTime = event.timestamp;
        }

        Long key = (event.timestamp - startTime) / 1000000L;
        List sensors = map.get(key);
        if (sensors == null) {
            sensors = new ArrayList<>(
                    Arrays.asList("", "", "", "", "", "", "", "", "", "", ""));
            map.put(key, sensors);
        }

        if(isRunning) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged: X:" + value[0] + ",Y: " + value[1] + ",Z: " + value[2]);

                sensors.set(1,String.valueOf(event.values[0]));
                sensors.set(2,String.valueOf(event.values[1]));
                sensors.set(3,String.valueOf(event.values[2]));
            }
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "onSensorChanged GYROSCOPE:" + value.length);

                sensors.set(4,String.valueOf(event.values[0]));
                sensors.set(5,String.valueOf(event.values[1]));
                sensors.set(6,String.valueOf(event.values[2]));
                Log.d(TAG, "on writing to map: " + map.get(key));
            }
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                Log.d(TAG, "onSensorChanged MAGNETIC FIELD:" + value[0]);

                sensors.set(7,String.valueOf(event.values[0]));
                sensors.set(8,String.valueOf(event.values[1]));
                sensors.set(9,String.valueOf(event.values[2]));
            }
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                Log.d(TAG, "onSensorChanged LIGHT:" + value[0]);

                sensors.set(10,String.valueOf(event.values[0]));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * Create and start intent to share the CSV file created.
     */
    private void onShareCSV() {

        Context context = getApplicationContext();
        File filelocation = new File(getFilesDir(), fileName);
        Uri path = FileProvider.getUriForFile(context, "com.example.charleschung.mci_pa1", filelocation);

        Log.d(TAG, "onShareCSV: " + path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setData(path);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "This is a CSV I'm sharing.");
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(shareIntent, "Share..."));
    }

    /**
     * writeToFile would be called after user clicks buttonStop.
     * It writes the data in hashmap map into a CSV file.
     */
    public void writeToFile() {
        SortedSet<Long> keys = new TreeSet<>(map.keySet());
        for (Long key : keys) {
            List values = map.get(key);
            values.set(0,String.valueOf(key));

            Object[] objectList = values.toArray();
            String[] array =  Arrays.copyOf(objectList,objectList.length,String[].class);
            writer.writeNext(array);
        }

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (pos){
            case 0 :
                activity = "WALKING";
                break;
            case 1:
                activity = "RUNNING";
                break;
            case 2:
                activity = "IDLE";
                break;
            case 3:
                activity = "STAIRS";
                break;
            case 4:
                activity = "JUMPING";
                break;
        }
        Log.d(TAG, "onItemSelected: " + activity);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
