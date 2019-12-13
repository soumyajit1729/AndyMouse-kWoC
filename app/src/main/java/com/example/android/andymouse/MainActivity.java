package com.example.android.andymouse;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    //extract data from sensors
    SensorManager sensorManager = null;
    //int count=0;
    int on_bit = 0;
    int stop = -10;
    int start = 10;
    long measure_time;
    boolean to_send, notConnected = false;
    TextView textView_sensor_X_acc, textView_sensor_Y_acc, textView_sensor_Z_acc;

    EditText ip_address;
    List list, list2;
    Button btnStart, btnStop, btnLeftClick, btnRightClick;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialise();

        /*Setting the behaviour for the start button*/
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_bit = start;
                if (list.size() > 0) {
                    sensorManager.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
                }

                to_send = true;
                notConnected = false;
                System.out.println(SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        /*Setting the behaviour for the stop button*/
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_bit = stop;
                if (list.size() > 0) {
                    sensorManager.unregisterListener(sel);
                }
                //System.out.println(to_send);
                to_send = false;
                notConnected = false;
                //System.out.println(to_send);
                // super.onStop();
            }
        });

        /*Setting the behaviour for the left click button*/
        btnLeftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_bit = 10000;
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                send_loop();

                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                on_bit = 0;
            }
        });

        btnRightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_bit = -1;
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                send_loop();

                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                on_bit = 0;
            }
        });

    }

    /*Define what happens when the onStop() call is executed*/
    @Override
    protected void onStop() {
        on_bit = stop;
        if (list.size() > 0) {
            sensorManager.unregisterListener(sel);
        }
        System.out.println(to_send);
        to_send = false;
        notConnected = false;
        System.out.println(to_send);
        super.onStop();
    }

    /*This method initializes the class variables*/
    private void initialise() {

        /* 1.  Get a SensorManager instance */

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        list = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        /* initialize the views */
        textView_sensor_X_acc = findViewById(R.id.textView_sensor_X_acc);
        textView_sensor_Y_acc = findViewById(R.id.textView_sensor_Y_acc);
        textView_sensor_Z_acc = findViewById(R.id.textView_sensor_Z_acc);
        btnStart = findViewById(R.id.button_start);
        btnStop = findViewById(R.id.button_stop);
        ip_address = findViewById(R.id.ip_addr);
        btnLeftClick = findViewById(R.id.button_left);
        btnRightClick = findViewById(R.id.button_right);

    }


    /*
    * Create a SensorEventLister instance to show the changes in the values of the necessary textViews
    */
    SensorEventListener sel = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                //round up to 2 decimal places

                textView_sensor_X_acc.setText("" + (values[0] / values[2]));
                textView_sensor_Y_acc.setText("" + (values[1] / values[2]));
                textView_sensor_Z_acc.setText("" + values[2]);
            }

            System.out.println(to_send);

            if (notConnected) {
                Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
            }

            notConnected = false;

            if (to_send) {
                send_loop();
            }


        }
    };


    /*
    * This areas marks the beginning of the section where we start sending the data
    * to the rest API server(laptop) from the REST API client(Android Device)
    */


    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    String response = HttpPost(urls[0]);  //if phone is not connected to sensor.py then this statement will throw an error.
                    notConnected = false;
                    return response;
                } catch (JSONException e) {
                    to_send = false;
                    notConnected = true;
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                to_send = false;
                notConnected = true;
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            System.out.println("post request executed");
        }
    }

    private String HttpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buildJsonObject();

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message
        return conn.getResponseMessage() + "";

    }

    public void send(View view) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        String ip = ip_address.getText().toString();
        //new HTTPAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
        //new HTTPAsyncTask().execute(ip);//"http://10.146.121.148:8080/sensor";
        // new HTTPAsyncTask().execute("http://10.146.85.93:8080/sensor");
        //new HTTPAsyncTask().execute("http://10.146.94.129:8080/sensor");

        new HTTPAsyncTask().execute("http://" + ip + ":8080/sensor");
    }

    public void send_loop() {
        // Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        String ip = ip_address.getText().toString();
        //new HTTPAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
        // new HTTPAsyncTask().execute("http://10.146.94.129:8080/sensor");
        new HTTPAsyncTask().execute("http://" + ip + ":8080/sensor");//"http://10.145.170.91:8080/sensor";

    }

    private JSONObject buildJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("x_tilt", textView_sensor_X_acc.getText().toString());
        jsonObject.accumulate("y_tilt", textView_sensor_Y_acc.getText().toString());
        // jsonObject.accumulate("zacc",  textView_sensor_Z_acc.getText().toString());
        jsonObject.accumulate("arrow", on_bit);
        // jsonObject.accumulate("left",  on_bit);
        // jsonObject.accumulate("zgyro",  textView_sensor_Z_gyro.getText().toString());


        return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn,
                                       JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }
}
