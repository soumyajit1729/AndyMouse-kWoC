package com.example.android.andymouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Touchpad extends AppCompatActivity {

    float ptr1_x, ptr1_y, ptr1_x0, ptr1_y0, ptr1_dx=0, ptr1_dy=0;
    float ptr2_x, ptr2_y, ptr2_x0, ptr2_y0, ptr2_dx=0, ptr2_dy=0;
    long count_left_clicks, count_right_clicks, check = 0;
    int left_click, right_click = 0;
    Button leftclick, rightclick;
    int first_pointer=0;
    int second_pointer=0;
    int first_pointer_id;
    float scroll = 0;
    int flag = 0;
    int num = 10;
    boolean drag = false;
    String action = "touchpad";
    TextView touchpad;
    String ip_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        touchpad = findViewById(R.id.touchpad);
        leftclick = findViewById(R.id.button_left);
        rightclick = findViewById(R.id.button_right);

        Intent intent = getIntent();
        ip_address = intent.getStringExtra("ip_address");


        touchpad.setOnTouchListener(new View.OnTouchListener() {
            long startTime=0;
            long drag_startTime=0;
            @Override
            public boolean onTouch(View v, MotionEvent event){

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() == 1) {
                            if (System.nanoTime() - drag_startTime < 100000000) {
                                action = "drag";
                                drag = true;
                            }
                            first_pointer = event.getActionIndex();
                            first_pointer_id = event.getPointerId(event.getActionIndex());
                            ptr1_x0 = event.getX(first_pointer);
                            ptr1_y0 = event.getY(first_pointer);
                            ptr1_dx = 0;
                            ptr1_dy = 0;
                            check++;
                            send_loop();
                            startTime = System.nanoTime();
                        }
                        if (event.getPointerCount() == 2) {
                            second_pointer = event.getActionIndex();
                            ptr2_x0 = event.getX(second_pointer);
                            ptr2_y0 = event.getY(second_pointer);
                            ptr1_dx = 0;
                            ptr1_dy = 0;
                            ptr2_dx = 0;
                            ptr2_dy = 0;
                            check++;
                            send_loop();
                            flag = 1;
                            num = 10;
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        if (event.getPointerCount() == 1) {
                            long difference = System.nanoTime() - startTime;
                            if (difference < 200000000) {
                                drag_startTime = System.nanoTime();
                                if (flag == 1) {
                                    count_right_clicks++;
                                    send_loop();
                                } else {
                                    count_left_clicks++;
                                    send_loop();
                                    drag_startTime = System.nanoTime();
                                }
                            }
                            flag = 0;
                            scroll = 0;
                            drag = false;
                            ptr1_dx = 0;
                            ptr1_dy = 0;
                            check++;
                            send_loop();
                            action = "touchpad";

                        }
                        if (event.getPointerCount() == 2) {
                            int new_pointer;
                            if (event.getPointerId(event.getActionIndex()) == first_pointer_id) {
                                new_pointer = event.getActionIndex() == 0 ? 1 : 0;
                                ptr1_x0 = event.getX(new_pointer);
                                ptr1_y0 = event.getY(new_pointer);
                            }
                            else{
                                ptr1_x0 = event.getX(first_pointer);
                                ptr1_y0 = event.getY(first_pointer);
                            }
                            ptr1_dx = 0;
                            ptr1_dy = 0;
                            scroll = 0;
                            check++;
                            send_loop();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount()==1) {
                            ptr1_x = event.getX(first_pointer);
                            ptr1_y = event.getY(first_pointer);
                            ptr1_dx = (ptr1_x - ptr1_x0);
                            ptr1_dy = (ptr1_y - ptr1_y0);
                            send_loop();
                        }
                        if (event.getPointerCount() == 2) {
                            ptr1_x = event.getX(first_pointer);
                            ptr1_y = event.getY(first_pointer);
                            ptr2_x = event.getX(second_pointer);
                            ptr2_y = event.getY(second_pointer);
                            ptr1_dx = ptr1_x - ptr1_x0;
                            ptr1_dy = ptr1_y - ptr1_y0;
                            ptr2_dx = ptr2_x - ptr2_x0;
                            ptr2_dy = ptr2_y - ptr2_y0;
                            if (Math.abs((ptr1_dy + ptr2_dy)/2) > Math.abs(scroll)){
                                scroll = (ptr1_dy + ptr2_dy)/2;
                            }
                            else {
                                ptr1_x0 = event.getX(first_pointer);
                                ptr1_y0 = event.getY(first_pointer);
                                ptr2_x0 = event.getX(second_pointer);
                                ptr2_y0 = event.getY(second_pointer);
                                ptr1_dx = 0;
                                ptr1_dy = 0;
                                ptr2_dx = 0;
                                ptr2_dy = 0;
                                scroll = 0;
                                num = 10;
                            }
                            ptr1_dx = 0;
                            ptr1_dy = 0;
                            if (Math.abs((int)(scroll/num)) == 1){
                                send_loop();
                                num += 10;
                            }
                        }

                        break;
                }

                return true;
            }

        });

        leftclick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        left_click = 1;
                        action = "button";
                        send_loop();
                        break;
                    case MotionEvent.ACTION_UP:
                        left_click = 0;
                        send_loop();
                        action = "touchpad";
                        break;
                }
                return true;
            }
        });

        rightclick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        right_click = 1;
                        action = "button";
                        send_loop();
                        break;
                    case MotionEvent.ACTION_UP:
                        right_click = 0;
                        send_loop();
                        action = "touchpad";
                        break;
                }
                return true;
            }
        });


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int getaction,keycode;

        getaction = event.getAction();
        keycode = event.getKeyCode();

        switch (keycode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (getaction == KeyEvent.ACTION_DOWN) {
                    left_click = 1;
                    action = "button";
                    send_loop();
                } else if (getaction == KeyEvent.ACTION_UP) {
                    left_click = 0;
                    send_loop();
                    action = "touchpad";
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (getaction == KeyEvent.ACTION_DOWN) {
                    right_click = 1;
                    action = "button";
                    send_loop();
                } else if (getaction == KeyEvent.ACTION_UP) {
                    right_click = 0;
                    send_loop();
                    action = "touchpad";
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        check++;
        send_loop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        check++;
        send_loop();
    }


    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    String response = HttpPost(urls[0]);
                    return response;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
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

    public void send_loop() {
        String ip = ip_address;
        new HTTPAsyncTask().execute("http://" + ip + ":8080/touchpad");

    }
    private JSONObject buildJsonObject()throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("check", check+"");
        jsonObject.accumulate("count_left_clicks", count_left_clicks+"");
        jsonObject.accumulate("count_right_clicks", count_right_clicks+"");
        jsonObject.accumulate("dx", ptr1_dx+"");
        jsonObject.accumulate("dy", ptr1_dy+"");
        jsonObject.accumulate("scroll", scroll+"");
        jsonObject.accumulate("drag", drag+"");
        jsonObject.accumulate("left_click", left_click+"");
        jsonObject.accumulate("right_click", right_click+"");
        jsonObject.accumulate("action", action+"");

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