package com.example.charleschung.mci_pa1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

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
    EditText stepDistanceInput;
    String stepDistance;
    String filePath;
    String fileName;
    CSVWriter writer = null;
    long startTime;
    Map<Long, List> map = new HashMap<>();
    String activity;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    String recordingFilePath;
    String recordingFileName;
    WifiManager wifiManager = null;


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
         * Initialize and get Wifi sensor
         */
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        /**
         * Set up distanceInput, Spinner, BtnStart, BtnStop UI.
         */
        stepDistanceInput = (EditText) findViewById(R.id.distance_input);

        /*
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.activities_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
         spinner.setAdapter(adapter);

        */

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                stepDistance = stepDistanceInput.getText().toString();

                filePath = getFilesDir().getAbsolutePath();
                fileName = "Jaewon_Nathaniel_Yu-Lin_" + dtf.format(now) + "_PA2.csv";

                try {
                    /**
                     * Initialize CSVWriter
                     */
                    writer = new CSVWriter(new FileWriter(filePath + "/" + fileName,true));
                    Log.d(TAG, "Writing to " + filePath  + "/" + fileName);
                    // writer = new FileWriter(new File(getFilesDir().getAbsolutePath(), fileName));
                    writer.writeNext(String.format("Timestamp, Accel x, Accel y, Accel z, Gyro x, Gyro y, Gyro z,Mag x,Mag y,Mag z,Light Intensity,Wifi Rssi Signal\n").split(","));
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

                recordingFilePath = getFilesDir().getAbsolutePath();
                recordingFileName = "Audio " + dtf.format(now) + ".3gp" ;

                startRecording();

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

                stopRecording();
                onShareAudio();

                lastPeakTime = 0;
                lastRotateTime = 0;
            }
        });
    }

    /**
     * Sound recording
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    private void startRecording() {
        if( recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(recordingFilePath + "/" + recordingFileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.d(TAG, "startRecording: recorder initialized");
        }
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording: ", e);
            Log.d(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    /**
     * onSensorChanged will be called every time the sensor reads new data.
     * The data would be written to a hashmap called map.
     */

    double lastPeakTime = 0;
    double lastRotateTime = 0;
    int stepCount = 0;
    int walkingDistance = 0;
    float Rotate[] = new float[9];
    float I[] = new float[9];
    float mGravity[] = new float[3];
    float mGeomagnetic[] = new float[3];
    float orientation[] = new float[3];
    double previousDegree = 0;
    double totalDegree = 0;

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
            sensors = new ArrayList<>(Arrays.asList("", "", "", "", "", "", "", "", "", "", "", ""));
            map.put(key, sensors);
        }

        if(isRunning) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged: X:" + value[0] + ",Y: " + value[1] + ",Z: " + value[2]);

                sensors.set(1,String.valueOf(event.values[0]));
                sensors.set(2,String.valueOf(event.values[1]));
                sensors.set(3,String.valueOf(event.values[2]));

                float Ax = event.values[0];
                float Ay = event.values[1];
                float Az = event.values[2];
                double meg = Math.sqrt(Ax * Ax + Ay * Ay + Az * Az );


                if (meg > 12.0 && key - lastPeakTime > 500){
                    TextView stepView = (TextView) findViewById(R.id.step_view);
                    TextView distanceView = (TextView) findViewById(R.id.distance_view);
                    stepView.setText(++stepCount + " Steps count");

                    walkingDistance += Integer.valueOf(stepDistance);
                    distanceView.setText(walkingDistance +" cm walked");
                    lastPeakTime = key;
                }

                mGravity = event.values;
            }
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "onSensorChanged GYROSCOPE:" + value.length);

                sensors.set(4,String.valueOf(event.values[0]));
                sensors.set(5,String.valueOf(event.values[1]));
                sensors.set(6,String.valueOf(event.values[2]));

                Log.d(TAG, "on writing to map: " + map.get(key));

                int rssi = wifiManager.getConnectionInfo().getRssi();
                Log.d(TAG, "RSSI: "+ rssi);
                sensors.set(11,String.valueOf(rssi));
            }
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                Log.d(TAG, "onSensorChanged MAGNETIC FIELD:" + value[0]);

                sensors.set(7,String.valueOf(event.values[0]));
                sensors.set(8,String.valueOf(event.values[1]));
                sensors.set(9,String.valueOf(event.values[2]));

                mGeomagnetic = event.values;

                if (mGravity != null && mGeomagnetic != null && key-lastRotateTime > 450){
                    boolean success = SensorManager.getRotationMatrix(Rotate,I,mGravity,mGeomagnetic);
                    if (success){
                        SensorManager.getOrientation(Rotate,orientation);
                        double azimuth = Math.toDegrees(orientation[0]);

                        Log.d(TAG, "on MAGNETIC FIELD: azimuth " + azimuth);
                        TextView rotationView = (TextView) findViewById(R.id.rotation_view);
                        // TextView currentRotationView = (TextView) findViewById(R.id.current_rotation_view);
                        if (previousDegree == 0){}
                        else {
                            double degreeChange = Math.min(Math.abs(azimuth - previousDegree),360.0 - Math.abs(azimuth - previousDegree));
                            if ( degreeChange > 25.0) {
                                totalDegree += degreeChange;
                            }
                        }
                        previousDegree = azimuth;
                        // currentRotationView.setText("heading to: " + azimuth);
                        rotationView.setText(totalDegree + " degrees in total");
                        lastRotateTime = key;
                    }

                }

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
     * Create and start intent to share the Audio file created.
     */
    private void onShareAudio() {

        Context context = getApplicationContext();
        File filelocation = new File(getFilesDir(), recordingFileName);
        Uri path = FileProvider.getUriForFile(context, "com.example.charleschung.mci_pa1", filelocation);

        Log.d(TAG, "onShareAudio: " + path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setData(path);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "This is the audio I'm sharing.");
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(shareIntent, "Share..."));
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
