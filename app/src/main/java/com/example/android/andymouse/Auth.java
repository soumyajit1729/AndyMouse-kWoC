package com.example.android.andymouse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Auth extends AppCompatActivity {

    EditText ip_address, password;
    Button connect, touchpad, accelerometer;
    boolean isConnected = false;
    int flag = 0;
    String Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ip_address = findViewById(R.id.ip_address);
        password = findViewById(R.id.password);
        connect = findViewById(R.id.connect);
        touchpad = findViewById(R.id.touchpad);
        accelerometer = findViewById(R.id.accelerometer);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send_loop();
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    Log.i("Error_message", "Interrupted exception");
                }
                if (isConnected) {
                    Url = "http://" + ip_address.getText().toString() + ":8080/";
                    new Auth.checkpass().execute();
                    try {
                        TimeUnit.MILLISECONDS.sleep(250);
                    } catch (InterruptedException e) {
                        Log.i("Error_message", "Interrupted exception");
                    }
                    if (flag == 1) {
                        Toast.makeText(Auth.this, "Connected", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Auth.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Auth.this, "Not Connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        accelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == 1) {
                    Intent intent = new Intent(Auth.this, MainActivity.class);
                    intent.putExtra("ip_address", ip_address.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(Auth.this, "Not Connected", Toast.LENGTH_SHORT).show();
                }

            }
        });

        touchpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == 1) {
                    Intent intent = new Intent(Auth.this, Touchpad.class);
                    intent.putExtra("ip_address", ip_address.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(Auth.this, "Not Connected", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    String response = HttpPost(urls[0]);      //if phone is not connected to sensor.py then this statement will throw an error.
                    isConnected = true;
                    return response;
                } catch (JSONException e) {
                    isConnected = false;
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                isConnected = false;
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            System.out.println("post request executed");
        }
    }

    public class checkpass extends AsyncTask<Void,Void,Void> {
        String data ="";
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }

                JSONObject jsonObject = new JSONObject(data);
                flag = Integer.parseInt(jsonObject.get("flag")+"");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

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

    public void send_loop() {
        String ip = ip_address.getText().toString();
        new Auth.HTTPAsyncTask().execute("http://" + ip + ":8080/");
    }
    private JSONObject buildJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("password", password.getText().toString());

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

