package com.example.android.andymouse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;


public class MainActivity extends Activity {

    //extract data from sensors
    SensorManager sm = null;
    //int count=0;
    int on_bit=0;
    int stop = -10;
    int start = 10;
    double sensitivity=25;
    boolean to_send,notConnected=false;
    TextView textView_sensor_X_acc;
    TextView textView_sensor_Y_acc;
    SeekBar seekBar;
    //TextView textView_sensor_X_gyro;
   // TextView textView_sensor_Y_gyro;
   // TextView textView_sensor_Z_gyro;
    EditText ip_address;
    Button leftclick,rightclick;
    List list,list2;

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int rotation = display.getRotation();
                if (rotation == Surface.ROTATION_90) {
                    textView_sensor_Y_acc.setText("" + new DecimalFormat("#.#####").format(values[0] / values[2]));
                    textView_sensor_X_acc.setText("" + new DecimalFormat("#.#####").format(-values[1] / values[2]));
                } else if (rotation == Surface.ROTATION_0){
                    textView_sensor_X_acc.setText("" + new DecimalFormat("#.#####").format(values[0] / values[2]));
                    textView_sensor_Y_acc.setText("" + new DecimalFormat("#.#####").format(values[1] / values[2]));
                } else if (rotation == Surface.ROTATION_270) {
                    textView_sensor_Y_acc.setText("" + new DecimalFormat("#.#####").format(-values[0] / values[2]));
                    textView_sensor_X_acc.setText("" + new DecimalFormat("#.#####").format(values[1] / values[2]));
                } else {
                    textView_sensor_X_acc.setText("" + new DecimalFormat("#.#####").format(-values[0] / values[2]));
                    textView_sensor_Y_acc.setText("" + new DecimalFormat("#.#####").format(-values[1] / values[2]));
                }
            }
            /*if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] values2 = event.values;
                //round up to 2 decimal places
                values2[0]=Math.round(values2[0] * 100.0f) / 100.0f;
                values2[1]=Math.round(values2[1] * 100.0f) / 100.0f;
                values2[2]=Math.round(values2[2] * 100.0f) / 100.0f;

                textView_sensor_X_gyro.setText(""+values2[0]);
                textView_sensor_Y_gyro.setText(""+values2[1]);
                textView_sensor_Z_gyro.setText(""+values2[2]);
            }*/
            System.out.println(to_send);


            if (notConnected)
                Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
            notConnected=false;

            if(to_send)
           {
               send_loop();
           }


        }
    };


    public void nextbutton(View view) throws java.lang.InterruptedException
    {

        on_bit=-1;

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;

    }
    public void previousbutton(View view) throws java.lang.InterruptedException
    {
        on_bit=10000;

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;

    }

    public void rightclick(View view)throws java.lang.InterruptedException {

        on_bit=2;

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;


    }

    public void leftclick(View view)throws java.lang.InterruptedException {

        on_bit=3;

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action,keycode;

        action = event.getAction();
        keycode = event.getKeyCode();

        switch (keycode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    leftclick.performClick();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    rightclick.performClick();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get a SensorManager instance */

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        textView_sensor_X_acc = (TextView)findViewById(R.id.textView_sensor_X_acc);
        textView_sensor_Y_acc= (TextView)findViewById(R.id.textView_sensor_Y_acc);

        ip_address=(EditText)findViewById(R.id.ip_addr);

        leftclick = findViewById(R.id.button_left);
        rightclick = findViewById(R.id.button_right);


        list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(25);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sensitivity=progress*0.99+1;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

    }

   public void onStart(View view)
    {
        super.onStart();

        on_bit=start;

        if(list.size()>0){
            sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_GAME);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }
       /* list2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(list2.size()>0){
            sm.registerListener(sel, (Sensor) list2.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
        }*/
        to_send=true;
        notConnected=false;
        System.out.println(SensorManager.SENSOR_DELAY_GAME);

    }


    public void onStop(View view) {
        on_bit=stop;
        textView_sensor_X_acc.setText(0.0 + "0");
        textView_sensor_Y_acc.setText(0.0 + "0");
        if(list.size()>0){
            sm.unregisterListener(sel);
        }
        //System.out.println(to_send);
        to_send=false;
        notConnected=false;
        //System.out.println(to_send);
        super.onStop();
    }
    @Override
    protected void onStop() {
        on_bit=stop;
        textView_sensor_X_acc.setText(0.0 + "0");
        textView_sensor_Y_acc.setText(0.0 + "0");
        if(list.size()>0){
            sm.unregisterListener(sel);
        }
        System.out.println(to_send);
        to_send=false;
        notConnected=false;
        System.out.println(to_send);
        super.onStop();
    }


    //send sensor data


    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    String response = HttpPost(urls[0]);      //if phone is not connected to sensor.py then this statement will throw an error.
                    notConnected=false;
                    return response;
                } catch (JSONException e) {
                    to_send=false;
                    notConnected=true;
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                to_send=false;
                notConnected=true;
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
        return conn.getResponseMessage()+"";

    }

    public void send(View view) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        String ip=ip_address.getText().toString();
            //new HTTPAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
            //new HTTPAsyncTask().execute(ip);//"http://10.146.121.148:8080/sensor";
           // new HTTPAsyncTask().execute("http://10.146.85.93:8080/sensor");
            //new HTTPAsyncTask().execute("http://10.146.94.129:8080/sensor");

            new HTTPAsyncTask().execute("http://"+ip+":8080/sensor");
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
        jsonObject.accumulate("y_tilt",  textView_sensor_Y_acc.getText().toString());
       // jsonObject.accumulate("zacc",  textView_sensor_Z_acc.getText().toString());
        jsonObject.accumulate("arrow", on_bit);
        jsonObject.accumulate("sensitivity", sensitivity);
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
