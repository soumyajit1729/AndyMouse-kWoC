package com.example.android.andymouse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    //extract data from sensors
    SensorManager sm = null;
    //int count=0;
    int on_bit=0;
    Boolean to_send=false;
    TextView textView_sensor_X_acc;
    TextView textView_sensor_Y_acc;
    TextView textView_sensor_Z_acc;
    //TextView textView_sensor_X_gyro;
   // TextView textView_sensor_Y_gyro;
   // TextView textView_sensor_Z_gyro;
    EditText ip_address;
    List list,list2;

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                //round up to 2 decimal places
                /*values[0]=Math.round(values[0] * 100.0f) / 100.0f;
                values[1]=Math.round(values[1] * 100.0f) / 100.0f;
                values[2]=Math.round(values[2] * 100.0f) / 100.0f;*/


                textView_sensor_X_acc.setText("" + (values[0]/values[2]));
                textView_sensor_Y_acc.setText("" + (values[1]/values[2]));
                textView_sensor_Z_acc.setText("" + values[2]);
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
            if(to_send)
           {
               send_loop();
           }



        }
    };


    public void rightClick(View view) throws java.lang.InterruptedException
    {

        on_bit=-1;
        TimeUnit.MILLISECONDS.sleep(250);

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;

    }
    public void leftClick(View view) throws java.lang.InterruptedException
    {
        on_bit=10000;
        TimeUnit.MILLISECONDS.sleep(250);

        send_loop();

        TimeUnit.MILLISECONDS.sleep(250);
        on_bit=0;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

   public void onStart(View view)
    {
        super.onStart();
        /* Get a SensorManager instance */

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        textView_sensor_X_acc = (TextView)findViewById(R.id.textView_sensor_X_acc);
        textView_sensor_Y_acc= (TextView)findViewById(R.id.textView_sensor_Y_acc);
        textView_sensor_Z_acc = (TextView)findViewById(R.id.textView_sensor_Z_acc);

        ip_address=(EditText)findViewById(R.id.ip_addr);


        list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(list.size()>0){
            sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL*(100000/3));
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
        System.out.println(SensorManager.SENSOR_DELAY_NORMAL);

    }


    public void onStop(View view) {
        if(list.size()>0){
            sm.unregisterListener(sel);
        }
        //System.out.println(to_send);
        to_send=false;
        //System.out.println(to_send);
        super.onStop();
    }
    @Override
    protected void onStop() {
        if(list.size()>0){
            sm.unregisterListener(sel);
        }
        System.out.println(to_send);
        to_send=false;
        System.out.println(to_send);
        super.onStop();
    }


    //send sensor data

    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            // show "Connected" & type of network "WIFI or MOBILE"


            System.out.println("is connected");


        } else {
            // show "Not Connected"

            System.out.println("is not connected");
        }

        return isConnected;
    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpPost(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
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
        if(checkNetworkConnection()) {
            //new HTTPAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
            //new HTTPAsyncTask().execute(ip);//"http://10.146.121.148:8080/sensor";
           // new HTTPAsyncTask().execute("http://10.146.85.93:8080/sensor");
            //new HTTPAsyncTask().execute("http://10.146.94.129:8080/sensor");
            new HTTPAsyncTask().execute("http://"+ip+":8080/sensor");


        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }
    public void send_loop()
    {
       // Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        String ip=ip_address.getText().toString();
        if(checkNetworkConnection()) {
            //new HTTPAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
           // new HTTPAsyncTask().execute("http://10.146.94.129:8080/sensor");
            new HTTPAsyncTask().execute("http://"+ip+":8080/sensor");//"http://10.145.170.91:8080/sensor";
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }

    private JSONObject buildJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("x_tilt", textView_sensor_X_acc.getText().toString());
        jsonObject.accumulate("y_tilt",  textView_sensor_Y_acc.getText().toString());
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
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }


}