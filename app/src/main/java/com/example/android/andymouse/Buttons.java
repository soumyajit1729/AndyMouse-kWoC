package com.example.android.andymouse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Buttons extends AppCompatActivity {

    Button accelerometer,touchpad;
    String ip_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons);

        accelerometer = findViewById(R.id.accelerometer);
        touchpad = findViewById(R.id.touchpad);

        Intent my_intent = getIntent();
        ip_address = my_intent.getStringExtra("ip_address");

        accelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Buttons.this, MainActivity.class);
                intent.putExtra("ip_address", ip_address);
                startActivity(intent);
            }
        });

        touchpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Buttons.this, Touchpad.class);
                intent.putExtra("ip_address", ip_address);
                startActivity(intent);
            }
        });
    }
}
